# Tablet survey from your PC (read-only)

**What it does:** asks the tablet about itself (memory, what is running, how apps are compiled, heat, **what fills the storage**, the ROM's own background manager) and writes the answers
into one text file, `optimizer-report.txt`. **It only reads. It changes nothing on the tablet**, and a self-test in this repo fails the build if any command in the list
could write, kill, disable or install anything. Nothing here touches your Launcher or Browser.

**What you need:** your PC, the USB cable, the Android platform-tools (`adb`) on the PC, and USB debugging switched on in the tablet's Developer options (the same setup you already use for the Launcher).

## Steps
1. Download this folder from GitHub (the three files `intake.bat`, `intake.sh` and `intake-commands.txt` must stay together). Easiest: on the repo page choose **Code > Download ZIP** and open `tools/adb`.
2. Plug the tablet in. If it asks "Allow USB debugging?", tap **Allow** (tick "always allow" if you like).
3. **Windows:** double-click `intake.bat`. **Mac, Linux, or Git Bash on Windows:** open a terminal in the folder and run `bash intake.sh`.
4. Wait one to two minutes while the section names scroll by. It ends with "Done: optimizer-report.txt ... Nothing on the tablet was changed."
5. Send me `optimizer-report.txt` (attach it in the chat). I run it through `tools/intake.sh`, which **removes serial numbers, MAC and IP addresses, e-mail addresses and Wi-Fi names before anything is saved** to the public repository, **hides the names of installer (.apk) files in Downloads unless they are your own app builds** (only their sizes are kept), and refuses the whole file if anything key-shaped is left. The survey never lists your photos, documents or other file names.

## Tips
- **Run it when the tablet feels sluggish**, with your usual apps open, not right after a restart: that is when the numbers show what is slowing it down.
- A second run later (for example after a change we agreed on) lets us compare before and after.
- `intake.bat` was written without a Windows machine to test on. If it prints an error, send me the message, or use `bash intake.sh` in Git Bash.
- If you would like to read the file first: it is plain text, and the `#####` lines mark each section.
