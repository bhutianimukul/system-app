# Gakseong · 각성

An Android discipline game where every quest is proven by the phone itself. Built for a 15–30
anime-adjacent audience.

*Gakseong* is Korean for **awakening** — the moment an ordinary person discovers they have a rank.
**The System** is what the app calls itself when it speaks to you, and that never changes.

Design phase closed 28 July 2026. **No application code exists yet.**

## What is here

| file | what it is |
|---|---|
| `CLAUDE.md` | the feature spec and build rules, loaded automatically by Claude Code |
| `DECISIONS.md` | 18 sections of locked decisions with the reasoning behind each |
| `SESSION-LOG.md` | every instruction that produced the design, in order |
| `assets/` | 109 files. Character art, shadows, emblems, sigils, gates, scenes, app icon |
| `assets/MANIFEST.md` | what each asset is and the rules it follows |
| `assets/PROMPTS.md` | the generation prompt kit for producing more |

## Live design

No login needed for any of these.

| | link |
|---|---|
| Full app design — 46 screens, 7 classes | https://anyartifact-production.up.railway.app/oMhpqnEKxiSm |
| Feature spec | https://anyartifact-production.up.railway.app/0XY02nXT3S84 |
| Decision record and instruction log | https://anyartifact-production.up.railway.app/-xE8brb4PuOA |
| Rank ascension ceremony | https://anyartifact-production.up.railway.app/Dq0Y8K2gCdoc |

The design page loads its art from `web/` in this repo by URL rather than inlining it, which is what keeps
it at 230 KB instead of 2.6 MB. That is also why this repo is public.

## The one-line thesis

Your phone is the dungeon. It already keeps the record; the app just starts reading it out loud.

## Next

Phase 01 is the aura and rank engine as pure functions plus one test file. No UI, no persistence, no
Android dependencies. Everything else depends on it being correct.

Outstanding art: Envoy full-body crops, full-body crops for all seven classes for the profile screen,
flat small-size rank emblems (the ornate set is unreadable at 48px), and a Tanker regenerated on pure
black.
