package it.lycoris.j6502.emulator.emulated;

import java.util.ArrayList;
import java.util.List;

/**
 * The SystemBus acts as a Mediator between the CPU and all peripheral devices.
 * It routes read and write requests to the appropriate mapped hardware based on the address.
 */
public class SystemBus {
    private final List<BusDevice> devices = new ArrayList<>();

    /**
     * Connects a new hardware peripheral to the system bus.
     * Order of attachment matters: overlapping address spaces will be handled
     * by the first device that accepts the address.
     *
     * @param device The hardware component to attach.
     */
    public void attachDevice(BusDevice device) {
        this.devices.add(device);
    }

    /**
     * Routes a read request to the appropriate device.
     *
     * @param address The 16-bit requested address.
     * @return int The 8-bit value read, or 0x00 if the address is unmapped (open bus).
     */
    public int read(int address) {
        int boundedAddress = address & 0xFFFF;

        for (BusDevice device : this.devices) {
            if (device.accepts(boundedAddress)) {
                return device.read(boundedAddress);
            }
        }

        // Behavior of an "open bus" (no device answered)
        return 0x00;
    }

    /**
     * Routes a write request to the appropriate device.
     *
     * @param address The 16-bit requested address.
     * @param value   The 8-bit value to broadcast.
     */
    public void write(int address, int value) {
        int boundedAddress = address & 0xFFFF;
        int byteValue = value & 0xFF;

        for (BusDevice device : this.devices) {
            if (device.accepts(boundedAddress)) {
                device.write(boundedAddress, byteValue);
                return;
            }
        }
    }
}
