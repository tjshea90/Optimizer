# CHECKPOINT 20 — read me first, then TASKS.md

**Written:** 2026-11-11T21:22:42Z · **tests:** 1 RED: test_handoff.sh (0 green)
**Branch:** `ccr-726a5950-fnv7m9` · **builds on:** `e23cdaa` (this checkpoint is the commit after it)

## Just done
Ported the Browser repo's save ladder into this single-app repo (tools/*.sh, ship.sh, bootstrap.sh, hooks in .claude/settings.json; auto-checkpoint commits are live on GitHub). Android skeleton com.tj.optimizer (minSdk 29, targetSdk 29, memory-snapshot screen, 7 JVM tests). Public dev keystore pinned in signing/CERT_SHA256. TASKS/CLAUDE/BUILDLOG/INBOX written. Local ./gradlew :app:testDebugUnitTest :app:lintRelease :app:assembleRelease is green and tools/verify-apk.sh passes every check on the APK.

## Do this next
1) Rewrite .github/workflows/android.yml (still the Browser's two-app copy): a check job then ONE build job; tag trigger v*; inputs full_build/publish/note; publish=false keeps the APK as a 14-day artifact; adapt .github/actions/setup-build (platform 36 + build-tools 36.0.0 only) and .github/scripts/publish-release.sh (single app, tag v<ver>, asset Optimizer-v<ver>.apk). 2) Write HANDOFF.md, BRIEF.md, README.md, AGENTS.md, docs/HARDWARE.md (Browser's hardware_.txt WITHOUT the Android device id line), docs/ARCHITECTURE.md, research/FEASIBILITY.md. 3) bash tools/test_handoff.sh until green (red now: workflow inputs, HANDOFF/BRIEF missing), then tick I2 and I7 in TASKS.md. 4) Dispatch android.yml full_build=true publish=false on this branch, confirm green and that the artifact APK passes tools/verify-apk.sh (I8). 5) Final report to the owner (I9).

*(resuming? CLAUDE.md's "FIRST ACTION OF EVERY SESSION" says how; the briefing is tools/resume.sh. Do that first, or autosave may stay off all session.)*

## Uncommitted right now
    (nothing — the tree is clean as of this checkpoint)

## Last ten checkpoints
```
```
