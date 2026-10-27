#!/usr/bin/env sh
set -eu
cd -- "$(dirname -- "$0")"
sh build.sh
mkdir -p build-tests
if command -v javac >/dev/null 2>&1; then
    javac -encoding UTF-8 -cp build -d build-tests tests/Phase1Tests.java
else
    java -m jdk.compiler/com.sun.tools.javac.Main -encoding UTF-8 -cp build -d build-tests tests/Phase1Tests.java
fi
java -cp 'build:build-tests' Phase1Tests
