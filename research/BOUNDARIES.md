# Boundaries: what the Launcher, the Browser and the ROM already own, and so what Optimizer may never do  (TASKS G2 / R2)

Written 2026-11-11 from a survey of the owner's **Launcher** and **Browser** repos (read-only clones, state of about 22:00Z) by two research agents. `[checked]` = I re-read the cited source myself; `[agent]` = the agent's report, cite
the file:line it gives and re-check before relying on it. Owner rule, 2026-11-05 (Launcher `research/SAFETY_2026-11-05.md:3`): "doesn't break anything or require me to factory reset my tablet" **overrides every feature**. When this file and
an Optimizer idea disagree, this file wins; to change a rule, change it in the owning repo first.

## 0. The rules these facts add up to
1. **Optimizer never writes a system setting, appop, package state or process state that the Launcher or the Browser owns** (sections 1-2).
2. **Optimizer does no killing of any kind**: no force-stop, no kill-background-processes, no task or Recents editing. The Launcher already does all three on its own schedule, the Browser already ends its whole process tree when idle, and the ROM
   has its own background manager (section 3). A fourth killer can only fight them (and cached apps are not a cost: `research/FEASIBILITY.md` section 4).
3. **Optimizer runs no resident process**: no foreground service, wake lock, polling loop, accessibility service or shell helper. The Launcher's accessibility service already sees tens of thousands of events (45,521 in one 2-hour capture, 38,643 dropped as noise),
   a second service adds load and can interfere with its overlays; a second shell helper would die like the Launcher's (HLP1) and TCP sockets are refused on this ROM (HLP2). The owner allowed an accessibility service only if it breaks or interferes with nothing: it cannot be shown to meet that, so the answer is no.
4. **Anything that changes the system is a one-time, reversible step run by the owner from the PC with adb**, from a written list the owner approved, each with an undo command and a before/after measurement. It must still be correct after a reboot and after the Launcher/Browser update.
5. **Measure before tuning.** The data that decides everything is still missing (section 5).

## 1. What the Launcher owns: Optimizer MUST NOT TOUCH
| What | Why / evidence |
| :- | :- |
| `Settings.Global` `window_animation_scale`, `transition_animation_scale`, `animator_duration_scale` | Held at 0 by `AnimationSpeedup` during the return reset and the whole stock-Quickstep session; real animations REQUIRED on the lock screen and in the custom launcher. Owner, 2026-11-07 23:36Z: "Animations should only ever be turned off inside quickstep launcher" `[checked]` `research/ANIMATIONS_2026-11-08.md:3`; `AnimationPolicy.java:10-17` `[agent]`. **"No animations" for the whole system is therefore the Launcher's decision, not Optimizer's** (Browser and Optimizer have no animations in their own UI already). |
| `low_power`, `low_power_sticky` (battery saver) | Written to 0 only while custom/lock screen is active, observed by `BatterySaverGuard`. Owner 2026-11-06: battery saver stays on, "just turn off in custom launcher". `[agent]` `BatterySaverPolicy.java:20-22`, `BatterySaverGuard.java:34,52-56,95` |
| `sysui_qs_tiles`, `policy_control`, `default_input_method`, `icon_blacklist`, `heads_up_notifications_enabled`, `lock_screen_show_notifications`, `lock_screen_allow_private_notifications`, `device_demo_mode` | Snapshot-first writes of the Launcher's opt-in profiles `[agent]` (TileProfile, ImmersiveProfile, KeyboardSwitch, RadioIconHold, NotifQuietPolicy, LockQuiet). `enabled_accessibility_services` is merged, never replaced (`ShellServer.java:98-105`) |
| Default Home, every HOME filter, `KEYCODE_HOME` presses | The Launcher's `RouterActivity` is the only HOME activity; `StrayHomeGuard` presses Home when the stock home appears. A second HOME candidate raises the "Choose a Home" dialog `[agent]` (`AndroidManifest.xml:231-246`, `ADB_COMMANDS.txt:58-61`) |
| Force-stop, task removal, "Clear all", Recents edits | `ReturnReset` force-stops stock-session apps, `RecentsSweep` (default ON) removes every Recents task not in the custom drawer and clears all at custom start, the service presses "Clear all"; helper jobs FORCESTOP and RMTASKS. Never `com.tj.launcher`, `com.android.launcher3`, SystemUI, keyboards `[agent]` |
| The shell helper `ShellServer`, socket `tj_shell_helper`, ports 47631-47635, `pkill app_process` | Fixed command table, not extensible `[checked]` `ShellProtocol.java:197-217`; its hard deny-list refuses system, providers, GMS, `com.mediatek.*`, bluetooth, nfc, keychain, shell, permissioncontroller, keyboards, itself and the stock launcher `[checked]` `ShellProtocol.java:28-34` |
| The Launcher's own grants and appops: `WRITE_SECURE_SETTINGS`, `DUMP`, `CHANGE_CONFIGURATION`, `READ_EXTERNAL_STORAGE`, `PACKAGE_USAGE_STATS`, `GET_USAGE_STATS`/`WRITE_SETTINGS`/`RUN_IN_BACKGROUND`/`RUN_ANY_IN_BACKGROUND` allowed, `deviceidle whitelist +com.tj.launcher` | `[checked]` `ShellProtocol.java:197-210` and `AndroidManifest.xml:16-44`. Do not revoke, "restrict" or de-whitelist `com.tj.launcher` |
| `com.android.browser` (the stock browser) enable state | The Launcher's opt-in BROWSER_OFF/BROWSER_ON (`pm disable-user --user 0` / `pm enable`) `[checked]` `ShellProtocol.java:211-212` |
| The Launcher's compile state | It already runs `cmd package compile -m speed -f com.tj.launcher` for itself (owner: "significant delay when loading either launcher") `[checked]` `ShellProtocol.java:209-210`. It is the proof that ahead-of-time compiling is a lever that works for this owner; Optimizer extends it to OTHER apps and never recompiles the Launcher |

