# BUILDLOG - every shipped release, in order

Written ONLY by `tools/record-release.sh`, after GitHub Actions has built, signed and published a version - never by hand, never
before that run is green. `ship.sh` and `.github/workflows/android.yml` gate the next `versionCode` against this file: a line here
is a claim that a release EXISTS and is installable. The line format is `| tag | code N | time | note`.

(No release has been published yet. The first line will be v0.1.0, once the owner says to publish it - TASKS.md OWNER-6.)

| v0.1.0 | code 1 | 2026-11-11T22:13Z | First release (Actions run 43109636536): device snapshot screen with RAM, swap, heap limit, low-RAM flag, CPU type and a Copy report button; no optimizing features yet; not yet run on the tablet. APK sha256 ed2da046984912290281c475a39ae932ef9f70db942814ddedbfce46dbc73649
| v0.2.0 | code 2 | 2026-11-11T22:37Z | Baseline version (Actions run 43112944089): adds CPU, uptime, thermal status, swap/zram, memory-pressure counters, free storage, app count, read-only animation scales and a Storage speed test button; still changes nothing on the tablet; not yet run on it. APK sha256 a1425b4d0415bb966b5102f538178bd82c56d03b83ca031f917849fb252a34f9
| v0.3.0 | code 3 | 2026-11-11T23:02Z | Storage version (Actions run 43116255076): What uses my storage screen (per-app sizes; needs Usage access switched on once), the report flags a nearly full storage and says why some lines are n/a; reads only, deletes nothing; not yet run on the tablet. APK sha256 c0747aabe49e93bf9429e335e1baf9e2800123c1c0749910f4eeb68be7ba9ff5
| v0.3.1 | code 4 | 2026-11-11T23:06Z | v0.3.0's What uses my storage plus a Copy list button on that screen (Actions run 43117009887); supersedes v0.3.0; reads only, deletes nothing; not yet run on the tablet. APK sha256 ec375419fbbb32cf0c549ffe5d758601f917a1093b9d008fc565c506d4a3c1b1
