const byte COLOR_RED = $04;
const byte COLOR_GREEN = $05;
const byte COLOR_BLUE = $06;

void main() {
    print("Hello World");

    byte currentColor = COLOR_RED;
    byte pixelIndex = 0;

    while (1) {
        pixelIndex = 0;

        while (pixelIndex < 255) {
            SCREEN[pixelIndex] = currentColor;
            pixelIndex = pixelIndex + 1;
        }

        SCREEN[255] = currentColor;

        if (currentColor == COLOR_RED) {
            currentColor = COLOR_GREEN;
        } else {
            if (currentColor == COLOR_GREEN) {
                currentColor = COLOR_BLUE;
            } else {
                currentColor = COLOR_RED;
            }
        }

        delay();
    }
}