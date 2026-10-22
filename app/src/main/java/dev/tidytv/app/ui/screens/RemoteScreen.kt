package dev.tidytv.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.tidytv.app.RemoteApp
import dev.tidytv.app.RemoteState
import dev.tidytv.app.ui.AppIcon
import dev.tidytv.app.ui.BigButton
import dev.tidytv.app.ui.Bubble
import dev.tidytv.app.ui.CardBox
import dev.tidytv.app.ui.Note
import dev.tidytv.app.ui.ScreenFrame
import dev.tidytv.app.ui.Spinner
import dev.tidytv.app.ui.Tidy
import dev.tidytv.app.ui.TextLink
import dev.tidytv.app.ui.TopBar
import dev.tidytv.core.ops.RemoteKey

/** The phone remote. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(
    s: RemoteState,
    tvName: String?,
    onKey: (RemoteKey) -> Unit,
    onType: (String) -> Unit,
    onLaunch: (RemoteApp) -> Unit,
    onDismissMessage: () -> Unit,
    onBack: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val enabled = !s.reconnecting
    var text by remember { mutableStateOf("") }
    var sheet by remember { mutableStateOf(false) }
    val press: (RemoteKey) -> Unit = { k ->
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        onKey(k)
    }
    val send = {
        if (text.isNotEmpty()) {
            onType(text)
            text = ""
        }
    }

    ScreenFrame(top = { TopBar(back = "My TV", onBack = onBack, tvName = tvName) }) {
        if (s.reconnecting) Bubble("Reconnecting to your TV…", leading = { Spinner() })
        if (s.message != null) {
            Note(s.message)
            TextLink("OK", onDismissMessage)
        }

        Column(Modifier.alpha(if (enabled) 1f else 0.5f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                PowerKey(enabled) { press(RemoteKey.POWER) }
                Spacer(Modifier.weight(1f))
                RoundKey("↩", "Back", enabled) { press(RemoteKey.BACK) }
                Spacer(Modifier.width(10.dp))
                RoundKey("⌂", "Home", enabled) { press(RemoteKey.HOME) }
                Spacer(Modifier.width(10.dp))
                RoundKey("☰", "Menu", enabled) { press(RemoteKey.MENU) }
            }

            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(Modifier.size(260.dp).clip(CircleShape).background(Tidy.Blue)) {
                    Arrow("▲", "Up", enabled, Modifier.align(Alignment.TopCenter)) { press(RemoteKey.UP) }
                    Arrow("▼", "Down", enabled, Modifier.align(Alignment.BottomCenter)) { press(RemoteKey.DOWN) }
                    Arrow("◀", "Left", enabled, Modifier.align(Alignment.CenterStart)) { press(RemoteKey.LEFT) }
                    Arrow("▶", "Right", enabled, Modifier.align(Alignment.CenterEnd)) { press(RemoteKey.RIGHT) }
                    Box(
                        Modifier.align(Alignment.Center).size(96.dp).clip(CircleShape).background(Color.White)
                            .clickable(enabled = enabled, role = Role.Button, onClickLabel = null) { press(RemoteKey.OK) }
                            .semantics { contentDescription = "OK" },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("OK", style = MaterialTheme.typography.titleLarge, color = Tidy.Blue)
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(
                    Modifier.clip(RoundedCornerShape(999.dp)).background(Tidy.BlueSoft),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    GlyphBox("+", "Volume up", enabled, Modifier.size(width = 56.dp, height = 56.dp)) { press(RemoteKey.VOLUME_UP) }
                    GlyphBox("−", "Volume down", enabled, Modifier.size(width = 56.dp, height = 56.dp)) { press(RemoteKey.VOLUME_DOWN) }
                }
                Spacer(Modifier.width(10.dp))
                RoundKey("🔇", "Mute", enabled) { press(RemoteKey.MUTE) }
                Spacer(Modifier.weight(1f))
                RoundKey("⏪", "Rewind", enabled) { press(RemoteKey.REWIND) }
                Spacer(Modifier.width(6.dp))
                RoundKey("⏯", "Play or pause", enabled) { press(RemoteKey.PLAY_PAUSE) }
                Spacer(Modifier.width(6.dp))
                RoundKey("⏩", "Fast forward", enabled) { press(RemoteKey.FAST_FORWARD) }
            }

            CardBox {
                Text("Type on the TV", style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = text, onValueChange = { text = it }, enabled = enabled,
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    placeholder = { Text("Search or type") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { send() }),
                )
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextLink("Send", { if (enabled) send() })
                    Spacer(Modifier.weight(1f))
                    GlyphBox("⌫", "Delete", enabled, Modifier.size(48.dp).clip(CircleShape).background(Tidy.BlueSoft)) { press(RemoteKey.DELETE) }
                }
            }
        }

        BigButton("Apps on your TV", { sheet = true }, enabled = enabled)
    }

    if (sheet) {
        ModalBottomSheet(onDismissRequest = { sheet = false }) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                when {
                    s.loadingApps -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Spinner(); Spacer(Modifier.width(10.dp))
                        Text("Loading apps…", style = MaterialTheme.typography.bodyLarge, color = Tidy.Muted)
                    }
                    s.apps.isEmpty() -> Text("No apps found", style = MaterialTheme.typography.bodyLarge, color = Tidy.Muted, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    else -> Column(
                        Modifier.fillMaxWidth().height(420.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        s.apps.chunked(3).forEach { row ->
                            Row(Modifier.fillMaxWidth()) {
                                row.forEach { app ->
                                    Column(
                                        Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                                            .clickable(role = Role.Button) { sheet = false; onLaunch(app) }
                                            .padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        AppIcon(app.pkg, app.label, 56.dp)
                                        Spacer(Modifier.height(4.dp))
                                        Text(app.label, style = MaterialTheme.typography.labelMedium, color = Tidy.Ink, maxLines = 2, textAlign = TextAlign.Center)
                                    }
                                }
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GlyphBox(glyph: String, desc: String, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick).semantics { contentDescription = desc },
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, fontSize = 22.sp, color = Tidy.Ink, modifier = Modifier.clearAndSetSemantics { })
    }
}

@Composable
private fun RoundKey(glyph: String, desc: String, enabled: Boolean, onClick: () -> Unit) =
    GlyphBox(glyph, desc, enabled, Modifier.size(56.dp).clip(CircleShape).background(Tidy.BlueSoft), onClick)

/** Power, drawn rather than typed: Nunito has no ⏻ glyph. */
@Composable
private fun PowerKey(enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(56.dp).clip(CircleShape).background(Tidy.BlueSoft)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = "Power" },
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(Modifier.size(22.dp)) {
            val stroke = androidx.compose.ui.graphics.drawscope.Stroke(width = size.width * 0.13f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            val inset = size.width * 0.1f
            drawArc(Tidy.Ink, startAngle = -60f, sweepAngle = 300f, useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                size = androidx.compose.ui.geometry.Size(size.width - 2 * inset, size.height - 2 * inset), style = stroke)
            drawLine(Tidy.Ink, androidx.compose.ui.geometry.Offset(size.width / 2, 0f), androidx.compose.ui.geometry.Offset(size.width / 2, size.height * 0.45f),
                strokeWidth = size.width * 0.13f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
    }
}

@Composable
private fun Arrow(glyph: String, desc: String, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.padding(6.dp).size(64.dp).clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, fontSize = 26.sp, color = Color.White, modifier = Modifier.clearAndSetSemantics { })
    }
}
