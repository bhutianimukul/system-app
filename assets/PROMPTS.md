# Prompt kit

Every prompt is **two fixed blocks plus one variable block**. The fixed blocks are what make
twenty generations look like one game instead of twenty freelancers. Never edit them.

---

## Block 1 — STYLE (paste into everything)

> Korean manhwa digital illustration, dark fantasy. Cel-shaded linework with painted rendering and
> visible brush texture. Near-monochrome palette — deep indigo, charcoal and black — with exactly
> ONE saturated accent colour. High contrast, deep crushed blacks, cinematic. Single subject.
> No text, no logo, no watermark, no signature, no border, no UI, no frame.

## Block 2 — LIGHT (the single biggest lever)

> Lit by one strong source BEHIND the subject. Rim light traces the silhouette edges. The front of
> the subject falls into near-black shadow. No fill light. The accent colour appears only in that
> light source and in the subject's own glow.

**This block is why the good images came out good.** When a generation looks flat, cheap or
generic, it is almost always because the light got soft and even. Restate this block harder rather
than adding detail elsewhere.

---

## Block 3 — pick ONE by asset type

### A. Class portrait — composites into the UI

> Pure solid black background. No environment, no ground, no haze, no particles behind the subject.
> Chest-up crop, three-quarter view, head in the upper third of the frame. Vertical 9:16.

**Pure black is a hard requirement, not a preference.** These are composited with
`mix-blend-mode: lighten`, which deletes black. Any background and the asset is unusable in UI.

Append one class line:

| Class | Accent | Sigil | Append |
|---|---|---|---|
| Fighter | `#FF5A1E` orange-red | Clenched fist | Heavy pauldrons and scarred gauntlets, brawler's stance, chin raised, no helmet, short cropped hair, one glowing eye |
| Ranger | `#35E39A` green | Arrowhead | Deep hood over a travel cloak, longbow silhouetted over one shoulder, head turned alert to the side, glowing eyes in shadow |
| Sage | `#A855F7` violet | Orbiting rings | High rigid collar, three glowing glyph rings suspended and orbiting the head, eyes closed, utterly still |
| Healer | `#FFE9B8` pale gold | Halo | Thin veil across the lower face, layered heavy robes, halo of glowing glyphs behind the head, serene downward gaze |
| Assassin | `#ED3DC8` magenta | Reversed blade | Hood plus a mask over the lower face, blade held reversed along the forearm, body coiled, only the eyes lit |
| Tanker | `#5FD0F5` cyan | Tower shield | Massive tower shield planted in front, heavy layered plate, feet braced wide, immovable, head lowered behind the shield rim |
| Envoy | `#F0A03C` amber | Open palm | Open long coat, no weapon, one hand extended toward the viewer holding a small warm light, direct unflinching gaze, hood down |

### The accent ceiling, and how to get past it

Seven classes puts **three colours in the warm family** — Fighter, Healer, Envoy. Hue alone can no
longer carry identification at thumbnail size, which is where a guild list lives. Two fixes, both
already applied above:

1. **Spread the warms deliberately.** Fighter pushed redder, Healer pushed paler and whiter, Envoy
   sits in the middle at amber. Red → amber → cream is readable; three oranges is not.
2. **Give every class a sigil.** Colour plus shape is how Genshin runs seven elements without
   collisions — the icon disambiguates and the colour reinforces. Relying on hue alone caps you at
   about five classes; hue plus sigil scales indefinitely.

The accent still drives the whole screen. The sigil is what makes it legible at 24px.

### B. Full body — profile / character screen

> Full body, standing, feet visible. Subject centred, standing on a circular plinth of glowing
> carved runes. Swirling ribbons of black smoke curl around the legs. Pure solid black background
> above and behind the subject. Vertical 9:16.

Same class lines as above. Commission this crop **alongside** the chest-up, never after — it is
the same character sheet and costs nothing extra at the time.

### C. Shadow creature — the collection

> A single summoned shadow creature: [beast / knight / serpent / wraith]. Its body is solid black
> smoke holding a defined silhouette, edged with a thin glowing accent outline. Embers drift upward
> from it. Pure solid black background. Vertical 4:5.

### D. Scene — gate intro, story fragment

Drop the black-background requirement. Add camera language:

> Wide establishing shot, low camera angle. Volumetric light shafts, airborne dust and drifting
> embers, heavy atmospheric haze. Environment: [ruined cathedral / flooded subway platform /
> rain-slick city street at 3am / cracked stone dungeon hall carved with runes]. Vertical 9:16.

### E. Boss / Monarch — the scale shot

This is the composition that carries every "the System has turned against you" screen, and it is
worth generating several of:

