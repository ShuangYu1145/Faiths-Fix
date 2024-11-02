package dev.faiths.event.impl;

import dev.faiths.event.Event;

public class Render3DEvent extends Event {
    private final float partialTicks;

    public Render3DEvent(final float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
