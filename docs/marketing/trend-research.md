# TidyTV trend research: "Claude debloated my TV"

Researched 2026-10-20 (read-only; nothing posted, no logins).

**Method and reach notes**
- Opened directly: GitHub (via `gh`), cleanyour.tv, tv.cobanov.dev, TechCrunch, Mashable, It's FOSS, TechPP, Engadget, AI In Context, tvslim.app, HN Algolia API, YouTube metadata (`yt-dlp`).
- **Reddit: not reachable.** Direct and Jina fetches returned 403, and the OpenCLI browser extension isn't connected. Reddit facts below come from Exa search snippets (titles, scores, comment text), marked "(snippet)".
- **X/Twitter: not opened directly.** Tweet text and likes come from cleanyour.tv, TechCrunch and Mashable quoting them.
- **Partial opens:** Tom's Hardware, The Verge and FlatpanelsHD returned only the headline, nav or a bot wall. For Tom's Hardware I also have Exa's article excerpt. For The Verge and FlatpanelsHD I have headline and URL only.
- Exa's free rate limit hit near the end, so a few extra X and Fire TV/Samsung searches did not run.
- **Timing:** this is two waves, not one 8-week trend. Wave 1 ran late Aug to Sept 2026 (Mert Cobanov). Wave 2 started Oct 17 (Gathin, cleanyourtv) and is 3 days old and still moving fast. All GitHub numbers are as of 2026-10-20.

## Summary

