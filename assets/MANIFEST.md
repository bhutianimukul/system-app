# Asset library

Art for the discipline app. Two categories, and the difference is technical, not aesthetic.

## Categories

**`characters/`** — class portraits. **Pure black background, chest-up.** These composite into UI
via `mix-blend-mode: lighten`, which knocks the black out so the figure sits *inside* the scene
lighting rather than on top of it. A portrait with any background breaks this.

**`scenes/`** — full illustrations with their own background and sky. Cannot composite. Use as
backdrops in their own right: gate intros, story fragments, loading screens, share cards.

**`scenes/wide/`** — 16:9 landscape scenes. Different job again: full-bleed event screens, boss
encounters, and share cards, where a portrait crop would waste the composition.

## Folders

```
characters/source/   originals, full resolution
characters/web/      520px JPEG, quality 68 — the set embedded in the ceremony mockup
scenes/              9:16 full-background illustrations
scenes/wide/         16:9 landscape scenes
references/          third-party moodboard — NEVER SHIP. see note below
```

### Scenes on hand (9:16 / portrait, full background)

| File | What it is | Notes |
|---|---|---|
| `tanker-shield-corridor.png` | Bald scarred warrior, gold-ornamented shield, green lightning aura, rune-carved corridor | Reads **Tanker / Guardian** — an archetype not yet in the class list. Its green collides with Ranger's accent; if this becomes a class it needs steel-blue or white-gold. To use as a class portrait, **regenerate on pure black** rather than masking — that background is too intricate to cut cheaply |
| `blueflame-daggers-portrait.png` | Twin daggers crossed, blue flame aura | Brighter, more saturated style than the black-background set — closer to Arise than to the class portraits |
| `blueflame-ruins-battle.png` | Battle stance in ruined city, blue flame | Story fragment / gate intro |

### `condition/` — the HRV readout states

**Complete, 3/3.** Sliced from `_sheet-condition-3up.png` at 341×572. Same figure, identical pose,
aura only.

| File | State | Meaning |
|---|---|---|
| `condition-1-normal.png` | Aura bright and steady | Full quest load |
| `condition-2-fatigued.png` | Aura dim, guttering | Load reduced, rank protected |
| `condition-3-exhausted.png` | Aura extinguished, colour drained to grey | Threshold waived — one per Pact |

This is the safety valve made visible. The player sees the System *assessing* them rather than
going easy on them, which is what keeps a penalty-driven app defensible.

### `gates/` — rank-tiered portals

**Complete, 5/5.** Sliced from `_sheet-gate-tiers-5up.png` at 204×576. Identical composition —
ruined hall, cracked stone, orbiting rune rings — with only the light colour changing.

`gate-1-iron` grey-blue · `gate-2-silver` cold white · `gate-3-brass` warm amber ·
`gate-4-gold` yellow-white · `gate-5-molten` searing white with violet

Full-resolution single gate lives at `scenes/gate-portal.png`. Map tiers to rank bands for location
gates and Gate events, so a higher-rank gate is visibly hotter.

### `sigils/` — class marks

**Complete, 7/7.** Sliced from `_sheet-sigils-7up.png` at 256×256. Flat single-tone pale cyan on
black — **tint these in code** to each class accent rather than shipping seven coloured copies, so
an accent change is a hex edit.

`fighter-fist` · `ranger-arrowhead` · `sage-rings` · `healer-halo` · `assassin-dagger` ·
`tanker-shield` · `envoy-palm`

Verified legible at 24px. This is what makes seven classes distinguishable in a guild list once
three of the accents are warm — colour alone stops working at that size.

### `icon/` — app icon

`app-icon-1024.png` plus 512 / 192 / 108 / 48 derivatives. Geometric rune-glyph mark, cyan on
near-black. Verified at 48px: the outer star reads, the inner cross muddles slightly. Usable; a
version with one fewer concentric layer would be sharper.

### `emblems/` — rank badges

Target: **6** (E→S), each in **two variants**. Have: **1** (E, hero only).

| Variant | Size | Where |
|---|---|---|
| Hero — carved, ornate, glowing | 1024px source | Ceremony, profile header |
| Flat — silhouette + letter, two tones, no filigree | 1:1, readable at 24px | League table, guild list, widget |

`rank-E.png` is the hero variant. Verified: **it turns to mush at 48px** — the flat variant is not
optional.

**Rank is a metal tier, not a colour.** Colour belongs to class; if emblems carry their own hue they
fight the seven class accents on the same screen.

| Rank | Material |
|---|---|
| E | Cracked stone |
| D | Blackened iron |
| C | Tarnished silver |
| B | Polished brass |
| A | Bright gold |
| S | White-hot molten light |

> `rank-E.png` currently has a cyan letter and violet glow — regenerate it as cracked stone when
> doing D through S, so the set is consistent and accent-neutral.

### `shadows/` — the collection layer

Awarded at 7 / 30 / 90-day streaks. Pure black background, violet edge glow.
**Complete — 10 of a target 8.** Sliced from `_sheet-shadows-10up.png` at 204×288 each,
web copies at 200px in `shadows/web/`.

`01-knight` · `02-wolf` · `03-serpent` · `04-wraith` · `05-bull` · `06-drone` · `07-spearman` ·
`08-hound-three-headed` · `09-serpent-many-eyed` · `10-knight-variant`

