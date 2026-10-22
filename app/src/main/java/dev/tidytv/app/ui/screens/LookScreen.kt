package dev.tidytv.app.ui.screens

import android.graphics.BitmapFactory
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.tidytv.app.LookId
import dev.tidytv.app.LookState
import dev.tidytv.app.PickedFile
import dev.tidytv.app.ui.BigButton
import dev.tidytv.app.ui.Body
import dev.tidytv.app.ui.Note
import dev.tidytv.app.ui.SafetyBanner
import dev.tidytv.app.ui.ScreenFrame
import dev.tidytv.app.ui.Spinner
import dev.tidytv.app.ui.Tidy
import dev.tidytv.app.ui.Title
import dev.tidytv.app.ui.TopBar

@Composable
fun LookScreen(
    look: LookState,
    onClose: () -> Unit,
    onApplyLook: (LookId) -> Unit,
    onApplyPhoto: (PickedFile) -> Unit,
) {
    val resolver = LocalContext.current.contentResolver
    val busy = look.applying != null
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            var name = "photo"
            var size = 0L
            runCatching {
                resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
                    if (c.moveToFirst()) {
                        val n = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val z = c.getColumnIndex(OpenableColumns.SIZE)
                        if (n >= 0 && !c.isNull(n)) name = c.getString(n) ?: "photo"
                        if (z >= 0 && !c.isNull(z)) size = c.getLong(z)
                    }
                }
            }
            onApplyPhoto(PickedFile(name, size) { resolver.openInputStream(uri) ?: error("can't open") })
        }
    }
    ScreenFrame(
        top = { TopBar(back = "Home screen", onBack = onClose, tvName = null) },
        bottom = { BigButton("Done", onClose, enabled = !busy) },
    ) {
        Title("Pick a look")
        look.message?.let { if (it.ok) SafetyBanner(it.message) else Note(it.message) }
        Body("Changes the wallpaper, dock and colours of the Apple TV style home screen. Your apps stay the same.")
        for (row in LookId.entries.chunked(2)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (id in row) Box(Modifier.weight(1f)) {
                    LookCard(id, selected = look.current == id.id, applying = look.applying == id.id, enabled = !busy) { onApplyLook(id) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        val photoOn = look.current == "photo"
        val shape = RoundedCornerShape(22.dp)
        Column(
            Modifier.fillMaxWidth().clip(shape)
                .border(if (photoOn) 3.dp else 2.dp, if (photoOn) Tidy.Blue else Tidy.Line, shape)
                .background(if (photoOn) Tidy.BlueSoft else Tidy.Card)
                .clickable(enabled = !busy, role = Role.Button) {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Your own photo", style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
            Text(
                "Pick any photo from your phone. TidyTV crops it to fit the TV and makes it small before sending.",
                style = MaterialTheme.typography.bodyMedium, color = Tidy.Muted,
            )
            if (look.applying == "photo") Row(verticalAlignment = Alignment.CenterVertically) {
                Spinner(22.dp)
                Spacer(Modifier.width(10.dp))
                Text("Making it TV-sized and sending…", style = MaterialTheme.typography.bodyMedium, color = Tidy.Ink)
            }
        }
    }
}

@Composable
private fun LookCard(id: LookId, selected: Boolean, applying: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(id) {
        runCatching {
            context.assets.open("themes/${id.id}/screenshot.jpg").use { BitmapFactory.decodeStream(it) }
        }.getOrNull()
    }
    val shape = RoundedCornerShape(18.dp)
    Column(
        Modifier.fillMaxWidth().clip(shape)
            .border(if (selected) 3.dp else 2.dp, if (selected) Tidy.Blue else Tidy.Line, shape)
            .background(if (selected) Tidy.BlueSoft else Tidy.Card)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(12.dp)).background(Tidy.Ink)) {
            if (bitmap != null) Image(
                bitmap.asImageBitmap(), "${id.title} look",
                Modifier.fillMaxWidth().aspectRatio(16f / 9f), contentScale = ContentScale.Crop,
            )
            if (applying) Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.45f)), contentAlignment = Alignment.Center) {
                Spinner(26.dp, Color.White)
            }
        }
        Text(id.title, style = MaterialTheme.typography.titleMedium, color = Tidy.Ink)
        Text(id.blurb, style = MaterialTheme.typography.bodyMedium, color = Tidy.Muted, modifier = Modifier.padding(bottom = 2.dp))
    }
}
