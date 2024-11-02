package dev.faiths.event;

public interface Handler<T extends Event> {
    void invoke(final T event);
}
