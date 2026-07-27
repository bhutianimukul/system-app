# The System

An Android discipline game where every quest is proven by the phone itself. Solo-Leveling-flavoured,
built for a 15–30 anime-adjacent audience.

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

| | link |
|---|---|
| Full app design — 39 screens, 4 pages | https://claude.ai/code/artifact/7a66e316-7246-464f-a701-06d58ad1346f |
| Feature spec | https://anyartifact-production.up.railway.app/0XY02nXT3S84 |
| Decision record and instruction log | https://anyartifact-production.up.railway.app/a4qyXBjaKuZ8 |
| Rank ascension ceremony | https://claude.ai/code/artifact/fe312f0c-032a-42d3-bb9e-6ed251cdc82a |

## The one-line thesis

Your phone is the dungeon. It already keeps the record; the app just starts reading it out loud.

## Next

Phase 01 is the aura and rank engine as pure functions plus one test file. No UI, no persistence, no
Android dependencies. Everything else depends on it being correct.

Outstanding art: Envoy full-body crops, full-body crops for all seven classes for the profile screen,
flat small-size rank emblems (the ornate set is unreadable at 48px), and a Tanker regenerated on pure
black.
