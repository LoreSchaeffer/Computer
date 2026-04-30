package it.lycoris.j6502.emulator.emulated;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The Advanced Audio Processing Unit (APU) for the Lycoris-8.
 * Implements a 4-channel polyphonic synthesizer inspired by the NES (Ricoh 2A03):
 * 2x Pulse (Square) Waves, 1x Triangle Wave, 1x Noise Generator.
 */
public class Apu implements BusDevice, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(Apu.class);
    private final int baseAddress;
    private final int endAddress;
    // Registers array (expanded to 16 bytes: $5000 to $500F)
    private final int[] registers = new int[16];
    // Audio Engine variables
    private static final int SAMPLE_RATE = 44100;
    private final AtomicBoolean engineRunning = new AtomicBoolean(false);
    private Thread audioThread;

    /**
     * Initializes the 4-channel APU and starts the background synthesis engine.
     *
     * @param baseAddress The starting 16-bit address for audio registers (e.g., 0x5000).
     */
    public Apu(int baseAddress) {
        this.baseAddress = baseAddress;
        this.endAddress = baseAddress + 15;

        this.startAudioEngine();
    }

    @Override
    public boolean accepts(int address) {
        return address >= this.baseAddress && address <= this.endAddress;
    }

    @Override
    public int read(int address) {
        int offset = address - this.baseAddress;
        return this.registers[offset];
    }

    @Override
    public void write(int address, int value) {
        int offset = address - this.baseAddress;
        this.registers[offset] = value & 0xFF;
    }

    /**
     * Starts the background thread responsible for continuous PCM buffer generation.
     */
    private void startAudioEngine() {
        this.engineRunning.set(true);

        this.audioThread = new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, false, false);
                SourceDataLine line = AudioSystem.getSourceDataLine(format);
                line.open(format, 2048);
                line.start();

                byte[] buffer = new byte[1024];

                // Phase accumulators for continuous waveform generation
                double anglePulse1 = 0.0;
                double anglePulse2 = 0.0;
                double angleTriangle = 0.0;

                // Noise generator state
                double currentNoiseValue = 0.0;
                int noiseHoldCounter = 0;

                while (this.engineRunning.get()) {
                    // Extract channel configurations from memory
                    int freqPulse1 = this.registers[0] | (this.registers[1] << 8);
                    int volPulse1 = this.registers[2] & 0x0F;

                    int freqPulse2 = this.registers[4] | (this.registers[5] << 8);
                    int volPulse2 = this.registers[6] & 0x0F;

                    int freqTriangle = this.registers[8] | (this.registers[9] << 8);
                    int volTriangle = this.registers[10] & 0x0F;

                    int noisePeriod = this.registers[12] & 0xFF;
                    int volNoise = this.registers[13] & 0x0F;

                    // Calculate phase increments
                    double stepPulse1 = (freqPulse1 > 0) ? (2.0 * Math.PI * freqPulse1) / SAMPLE_RATE : 0;
                    double stepPulse2 = (freqPulse2 > 0) ? (2.0 * Math.PI * freqPulse2) / SAMPLE_RATE : 0;
                    double stepTriangle = (freqTriangle > 0) ? (2.0 * Math.PI * freqTriangle) / SAMPLE_RATE : 0;

                    // Fill the PCM buffer
                    for (int i = 0; i < buffer.length; i++) {
                        double mixedSample = 0.0;

                        // 1. Pulse 1 (Square Wave)
                        if (volPulse1 > 0 && freqPulse1 > 0) {
                            double wave = Math.sin(anglePulse1) > 0 ? 1.0 : -1.0;
                            mixedSample += wave * (volPulse1 / 15.0);
                            anglePulse1 += stepPulse1;
                        }

                        // 2. Pulse 2 (Square Wave)
                        if (volPulse2 > 0 && freqPulse2 > 0) {
                            double wave = Math.sin(anglePulse2) > 0 ? 1.0 : -1.0;
                            mixedSample += wave * (volPulse2 / 15.0);
                            anglePulse2 += stepPulse2;
                        }

                        // 3. Triangle (Bass Wave)
                        if (volTriangle > 0 && freqTriangle > 0) {
                            // Mathematical approximation of a triangle wave using arcsin(sin(x))
                            double wave = (2.0 / Math.PI) * Math.asin(Math.sin(angleTriangle));
                            mixedSample += wave * (volTriangle / 15.0);
                            angleTriangle += stepTriangle;
                        }

                        // 4. Noise (White Noise for Percussion)
                        if (volNoise > 0 && noisePeriod > 0) {
                            noiseHoldCounter++;
                            // The period controls how often the random value changes (lower period = higher pitch)
                            if (noiseHoldCounter >= noisePeriod) {
                                currentNoiseValue = (Math.random() * 2.0) - 1.0;
                                noiseHoldCounter = 0;
                            }
                            mixedSample += currentNoiseValue * (volNoise / 15.0);
                        }

                        // Prevent phase overflow to avoid floating-point inaccuracies over time
                        if (anglePulse1 > 2.0 * Math.PI) anglePulse1 -= 2.0 * Math.PI;
                        if (anglePulse2 > 2.0 * Math.PI) anglePulse2 -= 2.0 * Math.PI;
                        if (angleTriangle > 2.0 * Math.PI) angleTriangle -= 2.0 * Math.PI;

                        // Mixer Normalization
                        // We divide by 4.0 because we have 4 channels that could output a max of +/- 1.0 each
                        double normalizedSample = mixedSample / 4.0;
                        int finalSample = (int) ((normalizedSample * 127.0) + 128.0);
                        buffer[i] = (byte) finalSample;
                    }

                    // Push the synthesized chunk to the sound card
                    line.write(buffer, 0, buffer.length);
                }

                line.drain();
                line.close();

            } catch (LineUnavailableException e) {
                LOG.error("Audio hardware is unavailable. APU disabled.", e);
            }
        });

        this.audioThread.setName("Lycoris-APU-Thread");
        this.audioThread.setDaemon(true);
        this.audioThread.start();
        LOG.info("4-Channel APU audio synthesis engine started successfully.");
    }

    @Override
    public void close() {
        this.engineRunning.set(false);
        if (this.audioThread != null) {
            this.audioThread.interrupt();
        }
    }
}
