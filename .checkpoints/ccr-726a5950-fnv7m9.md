# CHECKPOINT 121 — read me first, then TASKS.md

**Written:** 2026-11-11T23:05:30Z · **tests:** all 2 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `5e06136` (this checkpoint is the commit after it)

## Just done
Release v0.3.1 DISPATCHED: Actions run 43117009887 (android.yml run_number 6) at 23:05Z on head 5e06136, inputs full_build=true publish=true. v0.3.1 = v0.3.0 plus a Copy list button on the storage screen; v0.3.0 (published minutes earlier, not yet installed by the owner) is superseded.

## Do this next
WAIT for run 43117009887 - do NOT dispatch again (never ship v0.3.1 twice). Check mcp__github__actions_get get_workflow_run 43117009887 (success), mcp__github__get_release_by_tag v0.3.1 lists Optimizer-v0.3.1.apk + .sha256, then bash tools/record-release.sh v0.3.1 "<note from ckpt 120>". Then the owner reply.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
    (nothing — the tree is clean as of this checkpoint)

## Last ten checkpoints
```
  5e06136 ckpt 120: gated v0.3.1 (code 4) and pushed it: handoff self-test, unit tests and the ver
  5c9cc43 ckpt 111: Release v0.3.0 PUBLISHED by Actions run 43116255076 (https://github.com/tjshea
  43d8978 ckpt 109: Release v0.3.0 DISPATCHED: Actions run 43116255076 (android.yml run_number 5) 
  39bb7c8 ckpt 108: gated v0.3.0 (code 3) and pushed it: handoff self-test, unit tests and the ver
  b50c304 ckpt 107: ST2 coded and verified locally: v0.3.0 'What uses my storage' (StorageActivity
  ec0549f ckpt 99: ST1 done: the PC survey now also answers what fills the storage. 50 read-only c
  57c2a4e ckpt 94: The owner pasted the v0.2.0 Copy report: first real run on the tablet (installe
  b3a27aa ckpt 89: P1 done: the PC-side read-only survey kit. tools/adb/intake-commands.txt (45 re
  8d54b58 ckpt 72: Release v0.2.0 DISPATCHED: Actions run 43112944089 (android.yml run_number 3) a
  e53d58a ckpt 71: gated v0.2.0 (code 2) and pushed it: handoff self-test, unit tests and the vers
```
