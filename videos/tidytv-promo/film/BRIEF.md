---
workflow: product-launch-video
flow: automation
storyboard: yes
message: "Your TV came with junk. Tidy it from your phone in a few taps, and undo it with one."
destination: web
aspect: 1920x1080
language: en
audience: Android TV and Google TV owners who saw the "clean your TV with Claude" trend but don't have a laptop or want an AI agent on their TV
length: 30s
angle: before-after demo
voice: none
---

## Intent

A 30-second promo for TidyTV, a free, open-source Android phone app that turns off the ads, trackers and
bloat on Android TV and Google TV over Wi-Fi. It rides the current "clean your TV" trend. Story: a TV full
of sponsored rows (before), a phone that tidies it in a few taps, the clean home screen (after), and
"one tap puts it all back". The blue TV mascot is the guide: worried at the start, sweeping in the middle,
cheering at the end. Friendly, quick and honest, not salesy. Music and on-screen text, no voiceover; it
must work muted in a feed.

## Assets

- ../assets/phone/take1.mp4 (spare: take2.mp4) — the real app, recorded on the emulator in Demo TV mode at 1080x1920; take1-log.txt has the second each step starts. This is the phone screen in every phone shot.
- ../assets/tv/A-before.png, A-after.png — photoreal living room, TV before (sponsored Google TV home) and after (Apple TV style home). tv-screens.json has the screen corners.
- ../assets/tv/B-before.png, B-after.png — a second room, before and after (Simple home).
- ../assets/tv/C-plate.png — room A with the TV off, for compositing screen content.
- /Users/joe/Documents/Coding/mobile-apps/tidytv/site/img/art/mascot-stand.webp, mascot-happy.webp, mascot-cheer.webp — the mascot (also app/src/main/res/drawable-nodpi/mascot_worried.webp, mascot_search.webp).
- /Users/joe/Documents/Coding/mobile-apps/tidytv/assets/brand/icons/a1-mascot-flat.png — the app icon.
- /Users/joe/Documents/Coding/mobile-apps/tidytv/app/src/test/screenshots/7_share_card.png — the share card the app makes.

## Customizations

- The phone stays in shot as the carrier element; every change on the TV happens on a phone tap.
- Ad rows fly off the TV one by one in scene 5.
- A before → after wipe on the TV screen in scene 6.
- End card: Download + Star on GitHub, tidytv.winkle.dev.

## Notes

- The TV images are generated illustrations: show a small "Illustration" caption in the corner of TV scenes.
- No claims beyond what the app does: backup first, turns apps off (never deletes), one-tap restore, free and open source, no computer, no AI. No speed numbers, user counts or ratings.
- Brand colours and type from the TidyTV site: Nunito; blue #2F6BFF; yellow #FFD54A; ink #1B1B3A; ground #EAF2FB.
