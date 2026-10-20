---
title: "Debloating by brand: Xiaomi, TCL, Sony and Chromecast with Google TV"
seo_title: Debloat Xiaomi, TCL, Sony and Chromecast TVs | TidyTV
description: Brand-by-brand notes for debloating Xiaomi, TCL, Sony Bravia and Chromecast with Google TV: the known problems, and where to find your model's list.
dek: These four brands show up most in cleanup reports, and each has its own traps.
order: 8
date: 2026-10-20
---
Xiaomi, TCL and Sony Bravia TVs, plus the Chromecast with Google TV, come up most in the recent wave of "clean up your TV" reports. Each has its own trouble spot: Xiaomi and the stock launcher, TCL and black screens, Sony and older software, Chromecast and casting. Always use a list written for your exact model.

Debloating here means turning off pre-installed apps you don't want, without deleting them. It is done with ADB (Android Debug Bridge, a tool that sends instructions to an Android device). The wave began with Mert Cobanov's post on August 27, 2026, and picked up speed when Gathin posted on October 17 ([cleanyour.tv](https://cleanyour.tv/)). The problems below come from public issue reports. They are not a count of how often things go wrong.

## How do you debloat a Xiaomi or Mi TV?

Xiaomi is the most frequently mentioned brand. The story that started the second wave was a Xiaomi TV: Gathin says Claude Code removed 25 packages from his parents' set and built a launcher ([cleanyour.tv](https://cleanyour.tv/)). A launcher is the app that draws your home screen. Reports in the issue tracker cover several models, including the MiTV-MOOR2, MiTV-MOSR4 and a 2026 A Pro.

Know these trouble spots:

- Disabling the stock launcher crash-looped the system on a Xiaomi TV running Google TV 14 ([issue #90](https://github.com/Gathin23/cleanyourtv/issues/90)).
- A Xiaomi boot loop is reported in the tv-detox tracker ([#14](https://github.com/Ansub/tv-detox/issues/14)).
- Some TVs with China firmware refuse third-party launchers ([#37](https://github.com/Gathin23/cleanyourtv/issues/37)).

Model pages with a list for Xiaomi:

- [Xiaomi MiTV-MOOR2](/tv/xiaomi-mitv-moor2/)
- [Xiaomi MiTV-MOSR4](/tv/xiaomi-mitv-mosr4/)
- [Xiaomi MiTV-AXSO0 (Mi TV 4A Pro)](/tv/xiaomi-mitv-axso0/)
- [Xiaomi Mi Box S](/tv/xiaomi-mi-box-s/)

## How do you debloat a TCL Google TV?

TCL is the next most common brand. Reports mention a TCL 98C12L and an ak30a5 from China, and TV Slim says it was tested on a TCL 65C89K ([tvslim.app](https://tvslim.app/)).

The headline risk is a black screen. A TCL Google TV owner reported a black screen loop after a cleanup ([tv-detox #16](https://github.com/Ansub/tv-detox/issues/16)). Add the China-firmware launcher limits above, and TCL owners should be most careful with anything that touches the home screen app.

Model pages with a list for TCL:

- [TCL 65C735](/tv/tcl-65c735/)
- [TCL G10 55" 4K Google TV](/tv/tcl-g10-google-tv/)
- [TCL C755 QD-Mini LED](/tv/tcl-c755-google-tv/)
- [TCL QM6K Pro](/tv/tcl-qm6k-google-tv/)

## How do you debloat a Sony Bravia?

Sony Bravia owners are a big part of the story. The author of the [TechPP walkthrough](https://techpp.com/2026/10/18/speed-up-android-tv-claude-code/) used a Bravia, installed the Projectivy launcher, and used private DNS to deal with ads. Issue reports cover the KD-55X74K, KDL-49W800G, KD-55X8509C (a 2015 set on Android 7) and a K-85XR70.

Know these trouble spots:

- A KD-55X74K stuck on the Google TV boot screen after a restart and needed a factory reset ([issue #62](https://github.com/Gathin23/cleanyourtv/issues/62)).
- Older sets run older Android software, so package names on a newer list may not apply.
- Sony says recommendations on its Android TV models can't be removed, while Google TV models have an "Apps only" mode that hides them, at the cost of Google Assistant ([Sony](https://www.sony.com/electronics/support/articles/00225587)). Try that setting before changing any apps.

Model pages with a list for Sony:

- [Sony BRAVIA 4K GB](/tv/sony-bravia-4k-gb/)
- [Sony BRAVIA BF1](/tv/sony-bravia-bf1/)
- [Sony KD-55X8509C](/tv/sony-kd-55x8509c/)
- [Sony BRAVIA 9 II](/tv/sony-bravia-9-ii/)

## How do you debloat a Chromecast with Google TV?

Both the 4K and HD Chromecast with Google TV appear in the reports. The known trap is casting: disabling a package called `setupwraith` broke casting from phones, with an "Untrusted device" message ([issue #361](https://github.com/Gathin23/cleanyourtv/issues/361)). If you cast from your phone daily, test casting after every batch.

Model page with a list for the Chromecast:

- [Chromecast with Google TV 4K](/tv/google-chromecast-gtv-4k/)

## Is the same list safe on every model?

No. Packages with the same job can have different names between models, and a mistake can mean a boot loop. The projects keep one list per TV for that reason, about 92 as of October 20, 2026 ([cleanyour.tv](https://cleanyour.tv/)). Those lists are still being corrected in open issues, so apply them in small batches and check the picture, the remote and casting each time.

## Why do the same mistakes repeat across brands?

Most failures come from one of three moves: turning off the stock launcher before the replacement works, turning off a package that looks like clutter but carries a core job (inputs, casting, setup), and applying a big batch at once so you can't tell which change broke things. The Engadget and AI In Context critiques make a similar point about speed over care ([Engadget](https://www.engadget.com/2267074/why-claude-cleaning-tv-bloatware-not-best-idea/), [AI In Context](https://www.aiincontext.com/articles/a-developer-let-claude-debloat-his-tv-here-s-why-you-shouldn-t-b93f9fcd)). Small batches and a backup address all three.

## What if your brand is not here?

Others named in reports include Acer, Hisense, OnePlus, Philips, Sharp, Panasonic, Realme, Airtel Xstream and the Nvidia Shield (2019). Samsung (Tizen), LG (webOS) and Roku are not Android TV, so the Android prompts don't apply to them ([Mashable](https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup)). Fire TV is a partial case: one guide says it gets the debloat half only ([saascity](https://saascity.io/blog/debloat-android-tv-claude-code)).

## What is the safest way to start?

1. Find your exact model. If it isn't listed, be extra cautious.
2. Back up what is currently on.
3. Turn things off in small batches, never deleting.
4. Test after each batch, and know how to turn it all back on.

[TidyTV](/) is a phone app that runs these steps without a laptop: it saves a backup, turns apps off rather than deleting them, and has a one-tap restore. Browse the [full list of TV models](/tv/) to see whether yours is covered. It is not the only way. TV Slim and ADB TV are other no-laptop tools, and the AI route from Cobanov, Gathin, Ansub and others suits people who like to tinker.
