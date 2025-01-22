package dev.faiths.event.impl;

import dev.faiths.event.Event;
import lombok.*;

import static dev.faiths.utils.IMinecraft.mc;


@Getter
public class EyeHeightEvent extends Event {
    private double y;
    private boolean set;

    public EyeHeightEvent(double eyeHeight) {
        setEyeHeight(eyeHeight);
    }

    public double getEyeHeight() {
        return 1.62 - (mc.thePlayer.lastTickPosY +
                (((mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * mc.getTimer().renderPartialTicks)) - y);
    }

    public void setEyeHeight(double targetEyeHeight) {
        this.y = targetEyeHeight - 1.62 + mc.thePlayer.lastTickPosY +
                ((mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * (double) mc.getTimer().renderPartialTicks);
    }

    public void setY(double y) {
        this.y = y;
        this.set = true;
    }
}