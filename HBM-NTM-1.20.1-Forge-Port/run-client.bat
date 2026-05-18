@echo off
setlocal

cd /d "%~dp0"

set "JAVA_OPTS=-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890 -Dnet.minecraftforge.gradle.check.certs=false"

echo Starting HBM NTM Forge 1.20.1 client...
echo Project: %CD%
echo Proxy: 127.0.0.1:7890
echo.

call gradlew.bat runClient --no-daemon
set "EXIT_CODE=%ERRORLEVEL%"

echo.
if "%EXIT_CODE%"=="0" (
    echo Client exited successfully.
) else (
    echo Client failed with exit code %EXIT_CODE%.
)

pause
exit /b %EXIT_CODE%
