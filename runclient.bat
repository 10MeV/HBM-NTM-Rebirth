@echo off
setlocal

REM Compatibility entry point: keep the conventional "runclient" name while
REM delegating to the maintained client launcher.
call "%~dp0run-client.bat"
exit /b %ERRORLEVEL%
