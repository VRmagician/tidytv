---
title: The risks of letting an AI agent loose on your TV
seo_title: Is it safe to debloat your TV with Claude or AI? | TidyTV
description: Is it safe to debloat an Android TV or Google TV with Claude? The real failures people reported, what experts warn about, and how to lower the risk.
dek: Real boot loops, a black HDMI screen and broken casting. Here is what went wrong and how to avoid it.
order: 2
date: 2026-10-20
---
It is mostly safe if you do it carefully, but it is not risk free. People who let an AI agent clean up their Android TV or Google TV have reported boot loops, lost HDMI inputs and broken casting. Nearly all of those problems come from turning off the wrong app, and nearly all can be avoided with a backup, small steps and a short list of apps that must never be touched.

## Can an AI really brick a TV?

[TechCrunch](https://techcrunch.com/2026/10/19/people-are-using-ai-to-make-their-smart-tvs-dumb-again/) reported that one person said they bricked their Android TV using Claude. We only know that from the report. But the public issue tracker for [cleanyourtv](https://github.com/Gathin23/cleanyourtv), the most popular project behind the trend, shows how a TV can end up in trouble:

- **Stuck on the boot screen.** A Sony TV hung on the Google TV boot screen after a restart and needed a factory reset ([issue 62](https://github.com/Gathin23/cleanyourtv/issues/62)).
- **A crash loop.** On a Xiaomi TV running Google TV 14, disabling the stock launcher made a core system process crash again and again ([issue 90](https://github.com/Gathin23/cleanyourtv/issues/90)).
- **A black screen.** A TCL Google TV got into a black screen loop, and a Xiaomi set into a boot loop, in reports on [tv-detox](https://github.com/Ansub/tv-detox/issues/16) ([and another](https://github.com/Ansub/tv-detox/issues/14)).
- **No signal on HDMI.** A OnePlus TV lost its picture from connected devices after a cleanup ([issue 321](https://github.com/Gathin23/cleanyourtv/issues/321)).
- **Casting stopped.** Disabling an app called `setupwraith` on a Chromecast with Google TV caused an "Untrusted device" message when casting ([issue 361](https://github.com/Gathin23/cleanyourtv/issues/361)).

These are individual reports on open projects, not a count of how often things fail. Many people say it went fine. The takeaway is that the failures cluster around the same few things: the home screen, the HDMI inputs and setup services.

## What do the critics say?

[Engadget](https://www.engadget.com/2267074/why-claude-cleaning-tv-bloatware-not-best-idea/) and [AI In Context](https://www.aiincontext.com/articles/a-developer-let-claude-debloat-his-tv-here-s-why-you-shouldn-t-b93f9fcd) both argue against handing this to an agent. Their concerns, in plain terms:

- **Mistakes.** An AI can be confidently wrong about what a package does, and a TV maker may name the same service differently on each model.
- **Updates.** Engadget warns that turning off key packages may block future system updates. A cleanyourtv issue titled "Safe handling of firmware updates" ([issue 359](https://github.com/Gathin23/cleanyourtv/issues/359)) shows users share the worry.
- **Prompt hacking.** An agent that reads web pages or files could be steered by hidden instructions inside them.
- **Overkill.** AI In Context says the manual route, installing a launcher and clearing caches, is shorter than people expect.

[It's FOSS](https://itsfoss.com/news/debloat-smart-tv/) makes a point that sits underneath all of these: not every background service is bloat.

## What is the debugging port, and why close it?

To work, the agent talks to your TV through ADB, a built-in Android tool, over your network. That means turning on debugging on the TV. It's FOSS advises turning debugging off again afterwards, and a cleanyourtv issue ([issue 164](https://github.com/Gathin23/cleanyourtv/issues/164)) asks for a closing step to be added. Leaving it on is not a catastrophe on a home network, but there is no reason to keep a door open.

## How do you lower the risk?

The better prompts already contain safety rails. Copy the ideas even if you write your own:

1. **Back up first.** Record what is installed and enabled before changing anything. Keep that record somewhere other than the TV.
2. **Switch off, never uninstall.** The `pm disable-user` command stops an app but leaves it on the TV, so you can turn it back on. A deleted app is much harder to bring back.
3. **Use a never-touch list.** Per the prompts described by [saascity](https://saascity.io/blog/debloat-android-tv-claude-code) and tv-detox, that list includes HDMI and tuner input services, the source menu, remote and Bluetooth pairing, the keyboard and Google Play services.
4. **Go in small batches.** Disable a few apps, restart, check that Home, HDMI, remote and casting still work, then continue.
5. **Mind the launcher order.** Install the new home screen and confirm it starts before you disable the stock one. The It's FOSS guide flags this order as a common gotcha.
6. **Decide on voice, Chromecast and updates yourself.** Sony notes that Google Assistant is unavailable in its Apps only mode, so these are real trade-offs ([Sony](https://www.sony.com/electronics/support/articles/00225587)).
7. **Read each plan before it runs.** Ask the agent to list what it will turn off and why, and say no to anything you do not understand.
8. **Check your model.** A list built for a different brand is a guess. Look for reports from the same model before you start.

## What does "reversible" really mean?

It means the apps are still there and can be switched back on. It does not mean every problem is fixed with one tap. If a TV will not finish booting, you may not be able to reach the commands at all, which is why the Sony report ended with a factory reset. Reversible is a strong reason to disable instead of uninstall. It is not a replacement for going slowly.

## Where does TidyTV fit?

[TidyTV](/) is a free, open-source Android phone app that applies those rails by default. A backup is saved before any change and cannot be skipped. It never uninstalls. It keeps the Play Store, Play Services, keyboards, TV inputs and remote pairing untouched whatever a list says. It turns apps off one at a time, stops if anything unexpected happens, and offers a **Restore everything** button. It is not the only option: [TV Slim](https://tvslim.app/) also offers restore and risk levels per package, and ADB TV runs on the TV itself.

An app cannot remove all risk, and TidyTV's lists still come from the same community work as the agent route. If you want to check whether your TV has a list before you start, the [per-model pages](/tv/) are the place to look.

## So should you do it?

If you go carefully, yes, with any of these routes. The people who got hurt were mostly the ones who let a tool disable many things at once without a way back. Take a backup, start small and keep the TV's original home screen until the new one has proved itself.
