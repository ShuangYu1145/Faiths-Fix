package com.github.stachelbeere1248.zombiesutils.timer;

import net.minecraft.client.Minecraft;

public class Timer {

    private long startTick;
    private int roundStart;

    public Timer() {
        this.startTick = this.getCurrentTick();
    }

    public void correctStartTick() {
        this.startTick = this.getCurrentTick() - 200;
    }
    void split() {
        this.roundStart = this.getGameTime();
    }
    public int getGameTime() {
        return (int) (getCurrentTick() - startTick);
    }

    public short getRoundTime() {
        return (short) (getGameTime() - roundStart);
    }

    private long getCurrentTick() {
        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().theWorld == null) return 0;
        return Minecraft.getMinecraft().theWorld.getTotalWorldTime();
    }

}
