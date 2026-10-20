---
title: How to remove ads from the Google TV home screen (what works and what doesn't)
seo_title: Remove ads from the Google TV home screen: what works | TidyTV
description: Google says you can't switch off Google TV home screen ads. Here is what Apps only mode, the ad ID setting and a replacement launcher really do.
dek: There is no ads off switch, but there are three real ways to get a quieter home screen, each with a trade-off.
order: 6
date: 2026-10-20
---
You can't switch off the ads on a Google TV home screen with a setting. Google says so itself. What you can do is hide the recommendations with "Apps only" mode, limit ad tracking, or replace the home screen with a different launcher (the launcher is the app that draws your home screen). Each has a cost, covered below.

## Can you turn off ads on Google TV?

No. Google's own support page says you can't turn off ads on Google TV. It only lets you [reset or delete your advertising ID](https://support.google.com/googletv/answer/13392198?hl=en), which changes how targeted the ads are, not whether they appear.

You are not imagining how common it is. A Mashable explainer [cites a TiVo finding](https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup) that more than one third of people report too many ads on their TV's home page. Gathin, the developer behind the cleanyourtv project, describes a "sponsored ad before you could even get to Netflix" as part of why he cleaned up his parents' TV ([cleanyour.tv](https://cleanyour.tv/)).

## What does the advertising ID setting do?

It tells Google to stop using your viewing profile to pick ads. Per Google's page, that is all it does. The home screen still shows promoted rows. Think of it as a privacy tweak, not an ad blocker. It is worth doing, because it costs you nothing, but don't expect the home screen to look different afterwards.

## What is Apps only mode?

Sony's support page for its TVs explains that Google TV models have an ["Apps only" mode](https://www.sony.com/electronics/support/articles/00225587). It strips the home screen back to your apps, so the recommendation rows and the promoted content go away. This is the lightest fix, because it is a setting and not a change to the TV's apps.

Two limits to know:

- **Google Assistant is unavailable in Apps only mode,** according to Sony. If you use the voice button on your remote, this is a real loss.
- **Older Android TV models don't get it.** Sony says recommendations on Android TV (the older software that Google TV replaced) can't be removed. The mode belongs to Google TV.

Other brands may label things differently, and the research behind this guide only confirms the Sony wording. Check your own TV's settings under the home screen or account options.

## What about replacing the home screen?

This is the route behind the recent "clean up your TV with an AI" trend. The idea has two steps:

1. Install a replacement launcher. Names that come up are FLauncher, Projectivy and ATV Launcher ([TechCrunch](https://techcrunch.com/2026/10/19/people-are-using-ai-to-make-their-smart-tvs-dumb-again/), [Engadget](https://www.engadget.com/2267074/why-claude-cleaning-tv-bloatware-not-best-idea/)). A launcher like this shows your apps in a plain grid, with no promoted rows.
2. Turn off the stock launcher and the other apps that feed it, so Home opens the new one.

Mert Cobanov described this approach on August 27, 2026, when he had Claude remove apps from his four-year-old Android TV and install FLauncher ([Tom's Hardware](https://www.tomshardware.com/tech-industry/artificial-intelligence/developer-uses-claude-code-to-debloat-android-smart-tv-for-unbelievable-performance-upgrade-tv-now-smoother-than-it-was-new-as-autonomous-agent-deactivates-apps-shortens-animations-all-without-root-access)). Gathin's October 17 post, and his repo (first called tv-vibehack, now [cleanyourtv](https://github.com/Gathin23/cleanyourtv)), picked it up. Ansub's [tv-detox](https://github.com/Ansub/tv-detox) is a similar prompt with an ad-free launcher.

The apps are turned off with a command called `pm disable-user`, sent over ADB. ADB (Android Debug Bridge) is a built-in tool that lets a computer send instructions to an Android device. "Disable" is not "uninstall". The app stays on the TV and you can switch it back on ([It's FOSS](https://itsfoss.com/news/debloat-smart-tv/)).

## What can go wrong with the launcher route?

Turning off the stock launcher is the riskiest step, and the issue trackers show it:

- On a Xiaomi TV running Google TV 14, disabling the stock launcher sent the system into a crash loop ([cleanyourtv issue #90](https://github.com/Gathin23/cleanyourtv/issues/90)).
- A TCL Google TV owner reported a black screen loop ([tv-detox issue #16](https://github.com/Ansub/tv-detox/issues/16)).
- Some TVs sold with China firmware refuse third-party launchers ([issue #37](https://github.com/Gathin23/cleanyourtv/issues/37)).

The order matters too. It's FOSS flags launcher order as a gotcha, so install and test the replacement before turning off the old one. Also keep in mind that turning off the stock launcher can cost you things the TV relied on, like voice search.

If you used a computer to do this, turn off USB or network debugging afterwards. It's FOSS recommends closing that door, and one issue asks for it as a built-in final step ([#164](https://github.com/Gathin23/cleanyourtv/issues/164)).

## Which option should you pick?

Start at the bottom of the effort ladder and stop when you are happy.

1. **Reset your advertising ID.** It is a setting, so it carries no risk to the TV.
2. **Try Apps only mode** if your TV is on Google TV. Accept losing Assistant, or switch back.
3. **Use a replacement launcher** only if the ads still bother you, and only with a way back.

The third option is where having a backup and an undo button matters most. [TidyTV](/) is a phone app that does this without a laptop: it saves a backup first, turns apps off instead of deleting them, and has a one-tap restore. It works from per-model lists, so you can look up your TV on the [model pages](/tv/) first. It is one route among several. TV Slim ([tvslim.app](https://tvslim.app/)) also runs from a phone or a Windows PC, and ADB TV is an app that runs on the TV itself.

## Will the ads come back?

Possibly. TV Slim says its changes survive system updates, but Engadget warns that disabling key packages may block future updates. Treat a cleanup as something you may need to check after a big software update, and keep your restore option handy.
