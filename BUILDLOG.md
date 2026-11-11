# BUILDLOG - every shipped release, in order

Written ONLY by `tools/record-release.sh`, after GitHub Actions has built, signed and published a version - never by hand, never
before that run is green. `ship.sh` and `.github/workflows/android.yml` gate the next `versionCode` against this file: a line here
is a claim that a release EXISTS and is installable. The line format is `| tag | code N | time | note`.

(No release has been published yet. The first line will be v0.1.0, once the owner says to publish it - TASKS.md OWNER-6.)

| v0.1.0 | code 1 | 2026-11-11T22:13Z | First release (Actions run 43109636536): device snapshot screen with RAM, swap, heap limit, low-RAM flag, CPU type and a Copy report button; no optimizing features yet; not yet run on the tablet. APK sha256 ed2da046984912290281c475a39ae932ef9f70db942814ddedbfce46dbc73649
| v0.2.0 | code 2 | 2026-11-11T22:37Z | Baseline version (Actions run 43112944089): adds CPU, uptime, thermal status, swap/zram, memory-pressure counters, free storage, app count, read-only animation scales and a Storage speed test button; still changes nothing on the tablet; not yet run on it. APK sha256 a1425b4d0415bb966b5102f538178bd82c56d03b83ca031f917849fb252a34f9
