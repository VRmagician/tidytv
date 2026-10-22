package dev.tidytv.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tidytv.app.ui.BigButton
import dev.tidytv.app.ui.Body
import dev.tidytv.app.ui.Bubble
import dev.tidytv.app.ui.ChoiceButton
import dev.tidytv.app.ui.HeroMascot
import dev.tidytv.app.ui.Note
import dev.tidytv.app.ui.NumberedStep
import dev.tidytv.app.ui.Outline
import dev.tidytv.app.ui.ScreenFrame
import dev.tidytv.app.ui.TextLink
import dev.tidytv.app.ui.Tidy
import dev.tidytv.app.ui.Title
import dev.tidytv.core.flow.Mode
import dev.tidytv.core.flow.TvKind

@Composable
fun WelcomeScreen(
    onChoose: (TvKind) -> Unit,
    onRestore: () -> Unit,
    update: dev.tidytv.app.data.UpdateInfo? = null,
    onDismissUpdate: () -> Unit = {},
    savedTvs: List<dev.tidytv.app.data.KnownTv> = emptyList(),
    onSavedTv: (dev.tidytv.app.data.KnownTv) -> Unit = {},
    onRescue: (dev.tidytv.app.data.KnownTv) -> Unit = {},
) {
    ScreenFrame(top = {
        val actions = dev.tidytv.app.ui.LocalAppActions.current
        androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextLink("Feedback", { actions.feedback(dev.tidytv.app.ui.FeedbackRequest(dev.tidytv.app.data.FeedbackKind.FEATURE, "welcome")) }, color = Tidy.Muted)
            TextLink("↻  Restore a TV", onRestore)
        }
    }) {
        if (update != null) UpdateBanner(update, onDismissUpdate)
        if (savedTvs.isNotEmpty()) {
            androidx.compose.material3.Text("Your TVs", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = Tidy.Ink)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                savedTvs.take(3).forEach { tv ->
                    dev.tidytv.app.ui.TvCard(tv.title, tv.model, onClick = { onSavedTv(tv) })
                }
            }
            var pick by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
            TextLink("TV stuck after a change?", { if (savedTvs.size == 1) onRescue(savedTvs[0]) else pick = true }, color = Tidy.Muted)
            if (pick) androidx.compose.material3.AlertDialog(
                onDismissRequest = { pick = false },
                title = { androidx.compose.material3.Text("Which TV is stuck?") },
                text = {
                    Column {
                        savedTvs.forEach { tv ->
                            androidx.compose.material3.TextButton({ pick = false; onRescue(tv) }) { androidx.compose.material3.Text(tv.title) }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = { androidx.compose.material3.TextButton({ pick = false }) { androidx.compose.material3.Text("Cancel") } },
            )
            HeroMascot(height = 110.dp)
            Bubble("Connect another TV")
        } else {
            HeroMascot()
            Bubble("Hi! What TV do you have?")
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ChoiceButton("Android TV / Google TV", "Sony, TCL, Philips, Hisense, Xiaomi, Chromecast…", Tidy.Blue) { onChoose(TvKind.ANDROID_TV) }
            ChoiceButton("Amazon Fire TV", "Clean-up only", Tidy.Orange) { onChoose(TvKind.FIRE_TV) }
            ChoiceButton("Samsung or LG", null, Tidy.Purple) { onChoose(TvKind.SAMSUNG_LG) }
            ChoiceButton("Not sure", "We'll check once connected", Tidy.Grey, textColor = Tidy.Ink) { onChoose(TvKind.NOT_SURE) }
        }
        Body("Free and open source. TidyTV turns apps off, never deletes them, and saves a backup first so you can undo everything.")
    }
}

/** A newer release is out. TidyTV is sideloaded, so this is the only way people hear about it. */
@Composable
private fun UpdateBanner(update: dev.tidytv.app.data.UpdateInfo, onDismiss: () -> Unit) {
    val actions = dev.tidytv.app.ui.LocalAppActions.current
    androidx.compose.foundation.layout.Row(
        Modifier.fillMaxWidth()
            .background(Tidy.Yellow, androidx.compose.foundation.shape.RoundedCornerShape(18.dp))
            .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Text(
            "TidyTV ${update.version} is out", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = Tidy.Ink,
            modifier = Modifier.weight(1f),
        )
        TextLink("Get it", { actions.openUrl(dev.tidytv.app.data.UpdateCheck.SITE_URL) })
        TextLink("Not now", onDismiss, color = Tidy.Muted)
    }
}

@Composable
fun UnsupportedScreen(onBack: () -> Unit) {
    ScreenFrame(bottom = { BigButton("Back", onBack, color = Tidy.Blue) }) {
        HeroMascot(worried = true)
        Title("Samsung and LG aren't supported")
        Body("Samsung TVs run Tizen and LG TVs run webOS. Neither lets a phone switch apps off the way Android TV does, so TidyTV can't help with them.")
        Note("If your Samsung or LG has a Chromecast, Fire TV stick or Google TV streamer plugged in, pick that one instead.", color = Tidy.BlueSoft, textColor = Tidy.Ink)
    }
}

@Composable
fun GetReadyScreen(kind: TvKind?, mode: Mode, onReady: () -> Unit) {
    val fire = kind == TvKind.FIRE_TV
    ScreenFrame(bottom = { BigButton("My TV is ready", onReady) }) {
        Title(if (mode == Mode.RESTORE) "Let's reconnect to your TV" else "Get your TV ready")
        Body(
            if (mode == Mode.RESTORE) "Restoring needs the TV on and reachable. Keep your phone on the same home network as the TV and check debugging is still switched on."
            else "This takes about a minute and is only needed once. Keep your phone on the same home network as the TV.",
        )
        Outline {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (fire) {
                    NumberedStep(1, "Turn on developer options", "Settings › My Fire TV › About. Select your Fire TV's name 7 times until it says you're a developer.")
                    NumberedStep(2, "Turn on ADB debugging", "Settings › My Fire TV › Developer options › ADB debugging: On.")
                    NumberedStep(3, "Keep the TV on", "When your TV asks \"Allow USB debugging?\", tick Always allow, then press Allow.")
                } else {
                    NumberedStep(1, "Turn on developer options", "Settings › System › About. Select Android TV OS build 7 times until it says you're a developer.")
                    NumberedStep(2, "Turn on debugging", "Settings › System › Developer options. Turn on USB debugging, then Wireless debugging. Some TVs call it Network debugging or ADB over network instead.")
                    NumberedStep(3, "Keep the TV on", "When your TV asks \"Allow debugging?\", tick Always allow, then press Allow.")
                }
            }
        }
        Note("Using a stick powered from the TV's USB port? Plug it into its wall adapter for now, so it doesn't switch off halfway.")
        if (!fire) {
            Note(
                "TV on Ethernet? On most TVs Wireless debugging stays greyed out until the TV is on Wi-Fi. Turn on the TV's Wi-Fi and join the same network as your phone, then turn on Wireless debugging. You can turn Wi-Fi off again when you're done.",
                color = Tidy.BlueSoft, textColor = Tidy.Ink,
            )
            Body("With Wireless debugging, TidyTV asks once for the code your TV shows.", color = Tidy.Muted)
        }
    }
}

