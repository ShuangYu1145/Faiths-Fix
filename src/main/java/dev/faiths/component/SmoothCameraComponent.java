package dev.faiths.component;

import dev.faiths.event.Handler;
import dev.faiths.event.Listener;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.utils.MSTimer;

import static dev.faiths.utils.IMinecraft.mc;

public class SmoothCameraComponent implements Listener {

    public static double y;
    public static MSTimer stopWatch = new MSTimer();
    public static SmoothCameraComponent INSTANCE;

    public SmoothCameraComponent() {
        INSTANCE = this;
    }

    public static void setY(double y) {
        stopWatch.reset();
        SmoothCameraComponent.y = y;
    }

    public static void setY() {
        if (stopWatch.check(60)) SmoothCameraComponent.y = mc.thePlayer.lastTickPosY;
        stopWatch.reset();
    }

    public final Handler<MotionEvent> onPreMotion = event -> {
        if (event.isPost()) return;
        if (stopWatch.check(60)) return;
        mc.thePlayer.cameraYaw = 0;
        mc.thePlayer.cameraPitch = 0;
    };

    @Override
    public boolean isAccessible() {
        return true;
    }
}
