#!/bin/bash

# Function to display the help message
show_help() {
    echo "Usage: $0 -i <input_file.asm> -o <output_file.bin>"
    echo "Example: $0 -i min_mon.asm -o lyco8_os.bin"
    exit 1
}

INPUT_FILE=""
OUTPUT_FILE=""

# Parse command line arguments using getopts
while getopts "i:o:" opt; do
    case ${opt} in
        i ) INPUT_FILE=$OPTARG ;;
        o ) OUTPUT_FILE=$OPTARG ;;
        \? ) show_help ;;
    esac
done

# Validate that both parameters have been provided
if [ -z "$INPUT_FILE" ] || [ -z "$OUTPUT_FILE" ]; then
    echo "Error: Missing required parameters."
    show_help
fi

# Define intermediate object file and configuration
OBJ_FILE="temp_build.o"
CONFIG_FILE="lyco8.cfg"

echo "Compiling $INPUT_FILE..."
ca65 "$INPUT_FILE" -o "$OBJ_FILE"

# Check if the compilation was successful
if [ $? -ne 0 ]; then
    echo "Error: Compilation failed!"
    exit 1
fi

echo "Linking to $OUTPUT_FILE using $CONFIG_FILE..."
ld65 -C "$CONFIG_FILE" "$OBJ_FILE" -o "$OUTPUT_FILE"

# Check if the linking was successful
if [ $? -ne 0 ]; then
    echo "Error: Linking failed!"
    # Ensure cleanup even if linking fails
    rm -f "$OBJ_FILE"
    exit 1
fi

echo "Cleaning up intermediate files..."
rm -f "$OBJ_FILE"

echo "Build successful: $OUTPUT_FILE generated."