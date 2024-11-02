package dev.faiths.event.impl;

import dev.faiths.event.CancelableEvent;

public class MoveInputEvent extends CancelableEvent {
    private float forward;
    private float strafe;
    private boolean jump;
    private boolean sneak;
    private double sneakSlowDownMultiplier;

    public MoveInputEvent(float forward, float strafe, boolean jump, boolean sneak, double sneakSlowDownMultiplier) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneak = sneak;
        this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
    }

    
    
    public float getForward() {
        return this.forward;
    }

    
    public float getStrafe() {
        return this.strafe;
    }

    
    public boolean isJump() {
        return this.jump;
    }

    
    public boolean isSneak() {
        return this.sneak;
    }

    
    public double getSneakSlowDownMultiplier() {
        return this.sneakSlowDownMultiplier;
    }

    
    public void setForward(final float forward) {
        this.forward = forward;
    }

    
    public void setStrafe(final float strafe) {
        this.strafe = strafe;
    }

    
    public void setJump(final boolean jump) {
        this.jump = jump;
    }
}
    
