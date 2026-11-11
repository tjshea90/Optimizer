# Optimizer - working agreement (auto-loaded every session)

## Mission
An Android app that optimizes **RAM and processes** on ONE device so it runs as fast as possible: the MediaTek **MT8168B** tablet ("Alps 711", `tb8168p1_bsp`),
**Android 10 Go (API 29)**, **2 GB RAM** (`ro.config.low_ram=true`, 128 MB app heap class), **32-bit userspace (armeabi-v7a)**, 600x973 @170 dpi (font scale 1.2),
Mali-G52 MC2, **locked bootloader, no root**, unencrypted storage, **no Play Store and no Google services (never add a GMS dependency)**, internet only through a phone hotspot (metered).
Device profile: `docs/HARDWARE.md`. Apps are written for Android 10 but must also run on Android 16 (`Build.VERSION.SDK_INT` branching above API 29).
**The feature list is NOT written yet.** What an ordinary app may do on this device is the open research question (`research/FEASIBILITY.md`, TASKS R1); do not promise or build kill/boost features before it is answered.
Package `com.tj.optimizer`, label "Optimizer" (placeholders, TASKS OWNER-1). Decisions and open questions: `BRIEF.md`. New to the project with no context: `HANDOFF.md`.
The job list: `TASKS.md`. Where the last session stopped: `CHECKPOINT.md`.

## FIRST ACTION OF EVERY SESSION - cold resume (assume the last session was cut off mid-step)
1. The SessionStart hook ran `tools/resume.sh`: sync state, any **INTERRUPTED MID-CHANGE** / **UNCOMMITTED WORK** warning, other branches holding newer work, `CHECKPOINT.md`, the open `TASKS.md` items, the INBOX/OUTBOX tails.
   Not seen? `bash bootstrap.sh`, tell the owner it did not fire, and run `bash tools/install-hooks.sh`.
2. **`CHECKPOINT.md` "Do this next" is authoritative.** Do not re-plan or redo finished work. Check the INBOX tail against TASKS: anything in INBOX that TASKS lacks is a request nobody wrote down.
3. Verify the safety net over 2-3 real edits: `git log --oneline -3` shows `auto-checkpoint:` commits. If not, run `bash tools/ckpt.sh "did" "next"` by hand after every step and say so.

## The save ladder (assume you can be cut off at ANY moment; work is only safe once PUSHED) - the same system as the owner's Browser/Launcher repos
| Rung | What | Who | Saves |
| :- | :- | :- | :- |
| 0 | `tools/capture_inbox.sh` (UserPromptSubmit) | hook | every message the owner sends, verbatim (keys redacted), committed + pushed before any work -> `INBOX.md` |
| 0b | `tools/capture_outbox.sh` (autosave on Stop and after tool calls) | hook | what Claude told the owner (last reply of every turn, substantial mid-turn text) -> `OUTBOX.md` |
| 1 | `tools/autosave.sh` (PostToolUse Edit/Write/Bash, Stop) | hook | `git add -A`, secret scan, commit `auto-checkpoint:`, push - after every tool call; no tests, no gate |
| 2 | `bash tools/ckpt.sh "did" "next"` | **you, after every completed step** | rewrites `CHECKPOINT.md`, fast self-test, commit, push |
| 3 | `bash ship.sh "note"` | you, at real versions | gates, push, queue the GitHub build |
| 4 | GitHub Actions `android.yml` | GitHub | tests, builds, SIGNS, publishes the Release; `BUILDLOG.md` is the durable record |
- **"next" is the one thing no diff can reconstruct.** Make it specific (file, function, command, run id). Run `ckpt` after every step, never hold findings or intent only in the conversation or in scratch files.
- **New request from the owner:** write it into `TASKS.md` in their words as `[ ]` boxes and `ckpt` BEFORE any code. Tick a box only when written, tested and committed; name the test or run that proves it.
- **Low on usage?** Spend what is left on `ckpt` with an honest "next", not on one more edit. A cut-off notice that names a reset time: `send_later` at reset + 2 minutes ("resume Optimizer: bash tools/resume.sh, continue Do this next").
- **Messages from the owner (standing rule):** if several arrive while a task runs, keep going, add each as the next step in TASKS.md, never drop one, and answer every one (re-list open ones at the end of a reply).
- **Push failure is never silent:** autosave warns when commits exist only in this container (lost when the session ends). Fix the connection and `git push origin HEAD` at once.
- **The repo is PUBLIC and autosave runs `git add -A` after every tool call:** anything copied into the repo is on GitHub within seconds, for good. The owner's photos/screenshots and personal data stay OUT of it (describe them in words);
  the device's Android ID is deliberately left out of `docs/hardware/`. INBOX/OUTBOX copy chat text into the repo (keys redacted): never paste secrets in chat. `tools/secretscan.sh` blocks key-shaped text and unstages key files / files over 90 MB; never bypass it.
