---
title: "Claude Code vs a phone app: two ways to debloat a Google TV"
seo_title: Debloat Google TV: Claude Code vs a phone app, no PC | TidyTV
description: Debloat a Google TV or Android TV with Claude Code on a laptop, or with a phone app and no PC. What each needs, what can go wrong, and who should pick which.
dek: Both routes turn off the same kind of apps. They differ in what you need, how much you trust, and how you undo it.
order: 3
date: 2026-10-20
---
You can debloat a Google TV two ways: ask an AI coding agent such as Claude Code to do it from a laptop, or use a phone app that does the same job with no computer and no AI. The agent route is more flexible. The phone route needs less gear and is easier to undo. Here is how to choose.

"Debloat" means turning off the apps a TV maker preloaded: ads, trackers, demo modes and extras you never asked for. Both routes use the same underlying tool, **ADB** (Android Debug Bridge). It is a way for another device to send commands to your TV over Wi-Fi once you switch on debugging in the TV's settings.

## How did "Claude debloated my TV" become a thing?

Developer Mert Cobanov kicked it off on August 27, 2026. [According to Tom's Hardware](https://www.tomshardware.com/tech-industry/artificial-intelligence/developer-uses-claude-code-to-debloat-android-smart-tv-for-unbelievable-performance-upgrade-tv-now-smoother-than-it-was-new-as-autonomous-agent-deactivates-apps-shortens-animations-all-without-root-access), he told Claude to clean up his four-year-old Android TV over ADB and install a replacement launcher called FLauncher. His [prompt page](https://tv.cobanov.dev/) stresses "No root, 100% reversible."

A second wave began on October 17, when Gathin posted that he had given Claude Code his parents' Xiaomi TV model. His [tweet](https://x.com/gathint/status/2111379569030939102) says it removed 25 packages and built a launcher. His project, [cleanyourtv](https://github.com/Gathin23/cleanyourtv) (first called tv-vibehack), had over 9,000 stars as of October 20, 2026. Ansub's [tv-detox](https://github.com/Ansub/tv-detox) offers a similar prompt and a simple ad-free launcher. TidyTV did not start any of this.

## What does the Claude Code route involve?

[Saascity's guide](https://saascity.io/blog/debloat-android-tv-claude-code) describes three stages: back up the list of apps, turn off apps with `pm disable-user`, then install a replacement home screen (a "launcher"). `pm disable-user` switches an app off without deleting it.

What you need:

- A laptop on the same Wi-Fi as the TV.
- An agent that can run commands. [Mashable](https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup) says pasting the prompt into a regular chatbot won't work.
- A paid plan. [TechPP](https://techpp.com/2026/10/18/speed-up-android-tv-claude-code/) says Claude Pro starts at $20 a month. It also published a guide that needs no subscription.
- Developer options and debugging switched on at the TV.

## What are the strengths of the AI route?

Give it its due. It is the most adaptable option.

- **It handles odd TVs.** An agent can read what is installed on your set and reason about it, rather than following a fixed list.
- **It can do extras.** The TechPP walkthrough, from a Sony Bravia owner, also installed the Projectivy launcher and set up private DNS to block ads.
- **It is free to experiment with** if you already pay for the tool.
- **You can ask it to explain.** You can question each step before it runs.

## What can go wrong with the AI route?

Quite a bit, and reports are public.

- **Boot problems.** One Sony owner was stuck on the Google TV boot screen after a reboot and needed a factory reset ([issue #62](https://github.com/Gathin23/cleanyourtv/issues/62)). On a Xiaomi, disabling the stock launcher crash-looped the system ([#90](https://github.com/Gathin23/cleanyourtv/issues/90)). [TechCrunch](https://techcrunch.com/2026/10/19/people-are-using-ai-to-make-their-smart-tvs-dumb-again/) notes one person said they bricked an Android TV with Claude.
- **Collateral damage.** A OnePlus TV lost its HDMI signal ([#321](https://github.com/Gathin23/cleanyourtv/issues/321)). Disabling one setup app broke casting on a Chromecast with Google TV ([#361](https://github.com/Gathin23/cleanyourtv/issues/361)).
- **Judgment calls.** [Engadget](https://www.engadget.com/2267074/why-claude-cleaning-tv-bloatware-not-best-idea/) warns of an unstable TV, blocked updates and prompt hacking.
- **Wording.** TechCrunch and [It's FOSS](https://itsfoss.com/news/debloat-smart-tv/) report that Claude refuses a request phrased as "hack my TV" and needs a fuller prompt.
- **An open door.** It's FOSS advises turning debugging off afterwards.

Most of the better prompts add safety rails: a backup first, small batches and a never-disable list. Those help, but you are still trusting a model to apply them to your particular TV.

## What does the phone-app route involve?

The phone-app route replaces the laptop and the agent with an app. [TidyTV](/) is one. It is a free, open-source Android phone app. You turn on debugging at the TV, your phone finds the TV on Wi-Fi, and you tap through the steps.

- It saves a backup first, and you can't skip it.
- It shows apps in groups, such as ads and tracking, preloaded apps, voice assistant, casting and updates. The safe groups start switched on.
- It turns off one app at a time, checks it, and stops if anything looks wrong.
- Some apps are never touched, whatever a list says: Play Store, Play Services, keyboards, TV inputs and remote pairing among them.
- "Restore everything" turns everything back on, including your old home screen.

It is not a magic fix. It only runs on an Android phone, and it targets Android TV and Google TV. It turns off the TV maker's ads and trackers but not ads inside apps such as YouTube. How much faster the TV feels depends on the TV.

## Are there other no-agent options?

Yes. [TV Slim](https://tvslim.app/) runs from a Windows PC or an Android phone. Its site describes risk levels for each package, a restore option, and a claim that changes survive system updates. ADB TV is another option, running on the TV itself. If you want to compare, try each one's restore before you trust it.

## So which should you choose?

Choose the agent route if:

- you are comfortable with a terminal and a paid AI tool,
- you want a custom launcher or DNS-level ad blocking,
- you have an unusual TV and enjoy tinkering.

Choose a phone app if:

- you don't have a laptop handy, or don't want to pay for an agent,
- you want a backup and a one-tap undo built in,
- you'd rather follow a per-model list than rely on a model's judgment.

Whichever you pick, keep the same habits: turn off rather than uninstall, change a few apps at a time, and switch debugging off when you finish.

If the phone route sounds right, [TidyTV](/) is free. You can check whether your set has its own list on the [per-model pages](/tv/).
