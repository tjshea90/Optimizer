# CHECKPOINT 123 — read me first, then TASKS.md

**Written:** 2026-11-11T23:07:03Z · **tests:** all 2 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `e7439a8` (this checkpoint is the commit after it)

## Just done
Release v0.3.1 PUBLISHED by Actions run 43117009887 (https://github.com/tjshea90/Optimizer/releases/tag/v0.3.1, APK 49514 bytes + sha256) and recorded. v0.3.1 = v0.3.0 (What uses my storage: StorageActivity/StorageReader/StorageModel, report flags a nearly full storage, says why vmstat/swaps are n/a, one permission PACKAGE_USAGE_STATS) + a Copy list button. 35 unit tests, lint clean. Builds so far: v0.1.0, v0.2.0, v0.3.0, v0.3.1 all published by Actions and recorded.

## Do this next
WAIT FOR THE OWNER's next data. Expected: (a) the pasted Copy list from What uses my storage (v0.3.1: install, tap What uses my storage, tap Switch on Usage access, toggle Optimizer on, go back, wait ~30 s, tap Copy list) -> save it as research/tablet/2026-11-11-storage-list-v0.3.1.txt, write research/BASELINE_<date>.md ST3 section (what fills the 27.75 GB, safe candidates for the owner to decide, NEVER delete for them); (b) optionally optimizer-report.txt from the PC survey -> bash tools/intake.sh <file>, read research/tablet/LATEST.md, git add research/tablet, ckpt. Then TASKS P2/P3/P4 with numbers. Standing: do not touch animation scales, no killing, no resident process (research/BOUNDARIES.md).

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
    (nothing — the tree is clean as of this checkpoint)

## Last ten checkpoints
```
  39f0cb5 ckpt 121: Release v0.3.1 DISPATCHED: Actions run 43117009887 (android.yml run_number 6) 
  5e06136 ckpt 120: gated v0.3.1 (code 4) and pushed it: handoff self-test, unit tests and the ver
  5c9cc43 ckpt 111: Release v0.3.0 PUBLISHED by Actions run 43116255076 (https://github.com/tjshea
  43d8978 ckpt 109: Release v0.3.0 DISPATCHED: Actions run 43116255076 (android.yml run_number 5) 
  39bb7c8 ckpt 108: gated v0.3.0 (code 3) and pushed it: handoff self-test, unit tests and the ver
  b50c304 ckpt 107: ST2 coded and verified locally: v0.3.0 'What uses my storage' (StorageActivity
  ec0549f ckpt 99: ST1 done: the PC survey now also answers what fills the storage. 50 read-only c
  57c2a4e ckpt 94: The owner pasted the v0.2.0 Copy report: first real run on the tablet (installe
  b3a27aa ckpt 89: P1 done: the PC-side read-only survey kit. tools/adb/intake-commands.txt (45 re
  8d54b58 ckpt 72: Release v0.2.0 DISPATCHED: Actions run 43112944089 (android.yml run_number 3) a
```
