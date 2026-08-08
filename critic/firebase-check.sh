#!/usr/bin/env bash
# Validate app/google-services.json before the Gradle plugin does, because the plugin's failure names the
# problem badly and only at build time.
#
# Usage: critic/firebase-check.sh [path-to-google-services.json]

set -uo pipefail
cd "$(dirname "$0")/.."

SRC="${1:-app/google-services.json}"
WANT=$(grep -oE 'applicationId = "[^"]+"' app/build.gradle.kts | cut -d'"' -f2)

if [ ! -f "$SRC" ]; then
  echo "no config at $SRC"
  echo "Firebase is phase 07. Everything before it runs without this file."
  exit 0
fi

python3 - "$SRC" "$WANT" <<'PY'
import json, sys
path, want = sys.argv[1], sys.argv[2]
d = json.load(open(path))
pkgs = [c["client_info"]["android_client_info"]["package_name"] for c in d["client"]]
proj = d["project_info"]["project_id"]

print(f"project: {proj}")
print(f"clients: {', '.join(pkgs)}")
print(f"wanted:  {want}")

if want in pkgs:
    print(f"\nok   {want} is registered")
    others = [p for p in pkgs if p != want]
    if others:
        # Firestore rules and the database are project-wide, so a neighbour shares both.
        print(f"note this project also serves {', '.join(others)}.")
        print("     One Firestore database and one ruleset cover all of them.")
    sys.exit(0)

print(f"\nFAIL no client matches {want}")
print("     Firebase console -> Project settings -> Add app -> Android")
print(f"     Android package name: {want}")
sys.exit(1)
PY
