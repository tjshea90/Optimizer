# CHECKPOINT 61 — read me first, then TASKS.md

**Written:** 2026-11-11T22:24:44Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `33dbb57` (this checkpoint is the commit after it)

## Just done
Research done (R2, G2 ticked): both repo surveys back; key claims re-read in the source. Wrote research/BOUNDARIES.md (what the Launcher/Browser/ROM own; rules: never kill anything, no resident process, no accessibility service, no second helper, all system changes = owner-run PC adb steps, reversible, measured) and rewrote research/FEASIBILITY.md with evidence. Findings: MediaTek DuraSpeed is ON on the tablet; the Launcher already self-compiles (-m speed), clears Recents and owns the animation scales + battery saver; the Browser exits its process tree when idle; the Launcher's on-device helper dies in 15-27 min and TCP is EPERM. TASKS: proposed plan P0-P5, P0 claimed. Started P0 code: NEW files app/src/main/kotlin/com/tj/optimizer/{Parsers,StorageBench,Format}.kt are written but NOT yet compiled, wired or tested.

## Do this next
P0 (v0.2.0 baseline): 1) extend DeviceFacts (uptimeMs, cpu, swaps, vm, thermal, dataTotal/Free, animScales, appsTotal/System, selfPssKb) with defaults so old tests compile; 2) SystemReader fills them (try/catch per file; PowerManager.currentThermalStatus; StatFs; Settings.Global.getFloat for the 3 scales READ-ONLY; Debug.getMemoryInfo for own PSS); 3) ReportText.build(facts, bench) prints them; 4) MainActivity: third button 'Storage speed test' (Thread, dropCache = Os.posix_fadvise DONTNEED, file in cacheDir, skip if <256 MB free, always deleted); 5) unit tests for Parsers/Format/StorageBench/ReportText; 6) versionCode 2 / versionName 0.2.0; 7) ./gradlew :app:testDebugUnitTest :app:lintRelease :app:assembleRelease + bash tools/verify-apk.sh; 8) ckpt; then P1 (PC intake kit) while deciding whether to ship v0.2.0 via ship.sh.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
    (nothing — the tree is clean as of this checkpoint)

## Last ten checkpoints
```
  74c47ad ckpt 49: Release v0.1.0 PUBLISHED by GitHub Actions (run 43109636536 green: check 16 s, 
  e928075 ckpt 47: Release v0.1.0 DISPATCHED on GitHub Actions: run 43109636536 (android.yml run_n
  1df73c8 ckpt 46: gated v0.1.0 (code 1) and pushed it: handoff self-test, unit tests and the vers
  a3ce2ec ckpt 45: Recorded the owner's answers (INBOX hook captured the message verbatim and push
  a203193 ckpt 40: CI proven end to end: Actions run 43103221429 GREEN (check 20 s + build 2m52s: 
  659bcb9 ckpt 33: Dispatched the CI proof run: Actions run 43103221429 (workflow android.yml, run
  3a22c7d ckpt 32: CI + docs done and the whole tooling self-test is green (63 passed, 0 failed). 
  9fa6a48 ckpt 20: Ported the Browser repo's save ladder into this single-app repo (tools/*.sh, sh
```

(11 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
