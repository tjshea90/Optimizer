#!/usr/bin/env bash
# appinfo.sh - the ONE place that knows what this repo builds (a single app). Source it (. tools/appinfo.sh) or call it:
#   bash tools/appinfo.sh <field>          field = vname vcode gradle package title tag asset apk prerelease last_code
#   bash tools/appinfo.sh of-tag v0.1.0    -> "optimizer" (exit 0) when the tag is this app's, exit 1 otherwise
#
#   Optimizer   :app   package com.tj.optimizer   tag v<versionName>   asset Optimizer-v<versionName>.apk
#
# ship.sh, record-release.sh, resume.sh, verify-apk.sh, test_handoff.sh, .github/scripts/publish-release.sh and
# .github/workflows/android.yml all read versions through here, so the versionCode that is gated is always the one Gradle will really build.
# (The owner's Browser repo builds two apps and takes an <app> argument; this repo has one, so none of these functions do.)
APPINFO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

APP_TITLE="Optimizer"
APP_PACKAGE="com.tj.optimizer"
APP_GRADLE="app/build.gradle.kts"
APP_TAG_PREFIX="v"

app_title()   { echo "$APP_TITLE"; }
app_package() { echo "$APP_PACKAGE"; }
app_gradle()  { echo "$APP_GRADLE"; }
app_apk()     { echo "app/build/outputs/apk/release/app-release.apk"; }
app_vcode()   { grep -m1 -oE 'versionCode *= *[0-9]+' "$APPINFO_ROOT/$APP_GRADLE" 2>/dev/null | grep -oE '[0-9]+'; }
app_vname()   { grep -m1 -oE 'versionName *= *"[^"]+"' "$APPINFO_ROOT/$APP_GRADLE" 2>/dev/null | grep -oE '"[^"]+"' | tr -d '"'; }
app_tag()     { echo "${APP_TAG_PREFIX}$(app_vname)"; }
app_asset()   { echo "${APP_TITLE}-${APP_TAG_PREFIX}$(app_vname).apk"; }
app_prerelease() { case "$(app_vname)" in *-*) echo true ;; *) echo false ;; esac; }   # 0.2.0-alpha1 -> a pre-release
app_of_tag()  { case "${1:-}" in "${APP_TAG_PREFIX}"[0-9]*) echo optimizer ;; *) return 1 ;; esac; }
# highest versionCode ever RECORDED in BUILDLOG.md (0 when none). Line format: | v0.1.0 | code 1 | 2026-11-11T20:15Z | note
app_last_code() {
  local n; n="$(grep -E "^\| ${APP_TAG_PREFIX}[^|]+ \| code [0-9]+ " "$APPINFO_ROOT/BUILDLOG.md" 2>/dev/null | grep -oE 'code [0-9]+' | grep -oE '[0-9]+' | sort -n | tail -1)"
  echo "${n:-0}"
}

# called as a program (not sourced): print one field
if [ "${BASH_SOURCE[0]}" = "$0" ]; then
  if [ "${1:-}" = "of-tag" ]; then app_of_tag "${2:-}"; exit $?; fi
  case "${1:-}" in
    vname) app_vname ;; vcode) app_vcode ;; gradle) app_gradle ;; package) app_package ;; title) app_title ;;
    tag) app_tag ;; asset) app_asset ;; apk) app_apk ;; prerelease) app_prerelease ;; last_code) app_last_code ;;
    *) echo "usage: bash tools/appinfo.sh <vname|vcode|gradle|package|title|tag|asset|apk|prerelease|last_code> | of-tag <tag>" >&2; exit 2 ;;
  esac
fi
