package it.multicoredev.computer.v2.components;

import it.multicoredev.computer.constants.Debug;
import it.multicoredev.computer.util.BitStrings;
import it.multicoredev.computer.util.ComponentIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Component {
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final int ID;
    protected final boolean[] in;
    protected final boolean[] out;
    private final List<Component> outboundConnections = new ArrayList<>();

    public Component(int inSize, int outSize) {
        this.in = new boolean[inSize];
        this.out = new boolean[outSize];
        ID = ComponentIds.nextId();
        if (!Debug.LOG_DISABLED.contains(getClass())) LOG.debug("{}({})\t\tCREATED", getClass().getSimpleName(), ID);
    }

    public Component in(boolean... in) {
        if (in.length != this.in.length) throw new IllegalArgumentException("Inputs must be " + this.in.length + " bits long");

        System.arraycopy(in, 0, this.in, 0, in.length);
        run();

        return this;
    }

    public final boolean[] in() {
        return in;
    }

    public final boolean[] out() {
        return out;
    }

    protected final void run() {
        UUID runId = UUID.randomUUID();

        logStatus("BT", runId);
        update();
        logStatus("AT", runId);

        outboundConnections.forEach(component -> {
            component.in(out);
        });
    }

    protected abstract void update();

    public void connect(Component component) {
        outboundConnections.add(component);
    }

    public void disconnect(Component component) {
        outboundConnections.remove(component);
    }

    private void logStatus(String time, UUID runId) {
        if (Debug.LOG_DISABLED.contains(getClass())) return;

        String inSpaces = " ".repeat(16 - in.length);
        String outSpaces = " ".repeat(16 - out.length);

        LOG.debug(
                "{}({})\t\t{} -> [{}]{}\tOUT [{}]{}\t{}",
                getClass().getSimpleName(),
                ID,
                time,
                BitStrings.toString(in),
                inSpaces,
                BitStrings.toString(out),
                outSpaces,
                runId
        );
    }
}
