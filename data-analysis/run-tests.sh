#!/bin/bash
# Test compilation and execution script for Linux/Mac

JUNIT_JAR="lib/junit-platform-console-standalone-1.10.0.jar"

if [ ! -f "$JUNIT_JAR" ]; then
    echo "JUnit JAR not found. Downloading..."
    mkdir -p lib
    curl -L -o "$JUNIT_JAR" \
      https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.0/junit-platform-console-standalone-1.10.0.jar
fi

echo "Compiling main source files..."
mkdir -p out
find src/main/java -name "*.java" -print0 | xargs -0 javac -d out -encoding UTF-8

if [ $? -ne 0 ]; then
    echo "✗ Main compilation failed"
    exit 1
fi

echo "Compiling test source files..."
mkdir -p out-test
find src/test/java -name "*.java" -print0 | xargs -0 javac -d out-test -encoding UTF-8 -cp "out:$JUNIT_JAR"

if [ $? -ne 0 ]; then
    echo "✗ Test compilation failed"
    exit 1
fi

echo "Running tests..."
java -jar "$JUNIT_JAR" --class-path "out:out-test" --scan-class-path

