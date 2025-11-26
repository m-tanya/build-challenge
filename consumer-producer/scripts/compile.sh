#!/bin/bash

# compile.sh - Compiles all Java source and test files

echo "╔════════════════════════════════════════╗"
echo "║         Compiling Java Files           ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Create bin directory if it doesn't exist
mkdir -p bin

# Compile source files
echo "Compiling source files..."
MAIN_SOURCES=$(find src/main/java -name "*.java")

if [ -z "$MAIN_SOURCES" ]; then
    echo "✗ No main sources found under src/main/java"
    exit 1
fi

javac -d bin $MAIN_SOURCES

if [ $? -eq 0 ]; then
    echo "✓ Source files compiled successfully"
else
    echo "✗ Source compilation failed"
    exit 1
fi

echo ""

# Compile test files (with JUnit in classpath)
echo "Compiling test files..."
TEST_SOURCES=$(find src/test/java -name "*.java")

if [ -z "$TEST_SOURCES" ]; then
    echo "✗ No test sources found under src/test/java"
    exit 1
fi

javac -d bin -cp "bin:lib/*" $TEST_SOURCES

if [ $? -eq 0 ]; then
    echo "✓ Test files compiled successfully"
else
    echo "✗ Test compilation failed"
    exit 1
fi

echo ""
echo "✓ All files compiled successfully!"
echo "  Output directory: bin/"
