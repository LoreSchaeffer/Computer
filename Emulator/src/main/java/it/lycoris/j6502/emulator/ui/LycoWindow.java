package it.lycoris.j6502.emulator.ui;

import it.lycoris.j6502.emulator.hardware.Keyboard;
import it.lycoris.j6502.emulator.hardware.TileGraphicsPpu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * The main graphical user interface for the Lyco-8 emulator.
 * It provides the rendering canvas for the Tile-Based PPU and captures keyboard inputs.
 * Optimized for high-performance direct buffer manipulation using standard ARGB pixels.
 */
public class LycoWindow extends JFrame {

    private static final int PIXEL_SCALE = 8;
    private static final int FRAMES_PER_SECOND = 60;
    private static final int FRAME_TIME_MS = 1000 / FRAMES_PER_SECOND;

    private final TileGraphicsPpu ppu;
    private final Keyboard keyboard;

    private final BufferedImage screenImage;
    private final int[] displayPixels;

    private final int[] hexPalette = new int[]{
            0xFF000000, // 0: BLACK
            0xFFFFFFFF, // 1: WHITE
            0xFFFF0000, // 2: RED
            0xFF00FFFF, // 3: CYAN
            0xFFFF00FF, // 4: MAGENTA
            0xFF00FF00, // 5: GREEN
            0xFF0000FF, // 6: BLUE
            0xFFFFFF00, // 7: YELLOW
            0xFFFFA500, // 8: ORANGE
            0xFF8B4513, // 9: BROWN
            0xFFFFC0CB, // A: PINK
            0xFF404040, // B: DARK_GRAY
            0xFF808080, // C: GRAY
            0xFFD3D3D3, // D: LIGHT_GRAY
            0xFFADD8E6, // E: LIGHT_BLUE
            0xFF90EE90  // F: LIGHT_GREEN
    };

    /**
     * Initializes the emulator console window.
     *
     * @param ppu      The Tile-Based Picture Processing Unit responsible for rendering.
     * @param keyboard The memory-mapped Keyboard device to capture user inputs.
     */
    public LycoWindow(TileGraphicsPpu ppu, Keyboard keyboard) {
        this.ppu = ppu;
        this.keyboard = keyboard;

        this.screenImage = new BufferedImage(
                TileGraphicsPpu.SCREEN_WIDTH_PIXELS,
                TileGraphicsPpu.SCREEN_HEIGHT_PIXELS,
                BufferedImage.TYPE_INT_RGB
        );

        this.displayPixels = ((DataBufferInt) this.screenImage.getRaster().getDataBuffer()).getData();

        this.setupUserInterface();
        this.setupInputHandling();
        this.startRenderLoop();
    }

    private void setupUserInterface() {
        this.setTitle("Lyco-8 Emulator");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        JPanel renderPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphicsContext) {
                super.paintComponent(graphicsContext);

                graphicsContext.drawImage(
                        screenImage,
                        0,
                        0,
                        TileGraphicsPpu.SCREEN_WIDTH_PIXELS * PIXEL_SCALE,
                        TileGraphicsPpu.SCREEN_HEIGHT_PIXELS * PIXEL_SCALE,
                        null
                );
            }
        };

        renderPanel.setPreferredSize(new Dimension(
                TileGraphicsPpu.SCREEN_WIDTH_PIXELS * PIXEL_SCALE,
                TileGraphicsPpu.SCREEN_HEIGHT_PIXELS * PIXEL_SCALE
        ));
        renderPanel.setBackground(Color.BLACK);

        this.add(renderPanel);
        this.pack();
        this.setLocationRelativeTo(null);

        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    private void setupInputHandling() {
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                int keyCode = event.getKeyCode();

                if (keyCode == KeyEvent.VK_ESCAPE) {
                    keyboard.pressKey(0x1B);
                } else {
                    char keyChar = Character.toUpperCase(event.getKeyChar());
                    if (keyChar >= 32 && keyChar <= 126) keyboard.pressKey(keyChar);
                }
            }
        });
    }

    private void startRenderLoop() {
        Timer renderTimer = new Timer(FRAME_TIME_MS, _ -> {
            this.updateScreenBuffer();
            this.repaint();
        });
        renderTimer.start();
    }

    /**
     * Delegates the pixel decoding and rendering logic to the PPU.
     */
    private void updateScreenBuffer() {
        this.ppu.renderToBuffer(this.displayPixels, this.hexPalette);
    }
}