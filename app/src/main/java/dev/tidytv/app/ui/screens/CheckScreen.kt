package dev.tidytv.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tidytv.app.CheckState
import dev.tidytv.app.ui.AppIcon
import dev.tidytv.app.ui.BigButton
import dev.tidytv.app.ui.Body
import dev.tidytv.app.ui.Bubble
import dev.tidytv.app.ui.HeroMascot
import dev.tidytv.app.ui.Mood
import dev.tidytv.app.ui.Note
import dev.tidytv.app.ui.Outline
import dev.tidytv.app.ui.SafetyBanner
import dev.tidytv.app.ui.ScreenFrame
import dev.tidytv.app.ui.Spinner
import dev.tidytv.app.ui.TextLink
import dev.tidytv.app.ui.Tidy
import dev.tidytv.app.ui.Title
import dev.tidytv.app.ui.TopBar
import dev.tidytv.core.lists.AppLabels

/** Check my TV: compares the TV with what TidyTV turned off, and offers to turn things off again. */
@Composable
fun CheckScreen(
    s: CheckState,
    tvName: String?,
    labels: Map<String, String>,
    onRetidy: () -> Unit,
    onRebootTest: () -> Unit,
    onRecheck: () -> Unit,
    onBack: () -> Unit,
) {
    val report = s.report
    val busy = s.rebooting != null || s.running
    ScreenFrame(
        top = { TopBar(back = "My TV", onBack = onBack, tvName = tvName) },
        bottom = {
            if (s.rebooting == null) {
                if (report != null && report.toTidy.isNotEmpty()) {
                    BigButton(
                        if (s.retidying) "Turning off…" else "Turn these off again",
                        onRetidy, color = Tidy.Green, enabled = !s.retidying,
                    )
                }
                if (!busy) TextLink("Restart the TV and check", onRebootTest)
                if (report != null && report.toTidy.isEmpty() && !busy) TextLink("Check again", onRecheck)
            }
        },
    ) {
        Title("Check my TV")
        Body("TV updates sometimes switch apps back on. This checks your TV against what TidyTV turned off.")
        if (s.rebooting != null) {
            HeroMascot(mood = Mood.SEARCH, height = 120.dp)
            Bubble(s.rebooting, leading = { Spinner() })
            return@ScreenFrame
        }
        if (s.running) Bubble("Checking your TV…", leading = { Spinner() })
        s.message?.let { if (it.ok) SafetyBanner(it.message) else Note(it.message) }
        if (report != null) {
            if (report.allGood) {
                HeroMascot(mood = Mood.HAPPY, height = 120.dp)
                SafetyBanner("All tidy: ${report.stillOff} apps still off, nothing came back.")
            } else {
                HeroMascot(mood = Mood.WORRIED, height = 120.dp)
                if (report.backOn.isNotEmpty()) PackageCard("Switched back on", "Usually after a TV update.", report.backOn, labels)
                if (report.newOnList.isNotEmpty()) PackageCard("New since your last tidy", null, report.newOnList, labels)
                if (report.homeChanged) {
                    Note("Your TV's old home screen took over again. Open Home screen on My TV to switch back.")
                }
            }
        }
        Text(
            "Restarting checks that everything stays off after a reboot. It takes about 2 minutes.",
            style = MaterialTheme.typography.bodyMedium, color = Tidy.Muted,
        )
    }
}

@Composable
private fun PackageCard(title: String, note: String?, pkgs: List<String>, labels: Map<String, String>) {
    Outline {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
        if (note != null) Text(note, style = MaterialTheme.typography.bodyMedium, color = Tidy.Muted)
        Spacer(Modifier.heightIn(min = 10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            for (pkg in pkgs) {
                val label = labels[pkg] ?: AppLabels.label(pkg)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(pkg, label, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Text(label, style = MaterialTheme.typography.titleMedium, color = Tidy.Ink, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
