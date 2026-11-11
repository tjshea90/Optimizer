# CHECKPOINT 107 — read me first, then TASKS.md

**Written:** 2026-11-11T22:58:47Z · **tests:** all 2 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `22937be` (this checkpoint is the commit after it)

## Just done
ST2 coded and verified locally: v0.3.0 'What uses my storage' (StorageActivity + StorageReader via StorageStatsManager + pure StorageModel), main-screen button, the report flags a nearly full storage (VERY LOW / LOW) and says WHY memory counters and the swap device are n/a, manifest declares only PACKAGE_USAGE_STATS (reason in BRIEF.md), 34 unit tests, lint clean, APK 47 KB passes verify-apk. NOT yet run on the tablet.

## Do this next
Ship v0.3.0: CLAUDE_PROJECT_DIR=$PWD bash ship.sh "<plain note>", dispatch android.yml {full_build:true, publish:true, note}, wait, mcp__github__get_release_by_tag v0.3.0, bash tools/record-release.sh v0.3.0 "note". Then reply to the owner: what their report showed (storage 97% full is the headline), what v0.3.0 adds (they must switch on Usage access once for the per-app list), the updated PC survey, ST3 (they free space by their own choice, then re-run the storage speed test), ST4 (tell the Browser sessions the update-space rule assumed 16 GB).

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
    (nothing — the tree is clean as of this checkpoint)

## Last ten checkpoints
```
  ec0549f ckpt 99: ST1 done: the PC survey now also answers what fills the storage. 50 read-only c
  57c2a4e ckpt 94: The owner pasted the v0.2.0 Copy report: first real run on the tablet (installe
  b3a27aa ckpt 89: P1 done: the PC-side read-only survey kit. tools/adb/intake-commands.txt (45 re
  8d54b58 ckpt 72: Release v0.2.0 DISPATCHED: Actions run 43112944089 (android.yml run_number 3) a
  e53d58a ckpt 71: gated v0.2.0 (code 2) and pushed it: handoff self-test, unit tests and the vers
  303daa1 ckpt 70: P0 coded and verified locally: v0.2.0 baseline (versionCode 2). The report now 
  69c481c ckpt 61: Research done (R2, G2 ticked): both repo surveys back; key claims re-read in th
  74c47ad ckpt 49: Release v0.1.0 PUBLISHED by GitHub Actions (run 43109636536 green: check 16 s, 
  e928075 ckpt 47: Release v0.1.0 DISPATCHED on GitHub Actions: run 43109636536 (android.yml run_n
  1df73c8 ckpt 46: gated v0.1.0 (code 1) and pushed it: handoff self-test, unit tests and the vers
```

(7 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
