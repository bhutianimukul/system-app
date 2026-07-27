# web/ — display-optimised art

The 62 files the hosted design page loads by URL. Each is sized to the exact device-pixel
width it renders at (a 330 px CSS phone frame at 2x DPR, so 700 px for a full-bleed layer),
which is why they look small for their apparent quality.

Characters are rendered on pure black so `mix-blend-mode: lighten` knocks the background out.
Do not re-encode these at lower quality: the art is dark and rim-lit, and JPEG destroys
low-luminance detail first, which makes the figures disappear under the blend mode.

Sources are in `../assets/`.
