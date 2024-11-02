package dev.faiths.component;

import dev.faiths.event.Listener;
import dev.faiths.event.impl.MoveEvent;
import dev.faiths.event.impl.TickUpdateEvent;
import net.minecraft.client.Minecraft;

import static dev.faiths.utils.IMinecraft.mc;

public class MovementComponent implements Listener {
    public static final MovementComponent INSTANCE = new MovementComponent();
    public static boolean cancelMove = false;
    public static boolean forceStuck = false;

    public static void cancelMove() {
        cancelMove(false);
    }

    public static void cancelMove(boolean force) {
        if (mc.thePlayer == null) {
            return;
        }
        if (cancelMove) {
            return;
        }
        forceStuck = force;
        cancelMove = true;
    }

    public static void resetMove() {
        cancelMove = false;
        mc.skipTicks = 0;
    }

    public void onMove(MoveEvent event) {
        if (cancelMove) {
            if (forceStuck) {
                return;
            }
            if (mc.skipTicks > 0) {
                return;
            }
            mc.skipTicks = 20;
        }
    }


    public void onTick(TickUpdateEvent event) {
        if (cancelMove && forceStuck) {
            mc.skipTicks = 20;
        }
    }

//    public void onPacketReceive(PacketReceiveEvent event) {
//        if (event.getPacket() instanceof S12PacketEntityVelocity s12 && s12.getEntityID() == mc.thePlayer.getEntityId() && cancelMove && !forceStuck) {
//            if (mc.theWorld.skiptick <= 0) {
//                mc.theWorld.skiptick--;
//                return;
//            }
//            mc.theWorld.skiptick = 0;
//        }
//    }

    @Override
    public boolean isAccessible() {
        return true;
    }
}