package it.lycoris.j6502.emulator.hardware;

import it.lycoris.j6502.emulator.core.MemoryMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class VirtualFileSystem implements BusDevice {
    private static final Logger LOG = LoggerFactory.getLogger(VirtualFileSystem.class);

    private final Ram systemRam;
    private final Keyboard keyboard;

    private int statusRegister = 0x00;

    private static final int PTR_SMEML = 0x79;
    private static final int PTR_SVARL = 0x7B;
    private static final int PTR_SARRYL = 0x7D;
    private static final int PTR_EARRYL = 0x7F;

    public VirtualFileSystem(Ram systemRam, Keyboard keyboard) {
        this.systemRam = systemRam;
        this.keyboard = keyboard;
    }

    @Override
    public boolean accepts(int address) {
        return address == MemoryMap.VFS_COMMAND;
    }

    @Override
    public int read(int address) {
        if (address == MemoryMap.VFS_COMMAND) return this.statusRegister;
        return 0x00;
    }

    @Override
    public void write(int address, int value) {
        if (address == MemoryMap.VFS_COMMAND) {
            this.statusRegister = 0x00;
            try {
                SwingUtilities.invokeAndWait(() -> {
                    if (value == 0x01) executeLoad();
                    if (value == 0x02) executeSave();
                });
            } catch (Exception e) {
                LOG.error("VFS execution failed", e);
            }
        }
    }

    private void executeSave() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Save Lyco-8 Binary (.bin)");

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                int start = systemRam.read(PTR_SMEML) | (systemRam.read(PTR_SMEML + 1) << 8);
                int end = systemRam.read(PTR_SVARL) | (systemRam.read(PTR_SVARL + 1) << 8);

                if (start >= MemoryMap.RAM_SIZE || end > MemoryMap.RAM_SIZE || start > end) {
                    LOG.error("Corrupted pointers.");
                    return;
                }

                byte[] buffer = new byte[end - start];
                for (int i = 0; i < buffer.length; i++) {
                    buffer[i] = (byte) systemRam.read(start + i);
                }

                Files.write(file.toPath(), buffer);
                LOG.info("Program saved perfectly ({} bytes) from ${} to ${}.",
                        buffer.length, String.format("%04X", start), String.format("%04X", end));
            } catch (Exception e) {
                LOG.error("Failed to save file", e);
            }
        }
    }

    private void executeLoad() {
        JFileChooser chooser = new JFileChooser(new File("."));
        chooser.setDialogTitle("Load File (.txt or .bin)");

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                byte[] data = Files.readAllBytes(file.toPath());

                if (isTextContent(data)) {
                    String content = new String(data, StandardCharsets.UTF_8);
                    this.keyboard.injectText("NEW\r" + content + "\r");
                    this.statusRegister = 0x00;
                    LOG.info("Plain text file injected into Keyboard buffer.");
                } else {
                    int newStart = systemRam.read(PTR_SMEML) | (systemRam.read(PTR_SMEML + 1) << 8);

                    if (newStart < 0x0200 || newStart >= MemoryMap.RAM_SIZE) {
                        LOG.error("Invalid start address for binary load: {}", newStart);
                        return;
                    }

                    for (int i = 0; i < data.length; i++) {
                        systemRam.write(newStart + i, data[i] & 0xFF);
                    }

                    int currentOffset = 0;

                    while (currentOffset + 1 < data.length) {
                        int pointerLow = data[currentOffset] & 0xFF;
                        int pointerHigh = data[currentOffset + 1] & 0xFF;

                        if (pointerLow == 0 && pointerHigh == 0) {
                            break;
                        }

                        int endOfLineOffset = currentOffset + 4;

                        while (endOfLineOffset < data.length && data[endOfLineOffset] != 0x00) {
                            endOfLineOffset++;
                        }

                        int nextLineOffset = endOfLineOffset + 1;
                        int newAbsolutePtr = newStart + nextLineOffset;

                        systemRam.write(newStart + currentOffset, newAbsolutePtr & 0xFF);
                        systemRam.write(newStart + currentOffset + 1, (newAbsolutePtr >> 8) & 0xFF);

                        currentOffset = nextLineOffset;
                    }

                    int newEndAddress = newStart + data.length;
                    systemRam.write(PTR_SVARL, newEndAddress & 0xFF);
                    systemRam.write(PTR_SVARL + 1, (newEndAddress >> 8) & 0xFF);
                    systemRam.write(PTR_SARRYL, newEndAddress & 0xFF);
                    systemRam.write(PTR_SARRYL + 1, (newEndAddress >> 8) & 0xFF);
                    systemRam.write(PTR_EARRYL, newEndAddress & 0xFF);
                    systemRam.write(PTR_EARRYL + 1, (newEndAddress >> 8) & 0xFF);

                    this.statusRegister = 0xFF;

                    LOG.info("Relocating Loader success. Requested OS Warm Start.");
                }
            } catch (Exception e) {
                LOG.error("Failed to load binary or text file into Lyco-8 memory", e);
            }
        }
    }

    private boolean isTextContent(byte[] data) {
        for (byte b : data) {
            if (b == 0x00) return false;
        }
        return true;
    }
}