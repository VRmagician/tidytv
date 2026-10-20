package dev.tidytv.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import dev.tidytv.app.UiState
import dev.tidytv.app.WizardViewModel
import dev.tidytv.app.ui.screens.ConnectScreen
import dev.tidytv.app.ui.screens.DoneScreen
import dev.tidytv.app.ui.screens.GetReadyScreen
import dev.tidytv.app.ui.screens.HomeScreenStep
import dev.tidytv.app.ui.screens.ReviewScreen
import dev.tidytv.app.ui.screens.TidyingScreen
import dev.tidytv.app.ui.screens.UnsupportedScreen
import dev.tidytv.app.ui.screens.WelcomeScreen
import dev.tidytv.core.flow.Mode
import dev.tidytv.core.flow.Step

@Composable
fun TidyApp(s: UiState, vm: WizardViewModel, onShareLogs: () -> Unit) {
    AnimatedContent(s.wizard.step, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "step") { step ->
        Screen(step, s, vm, onShareLogs)
    }
    s.problem?.let { msg ->
        AlertDialog(
            onDismissRequest = vm::dismissProblem,
            confirmButton = { TextButton(vm::dismissProblem) { Text("OK") } },
            dismissButton = { TextButton(onShareLogs) { Text("Share logs") } },
            text = { Text(msg) },
        )
    }
}

/** One screen per wizard step. Also used directly by the screenshot tests. */
@Composable
fun Screen(step: Step, s: UiState, vm: WizardViewModel?, onShareLogs: () -> Unit) {
    when (step) {
        Step.WELCOME -> WelcomeScreen({ vm?.choose(it) }, { vm?.restoreTapped() })
        Step.UNSUPPORTED -> UnsupportedScreen { vm?.back() }
        Step.GET_READY -> GetReadyScreen(s.wizard.kind, s.wizard.mode) { vm?.ready() }
        Step.CONNECT -> ConnectScreen(s.connect, { vm?.connect(it) }, onShareLogs)
        Step.REVIEW -> ReviewScreen(
            s.review, { vm?.toggleApp(it) }, { g, on -> vm?.toggleGroup(g, on) }, { vm?.startTidy() },
            { vm?.retryBackup() }, { vm?.toDone() },
        )
        Step.TIDYING -> TidyingScreen(s.tidy, { vm?.toDone(); vm?.restoreEverything() }, { vm?.toDone() }, onShareLogs)
        Step.HOME_SCREEN -> HomeScreenStep(
            s.home, s.tidy.disabledCount, { vm?.toggleDock(it) }, { vm?.installHome() }, { vm?.skipHome() },
            { a, eth -> vm?.homeAction(a, eth) }, { vm?.homeContinue() }, onShareLogs,
        )
        Step.DONE -> DoneScreen(
            s.tv, s.done, s.tidy.disabledCount, s.wizard.mode == Mode.RESTORE, s.labels,
            { vm?.restoreEverything() }, { vm?.checkDebuggingOff() }, onShareLogs, { vm?.startOver() },
        )
    }
}
