package dev.faiths.event.impl;

import dev.faiths.event.CancelableEvent;
import dev.faiths.utils.player.PlayerUtils;

public class StrafeEvent extends CancelableEvent {
    public float strafe;
    public float forward;
    public float friction;
    public float yaw;

    public StrafeEvent(float Strafe, float Forward, float Friction, float Yaw) {
        this.strafe = Strafe;
        this.forward = Forward;
        this.friction = Friction;
        this.yaw = Yaw;
    }

    public void setSpeed(final double speed) {
        setFriction((float) (getForward() != 0 && getStrafe() != 0 ? speed * 0.98F : speed));
        PlayerUtils.stop();
    }

    
    
    public float getStrafe() {
        return this.strafe;
    }

    
    public float getForward() {
        return this.forward;
    }

    
    public float getFriction() {
        return this.friction;
    }

    
    public float getYaw() {
        return this.yaw;
    }

    
    public void setStrafe(final float strafe) {
        this.strafe = strafe;
    }

    
    public void setForward(final float forward) {
        this.forward = forward;
    }

    
    public void setFriction(final float friction) {
        this.friction = friction;
    }

    
    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }
    
}
