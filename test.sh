#!/usr/bin/env sh
set -eu
cd -- "$(dirname -- "$0")"
sh build.sh
mkdir -p build-tests
if command -v javac >/dev/null 2>&1; then
    javac -source 8 -target 8 -Xlint:-options -encoding UTF-8 -cp build -d build-tests tests/*.java
else
    java -m jdk.compiler/com.sun.tools.javac.Main -source 8 -target 8 -Xlint:-options -encoding UTF-8 -cp build -d build-tests tests/*.java
fi
java -cp 'build:build-tests' Phase1Tests
java -cp 'build:build-tests' Phase2Tests
java -cp build RentTest
