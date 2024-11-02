package dev.faiths.event.impl;

import dev.faiths.event.CancelableEvent;

public class JumpEvent extends CancelableEvent {
    public float motion;
    public float yaw;

    public JumpEvent(float yaw, float motion) {
        this.yaw = yaw;
        this.motion = motion;
    }

    public float getMotion() {
        return this.motion;
    }

    public float getYaw() {
        return this.yaw;
    }

    public void setMotion(final float motion) {
        this.motion = motion;
    }

    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }
}
