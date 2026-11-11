# CHECKPOINT 45 — read me first, then TASKS.md

**Written:** 2026-11-11T22:10:23Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `73deab3` (this checkpoint is the commit after it)

## Just done
Recorded the owner's answers (INBOX hook captured the message verbatim and pushed it): name OK, adb cable available, goals = snappy OS / fast app loading / fast Browser / no animations WITHOUT breaking the custom Launcher, accessibility OK if nothing breaks or interferes, public key fine, GitHub should build the release. TASKS: OWNER-1..6 answered, new REL-1 and G1-G4, R2 claimed.

## Do this next
REL-1: run  CLAUDE_PROJECT_DIR=$PWD bash ship.sh "First release: device snapshot screen..."  (gates + local unit tests + push), then dispatch android.yml {full_build:true, publish:true, note:...} on ccr-726a5950-fnv7m9 via mcp__github__actions_run_trigger, wait with a background until-loop, verify the Release (mcp__github__get_release_by_tag v0.1.0), bash tools/record-release.sh v0.1.0 "note", ckpt, tell the owner the link. In parallel while CI builds: R2/G2 - read the Launcher repo's notes (clone at tjshea90/launcher under the scratchpad's sibling folder, read-only) and write research/BOUNDARIES.md.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
    (nothing — the tree is clean as of this checkpoint)

## Last ten checkpoints
```
  a203193 ckpt 40: CI proven end to end: Actions run 43103221429 GREEN (check 20 s + build 2m52s: 
  659bcb9 ckpt 33: Dispatched the CI proof run: Actions run 43103221429 (workflow android.yml, run
  3a22c7d ckpt 32: CI + docs done and the whole tooling self-test is green (63 passed, 0 failed). 
  9fa6a48 ckpt 20: Ported the Browser repo's save ladder into this single-app repo (tools/*.sh, sh
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
