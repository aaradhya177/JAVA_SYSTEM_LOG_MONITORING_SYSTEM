#!/bin/bash

# Compile Java sources
echo "Compiling Java sources..."
javac -d bin -cp "lib/*" src/com/logmonitor/model/*.java src/com/logmonitor/util/*.java src/com/logmonitor/dao/*.java src/com/logmonitor/service/*.java src/com/logmonitor/server/*.java src/com/logmonitor/ui/*.java src/com/logmonitor/*.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo "Compiled classes are in bin/ directory"
else
    echo "Compilation failed!"
    exit 1
fi
