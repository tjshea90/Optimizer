# CHECKPOINT 108 — read me first, then TASKS.md

**Written:** 2026-11-11T22:59:18Z · **tests:** all 2 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `b50c304` (this checkpoint is the commit after it)

## Just done
gated v0.3.0 (code 3) and pushed it: handoff self-test, unit tests and the versionCode check passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow_id android.yml, ref ccr-726a5950-fnv7m9, inputs {"full_build":"true","publish":"true","note":"Storage version. New button What uses my storage lists the apps using the most space and the totals for pictures, video and audio. Tap an app to open its own Android settings page (Storage, then Clear cache). It needs Usage access switched on for Optimizer once; a button takes you there. The report now also warns when storage is nearly full and says why some lines show n/a. Optimizer still never deletes or changes anything, and has not been run on the tablet in this version yet."}.
When the run is green, confirm the Release shows the APK (mcp__github__get_release_by_tag v0.3.0), then:  bash tools/record-release.sh v0.3.0 "Storage version. New button What uses my storage lists the apps using the most space and the totals for pictures, video and audio. Tap an app to open its own Android settings page (Storage, then Clear cache). It needs Usage access switched on for Optimizer once; a button takes you there. The report now also warns when storage is nearly full and says why some lines show n/a. Optimizer still never deletes or changes anything, and has not been run on the tablet in this version yet."
NEVER ship v0.3.0 twice (check BUILDLOG.md and the Releases first).

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
    (nothing — the tree is clean as of this checkpoint)

## Last ten checkpoints
```
  b50c304 ckpt 107: ST2 coded and verified locally: v0.3.0 'What uses my storage' (StorageActivity
  ec0549f ckpt 99: ST1 done: the PC survey now also answers what fills the storage. 50 read-only c
  57c2a4e ckpt 94: The owner pasted the v0.2.0 Copy report: first real run on the tablet (installe
  b3a27aa ckpt 89: P1 done: the PC-side read-only survey kit. tools/adb/intake-commands.txt (45 re
  8d54b58 ckpt 72: Release v0.2.0 DISPATCHED: Actions run 43112944089 (android.yml run_number 3) a
  e53d58a ckpt 71: gated v0.2.0 (code 2) and pushed it: handoff self-test, unit tests and the vers
  303daa1 ckpt 70: P0 coded and verified locally: v0.2.0 baseline (versionCode 2). The report now 
  69c481c ckpt 61: Research done (R2, G2 ticked): both repo surveys back; key claims re-read in th
  74c47ad ckpt 49: Release v0.1.0 PUBLISHED by GitHub Actions (run 43109636536 green: check 16 s, 
  e928075 ckpt 47: Release v0.1.0 DISPATCHED on GitHub Actions: run 43109636536 (android.yml run_n
```
