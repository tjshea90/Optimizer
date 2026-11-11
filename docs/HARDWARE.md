# Target hardware and what it forces on the design

Source: `docs/hardware/hardware_.txt` (supplied by the owner for the Browser repo; the Android device id line is deliberately left out here) plus the owner's statements, repeated in their
Browser and Launcher repos, that the tablet has **2 GB of RAM**, **no Google Play Store and no Google services**, and reaches the internet **only through a phone hotspot**.

| Fact | Derived constraint for an optimizer |
|---|---|
| MediaTek MT8168B (public specs: quad Cortex-A53, in-order), Mali-G52 MC2 | Slow single-thread performance: anything the optimizer does must be tiny on the CPU; a polling loop is a cost, not a free feature. |
| Android 10 **Go** (API 29), "modified" build, security patch 2021-07-05, kernel 4.14.141 | `minSdk 29`; newer APIs behind `Build.VERSION.SDK_INT` guards (the APK should still run on Android 16). Go builds run an aggressive low-memory killer; whether this ROM also lowers the cached-process limit is unverified (research/FEASIBILITY.md section 2, item 6). |
| **2 GB RAM** (reports about 1.9 GB), `ro.config.low_ram=true`, memory class **128 MB** | Per-app heap cap 128 MB; no `largeHeap`; keep our own heap well under 25 MB. Free RAM is not the goal; responsiveness is (research/FEASIBILITY.md section 4). |
| `sys_mssi_t_32_ago` system image | 32-bit userspace (armeabi-v7a). Ship **no native code** so the ABI does not matter. |
| `ro.boot.flash.locked=1`, secure boot, verified boot green | **No root, no custom ROM, no flashing.** Everything must work from an ordinary app, plus whatever one-time `adb shell pm grant` permissions the owner can give with a cable (TASKS OWNER-2). |
| `ro.crypto.state=unencrypted` | App data at rest is unencrypted: persist as little as possible; `allowBackup=false`. |
| 600x973 @170 dpi, font scale 1.2 | About 564x916 dp portrait tablet (below `sw600dp`), mdpi resource bucket: vector icons, `sp` text, 48+ dp targets (56 dp used), honour the font scale. |
| `getMaxSupportedUsers=1` | No multi-user handling. |
| Stock launcher Quickstep (Android 10) | Gesture navigation may be on: avoid edge-swipe gestures in our own UI. The owner's Launcher repo can replace it. |
| **No Google Play Store / Google services** (owner) | Never depend on GMS APIs; manual APK updates from GitHub Releases. |
| **Internet only via phone hotspot** (owner) | Metered and flaky: the app needs no network. No INTERNET permission until a feature proves it must. |

## Measured on the tablet (2026-11-11, Optimizer v0.2.0 Copy report; details and meaning: `research/BASELINE_2026-11-11.md`)
RAM 1.91 GB total (691 MB available at the time, swap 621 MB used of 1.72 GB), 4 x Cortex-A53 with crypto extensions (`aes pmull sha1 sha2 crc32`), heap 128 MB (large 256 MB), lowmem threshold 144 MB, thermal NONE,
**data partition 27.75 GB with only 996 MB (3%) free**, flash write 31.3 MB/s sequential + fsync, 4 KB synced writes 1.74 ms mean / 5.93 ms 95th percentile, uptime 6 days, 182 packages (138 system, 44 added),
animation scales held at 0.0 by the Launcher during that session. Apps cannot read `/proc/vmstat` or `/proc/swaps` on this ROM.

## Still unknown (needs the PC survey, `tools/adb/README.md`)
Whether the swap is zram or flash, the memory-pressure counters, which processes are resident and what each costs, the compile state of the apps, what fills the storage, DuraSpeed's effect, storage read speed, GPU strings, cpu governor and frequencies. See `research/BOUNDARIES.md` section 5.
