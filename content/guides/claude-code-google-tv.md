---
title: Can Claude Code really clean up your Google TV?
seo_title: Debloat Android TV with Claude Code, step by step | TidyTV
description: Can Claude Code debloat your Android TV or Google TV? See what it does in three steps, what you need, what it costs, and a no-laptop option.
dek: Yes, it can, if you have a laptop and a paid plan. Here is what actually happens on your TV.
order: 1
date: 2026-10-20
---
Yes, Claude Code can clean up an Android TV or Google TV, and many people have done it in an afternoon. It needs a laptop on the same Wi-Fi as the TV, plus an AI coding agent that is allowed to run commands. Nothing gets deleted: apps are switched off, and they can be switched back on.

## Where did the "Claude cleaned my TV" trend come from?

It came in two waves. On August 27, developer [Mert Cobanov](https://x.com/mertcobanov/status/2092980660386378154) told Claude to clean up his four-year-old Android TV and install a replacement home screen called FLauncher. [Tom's Hardware](https://www.tomshardware.com/tech-industry/artificial-intelligence/developer-uses-claude-code-to-debloat-android-smart-tv-for-unbelievable-performance-upgrade-tv-now-smoother-than-it-was-new-as-autonomous-agent-deactivates-apps-shortens-animations-all-without-root-access) covered it in early September. His [prompt page](https://tv.cobanov.dev/) says the method needs no root and backs up first.

The second wave began on October 17. A developer called Gathin posted that he had given Claude Code his parents' Xiaomi TV model. It removed 25 packages and built a custom launcher. He opened with "Vibe coded my parents' TV this afternoon!" (as quoted on [cleanyour.tv](https://cleanyour.tv/)). His tweet had more than 31,000 likes within three days. His project, [cleanyourtv](https://github.com/Gathin23/cleanyourtv) (first named tv-vibehack), had over 9,000 stars on GitHub as of October 20, 2026.

Others built on the idea. [Ansub's tv-detox](https://github.com/Ansub/tv-detox) is a prompt plus an ad-free launcher, and it turns apps off rather than deleting them. Press from TechCrunch to Mashable followed within days.

## What does Claude Code actually do to your TV?

First, some words. **ADB** (Android Debug Bridge) is a built-in Android tool that lets a computer send commands to a device. A **launcher** is the home screen app that shows your apps and rows of recommendations. **Debloat** means switching off the preloaded apps you never asked for.

A [walkthrough on saascity](https://saascity.io/blog/debloat-android-tv-claude-code) splits the job into three stages:

1. **Backup.** The agent records what is installed and what is switched on, so there is something to return to.
2. **Turn off apps.** It uses a command called `pm disable-user`. The app stays on the TV but stops running. [It's FOSS](https://itsfoss.com/news/debloat-smart-tv/) stresses this point: disable, not uninstall.
3. **Replace the launcher.** It installs a cleaner home screen, such as FLauncher or Projectivy, and makes it the default.

A good prompt also tells the agent which apps never to touch. Those lists cover the services behind your HDMI and tuner inputs, the source menu, remote and Bluetooth pairing, the keyboard and Google Play services. A checklist described by It's FOSS also asks you whether to keep the voice assistant, Chromecast and system updates, because those are your decisions, not clutter.

## What do you need to do it yourself?

- **A laptop on the same Wi-Fi as the TV.** [Mashable](https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup) notes that pasting a prompt into an ordinary chatbot will not work. The agent must be able to run commands on your computer.
- **An agent with shell access.** Claude Code or Codex both fit. [TechPP](https://techpp.com/2026/10/18/speed-up-android-tv-claude-code/) says Claude Pro starts at $20 a month, and it also published a guide for people who would rather skip a subscription and type the ADB commands by hand.
- **Developer options and debugging switched on.** On the TV, you tap the build number in Settings seven times, then turn on USB or network debugging.
- **An Android TV or Google TV.** The prompts do not cover Samsung, LG or Roku sets. Fire TV gets only part of the job.

## Does it always go smoothly?

No. Reports on the cleanyourtv issue tracker include a Sony that stuck on its boot screen after a restart ([issue 62](https://github.com/Gathin23/cleanyourtv/issues/62)) and a Xiaomi TV where disabling the stock launcher crashed a core system process ([issue 90](https://github.com/Gathin23/cleanyourtv/issues/90)). Some TVs do not offer wireless debugging at all, and some China-market sets refuse a third-party launcher. The [next article in this series](/guides/ai-tv-debloat-risks/) goes through the risks and how to lower them.

The results people describe are not magic either. Cleaning a TV can free memory and remove a sponsored row or two. Google says on its [support page](https://support.google.com/googletv/answer/13392198?hl=en) that you cannot turn ads off on Google TV, only the advertising ID, so a replacement launcher is what hides them.

## How long does it take?

The sources do not give a timing, so treat any number with care. Gathin described his own cleanup as an afternoon's work. Most of the effort goes into setup rather than the cleanup itself: finding your TV's address, switching on debugging and approving the connection on the TV screen. After that, the agent runs its commands one by one, and you can watch each step and ask it to explain what it is doing.

## Is Claude Code the right way for you?

It is a good fit if you are comfortable in a terminal, you already pay for an agent, or you want a home screen designed around your own taste. It is flexible. You can ask for exactly the changes you want and ask the agent to explain each one.

It is a poorer fit if you do not own a laptop, do not want a subscription for a one-off job, or do not want to depend on an AI's judgment about which apps your TV needs.

## Is there a way to do this without a laptop?

Yes. [TV Slim](https://tvslim.app/) runs from a Windows PC or an Android phone, and ADB TV is an app that runs on the TV itself. [TidyTV](/) is a free, open-source Android phone app that does the same job with taps. It finds your TV on Wi-Fi, saves a backup before it changes anything, turns apps off in small groups and never uninstalls. One button restores everything. It reuses the community per-model lists and the launchers from cleanyourtv and tv-detox, so the credit for the idea belongs to them.

If you want to see whether your model has a list, browse the [per-model pages](/tv/). Whichever route you pick, take the backup first and change a few things at a time.
