@echo off
setlocal

cd /d "%~dp0"

set "HBM_BUILD_JAVA_OPTS=-Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=7890 -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=7890 -Dnet.minecraftforge.gradle.check.certs=false"
if defined JAVA_OPTS (
    set "JAVA_OPTS=%JAVA_OPTS% %HBM_BUILD_JAVA_OPTS%"
) else (
    set "JAVA_OPTS=%HBM_BUILD_JAVA_OPTS%"
)

echo Building HBM NTM Forge 1.20.1 mod...
echo Project: %CD%
echo Proxy: 127.0.0.1:7890
echo.

call gradlew.bat build --no-daemon
set "EXIT_CODE=%ERRORLEVEL%"

echo.
if "%EXIT_CODE%"=="0" (
    echo Build completed successfully.
    echo Output directory: %CD%\build\libs
) else (
    echo Build failed with exit code %EXIT_CODE%.
)

pause
exit /b %EXIT_CODE%