- **Wave 2 exploded in 3 days.** Gathin's Oct 17 tweet got 31,098 likes and 778 replies. His repo, now `Gathin23/cleanyourtv` (originally `tv-vibehack`), was created Oct 17 and has 9,324 stars, 1,056 forks and 61+ open issues as of Oct 20 ([cleanyour.tv](https://cleanyour.tv/), `gh repo view`).
- **Mainstream press picked it up within 72 hours.** TechCrunch ("People are using AI to make their smart TVs dumb again"), Mashable, It's FOSS, TechPP, The Verge (headline only) and a dozen non-English outlets, all listed on [cleanyour.tv](https://cleanyour.tv/). Wave 1 had Tom's Hardware (Sept 2) and Engadget (Sept 27).
- **The approach is a laptop, a coding agent and ADB.** Enable Developer options, turn on USB or network debugging, paste a prompt into Claude Code or Codex. `pm disable-user` handles the apps and a replacement launcher handles the home screen ([It's FOSS](https://itsfoss.com/news/debloat-smart-tv/), [PROMPT.md](https://github.com/Gathin23/cleanyourtv/blob/main/PROMPT.md)). This matches TidyTV's mechanics, but TidyTV needs no laptop and no agent.
- **Skeptics and pain points are loud.** Engadget and AI In Context argue against letting an agent loose on a TV. TechCrunch notes one person said they bricked their Android TV with Claude. The cleanyourtv issue tracker is full of boot loops, "No Signal" HDMI and lost casting ([#62](https://github.com/Gathin23/cleanyourtv/issues/62), [#90](https://github.com/Gathin23/cleanyourtv/issues/90), [#361](https://github.com/Gathin23/cleanyourtv/issues/361)). Reversible backup-first is the gap TidyTV can own.
- **Phone-based, no-PC tools exist and are small.** TV Slim (Windows PC or Android phone, [tvslim.app](https://tvslim.app/)) and ADB TV (on-TV app) are the visible competitors. Fire TV, Samsung and LG are the most-asked unsupported platforms, and TidyTV should say plainly which it covers.

## Sources

Dates are publication or post dates. "Eng." is engagement as visible to me.

| # | Platform | Date | URL | One-line summary | Eng. |
|---|---|---|---|---|---|
| 1 | X (via cleanyour.tv) | 2026-10-17 | https://x.com/gathint/status/2111379569030939102 | Gathin: gave Claude Code his parents' Xiaomi TV model, it removed 25 packages and built a launcher | 31,098 likes, 778 replies |
| 2 | GitHub | created 2026-10-17 | https://github.com/Gathin23/cleanyourtv | Prompt, ADB tools, launcher, per-model debloat lists. Pushed 2026-10-20 | 9,324 stars, 1,056 forks, ~61 open issues |
| 3 | Site | 2026-10-17 on | https://cleanyour.tv/ | Project site. Claims 93 TVs from 31 brands in the first 48h, lists press coverage | n/a |
| 4 | GitHub | created 2026-10-18 | https://github.com/Ansub/tv-detox | Claude Code/Codex prompt plus an ad-free launcher. Apps turned off, never deleted | 773 stars, 83 forks |
| 5 | GitHub | created 2026-10-18 | https://github.com/humblemuzzu/dumbtv | "No ads from the TV itself, no watch tracking." Tested on Fire TV Edition and Android/Google TV | 804 stars, 72 forks |
| 6 | News | 2026-10-19 | https://techcrunch.com/2026/10/19/people-are-using-ai-to-make-their-smart-tvs-dumb-again/ | Covers dumbtv, cleanyourtv and Ryan Doyle's "Can you hack this?" post. Notes one reported brick and Claude refusing "hack my TV" | n/a |
| 7 | News | 2026-10-19 | https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup | Explainer and steps. Cites TiVo's finding that over one-third report excessive home-page ads | n/a |
| 8 | News | 2026-10-19 | https://itsfoss.com/news/debloat-smart-tv/ | Explains how it works, gotchas (disable not uninstall, launcher order, close ADB port) | n/a |
| 9 | Blog/News | 2026-10-18 | https://techpp.com/2026/10/18/speed-up-android-tv-claude-code/ | Sony Bravia owner walkthrough: Claude Code, Projectivy launcher, private DNS for ads | n/a |
| 10 | YouTube | 2026-10-18 | https://www.youtube.com/watch?v=fP2SGGVsaB0 | "Claude Made My Old Android TV Feel Brand New!" (Ansub Khan) | 432,283 views, 10,550 likes |
| 11 | YouTube | 2026-10-18 | https://www.youtube.com/watch?v=YAaxqBvd1sY | "How to debloat your Android TV with Claude Code" (TechPP) | 26,718 views, 476 likes |
| 12 | YouTube | 2026-10-19 | https://www.youtube.com/watch?v=TrgzQAbKIgA | "Claude Just Made My Old Android TV Feel Brand New" (Codedigipt) | 14,606 views, 98 likes |
| 13 | Hacker News | 2026-10-20 | https://news.ycombinator.com/item?id=50164485 | "Strip the Bloatware from Any Android TV or Google TV over Wi-Fi" (API data) | 4 pts, 0 comments (very early) |
| 14 | News | 2026-10-18 | https://www.theverge.com/ai-artificial-intelligence/1013505/maybe-claude-make-your-android-tv-less-crappy-too | The Verge: "Maybe Claude can make your Android TV less crappy too." Only headline retrieved | n/a |
| 15 | News | 2026-10-20 | https://www.flatpanelshd.com/news.php?id=1792478262&subaction=showfull | FlatpanelsHD: "People are using AI to clean up their Smart TV homescreens." Headline only (bot wall) | n/a |
| 16 | Blog | 2026-10-19 | https://saascity.io/blog/debloat-android-tv-claude-code | Guide. Says repo had 3,378 stars after 2 days and was renamed from tv-vibehack on Oct 19. Fire TV gets debloat only; Samsung/LG/Roku out | n/a |
| 17 | X (via Tom's Hardware) | 2026-08-27 | https://x.com/mertcobanov/status/2092980660386378154 | Wave 1 origin: Mert Cobanov told Claude to ADB-debloat his 4-year-old Android TV and install FLauncher | n/a |
| 18 | Site | 2026-08/09 | https://tv.cobanov.dev/ | Wave 1 prompt page: "No root, 100% reversible," disable-user only, backup first | n/a |
| 19 | News | 2026-09-02 | https://www.tomshardware.com/tech-industry/artificial-intelligence/developer-uses-claude-code-to-debloat-android-smart-tv-for-unbelievable-performance-upgrade-tv-now-smoother-than-it-was-new-as-autonomous-agent-deactivates-apps-shortens-animations-all-without-root-access | Tom's Hardware on Cobanov (page body not retrievable, text from Exa excerpt) | Aggregator showed 228 pts |
| 20 | Reddit r/tomshardware (snippet) | 2026-09-02 | https://www.reddit.com/r/tomshardware/comments/1w57abw/developer_uses_claude_code_to_debloat_android/ | Thread on #19. A commenter says non-developers can do it by giving Claude the TV's IP | 90 pts, 25 comments |
| 21 | Reddit r/vibecoding (snippet) | 2026-09-07 | https://www.reddit.com/r/vibecoding/comments/1w9tcdb/claude_just_rooted_my_tv_which_ive_been_chasing/ | "Claude just rooted my TV." Comments ask for Samsung help and report telemetry findings | 288 pts, 46 comments |
| 22 | News | 2026-09-27 | https://www.engadget.com/2267074/why-claude-cleaning-tv-bloatware-not-best-idea/ | Engadget: why letting Claude clean your TV isn't the best idea. Risks: unstable TV, blocked updates, prompt hacking | n/a |
| 23 | News | 2026-09-27 | https://www.aiincontext.com/articles/a-developer-let-claude-debloat-his-tv-here-s-why-you-shouldn-t-b93f9fcd | Same critique. Says the manual route (FLauncher, cache clearing) is shorter than people think | n/a |
| 24 | Reddit r/AndroidTV (snippet) | 2026-02-25 | https://www.reddit.com/r/AndroidTV/comments/1refcgt/guide_to_debloat_androidgoogle_tv_max/ | Pre-trend baseline: Hisense owner asks how to strip an Android TV to the minimum | 4 pts, 10 comments |
| 25 | Site | 2026-10-08 | https://tvslim.app/ | Competitor: TV Slim, from a Windows PC or Android phone. Risk levels per package, restore, "survives system updates" | n/a |
| 26 | GitHub issues | 2026-10-18 to 20 | https://github.com/Gathin23/cleanyourtv/issues | Bug and device reports from real users (see Pain points) | n/a |

## Phrases people use

Quotes are short and attributed to where I saw them.

**Describing the trend**
- "Vibe coded my parents' TV this afternoon!" (Gathin's tweet, [cleanyour.tv](https://cleanyour.tv/))
- "Looks like an Apple TV now" (same tweet)
- "vibe-coding your TV" ([TechCrunch](https://techcrunch.com/2026/10/19/people-are-using-ai-to-make-their-smart-tvs-dumb-again/))
- "make their smart TVs dumb again" (TechCrunch headline)
- "dumb TV" hacks (TechCrunch); "Your TV, minus the ads." ([cleanyour.tv](https://cleanyour.tv/))
- "Claude Made My Old Android TV Feel Brand New" ([YouTube](https://www.youtube.com/watch?v=fP2SGGVsaB0), 432k views)
- "Can you hack this?" (Ryan Doyle's prompt, per [TechCrunch](https://techcrunch.com/2026/10/19/people-are-using-ai-to-make-their-smart-tvs-dumb-again/))
- "Claude just rooted my TV" ([r/vibecoding](https://www.reddit.com/r/vibecoding/comments/1w9tcdb/claude_just_rooted_my_tv_which_ive_been_chasing/), snippet)

**What people ask the AI to do**
- "clean up my Android TV over ADB" ([tv.cobanov.dev](https://tv.cobanov.dev/))
- "Help me clean up my Android TV and make it faster." ([tv-detox](https://github.com/Ansub/tv-detox))
- "Hey Claude, my Android TV is at 192.168.0.203. fix it!" (commenter, [r/tomshardware](https://www.reddit.com/r/tomshardware/comments/1w57abw/developer_uses_claude_code_to_debloat_android/), snippet)
- "hack my TV" (people said Claude refused this phrasing, per [TechCrunch](https://techcrunch.com/2026/10/19/people-are-using-ai-to-make-their-smart-tvs-dumb-again/) and [It's FOSS](https://itsfoss.com/news/debloat-smart-tv/))

**Problem and result language**
- "ads, bloatware, spyware [and] laggy UI" (a user quoted by [Mashable](https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup))
- "sponsored ad before you could even get to Netflix" (Gathin, [cleanyour.tv](https://cleanyour.tv/))
- "runs smoother than when it was new" (Cobanov, [Tom's Hardware excerpt](https://www.tomshardware.com/tech-industry/artificial-intelligence/developer-uses-claude-code-to-debloat-android-smart-tv-for-unbelievable-performance-upgrade-tv-now-smoother-than-it-was-new-as-autonomous-agent-deactivates-apps-shortens-animations-all-without-root-access))
- "Nothing is uninstalled, so every step can be reversed" ([tv.cobanov.dev](https://tv.cobanov.dev/) via Tom's Hardware)
- "How do you remove the shitware?" ([r/AndroidTV 2024](https://www.reddit.com/r/AndroidTV/comments/1aqjtu4/how_to_remove_ads_from_tv/), snippet)
- "debloat my older Samsung tv so it doesn't drag for forty seconds on boot" ([r/vibecoding](https://www.reddit.com/r/vibecoding/comments/1w9tcdb/claude_just_rooted_my_tv_which_ive_been_chasing/), snippet)
- "Do you plan to support cheaper Android TVs?" (issue [#222](https://github.com/Gathin23/cleanyourtv/issues/222))
- "When will it be able for FIRE TV?" (issue [#79](https://github.com/Gathin23/cleanyourtv/issues/79))

**Search-style phrasings** (from titles and sites, not search-volume data)
- "debloat Android TV with Claude Code" (YouTube/blog titles, [saascity](https://saascity.io/blog/debloat-android-tv-claude-code), [TechPP](https://techpp.com/2026/10/18/speed-up-android-tv-claude-code/))
- "debloat Android TV without root" ([TV Slim](https://tvslim.app/), [Tom's Hardware](https://www.tomshardware.com/tech-industry/artificial-intelligence/developer-uses-claude-code-to-debloat-android-smart-tv-for-unbelievable-performance-upgrade-tv-now-smoother-than-it-was-new-as-autonomous-agent-deactivates-apps-shortens-animations-all-without-root-access))
- "Google TV slow", "Google TV ads": no verbatim hits. The closest are the Sony and Google support pages on hiding ads ([Sony](https://www.sony.com/electronics/support/articles/00225587), [Google](https://support.google.com/googletv/answer/13392198?hl=en)).
- I did **not** find a verbatim "without PC" phrase in a user post. TV Slim's page states "from a Windows PC or an Android phone."

## Pain points

- **Fear of bricking or breaking Home.**
  - TechCrunch says one person claimed to have bricked an Android TV using Claude.
  - Reported failures: stuck on the Google TV boot screen after reboot, needing a factory reset (Sony KD-55X74K, [#62](https://github.com/Gathin23/cleanyourtv/issues/62)); stock-launcher disable crash-looping `system_server` on Google TV 14 (Xiaomi, [#90](https://github.com/Gathin23/cleanyourtv/issues/90)); black screen loop on a TCL Google TV ([tv-detox #16](https://github.com/Ansub/tv-detox/issues/16)); Xiaomi boot loop ([tv-detox #14](https://github.com/Ansub/tv-detox/issues/14)).
  - A r/vibecoding commenter: "I remember bricking phones back in the day" (snippet).
- **Which apps are safe.**
  - The prompts carry a never-disable list: HDMI and tuner input services, the source menu, remote and Bluetooth pairing, keyboard, Play services ([saascity](https://saascity.io/blog/debloat-android-tv-claude-code), [tv-detox](https://github.com/Ansub/tv-detox)).
  - It's FOSS: "not every background service is bloat."
  - Collateral damage seen: OnePlus TV debloat caused HDMI "No Signal" ([#321](https://github.com/Gathin23/cleanyourtv/issues/321)); disabling `setupwraith` broke casting with "Untrusted device" on Chromecast with Google TV ([#361](https://github.com/Gathin23/cleanyourtv/issues/361)).
- **Losing voice, Chromecast, updates.** The checklist asks whether to keep the voice assistant, Chromecast and updates ([It's FOSS](https://itsfoss.com/news/debloat-smart-tv/)). Sony's own note says Google Assistant is unavailable in Apps only mode ([Sony](https://www.sony.com/electronics/support/articles/00225587)). Issue [#359](https://github.com/Gathin23/cleanyourtv/issues/359) is titled "Safe handling of firmware updates." Engadget warns disabled key packages may block future system updates.
- **Needing a laptop and a paid agent.** The method needs a computer on the same Wi-Fi plus an agent with shell access (Mashable: pasting into a regular chatbot won't work). TechPP says Claude Pro starts at $20 per month, and added a no-subscription ADB guide. Issues ask for the launcher APK separately ([#134](https://github.com/Gathin23/cleanyourtv/issues/134), [#170](https://github.com/Gathin23/cleanyourtv/issues/170)). TV Slim and ADB TV are the no-agent options.
- **AI reliability and safety.** Claude refusing "hack my TV" and needing a fuller prompt (TechCrunch, It's FOSS). Engadget and AI In Context cite mistakes, prompt-hacking and instability. An API 400 error shows up in [#364](https://github.com/Gathin23/cleanyourtv/issues/364).
- **Open ADB port left on.** It's FOSS says to turn debugging off afterward. Issue [#164](https://github.com/Gathin23/cleanyourtv/issues/164) asks for a closing step.
- **Setup friction.** Wireless debugging or pairing codes unavailable on some TVs ([#101](https://github.com/Gathin23/cleanyourtv/issues/101), [#227](https://github.com/Gathin23/cleanyourtv/issues/227)). Some China-firmware TVs refuse third-party launchers ([#37](https://github.com/Gathin23/cleanyourtv/issues/37)). Some boxes cut USB power while debugging is on ([saascity](https://saascity.io/blog/debloat-android-tv-claude-code)).
- **Fire TV, Samsung, LG, Roku.**
  - Not supported by the Android/Google TV prompt ([PROMPT.md](https://github.com/Gathin23/cleanyourtv/blob/main/PROMPT.md), [Mashable](https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup)).
  - saascity says Fire TV gets the debloat half only.
  - Samsung demand is visible in r/vibecoding comments (snippet) and Engadget notes Samsung's OS takes about 20% of storage.
- **Google TV home-screen ads.** Google says "You can't turn off ads" on Google TV, only the advertising ID ([Google](https://support.google.com/googletv/answer/13392198?hl=en)). Sony says Android TV recommendations can't be removed, and Google TV has "Apps only" mode ([Sony](https://www.sony.com/electronics/support/articles/00225587)). Mashable cites TiVo: over one-third report excessive home-page ads.
- **Privacy and ACR.** TechCrunch and Engadget explain ACR (screenshots, audio fingerprinting). A r/vibecoding commenter says Claude found telemetry on another device (snippet).

## Brands and models

Counts below are from the sources named, not a full tally.

- **Xiaomi / Mi TV** is the most frequent. The origin story is a Xiaomi TV (25 packages). Issues: MiTV-MOOR2, MiTV-MOSR4, MiTV-MZTU0 (A Pro 75" 2026), MiTV-AXFU0, Redmi A75 fork. A Chinese Xiaomi user is on cleanyour.tv's "On air" wall.
- **TCL** is next: TCL 98C12L and ak30a5 (China), a TCL G13 fork, and a TCL Google TV black-screen report. TV Slim tested on a TCL 65C89K.
- **Sony Bravia:** KD-55X74K, KDL-49W800G, KD-55X8509C (2015, Android 7), K-85XR70. The TechPP author used a Sony Bravia.
- **Others named:** Acer, Hisense, OnePlus (Android 11), Philips, Sharp, Panasonic, Realme, Airtel Xstream, Nvidia Shield (2019), Chromecast with Google TV (4K and HD), Mi Box S, a SPPL 32" budget set and a Haixin 55V1F-R fork.
- **Not Android TV:** Samsung Tizen, LG webOS, Roku and Fire TV. cleanyour.tv quotes Gathin saying people also tried Fire TV, LG and projectors (TechCrunch).
- **Launchers named:** FLauncher, Projectivy, ATV Launcher (TechCrunch, Engadget), Startlight (TV Slim).
- **Spread:** cleanyour.tv claims 93 TVs from 31 brands; the repo lists 92 TV lists.

## Article plan

Eight topics for tidytv.winkle.dev. Points rely only on the sources above. Anything about TidyTV's own features comes from the project brief (backup first, `pm disable-user`, clean home screen, one-tap restore, per-model lists, phone-only).

### AI / Claude articles (4)

1. **Can Claude Code really clean up your Google TV? What it does, step by step**
   - Target query: "debloat Android TV with Claude Code" / "Claude Code cleaned my Google TV".
   - Key points: the 3 stages (backup, `pm disable-user`, replacement launcher, per [saascity](https://saascity.io/blog/debloat-android-tv-claude-code)). Needs a laptop on the same Wi-Fi plus an agent with shell access ([Mashable](https://mashable.com/tech/smart-tv-ads-claude-code-ai-cleanup)). Nothing is uninstalled. The wave started with Cobanov (Aug 27) and Gathin (Oct 17). Explain this in plain language and end with the no-laptop alternative.

2. **The risks of letting an AI agent loose on your TV (and how to lower them)**
   - Target query: "is it safe to debloat TV with Claude" / "Claude bricked my Android TV".
   - Key points: documented failures (boot screen loop #62, `system_server` crash #90, HDMI "No Signal" #321, casting broken by `setupwraith` #361). Critiques from Engadget and AI In Context (mistakes, update risk, prompt hacking). The safety rails prompts already use (backup first, small batches, never-disable list, launcher order). What reversible means in practice. State that TidyTV never uninstalls and has one-tap restore.

3. **Claude Code vs a phone app: two ways to debloat a Google TV**
   - Target query: "debloat Android TV without PC" / "disable bloatware Android TV without computer".
   - Key points: a comparison table. Agent route: flexible and custom launchers, but needs a laptop, a paid plan (TechPP: Claude Pro $20/mo), a long prompt, and trust in the model's judgment. Phone-app route: no agent, per-model lists, backup and restore built in. Be fair: TV Slim and ADB TV also exist ([tvslim.app](https://tvslim.app/)); link or acknowledge rather than ignore. TidyTV's difference is a phone app built on community per-model lists.

4. **Where the AI TV cleanup lists come from: cleanyourtv, tv-detox and what TidyTV adds**
   - Target query: "cleanyourtv" / "cleanyourtv alternative" / "tv-detox Google TV".
   - Key points: what the repos are and their scale (9,324 and 773 stars, Oct 20). Crediting Gathin23/cleanyourtv and Ansub/tv-detox is accurate to the project. Per-model lists exist for roughly 92 TVs. Sets expectation that lists are community-tested and still being fixed (see open issues).
   - Caution: the repo was renamed (tv-vibehack to cleanyourtv on Oct 19). Mention both names so searches match.

5. **"Hack my TV" got refused: how to ask an AI for a TV cleanup that works** (AI how-to)
   - Target query: "Claude won't hack my TV" / "prompt to debloat Android TV".
   - Key points: TechCrunch and It's FOSS report refusals on "hack my TV." What helps: give the exact model, the package list, and ask to turn off ads and speed things up. Include the safe-rails checklist from the prompts. Then offer the app route for people who don't want to prompt at all.

### Intent-matched how-to articles

6. **How to remove ads from the Google TV home screen (what works and what doesn't)**
   - Target query: "remove ads from Google TV home screen" / "Google TV ads".
   - Key points: Google says ads can't be turned off, only the ad ID ([Google](https://support.google.com/googletv/answer/13392198?hl=en)). Sony says Google TV "Apps only" mode hides recommendations, and Android TV models can't ([Sony](https://www.sony.com/electronics/support/articles/00225587)). The trade-off: Google Assistant is unavailable in Apps only mode. Then the disable-plus-launcher route.

7. **Which Android TV apps are safe to disable, and which you should never touch**
   - Target query: "safe to disable apps Android TV" / "Android TV bloatware list".
   - Key points: never-disable groups from the prompts (HDMI/tuner inputs, source menu, remote and Bluetooth, keyboard, Play services). Real breakages as examples (#321, #361, #62). Voice assistant, Chromecast and updates are decisions, not defaults. Disable vs uninstall, and how restore works. Per-model lists matter because the same package means different things on different brands.

8. **Debloating by brand: Xiaomi, TCL, Sony and Chromecast with Google TV**
   - Target query: "debloat Xiaomi TV" / "debloat TCL Google TV" / "Sony Bravia bloatware".
   - Key points: the brands that dominate reports, with the specific gotchas seen in issues: Xiaomi needs `setupwraith` handled and Home can fall back to Google TV (#53, #49); TCL has the black-screen loop after disabling `launcherx` and China-firmware limits; Sony Bravia's older models (Android 7) and the boot-screen report; Chromecast casting broken by `setupwraith`. Only claim support for models TidyTV actually has lists for.

9. **What about Fire TV, Samsung and LG? Honest answers** (optional ninth)
   - Target query: "debloat Samsung TV" / "remove bloatware Fire TV" / "LG webOS ads".
   - Key points: the AI prompts don't work on Tizen, webOS or Roku (PROMPT.md, Mashable). Fire TV gets partial support (saascity); dumbtv claims Fire TV Edition testing (TechCrunch). Samsung demand is visible in comments. Say clearly what TidyTV covers (Android TV and Google TV) and don't promise the rest.

### Notes for the writer
- Lead with accuracy on the trend's origin: Gathin's tweet (Oct 17) built on Cobanov's earlier work (Aug 27) and Ryan Doyle's viral post. Don't claim TidyTV started it.
- Don't quote search volume; I found none.
- Re-check star counts and press links before publishing, since the numbers move daily.
- Reddit comment text came from search snippets only. Re-verify any Reddit quote on the live page before using it.
