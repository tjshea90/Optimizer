#!/usr/bin/env bash
# repomap.sh - a map of the repo for a session with NO context: every source file with its size and the first line of its own doc comment.
# Prints to stdout only (nothing is committed, so it is never stale and never causes merge conflicts between sessions).
#   bash tools/repomap.sh            the whole map (about 150 lines)
#   bash tools/repomap.sh memory     only paths containing the word
# Costs far less usage than exploring with Glob/Grep/Read: run it first, then read only the files it points at.
set -uo pipefail
D="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"; cd "$D" || exit 1
FILTER="${1:-}" python3 -I - <<'PY'
import os, re, subprocess
flt = os.environ.get("FILTER", "")
files = subprocess.run(["git", "ls-files"], capture_output=True, text=True).stdout.split("\n")
roots = [
    ("app/src/main/kotlin", "app/ Android code (package com.tj.optimizer): UI, memory/process readers, policies"),
    ("app/src/main/java", "app/ Android code (Java)"),
    ("app/src/main/res", "app/ resources"),
    ("app/src/test", "app JVM unit tests (pure logic, no emulator)"),
    ("app/src/androidTest", "app instrumented tests"),
]
def describe(path):
    try:
        text = open(path, encoding="utf-8", errors="replace").read(6000)
    except Exception:
        return ""
    m = re.search(r"/\*\*\s*(.+?)(?:\n|\*/)", text, re.S)
    if m:
        d = re.sub(r"\s*\*\s*", " ", m.group(1)).strip(" */")
        if d: return d
    for ln in text.split("\n")[:40]:
        s = ln.strip()
        if s.startswith("//") and len(s) > 6 and not s.startswith("// shellcheck"):
            return s.lstrip("/ ").strip()
        if s.startswith("/*") and len(s) > 6:
            return s.strip("/* ").strip()
    names = re.findall(r"^(?:internal |private |public |data |sealed |abstract |open )*(?:class|object|interface|enum class|fun) +([A-Za-z0-9_.<>]+)", text, re.M)
    return ("declares " + ", ".join(names[:4])) if names else ""
for prefix, title in roots:
    group = [f for f in files if f.startswith(prefix) and f.endswith((".kt", ".java", ".js", ".json", ".xml", ".kts")) and "/build/" not in f]
    if flt: group = [f for f in group if flt in f]
    if not group: continue
    print("\n## " + title)
    for f in group:
        try: n = sum(1 for _ in open(f, encoding="utf-8", errors="replace"))
        except Exception: n = 0
        d = describe(f)
        short = f[len(prefix):].lstrip("/") or f
        print("  %-52s %5d  %s" % (short[:52], n, d[:100]))
if not flt:
    print("\n## docs and state files: CLAUDE.md (rules) HANDOFF.md (cold start) BRIEF.md (decisions) TASKS.md (job list) CHECKPOINT.md (where we stopped) BUILDLOG.md (releases) research/*.md (findings) docs/ARCHITECTURE.md docs/HARDWARE.md")
PY
