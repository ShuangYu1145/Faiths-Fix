package dev.faiths.event.impl;

import dev.faiths.event.Event;

public class MotionEvent extends Event {
    public EventState eventState;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private boolean ground;

    public static float prevRenderYaw;
    public static float prevRenderPitch;


    public MotionEvent(double x, double y, double z, float yaw, float pitch, boolean ground, EventState eventState) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.ground = ground;
        this.eventState = eventState;
    }


    public boolean isPre() {
        return this.eventState == EventState.PRE;
    }

    public boolean isPost() {
        return this.eventState == EventState.POST;
    }


    public enum EventState {
        PRE, POST
    }


    public EventState getEventState() {
        return this.eventState;
    }


    public double getX() {
        return this.x;
    }


    public double getY() {
        return this.y;
    }


    public double getZ() {
        return this.z;
    }


    public float getYaw() {
        return this.yaw;
    }


    public float getPitch() {
        return this.pitch;
    }


    public boolean isGround() {
        return this.ground;
    }


    public void setEventState(final EventState eventState) {
        this.eventState = eventState;
    }


    public void setX(final double x) {
        this.x = x;
    }


    public void setY(final double y) {
        this.y = y;
    }


    public void setZ(final double z) {
        this.z = z;
    }


    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }


    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }


    public void setGround(final boolean ground) {
        this.ground = ground;
    }

}
