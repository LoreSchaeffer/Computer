package it.lycoris.lyco8.emulator.hardware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

/**
 * Enterprise utility class responsible for loading a font from a bitmap tilemap
 * and injecting it into the PPU's CHR-RAM memory space.
 * Follows the Data-Driven design pattern to decouple graphical assets from Java code.
 */
public class CharRom {
    private static final Logger LOG = LoggerFactory.getLogger(CharRom.class);

    private static final int TILE_SIZE_PIXELS = 8;
    private static final int TILES_PER_ROW = 16;
    private static final int TOTAL_CHARACTERS = 128;
    private static final int BYTES_PER_TILE = 32;

    /**
     * Loads a monochrome tilemap from resources and flashes it as 4BPP into PPU VRAM.
     *
     * @param resourcePath The classpath path to the PNG tilemap (e.g., "/fonts/default.png").
     * @param ppu          The target PPU to flash the data into.
     */
    public void injectFont(String resourcePath, Ppu ppu) {
        LOG.info("Loading Character ROM from resource: {}", resourcePath);

        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) throw new RuntimeException("CharROM resource not found: " + resourcePath);
            BufferedImage tilemap = ImageIO.read(is);

            for (int charIndex = 0; charIndex < TOTAL_CHARACTERS; charIndex++) {
                int tileX = (charIndex % TILES_PER_ROW) * TILE_SIZE_PIXELS;
                int tileY = (charIndex / TILES_PER_ROW) * TILE_SIZE_PIXELS;

                int tileBaseAddress = charIndex * BYTES_PER_TILE;

                for (int row = 0; row < 8; row++) {
                    for (int byteColumn = 0; byteColumn < 4; byteColumn++) {
                        int leftPixelX = tileX + (byteColumn * 2);
                        int rightPixelX = leftPixelX + 1;
                        int pixelY = tileY + row;

                        boolean isLeftPixelSet = this.isPixelForeground(tilemap, leftPixelX, pixelY);
                        boolean isRightPixelSet = this.isPixelForeground(tilemap, rightPixelX, pixelY);

                        int leftColorIndex = isLeftPixelSet ? 1 : 0;
                        int rightColorIndex = isRightPixelSet ? 1 : 0;

                        int packed4BppByte = ((leftColorIndex & 0x0F) << 4) | (rightColorIndex & 0x0F);
                        int memoryAddress = tileBaseAddress + (row * 4) + byteColumn;

                        ppu.flashCharacterRom(memoryAddress, packed4BppByte);
                    }
                }
            }

            LOG.info("Successfully flashed {} characters into PPU CHR-RAM.", TOTAL_CHARACTERS);
        } catch (Exception e) {
            LOG.error("Failed to inject Character ROM", e);
        }
    }

    private boolean isPixelForeground(BufferedImage image, int x, int y) {
        if (x >= image.getWidth() || y >= image.getHeight()) return false;

        int argb = image.getRGB(x, y);
        int alpha = (argb >> 24) & 0xFF;
        if (alpha < 128) return false;

        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        return (red + green + blue) > 100;
    }
}
