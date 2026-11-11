#!/usr/bin/env bash
# intake.sh - save the tablet survey (made by tools/adb/intake.sh on the owner's PC) into the project: redacted, with a FACTS.md digest.
#   bash tools/intake.sh <optimizer-report.txt>            a file the owner attached
#   bash tools/intake.sh - < pasted-report.txt             the report text itself
#   options: --date YYYY-MM-DD   --out DIR (default research/tablet)
# Afterwards: read research/tablet/LATEST.md (red flags first), then   git add research/tablet && bash tools/ckpt.sh "tablet survey" "what the red flags decide"
# The input is untrusted data, so Python runs isolated (-I): a file next to it can never be imported.
set -euo pipefail
D="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$D/.." || exit 1
exec python3 -I "$D/intake.py" "$@"
