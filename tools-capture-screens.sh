#!/bin/bash
# Rebuild screenshots/ from a fresh capture run. Globs the source directory rather than working from a
# hand-written order list, because the old script silently dropped every screen added after it was written.
set -e
SRC="$1"
DEST=/Users/mukul/Documents/Personal/system-app/screenshots
ORDER="splash welcome perms intent diag apps contract class stage home focus read raidhub raid runraid runsettle invite gate gates monarch break arise shadows ceremony share shareraid complete league guild feed refer soon profile private report privset thresh weights newapp aikey bonus widget pact contain chat store settings type"

rm -f "$DEST"/*.png "$DEST"/index.html
mkdir -p "$DEST"

# In app order first, then anything the order list has not heard of, so nothing can go missing again.
listed=""
for n in $ORDER; do
  [ -f "$SRC/$n.png" ] || continue
  cp "$SRC/$n.png" "$DEST/$n.png"
  sips -Z 640 "$DEST/$n.png" >/dev/null 2>&1
  listed="$listed $n"
done
for f in "$SRC"/*.png; do
  n=$(basename "$f" .png)
  case " $listed " in *" $n "*) continue ;; esac
  cp "$f" "$DEST/$n.png"
  sips -Z 640 "$DEST/$n.png" >/dev/null 2>&1
  listed="$listed $n"
  echo "not in the order list: $n"
done

count=$(echo $listed | wc -w | tr -d ' ')
{
  cat <<HTML
<!DOCTYPE html><html><head><meta charset="utf-8"><title>Gakseong — $count screens</title>
<style>
  body{margin:0;background:#0B0D1A;color:#C6CCDD;font:14px system-ui;padding:28px}
  h1{color:#fff;font-size:20px;letter-spacing:-.02em;margin:0 0 4px}
  p{margin:0 0 24px;color:#8B93A8}
  .g{display:grid;grid-template-columns:repeat(auto-fill,minmax(210px,1fr));gap:20px}
  figure{margin:0}
  img{width:100%;display:block;border-radius:14px;border:1px solid #FFFFFF1F}
  figcaption{font-size:10px;letter-spacing:.16em;text-transform:uppercase;color:#8B93A8;margin-top:8px}
</style></head><body>
<h1>Gakseong — $count screens</h1>
<p>Debug APK on a Pixel 7 emulator, 1080&times;2400 at 420dpi, in app order. Condensed display type, tracked-sans
labels, and the rebuilt bottom nav are all in this pass.</p>
<div class="g">
HTML
  for n in $listed; do
    printf '<figure><img src="%s.png" alt="%s" loading="lazy"><figcaption>%s</figcaption></figure>\n' "$n" "$n" "$n"
  done
  printf '</div></body></html>\n'
} > "$DEST/index.html"

echo "$count screens"
du -sh "$DEST"