Generated with `scenes/wide/monarch-red-army.png` as a style reference, so the collection screen and
the Monarch fight belong to the same world. Use that same trick for any future additions.

> **Sheets slice cleanly.** Ask for sets as a single grid image — 5×2 or 3×2 — then cut them locally.
> One generation gives ten guaranteed-consistent assets; ten generations give ten that don't match.
> Sliced with `sips --cropToHeightWidth H W --cropOffset Y X`. Note: `sips` silently fails when
> X is exactly `0` on a non-zero Y — use `1` instead.

### Wide scenes on hand

| File | What it is | Use it for |
|---|---|---|
| `boss-colossus-hall.png` | Tiny hooded figure facing a colossal grinning stone face, blue-lit ruin | **Dungeon Break** intro. A *scale* shot — it says "you are outmatched," the exact register a penalty gate needs |
| `monarch-red-army.png` | Red-armoured antagonist leading a shadow army, colossus looming behind | **Monarch fight**. Also the **style bible for the shadow set** — attach it as a reference when generating shadows |

Scale shots like this are the hardest thing to fake with UI and the easiest thing to reuse. One of
them carries every "the System has decided something is wrong" moment in the app.

> **Save art the moment it arrives.** Claude's image cache both evicts old files *and reuses
> filenames* — `5.png` was Ranger, then became the boss scene an hour later. Anything not copied
> into this folder is already gone.

## Class accents

Each class owns one accent colour, taken from that character's own glow. In the ceremony the
entire screen reads from these three variables, so a class is four lines of CSS and an image —
no second theme, no second screen.

Authoritative accent list lives in **`PROMPTS.md`** — it also carries the per-class sigil, which is
what keeps seven classes legible at thumbnail size once three of them are warm.

| Class | `--hot` | `--soft` | `--deep` | Trains | Art |
|---|---|---|---|---|---|
| Fighter | `#FF5A1E` | `#FFA575` | `#A83A0E` | Physical, strength | 520px only — regenerate |
| Ranger | `#35E39A` | `#8AF2C8` | `#12805A` | Movement, outdoors | ✓ |
| Sage | `#A855F7` | `#CDA5FF` | `#5E1FAC` | Focus, study | ✓ |
| Healer | `#FFE9B8` | `#FFF6E2` | `#A87A2E` | Sleep, recovery | ✓ |
| Assassin | `#ED3DC8` | `#FF9AE6` | `#8E1274` | Screen discipline | ✓ |
| Envoy | `#F0A03C` | `#FFD79A` | `#9A5A10` | Socialisation | ✓ near-black bg, crush levels |
| Tanker | `#5FD0F5` | `#B4EBFF` | `#1B6E96` | Defence, consistency | candidate in `scenes/` — needs black-bg regen |

Fighter shifted redder and Healer paler when Envoy's amber arrived — three warm accents cannot sit
at the same saturation. The ceremony mockup still carries the older Fighter and Healer values.

Because every glow reads from `--hot`, the accent can also follow **state** rather than class —
red for a penalty, gold for an S-rank ascension — without touching the composition.

## Regenerating or extending

Paste this spine verbatim into every generation. It is what makes the set look like one game
instead of six freelancers:

> Original anime dark-fantasy webtoon illustration, manhwa style, painterly cel-shading. Chest-up
> composition, three-quarter view. Rim light from behind ONLY — the front of the figure is almost
> black. Near-monochrome deep indigo and black palette with one hot accent colour. Pure solid black
> background, no scenery, no text, no logo, no watermark. High contrast, cinematic key visual.
> Vertical portrait 9:16.

Then append one line:

| Class | Append |
|---|---|
| Fighter | Heavy pauldrons and scarred gauntlets, brawler's stance, chin raised, no helmet, short cropped hair, glowing eyes |
| Ranger | Deep hood over a travel cloak, longbow silhouetted over one shoulder, head turned alert to the side, glowing eyes |
| Sage | High rigid collar, three glowing glyph rings suspended around the head, eyes closed, utterly still |
| Healer | Thin veil across the face, layered heavy robes, faint halo ring behind the head, serene downward gaze |
| Assassin | Hood plus a mask over the lower face, blade held reversed along the forearm, coiled, only the eyes lit |
| Envoy | Open long coat, no weapon, one hand extended toward the viewer, direct unflinching gaze |

## Outstanding

- **Fighter source was lost.** The image cache rolled over before it was saved, so only
  `fighter-ONLY-520px.jpg` survives. Regenerate at full resolution before production.
- **Envoy not generated.**
- **Two crops per class needed.** Tight chest-up for the ceremony, full-body for the character
  profile screen. Same character, same commission, two framings — the glyph rings on Sage, Healer
  and Assassin currently clip against the ceremony's top scrim.

## References folder — do not ship

`references/` holds official Solo Leveling art (Chugong · D&C Media · Redice Studio · A-1 Pictures).
It is a moodboard for lighting, palette discipline and composition. **None of it ships in the app,
in store screenshots, or in marketing.** Study one thing in each: how the whole frame commits to a
single accent colour.

## Live mockups

- Feature spec — https://claude.ai/code/artifact/8fa0df80-201f-4380-88dc-cb8f3c98ffb3
- Rank ascension ceremony, five classes — https://claude.ai/code/artifact/fe312f0c-032a-42d3-bb9e-6ed251cdc82a
