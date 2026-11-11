# TASKS - the job list. Tick `[x]` only when written, tested, committed (name the test / the run that proves it).

New request from the owner => write it here in their words as `[ ]` boxes and `ckpt` BEFORE any code. Claim an item before starting anything
that takes more than one checkpoint: `bash tools/claim.sh <ID>` (marks it `[~]`), `--done "evidence"` ticks it, `--release` gives it back.

Owner's request 1, 2026-11-11 (verbatim, also in INBOX.md): "Set up this repo for the Claude checkpoint and github save functions and github actions apk
building like my other reps. This repo will be an app to optimize ram and processes on my Android 10 go armv7 mt8168 tablet for the best speed possible"

## Request 1a - save system, GitHub saves, GitHub Actions APK building (same system as the Browser / Launcher repos)
- [x] I1 Study the owner's system: Browser (newest, multi-session), Launcher, LinkMeter - read-only clones, nothing copied blindly; the Browser `tools/` set is the template
- [ ] I2 Port the save ladder (autosave, capture_inbox/outbox, ckpt, sync, claim, resume, install-hooks, push, secretscan, toobig, ship.sh, bootstrap.sh) - files are in and auto-checkpointing is live on GitHub (16+ commits pushed); ticks when I7 is green
- [x] I3 Android skeleton that really builds - `./gradlew :app:testDebugUnitTest :app:lintRelease :app:assembleRelease` BUILD SUCCESSFUL in 30 s here; `bash tools/verify-apk.sh` all OK (pinned certificate, package, version, no GMS, 27 KB)
- [x] I4 Public dev keystore `signing/dev/optimizer-dev.jks` + pinned certificate `signing/CERT_SHA256` (same arrangement the owner chose for Launcher and Browser)
- [ ] I5 `.github/workflows/android.yml` for ONE app: tags + manual dispatch only (never a `push:` branch trigger), inputs `full_build` / `publish` / `note`, GitHub creates the tag; `setup-build` action and `publish-release.sh` adapted
- [ ] I6 Docs for a cold start: CLAUDE.md, HANDOFF.md, BRIEF.md, README.md, AGENTS.md, docs/HARDWARE.md, docs/ARCHITECTURE.md, research/FEASIBILITY.md
- [ ] I7 `bash tools/test_handoff.sh` green (hermetic self-test of the whole save/release system; it also runs inside every `ckpt` and as the first CI gate)
- [ ] I8 Prove the CI end to end: dispatch android.yml with `full_build=true, publish=false` on this branch -> green, APK artifact downloads and passes `tools/verify-apk.sh` (publishes NOTHING)
- [ ] I9 Tell the owner what was set up, what was and was not verified, and the OWNER items below

## OWNER decisions / actions (nothing here blocks the infrastructure; most block the first real feature)
- [ ] OWNER-1 App name + package: placeholder label "Optimizer", `applicationId` `com.tj.optimizer` (same style as com.tj.launcher). The id can never change once installed - say if you want another before the first install
- [ ] OWNER-2 Can you use a PC with a USB cable for one-time `adb shell pm grant ...` permissions on the tablet? (Launcher asked the same: its "one-cable recipe"). Decides how much of research/FEASIBILITY.md is reachable
- [ ] OWNER-3 What does "best speed" mean to you: app launch time, scrolling/UI smoothness, the Browser, everything after a long uptime? Which apps are the heavy ones you notice?
- [ ] OWNER-4 Is an always-on accessibility service acceptable (Launcher already uses one), or should the optimizer only act when you tap a button (default: on demand, because a resident service costs RAM on a 2 GB tablet)?
- [ ] OWNER-5 Optional: a PRIVATE signing key in the repo secrets instead of the public dev key (steps in signing/README.md); optional: make `main` the default branch and set `tools/MIRROR_MAIN` to `on`
- [ ] OWNER-6 First release: say when to publish v0.1.0 (the skeleton "device snapshot" screen) so the signing chain is proven on the real tablet; nothing is published until you say so

## Request 1b - the app: optimize RAM and processes on the MT8168 Android 10 Go tablet for the best speed possible (feature list NOT written yet)
- [ ] R1 Research: what an ordinary (non-root, locked-bootloader) Android 10 app can do for RAM/process speed at each privilege level - `research/FEASIBILITY.md` holds the unverified starting hypotheses; verify against the Android 10 source (android-10.0.0_r47, as Launcher did) and write the findings there
- [ ] R2 Read the Launcher and Browser repos' notes on what this tablet allowed (adb helper, accessibility, grants) so nothing is re-derived: Launcher `HANDOFF.md` / `CLAUDE.md`, Browser `docs/HARDWARE.md`
- [ ] R3 Baseline first: the skeleton's snapshot screen (RAM total/available, swap, heap class, low-RAM flag, ABIs) installed on the tablet, owner pastes the "Copy report" text back; real numbers go into docs/HARDWARE.md
- [ ] R4 Design the feature list with the owner from R1 + OWNER-2/3/4, then write each feature here as `[ ]` boxes with a test that proves it
