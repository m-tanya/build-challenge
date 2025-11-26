#!/bin/bash

# run-demo.sh - Compiles and runs the ProducerConsumerDemo

# Compile first
./scripts/compile.sh

if [ $? -ne 0 ]; then
    echo "Compilation failed. Exiting."
    exit 1
fi

echo ""
echo "╔════════════════════════════════════════╗"
echo "║         Running Demo                   ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Run the demo
java -cp bin com.producerconsumer.ProducerConsumerDemo
