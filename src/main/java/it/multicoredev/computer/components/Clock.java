package it.multicoredev.computer.components;

import it.multicoredev.computer.util.Scheduler;
import it.multicoredev.computer.util.ClockListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Clock {
    private long period;
    private TimeUnit timeUnit;
    private boolean status;
    private ScheduledFuture<?> task;
    private final List<ClockListener> listeners = new ArrayList<>();

    public Clock(long period, TimeUnit timeUnit) {
        this.period = period;
        this.timeUnit = timeUnit;
    }

    public void start() {
        task = Scheduler.scheduleAtFixedRate(new ScheduledTask(), 0, period, timeUnit);
    }

    public void stop() {
        task.cancel(false);
        task = null;
    }

    public void addListener(ClockListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ClockListener listener) {
        listeners.remove(listener);
    }

    private class ScheduledTask implements Runnable {

        @Override
        public void run() {
            status = !status;
            for (ClockListener listener : listeners) listener.clk(status);
        }
    }
}
