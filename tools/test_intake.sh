#!/usr/bin/env bash
# test_intake.sh - self-test of the PC-side survey kit: tools/adb/intake-commands.txt (must be READ-ONLY), tools/adb/intake.sh (against a fake adb),
# tools/intake.py (redaction, credential refusal, FACTS digest) and tools/intake.sh. Hermetic: temp dir only, no tablet, no network.
# Runs inside tools/test_handoff.sh (so inside every ckpt and as the first CI gate) and on its own:  bash tools/test_intake.sh
set -uo pipefail
D="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
PASS=0; FAIL=0
ok(){ PASS=$((PASS+1)); }
bad(){ FAIL=$((FAIL+1)); echo "  FAIL  $1"; }
check(){ if eval "$2" >/dev/null 2>&1; then ok; else bad "$1"; fi; }
LIST="$D/tools/adb/intake-commands.txt"
FIX="$D/tools/testdata/intake-sample/optimizer-report.txt"

# ---------------------------------------------------------------- the command list is read-only by construction
# (the checker is a file written with a QUOTED heredoc: backticks, $ and backslashes in its regexes must reach Python untouched)
cat > "$T/ro_check.py" <<'PYEOF'
import re, sys
ALLOWED = [
  r"date", r"getprop", r"wm (size|density)", r"df -h", r"top -b -n 1 -m \d+", r"ps -A -o [A-Z,]+",
  r"cat /proc/(uptime|version|cpuinfo|meminfo|swaps|vmstat|buddyinfo|loadavg|mounts)", r"cat /sys/devices/system/cpu/online",
  r"grep -H \. (/[A-Za-z0-9_./*:-]+ ?)+",
  r"dumpsys (thermalservice|meminfo|SurfaceFlinger|display|diskstats|activity oom|activity lru|procstats --hours \d+|package dexopt|deviceidle whitelist)",
  r"du -sk (/sdcard/[A-Za-z0-9_/]+ ?)+", r"ls -l /sdcard/Download/\*\.apk",
  r"pm list packages( -[a-z0-9])*",
  r"cmd appops query-op RUN_ANY_IN_BACKGROUND (ignore|deny)",
  r"settings get (global|system) [A-Za-z_.]+", r"settings list (global|system)",
]
FORBID = set('<>;&`$"%^!\\')
def split_pipes(cmd):
    out, cur, quoted = [], "", False
    for ch in cmd:
        if ch == "'": quoted = not quoted
        if ch == "|" and not quoted:
            out.append(cur.strip()); cur = ""
        else:
            cur += ch
    out.append(cur.strip())
    return out
labels, bad = set(), []
for n, line in enumerate(open(sys.argv[1], encoding="utf-8"), 1):
    line = line.rstrip("\n")
    if not line.strip() or line.lstrip().startswith("#"):
        continue
    if "|" not in line:
        bad.append("line %d has no label|command" % n); continue
    label, cmd = line.split("|", 1)
    if not re.fullmatch(r"[a-z0-9-]+", label) or label in labels:
        bad.append("line %d: bad or repeated label %r" % (n, label))
    labels.add(label)
    if any(c in FORBID for c in cmd):
        bad.append("line %d uses a forbidden character: %s" % (n, cmd[:60]))
    segs = split_pipes(cmd)
    if not any(re.fullmatch(p, segs[0]) for p in ALLOWED):
        bad.append("line %d: first command not on the allow-list: %s" % (n, segs[0][:70]))
    for seg in segs[1:]:
        if not re.fullmatch(r"grep( -[a-zA-Z])+ '[^']*'", seg):
            bad.append("line %d: only grep may follow a pipe: %s" % (n, seg[:60]))
if bad:
    print("\n".join(bad)); sys.exit(1)
