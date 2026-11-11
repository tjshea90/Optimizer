# CHECKPOINT 94 — read me first, then TASKS.md

**Written:** 2026-11-11T22:51:20Z · **tests:** all 2 fast checks green (gradle suite: see ship.sh)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `03212a2` (this checkpoint is the commit after it)

## Just done
The owner pasted the v0.2.0 Copy report: first real run on the tablet (installed, no crash). Saved verbatim in research/tablet/2026-11-11-app-report-v0.2.0.txt and analysed in research/BASELINE_2026-11-11.md. Headline: data partition 27.75 GB with only 996 MB (3%) free; swap 621 MB used of 1.72 GB (type unknown, apps cannot read /proc/swaps or /proc/vmstat on this ROM); flash itself is fine (31.3 MB/s write, 4 KB synced write 1.74 ms); RAM healthy at that moment (691 MB available); uptime 6 days; CPU has crypto extensions. docs/HARDWARE.md updated. TASKS: RPT-1 ticked, ST1-ST4 added (storage is now lever 1).

## Do this next
ST1 then ST2. ST1: add to tools/adb/intake-commands.txt (and the allow-list in tools/test_intake.sh + digest in tools/intake.py): 'dumpsys diskstats', 'du -sk' of a FIXED list of top-level shared folders (Download DCIM Pictures Movies Music Documents Android/data Android/media), 'ls -l /sdcard/Download/*.apk' (installer names/sizes only, no other file names: the repo is public), f2fs segment counters; keep the fixture + tests green. ST2: v0.3.0 StorageActivity (StorageStatsManager, PACKAGE_USAGE_STATS with tools:ignore=ProtectedPermissions, pure StorageModel with JVM tests), BRIEF.md permission reason, ship via ship.sh. Then reply to the owner.

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
     M TASKS.md
     M docs/HARDWARE.md

## Last ten checkpoints
```
  b3a27aa ckpt 89: P1 done: the PC-side read-only survey kit. tools/adb/intake-commands.txt (45 re
  8d54b58 ckpt 72: Release v0.2.0 DISPATCHED: Actions run 43112944089 (android.yml run_number 3) a
  e53d58a ckpt 71: gated v0.2.0 (code 2) and pushed it: handoff self-test, unit tests and the vers
  303daa1 ckpt 70: P0 coded and verified locally: v0.2.0 baseline (versionCode 2). The report now 
  69c481c ckpt 61: Research done (R2, G2 ticked): both repo surveys back; key claims re-read in th
  74c47ad ckpt 49: Release v0.1.0 PUBLISHED by GitHub Actions (run 43109636536 green: check 16 s, 
  e928075 ckpt 47: Release v0.1.0 DISPATCHED on GitHub Actions: run 43109636536 (android.yml run_n
  1df73c8 ckpt 46: gated v0.1.0 (code 1) and pushed it: handoff self-test, unit tests and the vers
  a3ce2ec ckpt 45: Recorded the owner's answers (INBOX hook captured the message verbatim and push
  a203193 ckpt 40: CI proven end to end: Actions run 43103221429 GREEN (check 20 s + build 2m52s: 
```

(4 automatic checkpoint(s) since the last deliberate one — the
session was still mid-step. `git diff` against it shows what changed.)
