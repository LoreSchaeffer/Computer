package it.lycoris.j6502.emulator.ui;

import it.lycoris.j6502.emulator.emulated.GraphicsPpu;
import it.lycoris.j6502.emulator.emulated.Keyboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * The main graphical user interface for the Lyco-8.
 * It renders the PPU framebuffer and captures physical keyboard inputs.
 */
public class LycoWindow extends JFrame {
    private final GraphicsPpu ppu;
    private final Keyboard keyboard;
    private final BufferedImage screenImage;
    private static final int PIXEL_SCALE = 8;

    // A simple 16-color palette (C64/EGA style) mapped to byte values 0x00 to 0x0F
    private final Color[] palette = new Color[]{
            Color.BLACK,
            Color.WHITE,
            Color.RED,
            Color.CYAN,
            Color.MAGENTA,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.ORANGE,
            new Color(139, 69, 19),
            Color.PINK,
            Color.DARK_GRAY,
            Color.GRAY,
            Color.LIGHT_GRAY,
            new Color(173, 216, 230),
            new Color(144, 238, 144)
    };

    /**
     * Initializes the console window.
     *
     * @param ppu      The Graphics Picture Processing Unit holding the VRAM.
     * @param keyboard The memory-mapped Keyboard device to feed inputs into.
     */
    public LycoWindow(GraphicsPpu ppu, Keyboard keyboard) {
        this.ppu = ppu;
        this.keyboard = keyboard;

        this.screenImage = new BufferedImage(GraphicsPpu.SCREEN_WIDTH, GraphicsPpu.SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);

        this.setupUI();
        this.setupInputHandling();
        this.startRenderLoop();
    }

    private void setupUI() {
        this.setTitle("Lyco-8");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        JPanel renderPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(
                        screenImage,
                        0,
                        0,
                        GraphicsPpu.SCREEN_WIDTH * PIXEL_SCALE,
                        GraphicsPpu.SCREEN_HEIGHT * PIXEL_SCALE,
                        null
                );
            }
        };

        renderPanel.setPreferredSize(new Dimension(GraphicsPpu.SCREEN_WIDTH * PIXEL_SCALE, GraphicsPpu.SCREEN_HEIGHT * PIXEL_SCALE));
        this.add(renderPanel);
        this.pack();
        this.setLocationRelativeTo(null);
    }

    private void setupInputHandling() {
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char keyChar = Character.toUpperCase(e.getKeyChar());
                if (keyChar >= 32 && keyChar <= 126) keyboard.pressKey(keyChar);
            }
        });
    }

    private void startRenderLoop() {
        // Render at approximately 60 FPS (~16ms per frame)
        Timer timer = new Timer(16, e -> {
            this.updateScreenImage();
            this.repaint();
        });
        timer.start();
    }

    private void updateScreenImage() {
        int[] vram = this.ppu.getVram();

        for (int y = 0; y < GraphicsPpu.SCREEN_HEIGHT; y++) {
            for (int x = 0; x < GraphicsPpu.SCREEN_WIDTH; x++) {
                int index = (y * GraphicsPpu.SCREEN_WIDTH) + x;
                int colorByte = vram[index];

                // Map the byte to our 16-color palette (fallback to black if out of bounds)
                Color pixelColor = (colorByte >= 0 && colorByte < this.palette.length) ? this.palette[colorByte] : Color.BLACK;

                this.screenImage.setRGB(x, y, pixelColor.getRGB());
            }
        }
    }
}
