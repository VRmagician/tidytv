---
title: Which Android TV apps are safe to disable, and which you should never touch
seo_title: Which Android TV apps are safe to disable? | TidyTV
description: A plain guide to Android TV bloatware: which apps are safe to turn off, which break HDMI, remotes or casting, and why your model's list matters.
dek: Most pre-installed apps can be turned off safely. A few will take your inputs, remote or casting with them.
order: 7
date: 2026-10-20
---
Most of the apps that came with your Android TV can be turned off without trouble, but some run the basics: HDMI inputs, the remote, the keyboard and Google Play services. The safe approach is to turn off (not delete) a few apps at a time, never touch the essentials, and keep a way to turn everything back on.

## What is the difference between disabling and uninstalling?

Disabling hides an app and stops it running, but the app stays on the TV. Uninstalling removes it. Most TV cleanup guides and AI prompts use disabling, through a command called `pm disable-user` sent over ADB (Android Debug Bridge, a tool that lets a computer or app send instructions to your TV). Mert Cobanov's prompt page says nothing is uninstalled, so every step can be reversed ([tv.cobanov.dev](https://tv.cobanov.dev/)). Ansub's [tv-detox](https://github.com/Ansub/tv-detox) also turns apps off and never deletes them.

That is why disabling is the right default. If something breaks, you turn the app back on.

## Which apps should you never disable?

The never-disable list in the popular cleanup prompts is similar from project to project. The groups are:

- **HDMI and tuner input services.** These carry the picture from your console, cable box or antenna.
- **The source or input menu.** This is how you switch between inputs.
- **Remote control and Bluetooth pairing.** Without them the remote may stop working.
- **The keyboard.** You need it to type passwords and search.
- **Google Play services.** Many other apps depend on it.

These groups appear in the [saascity guide](https://saascity.io/blog/debloat-android-tv-claude-code) and in tv-detox. It's FOSS puts the principle in a few words: not every background service is bloat ([It's FOSS](https://itsfoss.com/news/debloat-smart-tv/)).

## What actually breaks when people get it wrong?

Real reports from the cleanyourtv issue tracker show what the list is protecting:

- **HDMI "No Signal."** After a cleanup on a OnePlus TV, the screen showed "No Signal" on HDMI ([issue #321](https://github.com/Gathin23/cleanyourtv/issues/321)).
- **Casting stopped.** On a Chromecast with Google TV, disabling a package called `setupwraith` broke casting and the phone reported an "Untrusted device" message ([issue #361](https://github.com/Gathin23/cleanyourtv/issues/361)).
- **Stuck on the boot screen.** A Sony set stayed on the Google TV boot screen after a restart and needed a factory reset ([issue #62](https://github.com/Gathin23/cleanyourtv/issues/62)).

These are reports from individual users, not a count of how often it happens. They are a good reason to go slowly. TechCrunch also notes that one person said they bricked an Android TV using Claude ([TechCrunch](https://techcrunch.com/2026/10/19/people-are-using-ai-to-make-their-smart-tvs-dumb-again/)).

## What about the voice assistant, Chromecast and updates?

These are your decision, not a default either way. It's FOSS says a good cleanup asks you whether to keep the voice assistant, Chromecast and system updates.

- **Voice assistant.** Turn it off and the voice button may stop working. Sony notes that Google Assistant is unavailable in "Apps only" mode ([Sony](https://www.sony.com/electronics/support/articles/00225587)).
- **Chromecast (casting from your phone).** As issue #361 shows, a package that looks like setup clutter can be part of the casting chain.
- **System updates.** Engadget warns that disabling key packages may block future system updates ([Engadget](https://www.engadget.com/2267074/why-claude-cleaning-tv-bloatware-not-best-idea/)). One issue is titled "Safe handling of firmware updates" ([#359](https://github.com/Gathin23/cleanyourtv/issues/359)).

If you stream with casting every night, keep it. If you never speak to your remote, the voice apps are an easy cut.

## What is usually safe to disable?

The research behind this guide does not give a universal list of safe packages, and that is the point. The apps people go after are the ones behind what one user, quoted by [Mashable](https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup), called "ads, bloatware, spyware [and] laggy UI". But the name of a package means different things on different TVs, so a generic "safe list" is a bad idea.

## Why does your exact model matter?

The same job is done by different packages on different brands. The list for the [Xiaomi MiTV-MOOR2](/tv/xiaomi-mitv-moor2/) notes that on its firmware the stock launcher is `com.google.android.apps.tv.launcherx` and not the more common `com.google.android.tvlauncher`. A list written for one TV can be wrong for another, and "wrong" can mean a boot loop. This is why the cleanyourtv repo keeps a separate list per model, about 92 TVs as of October 20, 2026 ([cleanyour.tv](https://cleanyour.tv/)). Those lists are community-tested and still being corrected, so treat them as a careful start, not a guarantee.

## How do you keep this safe?

1. **Back up first,** so you know exactly what was on, and can put it back.
2. **Disable in small batches.** Test the picture, the remote and casting after each one.
3. **Use a list for your model,** not a generic one.
4. **Install any replacement home screen before you turn off the stock one.**
5. **Know how to restore.** A factory reset is the last resort, not the plan.

[TidyTV](/) follows this pattern from your phone, without a laptop: it saves a backup, turns apps off instead of deleting them, and restores with one tap. Look up your TV on the [per-model pages](/tv/) to see what applies. TV Slim ([tvslim.app](https://tvslim.app/)) also shows a risk level per package and offers a restore.

## What if something goes wrong anyway?

Don't panic and don't keep disabling things to fix it. Turn back on the last batch you changed, then restart the TV. The cleanup prompts are built around that idea, and a backup of what you changed is how you know what to turn on. A factory reset, as in issue #62, is the last resort because it wipes your settings and logins.

Setup problems are common too: some TVs don't offer wireless debugging or pairing codes ([#101](https://github.com/Gathin23/cleanyourtv/issues/101), [#227](https://github.com/Gathin23/cleanyourtv/issues/227)). If your TV won't connect, stop there. It is not a reason to try riskier methods.

## Can you undo it all?

Yes, as long as you only disabled things. Turning a package back on reverses it, which is why backing up the list of what you changed is worth the effort.
