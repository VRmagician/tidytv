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
        WizardViewModel(Store(dir), Trace(File(dir, "logs")), { NoNetwork }, demo, { catalog }, { ByteArray(64) })

    private fun TestScope.connectToDemo(vm: WizardViewModel) {
        vm.ready()
        advanceTimeBy(4_000)
        val scan = vm.ui.value.connect as ConnectState.Scanning
        assertTrue(scan.found.contains(DemoBackend.DEMO))
        vm.connect(ConnectEvent.Pick(DemoBackend.DEMO))
        advanceUntilIdle()
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
        assertEquals(HomeState.Installed, vm.ui.value.home)
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
        connectToDemo(second)
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
        val s = vm.ui.value
        assertTrue(s.review.backup is BackupStatus.Failed)
        vm.startTidy()
        advanceUntilIdle()
        assertEquals(Step.REVIEW, vm.ui.value.wizard.step)
        assertTrue(demo.tv.commands.none { it.startsWith("pm disable-user") })
    }

    @Test fun backFromReviewDisconnectsAndRescans() = runTest(dispatcher) {
        val vm = vm(DemoBackend())
        vm.choose(TvKind.ANDROID_TV)
        connectToDemo(vm)
        assertTrue(vm.back())
        advanceTimeBy(100)
        assertEquals(Step.CONNECT, vm.ui.value.wizard.step)
        assertTrue(vm.ui.value.connect is ConnectState.Scanning)
    }
}
