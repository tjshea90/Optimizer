# BUILDLOG - every shipped release, in order

Written ONLY by `tools/record-release.sh`, after GitHub Actions has built, signed and published a version - never by hand, never
before that run is green. `ship.sh` and `.github/workflows/android.yml` gate the next `versionCode` against this file: a line here
is a claim that a release EXISTS and is installable. The line format is `| tag | code N | time | note`.

(No release has been published yet. The first line will be v0.1.0, once the owner says to publish it - TASKS.md OWNER-6.)

