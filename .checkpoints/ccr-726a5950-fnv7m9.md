# CHECKPOINT 89 — read me first, then TASKS.md

**Written:** 2026-11-11T22:44:19Z · **tests:** all 2 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `689e33a` (this checkpoint is the commit after it)

## Just done
P1 done: the PC-side read-only survey kit. tools/adb/intake-commands.txt (45 read-only commands: memory, per-process PSS, LRU/OOM, procstats, dexopt state, packages, background appops, heat, zram, vm counters, storage, DuraSpeed, animation scales), runners intake.sh and intake.bat (the .bat is untried on Windows), owner guide tools/adb/README.md, repo side tools/intake.py + intake.sh (redacts serial/MAC/IP/e-mail/SSID/long digits, refuses on a credential, writes research/tablet/<date>/ + FACTS.md red-flags-first + LATEST.md), synthetic fixture tools/testdata/intake-sample, tools/test_intake.sh (17 checks, a checker proving the list is read-only) wired into test_handoff.sh (65/65). Docs: CLAUDE.md, HANDOFF.md 2b. Release v0.2.0 is published and recorded; TASKS P0 and P1 ticked, OWNER-8 added.

## Do this next
Final report to the owner NOW (they are waiting for: the v0.2.0/v0.1.0 links, what the research found, the plan, and the OWNER-8 ask). Then wait for their data: when optimizer-report.txt or a pasted Copy report arrives: bash tools/intake.sh <file>, read research/tablet/LATEST.md, git add research/tablet, ckpt, then TASKS P2 (research/BASELINE_<date>.md with a number per candidate lever) and P3 (benchmark harness). Open ideas: P0b cold-read timing via Os.mmap+Os.mincore on an installed APK; add Optimizer-pc-tools zip to the next release assets in .github/scripts/publish-release.sh.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
     M TASKS.md

## Last ten checkpoints
```
  8d54b58 ckpt 72: Release v0.2.0 DISPATCHED: Actions run 43112944089 (android.yml run_number 3) a
  e53d58a ckpt 71: gated v0.2.0 (code 2) and pushed it: handoff self-test, unit tests and the vers
  303daa1 ckpt 70: P0 coded and verified locally: v0.2.0 baseline (versionCode 2). The report now 
  69c481c ckpt 61: Research done (R2, G2 ticked): both repo surveys back; key claims re-read in th
  74c47ad ckpt 49: Release v0.1.0 PUBLISHED by GitHub Actions (run 43109636536 green: check 16 s, 
  e928075 ckpt 47: Release v0.1.0 DISPATCHED on GitHub Actions: run 43109636536 (android.yml run_n
  1df73c8 ckpt 46: gated v0.1.0 (code 1) and pushed it: handoff self-test, unit tests and the vers
  a3ce2ec ckpt 45: Recorded the owner's answers (INBOX hook captured the message verbatim and push
  a203193 ckpt 40: CI proven end to end: Actions run 43103221429 GREEN (check 20 s + build 2m52s: 
  659bcb9 ckpt 33: Dispatched the CI proof run: Actions run 43103221429 (workflow android.yml, run
```

(15 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
