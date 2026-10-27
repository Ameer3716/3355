@echo off
setlocal
cd /d "%~dp0"
call build.bat
if errorlevel 1 exit /b 1
if not exist build-tests mkdir build-tests
where javac >nul 2>nul
if errorlevel 1 (
    java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -cp build -d build-tests tests\Phase1Tests.java
) else (
    javac -encoding UTF-8 -cp build -d build-tests tests\Phase1Tests.java
)
if errorlevel 1 exit /b 1
java -cp "build;build-tests" Phase1Tests
set "projectExitCode=%ERRORLEVEL%"
echo.
pause
exit /b %projectExitCode%
