#!/bin/bash

# run-tests.sh - Compiles and runs all tests

# Compile first
./scripts/compile.sh

if [ $? -ne 0 ]; then
    echo "Compilation failed. Exiting."
    exit 1
fi

echo ""
echo "╔════════════════════════════════════════╗"
echo "║         Running All Tests              ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Run tests using JUnit 5 Console Launcher
echo "Running tests with JUnit 5..."
java -jar lib/junit-platform-console-standalone.jar \
    -cp bin \
    --scan-classpath \
    --details=tree \
    --disable-banner \
    --disable-ansi-colors

# Check exit code
if [ $? -eq 0 ]; then
    echo ""
    echo "╔════════════════════════════════════════╗"
    echo "║   All Tests Passed Successfully!      ║"
    echo "╚════════════════════════════════════════╝"
    exit 0
else
    echo ""
    echo "╔════════════════════════════════════════╗"
    echo "║   Some Tests Failed                   ║"
    echo "╚════════════════════════════════════════╝"
    exit 1
fi
