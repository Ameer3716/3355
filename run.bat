@echo off
setlocal
cd /d "%~dp0"
java -jar "RiyadhPoly.jar" %*
set "projectExitCode=%ERRORLEVEL%"
echo.
pause
exit /b %projectExitCode%