Failsafes to respect: FailsafeKeys (lock handshakes, Konami, Solitaire hotspot, triple Home, Safe mode), the crash breaker (3 crashes in 2 minutes = back to Quickstep) `[agent]`. The Launcher's design rules: opt-in, default OFF, snapshot first, read back, "Restore everything"; no device admin, keyguard APIs, `SYSTEM_ALERT_WINDOW`, lock settings, boot receiver or wake lock (`DESIGN_2026-11-02.md:56-60`, `SAFETY_2026-11-05.md:16`) `[agent]`.

## 2. What the Browser needs from the system: Optimizer MUST NOT DO TO IT
- Do not kill or force-stop it or any `<package>:` process while it is open or downloading. It already exits by itself: `ProcessLife.exitIfIdle()` ends every sibling process and itself when no Activity, download or updater needs it `[checked]` `ProcessLife.kt:37-55`.
- Do not stop `DownloadService`, release its wake/Wi-Fi locks or restrict its background use. It is a foreground service (dataSync, `START_STICKY`) that holds locks only while bytes move `[checked]` `DownloadService.kt:65,71,80`. Downloads, the update APK and stream staging share the main process with Gecko.
- Do not clear its caches: the 512 MB disk cache saves hotspot bytes (`HW_SPEED.md:100`); do not touch `:private`, `files/streams` or `files/updates` (resume state) `[agent]`.
- Do not change font scale, density or dark mode while it is open: its `configChanges` omits uiMode/fontScale/density, so such a change recreates the Activity and closes sessions `[checked]` `geckoapp/src/main/AndroidManifest.xml:52`; `FREEZE.md:47` `[agent]`.
- Do not revoke install-unknown-apps or notifications, reset Wi-Fi or change DNS (it has its own DoH), and do not run a resident service or wake lock of Optimizer's own (the Browser's own research calls that a trap: `HW_SPEED.md:112`).
- What helps it from OUTSIDE (the Browser's own research, `HW_SPEED.md` sections d-e): free storage for updates (installs need 10% of the partition + 2 x APK + libraries + about 32 MiB free, about 1.4-1.5 GB here); keep OTHER apps' private memory low so libxul's 118 MB of code and the HTTP cache stay in the kernel page cache (lmkd measures refaults; thrash is jank); thermal status; owner-side animation scale 0 or 0.5x.
- The Browser's own list of tablet measurements nobody has taken: GL renderer, cpuset/governor, storage speed, cold start, memory pressure (`HW_SPEED.md` section e). Optimizer's intake collects the system-side ones (section 5).

## 3. The ROM's own process manager: MediaTek DuraSpeed
`setting.duraspeed.enabled` is 1 in both `Settings.Global` and `Settings.System` on the tablet (Launcher TASKS 472, 2026-11-05): **the ROM already has a background-app manager switched on**, and nobody has measured whether it helps or hurts speed, downloads, notifications or the owner's apps.
The Launcher team tested it once as the suspect for its dying helper: with DuraSpeed OFF the helper still died within 27 minutes (TASKS 481), so it is not the only killer (HLP1 is still open), and they restored it to 1 (TASKS 486). The capture also lists `background_power_saving_enable` and `default_restrict_background_data`.
**This is the most relevant "process optimization" fact in either repo.** The experiment that matters is a measured A/B of DuraSpeed on vs off (cold-start times, memory, behaviour of downloads and notifications), reversible with one command per setting; it is a proposal for the owner to approve, never something to flip silently.

## 4. What Optimizer MAY do
1. **Read** anything an ordinary app or the owner's adb shell can read, off the UI thread, on demand (the report, the storage test, the PC intake): no grants needed, no state changed.
2. **Ahead-of-time compile other apps** from the PC (`cmd package compile -m speed -f <pkg>` or the system's own `bg-dexopt-job`), one-time, undo `cmd package compile --reset <pkg>`; never the Launcher; needs re-running after an app's update replaces its compiled code. Gain unmeasured, storage cost to be measured.
3. **Per-app background limits** for named third-party apps the owner chooses (`cmd appops set <pkg> RUN_ANY_IN_BACKGROUND ignore`, undo `allow`): never the Launcher, the Browser (downloads), system packages or `com.mediatek.*`; may stop that app's notifications.
4. **Storage hygiene** the owner approves (old installers, caches of named apps, `sm fstrim`): low risk, and free space is a precondition for installs.
5. **DuraSpeed A/B** (section 3) and **Developer-options "Background process limit"** as owner-run experiments with numbers.
6. Anything new that needs a permission: the manifest must declare it BEFORE the owner's `pm grant` (`ADB_COMMANDS.txt:8`), `BRIEF.md` gets the reason, and the owner's cable recipe is extended in one place.

## 5. Missing measurements (the intake must collect them; none exists yet)
Per-process PSS and OOM levels (`dumpsys meminfo`, `dumpsys activity lru`, `dumpsys procstats`), cached-process count, `/proc/meminfo`, `/proc/swaps` and zram state, `/proc/vmstat` thrash counters, pressure files (`/proc/pressure/*` if the kernel has PSI),
lmk/zram/dalvik/pm.dexopt properties, per-package compile state (`dumpsys package dexopt`), storage free/speed, CPU frequency/governor and thermal status, DuraSpeed state, which packages are disabled, and the cold-start times of the owner's main apps.
The Launcher's captures lack all of it because its helper was not running (`research/tablet/LATEST.md`); a direct adb shell from the PC does not need the helper.
