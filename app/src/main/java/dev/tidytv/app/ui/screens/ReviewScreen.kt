package dev.tidytv.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.tidytv.app.BackupStatus
import dev.tidytv.app.ReviewState
import dev.tidytv.app.ui.AppIcon
import dev.tidytv.app.ui.Badge
import dev.tidytv.app.ui.BigButton
import dev.tidytv.app.ui.Body
import dev.tidytv.app.ui.Bubble
import dev.tidytv.app.ui.Check
import dev.tidytv.app.ui.Note
import dev.tidytv.app.ui.Outline
import dev.tidytv.app.ui.ScreenFrame
import dev.tidytv.app.ui.Spinner
import dev.tidytv.app.ui.TextLink
import dev.tidytv.app.ui.Tidy
import dev.tidytv.app.ui.Title
import dev.tidytv.app.ui.Toggle
import dev.tidytv.core.lists.Candidate
import dev.tidytv.core.lists.Group

private fun Group.badge(): Pair<Color, String> = when (this) {
    Group.ADS_TRACKING -> Color(0xFFFF4F6D) to "📢"
    Group.PRELOADED -> Tidy.Orange to "▦"
    Group.VOICE -> Tidy.Purple to "🎙"
    Group.CASTING -> Color(0xFF16A3C9) to "⇪"
    Group.UPDATES -> Color(0xFF6B7A99) to "↻"
    Group.STREAMING -> Tidy.Green to "▶"
}

private fun Group.help(): String? = when (this) {
    Group.VOICE -> "Off means the remote's voice button stops working."
    Group.CASTING -> "Off means you can't cast from your phone to this TV."
    Group.UPDATES -> "Off stops firmware updates. Turn back on before updating."
    Group.STREAMING -> "Only ones you don't use. Your sign-ins come back if you turn them on again."
    else -> null
}

@Composable
fun ReviewScreen(
    s: ReviewState,
    onToggleApp: (String) -> Unit,
    onToggleGroup: (Group, Boolean) -> Unit,
    onStart: () -> Unit,
    onRetryBackup: () -> Unit,
    onDone: () -> Unit,
) {
    val n = s.selected.size
    ScreenFrame(bottom = {
        when {
            s.noList -> BigButton("Finish", onDone, color = Tidy.Blue)
            else -> BigButton(
                if (n == 0) "Pick apps to turn off" else "Turn off $n app${if (n == 1) "" else "s"}", onStart,
                color = Tidy.Green, enabled = s.backup == BackupStatus.Saved && n > 0,
            )
        }
    }) {
        BackupHeader(s.backup, onRetryBackup)
        if (s.backup != BackupStatus.Saved) return@ScreenFrame
        if (s.noList) {
            Title("No tested list for this TV yet")
            Body("Your backup is saved, but tidytv only turns apps off from a list someone has checked on the same model. Nothing will be changed.")
            return@ScreenFrame
        }
        if (!s.exact) Note("This list is for a similar model, not tested on yours. The safe parts are switched on; look through the rest.")
        for (group in Group.entries) {
            val apps = s.candidates.filter { it.group == group }
            if (apps.isNotEmpty()) GroupCard(group, apps, s.selected, onToggleApp, onToggleGroup)
        }
        KeptCard(s.kept)
    }
}

@Composable
private fun BackupHeader(b: BackupStatus, onRetry: () -> Unit) {
    when (b) {
        BackupStatus.Running -> Bubble("Saving a backup…") { Spinner(26.dp) }
        BackupStatus.Saved -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Bubble("Backup saved", color = Tidy.GreenSoft, textColor = Tidy.GreenInk) { Check(Tidy.Green, 30.dp) }
            Text("You can undo these changes anytime.", style = MaterialTheme.typography.bodyMedium, color = Tidy.Muted,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
        }
        is BackupStatus.Failed -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Note("${b.message} tidytv won't change anything without a backup.", color = Tidy.RedSoft, textColor = Tidy.Red)
            TextLink("Try the backup again", onRetry)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GroupCard(group: Group, apps: List<Candidate>, selected: Set<String>, onToggleApp: (String) -> Unit, onToggleGroup: (Group, Boolean) -> Unit) {
    val on = apps.count { it.pkg in selected }
    val (color, glyph) = group.badge()
    Outline {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Badge(color, glyph)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${group.title} (${apps.size})", style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
                if (on in 1 until apps.size) Text("$on of ${apps.size} picked", style = MaterialTheme.typography.labelMedium, color = Tidy.Muted)
            }
            Toggle(on > 0, { onToggleGroup(group, it) }, group.title)
        }
        group.help()?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Tidy.Muted, modifier = Modifier.padding(top = 6.dp)) }
        FlowRow(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (a in apps) AppChip(a, a.pkg in selected) { onToggleApp(a.pkg) }
        }
    }
}

@Composable
fun AppChipLayout(pkg: String, label: String, picked: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.width(70.dp).clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Checkbox, onClick = onClick)
            .semantics { stateDescription = if (picked) "will be turned off" else "stays on" }
            .padding(vertical = 6.dp, horizontal = 2.dp).alpha(if (picked) 1f else 0.4f),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AppIcon(pkg, label, size = 46.dp)
        Text(label, style = MaterialTheme.typography.labelMedium, color = Tidy.Ink, textAlign = TextAlign.Center,
            maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun AppChip(c: Candidate, picked: Boolean, onClick: () -> Unit) = AppChipLayout(c.pkg, c.label, picked, onClick)

@Composable
private fun KeptCard(kept: List<String>) {
    Outline {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Badge(Color(0xFF9AA3B8), "🔒")
            Spacer(Modifier.width(12.dp))
            Text("Always kept", style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
        }
        Text(
            (listOf("Play Store", "Google Play Services", "keyboard", "remote and Bluetooth", "TV inputs and Live TV") + kept.take(12)).joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium, color = Tidy.Muted, modifier = Modifier.padding(top = 8.dp),
        )
    }
}
