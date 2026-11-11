# CHECKPOINT 111 — read me first, then TASKS.md

**Written:** 2026-11-11T23:02:38Z · **tests:** all 2 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `5cfcca6` (this checkpoint is the commit after it)

## Just done
Release v0.3.0 PUBLISHED by Actions run 43116255076 (https://github.com/tjshea90/Optimizer/releases/tag/v0.3.0, APK 48358 bytes + sha256) and recorded in BUILDLOG.md. Softened the release-notes template in .github/scripts/publish-release.sh: it no longer states the Browser repo's 'a tenth of storage must stay free' rule (derived for a 16 GB tablet; this one is 27.75 GB).

## Do this next
Reply to the owner now (analysis of their pasted report: storage 97% full is the headline; swap 621 MB of 1.72 GB; flash speed fine; 6-day uptime; what v0.3.0 adds and the one-time Usage access switch; updated PC survey; what to do first: look at What uses my storage, free space by their own choice, then re-run the Storage speed test; tell the Browser sessions the update-space rule assumed 16 GB). Then wait for their next data (screenshot/paste of What uses my storage, or optimizer-report.txt from the PC survey): bash tools/intake.sh <file>, TASKS ST3/P2.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
     M .github/scripts/publish-release.sh

## Last ten checkpoints
```
  43d8978 ckpt 109: Release v0.3.0 DISPATCHED: Actions run 43116255076 (android.yml run_number 5) 
  39bb7c8 ckpt 108: gated v0.3.0 (code 3) and pushed it: handoff self-test, unit tests and the ver
  b50c304 ckpt 107: ST2 coded and verified locally: v0.3.0 'What uses my storage' (StorageActivity
  ec0549f ckpt 99: ST1 done: the PC survey now also answers what fills the storage. 50 read-only c
  57c2a4e ckpt 94: The owner pasted the v0.2.0 Copy report: first real run on the tablet (installe
  b3a27aa ckpt 89: P1 done: the PC-side read-only survey kit. tools/adb/intake-commands.txt (45 re
  8d54b58 ckpt 72: Release v0.2.0 DISPATCHED: Actions run 43112944089 (android.yml run_number 3) a
  e53d58a ckpt 71: gated v0.2.0 (code 2) and pushed it: handoff self-test, unit tests and the vers
  303daa1 ckpt 70: P0 coded and verified locally: v0.2.0 baseline (versionCode 2). The report now 
  69c481c ckpt 61: Research done (R2, G2 ticked): both repo surveys back; key claims re-read in th
```
