package dev.tidytv.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import com.github.takahirom.roborazzi.captureRoboImage
import dev.tidytv.app.ui.Screen
import dev.tidytv.app.ui.TidyTheme
import dev.tidytv.core.flow.ConnectState
import dev.tidytv.core.flow.FoundTv
import dev.tidytv.core.flow.Mode
import dev.tidytv.core.flow.Step
import dev.tidytv.core.flow.TvKind
import dev.tidytv.core.flow.Wizard
import dev.tidytv.core.lists.Candidate
import dev.tidytv.core.lists.Group
import dev.tidytv.core.ops.ActionRecord
import dev.tidytv.core.ops.Op
import dev.tidytv.core.ops.Phase
import dev.tidytv.core.ops.PkgResult
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Every screen state, at 360 dp and 412 dp wide and at 360 dp with large fonts.
 * `./gradlew :app:recordRoborazziDebug` writes the images; `verifyRoborazziDebug` compares against them.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class ScreenshotTest {
    private data class Size(val name: String, val widthDp: Int, val font: Float)
    private val sizes = listOf(Size("360", 360, 1f), Size("412", 412, 1f), Size("360-large", 360, 1.6f))

    private fun shoot(name: String, step: Step, s: UiState) {
        for (size in sizes) {
            RuntimeEnvironment.setQualifiers("w${size.widthDp}dp-h${size.widthDp * 2}dp-xxhdpi")
            RuntimeEnvironment.setFontScale(size.font)
            captureRoboImage("src/test/screenshots/${name}_${size.name}.png") {
                CompositionLocalProvider(LocalInspectionMode provides true) {
                    TidyTheme { Screen(step, s, vm = null, onShareLogs = {}) }
                }
            }
        }
    }

    private val tv = FoundTv("192.168.1.42", name = "Living room TV", model = "TCL 55C735", legacyOpen = true)
    private val summary = TvSummary("Living room TV", "TCL 55C735", "192.168.1.42", "SERIAL", 30, "11")
    private val candidates = listOf(
        Candidate("com.tcl.tv.tclchannel", "TCL Channel", "", Group.ADS_TRACKING, true, false),
        Candidate("com.google.android.feedback", "Google Feedback", "", Group.ADS_TRACKING, true, false),
        Candidate("com.tcl.guard", "TGuard", "", Group.ADS_TRACKING, true, false),
        Candidate("com.facebook.appmanager", "Facebook App Manager", "", Group.ADS_TRACKING, true, false),
        Candidate("com.google.android.play.games", "Play Games", "", Group.PRELOADED, false, false),
        Candidate("com.google.android.katniss", "Google Assistant", "", Group.VOICE, false, false),
        Candidate("com.google.android.apps.mediashell", "Chromecast built-in", "", Group.CASTING, false, false),
        Candidate("com.spotify.tv.android", "Spotify", "", Group.STREAMING, false, false),
        Candidate("com.disney.disneyplus", "Disney+", "", Group.STREAMING, false, false),
    )
    private val review = ReviewState(BackupStatus.Saved, "TCL Smart TV Pro", true, candidates,
        candidates.filter { it.selected }.map { it.pkg }.toSet(), listOf("TV inputs", "Live TV"))
    private val clean = Wizard(Step.REVIEW, TvKind.ANDROID_TV)

    @Test fun welcome() = shoot("1_welcome", Step.WELCOME, UiState())
    @Test fun unsupported() = shoot("1b_unsupported", Step.UNSUPPORTED, UiState())
    @Test fun getReady() = shoot("2_get_ready", Step.GET_READY, UiState(wizard = Wizard(Step.GET_READY, TvKind.ANDROID_TV)))
    @Test fun getReadyFire() = shoot("2b_get_ready_fire", Step.GET_READY, UiState(wizard = Wizard(Step.GET_READY, TvKind.FIRE_TV)))

    @Test fun connectScanning() = shoot("3_scan", Step.CONNECT, UiState(connect = ConnectState.Scanning(listOf(tv, FoundTv("192.168.1.50", legacyOpen = true)), done = true)))
    @Test fun connectPressAllow() = shoot("3b_press_allow", Step.CONNECT, UiState(connect = ConnectState.PressAllow(tv, canRetry = true)))
    @Test fun connectPairing() = shoot("3c_pairing", Step.CONNECT, UiState(connect = ConnectState.Pairing(tv.copy(tlsPort = 37000), error = "Pairing didn't work. Check the code and try again.")))
    @Test fun connectNothingFound() = shoot("3d_nothing_found", Step.CONNECT, UiState(connect = ConnectState.NothingFound(badIp = true)))
    @Test fun connectTurnOnDebugging() = shoot("3e_turn_on_debugging", Step.CONNECT, UiState(connect = ConnectState.TurnOnDebugging(tv)))

    @Test fun reviewBackingUp() = shoot("4a_backup_running", Step.REVIEW, UiState(wizard = clean, review = ReviewState()))
    @Test fun reviewReady() = shoot("4_review", Step.REVIEW, UiState(wizard = clean, review = review))
    @Test fun reviewSimilarModel() = shoot("4b_review_similar", Step.REVIEW, UiState(wizard = clean, review = review.copy(exact = false)))
    @Test fun reviewNoList() = shoot("4c_review_no_list", Step.REVIEW, UiState(wizard = clean, review = ReviewState(BackupStatus.Saved, noList = true)))

    private val order = candidates.take(4)
    @Test fun tidying() = shoot("5_tidying", Step.TIDYING, UiState(tidy = TidyState(order, mapOf(
        order[0].pkg to PkgResult.Disabled(order[0].pkg), order[1].pkg to PkgResult.AlreadyOff(order[1].pkg)), reconnecting = true)))
    @Test fun tidyingStopped() = shoot("5b_tidying_stopped", Step.TIDYING, UiState(tidy = TidyState(order, mapOf(
        order[0].pkg to PkgResult.Disabled(order[0].pkg), order[1].pkg to PkgResult.Failed(order[1].pkg, "still on")),
        stopReason = "Google Feedback didn't stay off. Nothing else was changed.")))

    @Test fun homeOffer() = shoot("5c_home_offer", Step.HOME_SCREEN, UiState(
        tidy = TidyState(order, order.associate { it.pkg to PkgResult.Disabled(it.pkg) }),
        home = HomeState.Offer(
        listOf(DockApp("com.netflix.ninja", "Netflix"), DockApp("com.google.android.youtube.tv", "YouTube"), DockApp("com.spotify.tv.android", "Spotify")),
        listOf("com.netflix.ninja", "com.google.android.youtube.tv"))))
    @Test fun homeSetupApp() = shoot("5d_home_setup_app", Step.HOME_SCREEN, UiState(home = HomeState.Problem(
        "Google TV's setup app grabbed Home, so tidytv put your old home screen back. It can turn the setup app off and try again.",
        worried = false, action = HomeAction.TURN_OFF_SETUP_APP)))

    private val records = listOf(
        ActionRecord(1_700_000_000_000, Op.DISABLE, "com.tcl.tv.tclchannel", Phase.DONE),
        ActionRecord(1_700_000_060_000, Op.SET_HOME, "com.ansub.tvhome/.MainActivity", Phase.DONE),
    )
    @Test fun done() = shoot("6_done", Step.DONE, UiState(tv = summary, tidy = TidyState(order, order.associate { it.pkg to PkgResult.Disabled(it.pkg) }),
        done = DoneState(freedMb = 140, newHome = true, records = records, debugging = DebugCheck.OFF),
        labels = mapOf("com.tcl.tv.tclchannel" to "TCL Channel")))
    @Test fun doneRestored() = shoot("6b_restored", Step.DONE, UiState(tv = summary, wizard = Wizard(Step.DONE, mode = Mode.RESTORE),
        done = DoneState(records = records, restore = RestoreUi.Finished("Everything is back the way it was: 4 apps on, old home screen back.", ok = true))))
}
