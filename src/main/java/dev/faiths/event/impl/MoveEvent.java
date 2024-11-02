package dev.faiths.event.impl;

import dev.faiths.event.Event;

public class MoveEvent extends Event {
    private double x, y, z;
    private boolean isSafeWalk = false;

    public MoveEvent(final double x, final double y, final double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public boolean isSafeWalk() {
        return isSafeWalk;
    }

    public void setSafeWalk(boolean safeWalk) {
        isSafeWalk = safeWalk;
    }
}
