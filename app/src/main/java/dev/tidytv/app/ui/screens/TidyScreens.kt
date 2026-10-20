package dev.tidytv.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tidytv.app.HomeAction
import dev.tidytv.app.HomeState
import dev.tidytv.app.TidyState
import dev.tidytv.app.ui.AppIcon
import dev.tidytv.app.ui.BigButton
import dev.tidytv.app.ui.Body
import dev.tidytv.app.ui.Bubble
import dev.tidytv.app.ui.Check
import dev.tidytv.app.ui.HeroMascot
import dev.tidytv.app.ui.Note
import dev.tidytv.app.ui.Outline
import dev.tidytv.app.ui.ProgressBar
import dev.tidytv.app.ui.SafetyBanner
import dev.tidytv.app.ui.ScreenFrame
import dev.tidytv.app.ui.Spinner
import dev.tidytv.app.ui.TextLink
import dev.tidytv.app.ui.Tidy
import dev.tidytv.app.ui.Title
import dev.tidytv.core.ops.PkgResult

@Composable
fun TidyingScreen(s: TidyState, onRestore: () -> Unit, onContinue: () -> Unit, onShareLogs: () -> Unit) {
    val stopped = s.stopReason != null
    ScreenFrame(bottom = if (!stopped) null else { {
        BigButton("↻  Restore everything", onRestore, color = Tidy.Green)
        TextLink("Keep what was done and continue", onContinue)
        TextLink("Share logs", onShareLogs)
    } }) {
        if (stopped) {
            HeroMascot(worried = true)
            Title("tidytv stopped to be safe")
            Note(s.stopReason!!)
            Body("Your TV is fine. ${s.disabledCount} apps were turned off before it stopped; Restore everything turns them back on.")
        } else {
            Bubble(if (s.saving) "Letting your TV save…" else "Tidying up…") { Spinner(26.dp) }
            ProgressBar(if (s.order.isEmpty()) 0f else s.results.size / s.order.size.toFloat(), Tidy.Green)
            if (s.reconnecting) Note("Wi-Fi dropped. Reconnecting and carrying on where it left off…", color = Tidy.BlueSoft, textColor = Tidy.Ink)
            if (s.saving) Body("Android saves these changes a few seconds later. Please don't turn the TV off yet.")
        }
        Outline {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (c in s.order) Row(Modifier.fillMaxWidth().heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(c.pkg, c.label, 34.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(c.label, style = MaterialTheme.typography.titleMedium, color = Tidy.Ink, modifier = Modifier.weight(1f))
                    Status(s.results[c.pkg], running = !stopped && !s.finished)
                }
            }
        }
    }
}

@Composable
private fun Status(r: PkgResult?, running: Boolean) {
    when (r) {
        is PkgResult.Disabled -> Check(Tidy.Green, 26.dp)
        is PkgResult.AlreadyOff -> Small("already off")
        is PkgResult.NotInstalled -> Small("not on this TV")
        is PkgResult.Skipped -> Small("kept on")
        is PkgResult.Failed -> Small("didn't turn off", Tidy.Red)
        null -> if (running) Spinner(20.dp, Tidy.Line) else Small("not touched")
    }
}

@Composable
private fun Small(t: String, color: androidx.compose.ui.graphics.Color = Tidy.Muted) =
    Text(t, style = MaterialTheme.typography.labelMedium, color = color)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreenStep(
    s: HomeState,
    disabledCount: Int,
    onToggle: (String) -> Unit,
    onInstall: () -> Unit,
    onSkip: () -> Unit,
    onAction: (HomeAction, Boolean) -> Unit,
    onContinue: () -> Unit,
    onShareLogs: () -> Unit,
) {
    when (s) {
        is HomeState.Offer -> ScreenFrame(bottom = {
            BigButton("Get the cleaner home screen", onInstall)
            TextLink("No thanks, keep my home screen", onSkip)
        }) {
            SafetyBanner("$disabledCount apps turned off")
            Title("Get a cleaner home screen?")
            Body("A tiny home screen (37 KB) that shows just your apps: no ads, no rows of suggestions. Your old one comes back with Restore.")
            if (s.options.isNotEmpty()) Outline {
                Text("Apps on your home screen", style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
                FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (o in s.options) AppChipLayout(o.pkg, o.label, o.pkg in s.chosen) { onToggle(o.pkg) }
                }
            }
        }
        HomeState.Running -> ScreenFrame {
            HeroMascot()
            Bubble("Setting up your new home screen…") { Spinner(26.dp) }
            Body("This takes about 30 seconds. Please don't press buttons on the remote.")
        }
        HomeState.Installed -> ScreenFrame(bottom = { BigButton("Continue", onContinue) }) {
            HeroMascot()
            Bubble("New home screen is on", color = Tidy.GreenSoft, textColor = Tidy.GreenInk) { Check(Tidy.Green, 30.dp) }
            Body("Press Home on your remote to see it.")
        }
        is HomeState.Problem -> {
            var ethernet by rememberSaveable { mutableStateOf(false) }
            ScreenFrame(bottom = {
                when (s.action) {
                    HomeAction.REPLACE_COPY -> BigButton("Replace it", { onAction(HomeAction.REPLACE_COPY, false) })
                    HomeAction.TURN_OFF_SETUP_APP -> BigButton("Turn off the setup app and try again", { onAction(HomeAction.TURN_OFF_SETUP_APP, ethernet) })
                    HomeAction.RETRY -> BigButton("Try again", { onAction(HomeAction.RETRY, false) })
                    null -> Unit
                }
                TextLink(if (s.action == null) "Continue" else "Skip, keep my home screen", onContinue)
                if (s.worried) TextLink("Share logs", onShareLogs)
            }) {
                HeroMascot(worried = s.worried)
                Title(if (s.worried) "Something needs a look" else "Home screen not changed")
                Body(s.message)
                if (s.action == HomeAction.TURN_OFF_SETUP_APP) Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(ethernet, { ethernet = it })
                    Text("My TV is plugged in with a network cable", style = MaterialTheme.typography.bodyLarge, color = Tidy.Ink)
                }
                if (s.rescue.isNotEmpty()) Outline {
                    Text("If the TV looks wrong, these commands put it back (from a computer):", style = MaterialTheme.typography.bodyMedium, color = Tidy.Muted)
                    for (c in s.rescue) Text(c, style = MaterialTheme.typography.labelMedium, color = Tidy.Ink)
                }
            }
        }
    }
}