print(len(labels), "commands, all read-only")
PYEOF
check "every command in intake-commands.txt is on the read-only allow-list (and uses no character that writes, chains or breaks the Windows runner)" 'python3 -I "$T/ro_check.py" "$LIST"'
printf 'x|rm -rf /data/local/tmp\n' > "$T/bad1.txt"; printf 'x|cat /proc/meminfo > /sdcard/x\n' > "$T/bad2.txt"; printf 'x|dumpsys meminfo | sh\n' > "$T/bad3.txt"; printf 'x|am force-stop com.foo\n' > "$T/bad4.txt"
check "the read-only checker itself rejects a delete, a redirect, a pipe into sh and a force-stop" '! python3 -I "$T/ro_check.py" "$T/bad1.txt" && ! python3 -I "$T/ro_check.py" "$T/bad2.txt" && ! python3 -I "$T/ro_check.py" "$T/bad3.txt" && ! python3 -I "$T/ro_check.py" "$T/bad4.txt"'
check "the Windows runner reads the same list and deletes/moves/copies nothing" 'grep -q "intake-commands.txt" "$D/tools/adb/intake.bat" && ! grep -qiE "(^| )(del|rd|rmdir|erase|format|move|copy) " "$D/tools/adb/intake.bat"'
check "the runners pass bash -n and never call adb with anything but get-state and shell" 'bash -n "$D/tools/adb/intake.sh" && ! grep -E "adb (push|pull|install|uninstall|reboot|root|remount|sideload|forward|reverse|tcpip|connect)" "$D/tools/adb/intake.sh" "$D/tools/adb/intake.bat"'

# ---------------------------------------------------------------- the runner, against a fake adb
mkdir -p "$T/bin"
cat > "$T/bin/adb" <<'FAKE'
#!/usr/bin/env bash
echo "$@" >> "$FAKE_ADB_LOG"
case "$1" in
  get-state) echo "${FAKE_ADB_STATE:-device}" ;;
  shell) printf 'fake output for: %s\r\n' "$2" ;;
  *) exit 1 ;;
esac
FAKE
chmod +x "$T/bin/adb"
export FAKE_ADB_LOG="$T/adb.log"
NCMD="$(grep -c -E '^[^#[:space:]]' "$LIST")"
check "intake.sh writes one section per command, in order, with no carriage returns, and ends with the end marker" 'PATH="$T/bin:$PATH" bash "$D/tools/adb/intake.sh" "$T/report.txt" >/dev/null && [ "$(grep -c "^##### " "$T/report.txt")" = "$((NCMD+1))" ] && tail -1 "$T/report.txt" | grep -q "^##### end$" && ! grep -q "$(printf "\r")" "$T/report.txt" && grep -q "^# optimizer-report" "$T/report.txt"'
check "intake.sh asked adb get-state once and then exactly one shell command per list line" 'head -1 "$FAKE_ADB_LOG" | grep -qx "get-state" && [ "$(grep -c "^shell " "$FAKE_ADB_LOG")" = "$NCMD" ] && [ "$(grep -vc -E "^(get-state|shell )" "$FAKE_ADB_LOG")" = 0 ]'
check "intake.sh exits non-zero with a helpful message and writes NO file when the tablet is not ready" 'rm -f "$T/none.txt"; O="$(FAKE_ADB_STATE=unauthorized PATH="$T/bin:$PATH" bash "$D/tools/adb/intake.sh" "$T/none.txt" 2>&1)"; RC=$?; [ "$RC" -ne 0 ] && printf "%s" "$O" | grep -q "No tablet is ready" && [ ! -e "$T/none.txt" ]'

