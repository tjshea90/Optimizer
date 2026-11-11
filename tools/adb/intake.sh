#!/usr/bin/env bash
# intake.sh - READ-ONLY survey of the tablet from a PC over adb (Linux, macOS, Windows Git Bash). It changes NOTHING on the tablet.
#   bash intake.sh [output-file]        default output: optimizer-report.txt in the current folder
# It runs every command in intake-commands.txt (reviewed list, read-only) with `adb shell`, and writes the answers into one text file.
# Send that file to Claude; the repo's tools/intake.sh redacts it (serial numbers, MAC/IP addresses, e-mail, Wi-Fi names) before it is saved anywhere.
# Needs: adb on the PATH, USB debugging on, and "Allow" tapped on the tablet for this computer.
set -u
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT="${1:-optimizer-report.txt}"
if ! command -v adb >/dev/null 2>&1; then
  echo "adb was not found. Install the Android platform-tools and put adb on your PATH, then run this again."; exit 1
fi
STATE="$(adb get-state 2>&1 | tr -d '\r')"
if [ "$STATE" != "device" ]; then
  echo "No tablet is ready (adb says: $STATE)."
  echo "Plug in the cable, turn on USB debugging (Settings > Developer options) and tap Allow on the tablet, then run this again."; exit 1
fi
{
  echo "# optimizer-report: READ-ONLY survey written by tools/adb/intake.sh"
  echo "# host date: $(date -u +%Y-%m-%dT%H:%M:%SZ)"
} > "$OUT"
N=0
while IFS='|' read -r label cmd; do
  case "$label" in ''|'#'*) continue ;; esac
  N=$((N+1)); printf '  [%02d] %s\n' "$N" "$label"
  {
    echo
    echo "##### $label"
    if command -v timeout >/dev/null 2>&1; then timeout 120 adb shell "$cmd" 2>&1; else adb shell "$cmd" 2>&1; fi
  } | tr -d '\r' >> "$OUT"
done < "$HERE/intake-commands.txt"
echo "##### end" >> "$OUT"
echo "Done: $OUT ($(wc -c < "$OUT" | tr -d ' ') bytes, $N sections). Send this file to Claude. Nothing on the tablet was changed."
