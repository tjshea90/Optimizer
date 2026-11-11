# Optimizer

An Android app to optimize **RAM and processes** on one specific tablet for the best speed possible: the *Alps 711 / MediaTek MT8168B, Android 10 Go* tablet
(2 GB RAM, 32-bit, 600x973 at 170 dpi, locked bootloader so no root, **no Google Play Store and no Google services**, internet only through a phone hotspot).

> ### Status: foundation only
> The build, signing, release and save-everything-to-GitHub system is set up and the app is a small **device snapshot** screen (RAM, swap, heap class, low-RAM flag, CPU ABIs, with a *Copy report* button).
> The optimizing features are **not designed yet**: what an ordinary app may do on this tablet is being researched first (`research/FEASIBILITY.md`). Nothing has been released and nothing has been run on a device yet.

## Install on the tablet (once a release exists)
1. Open the [Releases](../../releases) page on the tablet and download `Optimizer-v<version>.apk`. Optional: check it against the `.sha256` file next to it.
2. Open the downloaded file. Android 10 asks you to allow **Install unknown apps** for the app you opened it from: allow it once, go back, tap *Install*.
3. Updating later: install the newer APK over the old one. Every release is signed with the same certificate (`signing/README.md`), so updates install in place.

## How it is built
GitHub Actions tests, builds, signs and publishes every APK (`.github/workflows/android.yml`, run by hand or by a version tag, never on ordinary pushes). Claude writes the code; GitHub makes the APK.
Developed across many Claude Code sessions: every tool call is committed and pushed (`tools/autosave.sh`), `CHECKPOINT.md` says where the last session stopped, and a new session resumes cold from
`HANDOFF.md`. See `CLAUDE.md` for the rules, `TASKS.md` for the job list, `BRIEF.md` for decisions.

Local checks: `./gradlew :app:testDebugUnitTest :app:lintRelease` and `bash tools/test_handoff.sh`.
