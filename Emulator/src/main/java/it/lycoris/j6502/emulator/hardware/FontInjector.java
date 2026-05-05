package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.core.SystemBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * Enterprise utility class responsible for loading a font from a bitmap tilemap
 * and injecting it into the PPU's CHR-RAM memory space.
 * Follows the Data-Driven design pattern to decouple graphical assets from Java code.
 */
public class FontInjector {
    private static final Logger LOG = LoggerFactory.getLogger(FontInjector.class);

    private static final int CHR_RAM_BASE_ADDRESS = 0x2000;
    private static final int TILE_SIZE_PIXELS = 8;
    private static final int TILES_PER_ROW = 16;
    private static final int TOTAL_CHARACTERS = 128;

    private static final int BYTES_PER_TILE = 32;

    /**
     * Loads a monochrome tilemap from resources and injects it as 4BPP into memory.
     *
     * @param resourcePath    The classpath path to the PNG tilemap (e.g., "font.png").
     * @param bus             The system bus to write the binary data to.
     * @param foregroundColor The 4-bit palette index for text (0-15).
     * @param backgroundColor The 4-bit palette index for background (0-15).
     */
    public void injectFont(String resourcePath, SystemBus bus, int foregroundColor, int backgroundColor) {
        LOG.info("Loading bitmap font from resource: {}", resourcePath);

        try (InputStream imageStream = this.getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (imageStream == null) throw new IllegalArgumentException("Font image resource not found in classpath: " + resourcePath);

            BufferedImage tilemap = ImageIO.read(imageStream);

            if (tilemap.getWidth() < (TILE_SIZE_PIXELS * TILES_PER_ROW)) throw new IllegalArgumentException("Tilemap image is too small. Expected 16 columns of 8x8 tiles.");

            this.processAndInjectTilemap(tilemap, bus, foregroundColor, backgroundColor);
        } catch (IOException exception) {
            LOG.error("Failed to load or parse the bitmap font from path: {}", resourcePath, exception);
            throw new RuntimeException("Font initialization failed", exception);
        }
    }

    /**
     * Iterates over the image pixels and translates them into the PPU memory format.
     */
    private void processAndInjectTilemap(BufferedImage tilemap, SystemBus bus, int foregroundColor, int backgroundColor) {
        for (int asciiCode = 0; asciiCode < TOTAL_CHARACTERS; asciiCode++) {
            int tileX = (asciiCode % TILES_PER_ROW) * TILE_SIZE_PIXELS;
            int tileY = (asciiCode / TILES_PER_ROW) * TILE_SIZE_PIXELS;

            int tileBaseAddress = CHR_RAM_BASE_ADDRESS + (asciiCode * BYTES_PER_TILE);

            for (int row = 0; row < TILE_SIZE_PIXELS; row++) {
                for (int byteColumn = 0; byteColumn < 4; byteColumn++) {
                    int leftPixelX = tileX + (byteColumn * 2);
                    int rightPixelX = leftPixelX + 1;
                    int pixelY = tileY + row;

                    boolean isLeftPixelSet = this.isPixelForeground(tilemap, leftPixelX, pixelY);
                    boolean isRightPixelSet = this.isPixelForeground(tilemap, rightPixelX, pixelY);

                    int leftColor = isLeftPixelSet ? foregroundColor : backgroundColor;
                    int rightColor = isRightPixelSet ? foregroundColor : backgroundColor;

                    int packed4BppByte = ((leftColor & 0x0F) << 4) | (rightColor & 0x0F);
                    int memoryAddress = tileBaseAddress + (row * 4) + byteColumn;

                    bus.write(memoryAddress, packed4BppByte);
                }
            }
        }

        LOG.info("Successfully injected {} characters into CHR-RAM.", TOTAL_CHARACTERS);
    }

    /**
     * Determines if a pixel should be treated as text (foreground) or empty space.
     * Evaluates transparency (Alpha) and overall brightness.
     */
    private boolean isPixelForeground(BufferedImage image, int x, int y) {
        int argb = image.getRGB(x, y);
        int alpha = (argb >> 24) & 0xFF;

        if (alpha < 128) return false;

        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        int brightness = (red + green + blue) / 3;
        return brightness > 127;
    }
}
