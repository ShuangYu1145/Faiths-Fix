package com.github.stachelbeere1248.zombiesutils.game.windows;

public class Window {
    private final short[] xyz = new short[3];
    private final int id;
    private boolean isActive;

    public Window(int id, int x, int y, int z) {
        this.id = id;
        xyz[0] = (short) x;
        xyz[1] = (short) y;
        xyz[2] = (short) z;
    }

    public int getID() {
        return id;
    }

    public short[] getXYZ() {
        return xyz;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    private void rotateCounterClockwise() {
        final short x = xyz[0], z = xyz[2];
        xyz[0] = (short) -z;
        xyz[2] = x;
    }

    private void mirrorBoth() {
        xyz[0] = (short) -xyz[0];
        xyz[2] = (short) -xyz[2];
    }

    private void rotateClockwise() {
        final short x = xyz[0], z = xyz[2];
        xyz[0] = z;
        xyz[2] = (short) -x;
    }

    public void rotate(int rotations) {
        rotations %= 4;
        switch (rotations) {
            case -3:
            case 1:
                rotateCounterClockwise();
                break;
            case -2:
            case 2:
                mirrorBoth();
                break;
            case -1:
            case 3:
                rotateClockwise();
                break;
            case 0:
                break;
        }
    }

    public void mirrorX() {
        xyz[0] = (short) -xyz[0];
    }

    public void mirrorZ() {
        xyz[2] = (short) -xyz[2];
    }
}
