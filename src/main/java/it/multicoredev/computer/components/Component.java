package it.multicoredev.computer.components;

import it.multicoredev.computer.constants.Debug;
import it.multicoredev.computer.util.BitStrings;
import it.multicoredev.computer.util.ComponentIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class Component {
    private final Logger LOG = LoggerFactory.getLogger(getClass());
    private final int ID;
    protected final boolean[] in;
    protected final boolean[] out;
    private final Map<Object, Consumer<boolean[]>> outboundConnections = new HashMap<>();

    public Component(int inSize, int outSize) {
        this.in = new boolean[inSize];
        this.out = new boolean[outSize];
        ID = ComponentIds.nextId();
    }

    public void in(boolean... in) {
        if (in.length != this.in.length) throw new IllegalArgumentException("Inputs must be " + this.in.length + " bits long");

        System.arraycopy(in, 0, this.in, 0, in.length);
        run();
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

        outboundConnections.forEach((owner, consumer) -> consumer.accept(out));
    }

    protected abstract void update();

    public void connect(Component component) {
        outboundConnections.put(component, component::in);
    }

    public void connect(Object owner, Consumer<boolean[]> consumer) {
        outboundConnections.put(owner, consumer);
    }

    public void disconnect(Object owner) {
        outboundConnections.remove(owner);
    }

    private void logStatus(String time, UUID runId) {
        if (Debug.LOG_DISABLED.contains(getClass())) return;

        String nameSpaces = " ".repeat(Math.max(40 - getClass().getSimpleName().length() - String.valueOf(ID).length(), 0));
        String inSpaces = " ".repeat(32 - in.length);
        String outSpaces = " ".repeat(32 - out.length);

        LOG.debug(
                "{}({}){}\t{} -> [{}]{}\tOUT [{}]{}\t{}",
                getClass().getSimpleName(),
                ID,
                nameSpaces,
                time,
                BitStrings.toString(in),
                inSpaces,
                BitStrings.toString(out),
                outSpaces,
                runId
        );
    }
}
