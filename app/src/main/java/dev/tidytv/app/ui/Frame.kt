package dev.tidytv.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Every screen: a white rounded sheet on the pale-blue backdrop (concept C), a scrolling body and an
 * optional pinned bottom action so the main button is always in reach.
 */
@Composable
fun ScreenFrame(
    top: (@Composable () -> Unit)? = null,
    bottom: (@Composable ColumnScope.() -> Unit)? = null,
    body: @Composable ColumnScope.() -> Unit,
) {
    Box(Modifier.fillMaxSize().background(Tidy.Backdrop).safeDrawingPadding().imePadding(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 560.dp).fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(28.dp)).background(Tidy.Card),
        ) {
            if (top != null) Box(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 6.dp)) { top() }
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = body,
            )
            if (bottom != null) Column(
                Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, bottom = 16.dp, top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = bottom,
            )
        }
    }
}

@Composable
fun Title(text: String, modifier: Modifier = Modifier, align: TextAlign = TextAlign.Start) =
    Text(text, style = MaterialTheme.typography.headlineMedium, color = Tidy.Ink, textAlign = align,
        modifier = modifier.fillMaxWidth().semantics { heading() })

@Composable
fun Body(text: String, color: Color = Tidy.Muted, align: TextAlign = TextAlign.Start) =
    Text(text, style = MaterialTheme.typography.bodyLarge, color = color, textAlign = align, modifier = Modifier.fillMaxWidth())

/** Speech bubble the mascot "says", as in concept C. */
@Composable
fun Bubble(text: String, color: Color = Tidy.BlueSoft, textColor: Color = Tidy.Ink, leading: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(color).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) { leading(); Spacer(Modifier.width(12.dp)) }
        Text(text, style = MaterialTheme.typography.headlineMedium, color = textColor, modifier = Modifier.semantics { heading() })
    }
}

/** Mascot with the little green "sparkle" dashes from the concept. */
@Composable
fun HeroMascot(worried: Boolean = false) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (!worried) Canvas(Modifier.size(200.dp, 150.dp)) {
            val dashes = listOf(Offset(0.12f, 0.25f) to Offset(0.2f, 0.32f), Offset(0.06f, 0.55f) to Offset(0.16f, 0.53f),
                Offset(0.88f, 0.25f) to Offset(0.8f, 0.32f), Offset(0.94f, 0.55f) to Offset(0.84f, 0.53f))
            for ((a, b) in dashes) drawLine(Tidy.Green.copy(alpha = 0.75f), Offset(a.x * size.width, a.y * size.height),
                Offset(b.x * size.width, b.y * size.height), strokeWidth = 9f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        }
        Mascot(132.dp, worried = worried)
    }
}

@Composable
fun Toggle(on: Boolean, onChange: (Boolean) -> Unit, label: String) {
    Switch(
        checked = on, onCheckedChange = onChange,
        modifier = Modifier.semantics { contentDescription = label },
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White, checkedTrackColor = Tidy.Green, checkedBorderColor = Tidy.Green,
            uncheckedThumbColor = Color.White, uncheckedTrackColor = Tidy.Grey, uncheckedBorderColor = Tidy.Line,
        ),
    )
}

/** Numbered instruction, used on Get ready and the help states. */
@Composable
fun NumberedStep(n: Int, title: String, detail: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(Tidy.Blue), contentAlignment = Alignment.Center) {
            Text("$n", style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodyMedium, color = Tidy.Muted)
        }
    }
}

/** A little TV with a colourful screen, standing in for the TV's own picture. */
@Composable
fun TvPicture(width: androidx.compose.ui.unit.Dp = 92.dp) {
    Canvas(Modifier.size(width, width * 0.66f)) {
        val w = size.width; val h = size.height
        drawRoundRect(Tidy.Ink, Offset(0f, 0f), Size(w, h * 0.86f), CornerRadius(w * 0.04f))
        drawRoundRect(
            Brush.linearGradient(listOf(Color(0xFF2D5BFF), Color(0xFFB040E0), Color(0xFFFF5A3C), Color(0xFFFFB020)), Offset(0f, 0f), Offset(w, h)),
            Offset(w * 0.035f, h * 0.05f), Size(w * 0.93f, h * 0.74f), CornerRadius(w * 0.02f),
        )
        drawLine(Tidy.Ink, Offset(w * 0.2f, h * 0.86f), Offset(w * 0.14f, h), strokeWidth = w * 0.03f)
        drawLine(Tidy.Ink, Offset(w * 0.8f, h * 0.86f), Offset(w * 0.86f, h), strokeWidth = w * 0.03f)
    }
}

/** Card for one TV in the scan list and on My TVs. */
@Composable
fun TvCard(title: String, subtitle: String, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Tidy.Card)
            .border(2.dp, Tidy.Line, RoundedCornerShape(22.dp))
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .heightIn(min = 72.dp).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TvPicture(84.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = Tidy.Ink)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Tidy.Muted)
        }
        if (trailing != null) trailing() else if (onClick != null) Text("›", style = MaterialTheme.typography.headlineMedium, color = Tidy.Muted)
    }
}

/** Round coloured badge holding a glyph, for group headers. */
@Composable
fun Badge(color: Color, glyph: String) {
    Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(color), contentAlignment = Alignment.Center) {
        Text(glyph, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

@Composable
fun Outline(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) =
    CardBox(modifier, border = Tidy.Line, content = content)