# ---------------------------------------------------------------- the repo-side processor
OUTD="$T/out"
check "intake saves the synthetic report: redacted copy + FACTS.md + INTAKE.txt + LATEST.md" 'bash "$D/tools/intake.sh" "$FIX" --out "$OUTD" --date 2026-01-01 >/dev/null && [ -f "$OUTD/2026-01-01/optimizer-report.txt" ] && [ -f "$OUTD/2026-01-01/FACTS.md" ] && [ -f "$OUTD/2026-01-01/INTAKE.txt" ] && grep -q "^# Latest tablet survey" "$OUTD/LATEST.md"'
check "redaction removed every planted private value (serial, e-mail, MAC, IP, Wi-Fi name, 16-digit number) from ALL saved files" '! grep -r -E "FAKESERIAL12345|someone@example|02:00:00:aa:bb:cc|192\.0\.2\.44|HomeNetwork|1234567890123456" "$OUTD"'
check "redaction counted what it removed" 'grep -q "serial" "$OUTD/2026-01-01/INTAKE.txt" && grep -q "ipv4" "$OUTD/2026-01-01/INTAKE.txt" && grep -q "email" "$OUTD/2026-01-01/INTAKE.txt"'
check "FACTS.md puts red flags first and has the numbers (RAM, zram, thrash rate, compile state, DuraSpeed, /data)" 'F="$OUTD/2026-01-01/FACTS.md"; [ "$(grep -n "^## Red flags" "$F" | head -1 | cut -d: -f1)" -lt "$(grep -n "^## Identity" "$F" | head -1 | cut -d: -f1)" ] && grep -q "RAM total 1.86 GB" "$F" && grep -q "zram: disksize 512 MB" "$F" && grep -q "major page faults 1230456 (16198/h)" "$F" && grep -q "Packages by status" "$F" && grep -q "DuraSpeed" "$F" && grep -q "/data is 90% full" "$F" && grep -q "Top processes by PSS: system 148 MB" "$F"'
check "a second intake on the same day goes to a new folder (-2), the first is untouched" 'bash "$D/tools/intake.sh" "$FIX" --out "$OUTD" --date 2026-01-01 >/dev/null && [ -d "$OUTD/2026-01-01-2" ] && [ -f "$OUTD/2026-01-01/FACTS.md" ]'
check "intake reads the report from standard input too (-)" 'bash "$D/tools/intake.sh" - --out "$OUTD" --date 2026-02-02 < "$FIX" >/dev/null && [ -f "$OUTD/2026-02-02/FACTS.md" ]'
# a credential-shaped string (built here, never stored in the repo) must refuse the WHOLE intake and save nothing
{ cat "$FIX"; echo "##### leak"; printf 'token=ghp_%s\n' "$(printf 'a%.0s' $(seq 1 36))"; } > "$T/leaky.txt"
check "a credential in the report refuses the whole intake and saves nothing" '! bash "$D/tools/intake.sh" "$T/leaky.txt" --out "$T/out-leak" --date 2026-03-03 >/dev/null 2>&1 && [ ! -e "$T/out-leak" ]'
printf 'just some text, not a survey\n' > "$T/foreign.txt"
check "something that is not a survey is refused and saves nothing" '! bash "$D/tools/intake.sh" "$T/foreign.txt" --out "$T/out-foreign" --date 2026-04-04 >/dev/null 2>&1 && [ ! -e "$T/out-foreign" ]'
check "a bad --date is refused (it becomes a folder name)" '! bash "$D/tools/intake.sh" "$FIX" --out "$T/out-date" --date ../../x >/dev/null 2>&1 && [ ! -e "$T/out-date" ]'
printf '##### props\nnothing useful\n##### meminfo\nMemTotal: lots kB\n##### end\n' > "$T/thin.txt"
check "a report with unexpected content still saves the redacted copy (the digest may be thin, the data is never lost)" 'bash "$D/tools/intake.sh" "$T/thin.txt" --out "$T/out-thin" --date 2026-05-05 >/dev/null && [ -f "$T/out-thin/2026-05-05/optimizer-report.txt" ] && [ -f "$T/out-thin/2026-05-05/FACTS.md" ]'

echo "intake self-test: $PASS passed, $FAIL failed"
[ "$FAIL" -eq 0 ]
