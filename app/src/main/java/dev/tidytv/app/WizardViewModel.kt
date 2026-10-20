package dev.tidytv.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.tidytv.app.data.KnownTv
import dev.tidytv.app.data.Store
import dev.tidytv.app.data.Trace
import dev.tidytv.app.net.Backend
import dev.tidytv.app.net.DemoBackend
import dev.tidytv.core.flow.Attempt
import dev.tidytv.core.flow.ConnectEvent
import dev.tidytv.core.flow.ConnectMachine
import dev.tidytv.core.flow.ConnectState
import dev.tidytv.core.flow.FoundTv
import dev.tidytv.core.flow.Mode
import dev.tidytv.core.flow.Step
import dev.tidytv.core.flow.Wizard
import dev.tidytv.core.flow.WizardEvent
import dev.tidytv.core.flow.WizardReducer
import dev.tidytv.core.lists.AppLabels
import dev.tidytv.core.lists.Candidate
import dev.tidytv.core.lists.DebloatList
import dev.tidytv.core.lists.Group
import dev.tidytv.core.lists.Grouping
import dev.tidytv.core.lists.HomeApp
import dev.tidytv.core.lists.Launchers
import dev.tidytv.core.lists.ListCatalog
import dev.tidytv.core.lists.ListEntry
import dev.tidytv.core.lists.ModelMatcher
import dev.tidytv.core.lists.Protected
import dev.tidytv.core.ops.ActionLog
import dev.tidytv.core.ops.ActionRecord
import dev.tidytv.core.ops.ApkFetcher
import dev.tidytv.core.ops.DebloatOutcome
import dev.tidytv.core.ops.Debloater
import dev.tidytv.core.ops.Finisher
import dev.tidytv.core.ops.HandoverOutcome
import dev.tidytv.core.ops.LauncherHandover
import dev.tidytv.core.ops.PkgResult
import dev.tidytv.core.ops.RestoreOutcome
import dev.tidytv.core.ops.Restorer
import dev.tidytv.core.ops.TvConnectionLost
import dev.tidytv.core.ops.TvReader
import dev.tidytv.core.ops.TvShell
import dev.tidytv.core.ops.TvSnapshot
import dev.tidytv.core.ops.WraithCheck
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

data class TvSummary(val title: String, val model: String, val host: String, val serial: String, val sdk: Int, val release: String)

sealed interface BackupStatus {
    data object Running : BackupStatus
    data object Saved : BackupStatus
    data class Failed(val message: String) : BackupStatus
}

data class ReviewState(
    val backup: BackupStatus = BackupStatus.Running,
    val listTitle: String? = null,
    /** False: a similar model's list, not tested on this exact TV. */
    val exact: Boolean = false,
    val candidates: List<Candidate> = emptyList(),
    val selected: Set<String> = emptySet(),
    /** Installed apps the list mentions that TidyTV will always leave on. */
    val kept: List<String> = emptyList(),
    /** No list matches this TV: backup only, nothing to turn off. */
    val noList: Boolean = false,
)

data class TidyState(
    val order: List<Candidate> = emptyList(),
    val results: Map<String, PkgResult> = emptyMap(),
    val saving: Boolean = false,
    val reconnecting: Boolean = false,
    val stopReason: String? = null,
    val finished: Boolean = false,
) {
    val disabledCount get() = results.values.count { it is PkgResult.Disabled }
}

data class DockApp(val pkg: String, val label: String)

enum class HomeAction { REPLACE_COPY, TURN_OFF_SETUP_APP, RETRY }

sealed interface HomeState {
    data class Offer(
        val options: List<DockApp> = emptyList(),
        val chosen: List<String> = emptyList(),
        val app: HomeApp = HomeApp.SIMPLE,
        /** Home screens this TV can run (the styled one needs Android 8). */
        val available: List<HomeApp> = listOf(HomeApp.SIMPLE),
    ) : HomeState
    data object Running : HomeState
    data class Problem(val message: String, val worried: Boolean, val action: HomeAction? = null, val rescue: List<String> = emptyList()) : HomeState
    data class Installed(val app: HomeApp = HomeApp.SIMPLE) : HomeState
}

