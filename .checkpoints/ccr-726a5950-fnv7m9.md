# CHECKPOINT 99 — read me first, then TASKS.md

**Written:** 2026-11-11T22:53:50Z · **tests:** all 2 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `16c8738` (this checkpoint is the commit after it)

## Just done
ST1 done: the PC survey now also answers what fills the storage. 50 read-only commands (added dumpsys diskstats, du -sk of a fixed list of shared folders, ls -l of Download/*.apk, F2FS free/dirty/lifetime-write counters, eMMC life_time/pre_eol_info). tools/intake.py masks installer file names that are not the owner's own app builds (public repo), digests kinds / biggest apps / shared folders / installers with a red flag. Fixture extended, tools/test_intake.sh 19/19 (allow-list widened for exactly those five commands). Owner guide updated.

## Do this next
ST2: v0.3.0 in-app 'What uses my storage' (read-only). Plan: AndroidManifest add <uses-permission android:name=android.permission.PACKAGE_USAGE_STATS tools:ignore=ProtectedPermissions/> + StorageActivity (exported=false); pure app/src/main/kotlin/com/tj/optimizer/StorageModel.kt (sort, top-N, totals, verdict text, formatting) with JVM tests; StorageReader.kt (StorageStatsManager.getTotalBytes/getFreeBytes/queryExternalStatsForUser/queryStatsForPackage per installed app, off the UI thread, each call guarded; SecurityException = no Usage access yet -> show a button to Settings.ACTION_USAGE_ACCESS_SETTINGS); tap a row -> Settings.ACTION_APPLICATION_DETAILS_SETTINGS for that package (the owner clears cache there); NEVER delete anything. Main screen: button + a storage verdict line; report says WHY memory counters/swap device are n/a. BRIEF.md: record the permission and its reason. Version 0.3.0 (code 3), ship.sh, dispatch, record.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
    (nothing — the tree is clean as of this checkpoint)

## Last ten checkpoints
```
  57c2a4e ckpt 94: The owner pasted the v0.2.0 Copy report: first real run on the tablet (installe
  b3a27aa ckpt 89: P1 done: the PC-side read-only survey kit. tools/adb/intake-commands.txt (45 re
  8d54b58 ckpt 72: Release v0.2.0 DISPATCHED: Actions run 43112944089 (android.yml run_number 3) a
  e53d58a ckpt 71: gated v0.2.0 (code 2) and pushed it: handoff self-test, unit tests and the vers
  303daa1 ckpt 70: P0 coded and verified locally: v0.2.0 baseline (versionCode 2). The report now 
  69c481c ckpt 61: Research done (R2, G2 ticked): both repo surveys back; key claims re-read in th
  74c47ad ckpt 49: Release v0.1.0 PUBLISHED by GitHub Actions (run 43109636536 green: check 16 s, 
  e928075 ckpt 47: Release v0.1.0 DISPATCHED on GitHub Actions: run 43109636536 (android.yml run_n
  1df73c8 ckpt 46: gated v0.1.0 (code 1) and pushed it: handoff self-test, unit tests and the vers
  a3ce2ec ckpt 45: Recorded the owner's answers (INBOX hook captured the message verbatim and push
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
