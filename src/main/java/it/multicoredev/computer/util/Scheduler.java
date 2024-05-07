package it.multicoredev.computer.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    private Scheduler() {
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit timeUnit) {
        return SCHEDULER.scheduleAtFixedRate(task, initialDelay, period, timeUnit);
    }
}