enum class DebugCheck { UNKNOWN, CHECKING, STILL_ON, OFF }

sealed interface RestoreUi {
    data object Idle : RestoreUi
    data class Running(val count: Int) : RestoreUi
    data class Finished(val message: String, val ok: Boolean) : RestoreUi
}

data class DoneState(
    val freedMb: Int? = null,
    val newHome: Boolean = false,
    val debugging: DebugCheck = DebugCheck.UNKNOWN,
    val records: List<ActionRecord> = emptyList(),
    val restore: RestoreUi = RestoreUi.Idle,
)

data class UiState(
    val wizard: Wizard = Wizard(),
    val connect: ConnectState = ConnectState.Scanning(),
    val tv: TvSummary? = null,
    val review: ReviewState = ReviewState(),
    val tidy: TidyState = TidyState(),
    val home: HomeState = HomeState.Offer(),
    val done: DoneState = DoneState(),
    /** A plain-language message for something that went wrong outside a specific step. */
    val problem: String? = null,
    /** Friendly names from the list notes, so the activity list reads like the review screen. */
    val labels: Map<String, String> = emptyMap(),
    /** The real app icons read from the TV, as PNG files on the phone, by package. */
    val icons: Map<String, String> = emptyMap(),
)

/**
 * Drives the whole flow. The screens only read [ui] and call the public functions; every TV
 * change goes through core (Debloater, LauncherHandover, Restorer), one at a time ([tvLock]).
 */
