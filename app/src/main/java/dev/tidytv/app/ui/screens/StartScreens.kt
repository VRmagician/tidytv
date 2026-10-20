package dev.tidytv.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
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
fun WelcomeScreen(onChoose: (TvKind) -> Unit, onRestore: () -> Unit) {
    ScreenFrame(top = {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { TextLink("↻  Restore a TV", onRestore) }
    }) {
        HeroMascot()
        Bubble("Hi! What TV do you have?")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ChoiceButton("Android TV / Google TV", "Sony, TCL, Philips, Hisense, Xiaomi, Chromecast…", Tidy.Blue) { onChoose(TvKind.ANDROID_TV) }
            ChoiceButton("Amazon Fire TV", "Clean-up only", Tidy.Orange) { onChoose(TvKind.FIRE_TV) }
            ChoiceButton("Samsung or LG", null, Tidy.Purple) { onChoose(TvKind.SAMSUNG_LG) }
            ChoiceButton("Not sure", "We'll check once connected", Tidy.Grey, textColor = Tidy.Ink) { onChoose(TvKind.NOT_SURE) }
        }
        Body("Free and open source. tidytv turns apps off, never deletes them, and saves a backup first so you can undo everything.")
    }
}

@Composable
fun UnsupportedScreen(onBack: () -> Unit) {
    ScreenFrame(bottom = { BigButton("Back", onBack, color = Tidy.Blue) }) {
        HeroMascot(worried = true)
        Title("Samsung and LG aren't supported")
        Body("Samsung TVs run Tizen and LG TVs run webOS. Neither lets a phone switch apps off the way Android TV does, so tidytv can't help with them.")
        Note("If your Samsung or LG has a Chromecast, Fire TV stick or Google TV streamer plugged in, pick that one instead.", color = Tidy.BlueSoft, textColor = Tidy.Ink)
    }
}

@Composable
fun GetReadyScreen(kind: TvKind?, mode: Mode, onReady: () -> Unit) {
    val fire = kind == TvKind.FIRE_TV
    ScreenFrame(bottom = { BigButton("My TV is ready", onReady) }) {
        Title(if (mode == Mode.RESTORE) "Let's reconnect to your TV" else "Get your TV ready")
        Body("This takes about a minute and is only needed once. Keep your phone on the same Wi-Fi as the TV.")
        Outline {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (fire) {
                    NumberedStep(1, "Turn on developer options", "Settings › My Fire TV › About. Select your Fire TV's name 7 times until it says you're a developer.")
                    NumberedStep(2, "Turn on ADB debugging", "Settings › My Fire TV › Developer options › ADB debugging: On.")
                    NumberedStep(3, "Keep the TV on", "When your TV asks \"Allow USB debugging?\", tick Always allow, then press Allow.")
                } else {
                    NumberedStep(1, "Turn on developer options", "Settings › System › About. Select Android TV OS build 7 times until it says you're a developer.")
                    NumberedStep(2, "Turn on debugging", "Settings › System › Developer options. Turn on USB debugging, and Network debugging if you see it.")
                    NumberedStep(3, "Keep the TV on", "When your TV asks \"Allow debugging?\", tick Always allow, then press Allow.")
                }
            }
        }
        Note("Using a stick powered from the TV's USB port? Plug it into its wall adapter for now, so it doesn't switch off halfway.")
        if (!fire) Body("Only see Wireless debugging? That works too: tidytv will ask for the code your TV shows.", color = Tidy.Muted)
    }
}

