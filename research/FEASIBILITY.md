# What can an RAM / process optimizer actually do on this tablet?  (TASKS R1)

**STATUS: UNVERIFIED STARTING HYPOTHESES.** Written 2026-11-11 by the foundation session from general Android 10 knowledge, NOT checked against the source or the tablet.
Every `[?]` below must be confirmed (Android 10 source `android-10.0.0_r47`, which the Launcher repo also used, or an experiment on the tablet) before it drives a design decision.
When a hypothesis is confirmed or killed, edit it here with the evidence and the date. Read the Launcher repo's `HANDOFF.md`/`CLAUDE.md` first: it already learned a lot about what this tablet allows (adb grants, an accessibility service,
a shell helper that dies after minutes, no TCP sockets without INTERNET). Do not re-derive that.

## 1. Privilege ladder (cheapest first). Locked bootloader: there is no level 5 (root).
| Level | What the owner does | What an app could do there | Cost / risk |
| :- | :- | :- | :- |
| 0 | nothing | Read `ActivityManager.getMemoryInfo`, `isLowRamDevice`, `memoryClass`, `/proc/meminfo` (the snapshot screen already does). `[?]` list only its OWN processes (other apps' are hidden from ordinary apps since Android 5.1/7). | free |
| 1 | install | Normal permissions only. `KILL_BACKGROUND_PROCESSES`: `killBackgroundProcesses(pkg)` asks the system to kill a package's cached/background processes, like the low-memory killer would. `[?]` it cannot stop foreground services or visible apps. | Usually backfires, see section 4 |
| 2 | taps in Settings, no cable | Usage access (`PACKAGE_USAGE_STATS`): which apps ran when, standby buckets `[?]`. Accessibility service: can drive other apps' UI, e.g. Settings > App info > Force stop (fragile, slow, resident). Notification listener: the Launcher repo found it impossible on this Go ROM. | Accessibility is a resident service = RAM (OWNER-4) |
| 3 | one-time `adb shell pm grant` / `appops set` from a PC | `WRITE_SECURE_SETTINGS` (the Launcher already holds it for animation scales): write `Settings.Global` keys. `[?]` `activity_manager_constants` (for example `max_cached_processes`) is observed live by the activity manager on Android 10 - a direct lever on how many background processes survive. `DUMP`/`PACKAGE_USAGE_STATS`/`READ_LOGS` for reading `dumpsys`-style data `[?]` (hidden-API limits apply). | One cable session; settings writes must be reversible and never leave the tablet stuck |
| 4 | a persistent adb shell helper (`adb tcpip`, then the app talks to localhost as an adb client) | Run shell-uid commands: `am force-stop`, `pm disable-user --user 0 <pkg>` / `pm enable` (park preinstalled bloat, reversible), `cmd package compile -m speed-profile -a` (AOT-compile apps for faster starts), `appops set <pkg> RUN_ANY_IN_BACKGROUND ignore` (stop background running), `dumpsys meminfo` (per-process PSS), `am set-standby-bucket`. | The Launcher saw this helper die within minutes, and `service.adb.tcp.port` is empty on the tablet; nothing survives a reboot. Loopback sockets need the INTERNET permission. `[?]` all of it |

## 2. Candidate levers, ordered by expected value for "best speed" (all `[?]`, none measured)
1. **Find out what is resident and what it costs.** `dumpsys meminfo` and `dumpsys activity processes` (level 4, or the owner pastes them once from a PC). Without this every other idea is guessing.
2. **Park preinstalled apps that run at boot and are never used** (`pm disable-user --user 0`, fully reversible with `pm enable`). On a 2 GB Go device this is the usual biggest sustained win. The app can also just print the exact adb command list for the owner to run once (no resident helper).
3. **Restrict background running of heavy apps** (`appops ... RUN_ANY_IN_BACKGROUND ignore`, or Settings > Apps > Battery, which the owner can do by hand).
4. **Animation scales** to 0.5 or 0 (`WRITE_SECURE_SETTINGS`): the Launcher already does this inside Quickstep sessions; do not duplicate it.
5. **AOT compile the apps the owner uses** (`cmd package compile`): faster launches at the price of storage. Needs shell privileges.
6. **Cached-process limit** (`activity_manager_constants`/`max_cached_processes`): could keep fewer background processes alive (more free RAM) or more of them (faster app switching). Pure experiment; measure both ways.
7. Anything that kills processes in a loop: see section 4.

## 3. What would count as success (decide with the owner, OWNER-3)
Not "MB free". Candidates: cold-start time of the apps they use (`am start -W` through a shell, or a stopwatch), time to first frame after switching apps, jank while scrolling the Browser, and the number of times per day an app is found killed.
A feature ships only with a before/after on the tablet, several runs each (the Launcher repo's lesson: emulator noise can exceed the effect; never write "N ms faster" from one pair of runs).

## 4. Why "free RAM" is the wrong goal
Android keeps cached processes on purpose: a cached app costs RAM the kernel would reclaim anyway, and relaunching a killed app costs CPU and storage reads (cold start), which on a 4x Cortex-A53 with eMMC is slow.
Repeatedly killing cached processes therefore usually makes the device SLOWER; lmkd already kills when memory is genuinely short. Keep an optimizer from becoming a battery- and RAM-hungry process of its own.

## 5. Where to verify (next session)
- Android 10 source, branch `android-10.0.0_r47`: `frameworks/base/services/core/java/com/android/server/am/ActivityManagerConstants.java` (which keys exist and whether the observer applies them live), `ActivityManager.java` (`killBackgroundProcesses`,
  `getRunningAppProcesses` restrictions), `ActivityManagerService.java` (`setProcessLimit` and its permission), `lmkd` docs for the low-RAM configuration.
- The Launcher repo's `research/` notes on this tablet (AOSP 10 findings, adb recipes) and Browser's `docs/HARDWARE.md`.
- On the tablet, via the owner: the snapshot screen's Copy report, and (if a cable is possible) `adb shell getprop | grep -E "lmk|zram|low_ram|dalvik"`, `adb shell dumpsys meminfo`, `adb shell settings get global activity_manager_constants`.