> Extreme scale contrast. A small lone human figure stands in the lower foreground, seen from
> behind, fully silhouetted, no more than one tenth of the frame height. A colossal [subject] fills
> the upper two thirds of the frame — [a vast grinning stone face with glowing eyes / an armoured
> giant rising from mist / a crowned figure on a throne of rubble]. Cold light from the colossus is
> the only light source. Landscape 16:9.

### F. Wide banner — share card, weekly report

> Wide cinematic composition with deliberate empty space in the [left / right] third for text
> overlay. Subject positioned off-centre. Landscape 16:9.

Empty space is the whole point here — art with the subject dead centre cannot take a headline.

---

## Matched pairs — the highest-value technique here

Generate a scene, then **edit that same image** rather than writing a second prompt. Pass the
finished image back with one instruction:

> Keep this image identical — same pose, same composition, same background, same lighting.
> Change only [X].

Two frames that differ in exactly one element let you show **transformation**, which no single
illustration can. Prompting twice never produces a match; editing does.

Uses that need it:

| Pair | Change | Screen |
|---|---|---|
| Before / after ascension | Cape red → black shadow flame · aura off → on | Rank-up ceremony, crossfaded live |
| Healthy / penalised | Aura lit → guttering, colour drained to grey | Penalty and Dungeon Break |
| Empty / summoned | Alone → shadow army silhouettes behind | Shadow extraction |
| Rested / exhausted | Posture upright → head down, aura dim | `CONDITION: FATIGUED` |

### On hand — the knight sequence

Three frames, same character, same storm:

| Frame | Shot |
|---|---|
| `knight-cliff-01-before.png` | Wide. Red cape, ordinary armour, twin swords, cliff edge |
| `knight-cliff-02-after.png` | Wide, identical framing. Cape is now black shadow flame |
| `knight-cliff-03-closeup.png` | Push in. Same knight, shadow cape filling frame, red gem lit |

That is not two assets, it is a **three-shot cutscene**: establish → transform → push in. Run it
behind the ceremony and the player watches themselves *become* something instead of reading that
they did.

**Extend any scene into a sequence the same way** — generate the wide, then edit it twice: once to
change one element, once to crop closer. Three frames of continuity for the price of one prompt and
two edits, and it is the cheapest cinematic quality available to you.

### Class recolours — one scene serves all seven

`knight-cliff-05-sage-recolour.png` is frame 04 edited into Sage: violet throughout, glyph rings
orbiting the head, eyes closed, hands folded. Same cliff, same storm, same pose.

This is the highest-leverage move in the whole kit. **One good scene composition recoloured seven
times gives every class its own ceremony backdrop** — for one prompt and six edits, instead of seven
separate scenes that would never match each other.

> Keep this image identical — same pose, same composition, same background, same lighting.
> Recolour everything to [accent]. Replace the shadow flame with [class signature element].
> [Class-specific pose note.]

| Class | Signature element to swap in | Pose note |
|---|---|---|
| Fighter | Orange-red ember aura, cracked ground | Fists clenched, chin raised |
| Ranger | Green wind-trails, leaves in the gale | Bow drawn low, head turned alert |
| Sage | Violet glyph rings orbiting the head | Eyes closed, hands folded — *done* |
| Healer | Pale gold halo, motes of light rising | Head bowed, palms open |
| Assassin | Magenta shadow flame, blade reversed | Half-turned, body coiled |
| Tanker | Cyan barrier lightning, shield planted | Braced, immovable, head low |
| Envoy | Amber light held in one open palm | Facing viewer, hand extended |

Sage is done. Six edits and the ceremony has a bespoke backdrop per class.

## Sets — always ask for a sheet, never one at a time

Ten separate generations produce ten assets that don't match. **One grid image produces ten that
do**, and slicing locally is trivial. Use `sips --cropToHeightWidth H W --cropOffset Y X`, and note
that `sips` silently fails when X is exactly `0` on a non-zero Y — pass `1`.

Pick a grid whose cells come out square-ish and at least 200px wide:

| Set | Grid | Canvas | Cell |
|---|---|---|---|
| 10 shadows | 5 × 2 | 16:9 | 204 × 288 ✓ used |
| 6 rank emblems | 3 × 2 | 1:1 | 341 × 512 ✓ used |
| 7 class sigils | 4 × 2 | 2:1 | 256 × 256 |
| 5 gate tiers | 5 × 1 | 16:9 | 204 × 576 |

**Never let the model letter anything.** No labels, no captions, no rank names. All text in the app
is live UI text — it has to localise, respond to state, and scale with accessibility settings, and
baked text does none of that. Repeat *"no text, no labels, no words anywhere"* at the **end** of the
prompt as well as the start; the tail carries more weight and the style block alone does not hold.

The rank emblems are the one exception — there the letter *is* the artwork, not a caption.