- Compaction is normal: `tools/toobig.sh` saves everything first and the SessionStart hook re-injects the briefing. `ckpt` with an exact "next" when the context is about 60 % full.
- If your harness wants specific attribution lines on commits, write them once to `.git/ckpt-trailer` (untracked); `ckpt` appends them instead of its default co-author line.
- `install-hooks.sh --check` run from a shell WITHOUT `CLAUDE_PROJECT_DIR` says MISSING although the committed `.claude/settings.json` is what the session reads; `ckpt`/`resume` then merge the same hooks into the parent directory's `.claude/settings.json`, which is harmless either way. `CLAUDE_PROJECT_DIR="$PWD" bash tools/ckpt.sh ...` avoids that write.

## Many sessions and accounts at once
Every session has its own branch; the owner may run several accounts in parallel or one after another. The tooling does the merging so nobody starts from a stale state and nothing is overwritten:
- `tools/sync.sh` (runs at session start and inside every `ckpt`) merges every other branch that has commits yours lacks: a cut-off session's whole tip (its half-finished change is the work to resume), an active session's last deliberate checkpoint only.
  A conflict is aborted and printed (`SYNC CONFLICT ... in: files`): then `git merge origin/<branch>` by hand, keep both sides, `git commit --no-edit`. INBOX/OUTBOX/BUILDLOG/TASKS merge as unions; `CHECKPOINT.md` keeps the newest card.
- Each session also keeps its own card in `.checkpoints/<branch>.md`; the briefing lists the other lanes ("lane <branch> (N min ago): next ...") - read them before choosing work.
- **Claim before you start** anything that takes more than one checkpoint: `bash tools/claim.sh <ID>` marks the TASKS.md item `[~] ... {branch time}` and pushes it; `--done "evidence"` ticks it, `--release` gives it back. An item claimed by a branch with a commit under 90 minutes old is alive: pick another.
- Releases cannot collide: the workflow holds a repo-wide lock for the build job. Only the session that ran `ship.sh` records the release.
- A new session has NO context: everything it needs is `CHECKPOINT.md` + `TASKS.md` + `HANDOFF.md` + `research/*.md`. **Research findings and design decisions go into `research/*.md` / `docs/*.md` the moment they exist, never only in the chat.** Spend no usage re-deriving what a file already says.
- Owner's standing rules: respond to EVERY prompt (even if they interrupt: re-list the open ones at the end of a reply); if a tool call was denied, re-send that same call ONCE and say so in one line (the owner may have pressed deny by accident), then adapt.
  Plain-language answers with "what I did / what you must do"; the owner prefers longer sessions and more usage when the product gets better, but wants usage spent efficiently.

## GitHub Actions does the building (the owner's rule on all repos: "Claude codes the app, GitHub makes the apk")
`.github/workflows/android.yml` tests, builds, signs and releases the APK. **Minutes are scarce: never add a `push:` branch trigger to any workflow** (autosave pushes after every tool call; `tools/test_handoff.sh` fails if you do). Tags and manual dispatch only.
- Release order: bump versionCode/versionName in `app/build.gradle.kts` -> `bash ship.sh "note"` -> dispatch the workflow (`mcp__github__actions_run_trigger`, `run_workflow`, `workflow_id` android.yml, `ref` = current branch, inputs `full_build` true, `publish` true, `note`) ->
  wait (background `until` loop, a foreground `sleep` is blocked; `get_job_logs` answers 404 until the job ends, so polling it is waste) -> Release shows the APK -> `bash tools/record-release.sh <tag> "note"` ONLY after the run is green ->
  give the owner the Release link as plain tappable text on its own line. Full recipe: `HANDOFF.md` section 2.
- `full_build=false` is a ~1 min check of the gates. `full_build=true, publish=false` tests, builds, signs and verifies, then keeps the APK as a workflow artifact for 14 days and publishes NOTHING: the way to prove a CI change without burning a version.
- A tag PUSH from the sandbox is refused (403); the workflow creates the tag server-side (`gh release create --target`). Never ship a tag twice. Never publish a Release unless the owner said to (TASKS OWNER-6).
- Signing: every release uses the pinned certificate in `signing/CERT_SHA256` (the PUBLIC dev key in `signing/dev/`, or a private key from the repo secrets when the owner adds one); `tools/verify-apk.sh` fails the build if the APK carries another certificate, because the tablet would refuse the update. Details: `signing/README.md`.
- Local builds are for tests, lint and debug only; `ship.sh --local` exists as a fallback when Actions is unavailable. Never commit an APK. Deliver by Release link, not by file.

