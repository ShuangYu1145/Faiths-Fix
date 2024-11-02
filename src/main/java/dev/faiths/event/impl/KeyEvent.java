package dev.faiths.event.impl;

import dev.faiths.event.Event;

public class KeyEvent extends Event {
    private final int key;

    public int getKey() {
        return key;
    }

    public KeyEvent(int key) {
        this.key = key;
    }
}
