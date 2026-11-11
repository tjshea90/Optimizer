# Architecture

## The app (`app/`, package `com.tj.optimizer`)
One Gradle module, Kotlin, framework Views only, no runtime dependencies beyond the Kotlin stdlib.

| File | Role | Android classes? |
| :- | :- | :- |
| `MemInfo.kt` | Parses `/proc/meminfo` text into a `MemInfo` (kB, -1 = unknown) and formats sizes (`formatKb`) | none (pure JVM, unit-tested) |
| `ReportText.kt` | `DeviceFacts` + `ReportText.build`: the plain-text snapshot the screen prints and the Copy button copies | none (pure JVM, unit-tested) |
| `SystemReader.kt` | Reads `ActivityManager`, `Build` and `/proc/meminfo` into `DeviceFacts`; needs no permission | yes (thin glue) |
| `Parsers.kt` | `CpuInfo`, `SwapDevice`, `VmStat`: parsers for /proc/cpuinfo, /proc/swaps, /proc/vmstat (an app may not be allowed to read the last two: the report then says why) | none (pure JVM, unit-tested) |
| `Format.kt` | Sizes, durations, thermal names, animation scales as text | none (pure JVM, unit-tested) |
| `StorageBench.kt` | The storage speed test: sequential write + fsync and small synced writes on one temporary file in the app's cache (reads are deliberately not timed: the page cache would make them measure RAM) | none (pure java.io, unit-tested) |
| `StorageModel.kt` | Storage verdict, top apps, row and header text for "What uses my storage" | none (pure JVM, unit-tested) |
| `StorageReader.kt` | Reads `StorageStatsManager` (totals; per-app and per-kind sizes need the Usage access permission) | yes (thin glue) |
| `StorageActivity.kt` | "What uses my storage": READ-ONLY list; a tap opens that app's own Android settings page; never deletes anything | yes |
| `MainActivity.kt` | Builds the screen in code (title, scrolling monospace report, Refresh, Copy report, Storage speed test, What uses my storage); refreshes in `onResume` | yes |

Rule: logic lives in pure classes with a JVM test in `app/src/test`; Activities and services only wire things up. Features that change system state are opt-in, reversible and measured (CLAUDE.md, Engineering rules).

## Build and signing
- Gradle 8.14.3 (wrapper), AGP 8.13.2, Kotlin 2.3.10, compileSdk 36, minSdk/targetSdk 29, bytecode target 11, R8 + resource shrinking on release, lint `abortOnError` on the release variant.
- `signing/dev/optimizer-dev.jks` (public dev key, alias and password `optimizer-dev`) signs the release build unless a gitignored `keystore.properties` names another key (CI writes one from the repo secrets when they exist).
  `signing/CERT_SHA256` pins the certificate; `tools/verify-apk.sh` and the workflow compare it with the APK that was actually produced. Details: `signing/README.md`.

## GitHub Actions (`.github/workflows/android.yml`)
`check` job (seconds): decide the plan, run `tools/test_handoff.sh`, check the signing key against the pin, check versionCode is not a downgrade. `build` job (minutes, only with `full_build`): unit tests + lint, `assembleRelease`,
`tools/verify-apk.sh`, then either `.github/scripts/publish-release.sh` (`publish` true: creates the Release and tag `v<versionName>` server-side, attaches `Optimizer-v<versionName>.apk` + sha256) or a 14-day workflow artifact.
Triggers: manual dispatch and `v[0-9]*` tags only; a `push:` branch trigger is forbidden because autosave pushes after every tool call (`tools/test_handoff.sh` enforces it). The workflow commits nothing.

## The save system (`tools/`, `.claude/settings.json`)
Hooks run the scripts: SessionStart -> `resume.sh` (briefing), UserPromptSubmit -> `capture_inbox.sh`, PostToolUse -> `autosave.sh` (commit + push) and `posttool.sh`, Stop -> `autosave.sh` (+ `capture_outbox.sh`), PreCompact -> `toobig.sh`.
By hand: `ckpt.sh "did" "next"` (CHECKPOINT.md + commit + push + sync), `claim.sh`, `ship.sh`, `record-release.sh`. Many sessions: `sync.sh` merges other branches; each session keeps its own card in `.checkpoints/`.
`tools/test_handoff.sh` tests all of it against a throwaway repository with a fake remote. The files a cold session reads, in order: HANDOFF.md, CLAUDE.md, CHECKPOINT.md, TASKS.md, BRIEF.md.
