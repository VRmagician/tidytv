package dev.tidytv.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.tidytv.app.DebugCheck
import dev.tidytv.app.DoneState
import dev.tidytv.app.RestoreUi
import dev.tidytv.app.TvSummary
import androidx.compose.ui.graphics.Color
import dev.tidytv.app.data.FeedbackKind
import dev.tidytv.app.ui.BigButton
import dev.tidytv.app.ui.FeedbackRequest
import dev.tidytv.app.ui.LocalAppActions
import dev.tidytv.app.ui.Body
import dev.tidytv.app.ui.Check
import dev.tidytv.app.ui.HeroMascot
import dev.tidytv.app.ui.Mood
import dev.tidytv.app.ui.Note
import dev.tidytv.app.ui.NumberedStep
import dev.tidytv.app.ui.Outline
import dev.tidytv.app.ui.SafetyBanner
import dev.tidytv.app.ui.ScreenFrame
import dev.tidytv.app.ui.Spinner
import dev.tidytv.app.ui.TextLink
import dev.tidytv.app.ui.Tidy
import dev.tidytv.app.ui.TvCard
import dev.tidytv.core.lists.AppLabels
import dev.tidytv.core.ops.ActionRecord
import dev.tidytv.core.ops.Finisher
import dev.tidytv.core.ops.Op
import dev.tidytv.core.ops.Phase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DoneScreen(
    tv: TvSummary?,
    s: DoneState,
    disabledCount: Int,
    restoreMode: Boolean,
    labels: Map<String, String>,
    onRestore: () -> Unit,
    onCheckDebugging: () -> Unit,
    onShareLogs: () -> Unit,
    onStartOver: () -> Unit,
) {
    val restoring = s.restore is RestoreUi.Running
    ScreenFrame(bottom = { TextLink("Start over", onStartOver) }) {
        val fin = s.restore as? RestoreUi.Finished
        HeroMascot(mood = when {
            fin?.ok == false -> Mood.WORRIED
            fin?.ok == true || (!restoreMode && disabledCount > 0) -> Mood.CHEER
            else -> Mood.STAND
        })
        if (tv != null) TvCard(tv.title, listOf(tv.model.takeIf { it != tv.title }, tv.host).filterNotNull().joinToString("\n"))
        val restored = (s.restore as? RestoreUi.Finished)?.ok == true
        if (!restoreMode && !restored && disabledCount > 0) {
            SafetyBanner(buildString {
                append("$disabledCount apps turned off")
                if (s.newHome) append(" · new home screen")
            })
            s.freedMb?.takeIf { it > 0 }?.let { Body("Freed about $it MB of memory.", color = Tidy.GreenInk) }
            HowDidItGo()
        }
        when (val r = s.restore) {
            is RestoreUi.Running -> Row(verticalAlignment = Alignment.CenterVertically) {
                Spinner(); Spacer(Modifier.width(10.dp)); Body("Turning things back on… ${r.count}")
            }
            is RestoreUi.Finished -> if (r.ok) SafetyBanner(r.message) else Note(r.message)
            RestoreUi.Idle -> if (restoreMode && s.records.none { it.op == Op.DISABLE }) {
                Body("TidyTV has no changes saved on this phone for this TV. Restore still checks the list kept on the TV itself.")
            }
        }
        BigButton(if (restoring) "Restoring…" else "↻  Restore everything", onRestore, color = Tidy.Green, enabled = !restoring)
        if (!restoreMode) DebuggingCard(s.debugging, onCheckDebugging)
        Activity(s.records, labels)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextLink("Share logs", onShareLogs)
            val actions = LocalAppActions.current
            TextLink("Send feedback", { actions.feedback(FeedbackRequest(FeedbackKind.FEATURE, "done")) })
        }
    }
}

/** The ask, right after a good run: share a card if it went well, tell us if it didn't. */
@Composable
private fun HowDidItGo() {
    val actions = LocalAppActions.current
    Outline {
        Text("How did it go?", style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
        Spacer(Modifier.heightIn(min = 10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BigButton("It went great", actions.shareCard, Modifier.weight(1f), color = Tidy.Green)
            BigButton("Not quite", { actions.feedback(FeedbackRequest(FeedbackKind.BUG, "done")) }, Modifier.weight(1f), color = Color.White, textColor = Tidy.Ink)
        }
    }
}

@Composable
private fun DebuggingCard(d: DebugCheck, onCheck: () -> Unit) {
    Outline {
        Text("Turn off debugging", style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
        Spacer(Modifier.heightIn(min = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Finisher.TURN_OFF_STEPS.forEachIndexed { i, step -> NumberedStep(i + 1, step) }
        }
        Spacer(Modifier.heightIn(min = 10.dp))
        when (d) {
            DebugCheck.OFF -> Row(verticalAlignment = Alignment.CenterVertically) {
                Check(Tidy.Green, 24.dp); Spacer(Modifier.width(8.dp))
                Text("Debugging is off. All done!", style = MaterialTheme.typography.titleMedium, color = Tidy.GreenInk)
            }
            DebugCheck.CHECKING -> Row(verticalAlignment = Alignment.CenterVertically) { Spinner(); Spacer(Modifier.width(8.dp)); Body("Checking…") }
            DebugCheck.STILL_ON -> Column {
                Note("Your TV still answers on the debugging port. Turn Network debugging off, then check again.")
                TextLink("Check it's off", onCheck)
            }
            DebugCheck.UNKNOWN -> TextLink("Check it's off", onCheck)
        }
    }
}

@Composable
private fun Activity(records: List<ActionRecord>, labels: Map<String, String>) {
    val items = records.filter { it.phase == Phase.DONE && it.detail != "was not off" }.mapNotNull { r -> describe(r, labels)?.let { r to it } }
        .asReversed().take(40)
    if (items.isEmpty()) return
    Text("Recent activity", style = MaterialTheme.typography.titleMedium, color = Tidy.Muted)
    Outline {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for ((r, text) in items) Row(verticalAlignment = Alignment.CenterVertically) {
                val (bg, glyph) = when (r.op) {
                    Op.DISABLE -> Tidy.RedSoft to "⦸"
                    Op.ENABLE -> Tidy.GreenSoft to "↻"
                    else -> Tidy.BlueSoft to "⌂"
                }
                Box(Modifier.size(40.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
                    Text(glyph, style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(text, style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
                    Text(whenText(r.ts), style = MaterialTheme.typography.bodyMedium, color = Tidy.Muted)
                }
            }
        }
    }
}

private fun describe(r: ActionRecord, labels: Map<String, String>): String? {
    val name = { labels[r.target] ?: AppLabels.label(r.target) }
    return when (r.op) {
        Op.DISABLE -> "Turned off ${name()}"
        Op.ENABLE -> "Turned on ${name()}"
        Op.INSTALL -> "Installed the clean home screen"
        Op.UNINSTALL -> "Removed the clean home screen"
        Op.SET_HOME -> if (dev.tidytv.core.lists.HomeApp.of(r.target.substringBefore('/')) != null) "Set new home screen" else "Put the old home screen back"
        Op.SETTING -> "Restored a setting"
    }
}

private fun whenText(ts: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = ts }
    val now = Calendar.getInstance()
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ts))
    val sameDay = c.get(Calendar.YEAR) == now.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
    return if (sameDay) "Today at $time" else SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(ts)) + " at $time"
}

