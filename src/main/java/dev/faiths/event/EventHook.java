package dev.faiths.event;

public class EventHook<T extends Event> {
    private final Listener listener;
    private final Handler<T> handler;

    public EventHook(final Listener listener, final Handler<T> handler) {
        this.listener = listener;
        this.handler = handler;
    }

    public Handler<T> getHandler() {
        return handler;
    }

    public Listener getListener() {
        return listener;
    }
}
