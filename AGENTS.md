# AGENTS.md

This repo is worked on mainly by Claude Code sessions that can be cut off at any moment (usage caps) and resumed by another session or account. Any other coding agent follows the same protocol:

1. Read `HANDOFF.md` (one page, no prior context needed), then `CLAUDE.md` (the working agreement), then `CHECKPOINT.md` ("Do this next" is authoritative) and the open `[ ]` boxes of `TASKS.md`.
2. Run `bash tools/resume.sh --text` for the live briefing (sync state, interrupted-session warnings, other sessions' branches).
3. After every completed step run `bash tools/ckpt.sh "what I did" "exactly what comes next"`. Work is only safe once pushed. Develop only on your assigned branch; never force-push; never push `main`.
4. Never commit keys, photos or personal data (the repo is public). Never add a `push:` trigger to a workflow. GitHub Actions builds every APK; do not commit APKs.
5. Stack: Kotlin, framework Views, no libraries, Android 10 (API 29) target device, minSdk 29. Tests: `./gradlew :app:testDebugUnitTest`; tooling self-test: `bash tools/test_handoff.sh`.
