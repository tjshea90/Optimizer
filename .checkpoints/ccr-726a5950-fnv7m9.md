# CHECKPOINT 46 — read me first, then TASKS.md

**Written:** 2026-11-11T22:11:20Z · **tests:** all 1 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `a3ce2ec` (this checkpoint is the commit after it)

## Just done
gated v0.1.0 (code 1) and pushed it: handoff self-test, unit tests and the versionCode check passed here. NOT yet built - GitHub has not been asked.

## Do this next
TRIGGER THE BUILD: mcp__github__actions_run_trigger, method run_workflow, workflow_id android.yml, ref ccr-726a5950-fnv7m9, inputs {"full_build":"true","publish":"true","note":"First release: a device snapshot screen showing total and free RAM, swap, the app heap limit, the low-RAM flag and the CPU type, with a Copy report button. No optimizing features yet and it has not been run on the tablet yet."}.
When the run is green, confirm the Release shows the APK (mcp__github__get_release_by_tag v0.1.0), then:  bash tools/record-release.sh v0.1.0 "First release: a device snapshot screen showing total and free RAM, swap, the app heap limit, the low-RAM flag and the CPU type, with a Copy report button. No optimizing features yet and it has not been run on the tablet yet."
NEVER ship v0.1.0 twice (check BUILDLOG.md and the Releases first).

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
    (nothing — the tree is clean as of this checkpoint)

## Last ten checkpoints
```
  a3ce2ec ckpt 45: Recorded the owner's answers (INBOX hook captured the message verbatim and push
  a203193 ckpt 40: CI proven end to end: Actions run 43103221429 GREEN (check 20 s + build 2m52s: 
  659bcb9 ckpt 33: Dispatched the CI proof run: Actions run 43103221429 (workflow android.yml, run
  3a22c7d ckpt 32: CI + docs done and the whole tooling self-test is green (63 passed, 0 failed). 
  9fa6a48 ckpt 20: Ported the Browser repo's save ladder into this single-app repo (tools/*.sh, sh
```
