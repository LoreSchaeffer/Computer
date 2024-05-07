package it.multicoredev.computer.util.components;

import it.multicoredev.computer.util.listeners.ClockListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Clock {
    private int period;
    private boolean clock = true;
    private ScheduledExecutorService clockScheduler;
    private List<ClockListener> listeners = new ArrayList<>();

    public Clock(int period) {
        this.period = period;
        clockScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        clockScheduler.scheduleAtFixedRate(() -> {
            clock = !clock;
            for (ClockListener listener : listeners) listener.clock(clock);
        }, 0, period, TimeUnit.MILLISECONDS);
    }

    public void addListener(ClockListener listener) {
        listeners.add(listener);
    }
}
