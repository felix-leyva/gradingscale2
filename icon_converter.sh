#!/bin/bash

# Check if ImageMagick is installed and determine the correct command
if command -v magick &> /dev/null; then
    MAGICK_CMD="magick"
elif command -v convert &> /dev/null; then
    MAGICK_CMD="convert"
    echo "Note: Using legacy 'convert' command. Consider upgrading to ImageMagick v7+ for better support."
else
    echo "Error: ImageMagick is not installed. Please install it using Homebrew:"
    echo "brew install imagemagick"
    exit 1
fi

# Function to display usage information
function show_usage {
    echo "Usage: $0 input.svg [output_basename]"
    echo ""
    echo "Converts an SVG file to application icon formats:"
    echo "  - .icns for macOS"
    echo "  - .ico for Windows"
    echo "  - .png for Linux (multiple resolutions)"
    echo ""
    echo "Arguments:"
    echo "  input.svg        The input SVG file"
    echo "  output_basename  Optional: Base name for output files (default: 'app_icon')"
    exit 1
}

# Check if input file is provided
if [ $# -lt 1 ]; then
    show_usage
fi

# Set variables
INPUT_SVG="$1"
OUTPUT_BASE="${2:-app_icon}"
TEMP_DIR="$(mktemp -d)"

# Check if input file exists and is an SVG
if [ ! -f "$INPUT_SVG" ]; then
    echo "Error: Input file does not exist: $INPUT_SVG"
    exit 1
fi

if [[ ! "$INPUT_SVG" =~ \.svg$ ]]; then
    echo "Error: Input file is not an SVG: $INPUT_SVG"
    exit 1
fi

echo "Converting SVG to icon formats..."
echo "Using temporary directory: $TEMP_DIR"

# --- Create PNG files at various resolutions ---
echo "Creating PNG files..."
SIZES=(16 32 48 64 128 256 512 1024)
for SIZE in "${SIZES[@]}"; do
    echo "  Creating ${SIZE}x${SIZE} PNG"
    if [ "$MAGICK_CMD" = "magick" ]; then
        # ImageMagick v7 syntax
        $MAGICK_CMD convert -background none -density 1200 "$INPUT_SVG" -resize ${SIZE}x${SIZE} "${TEMP_DIR}/icon_${SIZE}x${SIZE}.png"
        $MAGICK_CMD convert "${TEMP_DIR}/icon_${SIZE}x${SIZE}.png" -strip -define png:compression-level=9 "${TEMP_DIR}/icon_${SIZE}x${SIZE}.png"
    else
        # ImageMagick v6 syntax (convert)
        $MAGICK_CMD -background none -density 1200 "$INPUT_SVG" -resize ${SIZE}x${SIZE} "${TEMP_DIR}/icon_${SIZE}x${SIZE}.png"
        $MAGICK_CMD "${TEMP_DIR}/icon_${SIZE}x${SIZE}.png" -strip -define png:compression-level=9 "${TEMP_DIR}/icon_${SIZE}x${SIZE}.png"
    fi

    # Verify the file was created
    if [ ! -f "${TEMP_DIR}/icon_${SIZE}x${SIZE}.png" ]; then
        echo "Error: Failed to create ${SIZE}x${SIZE} PNG"
        exit 1
    fi
done

# --- Create Linux PNG icon (using 256px size) ---
echo "Creating Linux PNG icon..."
cp "${TEMP_DIR}/icon_256x256.png" "${OUTPUT_BASE}.png"

# --- Create Windows ICO file with better quality ---
echo "Creating Windows ICO file..."
# For Windows ICO, ensure we have different sizes
ICO_ARGS=()
for SIZE in 16 32 48 64 128 256; do
    if [ -f "${TEMP_DIR}/icon_${SIZE}x${SIZE}.png" ]; then
        ICO_ARGS+=("${TEMP_DIR}/icon_${SIZE}x${SIZE}.png")
    else
        echo "Warning: Missing ${SIZE}x${SIZE} PNG for ICO creation"
    fi
done

# Create the ICO file with the correct command format
if [ "$MAGICK_CMD" = "magick" ]; then
    # ImageMagick v7 syntax
    $MAGICK_CMD convert "${ICO_ARGS[@]}" -colors 256 "${OUTPUT_BASE}.ico"
else
    # ImageMagick v6 syntax
    $MAGICK_CMD "${ICO_ARGS[@]}" -colors 256 "${OUTPUT_BASE}.ico"
fi

# --- Create macOS ICNS file ---
echo "Creating macOS ICNS file..."

# Create iconset directory
ICONSET="${TEMP_DIR}/icon.iconset"
mkdir -p "$ICONSET"

# Generate different sizes for macOS iconset
cp "${TEMP_DIR}/icon_16x16.png" "${ICONSET}/icon_16x16.png"
cp "${TEMP_DIR}/icon_32x32.png" "${ICONSET}/icon_16x16@2x.png"
cp "${TEMP_DIR}/icon_32x32.png" "${ICONSET}/icon_32x32.png"
cp "${TEMP_DIR}/icon_64x64.png" "${ICONSET}/icon_32x32@2x.png"
cp "${TEMP_DIR}/icon_128x128.png" "${ICONSET}/icon_128x128.png"
cp "${TEMP_DIR}/icon_256x256.png" "${ICONSET}/icon_128x128@2x.png"
cp "${TEMP_DIR}/icon_256x256.png" "${ICONSET}/icon_256x256.png"
cp "${TEMP_DIR}/icon_512x512.png" "${ICONSET}/icon_256x256@2x.png"
cp "${TEMP_DIR}/icon_512x512.png" "${ICONSET}/icon_512x512.png"
cp "${TEMP_DIR}/icon_1024x1024.png" "${ICONSET}/icon_512x512@2x.png"

# Convert iconset to icns (using macOS utility)
iconutil -c icns -o "${OUTPUT_BASE}.icns" "$ICONSET"

# Create a large PNG specifically for high-res needs
echo "Creating high-resolution PNG for additional needs..."
cp "${TEMP_DIR}/icon_1024x1024.png" "${OUTPUT_BASE}_large.png"

# Clean up
rm -rf "$TEMP_DIR"

echo "Done! Created the following icon files:"
echo "  - ${OUTPUT_BASE}.icns (macOS)"
echo "  - ${OUTPUT_BASE}.ico (Windows)"
echo "  - ${OUTPUT_BASE}.png (Linux, 256x256)"
echo "  - ${OUTPUT_BASE}_large.png (High-resolution, 1024x1024)"
