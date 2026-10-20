---
title: "What about Fire TV, Samsung and LG? Honest answers"
seo_title: Debloat Fire TV, Samsung or LG TV? What works | TidyTV
description: The AI and app TV cleanups were built for Android TV and Google TV. Here is what that means for Fire TV, Samsung Tizen, LG webOS and Roku, without hype.
dek: The viral cleanups work on Android-based TVs. Some other TVs get part of the way, and some get nothing yet.
order: 9
date: 2026-10-20
---
The "Claude cleaned my TV" trend was built for Android TV and Google TV. Fire TV gets part of it, because it is Android underneath. Samsung (Tizen), LG (webOS) and Roku do not run Android, so the same method does not work on them. This guide says what we know, and where we don't know.

## Why does the cleanup only work on some TVs?

The method uses ADB (Android Debug Bridge), which lets another device send commands to a TV over Wi-Fi. It turns apps off with `pm disable-user`, a command that exists on Android. The best-known prompt, in the [cleanyourtv project](https://github.com/Gathin23/cleanyourtv/blob/main/PROMPT.md), is written for Android and Google TV, and [Mashable](https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup) says other platforms are not supported.

That is why the people behind it, Mert Cobanov, Gathin and Ansub, all talk about Android TV. [TechCrunch](https://techcrunch.com/2026/10/19/people-are-using-ai-to-make-their-smart-tvs-dumb-again/) quotes Gathin saying that people also tried Fire TV, LG and projectors, but that is a report of attempts, not a promise that it worked.

## Can you debloat a Fire TV?

Partly. A Fire TV is built on Android, so apps can be turned off in the same way. Two reports describe it as cleanup only.

- [Saascity's guide](https://saascity.io/blog/debloat-android-tv-claude-code) says Fire TV gets the debloat half only. In other words, you can switch off apps, but you don't get a replacement home screen.
- [TidyTV](/) says the same: a Fire TV gets the clean-up but keeps Amazon's home screen.

The cleanyourtv project has lists for some Fire TV models. For example, its Fire TV Stick 4K (2018) list targets Amazon telemetry and ad and recommendation feeders, and it warns that the sponsored rows on the home screen need DNS blocking, not just app removal. Insignia and Toshiba Fire TV sets have lists too. If you own one of these, check the matching page: [Fire TV Stick 4K (2018)](/tv/amazon-fire-tv-stick-4k-2018/) or [Toshiba Fire TV](/tv/toshiba-fire-tv-aftha004/).

A third project, [dumbtv](https://github.com/humblemuzzu/dumbtv), says it was tested on a Fire TV Edition as well as Android and Google TV. We haven't verified that claim ourselves.

Users have asked for more. One issue on cleanyourtv simply asks, "When will it be able for FIRE TV?" ([#79](https://github.com/Gathin23/cleanyourtv/issues/79)).

The same habits apply as on any TV: turn apps off rather than uninstalling them, and switch debugging off when you are done.

What we can't tell you: how a Fire TV behaves after a software update, or whether a particular model is safe to clean. Back up, change a few apps at a time and keep a way to restore.

## What about Samsung TVs?

Samsung's TVs run Tizen, not Android. ADB-and-`pm` cleanups do not apply, and neither AI prompts nor TidyTV can do this job.

Demand is clearly there. People in the r/vibecoding comments asked for Samsung help, including one who wanted to stop an older Samsung dragging on boot. Those comments come from search snippets, so treat them as reported rather than checked. [Engadget](https://www.engadget.com/2267074/why-claude-cleaning-tv-bloatware-not-best-idea/) notes that Samsung's OS takes about 20% of the TV's storage, which is part of why people want to trim it.

We don't have a verified method for Samsung, so we won't invent one. Your best source for what you can change is Samsung's own support pages for your model.

## What about LG webOS and Roku?

The answer is the same. LG runs webOS and Roku runs its own system. Neither is Android, so the ADB route and TidyTV don't apply. We found no tested method for either in the sources we checked, and we won't guess at settings.

## Is there anything you can do on a TV that isn't Android?

Here are the options that don't need a made-up fix.

- **Use the TV's own settings.** Look in the privacy and advertising settings for your model. We can't list them for you without checking each brand.
- **Learn about ACR.** TechCrunch and Engadget explain automatic content recognition, which can identify what's on screen by taking screenshots or fingerprinting audio. It is worth knowing the term when you read your TV's privacy menu.
- **Add a second device.** An Android TV or Google TV box or stick can be plugged into the TV's HDMI port. You then use that device as your home screen and ignore the TV's built-in one. The box itself can be cleaned. Lists exist for devices such as the [Chromecast with Google TV 4K](/tv/google-chromecast-gtv-4k/) and the [NVIDIA SHIELD TV (2019)](/tv/nvidia-shield-tv-2019/).
- **Wait.** Fire TV, Samsung and LG are the most requested gaps. A request is not a plan, and we won't promise support.

## Which TVs does TidyTV cover?

- **Covered:** Android TV and Google TV sets and boxes. The app needs an Android phone on the same Wi-Fi as the TV.
- **Partly covered:** Fire TV. It cleans up apps but keeps Amazon's home screen.
- **Not covered:** Samsung and LG, because they don't run Android. Roku is not covered either.

Not sure which you have? TidyTV's first screen asks, and has a "not sure" option. You can also look for your model on the [per-model pages](/tv/). It will not remove ads inside apps such as YouTube on any TV.

## What is the honest bottom line?

If you own an Android or Google TV, the trend applies to you, with either an agent or a phone app. If you own a Fire TV, you can switch apps off but not change the home screen. If you own a Samsung, LG or Roku, nothing in this trend is built for you yet. Start at the [TidyTV home page](/) to see what the app does.
