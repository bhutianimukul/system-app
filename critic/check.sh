#!/usr/bin/env bash
# The critic. One command, honest pass or fail.
#
#   1. the engine's own asserts, and that kotlinc still compiles engine/ alone
#   2. JVM unit tests and the debug build
#   3. every route launched on a device, cold each time, logcat scanned for fatals
#   4. the placeholder audit: screen literals that look like state
#   5. the invented-people check: no fabricated human names on any screen
#   6. CRITIC.md regenerated
#
# Usage: critic/check.sh [--no-device]
#
# Written for bash on purpose. zsh does not word-split unquoted variables, so `for s in $ROUTES` runs the loop
# exactly once and reports a pass after testing one route. The route list below is a bash array for that reason.

set -uo pipefail
cd "$(dirname "$0")/.."

export JAVA_HOME="${JAVA_HOME:-$HOME/.sdkman/candidates/java/current}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
export PATH="$ANDROID_HOME/platform-tools:$JAVA_HOME/bin:$PATH"

FAIL=0
step() { printf '\n\033[1m== %s\033[0m\n' "$1"; }
bad()  { printf '\033[31mFAIL\033[0m %s\n' "$1"; FAIL=1; }
ok()   { printf '\033[32mok\033[0m   %s\n' "$1"; }
note() { printf '     %s\n' "$1"; }

ROUTES=(
  home focus ceremony raid raidhub runraid runsettle share shareraid arise league gate break
  invite guild feed refer soon profile private report splash welcome perms diag apps contract
  intent class stage privset thresh weights newapp read shadows complete monarch gates aikey
  bonus widget pact contain chat store type settings
)

