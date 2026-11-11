# HANDOFF - read this first if you are a Claude session with NO prior context

The single entry point for a new Claude session or account. After this, in order: `CLAUDE.md` (working agreement, auto-loaded) -> `CHECKPOINT.md`
(where the last session stopped, "Do this next") -> the open `[ ]` boxes of `TASKS.md` -> `BRIEF.md` (decisions that do not change, and what is still open).

## 0. In one minute
- **What:** Optimizer, an Android app to optimize RAM and processes on ONE tablet (MT8168B "Alps 711", Android 10 Go API 29, 2 GB RAM, 32-bit, locked bootloader so no root,
  no Google services, internet only through a phone hotspot) for the best speed possible. **The feature list is not written yet**: what an ordinary app may do on this tablet is
  the open research question (TASKS R1, `research/FEASIBILITY.md`). Do not build kill/boost features before it is answered; measure first.
- **State:** the infrastructure is in place and proven: the save ladder (hooks commit and push after every tool call), the Gradle project (JVM tests, lint, R8 release build, pinned signing key),
  and the GitHub Actions build. The app itself is still the skeleton: a "device snapshot" screen that prints RAM, swap, heap class, low-RAM flag and ABIs, with a Copy button.
  `CHECKPOINT.md` says where work stopped; `BUILDLOG.md` is the list of real releases (**v0.1.0 is published**, built by Actions run 43109636536; it has not been run on the tablet yet).
- **Who the owner is:** runs everything from a tablet/phone and a Claude chat, wants plain-language answers and a clear "what I did / what you must do". Prefers longer sessions and more usage
  if the product gets better, but wants usage spent efficiently. Their other repos (Browser, Launcher, LinkMeter) target the same tablet and are the source of this repo's tooling.
- **Repo:** the GitHub remote `origin` (public). The default branch is the first session branch; later sessions get their own `ccr-*` branch (section 6).

## 1. First 10 minutes
1. The SessionStart hook printed the briefing (`tools/resume.sh`). Missing? `bash bootstrap.sh`, and run `bash tools/install-hooks.sh`. Verify over 2-3 edits that `git log --oneline -3` shows `auto-checkpoint:` commits.
2. Read `CHECKPOINT.md` -> do "Do this next". Do not re-plan finished work.
3. New message from the owner? Write it into `TASKS.md` in their words as `[ ]` boxes, `bash tools/ckpt.sh "did" "next"`, THEN code. Answer every message.
4. Fast local checks: `./gradlew :app:testDebugUnitTest` (JVM, no emulator), `bash tools/test_handoff.sh` (the save/release tooling), `bash tools/repomap.sh` (what every source file is).

## 2. Release routine (GitHub Actions builds, Claude never builds a release here)
1. Bump `versionCode` (strictly higher than the last line in `BUILDLOG.md`) and `versionName` in `app/build.gradle.kts`; `bash tools/ckpt.sh "did" "next"`.
2. `bash ship.sh "what changed"`: runs the cheap local gates (handoff self-test, versionCode, unit tests), pushes, and writes the dispatch recipe into CHECKPOINT.md.
3. Dispatch `.github/workflows/android.yml`: `mcp__github__actions_run_trigger` method `run_workflow`, `workflow_id` android.yml, `ref` = the current branch,
   `inputs` `{"full_build":"true","publish":"true","note":"..."}`. The run checks the signing key against `signing/CERT_SHA256`, runs the unit tests and lint, builds the R8 release APK,
   audits it (`tools/verify-apk.sh`: certificate, package, version, no GMS, size) and creates the GitHub Release `v<versionName>` with `Optimizer-v<versionName>.apk` + sha256 attached
   (the TAG is created by GitHub: a tag push from here is refused with 403).
   - `publish` false (the default) builds and verifies the same way but makes NO Release: the APK stays a workflow artifact for 14 days. Use it to prove CI changes.
   - `full_build` false is a gates-only check, about a minute.
4. Wait without spending tokens: a Bash `run_in_background` loop `until [ "$(date -u +%H%M%S)" -ge HHMMSS ]; do sleep 20; done` (a foreground `sleep` is blocked), then `actions_list list_workflow_runs`
   (`resource_id` android.yml, `workflow_runs_filter {"event":"workflow_dispatch"}`, `perPage` 1) -> `list_workflow_jobs` -> `get_job_logs` (404 until the job ENDS; `tail_lines` about 60).
