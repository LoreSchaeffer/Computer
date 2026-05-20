package it.lycoris.lyco8.emulator.ui;

import it.lycoris.lyco8.emulator.hardware.Keyboard;
import it.lycoris.lyco8.emulator.hardware.Ppu;

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
public class Display extends JFrame {
    private static final int PIXEL_SCALE = 4;

    private final Ppu ppu;
    private final Keyboard keyboard;

    private final BufferedImage screenImage;
    private final int[] displayPixels;

    /**
     * Initializes the emulator console window.
     *
     * @param ppu      The Tile-Based Picture Processing Unit responsible for rendering.
     * @param keyboard The memory-mapped Keyboard device to capture user inputs.
     */
    public Display(Ppu ppu, Keyboard keyboard) {
        this.ppu = ppu;
        this.keyboard = keyboard;

        this.screenImage = new BufferedImage(
                Ppu.SCREEN_WIDTH_PIXELS,
                Ppu.SCREEN_HEIGHT_PIXELS,
                BufferedImage.TYPE_INT_RGB
        );

        this.displayPixels = ((DataBufferInt) this.screenImage.getRaster().getDataBuffer()).getData();

        this.setupUserInterface();
        this.setupInputHandling();
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
                        Ppu.SCREEN_WIDTH_PIXELS * PIXEL_SCALE,
                        Ppu.SCREEN_HEIGHT_PIXELS * PIXEL_SCALE,
                        null
                );
            }
        };

        renderPanel.setPreferredSize(new Dimension(
                Ppu.SCREEN_WIDTH_PIXELS * PIXEL_SCALE,
                Ppu.SCREEN_HEIGHT_PIXELS * PIXEL_SCALE
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
                    keyboard.pressKey(0x03);
                } else if (keyCode == KeyEvent.VK_ENTER) {
                    keyboard.pressKey(0x0D);
                } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
                    keyboard.pressKey(0x08);
                } else {
                    char keyChar = Character.toUpperCase(event.getKeyChar());
                    if (keyChar >= 32 && keyChar <= 126) keyboard.pressKey(keyChar);
                }
            }
        });
    }

    /**
     * Triggered externally by the Precision Emulator Loop.
     * Fetches the completed frame from the hardware double-buffer and schedules a UI repaint.
     */
    public void renderFrame() {
        int[] sourcePixels = this.ppu.getPixels();
        System.arraycopy(sourcePixels, 0, this.displayPixels, 0, this.displayPixels.length);
        this.repaint();
    }

    public void updateTitleWithFps(double fps) {
        SwingUtilities.invokeLater(() -> this.setTitle(String.format("Lyco-8 Emulator - FPS: %.1f", fps)));
    }
}