package it.lycoris.j6502.emulator.ui;

import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.InputDevicePlugin;
import de.gurkenlabs.input4j.InputDevices;
import de.gurkenlabs.input4j.components.XInput;
import it.lycoris.j6502.emulator.hardware.Joypad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A background service that polls a physical USB/Bluetooth Gamepad connected to the host PC
 * using the modern input4j library, translating its inputs into electrical signals
 * for the emulated 6502 hardware.
 */
public class HostGamepadPoller {
    private static final Logger LOG = LoggerFactory.getLogger(HostGamepadPoller.class);
    private static final int POLLING_INTERVAL_MS = 16; // Roughly 60 polls per second

    private final Joypad emulatedJoypad;
    private volatile boolean isRunning;

    /**
     * Initializes the host gamepad polling service.
     *
     * @param emulatedJoypad The emulated hardware joypad to forward signals to.
     */
    public HostGamepadPoller(Joypad emulatedJoypad) {
        this.emulatedJoypad = emulatedJoypad;
        this.isRunning = false;
    }

    /**
     * Starts the polling mechanism inside a lightweight Java Virtual Thread.
     */
    public void start() {
        this.isRunning = true;

        Thread.ofVirtual()
                .name("Gamepad")
                .start(this::pollLoop);

        LOG.info("Host Gamepad poller started successfully via input4j.");
    }

    /**
     * Gracefully terminates the polling service.
     */
    public void stop() {
        this.isRunning = false;
    }

    /**
     * The main polling loop. Initializes the input4j devices, sets up the event listeners,
     * and continuously polls the hardware state.
     */
    private void pollLoop() {
        try (InputDevicePlugin devices = InputDevices.init()) {
            InputDevice device = devices.getAll()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (device == null) {
                LOG.info("No physical input devices found by input4j. Hardware joypad will not be mapped.");
            } else {
                LOG.info("Gamepad detected: {}. Mapping event listeners...", device.getName());

                this.registerButtonEvent(device, XInput.A, Joypad.BUTTON_A);
                this.registerButtonEvent(device, XInput.B, Joypad.BUTTON_B);
                this.registerButtonEvent(device, XInput.BACK, Joypad.BUTTON_SELECT);
                this.registerButtonEvent(device, XInput.START, Joypad.BUTTON_START);
                this.registerButtonEvent(device, XInput.DPAD_UP, Joypad.BUTTON_UP);
                this.registerButtonEvent(device, XInput.DPAD_DOWN, Joypad.BUTTON_DOWN);
                this.registerButtonEvent(device, XInput.DPAD_LEFT, Joypad.BUTTON_LEFT);
                this.registerButtonEvent(device, XInput.DPAD_RIGHT, Joypad.BUTTON_RIGHT);
            }

            while (this.isRunning) {
                if (device != null) device.poll();
                Thread.sleep(POLLING_INTERVAL_MS);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOG.error("Gamepad polling thread was interrupted", exception);
        } catch (Exception exception) {
            LOG.error("An unexpected error occurred during input4j initialization or polling", exception);
        }
    }

    /**
     * Helper method to map a physical XInput button to the emulated Joypad bitmask.
     * Uses generics to safely accept the component identifier required by input4j.
     *
     * @param device     The physical input device.
     * @param xInputId   The identifier of the physical button (e.g., XInput.A).
     * @param buttonMask The bitmask of the emulated 6502 Joypad.
     */
    private <T> void registerButtonEvent(InputDevice device, InputComponent.ID xInputId, int buttonMask) {
        device.onButtonPressed(xInputId, () -> this.emulatedJoypad.pressButton(buttonMask));
        device.onButtonReleased(xInputId, () -> this.emulatedJoypad.releaseButton(buttonMask));
    }
}