5. Green + the Release shows its APK (`get_release_by_tag`): `bash tools/record-release.sh <tag> "note"`. Then give the owner the Release link as plain tappable text on its own line.
6. NEVER ship a tag twice (check BUILDLOG.md and the Releases first). Never publish a Release the owner has not asked for.

## 2b. Real tablet data (the owner sends it; nothing else shows what the tablet is doing)
- Owner runs `tools/adb/intake.bat` (Windows) or `intake.sh` with the USB cable: a READ-ONLY survey into `optimizer-report.txt` (guide for the owner: `tools/adb/README.md`). You run `bash tools/intake.sh <file>` (or `-` for pasted text): it REDACTS (serials, MAC/IP, e-mail, Wi-Fi names, long digit runs),
  refuses the whole file if a credential-shaped string is left, saves `research/tablet/<date>/{optimizer-report.txt,FACTS.md,INTAKE.txt}` and `research/tablet/LATEST.md`. Read LATEST.md (red flags first), `git add research/tablet`, `ckpt`, then decide with the numbers (TASKS P2).
- The app's own v0.2.0 "Copy report" text and its "Storage speed test" are the no-cable equivalent for memory/storage; the survey adds per-process memory, compile state, thrash counters, heat and the ROM's background manager.

## 3. Test system (what runs where)
- `app/src/test` JVM tests (pure logic: `MemInfoTest`), Android Lint on the release variant (`NewApi` guards minSdk 29), `tools/test_handoff.sh` (about 60 checks of the save/release system with a throwaway repo and a fake remote).
  The workflow repeats all of it on GitHub. There is no emulator in the cloud sandbox (no KVM) and no instrumented test yet: say honestly what was not run on the real tablet.
- A new feature gets a pure-Kotlin class with a JVM test first; the Activity only wires it up.

## 4. Architecture map
`app/` (package `com.tj.optimizer`): `MainActivity`, `MemInfo` (+ `formatKb`), `ReportText`/`DeviceFacts`, `SystemReader`. Details and the signing/CI/save-system picture: `docs/ARCHITECTURE.md`. Device facts: `docs/HARDWARE.md`.

## 5. Decisions already made - do not redo
minSdk 29 / targetSdk 29 / compileSdk 36, Kotlin + framework Views + no libraries, no native code, R8 on, pinned toolchain (AGP 8.13.2, Kotlin 2.3.10, Gradle 8.14.3), GitHub builds every APK,
public dev signing key pinned in `signing/CERT_SHA256`, package `com.tj.optimizer` (placeholder, OWNER-1), no permissions until a feature needs one: `BRIEF.md`.

## 6. Taking over after a cut-off session or another account
The repo is the ONLY memory: what a session pushed is safe; what it knew only in its head (not in CHECKPOINT/TASKS) is gone. Read, verify, then continue the OLD session's "Do this next".
1. `git fetch --all --prune`; `tools/resume.sh` names any branch whose commits this one lacks. The newest commit time is the other session's heartbeat: under ~20 minutes = it MAY still run
   (do not release or touch its CI runs; ask the owner), older = cut off: `tools/sync.sh` merges its branch (a conflict is printed, never forced; keep both sides of CHECKPOINT/TASKS conflicts) and you continue.
2. Before dispatching anything: `actions_list list_workflow_runs` for android.yml and `list_releases`, compare with BUILDLOG.md ("RELEASED BUT NEVER RECORDED" is printed by resume.sh).
3. Branches: develop and push ONLY on the branch the session was assigned; never force-push; `tools/MIRROR_MAIN` is `off` (landing on `main` is the owner's call).

## 7. What waits on the owner
The owner answered the first questions on 2026-11-11 (TASKS "Owner request 2"): name OK, a PC with a USB cable and adb is available, the goal is a snappy tablet (fast app loading, a fast Browser, no animations) without breaking or interfering with their custom Launcher, an accessibility service is acceptable if it breaks nothing,
the public signing key is fine, and GitHub builds the releases. Still open: OWNER-7 (optional: `main` as the default branch). Everything that changes system state needs their approval of a specific, reversible plan first (TASKS G2-G4).
