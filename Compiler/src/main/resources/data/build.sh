#!/bin/bash

# Default Configuration
INPUT_FILE="src/main.ls"
OUTPUT_FILE="build/output.bin"

# Parameter Parsing
while [[ "$#" -gt 0 ]]; do
    case $1 in
        -i|--input) INPUT_FILE="$2"; shift 2 ;;
        -o|--output) OUTPUT_FILE="$2"; shift 2 ;;
        *)
            echo "[ERROR] Unknown parameter passed: $1"
            echo "Usage: ./build.sh [-i input.ls] [-o output.bin]"
            exit 1
            ;;
    esac
done

# Derived File Names
ASM_FILE="${OUTPUT_FILE%.*}.asm"
OBJ_FILE="${OUTPUT_FILE%.*}.o"
BUILD_DIR=$(dirname "$OUTPUT_FILE")

echo "[PRE-FLIGHT] Checking dependencies..."
if ! command -v ca65 &> /dev/null; then
    echo "[ERROR] ca65 compiler not found!"
    echo "Please install cc65 using your package manager (e.g., sudo apt install cc65)"
    exit 1
fi

# Grants the presence of the build directory before compiling
mkdir -p "$BUILD_DIR"

echo "[1/4] Compiling LycoScript to Assembly..."
java -jar LycoCompiler.jar -i "$INPUT_FILE" -o "$ASM_FILE" || exit 1

echo "[2/4] Assembling Main Project..."
ca65 "$ASM_FILE" -o "$OBJ_FILE" || exit 1

echo "[3/4] Assembling Standard Library and Custom User Assembly (Recursive)..."
STDLIB_OBJS=""

# Compilation of Standard Library
for f in stdlib/assembly/*.asm; do
    if [ -f "$f" ]; then
        base=$(basename "$f" .asm)
        obj="$BUILD_DIR/${base}.o"
        ca65 "$f" -o "$obj" || exit 1
        STDLIB_OBJS="$STDLIB_OBJS $obj"
    fi
done

# Recursive search inside src and direct compilation to build directory
while IFS= read -r -d '' f; do
    base=$(basename "$f" .asm)
    obj="$BUILD_DIR/${base}.o"
    ca65 "$f" -o "$obj" || exit 1
    STDLIB_OBJS="$STDLIB_OBJS $obj"
done < <(find src -type f -name "*.asm" -print0)

echo "[4/4] Linking ROM..."
ld65 -C lyco8.cfg "$OBJ_FILE" $STDLIB_OBJS -o "$OUTPUT_FILE" || exit 1

echo "SUCCESS! Output generated in $OUTPUT_FILE"
exit 0