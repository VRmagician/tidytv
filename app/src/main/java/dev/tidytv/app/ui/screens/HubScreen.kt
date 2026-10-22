package dev.tidytv.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tidytv.app.TvSummary
import dev.tidytv.app.ui.Check
import dev.tidytv.app.ui.HeroMascot
import dev.tidytv.app.ui.Mood
import dev.tidytv.app.ui.ScreenFrame
import dev.tidytv.app.ui.TextLink
import dev.tidytv.app.ui.Tidy
import dev.tidytv.app.ui.Title
import dev.tidytv.app.ui.TvCard

private class HubItem(val emoji: String, val title: String, val sub: String, val bg: Color, val fg: Color, val onClick: () -> Unit)

/** The My TV hub: which TV, how tidy it is, and a tile per feature. */
@Composable
fun HubScreen(
    tv: TvSummary?,
    appsOff: Int,
    fireTv: Boolean,
    onTidy: () -> Unit,
    onRemote: () -> Unit,
    onFiles: () -> Unit,
    onHomeScreen: () -> Unit,
    onRestore: () -> Unit,
    onBack: () -> Unit,
    onCheck: () -> Unit = {},
) {
    val items = buildList {
        add(HubItem("🧹", "Tidy up", "Turn off ads and bloat", Tidy.Blue, Color.White, onTidy))
        add(HubItem("🎮", "Remote", "Your phone as the remote", Tidy.Purple, Color.White, onRemote))
        add(HubItem("📤", "Send files", "Photos, videos and more", Tidy.Orange, Color.White, onFiles))
        if (!fireTv) add(HubItem("🏠", "Home screen", "Pick a clean one", Tidy.Green, Color.White, onHomeScreen))
        add(HubItem("🩺", "Check my TV", "After a TV update", Tidy.Yellow, Tidy.Ink, onCheck))
        add(HubItem("↻", "Restore", "Undo any changes", Tidy.Grey, Tidy.Ink, onRestore))
    }
    ScreenFrame(top = { Row(Modifier.fillMaxWidth()) { TextLink("‹  Change TV", onBack) } }) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            HeroMascot(mood = Mood.HAPPY, height = 110.dp)
        }
        if (tv != null) TvCard(tv.title, tv.model)
        if (appsOff > 0) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Check(Tidy.Green, 18.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (appsOff == 1) "1 app turned off" else "$appsOff apps turned off",
                    style = MaterialTheme.typography.titleMedium, color = Tidy.GreenInk,
                )
            }
        } else {
            Text("Not tidied yet", style = MaterialTheme.typography.titleMedium, color = Tidy.Muted)
        }
        Title("What would you like to do?")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items.chunked(2).forEach { pair ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (pair.size == 2) {
                        HubTile(pair[0]); HubTile(pair[1])
                    } else {
                        HubTile(pair[0]) // lone last tile spans both columns
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.HubTile(item: HubItem) {
    Column(
        Modifier.weight(1f)
            .clip(RoundedCornerShape(22.dp)).background(item.bg)
            .semantics { contentDescription = item.title }
            .clickable(role = Role.Button, onClick = item.onClick)
            .heightIn(min = 132.dp).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(item.emoji, fontSize = 32.sp)
        Spacer(Modifier.weight(1f, fill = false).heightIn(min = 4.dp))
        Text(item.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = item.fg)
        Text(item.sub, style = MaterialTheme.typography.bodyMedium, color = item.fg.copy(alpha = 0.9f))
    }
}