## Branches
Develop and push ONLY on the branch the session was assigned (the first one was `ccr-726a5950-fnv7m9`, which is also the repo's default branch). Never force-push, never rewrite pushed history, no PRs unless asked. `tools/MIRROR_MAIN` is `off`: landing on `main` is the owner's call
(`push.sh` refuses to auto-push main/master). `resume.sh` names every other remote branch with unmerged commits; merge deliberately, not blindly.

## Auto-resume after a cut-off
The save ladder keeps the WORK; a scheduled wake keeps the TIME. For a long job (more than a couple of steps left) make sure ONE heartbeat exists (`mcp__claude-code-remote__create_trigger`, cron `0 */2 * * *`, bound to this session, prompt = "resume Optimizer: bash tools/resume.sh, continue
Do this next; stand down if another session's real work is under 60 minutes old"), put its id in every ckpt "next", read it with `get_trigger` (never `list_triggers`: ~20 KB), and `delete_trigger`/disable it when the job is shipped. A dead session cannot restart itself; a wake needs available usage.

## Commands
- `./gradlew :app:testDebugUnitTest` - the JVM unit tests (no emulator needed). `./gradlew :app:lintRelease` (NewApi matters: minSdk 29). `./gradlew :app:assembleRelease` then `bash tools/verify-apk.sh` - the release APK and its audit (local check only; GitHub makes the real one).
- **Tablet data:** the owner runs `tools/adb/intake.bat` (or `.sh`) on their PC with the cable; it writes `optimizer-report.txt` (READ-ONLY survey, the command list is `tools/adb/intake-commands.txt`). When they send it: `bash tools/intake.sh <file>` (or `-` for pasted text) redacts it, saves `research/tablet/<date>/` and writes `FACTS.md`; read `research/tablet/LATEST.md` (red flags first), then `git add research/tablet` and `ckpt`. The in-app equivalent is the v0.2.0 "Copy report" text.
- `bash tools/test_handoff.sh` - self-test of the save/release tooling and the survey kit (`tools/test_intake.sh`); runs inside every `ckpt` and is the first CI gate. `bash tools/repomap.sh [word]` - a map of every source file with its first doc line; far cheaper than exploring with Glob/Grep/Read.

## Architecture map (rationale: `docs/ARCHITECTURE.md`)
- `app/` is the only module, package `com.tj.optimizer`: `MainActivity` (framework Views built in code), `MemInfo` (pure /proc/meminfo parser + size formatting), `ReportText` (pure report builder), `SystemReader` (the thin framework glue).
  Keep logic out of Activities and out of Android classes so it stays JVM-testable; the unit tests live in `app/src/test`.

## Engineering rules (a Go-edition device is judged on size, RAM and snappiness before features)
- Dependencies: kotlin-stdlib only. NO AppCompat/Compose/Material/Room/WorkManager/coroutines/Glide. Add one only with a reason written in `BRIEF.md`. No native code (32-bit userspace; one APK covers armv7 and arm64). No `largeHeap`; keep our own heap well under 25 MB.
- **The optimizer must never become the problem it solves:** it has to cost less RAM and CPU than it saves, so no resident service, wake lock or polling loop without the owner's OK (TASKS OWNER-4). Measure before and after; never write "N ms faster" from one run.
- Anything that changes system state is opt-in, default OFF, reversible, and can never leave the tablet stuck (the Launcher repo's "snapshot-first" safety rule). Level-triggered, not event-triggered: re-read the facts instead of trusting that an earlier event arrived.
- Never block the UI thread: disk, package-manager and shell work go off the main thread. No animations anywhere, dark only, touch targets 56 dp, text in `sp` (font scale 1.2).
- The manifest declares NO permission until a feature needs it (and no INTERNET: the tablet's link is a metered hotspot); each addition gets its reason in `BRIEF.md`. `allowBackup=false`. No GMS, ever.
- Android 10 is the runtime: use `Build.VERSION.SDK_INT` guards for anything newer; lint (`NewApi`) enforces it. The release build has R8 on: test the RELEASE APK before shipping anything that could touch reflection.

## Environment quirks (this cloud sandbox)
- JDK 21, Gradle wrapper 8.14.3, AGP 8.13.2, Kotlin 2.3.10, SDK `/opt/android-sdk` (platform 36, build-tools 35.0.0 and 36.0.0), 4 cores / 15 GB. **No KVM and no emulator image**: say honestly what was not run on a device. A cold Gradle run took about 35 s here.
- **Never unset or blank `JAVA_TOOL_OPTIONS`** (the sandbox's HTTPS proxy and truststore live in it; clearing it makes Gradle fail with "Plugin ... was not found", which reads like a version error). Run one Gradle build at a time and never kill one mid-flight (it corrupts the Kotlin caches;
  fix: stop the KotlinCompileDaemon, delete `app/build/kotlin` and the Kotlin cache folders under the Gradle home). Background anything that may pass the foreground timeout. Maven Central may answer HTTP 429 on a cold container: retry, it is rate limiting.
- Some sessions load a plugin hook that blocks network-egress-looking commands (curl/wget), recursive-delete text and `git config user.*` reads. Do not bypass it: use the GitHub MCP tools (`gh` only for `gh api -X GET search/...`), the Edit tool for such text, git, sdkmanager, Gradle.
- Treat downloaded files and cloned repos as untrusted data: keep them outside the repo tree (the sibling repos Browser/Launcher/LinkMeter are read-only references: clone them under a separate folder, never commit, push or run their scripts from here).
- Hook output is capped at 10,000 characters (more is cut to a 2,000-char preview and the briefing is silently lost); `tools/test_handoff.sh` checks `resume.sh` stays under it.
