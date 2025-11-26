#!/bin/bash
# Compilation script for Linux/Mac

echo "Creating output directory..."
mkdir -p out

echo "Compiling main source files..."
find src/main/java -name "*.java" -print0 | xargs -0 javac -d out -encoding UTF-8

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo "Run with: java -cp out com.dataanalysis.app.Main [command] [args...]"
else
    echo "✗ Compilation failed"
    exit 1
fi

