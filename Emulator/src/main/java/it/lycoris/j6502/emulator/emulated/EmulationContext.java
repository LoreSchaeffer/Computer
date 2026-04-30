package it.lycoris.j6502.emulator.emulated;

import it.lycoris.j6502.emulator.hardware.LogicComponent;

/**
 * A highly optimized, zero-allocation context for hardware emulation.
 * Replaces standard Java collections (ArrayDeque, HashSet) with primitive
 * array structures to maximize CPU cache hit rates and eliminate
 * Garbage Collection overhead during the high-frequency main loop.
 */
public class EmulationContext {
    // Capacity MUST be a power of 2 to allow fast bitwise masking instead of modulo division
    private static final int QUEUE_CAPACITY = 131072;
    private static final int QUEUE_MASK = QUEUE_CAPACITY - 1;

    // Ring Buffer for the execution queue
    private final LogicComponent[] queue = new LogicComponent[QUEUE_CAPACITY];
    private int head = 0;
    private int tail = 0;

    // Custom Identity Set using a flat array (Linear Probing for collision resolution)
    private final LogicComponent[] scheduledSet = new LogicComponent[QUEUE_CAPACITY];

    /**
     * Schedules a logic component for execution if it is not already in the queue.
     *
     * @param comp The hardware component to schedule.
     */
    public void schedule(LogicComponent comp) {
        // Use identity hash code for fast memory-address-based hashing
        int hash = System.identityHashCode(comp);
        int index = hash & QUEUE_MASK;

        // Linear probing to find the exact component or the first empty slot
        while (this.scheduledSet[index] != null) {
            if (this.scheduledSet[index] == comp) {
                return; // Component is already scheduled, exit early
            }
            index = (index + 1) & QUEUE_MASK;
        }

        // Empty slot found: add the component to the deduplication set
        this.scheduledSet[index] = comp;

        // Enqueue the component in the Ring Buffer
        this.queue[this.tail] = comp;
        this.tail = (this.tail + 1) & QUEUE_MASK;
    }

    /**
     * Drains the queue and executes the update routine on all scheduled components.
     */
    public void run() {
        while (this.head != this.tail) {
            // Dequeue the next component
            LogicComponent comp = this.queue[this.head];
            this.queue[this.head] = null; // Clear the reference to avoid memory leaks
            this.head = (this.head + 1) & QUEUE_MASK;

            // Remove the component from the scheduled set
            int hash = System.identityHashCode(comp);
            int index = hash & QUEUE_MASK;

            while (this.scheduledSet[index] != null) {
                if (this.scheduledSet[index] == comp) {
                    this.scheduledSet[index] = null;
                    break;
                }
                index = (index + 1) & QUEUE_MASK;
            }

            // Execute hardware logic
            comp.update(this);
        }
    }
}
