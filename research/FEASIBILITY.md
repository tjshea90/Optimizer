# What can a RAM / process optimizer actually do on this tablet?  (TASKS R1)

**STATUS (2026-11-11, updated 22:40Z): partly evidence-based.** The first draft was unverified guesses from general Android 10 knowledge. It has since been tested against the owner's answers and the Launcher/Browser repos (`research/BOUNDARIES.md`).
`[ev]` = backed by evidence in those repos (cited); `[?]` = still a hypothesis that must be confirmed on the tablet or in the Android 10 source (`android-10.0.0_r47`) before it drives a design decision.
Owner's answers (2026-11-11 22:08Z): a PC + USB cable and adb ARE available; the goal is a snappy tablet (fast app loading, fast Browser, no animations) with nothing broken and no interference with the custom Launcher; an accessibility service is acceptable only if it breaks or interferes with nothing.

## 1. Privilege ladder. Locked bootloader and secure boot: there is no root.
| Level | What the owner does | What is possible | Verdict |
| :- | :- | :- | :- |
| 0 | nothing | Read `ActivityManager.getMemoryInfo`, `isLowRamDevice`, `memoryClass`, `/proc/meminfo`, `/proc/swaps`, `/proc/vmstat`, `/proc/cpuinfo`, storage, thermal status, the animation scales (read-only). v0.2.0 does this. `[?]` other apps' processes are hidden from an ordinary app. | **USE** (read-only, on demand) |
| 1 | install | `KILL_BACKGROUND_PROCESSES` / `killBackgroundProcesses(pkg)`: asks the system to kill a package's cached processes, as lmkd would. | **DON'T**: the Launcher already clears Recents and force-stops (BOUNDARIES 0.2), killing cached apps forces slow cold starts (section 4) |
| 2 | taps in Settings | Usage access; an accessibility service; a notification listener. | Accessibility: **DON'T** (BOUNDARIES 0.3: the Launcher's service already handles tens of thousands of events and owns overlays; "no interference" cannot be shown). Notification listener: impossible on this Go ROM `[ev]` Launcher TASKS "STEP 17 RESULT". Usage access: only if a measurement needs it |
| 3 | one-time `pm grant` / `appops` from the PC | Needs the permission declared in OUR manifest first (`ADB_COMMANDS.txt:8`). `WRITE_SECURE_SETTINGS` would let us write `Settings.Global`, but every useful key already has an owner (animation scales, battery saver, tiles...). `[?]` `activity_manager_constants` (`max_cached_processes`...) is untested and would interact with DuraSpeed. | **NOT NOW**: no feature needs it |
| 4a | **a PC adb shell session** (the owner runs a script, then unplugs) | The shell user can run `cmd package compile`, `cmd appops set`, `pm disable-user`, `dumpsys *`, `sm fstrim`, `settings get/put`, `am start -W` directly, no helper involved. Changes persist across reboots (compile state until the app is updated). | **THE MAIN ROUTE**: reversible, no resident cost, nothing runs when the owner is not looking |
| 4b | a persistent on-device shell helper | `[ev]` dead end on this tablet: TCP sockets give `EPERM` even with INTERNET declared (HLP2), the Launcher's unix-socket helper is silently killed within 15-27 minutes with the cable still in, with or without DuraSpeed (HLP1, open), nothing survives a reboot, and the Launcher's helper has a fixed command table that cannot be extended. | **DON'T** build a second one |

## 2. Candidate levers, re-ranked by evidence (every gain is unmeasured until section 3 data exists)
1. **Find out what is resident and what it costs** (4a, read-only): per-process PSS, OOM levels, procstats, thrash counters, zram, compile state, DuraSpeed state, storage and thermal. Without it everything below is a guess. -> the intake, TASKS P1.
2. **Ahead-of-time compile the apps the owner uses** (4a, `cmd package compile -m speed -f <pkg>`): `[ev]` the Launcher already does this for itself because the owner complained about "significant delay when loading either launcher" (`ShellProtocol.java:209-210`);
   sideloaded apps otherwise run half-optimised until the idle+charging dexopt, "likely overnight" (Browser `HW_SPEED.md` headline 8). Candidates: the Browser (only its Java side; libxul is native), Settings, the apps the owner names. Cost: storage; lost when the app is updated.
   A manual `cmd package bg-dexopt-job` while charging runs the system's own job on demand `[?]`.
3. **DuraSpeed A/B** (`setting.duraspeed.enabled` is 1 in global AND system `[ev]`): the ROM's own background manager; measure on vs off. Reversible (BOUNDARIES section 3).
4. **Park unused preinstalled apps** (4a, `pm disable-user --user 0`, undo `pm enable`): only from the tablet's real package list, only named apps the owner approves, never `com.mediatek.*`/system/providers (the Launcher's own deny-list is the reference), one batch at a time. `[?]` gain.
5. **Per-app background limits** for named third-party apps (`cmd appops ... RUN_ANY_IN_BACKGROUND ignore`): may break that app's notifications; owner chooses.
6. **Free storage** (old installers, caches of named apps, `sm fstrim`): installs need about 1.4-1.5 GB free (Browser `UPDATE_STORAGE.md`) and a nearly full flash slows everything.
7. Developer options "Background process limit": owner-side experiment `[?]`.
8. **Animations: not Optimizer's.** `[ev]` the Launcher owns the three scales and the owner wants them on in the custom launcher and lock screen. If the owner now wants them off everywhere, that is a change of `AnimationPolicy.why` in the Launcher repo.
9. Anything that kills processes in a loop: see section 4. **No.**

## 3. What would count as success (TASKS G4; decide numbers with the owner)
Not "MB free". Cold-start time of the apps the owner uses (`am start -W`, 5 runs each; never the Launcher or SystemUI, the Browser only with no download running), time back to an app after switching, scroll jank (`dumpsys gfxinfo <pkg> framestats`), memory-pressure counters per hour of uptime
(major faults, swap in/out, refaults), and the owner's own "does it feel snappy". A change ships only with a before/after on the tablet, several runs each: the Launcher repo's lesson is that emulator noise can exceed the effect and one pair of runs proves nothing.

## 4. Why "free RAM" is the wrong goal
Android keeps cached processes on purpose: a cached app costs RAM the kernel would reclaim anyway, and relaunching a killed app costs CPU and storage reads (a cold start), which on four Cortex-A53 cores with eMMC is slow. The Browser's own research says the same for its engine:
"using the RAM fully" means keeping OTHER processes' private memory low so libxul's code and the HTTP cache stay in the page cache, because lmkd measures refaults and thrash is jank (`HW_SPEED.md` headline 6), and lists `System.gc()`, "memory boosters", wake locks and pre-reading files as traps (section d).
Repeatedly killing cached processes therefore usually makes the device slower; lmkd already kills when memory is genuinely short.

## 5. Where to verify next
- Android 10 source `android-10.0.0_r47`: `ActivityManagerConstants.java` (keys, whether changes apply live), `ActivityManager.java`, the `cmd package` and `cmd appops` shell commands, and MediaTek DuraSpeed (`com.mediatek.duraspeed`, not in AOSP: look at the tablet's own package and settings).
- The tablet itself, via the PC intake (TASKS P1) and the in-app report (v0.2.0): section 5 of BOUNDARIES.md lists what is missing.