step "1. engine self-check"
if kotlinc engine/*.kt -include-runtime -d /tmp/engine.jar 2>/dev/null \
   && java -cp /tmp/engine.jar gakseong.engine.AuraSelfCheckKt > /tmp/engine-check.txt 2>&1 \
   && grep -q "all checks passed" /tmp/engine-check.txt; then
  ok "$(grep -c '^ok' /tmp/engine-check.txt) asserts pass, and kotlinc still compiles engine/ alone"
else
  bad "engine self-check — a dependency may have leaked into engine/"
  tail -5 /tmp/engine-check.txt 2>/dev/null
fi

step "2. unit tests and build"
if ./gradlew testDebugUnitTest assembleDebug -q > /tmp/gradle.txt 2>&1; then
  TESTS=$(grep -ho 'tests="[0-9]*"' app/build/test-results/testDebugUnitTest/*.xml 2>/dev/null \
          | grep -o '[0-9]*' | paste -sd+ - | bc)
  ok "build clean, ${TESTS:-0} JVM tests pass"
else
  bad "build or JVM tests"
  grep -E "^e:|FAILED" /tmp/gradle.txt | head -20
fi

step "3. route smoke test"
if [ "${1:-}" = "--no-device" ] || ! adb get-state > /dev/null 2>&1; then
  note "skipped: no device attached."
  note "The ${#ROUTES[@]} routes are UNVERIFIED, which is not the same as passing."
else
  adb install -r app/build/outputs/apk/debug/app-debug.apk > /dev/null 2>&1
  adb logcat -c
  for s in "${ROUTES[@]}"; do
    # -S force-stops first. Without it `am start` re-delivers the intent to the running instance, onCreate
    # never runs again, the route never changes, and the loop reports 48 clean screens having rendered one.
    adb shell am start -S -n app.gakseong/.MainActivity --es screen "$s" > /dev/null 2>&1
    sleep 1.1
  done
  CRASHES=$(adb logcat -d | grep -c "FATAL EXCEPTION")
  if [ "$CRASHES" -eq 0 ]; then
    ok "all ${#ROUTES[@]} routes launched clean"
  else
    bad "$CRASHES fatal exceptions across ${#ROUTES[@]} routes"
    adb logcat -d | grep -A8 "FATAL EXCEPTION" | head -40
  fi
fi

step "4. placeholder audit"
# The failure this catches: a screen that compiles, renders, passes the smoke test, and still shows 640.
# Any string literal in a screen carrying a multi-digit number or a rank label is state unless the allowlist
# says otherwise. pending.txt holds literals that are real state waiting on a later phase.
#
# Matching is EXACT on the extracted literal, never a substring of the grep line. An earlier version used
# `grep -vF -f pending.txt` against the whole line, so a pending entry of "600" silently swallowed every
# unrelated finding containing those digits. It reported zero findings while 139 existed.
# An entry is either `literal` (any screen) or `File.kt|literal` (that screen only). File scoping matters:
# "+450" is the spec's human-raid bonus in Raid.kt and a feed post's aura in Feed.kt, and only one of those is
# a constant.
grep -hv '^#\|^$' critic/allowlist.txt critic/pending.txt | sort -u > /tmp/critic-known.txt
SUSPECT=$(grep -rnoE '"[^"]*([0-9]{2,}|[EDCBAS] · (I|II|III))[^"]*"' \
            app/src/main/kotlin/app/gakseong/ui/screens/ 2>/dev/null \
          | grep -v 'Typography.kt' \
          | awk -F'"' -v known=/tmp/critic-known.txt '
              BEGIN { while ((getline l < known) > 0) seen[l] = 1 }
              {
                lit = $2
                # A literal that interpolates or formats is state-driven by construction: the number in it came
                # from somewhere. Listing each one individually would be listing the absence of a problem.
                if (lit ~ /\$\{/ || lit ~ /%[0-9.]*[dsf]/) next
                n = split($1, path, "/"); split(path[n], loc, ":"); file = loc[1]
                if (!(lit in seen) && !((file "|" lit) in seen)) print
              }
            ' \
          || true)
COUNT=$(printf '%s' "$SUSPECT" | grep -c . || true)
PENDING=$(grep -cv '^#\|^$' critic/pending.txt || true)
if [ "$COUNT" -eq 0 ]; then
  ok "no unwired state literals in screens"
  note "$PENDING literals deferred to a later phase, listed in critic/pending.txt"
else
  bad "$COUNT suspected unwired literals"
  printf '%s\n' "$SUSPECT" | head -40
fi

step "5. invented people"
# §Social: never generate plausible human usernames to pad a ladder. The ladder is the one thing in this app
# that has to be trustworthy, and a fabricated member who never posts in the guild feed gets noticed.
#
# Any name a screen renders must be the user, a labelled shadow pacer, or an opaque handle. This greps the
# first string argument of the composables that render a person.
# MemberRow and Post carry the name first; PartnerCard carries a drawable first and the name second.
SCREENS=app/src/main/kotlin/app/gakseong/ui/screens/
NAMES=$( { grep -rhoE '(MemberRow|Post)\( *"[^"]+"' "$SCREENS" 2>/dev/null | grep -oE '"[^"]+"'
           grep -rhoE 'PartnerCard\([^,]+, *"[^"]+"' "$SCREENS" 2>/dev/null | grep -oE '"[^"]+"$'
         } | tr -d '"' | sort -u \
        | grep -vE '^(You|Shadow)$' \
        | grep -vE '^Hunter ' \
        | grep -vE '◇' \
        || true)
NAME_COUNT=$(printf '%s' "$NAMES" | grep -c . || true)
if [ "$NAME_COUNT" -eq 0 ]; then
  ok "no invented people on any screen"
else
  bad "$NAME_COUNT name(s) that are neither the user, a labelled pacer, nor an opaque handle"
  printf '%s\n' "$NAMES"
fi

step "6. CRITIC.md"
{
  echo "# Critic"
  echo
  echo "Generated by \`critic/check.sh\`. Do not edit by hand."
  echo
  echo "\`exists\` means the file and symbol are present. \`wired\` means something outside that file calls it."
  echo "They are two columns because a declared function and a called one are different states, and the gap"
  echo "between them is where this kind of work rots."
  echo
  echo "| phase | capability | exists | wired |"
  echo "|---|---|---|---|"
  while IFS='|' read -r phase name probe; do
    case "${phase:-}" in ''|'#'*) continue;; esac
    # A leading ! marks a declaration-only row: no external caller is expected, so `no` would be a lie.
    local_only="no"
    case "$phase" in '!'*) local_only="yes"; phase="${phase#!}";; esac
    file="${probe%%:*}"; symbol="${probe##*:}"
    exists="no"; wired="no"
    [ "$local_only" = "yes" ] && wired="n/a"
    if [ -f "$file" ] && grep -q "$symbol" "$file" 2>/dev/null; then
      exists="yes"
      base=$(basename "$file")
      if [ "$local_only" = "no" ] && grep -rq "$symbol" --include="*.kt" --exclude="$base" app/src/main engine 2>/dev/null; then
        wired="yes"
      fi
    fi
    echo "| $phase | $name | $exists | $wired |"
  done < critic/inventory.txt
  echo
  echo "## Deferred literals"
  echo
  echo "Screen text that is real state but is waiting on a later phase. Tracked so it cannot be mistaken for"
  echo "finished work. Source: \`critic/pending.txt\`."
  echo
  grep -v '^#\|^$' critic/pending.txt | sed 's/^/- `/;s/$/`/'
} > CRITIC.md
MISSING=$(grep -c '| no |' CRITIC.md || true)
ok "CRITIC.md regenerated, $MISSING capabilities still missing or unwired"

step "verdict"
if [ "$FAIL" -eq 0 ]; then printf '\033[32mPASS\033[0m\n'; else printf '\033[31mFAIL\033[0m\n'; fi
exit "$FAIL"
