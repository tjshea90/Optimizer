#!/usr/bin/env python3
"""intake.py - save the tablet survey made by tools/adb/intake.sh INTO THE PROJECT, redacted, with a FACTS.md digest.

    python3 -I tools/intake.py <optimizer-report.txt | ->  [--date YYYY-MM-DD] [--out research/tablet]
    (use tools/intake.sh: it adds the -I)

Why: the report is the only way Claude sees the real tablet, and the repository is PUBLIC. So the text is redacted first (serial / IMEI-like values, MAC and IP
addresses, e-mail addresses, Wi-Fi names, any 14-19 digit number), and the WHOLE intake is refused, saving nothing, if a credential-shaped string is left.
Output (in <out>/<date>/, a second intake on the same day gets -2): optimizer-report.txt (redacted), FACTS.md (red flags first, then the numbers), INTAKE.txt;
and <out>/LATEST.md = the newest FACTS.md. The input is untrusted data: nothing in it is ever executed, and Python runs isolated (-I).
"""
import argparse
import collections
import datetime
import os
import re
import sys

MAX_BYTES = 12 * 1024 * 1024

# ---------------------------------------------------------------- redaction
REDACTIONS = [
    ("email", re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\.[A-Za-z0-9-]+)*\.[A-Za-z]{2,}"), "<email>"),
    ("mac", re.compile(r"\b(?:[0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}\b"), "<mac>"),
    ("ipv4", re.compile(r"\b(?!127\.0\.0\.1\b)(?!0\.0\.0\.0\b)(?:\d{1,3}\.){3}\d{1,3}\b"), "<ip>"),
    ("ssid", re.compile(r"(?i)(\bSSID\b\s*[:=]\s*)\"?[^\"\n]+\"?"), r"\1<redacted>"),
    ("serial", re.compile(r"(?im)(\b(?:ro\.serialno|ro\.boot\.serialno|serialno|serial number|imei|meid|android_id|bluetooth_address|bdaddr|wifi_mac)\b[\]\[: =]+)([^\s\]\[]+)"), r"\1<redacted>"),
    ("digits", re.compile(r"\b\d{14,19}\b"), "<digits>"),
]
CREDENTIALS = re.compile(
    r"sk-ant-[A-Za-z0-9_-]{20,}|sk-[A-Za-z0-9_-]{32,}|gh[pousr]_[A-Za-z0-9]{30,}|github_pat_[A-Za-z0-9_]{30,}|AIza[0-9A-Za-z_-]{30,}|AKIA[0-9A-Z]{16}|xox[baprs]-[A-Za-z0-9-]{10,}"
    r"|-----BEGIN [A-Z ]*PRIVATE KEY-----"
)


def redact(text):
    """-> (text, {kind: count})."""
    counts = {}
    for kind, rx, repl in REDACTIONS:
        text, n = rx.subn(repl, text)
        if n:
            counts[kind] = n
    return text, counts


# ---------------------------------------------------------------- parsing
def sections(text):
    """The report is `##### label` followed by that command's output. -> {label: text}."""
    out, label, buf = {}, None, []
    for line in text.split("\n"):
        if line.startswith("##### "):
            if label is not None:
                out[label] = "\n".join(buf).strip("\n")
            label, buf = line[6:].strip(), []
        elif label is not None:
            buf.append(line)
    if label is not None:
        out[label] = "\n".join(buf).strip("\n")
    out.pop("end", None)
    return out


def to_int(s):
    try:
        return int(str(s).replace(",", "").strip())
    except (ValueError, TypeError):
        return None


def kb_fields(text):
    d = {}
    for line in text.split("\n"):
        m = re.match(r"^([A-Za-z_()0-9]+):\s+(\d+)", line)
        if m:
            d[m.group(1)] = int(m.group(2))
    return d


def kv_lines(text):
    """`path:value` lines from grep -H . -> {basename: value}."""
    d = {}
    for line in text.split("\n"):
        m = re.match(r"^(\S+?):(.*)$", line)
        if m and "/" in m.group(1):
            d[os.path.basename(m.group(1)) if "thermal_zone" not in m.group(1) else m.group(1).split("/")[-2] + "/" + os.path.basename(m.group(1))] = m.group(2).strip()
    return d