class WizardViewModel(
    private val store: Store,
    private val trace: Trace,
    private val real: () -> Backend,
    private val demo: DemoBackend?,
    private val lists: () -> ListCatalog,
    private val launcherApk: (HomeApp) -> ByteArray?,
    /** Turns an APK fetched from the TV into a PNG of its icon; returns the file path. Android only. */
    private val iconFromApk: ((pkg: String, apk: ByteArray) -> String?)? = null,
) : ViewModel() {
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var backend: Backend? = null
    private var shell: TvShell? = null
    private var snapshot: TvSnapshot? = null
    private var list: DebloatList? = null
    private var log: ActionLog? = null
    private var found: FoundTv? = null
    private val tvLock = Mutex()

    private var scanJob: Job? = null
    private var connectJob: Job? = null
    private var allowTimer: Job? = null

    // ---------- Wizard ----------

    private fun wizard(e: WizardEvent) {
        val before = _ui.value.wizard
        val next = WizardReducer.reduce(before, e) ?: return
        if (next == before) return
        trace.line("wizard ${before.step} -> ${next.step} (${e::class.simpleName})")
        _ui.update { it.copy(wizard = next) }
        if (before.step != next.step) onEnter(next.step, before.step)
    }

    private fun onEnter(step: Step, from: Step) {
        when (step) {
            Step.CONNECT -> if (from != Step.REVIEW || shell == null) connect(ConnectEvent.StartScan)
            Step.WELCOME -> disconnect()
            Step.DONE -> enterDone()
            Step.HOME_SCREEN -> enterHome()
            else -> Unit
        }
    }

    fun choose(kind: dev.tidytv.core.flow.TvKind) = wizard(WizardEvent.Choose(kind))
    fun restoreTapped() = wizard(WizardEvent.RestoreTapped)
    fun ready() = wizard(WizardEvent.Ready)
    fun startOver() = wizard(WizardEvent.StartOver)
    fun toDone() = wizard(WizardEvent.ToDone)
    fun dismissProblem() = _ui.update { it.copy(problem = null) }

    /** Returns false when Back should close the app. */
    fun back(): Boolean {
        val s = _ui.value
        if (s.wizard.step == Step.CONNECT && s.connect !is ConnectState.Scanning && s.connect !is ConnectState.Connected) {
            connect(ConnectEvent.Back); return true
        }
        val next = WizardReducer.reduce(s.wizard, WizardEvent.Back) ?: return false
        if (s.wizard.step == Step.REVIEW && next.step == Step.CONNECT) {
            disconnect()
            _ui.update { it.copy(review = ReviewState()) }
        }
        wizard(WizardEvent.Back)
        return true
    }

    // ---------- Connect ----------

    fun connect(e: ConnectEvent) {
        val old = _ui.value.connect
        val new = ConnectMachine.reduce(old, e)
        if (new == old && e !is ConnectEvent.StartScan) return
        if (new::class != old::class) trace.line("connect ${old::class.simpleName} -> ${new::class.simpleName}")
        _ui.update { it.copy(connect = new) }

        if (new !is ConnectState.Scanning) scanJob?.cancel()
        if (new is ConnectState.Scanning && (e is ConnectEvent.StartScan || e is ConnectEvent.Back || old !is ConnectState.Scanning)) startScan()
        if (new is ConnectState.Connecting && (old !is ConnectState.Connecting || old.tv != new.tv)) startConnect(new.tv)
        if (new is ConnectState.Pairing && new.busy && !(old is ConnectState.Pairing && old.busy)) startPair(new.tv, (e as ConnectEvent.SubmitCode).code)
        if (new is ConnectState.PressAllow && old is ConnectState.Connecting) {
            allowTimer?.cancel()
            allowTimer = viewModelScope.launch { delay(30_000); connect(ConnectEvent.AllowRetryAvailable) }
        }
        if (new !is ConnectState.PressAllow) allowTimer?.cancel()
    }

    private fun startScan() {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            launch { delay(3_500); connect(ConnectEvent.ScanDone) }
            demo?.let { d -> launch { d.scan().collect { connect(ConnectEvent.Found(it)) } } }
            withTimeoutOrNull(20_000) {
                runCatching { real().scan().collect { connect(ConnectEvent.Found(it)) } }
                    .onFailure { if (it is kotlinx.coroutines.CancellationException) throw it; trace.line("scan failed: ${it.message}") }
            }
        }
    }

    private fun backendFor(tv: FoundTv): Backend = if (demo != null && tv.host == DemoBackend.DEMO.host) demo else real()

    private fun startConnect(tv: FoundTv) {
        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            val b = backendFor(tv)
            val link = b.connect(tv) { connect(ConnectEvent.AwaitingAllow) }
            if (link.attempt == Attempt.CONNECTED && link.shell != null) onLinked(b, tv, link.shell)
            connect(ConnectEvent.Result(link.attempt, link.message))
        }
    }

    private fun startPair(tv: FoundTv, code: String) {
        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            val b = backendFor(tv)
            val link = b.pair(tv, code)
            if (link.attempt == Attempt.CONNECTED && link.shell != null) onLinked(b, tv, link.shell)
            connect(ConnectEvent.Result(link.attempt, link.message))
        }
    }

    private suspend fun onLinked(b: Backend, tv: FoundTv, s: TvShell) {
        backend = b
        shell = s
        found = tv
        val snap = try {
            tvLock.withLock { TvReader.snapshot(s) }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            trace.line("snapshot failed: $e")
            _ui.update { it.copy(problem = "Connected, but the TV didn't answer the way TidyTV expected. Tap Share logs so we can fix it.") }
            return
        }
        snapshot = snap
        log = store.actionLog(snap.info.serial)
        val summary = TvSummary(
            title = tv.name ?: snap.info.displayName, model = tv.model ?: snap.info.displayName, host = tv.host,
            serial = snap.info.serial, sdk = snap.info.sdk, release = snap.info.release,
        )
        store.remember(KnownTv(summary.serial, summary.title, summary.model, tv.host, System.currentTimeMillis()))
        trace.line("tv ${snap.info.displayName} sdk ${snap.info.sdk} home ${snap.home}")
        _ui.update { it.copy(tv = summary) }
        val fire = snap.info.manufacturer.equals("Amazon", true) || snap.info.brand.equals("Amazon", true)
        val mode = _ui.value.wizard.mode
        wizard(WizardEvent.Connected(fireOs = fire))
        if (mode == Mode.CLEAN) prepareReview(snap) else loadLabels(snap)
    }

    /** Restore mode skips the review, but the activity list should still use the list's names. */
    private fun loadLabels(snap: TvSnapshot) {
        val l = ModelMatcher.match(snap.info, lists().lists).firstOrNull()?.list ?: return
        list = l
        val names = (l.entries + l.optional).associate { it.pkg to AppLabels.label(it.pkg, it.note) }
        _ui.update { it.copy(labels = it.labels + names) }
    }

    private fun disconnect() {
        scanJob?.cancel(); connectJob?.cancel(); allowTimer?.cancel()
        backend?.close(shell)
        shell = null
        snapshot = null
        _ui.update { UiState(wizard = it.wizard) }
    }

    /**
     * Gets the session back after Wi-Fi blips. Tries the same address, then looks for the TV by
     * name in case the router gave it a new one. The saved key means no new Allow prompt.
     */
    private suspend fun reconnect(): Boolean {
        val b = backend ?: return false
        var tv = found ?: return false
        backend?.close(shell)
        shell = null
        trace.line("reconnecting to ${tv.host}")
        repeat(8) { attempt ->
            val link = b.connect(tv) {}
            if (link.attempt == Attempt.CONNECTED && link.shell != null) {
                shell = link.shell
                trace.line("reconnected after ${attempt + 1} tries")
                return true
            }
            if (attempt == 3 && tv.name != null) {
                val name = tv.name
                val moved = withTimeoutOrNull(6_000) { b.scan().first { f -> f.name == name && f.host != tv.host } }
                if (moved != null) { tv = tv.copy(host = moved.host); found = tv; trace.line("tv moved to ${tv.host}") }
            }
            delay(3_000)
        }
        trace.line("reconnect gave up")
        return false
    }

    // ---------- Review ----------

    private fun prepareReview(snap: TvSnapshot) {
        viewModelScope.launch {
            _ui.update { it.copy(review = ReviewState(backup = BackupStatus.Running)) }
            val s = shell ?: return@launch
            try {
                val backup = tvLock.withLock { TvReader.backup(s, snap, System.currentTimeMillis()) }
                store.saveBackup(backup)
                trace.line("backup saved: ${backup.packages.size} packages, ${backup.disabled.size} already off")
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                trace.line("backup failed: $e")
                _ui.update { it.copy(review = it.review.copy(backup = BackupStatus.Failed(
                    if (e is TvConnectionLost) "The TV stopped answering while saving the backup." else "The backup couldn't be saved.",
                ))) }
                return@launch
            }
            val match = ModelMatcher.match(snap.info, lists().lists).firstOrNull()
            list = match?.list
            if (match == null) {
                trace.line("no list for ${snap.info.displayName}")
                _ui.update { it.copy(review = it.review.copy(backup = BackupStatus.Saved, noList = true)) }
                return@launch
            }
            trace.line("list ${match.list.id} score ${match.score}")
            val candidates = Grouping.candidates(match.list, snap.installed - snap.disabled, snap.liveProtected)
            val kept = (match.list.entries + match.list.optional).asSequence()
                .filter { it.pkg in snap.installed }
                .filter { Protected.isProtected(it.pkg, snap.liveProtected, match.list.keep) || Launchers.isHomeApp(it.pkg) }
                .map { AppLabels.label(it.pkg, it.note) }.distinct().sorted().toList()
            fetchIcons(candidates.map { c -> c.pkg })
            _ui.update {
                it.copy(labels = it.labels + candidates.associate { c -> c.pkg to c.label }, review = ReviewState(
                    backup = BackupStatus.Saved, listTitle = match.list.title, exact = match.exact,
                    candidates = candidates, selected = candidates.filter { c -> c.selected }.map { c -> c.pkg }.toSet(), kept = kept,
                ))
            }
        }
    }

    fun retryBackup() { snapshot?.let { prepareReview(it) } }

    private var iconJob: Job? = null

    /**
     * Reads each app's real icon from its APK on the TV, small APKs first-come, while the user reviews.
     * Runs between other TV work (one pull at a time under [tvLock]) and is simply skipped where it fails.
     */
    private fun fetchIcons(pkgs: List<String>) {
        val toIcon = iconFromApk ?: return
        iconJob?.cancel()
        iconJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            for (pkg in pkgs.distinct()) {
                if (pkg in _ui.value.icons) continue
                store.cachedIcon(pkg)?.let { path -> _ui.update { it.copy(icons = it.icons + (pkg to path)) }; continue }
                val s = shell ?: return@launch
                val apk = tvLock.withLock { ApkFetcher(s).fetch(pkg) }
                if (apk == null) { trace.line("icon $pkg: no small APK on the TV"); continue }
                val path = runCatching { toIcon(pkg, apk) }.onFailure { trace.line("icon $pkg: ${it.javaClass.simpleName}") }.getOrNull()
                if (path == null) { trace.line("icon $pkg: no icon in the APK (${apk.size} bytes)"); continue }
                _ui.update { it.copy(icons = it.icons + (pkg to path)) }
            }
            trace.line("icons: ${_ui.value.icons.size} read from the TV")
        }
    }

    fun toggleApp(pkg: String) = _ui.update {
        val sel = it.review.selected
        it.copy(review = it.review.copy(selected = if (pkg in sel) sel - pkg else sel + pkg))
    }

    fun toggleGroup(group: Group, on: Boolean) = _ui.update {
        val pkgs = it.review.candidates.filter { c -> c.group == group }.map { c -> c.pkg }.toSet()
        it.copy(review = it.review.copy(selected = if (on) it.review.selected + pkgs else it.review.selected - pkgs))
    }

    // ---------- Tidying ----------

    fun startTidy() {
        val r = _ui.value.review
        if (r.backup != BackupStatus.Saved || r.selected.isEmpty()) return
        iconJob?.cancel() // the clean-up comes first; missing icons keep their fallback
        val order = r.candidates.filter { it.pkg in r.selected }
        _ui.update { it.copy(tidy = TidyState(order = order)) }
        wizard(WizardEvent.StartTidy)
        viewModelScope.launch { runTidy(order.map { it.pkg }) }
    }

    private suspend fun runTidy(pkgs: List<String>) {
        val snap = snapshot ?: return
        val lg = log ?: return
        val b = backend ?: return
        while (true) {
            val s = shell ?: if (reconnect()) shell!! else null
            if (s == null) {
                _ui.update { it.copy(tidy = it.tidy.copy(reconnecting = false, stopReason = "TidyTV lost the connection to your TV and couldn't get it back. Everything done so far is saved, and Restore puts it back.")) }
                wizard(WizardEvent.TidyEnded(stopped = true, launcherPossible = false))
                return
            }
            _ui.update { it.copy(tidy = it.tidy.copy(reconnecting = false)) }
            val debloater = Debloater(s, lg, sleep = { _ ->
                _ui.update { it.copy(tidy = it.tidy.copy(saving = true)) }
                delay(b.saveWaitMs)
            })
            val outcome = tvLock.withLock {
                debloater.run(pkgs, snap.liveProtected, list?.keep.orEmpty()) { r -> record(r) }
            }
            trace.line("clean-up: ${outcome::class.simpleName}, ${outcome.disabledCount} off")
            when (outcome) {
                is DebloatOutcome.Finished -> {
                    _ui.update { it.copy(tidy = it.tidy.copy(saving = false, finished = true)) }
                    wizard(WizardEvent.TidyEnded(stopped = false, launcherPossible = launcherPossible()))
                    return
                }
                is DebloatOutcome.Stopped -> {
                    _ui.update { it.copy(tidy = it.tidy.copy(saving = false, stopReason = outcome.reason)) }
                    wizard(WizardEvent.TidyEnded(stopped = true, launcherPossible = false))
                    return
                }
                is DebloatOutcome.Disconnected -> {
                    // Resume: the Debloater re-reads the TV, so finished packages come back as AlreadyOff.
                    _ui.update { it.copy(tidy = it.tidy.copy(reconnecting = true)) }
                    shell = null
                }
            }
        }
    }

    /** A package that was turned off before a reconnect stays "turned off", not "was already off". */
    private fun record(r: PkgResult) = _ui.update {
        val prev = it.tidy.results[r.pkg]
        val keep = prev is PkgResult.Disabled && r is PkgResult.AlreadyOff
        it.copy(tidy = it.tidy.copy(results = if (keep) it.tidy.results else it.tidy.results + (r.pkg to r)))
    }

    private fun launcherPossible(): Boolean {
        val snap = snapshot ?: return false
        if (launcherApk(HomeApp.SIMPLE) == null) return false
        val homePkg = snap.home?.substringBefore('/')
        return homePkg in Launchers.KNOWN_STOCK || homePkg == "android"
    }

    // ---------- Home screen ----------

    private fun enterHome() {
        val snap = snapshot ?: return
        val options = (snap.installed - snap.disabled)
            .filter { Grouping.groupOf(ListEntry(it, "", "")) == Group.STREAMING }
            .filter { it !in _ui.value.tidy.results.filterValues { r -> r is PkgResult.Disabled }.keys }
            .map { DockApp(it, AppLabels.label(it)) }
            .sortedBy { it.label.lowercase() }
        val available = HomeApp.entries.filter { a -> snap.info.sdk >= a.minSdk && launcherApk(a) != null }
        _ui.update { it.copy(home = HomeState.Offer(options, options.take(8).map { o -> o.pkg }, HomeApp.SIMPLE, available)) }
        fetchIcons(options.map { it.pkg })
    }

    fun chooseHome(app: HomeApp) = _ui.update {
        val h = it.home as? HomeState.Offer ?: return@update it
        if (app in h.available) it.copy(home = h.copy(app = app)) else it
    }

    fun toggleDock(pkg: String) = _ui.update {
        val h = it.home as? HomeState.Offer ?: return@update it
        it.copy(home = h.copy(chosen = if (pkg in h.chosen) h.chosen - pkg else h.chosen + pkg))
    }

    fun skipHome() = wizard(WizardEvent.HomeScreenDone)

    fun installHome() {
        val offer = _ui.value.home as? HomeState.Offer
        lastDock = offer?.chosen ?: lastDock
        lastApp = offer?.app ?: lastApp
        val dock = lastDock
        runHome { h, apk, sdk -> h.run(apk, dock, sdk) }
    }

    private var lastDock: List<String> = emptyList()
    private var lastApp: HomeApp = HomeApp.SIMPLE

    fun homeAction(action: HomeAction, onEthernet: Boolean = false) {
        when (action) {
            HomeAction.RETRY -> runHome { h, apk, sdk -> h.run(apk, lastDock, sdk) }
            HomeAction.REPLACE_COPY -> runHome { h, apk, sdk ->
                if (h.removeConflictingCopy()) h.run(apk, lastDock, sdk)
                else HandoverOutcome.InstallFailed("The other copy couldn't be removed.")
            }
            HomeAction.TURN_OFF_SETUP_APP -> runHome { h, apk, sdk ->
                when (val c = h.disableSetupWraith(onEthernet)) {
                    WraithCheck.Ok -> h.run(apk, lastDock, sdk)
                    else -> wraithMessage(c)
                }
            }
        }
    }

    private class Blocked(val why: String) : RuntimeException(why)

    private fun wraithMessage(c: WraithCheck): HandoverOutcome = throw Blocked(
        when (c) {
            WraithCheck.OwnsWifi -> "Your TV's Wi-Fi was set up by the Google TV setup app, so turning it off would drop the TV off Wi-Fi. Your old home screen stays."
            WraithCheck.SetupIncomplete -> "Your TV hasn't finished its first setup, so the setup app has to stay on. Your old home screen stays."
            WraithCheck.Persistent -> "The setup app can't be turned off on this TV. Your old home screen stays."
            is WraithCheck.Unknown -> "TidyTV couldn't check the setup app safely (${c.why}), so it left it on. Your old home screen stays."
            WraithCheck.Ok -> ""
        },
    )

    private fun runHome(block: suspend (LauncherHandover, ByteArray, Int) -> HandoverOutcome) {
        val app = lastApp
        val apk = launcherApk(app) ?: return
        val lg = log ?: return
        val b = backend ?: return
        val sdk = _ui.value.tv?.sdk ?: 0
        wizard(WizardEvent.Busy(true))
        _ui.update { it.copy(home = HomeState.Running) }
        viewModelScope.launch {
            val outcome = try {
                var result: HandoverOutcome
                while (true) {
                    val s = shell ?: if (reconnect()) shell!! else null
                    if (s == null) { result = HandoverOutcome.Disconnected; break }
                    val h = LauncherHandover(s, lg, sleep = { delay(b.saveWaitMs) }, app = app)
                    result = tvLock.withLock { block(h, apk, sdk) }
                    if (result != HandoverOutcome.Disconnected) break
                    shell = null
                    if (!reconnect()) break
                }
                result
            } catch (e: Blocked) {
                _ui.update { it.copy(home = HomeState.Problem(e.why, worried = false)) }
                wizard(WizardEvent.Busy(false))
                return@launch
            }
            trace.line("home screen: $outcome")
            _ui.update { it.copy(home = homeState(outcome, app)) }
            wizard(WizardEvent.Busy(false))
        }
    }

    private fun homeState(o: HandoverOutcome, app: HomeApp): HomeState = when (o) {
        is HandoverOutcome.Done -> HomeState.Installed(app)
        is HandoverOutcome.NeedsChoice -> HomeState.Problem("Your TV has more than one home screen app, so TidyTV left Home alone.", worried = false)
        is HandoverOutcome.UnknownHome -> HomeState.Problem("Your TV uses a home screen TidyTV doesn't know, so it left Home alone.", worried = false)
        HandoverOutcome.SignatureConflict -> HomeState.Problem(
            "Another copy of this home screen is already on your TV. Replace it with TidyTV's?", worried = false, action = HomeAction.REPLACE_COPY,
        )
        is HandoverOutcome.InstallBlocked -> HomeState.Problem("This TV doesn't allow extra home screens. The clean-up still worked.", worried = false)
        is HandoverOutcome.InstallFailed -> HomeState.Problem("The home screen didn't install, so nothing about Home changed.", worried = false, action = HomeAction.RETRY)
        is HandoverOutcome.StartFailed -> HomeState.Problem("The new home screen didn't open, so TidyTV left your old one on.", worried = false, action = HomeAction.RETRY)
        is HandoverOutcome.SetupWraithTookHome -> HomeState.Problem(
            if (o.rolledBack) "Google TV's setup app grabbed Home, so TidyTV put your old home screen back. It can turn the setup app off and try again."
            else "Google TV's setup app grabbed Home and putting your old home screen back didn't finish. Tap Restore everything.",
            worried = !o.rolledBack, action = if (o.rolledBack) HomeAction.TURN_OFF_SETUP_APP else null,
        )
        is HandoverOutcome.RolledBack -> HomeState.Problem("Home didn't switch over, so TidyTV put your old home screen back.", worried = false, action = HomeAction.RETRY)
        is HandoverOutcome.RollbackFailed -> HomeState.Problem(
            "Home didn't switch over and putting the old one back didn't stick. Your TV still works; tap Restore everything, or share logs with us.",
            worried = true, rescue = o.rescue,
        )
        HandoverOutcome.Disconnected -> HomeState.Problem("The TV stopped answering. Check it's on, then try again.", worried = true, action = HomeAction.RETRY)
    }

    fun homeContinue() = wizard(WizardEvent.HomeScreenDone)

    // ---------- Done / My TVs ----------

    private fun enterDone() {
        val lg = log
        _ui.update { it.copy(done = it.done.copy(records = lg?.records().orEmpty(), newHome = it.home is HomeState.Installed)) }
        if (_ui.value.wizard.mode == Mode.CLEAN) viewModelScope.launch {
            val s = shell ?: return@launch
            val report = runCatching { tvLock.withLock { Finisher.measure(s, snapshot?.memAvailableKb) } }.getOrNull()
            _ui.update { it.copy(done = it.done.copy(freedMb = report?.freedMb)) }
        }
    }

    fun checkDebuggingOff() {
        val b = backend ?: return
        val host = found?.host ?: return
        _ui.update { it.copy(done = it.done.copy(debugging = DebugCheck.CHECKING)) }
        viewModelScope.launch {
            b.close(shell); shell = null
            val off = Finisher.debuggingOff { b.portOpen(host) }
            trace.line("debugging off: $off")
            _ui.update { it.copy(done = it.done.copy(debugging = if (off) DebugCheck.OFF else DebugCheck.STILL_ON)) }
        }
    }

    fun restoreEverything() {
        val lg = log ?: return
        if (_ui.value.done.restore is RestoreUi.Running) return
        wizard(WizardEvent.Busy(true))
        _ui.update { it.copy(done = it.done.copy(restore = RestoreUi.Running(0))) }
        viewModelScope.launch {
            var count = 0
            var outcome: RestoreOutcome? = null
            for (attempt in 0..1) {
                val s = shell ?: if (reconnect()) shell!! else null
                if (s == null) break
                outcome = tvLock.withLock {
                    Restorer(s, lg).run { count++; _ui.update { it.copy(done = it.done.copy(restore = RestoreUi.Running(count))) } }
                }
                if (outcome !is RestoreOutcome.Disconnected) break
                shell = null
            }
            trace.line("restore: $outcome")
            val ui = when (outcome) {
                is RestoreOutcome.Done -> {
                    // The old launcher coming back is "old home screen back", not one more app.
                    val apps = outcome.enabled.count { !dev.tidytv.core.lists.Launchers.isHomeApp(it) }
                    val home = outcome.homeRestored
                    RestoreUi.Finished(
                        when {
                            apps == 0 -> "Nothing to put back: every app is already on" + if (home) " and your old home screen is set." else "."
                            else -> "Everything is back the way it was: $apps ${if (apps == 1) "app" else "apps"} on" + if (home) ", old home screen back." else "."
                        },
                        ok = true,
                    )
                }
                is RestoreOutcome.Partial -> RestoreUi.Finished(
                    "${outcome.stillOff.size} apps wouldn't turn back on, so TidyTV kept your current home screen. Tap Restore everything again.", ok = false,
                )
                else -> RestoreUi.Finished("The TV stopped answering. Check it's on and on the same Wi-Fi, then tap Restore everything again.", ok = false)
            }
            _ui.update { it.copy(done = it.done.copy(restore = ui, records = lg.records(), debugging = DebugCheck.UNKNOWN)) }
            wizard(WizardEvent.Busy(false))
        }
    }

    // ---------- Logs ----------

    /** A redacted text file for Share logs. */
    fun exportLogs(dir: File, appVersion: String): File {
        val tv = _ui.value.tv
        val header = buildString {
            appendLine("TidyTV $appVersion on Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})")
            if (tv != null) appendLine("TV: ${tv.model}, Android ${tv.release} (SDK ${tv.sdk}), list: ${list?.id ?: "none"}")
            appendLine("step: ${_ui.value.wizard.step}, connect: ${_ui.value.connect::class.simpleName}")
        }
        val extra = linkedMapOf<String, String>()
        log?.records()?.let { r -> extra["actions"] = r.joinToString("\n") { "${it.ts} ${it.op} ${it.phase} ${it.target} ${it.detail.orEmpty()}" } }
        val text = trace.export(header, extra, secrets = listOfNotNull(tv?.serial, snapshot?.info?.serial))
        dir.mkdirs()
        return File(dir, "tidytv-log.txt").apply { writeText(text) }
    }

    override fun onCleared() {
        backend?.close(shell)
    }
}
