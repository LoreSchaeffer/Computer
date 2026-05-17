package it.lycoris.j6502.emulator.core;

import it.lycoris.j6502.emulator.hardware.BusDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * The SystemBus acts as a Mediator between the central processing unit and all peripheral devices.
 * It routes read and write requests to the appropriate mapped hardware based on the address space.
 */
public class SystemBus {
    private final List<BusDevice> devices = new ArrayList<>();

    /**
     * Connects a new hardware peripheral to the system bus.
     * The order of attachment establishes the priority: if address spaces overlap,
     * the first device that accepts the address will handle the request.
     *
     * @param device The hardware component to attach.
     */
    public void registerDevice(BusDevice device) {
        this.devices.add(device);
    }

    /**
     * Routes a read request to the appropriate device on the bus.
     *
     * @param address The requested address (will be masked to 16-bit).
     * @return The 8-bit value read, or 0x00 if the address is unmapped (floating bus behavior).
     */
    public int read(int address) {
        int boundedAddress = address & 0xFFFF;

        for (BusDevice device : this.devices) {
            if (device.accepts(boundedAddress)) return device.read(boundedAddress);
        }

        return 0x00;
    }

    /**
     * Routes a write request to the appropriate device on the bus.
     *
     * @param address The requested address (will be masked to 16-bit).
     * @param data    The value to write (will be masked to 8-bit).
     */
    public void write(int address, int data) {
        int boundedAddress = address & 0xFFFF;
        int byteData = data & 0xFF;

        for (BusDevice device : this.devices) {
            if (device.accepts(boundedAddress)) {
                device.write(boundedAddress, byteData);
                return;
            }
        }
    }
}