@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
where javac >nul 2>nul
if errorlevel 1 (
    java -m jdk.compiler/com.sun.tools.javac.Main -source 8 -target 8 -Xlint:-options -encoding UTF-8 -d build src\*.java
) else (
    javac -source 8 -target 8 -Xlint:-options -encoding UTF-8 -d build src\*.java
)
if errorlevel 1 exit /b 1
where jar >nul 2>nul
if errorlevel 1 (
    java -m jdk.jartool/sun.tools.jar.Main cfe RiyadhPoly.jar RiyadhPolyApp -C build .
) else (
    jar cfe RiyadhPoly.jar RiyadhPolyApp -C build .
)
if errorlevel 1 exit /b 1
echo Build complete: RiyadhPoly.jar
