@echo off
setlocal EnableExtensions
rem intake.bat - READ-ONLY survey of the tablet from a Windows PC over adb. It changes NOTHING on the tablet.
rem Same job as intake.sh: it runs every command in intake-commands.txt with adb shell and writes the answers into optimizer-report.txt.
rem Needs adb on the PATH, USB debugging on, and Allow tapped on the tablet for this computer.
set "HERE=%~dp0"
set "OUT=%~1"
if "%OUT%"=="" set "OUT=optimizer-report.txt"
where adb >nul 2>nul
if errorlevel 1 (
  echo adb was not found. Install the Android platform-tools and put adb on your PATH, then run this again.
  pause
  exit /b 1
)
set "STATE=unknown"
for /f "delims=" %%s in ('adb get-state 2^>^&1') do set "STATE=%%s"
if /i not "%STATE%"=="device" (
  echo No tablet is ready. adb says: %STATE%
  echo Plug in the cable, turn on USB debugging in Developer options and tap Allow on the tablet, then run this again.
  pause
  exit /b 1
)
> "%OUT%" echo # optimizer-report: READ-ONLY survey written by tools\adb\intake.bat
>> "%OUT%" echo # host date: %DATE% %TIME%
for /f "usebackq eol=# tokens=1,* delims=|" %%a in ("%HERE%intake-commands.txt") do (
  echo   %%a
  >> "%OUT%" echo.
  >> "%OUT%" echo ##### %%a
  adb shell "%%b" >> "%OUT%" 2>&1
)
>> "%OUT%" echo ##### end
echo Done: %OUT%  Send this file to Claude. Nothing on the tablet was changed.
pause
