package dev.tidytv.core.ops

import dev.tidytv.core.lists.Launchers
import dev.tidytv.core.testing.FakeTv
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RestorerTest {
    private val bloat = listOf("com.tcl.tv.plus", "com.tcl.ttvs", "com.tcl.guard")

    private suspend fun cleaned(tv: FakeTv = FakeTv.googleTv(), log: ActionLog = MemoryActionLog()): Pair<FakeTv, ActionLog> {
        Debloater(tv, log, clock = { 1L }, sleep = {}).run(bloat)
        return tv to log
    }

    @Test fun restoresFromPhoneLog() = runTest {
        val (tv, log) = cleaned()
        tv.files.remove(Commands.MIRROR_FILE) // TV-side list lost
        val out = Restorer(tv, log, clock = { 2L }).run()
        assertTrue(out is RestoreOutcome.Done)
        assertTrue(bloat.none { it in tv.disabled })
        assertTrue(log.records().disabledByUs().isEmpty())
    }

    @Test fun restoresFromTvMirrorWhenPhoneLogIsGone() = runTest {
        val (tv, _) = cleaned()
        val freshPhone = MemoryActionLog()
        val out = Restorer(tv, freshPhone).run()
        assertTrue(out is RestoreOutcome.Done)
        assertTrue(bloat.none { it in tv.disabled })
        assertNull("undo list cleared after a full restore", tv.files[Commands.MIRROR_FILE])
    }

    @Test fun ownerDisabledPackagesStayOff() = runTest {
        val tv = FakeTv.googleTv().apply { disabled += "com.netflix.ninja" }
        val (_, log) = cleaned(tv)
        Restorer(tv, log).run()
        assertTrue("com.netflix.ninja" in tv.disabled)
    }

    @Test fun packageUninstalledSinceIsFine() = runTest {
        val (tv, log) = cleaned()
        tv.installed -= "com.tcl.guard"
        tv.disabled -= "com.tcl.guard"
        val out = Restorer(tv, log).run()
        assertTrue(out is RestoreOutcome.Done)
    }

    @Test fun enableThatDoesNotStickIsPartialAndKeepsHome() = runTest {
        val (tv, log) = cleaned()
        tv.enableDoesNotStick += "com.tcl.ttvs"
        log.append(ActionRecord(3, Op.SET_HOME, Launchers.TIDY_HOME_COMPONENT, Phase.DONE, before = Launchers.LAUNCHERX + "/.MainActivity"))
        val homeBefore = tv.home()
        val out = Restorer(tv, log).run()
        assertTrue(out is RestoreOutcome.Partial)
        assertEquals(listOf("com.tcl.ttvs"), (out as RestoreOutcome.Partial).stillOff)
        assertEquals("Home is not moved until everything is back on", homeBefore, tv.home())
        assertTrue("undo list kept for the next attempt", tv.files[Commands.MIRROR_FILE] != null)
    }

    @Test fun runningTwiceIsHarmless() = runTest {
        val (tv, log) = cleaned()
        Restorer(tv, log).run()
        val second = Restorer(tv, log).run()
        assertTrue(second is RestoreOutcome.Done)
        assertTrue((second as RestoreOutcome.Done).enabled.isEmpty())
    }

    @Test fun droppedConnectionReportsDisconnectedThenFinishesOnRetry() = runTest {
        val (tv, log) = cleaned()
        tv.dropOnPrefix = "pm enable com.tcl.ttvs"
        assertTrue(Restorer(tv, log).run() is RestoreOutcome.Disconnected)
        assertTrue(Restorer(tv, log).run() is RestoreOutcome.Done)
        assertTrue(bloat.none { it in tv.disabled })
    }

    @Test fun settingsGoBackToBackedUpValues() = runTest {
        val tv = FakeTv.googleTv().apply { settings["global/window_animation_scale"] = "0.5" }
        val log = MemoryActionLog()
        log.append(ActionRecord(1, Op.SETTING, "global/window_animation_scale", Phase.DONE, before = "1.0"))
        log.append(ActionRecord(1, Op.SETTING, "global/private_dns_mode", Phase.DONE, before = "null"))
        tv.settings["global/private_dns_mode"] = "hostname"
        Restorer(tv, log).run()
        assertEquals("1.0", tv.settings["global/window_animation_scale"])
        assertFalse("global/private_dns_mode" in tv.settings)
    }

    @Test fun fullRoundTripWithLauncherPutsStockHomeBackAndRemovesOurs() = runTest {
        val tv = FakeTv.googleTv(sdk = 34)
        val log = MemoryActionLog()
        Debloater(tv, log, sleep = {}).run(bloat)
        val handover = LauncherHandover(tv, log, sleep = {}).run(ByteArray(10), listOf("com.netflix.ninja"), sdk = 34)
        assertTrue(handover is HandoverOutcome.Done)
        assertEquals(Launchers.TIDY_HOME_COMPONENT, tv.home())

        val out = Restorer(tv, log).run()
        assertTrue(out is RestoreOutcome.Done)
        assertTrue((out as RestoreOutcome.Done).homeRestored)
        assertEquals(Launchers.LAUNCHERX + "/.MainActivity", tv.home())
        assertFalse(Launchers.TIDY_HOME_PACKAGE in tv.installed)
        assertTrue(tv.disabled.isEmpty())
    }

    @Test fun androidNineHomeUsesFullComponent() = runTest {
        val tv = FakeTv.androidTv9()
        val log = MemoryActionLog()
        assertTrue(LauncherHandover(tv, log, sleep = {}).run(ByteArray(1), emptyList(), sdk = 28) is HandoverOutcome.Done)
        Restorer(tv, log).run()
        assertEquals("com.google.android.tvlauncher/.MainActivity", tv.home())
        assertTrue(tv.commands.any { it == "cmd package set-home-activity --user 0 com.google.android.tvlauncher/.MainActivity" })
        assertTrue("no role command below Android 10", tv.commands.none { it.startsWith("cmd role") })
    }
}
