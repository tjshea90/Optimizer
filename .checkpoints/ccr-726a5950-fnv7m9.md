# CHECKPOINT 120 — read me first, then TASKS.md

**Written:** 2026-11-11T23:04:49Z · **tests:** all 2 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `1f6a5a3` (this checkpoint is the commit after it)

## Just done
gated v0.3.1 (code 4) and pushed it: handoff self-test, unit tests and the versionCode check passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow_id android.yml, ref ccr-726a5950-fnv7m9, inputs {"full_build":"true","publish":"true","note":"Same as 0.3.0 (What uses my storage), plus a Copy list button on that screen so you can paste the list of biggest apps here in one step. Install this one instead of 0.3.0. It still never deletes or changes anything, and has not been run on the tablet in this version yet."}.
When the run is green, confirm the Release shows the APK (mcp__github__get_release_by_tag v0.3.1), then:  bash tools/record-release.sh v0.3.1 "Same as 0.3.0 (What uses my storage), plus a Copy list button on that screen so you can paste the list of biggest apps here in one step. Install this one instead of 0.3.0. It still never deletes or changes anything, and has not been run on the tablet in this version yet."
NEVER ship v0.3.1 twice (check BUILDLOG.md and the Releases first).

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
    (nothing — the tree is clean as of this checkpoint)

## Last ten checkpoints
```
  5c9cc43 ckpt 111: Release v0.3.0 PUBLISHED by Actions run 43116255076 (https://github.com/tjshea
  43d8978 ckpt 109: Release v0.3.0 DISPATCHED: Actions run 43116255076 (android.yml run_number 5) 
  39bb7c8 ckpt 108: gated v0.3.0 (code 3) and pushed it: handoff self-test, unit tests and the ver
  b50c304 ckpt 107: ST2 coded and verified locally: v0.3.0 'What uses my storage' (StorageActivity
  ec0549f ckpt 99: ST1 done: the PC survey now also answers what fills the storage. 50 read-only c
  57c2a4e ckpt 94: The owner pasted the v0.2.0 Copy report: first real run on the tablet (installe
  b3a27aa ckpt 89: P1 done: the PC-side read-only survey kit. tools/adb/intake-commands.txt (45 re
  8d54b58 ckpt 72: Release v0.2.0 DISPATCHED: Actions run 43112944089 (android.yml run_number 3) a
  e53d58a ckpt 71: gated v0.2.0 (code 2) and pushed it: handoff self-test, unit tests and the vers
  303daa1 ckpt 70: P0 coded and verified locally: v0.2.0 baseline (versionCode 2). The report now 
```

(8 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