def props(text):
    return dict(re.findall(r"^\[([^\]]+)\]: \[(.*)\]$", text, re.M))


def human_kb(kb):
    if kb is None:
        return "n/a"
    if kb >= 1024 * 1024:
        return "%.2f GB" % (kb / 1024.0 / 1024.0)
    if kb >= 1024:
        return "%d MB" % round(kb / 1024.0)
    return "%d kB" % kb


def human_secs(s):
    if s is None:
        return "n/a"
    s = int(s)
    d, h, m = s // 86400, (s // 3600) % 24, (s // 60) % 60
    return ("%d d %d h %d min" % (d, h, m)) if d else (("%d h %d min" % (h, m)) if h else ("%d min" % m))


def guard(fn, *a):
    try:
        return fn(*a)
    except Exception:  # a fact that cannot be read is simply left out
        return None


# ---------------------------------------------------------------- facts
def build_facts(sec, source, date, counts):
    flags, lines = [], []
    P = props(sec.get("props", ""))
    meminfo = kb_fields(sec.get("meminfo", ""))
    vm = {}
    for line in sec.get("vmstat", "").split("\n"):
        m = re.match(r"^(\S+)\s+(\d+)$", line)
        if m:
            vm[m.group(1)] = int(m.group(2))
    uptime = guard(lambda: float(sec["uptime"].split()[0]))
    hours = (uptime / 3600.0) if uptime and uptime > 60 else None

    def per_hour(n):
        return ("%d/h" % (n / hours)) if (hours and n is not None) else "n/a"

    # ---- identity
    ident = "%s, Android %s (API %s), %s" % (P.get("ro.product.model", "?"), P.get("ro.build.version.release", "?"), P.get("ro.build.version.sdk", "?"), P.get("ro.build.fingerprint", "?"))
    lines += ["## Identity and time", "- %s" % ident, "- Up since boot: %s (uptime file: %s)" % (human_secs(uptime), (sec.get("uptime", "") or "n/a").split("\n")[0])]

    # ---- memory
    total, avail, free = meminfo.get("MemTotal"), meminfo.get("MemAvailable"), meminfo.get("MemFree")
    swap_t, swap_f = meminfo.get("SwapTotal"), meminfo.get("SwapFree")
    lines += ["", "## Memory"]
    if total:
        pct = (100.0 * avail / total) if avail is not None else None
        lines.append("- RAM total %s, available %s%s, free %s, file cache %s" % (human_kb(total), human_kb(avail), (" (%d%%)" % pct) if pct is not None else "", human_kb(free), human_kb(meminfo.get("Cached"))))
        if pct is not None and pct < 20:
            flags.append("Only %d%% of RAM is available at the time of the survey (%s of %s): the tablet was already short of memory." % (pct, human_kb(avail), human_kb(total)))
        if swap_t is not None:
            lines.append("- Swap %s used of %s" % (human_kb(swap_t - swap_f) if swap_f is not None else "n/a", human_kb(swap_t)))
            if swap_t == 0:
                flags.append("No swap / zram is configured: under pressure the memory killer is the only relief.")
    zram = kv_lines(sec.get("zram", ""))
    if zram:
        ms = (zram.get("mm_stat") or "").split()
        zl = "- zram: disksize %s, algorithm %s" % (human_kb(to_int(zram.get("disksize")) // 1024) if to_int(zram.get("disksize")) else "n/a", zram.get("comp_algorithm", "n/a"))
        if len(ms) >= 3 and to_int(ms[0]) and to_int(ms[1]):
            zl += ", %s of data stored in %s (ratio %.1f:1), %s of RAM used" % (human_kb(to_int(ms[0]) // 1024), human_kb(to_int(ms[1]) // 1024), to_int(ms[0]) / float(to_int(ms[1])), human_kb(to_int(ms[2]) // 1024))
        lines.append(zl)
    sysctl = kv_lines(sec.get("vm-sysctl", ""))
    if sysctl:
        lines.append("- vm settings: " + ", ".join("%s=%s" % (k, v) for k, v in sorted(sysctl.items())))
    pr = sec.get("pressure", "")
    lines.append("- Pressure-stall files (/proc/pressure): " + ("present" if re.search(r"^/proc/pressure/memory:", pr, re.M) else "not available on this kernel (lmkd then uses its older minfree mode)"))
    if vm:
        stalls = sum(v for k, v in vm.items() if k.startswith("allocstall"))
        refault = vm.get("workingset_refault", sum(v for k, v in vm.items() if k.startswith("workingset_refault")))
        lines.append("- Since boot (%s): major page faults %s (%s), refaults %s (%s), swapped in %s / out %s pages, direct-reclaim stalls %s (%s)" % (
            human_secs(uptime), vm.get("pgmajfault", "n/a"), per_hour(vm.get("pgmajfault")), refault, per_hour(refault), vm.get("pswpin", "n/a"), vm.get("pswpout", "n/a"), stalls, per_hour(stalls)))

    # ---- who uses the memory
    dm = sec.get("dumpsys-meminfo", "")
    blk = re.search(r"Total PSS by process:(.*?)(?:\n\s*Total PSS by OOM adjustment:|\Z)", dm, re.S)
    procs = [(to_int(m.group(1)), m.group(2).strip()) for m in re.finditer(r"^\s+([\d,]+)K: (.+?) \(pid \d+", blk.group(1) if blk else dm, re.M)]
    totals = {}
    for key in ("Total RAM", "Free RAM", "Used RAM", "Lost RAM", "ZRAM"):
        m = re.search(r"^\s*%s:\s+(.*)$" % re.escape(key), dm, re.M)
        if m:
            totals[key] = m.group(1).strip()
    if procs or totals:
        lines += ["", "## Who uses the memory (dumpsys meminfo)"]
        for k, v in totals.items():
            lines.append("- %s: %s" % (k, v))
        top = sorted([p for p in procs if p[0] is not None], reverse=True)[:15]
        if top:
            lines.append("- Top processes by PSS: " + "; ".join("%s %s" % (name, human_kb(kb)) for kb, name in top))
        m = re.search(r"Total PSS by OOM adjustment:(.*?)(?:\nTotal PSS by category:|\Z)", dm, re.S)
        if m:
            groups = re.findall(r"^\s{1,6}([\d,]+)K: ([A-Za-z ]+)$", m.group(1), re.M)
            if groups:
                lines.append("- By importance: " + ", ".join("%s %s" % (g.strip(), human_kb(to_int(k))) for k, g in groups))
    lru = sec.get("activity-lru", "")
    adj = collections.Counter(m.group(1) for m in re.finditer(r"^\s*#\d+:\s+(\S+)\s", lru, re.M))
    if adj:
        lines.append("- Processes in the LRU list by state: " + ", ".join("%s %d" % (k, v) for k, v in adj.most_common()))

    # ---- cpu / thermal / storage / display
    lines += ["", "## CPU, heat, storage, screen"]
    cf = kv_lines(sec.get("cpufreq-cpu0", ""))
    if cf:
        lines.append("- cpu0: governor %s, now %s kHz (min %s, max %s); steps %s" % (cf.get("scaling_governor", "n/a"), cf.get("scaling_cur_freq", "n/a"), cf.get("scaling_min_freq", "n/a"), cf.get("scaling_max_freq", "n/a"), cf.get("scaling_available_frequencies", "n/a")))
    tz = kv_lines(sec.get("thermal-zones", ""))
    temps = []
    for k, v in tz.items():
        if k.endswith("/temp") and to_int(v) is not None:
            zone = k.split("/")[0]
            temps.append((to_int(v), tz.get(zone + "/type", zone)))
    if temps:
        lines.append("- Temperatures: " + ", ".join("%s %.1f C" % (name, t / 1000.0) for t, name in sorted(temps, reverse=True)[:8]))
        if max(t for t, _ in temps) >= 70000:
            flags.append("A thermal zone reads %.0f C or more at survey time: the CPU may be throttling." % (max(t for t, _ in temps) / 1000.0))
    th = sec.get("thermalservice", "")
    m = re.search(r"Thermal Status:\s*(\d+)", th)
    if m:
        lines.append("- ThermalService status %s (0 = none)" % m.group(1))
    for line in sec.get("df", "").split("\n"):
        if re.search(r"\s/data$", line):
            lines.append("- /data: " + " ".join(line.split()))
            m = re.search(r"(\d+)%", line)
            if m and int(m.group(1)) >= 85:
                flags.append("/data is %s%% full: installs need about 10%% free and a nearly full flash slows everything." % m.group(1))
    for line in sec.get("mounts", "").split("\n"):
        if re.search(r"\s/data\s", line):
            lines.append("- /data mount: " + " ".join(line.split()[:4]))
    eq = kv_lines(sec.get("emmc-queue", ""))
    if eq:
        lines.append("- eMMC queue: " + ", ".join("%s=%s" % (k, v) for k, v in sorted(eq.items())))
    disp = "; ".join(x for x in (sec.get("wm-size", "").strip(), sec.get("wm-density", "").strip()) if x)
    if disp:
        lines.append("- Screen: " + disp.replace("\n", " "))
    gpu = [l.strip() for l in sec.get("gpu", "").split("\n") if re.search(r"GLES|Mali|PowerVR", l)]
    if gpu:
        lines.append("- GPU: " + " | ".join(gpu[:2]))

    # ---- compile state
    dex = sec.get("dexopt", "")
    status = {}
    cur = None
    for line in dex.split("\n"):
        m = re.match(r"^\s*\[([A-Za-z0-9_.]+)\]\s*$", line)
        if m:
            cur = m.group(1)
            continue
        m = re.search(r"\[status=([a-z-]+)\]", line)
        if m and cur:
            status.setdefault(cur, m.group(1))
    if status:
        lines += ["", "## Ahead-of-time compile state (dumpsys package dexopt)"]
        counts_by = collections.Counter(status.values())
        lines.append("- Packages by status: " + ", ".join("%s %d" % (k, v) for k, v in counts_by.most_common()))
        key = [p for p in status if p in ("com.tj.launcher", "com.tj.optimizer", "com.android.settings", "com.android.systemui") or p.startswith("app.kestrel.browser")]
        if key:
            lines.append("- Key apps: " + ", ".join("%s=%s" % (p, status[p]) for p in sorted(key)))
        third = [l for l in sec.get("packages-third-party", "").split("\n") if l.startswith("package:")]
        names = [l.rsplit("=", 1)[-1].strip() for l in third]
        names = [n for n in names if n in status]
        if names:
            bad = [n for n in names if status[n] in ("verify", "quicken", "extract", "run-from-apk", "run-from-vdex-fallback")]
            lines.append("- Added (third-party) apps: %d, of which %d are not compiled ahead of time (%s)" % (len(names), len(bad), ", ".join(sorted(bad)[:12])))
            if names and len(bad) * 100 >= 60 * len(names):
                flags.append("Most added apps (%d of %d) are not compiled ahead of time, so they start half-optimised: a candidate for `cmd package compile` (research/BOUNDARIES.md section 4)." % (len(bad), len(names)))

    # ---- packages / background rules / settings
    lines += ["", "## Packages, background rules, settings"]
    sysn = len([l for l in sec.get("packages-system", "").split("\n") if l.startswith("package:")])
    thirdn = len([l for l in sec.get("packages-third-party", "").split("\n") if l.startswith("package:")])
    dis = [l[8:] for l in sec.get("packages-disabled", "").split("\n") if l.startswith("package:")]
    lines.append("- Packages: %d system, %d added, %d disabled%s" % (sysn, thirdn, len(dis), (" (" + ", ".join(sorted(dis)[:15]) + ")") if dis else ""))
    for label, title in (("appops-bg-ignore", "Apps with background running IGNORED"), ("appops-bg-deny", "Apps with background running DENIED")):
        txt = sec.get(label, "").strip()
        lines.append("- %s: %s" % (title, txt.replace("\n", " | ")[:300] if txt else "none listed"))
    lines.append("- Device-idle whitelist (first lines): " + " | ".join(l.strip() for l in sec.get("deviceidle-whitelist", "").split("\n")[:8] if l.strip())[:400])
    a = [sec.get(k, "?").strip() for k in ("anim-window", "anim-transition", "anim-animator")]
    lines.append("- Animation scales (window/transition/animator): %s / %s / %s  (the Launcher owns these)" % tuple(a))
    ds_g, ds_s = sec.get("duraspeed-global", "?").strip(), sec.get("duraspeed-system", "?").strip()
    lines.append("- MediaTek DuraSpeed (setting.duraspeed.enabled): global=%s, system=%s" % (ds_g, ds_s))
    if ds_g == "1" or ds_s == "1":
        flags.append("DuraSpeed (the ROM's own background-app manager) is ON: its effect on speed is unmeasured; see research/BOUNDARIES.md section 3.")
    lines.append("- activity_manager_constants: %s" % sec.get("activity-manager-constants", "?").strip())
    for label in ("speed-keys-global", "speed-keys-system"):
        t = [l.strip() for l in sec.get(label, "").split("\n") if l.strip()]
        if t:
            lines.append("- %s: %s" % (label, "; ".join(t)[:600]))

    out = ["# Tablet survey %s" % date, "", "Saved by `tools/intake.py` from `%s`; private details were redacted before saving (%s). **Read this first, then the raw report beside it.**" % (source, ", ".join("%s x%d" % kv for kv in sorted(counts.items())) or "nothing needed redacting"), ""]
    out += ["## Red flags (read first)"] + (["- " + f for f in flags] if flags else ["- none detected by the automatic rules (that is not the same as no problem: read the numbers)"]) + [""]
    return "\n".join(out + lines) + "\n"


# ---------------------------------------------------------------- main
def next_dir(base, date):
    d = os.path.join(base, date)
    n = 2
    while os.path.exists(d):
        d = os.path.join(base, "%s-%d" % (date, n))
        n += 1
    return d


def main(argv):
    ap = argparse.ArgumentParser(description="Save the tablet survey into the project, redacted.")
    ap.add_argument("report", help="optimizer-report.txt, or - for standard input")
    ap.add_argument("--date", default=datetime.datetime.utcnow().strftime("%Y-%m-%d"))
    ap.add_argument("--out", default=os.path.join("research", "tablet"))
    args = ap.parse_args(argv)
    if not re.fullmatch(r"\d{4}-\d{2}-\d{2}", args.date):
        print("refused: --date must look like 2026-11-11")
        return 2
    raw = sys.stdin.buffer.read(MAX_BYTES + 1) if args.report == "-" else open(args.report, "rb").read(MAX_BYTES + 1)
    if len(raw) > MAX_BYTES:
        print("refused: the report is over %d MB" % (MAX_BYTES // 1024 // 1024))
        return 2
    text = raw.decode("utf-8", "replace").replace("\r\n", "\n").replace("\r", "\n")
    if "##### " not in text:
        print("refused: this does not look like an optimizer-report.txt (no '##### label' sections)")
        return 2
    text, counts = redact(text)
    leak = CREDENTIALS.search(text)
    if leak:
        print("REFUSED, nothing saved: something shaped like a live credential is in the report (%s...). Remove it and run again." % leak.group(0)[:6])
        return 3
    sec = sections(text)
    source = os.path.basename(args.report) if args.report != "-" else "standard input"
    dest = next_dir(args.out, args.date)
    os.makedirs(dest)
    try:
        facts = build_facts(sec, source, os.path.basename(dest), counts)
    except Exception as e:  # the redacted report is still saved; only the digest is missing
        facts = "# Tablet survey %s\n\nThe FACTS digest could not be built (%s: %s). Read optimizer-report.txt beside this file.\n" % (os.path.basename(dest), type(e).__name__, e)
    with open(os.path.join(dest, "optimizer-report.txt"), "w", encoding="utf-8") as f:
        f.write(text)
    with open(os.path.join(dest, "FACTS.md"), "w", encoding="utf-8") as f:
        f.write(facts)
    with open(os.path.join(dest, "INTAKE.txt"), "w", encoding="utf-8") as f:
        f.write("source: %s\ndate: %s\nbytes read: %d\nsections: %d\nredactions: %s\n" % (source, args.date, len(raw), len(sec), counts or "none"))
    with open(os.path.join(args.out, "LATEST.md"), "w", encoding="utf-8") as f:
        f.write("# Latest tablet survey: %s\n\nSaved in the folder `%s/` next to this file. Older surveys stay in their own folders.\n\n" % (os.path.basename(dest), os.path.basename(dest)) + facts)
    flags = [l for l in facts.split("\n") if l.startswith("- ") and facts.index(l) < facts.index("## Identity")]
    print("saved %s (%d sections; redactions: %s; %d red flag line(s))" % (dest, len(sec), counts or "none", len([f for f in flags if "none detected" not in f])))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
