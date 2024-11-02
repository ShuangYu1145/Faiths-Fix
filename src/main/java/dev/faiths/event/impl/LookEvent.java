package dev.faiths.event.impl;

import dev.faiths.event.Event;
import org.lwjgl.util.vector.Vector2f;

public class LookEvent extends Event {
    private Vector2f rotation;



    public LookEvent(Vector2f rotation) {
        this.rotation = rotation;
    }

    
    public Vector2f getRotation() {
        return this.rotation;
    }

    
    
    public void setRotation(final Vector2f rotation) {
        this.rotation = rotation;
    }
}
    
