# Brief: guides for tidytv.winkle.dev

You write long-form articles for people who saw the "clean up your TV with Claude" trend and are searching
for how to do it. The research with every source is in `docs/marketing/trend-research.md`: read it fully
first. Facts about TidyTV are in `README.md` and `site/index.html`.

## Output
One Markdown file per article: `content/guides/<slug>.md`. The slug is short and readable, e.g.
`claude-code-google-tv`. The file starts with front matter, then the body:

```
---
title: Can Claude Code really clean up your Google TV?
seo_title: Debloat Android TV with Claude Code, step by step | TidyTV
description: 130 to 158 characters, with the target search words, saying what the reader gets.
dek: One sentence under the title.
order: 1
date: 2026-10-20
---
Body in Markdown: paragraphs, ## and ### headings, - lists, 1. lists, **bold**, [links](https://...), > quotes.
```

- `seo_title` is at most 65 characters, including " | TidyTV".
- Body: 900 to 1,400 words. Open with the answer in the first two sentences, then explain.
- Use ## headings that match real questions people ask (they help search).
- Tables are not supported by the page builder: use lists instead.

## Voice
- Plain English for a non-technical TV owner. Short sentences. Explain any technical word the first time (ADB, launcher, debloat).
- Honest and fair. Give the AI route its due: it is flexible and it works for many people. Say clearly what can go wrong.
- TidyTV appears where it genuinely helps (no laptop, backup, per-model lists, one-tap restore), at most 3 or 4 mentions, and once near the end with a link to `/` and to `/tv/` (the per-model pages). Never call it the best or the only option. Mention TV Slim and ADB TV where the research does.
- Credit the people behind the trend accurately: Mert Cobanov (Aug 27), Gathin and cleanyourtv (Oct 17, formerly tv-vibehack), Ansub's tv-detox. TidyTV did not start it.

## Rules
- Every fact, number, date and quote comes from a source in the research file. Link the source inline the first time you use it.
- Quotes: at most one per article, under 15 words, in quotation marks with the source.
- Star counts change: write "over 9,000 stars as of October 20, 2026" style, with the date.
- Nothing invented: no user counts, ratings, testimonials or speed numbers.
- Don't reproduce copyrighted text; summarise in your own words.
- Don't mention Reddit quotes the research marks as snippet-only unless you also mark them as reported.
- Link to model pages as `/tv/<id>/` only for ids that exist in `content/tv/data/` (check with ls).
