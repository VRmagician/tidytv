package dev.tidytv.core.flow

import dev.tidytv.core.flow.WizardEvent as E
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WizardTest {
    private fun r(s: Wizard, vararg es: E): Wizard? = es.fold<E, Wizard?>(s) { acc, e -> acc?.let { WizardReducer.reduce(it, e) } }

    @Test fun googleTvHappyPath() {
        var s = r(Wizard(), E.Choose(TvKind.ANDROID_TV))!!
        assertEquals(Step.GET_READY, s.step)
        s = r(s, E.Ready, E.Connected(fireOs = false))!!
        assertEquals(Step.REVIEW, s.step)
        s = r(s, E.StartTidy)!!
        assertEquals(Wizard(Step.TIDYING, TvKind.ANDROID_TV, Mode.CLEAN, busy = true), s)
        s = r(s, E.TidyEnded(stopped = false, launcherPossible = true))!!
        assertEquals(Step.HOME_SCREEN, s.step)
        assertEquals(Step.DONE, r(s, E.HomeScreenDone)!!.step)
    }

    @Test fun samsungAndLgAreNotSupported() {
        val s = r(Wizard(), E.Choose(TvKind.SAMSUNG_LG))!!
        assertEquals(Step.UNSUPPORTED, s.step)
        assertEquals(Wizard(), r(s, E.Back))
    }

    @Test fun fireTvSkipsHomeScreen() {
        val s = r(Wizard(), E.Choose(TvKind.FIRE_TV), E.Ready, E.Connected(fireOs = true), E.StartTidy, E.TidyEnded(false, launcherPossible = true))!!
        assertEquals(Step.DONE, s.step)
    }

    @Test fun tvItselfDecidesFireOs() {
        assertEquals(TvKind.FIRE_TV, r(Wizard(), E.Choose(TvKind.NOT_SURE), E.Ready, E.Connected(fireOs = true))!!.kind)
        assertEquals(TvKind.ANDROID_TV, r(Wizard(), E.Choose(TvKind.FIRE_TV), E.Ready, E.Connected(fireOs = false))!!.kind)
    }

    @Test fun stoppedTidyStaysWithMessage() {
        val s = r(Wizard(), E.Choose(TvKind.ANDROID_TV), E.Ready, E.Connected(false), E.StartTidy, E.TidyEnded(stopped = true, launcherPossible = true))!!
        assertEquals(Step.TIDYING, s.step)
        assertEquals(false, s.busy)
        assertEquals(Step.DONE, r(s, E.ToDone)!!.step)
    }

    @Test fun restoreGoesStraightToMyTvsAfterConnect() {
        val s = r(Wizard(), E.RestoreTapped, E.Ready, E.Connected(false))!!
        assertEquals(Wizard(Step.DONE, TvKind.ANDROID_TV, Mode.RESTORE), s)
    }

    @Test fun backNavigation() {
        val connect = r(Wizard(), E.Choose(TvKind.ANDROID_TV), E.Ready)!!
        assertEquals(Step.GET_READY, r(connect, E.Back)!!.step)
        val review = r(connect, E.Connected(false))!!
        assertEquals(Step.CONNECT, r(review, E.Back)!!.step)
        assertEquals(Step.DONE, r(review, E.StartTidy, E.Busy(false), E.Back)!!.step)
        assertEquals(Wizard(), r(Wizard(step = Step.DONE), E.Back))
        assertNull(r(Wizard(), E.Back))
    }

    @Test fun nothingLeavesWhileBusy() {
        val busy = r(Wizard(), E.Choose(TvKind.ANDROID_TV), E.Ready, E.Connected(false), E.StartTidy)!!
        assertEquals(busy, r(busy, E.Back))
        assertEquals(busy, r(busy, E.StartOver))
        assertEquals(busy, r(busy, E.RestoreTapped))
        assertEquals(busy, r(busy, E.ToDone))
    }

    @Test fun eventsForOtherScreensAreIgnored() {
        assertEquals(Wizard(), r(Wizard(), E.StartTidy))
        assertEquals(Wizard(), r(Wizard(), E.Connected(false)))
        assertEquals(Wizard(), r(Wizard(), E.HomeScreenDone))
        val review = Wizard(Step.REVIEW, TvKind.ANDROID_TV)
        assertEquals(review, r(review, E.Choose(TvKind.FIRE_TV)))
    }
}
