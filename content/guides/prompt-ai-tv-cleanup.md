---
title: "Hack my TV" got refused: how to ask an AI for a TV cleanup
seo_title: Claude won't hack my TV: a prompt to debloat Android TV | TidyTV
description: Claude refused "hack my TV"? Learn how to ask an AI to debloat your Android TV or Google TV with a prompt that works, plus a safety checklist.
dek: The wording matters. Here is what to tell the AI, what to include, and what to leave out.
order: 5
date: 2026-10-20
---
If an AI refused your request to "hack my TV", the fix is to describe the job you actually want: turn off ads and preloaded apps on your own Android TV, safely and reversibly. Name your exact model, say what to keep, ask for a backup first and let the agent work in small steps. That wording is closer to what the popular projects use.

## Why did the AI refuse "hack my TV"?

[TechCrunch](https://techcrunch.com/2026/10/19/people-are-using-ai-to-make-their-smart-tvs-dumb-again/) and [It's FOSS](https://itsfoss.com/news/debloat-smart-tv/) both report that people said Claude refused when they used that phrasing, and that a fuller prompt was needed. The reports do not say why. My guess, and it is only a guess, is that "hack" sounds like breaking into someone else's device, while what you want is to change settings on a TV you own.

The trend itself started with other words. Developer Mert Cobanov's [prompt page](https://tv.cobanov.dev/) asks Claude to clean up an Android TV over ADB. (ADB, or Android Debug Bridge, is the built-in tool a computer uses to send commands to an Android device.) [Ansub's tv-detox](https://github.com/Ansub/tv-detox) opens with "Help me clean up my Android TV and make it faster." Both treat the job as maintenance.

## What should a good prompt include?

The [cleanyourtv](https://github.com/Gathin23/cleanyourtv/blob/main/PROMPT.md) prompt by Gathin, the project that took off on October 17, is a long document, not a one-liner. You do not need to copy it, but the ingredients are worth borrowing:

- **Say it is your TV.** "My TV, on my home network" is plain and true.
- **Give the exact model and Android version.** You will find the model name on the TV's About screen. Per-model lists are the reason the projects say they work, and the same app can be called different things on different brands.
- **State the goal.** For example: turn off ads, tracking and preloaded apps, and install a cleaner home screen.
- **Say what to keep.** Voice assistant, Chromecast and updates are your decision. [It's FOSS](https://itsfoss.com/news/debloat-smart-tv/) says the checklist asks about each.
- **Set the method.** Turn apps off with `pm disable-user`, never uninstall, and keep a record of every change.
- **Ask for a plan first.** Make the agent list what it intends to switch off and why, then wait for you.

## Is there a prompt you can copy?

Here is a starting point in my own words. Adjust the brackets:

> I own a [brand and model] running Android TV or Google TV [version]. It is on my home Wi-Fi at [IP address], with network debugging switched on. Please clean it up over ADB so it is faster and shows fewer ads. First, back up the list of installed and enabled apps to a file on this computer. Then show me a plan of which apps you would turn off and why, and wait for my approval. Only disable apps with pm disable-user. Never uninstall anything. Never touch HDMI or tuner input services, the source menu, remote or Bluetooth pairing, the keyboard, or Google Play services. Work in batches of five, and after each batch ask me to check that Home, HDMI and the remote still work. Ask me before touching the voice assistant, Chromecast or system updates. Install a replacement launcher and confirm it starts before disabling the stock one. At the end, give me the exact commands to undo every change.

The never-touch list is the one in the prompts described by [saascity](https://saascity.io/blog/debloat-android-tv-claude-code) and tv-detox.

## What do you need besides the prompt?

An AI chat window is not enough. [Mashable](https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup) explains that pasting the text into an ordinary chatbot will not work, because the agent must be able to run commands on a computer connected to your TV. You need a laptop on the same Wi-Fi and a coding agent such as Claude Code or Codex. [TechPP](https://techpp.com/2026/10/18/speed-up-android-tv-claude-code/) notes that Claude Pro starts at $20 a month.

On the TV, open About in Settings and press the build number seven times to unlock developer options. Then switch on USB or network debugging. Some TVs only offer wireless debugging with a pairing code, and a few boxes cut USB power while debugging is on, according to saascity.

## What should you not ask for?

- **Do not ask it to "root" the TV.** The popular methods say they need no root. You are not trying to change the system, just to turn apps off.
- **Do not ask for everything at once.** Several reported failures, such as a [boot screen hang on a Sony](https://github.com/Gathin23/cleanyourtv/issues/62), came after the stock launcher or a core service was disabled.
- **Do not skip the backup.** If the agent cannot show you what it saved, stop.
- **Do not leave debugging on.** Turn it off when you are done, as It's FOSS advises.

The [risks article](/guides/ai-tv-debloat-risks/) covers what went wrong for other people and how to lower the odds.

## What if you do not want to write a prompt at all?

Then skip the agent. [TidyTV](/) is a free, open-source Android phone app that does the same job with taps. You turn on debugging on the TV, the phone finds it on Wi-Fi, and a backup is saved before anything changes. Apps are shown in groups, such as ads and tracking, preloaded apps, voice assistant, casting and updates, and are switched off one at a time. It never uninstalls, and one button restores everything. It uses the same community lists and launchers as cleanyourtv and tv-detox, which deserve the credit for the idea. [TV Slim](https://tvslim.app/) is another option that runs from a PC or phone.

It covers Android TV and Google TV. Samsung and LG sets do not run Android, and the AI prompts do not work on them either. To see whether your model has a list, check the [per-model pages](/tv/).

Whichever route you take, the pattern is the same: say what you own, say what you want, protect the essentials and go slowly.
