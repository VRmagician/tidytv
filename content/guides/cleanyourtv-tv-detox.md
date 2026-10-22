---
title: "Where the AI TV cleanup lists come from: cleanyourtv, tv-detox and what TidyTV adds"
seo_title: cleanyourtv and tv-detox explained, plus alternatives | TidyTV
description: What cleanyourtv (once tv-vibehack) and tv-detox are, where their per-TV debloat lists come from, how reliable they are, and what TidyTV adds on top.
dek: Two open-source projects sit behind most "Claude cleaned my TV" stories. Here is what each does and how far to trust the lists.
order: 4
date: 2026-10-20
---
cleanyourtv and tv-detox are two free, open-source projects that give an AI agent, or a person, a ready-made way to clean up an Android TV. cleanyourtv, which was first called tv-vibehack, adds per-model lists of apps that are safe to turn off. tv-detox is a prompt plus a simple, ad-free home screen. TidyTV builds on both and adds a phone app, a backup and a one-tap undo.

A "debloat" list is just a text file naming preloaded apps (ads, trackers, demos) that can be switched off on a given TV. A "launcher" is the home screen app.

## Who started this, and when?

The story has two waves.

- **Wave one, late August 2026.** Mert Cobanov told Claude to clean up his four-year-old Android TV over ADB, the tool that lets another device send commands to a TV. He also had it install FLauncher. [Tom's Hardware covered it](https://www.tomshardware.com/tech-industry/artificial-intelligence/developer-uses-claude-code-to-debloat-android-smart-tv-for-unbelievable-performance-upgrade-tv-now-smoother-than-it-was-new-as-autonomous-agent-deactivates-apps-shortens-animations-all-without-root-access) on September 2. His [prompt page](https://tv.cobanov.dev/) says the method needs no root and is reversible.
- **Wave two, from October 17.** Gathin's [tweet](https://x.com/gathint/status/2111379569030939102) said Claude Code removed 25 packages from his parents' Xiaomi TV and built a launcher. It drew 31,098 likes. [TechCrunch](https://techcrunch.com/2026/10/19/people-are-using-ai-to-make-their-smart-tvs-dumb-again/) and [Mashable](https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup) wrote it up within days.

Ryan Doyle's viral "Can you hack this?" post, per TechCrunch, was part of the mix too. TidyTV came after all of them.

## What is cleanyourtv (formerly tv-vibehack)?

[cleanyourtv](https://github.com/Gathin23/cleanyourtv) is Gathin's repository. It was created on October 17, and [saascity](https://saascity.io/blog/debloat-android-tv-claude-code) says it was renamed from tv-vibehack on October 19. Search for either name and you will find it. As of October 20, 2026, GitHub showed over 9,000 stars, over 1,000 forks and about 60 open issues.

It contains a [prompt](https://github.com/Gathin23/cleanyourtv/blob/main/PROMPT.md) for an AI agent, ADB tools, a launcher and the per-model lists. The companion site, [cleanyour.tv](https://cleanyour.tv/), claims 93 TVs from 31 brands within the first 48 hours and lists the press coverage.

## How many lists are there?

Counting the model folders in the repository's `examples` directory on October 22, 2026 gives 232. Each folder holds a `debloat.txt` for one TV model. Early write-ups gave slightly different figures, so treat the number as a snapshot that will grow.

The project describes these lists as starting points. Packages are grouped and commented, and you are meant to comment out anything you use. The notes show how model-specific TVs are. Typical entries mention ad and tracking services, retail demo modes and store or hotel modes. Several say one setup app must be turned off together with the stock launcher, or Home falls back to a recovery screen. Others say a particular service must stay, or the Input key stops working. Those are the kind of details a generic list would miss.

## What is tv-detox?

[tv-detox](https://github.com/Ansub/tv-detox) is by Ansub. It was created October 18 and had 773 stars and 83 forks as of October 20, 2026. It is a prompt for Claude Code or Codex plus an ad-free launcher, and apps are turned off, never deleted. The opening request is simply "Help me clean up my Android TV and make it faster." Ansub also made a popular [YouTube video](https://www.youtube.com/watch?v=fP2SGGVsaB0) on the idea, with over 430,000 views at the time of writing.

A third project, [dumbtv](https://github.com/humblemuzzu/dumbtv), appeared the same day and promises no ads from the TV itself and no watch tracking. It says it was tested on a Fire TV Edition and on Android and Google TV.

## How reliable are the lists?

Community-tested, and still being fixed. The issue trackers are open for anyone to read.

- A Sony TV hung on the Google TV boot screen and needed a factory reset ([#62](https://github.com/Gathin23/cleanyourtv/issues/62)).
- On a Xiaomi set, turning off the stock launcher crash-looped the system ([#90](https://github.com/Gathin23/cleanyourtv/issues/90)).
- A OnePlus TV showed "No Signal" on HDMI ([#321](https://github.com/Gathin23/cleanyourtv/issues/321)).
- Casting broke with an "Untrusted device" message on a Chromecast with Google TV ([#361](https://github.com/Gathin23/cleanyourtv/issues/361)).
- One issue asks for safer handling of firmware updates ([#359](https://github.com/Gathin23/cleanyourtv/issues/359)).

None of that is a criticism of volunteers sharing what worked on their own TV. It is a reminder to treat a list as a recipe for the TV it names, and to keep a way back.

## What does TidyTV add?

TidyTV is a free Android phone app that reads these two projects' work rather than replacing it. It bundles the cleanyourtv lists and offers two home screens: a **Simple** one from tv-detox and an **Apple TV style** one from cleanyourtv. Credit for both lies with the original authors.

What TidyTV adds is the wrapper:

- No laptop and no AI. The phone talks to the TV over Wi-Fi.
- A backup before any change, which can't be skipped.
- A short list of apps that are never touched, whatever a list says.
- Apps turned off, never uninstalled, one at a time with a check, and a stop if anything unexpected happens.
- "Restore everything" to undo it all.
- A page for each TV that has a list, on the [per-model pages](/tv/).

It also has limits. It runs from an Android phone, targets Android TV and Google TV, and does not touch ads inside apps like YouTube. And its lists are only as good as the upstream ones, so the open issues above apply to it too.

## Should you use the repositories or the app?

If you like command lines or want to hand the job to an agent, go to the repositories. They are the source, and they are moving fast. If you want the same lists without a laptop, a subscription or a prompt, try [TidyTV](/).