---

## Remaining prompts

### Class sigils — 7, as one sheet

> Eight cells in a 4 by 2 grid on a single image, each cell containing one minimal icon, evenly
> spaced, generous margins, nothing touching the cell edges.
>
> Row 1: a clenched fist · an arrowhead · three concentric orbiting rings · a halo ring
> Row 2: a dagger held point-down · a tall tower shield · an open upturned palm · leave this cell empty
>
> Flat geometric insignia. Thick clean strokes, no shading, no gradient, no texture, no inner
> detail. Each icon must remain readable at 24 pixels. Solid pale cyan on pure black background.
> Landscape 2:1.
>
> No text, no labels, no letters, no words anywhere in the image.

Recolour each sliced icon in code to its class accent — do **not** generate seven coloured versions.
Flat single-tone art tints perfectly with a CSS filter or an Android tint, so one sheet serves all
seven and every future accent change is free.

### Flat rank emblems — 6, as one sheet

The ornate emblems already generated are hero assets; verified to turn to mush at 48px. These are
the list-view companions.

> Six cells in a 3 by 2 grid on a single image, each containing one flat rank badge.
>
> Row 1: shield shape containing "E" · shield containing "D" · shield containing "C"
> Row 2: shield containing "B" · shield containing "A" · shield containing "S"
>
> Flat geometric shield silhouette with a heavy bold letter cut out of it. Two tones only, no
> filigree, no carving, no texture, no glow, no inner detail. Each badge must remain readable at 24
> pixels. Solid pale grey on pure black background. Square 1:1.
>
> No extra text, no labels, no words — only the single rank letter inside each shield.

### Gate tiers — 5, as one sheet, clean

> [attach `scenes/gate-portal.png`]
>
> Five variants of this exact gate side by side as five equal vertical panels in one image. Same
> composition, same ruined hall, same cracked stone, same rune rings in every panel — only the light
> colour changes.
>
> Panel 1 blackened iron, dull grey-blue light. Panel 2 tarnished silver, pale cold white light.
> Panel 3 polished brass, warm amber light. Panel 4 bright gold, brilliant yellow-white light.
> Panel 5 white-hot molten, searing white with violet edges.
>
> No borders or dividers between panels. Landscape 16:9.
>
> Absolutely no text, no labels, no captions, no rank names, no words anywhere in the image.

### Condition states — 3, for the Condition readout

> [attach any class portrait]
>
> Three variants of this figure side by side as three equal vertical panels, identical pose and
> framing in each. Panel 1 aura bright and steady, posture upright. Panel 2 aura dim and guttering,
> shoulders dropped. Panel 3 aura almost extinguished, head down, colour drained toward grey.
> Pure solid black background throughout. Landscape 16:9.
>
> No text, no labels, no words anywhere in the image.

## Fixing a bad generation

| Problem | Fix |
|---|---|
| Flat, generic, cheap-looking | The light went even. Restate Block 2 and add *"no fill light, front of subject in near-black shadow"* |
| Background bled into a "black background" portrait | Add *"the background is pure #000000, absolutely empty"* and *"no glow behind the subject"* |
| Too colourful, muddy | You have two accents fighting. Name the single accent explicitly: *"the only saturated colour in the image is [hex]"* |
| Face looks wrong or uncanny | Hide it. Add hood, mask, veil, lowered head, or closed eyes. Obscured faces read as intentional and menacing; imperfect faces read as amateur |
| Wrong crop | Image models respect *"head in the upper third"* far better than *"chest-up"* |
| Watermark or signature appeared | Repeat the exclusion list at the *end* of the prompt as well as the start |
| Style drifting between characters | Blocks 1 and 2 were edited. Paste them verbatim |

---

## Worked example — Tanker portrait

> Korean manhwa digital illustration, dark fantasy. Cel-shaded linework with painted rendering and
> visible brush texture. Near-monochrome palette — deep indigo, charcoal and black — with exactly
> ONE saturated accent colour, cyan. High contrast, deep crushed blacks, cinematic. Single subject.
> No text, no logo, no watermark, no signature, no border, no UI, no frame.
>
> Lit by one strong source BEHIND the subject. Rim light traces the silhouette edges. The front of
> the subject falls into near-black shadow. No fill light. The accent colour appears only in that
> light source and in the subject's own glow.
>
> Pure solid black background. No environment, no ground, no haze, no particles behind the subject.
> Chest-up crop, three-quarter view, head in the upper third of the frame. Vertical 9:16.
>
> Massive tower shield planted in front, heavy layered plate armour, feet braced wide, immovable,
> head lowered behind the shield rim. Cyan rim light along the shield edge and pauldrons.
>
> No text, no watermark, no signature.
