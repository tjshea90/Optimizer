# BRIEF - platform decisions, owner facts, and what is still open

Written 2026-11-11 by the foundation session. The save system follows the owner's other repos: `CHECKPOINT.md` = where the last session stopped, `TASKS.md` = the job list,
this file = decisions that do not change and the questions that are still open. Design rationale: `docs/ARCHITECTURE.md`.

## What this is
An Android app, sideloaded (never on Play), to optimize RAM and processes on the owner's ONE tablet for the best speed possible. The request (2026-11-11, verbatim in INBOX.md):
"This repo will be an app to optimize ram and processes on my Android 10 go armv7 mt8168 tablet for the best speed possible". **There is no feature list yet.**

## The device (`docs/HARDWARE.md`, raw file `docs/hardware/hardware_.txt`)
MediaTek MT8168B "Alps 711" (`tb8168p1_bsp`), Mali-G52 MC2, **Android 10 Go (API 29)**, security patch 2021-07-05, kernel 4.14.141, **2 GB RAM, `low_ram=true`, heap class 128 MB**, 600x973 @170 dpi (font scale 1.2),
32-bit userspace (`sys_mssi_t_32_ago`), unencrypted, single user. **Locked bootloader, secure boot, verified boot green: no root and no custom ROM.**
**No Google Play Store, no Google services**: manual APK installs, never a GMS dependency. Internet is only a phone hotspot (metered): the app needs no network until a feature proves otherwise.

## Owner facts (authoritative)
- GitHub Actions builds and releases every APK: "GitHub actions should do all the app compiling and apk making. Claude codes the app, GitHub makes the apk."
- Their other repos target the same tablet: **Browser** (Kestrel, a light browser), **Launcher** (an Android Go launcher that works with adb-granted permissions and an accessibility service), **LinkMeter** (phone/tablet hotspot control).
  What those sessions learned about what this tablet allows is the first source for TASKS R1/R2: do not re-derive it.
- Prefers longer sessions and more usage if it makes a better product or fixes bugs, but wants usage spent efficiently. Wants progress saved to GitHub frequently and a new session able to resume cold with nothing lost.
- Their apps are written for Android 10 here but should still run on Android 16.

## Decided (foundation session, 2026-11-11)
| Item | Decision | Why / how to revisit |
| :- | :- | :- |
| Platform | `minSdk 29`, `targetSdk 29`, `compileSdk 36` | Target = the real device's runtime, so no newer-Android behaviour is assumed (the Launcher repo's reasoning). A targetSdk-29 app also sees every installed package on Android 11+ (no package-visibility filtering, no `QUERY_ALL_PACKAGES`), which a process/app manager would otherwise need. Lint's Play-only `ExpiredTargetSdkVersion` check is disabled for that reason. Browser chose targetSdk 36; revisit only if a feature needs newer-only APIs. |
| Language / UI | Kotlin; framework Views built in code, no UI library | Go devices are RAM-tight, UI weight matters. Compose, AppCompat and Material are out. |
| Dependencies | kotlin-stdlib only (JUnit for tests) | Add one only with a reason written here: every dependency costs APK size, dex methods and RAM. |
| ABI | No native code | One APK covers armv7 and arm64. The snapshot screen prints the device's ABIs: re-verify on the tablet. |
| Release build | R8 minify + resource shrink on | Small APK for Go. Add keep rules in `app/proguard-rules.pro` if reflection ever appears, and test the RELEASE APK. |
| Toolchain (pinned, proven in Browser and Launcher) | AGP 8.13.2, Kotlin 2.3.10, Gradle 8.14.3, JDK 21 (bytecode target 11) | Do not "upgrade" because lint or a dependency suggests it; lint's version checks are off on purpose. |
| Building | GitHub Actions `android.yml`, tags + manual dispatch only | Minutes are scarce; this container only runs tests, lint and debug/release checks. |
| Signing | PUBLIC dev key `signing/dev/optimizer-dev.jks`, certificate pinned in `signing/CERT_SHA256`; a private key from the repo secrets wins if the owner adds one | Same arrangement the owner chose for Launcher and Browser. Never regenerate once anything is installed. `signing/README.md`. |
| Identity | `applicationId` and namespace `com.tj.optimizer`, label "Optimizer" | Placeholders until OWNER-1. The id can never change after the first install. |
| Permissions | `PACKAGE_USAGE_STATS` only (since v0.3.0) | For "What uses my storage": a special permission the owner switches on once in Settings > Usage access; it lets `StorageStatsManager` report per-app and per-kind SIZES (lint's ProtectedPermissions is acknowledged with `tools:ignore`). Optimizer reads sizes only, never usage events, and never deletes anything. Each further addition needs a reason written here. No INTERNET, no GMS. |
| Branches | work on the assigned branch; `tools/MIRROR_MAIN` = `off` | Landing on `main` is the owner's call. |
| Save system | the Browser repo's "save ladder", single-app version | `CLAUDE.md`. |
| First screen | "device snapshot": RAM, swap, heap class, low-RAM flag, ABIs, with a Copy report button | A seed that yields real numbers for `docs/HARDWARE.md` and proves the signing chain on the tablet. It is NOT a feature decision. |

## OPEN - ask the owner (do not assume)
1. App name and package (OWNER-1).
2. Whether a PC with a USB cable and `adb` is available for one-time permission grants (OWNER-2). It decides whether levels 3-4 of `research/FEASIBILITY.md` are reachable. The Launcher repo asked the same question: read its `HANDOFF.md` for the answer before asking again.
3. What "best speed" means: app launch, UI smoothness, the Browser, sustained use after hours of uptime? Which apps are the heavy ones? (OWNER-3)
4. Accessibility service acceptable, or strictly on demand? (OWNER-4) Default: on demand, because a resident service costs RAM on 2 GB.
5. Private signing key and main branch (OWNER-5); when to publish the first release (OWNER-6).

## Known issues / risks
- The skeleton has been compiled, unit-tested, linted and R8-built in the cloud sandbox only. It has NOT been run on a device or emulator (no KVM here): whether it installs and draws correctly on the tablet is unverified (TASKS R3 asks the owner to install it and paste the Copy report).
- "RAM optimizer" apps are notorious for making devices slower: killing cached processes forces cold starts, and free RAM is not the goal, responsiveness is. Every feature needs a before/after measurement on the tablet (`research/FEASIBILITY.md` section 4).
- The public dev key means anyone can sign an APK that installs over this app. The owner accepted that for Launcher and Browser (`signing/README.md`).
