#!/usr/bin/env sh
set -eu
cd -- "$(dirname -- "$0")"
mkdir -p build
if command -v javac >/dev/null 2>&1; then
    javac -encoding UTF-8 -d build src/*.java
else
    java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -d build src/*.java
fi
if command -v jar >/dev/null 2>&1; then
    jar cfe RiyadhPoly.jar RiyadhPolyApp -C build .
else
    java -m jdk.jartool/sun.tools.jar.Main cfe RiyadhPoly.jar RiyadhPolyApp -C build .
fi
printf '%s\n' 'Build complete: RiyadhPoly.jar'
