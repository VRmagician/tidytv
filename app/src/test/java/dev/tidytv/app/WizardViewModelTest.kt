package dev.tidytv.app

import dev.tidytv.app.data.Store
import dev.tidytv.app.data.Trace
import dev.tidytv.app.net.Backend
import dev.tidytv.app.net.DemoBackend
import dev.tidytv.app.net.Link
import dev.tidytv.core.flow.Attempt
import dev.tidytv.core.flow.ConnectEvent
import dev.tidytv.core.flow.ConnectState
import dev.tidytv.core.flow.FoundTv
import dev.tidytv.core.flow.Step
import dev.tidytv.core.flow.TvKind
import dev.tidytv.core.lists.Launchers
import dev.tidytv.core.lists.ListCatalog
import dev.tidytv.core.ops.PkgResult
import dev.tidytv.core.testing.FakeTv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import dev.tidytv.core.ops.disabledByUs
import org.junit.Test
import java.io.File
import java.nio.file.Files

/** The whole flow through the view model, against the simulated TV. No Android, no network. */
@OptIn(ExperimentalCoroutinesApi::class)
class WizardViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var dir: File

    private object NoNetwork : Backend {
        override fun scan() = emptyFlow<FoundTv>()
        override suspend fun connect(tv: FoundTv, onAwaitingAllow: () -> Unit) = Link(Attempt.UNREACHABLE)
        override suspend fun pair(tv: FoundTv, code: String) = Link(Attempt.FAILED)
        override suspend fun portOpen(host: String) = false
        override val saveWaitMs = 0L
    }

    private val catalog by lazy {
        val examples = File("../upstream/cleanyourtv/examples")
        ListCatalog(examples.listFiles()!!.filter { it.isDirectory }.associate { it.name to File(it, "debloat.txt").readText() })
    }

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
        dir = Files.createTempDirectory("tidytv-vm").toFile()
    }

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(demo: DemoBackend) =
        WizardViewModel(Store(dir), Trace(File(dir, "logs")), { NoNetwork }, demo, { catalog }, { home -> home.pkg.toByteArray() })

    /** Connects to the Demo TV, which lands on the My TV hub; [tidy] then opens Tidy up. */
    private fun TestScope.connectToDemo(vm: WizardViewModel, tidy: Boolean = true) {
        vm.ready()
        advanceTimeBy(4_000)
        val scan = vm.ui.value.connect as ConnectState.Scanning
        assertTrue(scan.found.contains(DemoBackend.DEMO))
        vm.connect(ConnectEvent.Pick(DemoBackend.DEMO))
        advanceUntilIdle()
        assertEquals(Step.HUB, vm.ui.value.wizard.step)
        if (tidy) { vm.openTidy(); advanceUntilIdle() }
    }

    @Test fun cleanUpWithWifiDropThenHomeScreenThenRestore() = runTest(dispatcher) {
        val demo = DemoBackend()
        val tv: FakeTv = demo.tv
        val vm = vm(demo)
        vm.choose(TvKind.ANDROID_TV)
        connectToDemo(vm)

        var s = vm.ui.value
        assertEquals(Step.REVIEW, s.wizard.step)
        assertEquals(BackupStatus.Saved, s.review.backup)
        assertTrue(File(dir, "tvs").walk().any { it.name.startsWith("backup-") })
        val picked = s.review.candidates.filter { it.pkg in s.review.selected }.map { it.pkg }
        assertTrue("expected a few apps picked, got $picked", picked.size >= 3)

        // Wi-Fi drops right before the third app is turned off.
        tv.dropOnPrefix = "pm disable-user --user 0 ${picked[2]}"
        vm.startTidy()
        advanceUntilIdle()

        s = vm.ui.value
        assertEquals(Step.HOME_SCREEN, s.wizard.step)
        for (p in picked) {
            assertTrue(p, p in tv.disabled)
            assertEquals("$p shows as turned off", PkgResult.Disabled(p), s.tidy.results[p])
            assertEquals("$p disabled exactly once", 1, tv.commands.count { it == "pm disable-user --user 0 $p" })
        }

        vm.installHome()
        advanceUntilIdle()
        assertEquals(HomeState.Installed(dev.tidytv.core.lists.HomeApp.SIMPLE), vm.ui.value.home)
        assertEquals(Launchers.TIDY_HOME_COMPONENT, tv.home())

        vm.homeContinue()
        advanceUntilIdle()
        assertEquals(Step.DONE, vm.ui.value.wizard.step)
        assertTrue((vm.ui.value.done.freedMb ?: 0) > 0)

        vm.restoreEverything()
        advanceUntilIdle()
        val r = vm.ui.value.done.restore as RestoreUi.Finished
        assertTrue(r.message, r.ok)
        assertTrue(picked.none { it in tv.disabled })
        assertEquals(Launchers.LAUNCHERX, tv.home()?.substringBefore('/'))
        assertTrue(Launchers.TIDY_HOME_PACKAGE !in tv.installed)
    }

    @Test fun styledHomeScreenCanBeChosenAndRestored() = runTest(dispatcher) {
        val demo = DemoBackend()
        val tv = demo.tv
        val vm = vm(demo)
        vm.choose(TvKind.ANDROID_TV)
        connectToDemo(vm)
        val turnedOff = vm.ui.value.review.selected.size
        vm.startTidy()
        advanceUntilIdle()
        assertEquals(Step.HOME_SCREEN, vm.ui.value.wizard.step)
        val offer = vm.ui.value.home as HomeState.Offer
        assertEquals(dev.tidytv.core.lists.HomeApp.entries, offer.available)
        assertEquals("the simple home screen is the default", dev.tidytv.core.lists.HomeApp.SIMPLE, offer.app)

        vm.chooseHome(dev.tidytv.core.lists.HomeApp.STYLED)
        vm.installHome()
        advanceUntilIdle()
        assertEquals(HomeState.Installed(dev.tidytv.core.lists.HomeApp.STYLED), vm.ui.value.home)
        assertEquals(dev.tidytv.core.lists.HomeApp.STYLED.component, tv.home())
        assertTrue(tv.pushed.keys.any { it.endsWith("com.gathin.tvhome/files/config.json") })

        vm.homeContinue()
        advanceUntilIdle()
        vm.restoreEverything()
        advanceUntilIdle()
        val first = vm.ui.value.done.restore as RestoreUi.Finished
        assertTrue(first.ok)
        assertEquals("the old launcher is not counted as an app",
            "Everything is back the way it was: $turnedOff apps on, old home screen back.", first.message)
        assertTrue(dev.tidytv.core.lists.HomeApp.STYLED.pkg !in tv.installed)
        vm.restoreEverything()
        advanceUntilIdle()
        assertEquals("Nothing to put back: every app is already on and your old home screen is set.", (vm.ui.value.done.restore as RestoreUi.Finished).message)
    }

    @Test fun oneAppCanBeTurnedBackOnByItself() = runTest(dispatcher) {
        val demo = DemoBackend()
        val tv = demo.tv
        val vm = vm(demo)
        vm.choose(TvKind.ANDROID_TV)
        connectToDemo(vm)
        val chosen = vm.ui.value.review.selected.toList()
        vm.startTidy()
        advanceUntilIdle()
        vm.skipHome()
        advanceUntilIdle()
        assertEquals(Step.DONE, vm.ui.value.wizard.step)
        val pkg = chosen.first()
        assertTrue(pkg in tv.disabled)

        vm.turnOn(pkg)
        advanceUntilIdle()
        assertTrue("that app is back on", pkg !in tv.disabled)
        assertTrue("the rest stay off", chosen.drop(1).all { it in tv.disabled })
        val note = vm.ui.value.done.turnOnNote!!
        assertTrue(note.message, note.ok && note.message.endsWith("is back on."))
        assertTrue(vm.ui.value.done.turningOn.isEmpty())
        assertTrue(pkg !in vm.ui.value.done.records.disabledByUs())

        vm.restoreEverything()
        advanceUntilIdle()
        assertTrue((vm.ui.value.done.restore as RestoreUi.Finished).ok)
        assertTrue(chosen.none { it in tv.disabled })
    }

    @Test fun styledHomeScreenIsHiddenBelowAndroid8() = runTest(dispatcher) {
        val demo = DemoBackend(dev.tidytv.core.testing.FakeTv.googleTv(sdk = 25))
        val vm = vm(demo)
        vm.choose(TvKind.ANDROID_TV)
        connectToDemo(vm)
        vm.startTidy()
        advanceUntilIdle()
        val offer = vm.ui.value.home as HomeState.Offer
        assertEquals("styled needs Android 8; Projectivy runs from Android 5.1",
            listOf(dev.tidytv.core.lists.HomeApp.SIMPLE, dev.tidytv.core.lists.HomeApp.PROJECTIVY), offer.available)
        vm.chooseHome(dev.tidytv.core.lists.HomeApp.STYLED)
        assertEquals(dev.tidytv.core.lists.HomeApp.SIMPLE, (vm.ui.value.home as HomeState.Offer).app)
    }

    @Test fun restoreFromAFreshStartUsesThePhoneLog() = runTest(dispatcher) {
        val demo = DemoBackend()
        val first = vm(demo)
        first.choose(TvKind.ANDROID_TV)
        connectToDemo(first)
        first.startTidy()
        advanceUntilIdle()
        val off = demo.tv.disabled.toSet()
        assertTrue(off.isNotEmpty())

        // App restarted: new view model, same phone storage, same TV.
        val second = vm(demo)
        second.restoreTapped()
        connectToDemo(second, tidy = false)
        second.openRestore()
        advanceUntilIdle()
        assertEquals(Step.DONE, second.ui.value.wizard.step)
        assertTrue(second.ui.value.done.records.isNotEmpty())
        second.restoreEverything()
        advanceUntilIdle()
        assertTrue((second.ui.value.done.restore as RestoreUi.Finished).ok)
        assertTrue(demo.tv.disabled.isEmpty())
    }

    @Test fun nothingChangesBeforeTheBackupIsSaved() = runTest(dispatcher) {
        val demo = DemoBackend()
        demo.tv.settings.clear() // settings list still answers; break the backup instead:
        val vm = vm(demo)
        vm.choose(TvKind.ANDROID_TV)
        vm.ready()
        advanceTimeBy(4_000)
        demo.tv.dropOnPrefix = "settings list"
        vm.connect(ConnectEvent.Pick(DemoBackend.DEMO))
        advanceUntilIdle()
        vm.openTidy()
        advanceUntilIdle()
        val s = vm.ui.value
        assertTrue(s.review.backup is BackupStatus.Failed)
        vm.startTidy()
        advanceUntilIdle()
        assertEquals(Step.REVIEW, vm.ui.value.wizard.step)
        assertTrue(demo.tv.commands.none { it.startsWith("pm disable-user") })
    }

    @Test fun backFromReviewReturnsToTheHubStillConnected() = runTest(dispatcher) {
        val vm = vm(DemoBackend())
        vm.choose(TvKind.ANDROID_TV)
        connectToDemo(vm)
        assertTrue(vm.back())
        advanceTimeBy(100)
        assertEquals(Step.HUB, vm.ui.value.wizard.step)
        assertTrue(vm.ui.value.tv != null)
        assertTrue("Back from the hub goes home", vm.back())
        advanceTimeBy(100)
        assertEquals(Step.WELCOME, vm.ui.value.wizard.step)
        assertEquals(null, vm.ui.value.tv)
    }

    @Test fun savedTvReconnectsInOneTapAndTheRemoteWorks() = runTest(dispatcher) {
        val demo = DemoBackend()
        val vm = vm(demo)
        vm.choose(TvKind.ANDROID_TV)
        connectToDemo(vm, tidy = false)
        vm.back(); advanceTimeBy(100)
        val saved = vm.ui.value.savedTvs.single()
        assertEquals(DemoBackend.DEMO.host, saved.host)

        vm.connectSaved(saved)
        advanceUntilIdle()
        assertEquals(Step.HUB, vm.ui.value.wizard.step)

        vm.openRemote(); advanceUntilIdle()
        assertTrue(vm.ui.value.remote.apps.any { it.pkg == "com.netflix.ninja" })
        vm.press(dev.tidytv.core.ops.RemoteKey.UP)
        vm.type("hi")
        vm.launchApp(vm.ui.value.remote.apps.first { it.pkg == "com.netflix.ninja" })
        advanceUntilIdle()
        assertEquals(listOf("KEYCODE_DPAD_UP"), demo.tv.keys)
        assertEquals(listOf("hi"), demo.tv.typed)
        assertEquals(listOf("com.netflix.ninja/.MainActivity"), demo.tv.launched)
        vm.back(); advanceTimeBy(100)
        assertEquals(Step.HUB, vm.ui.value.wizard.step)
    }

    @Test fun filesAreSentOneByOneIntoTheTvFolder() = runTest(dispatcher) {
        val demo = DemoBackend()
        val vm = vm(demo)
        vm.choose(TvKind.ANDROID_TV)
        connectToDemo(vm, tidy = false)
        vm.openFiles(); advanceUntilIdle()
        assertTrue(vm.ui.value.files.freeBytes!! > 0)
        val a = ByteArray(2000) { 1 }
        val b = ByteArray(10) { 2 }
        vm.sendFiles(listOf(PickedFile("photo.jpg", a.size.toLong()) { a.inputStream() }, PickedFile("photo.jpg", b.size.toLong()) { b.inputStream() }))
        advanceUntilIdle()
        val items = vm.ui.value.files.items
        assertEquals(listOf(SendStatus.SENT, SendStatus.SENT), items.map { it.status })
        assertEquals(listOf("photo.jpg", "photo (2).jpg"), items.map { it.savedAs })
        assertTrue(a.contentEquals(demo.tv.pushed["/sdcard/Download/TidyTV/photo.jpg"]))
    }

    /** Tidies the Demo TV and returns to the hub; the apps turned off. */
    private fun TestScope.tidyThenHub(vm: WizardViewModel): List<String> {
        vm.choose(TvKind.ANDROID_TV)
        connectToDemo(vm)
        val chosen = vm.ui.value.review.selected.toList()
        vm.startTidy(); advanceUntilIdle()
        vm.skipHome(); advanceUntilIdle()
        vm.toHub(); advanceTimeBy(100)
        assertEquals(Step.HUB, vm.ui.value.wizard.step)
        return chosen
    }

    @Test fun checkFindsAppsAnUpdateTurnedBackOnAndTidiesThemAgain() = runTest(dispatcher) {
        val demo = DemoBackend()
        val vm = vm(demo)
        val chosen = tidyThenHub(vm)
        vm.openCheck(); advanceUntilIdle()
        assertEquals(Step.CHECK, vm.ui.value.wizard.step)
        assertTrue("fresh tidy is all good", vm.ui.value.check.report!!.allGood)

        val back = chosen.first()
        demo.tv.disabled -= back // a firmware update switched it on
        vm.recheck(); advanceUntilIdle()
        assertEquals(listOf(back), vm.ui.value.check.report!!.backOn)

        vm.retidy(); advanceUntilIdle()
        assertTrue(back in demo.tv.disabled)
        assertTrue(vm.ui.value.check.message!!.ok)
        assertTrue(vm.ui.value.check.report!!.allGood)
    }

    @Test fun rebootTestReconnectsAndReportsWhatCameBack() = runTest(dispatcher) {
        val demo = DemoBackend()
        val vm = vm(demo)
        val chosen = tidyThenHub(vm)
        vm.openCheck(); advanceUntilIdle()
        demo.tv.reEnableOnReboot = mutableSetOf(chosen.first())
        vm.rebootTest(); advanceUntilIdle()
        assertEquals(1, demo.tv.reboots)
        assertEquals(null, vm.ui.value.check.rebooting)
        assertEquals(Step.CHECK, vm.ui.value.wizard.step)
        assertEquals(listOf(chosen.first()), vm.ui.value.check.report!!.backOn)
    }

    @Test fun rescueTurnsEverythingBackOnWhenTheTvAnswers() = runTest(dispatcher) {
        val demo = DemoBackend()
        val vm = vm(demo)
        val chosen = tidyThenHub(vm)
        vm.back(); advanceTimeBy(100)
        assertEquals(Step.WELCOME, vm.ui.value.wizard.step)
        val saved = vm.ui.value.savedTvs.single()

        vm.startRescue(saved); advanceUntilIdle()
        assertEquals(Step.RESCUE, vm.ui.value.wizard.step)
        val r = vm.ui.value.rescue
        assertTrue(r.result!!.message, r.result!!.ok)
        assertTrue(chosen.none { it in demo.tv.disabled })
        vm.stopRescue(); advanceTimeBy(100)
        assertEquals(Step.WELCOME, vm.ui.value.wizard.step)
    }

    @Test fun projectivyComesFromTheTvsPlayStoreAndStaysAfterRestore() = runTest(dispatcher) {
        val demo = DemoBackend()
        val tv = demo.tv
        tv.playStoreInstalls = false
        val vm = vm(demo)
        vm.choose(TvKind.ANDROID_TV)
        connectToDemo(vm)
        vm.startTidy(); advanceUntilIdle()
        vm.chooseHome(dev.tidytv.core.lists.HomeApp.PROJECTIVY)
        vm.installHome()
        advanceTimeBy(1_000)
        assertEquals(HomeState.WaitingForPlayStore(dev.tidytv.core.lists.HomeApp.PROJECTIVY), vm.ui.value.home)
        assertEquals(listOf(dev.tidytv.core.lists.HomeApp.PROJECTIVY.pkg), tv.playStoreOpened)

        tv.installProjectivy() // the owner pressed Install
        advanceUntilIdle()
        assertEquals(HomeState.Installed(dev.tidytv.core.lists.HomeApp.PROJECTIVY), vm.ui.value.home)
        assertTrue(tv.home()!!.startsWith(dev.tidytv.core.lists.HomeApp.PROJECTIVY.pkg))

        vm.homeContinue(); advanceUntilIdle()
        vm.restoreEverything(); advanceUntilIdle()
        assertTrue((vm.ui.value.done.restore as RestoreUi.Finished).ok)
        assertTrue("we never uninstall what we didn't install", dev.tidytv.core.lists.HomeApp.PROJECTIVY.pkg in tv.installed)
        assertTrue(!tv.home()!!.startsWith(dev.tidytv.core.lists.HomeApp.PROJECTIVY.pkg))
    }
}
