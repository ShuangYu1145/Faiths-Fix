package dev.faiths.module.player;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.WorldEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.world.ModuleScaffold;
import dev.faiths.utils.MSTimer;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.utils.player.PredictPlayer;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueInt;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import java.util.ArrayList;

import static dev.faiths.utils.IMinecraft.mc;

public class ModuleAntiVoid extends CheatModule {
    private final ValueInt pullbackTime = new ValueInt("PullbackTime", 800, 500, 2000);
    private final ValueBoolean onlyVoid = new ValueBoolean("OnlyVoid", false);
    public double[] lastGroundPos = new double[3];
    public static MSTimer timer = new MSTimer();
    public static ArrayList<Packet> packets = new ArrayList<>();

    public ModuleAntiVoid() {
        super("AntiVoid", Category.PLAYER);
    }

    private final Handler<WorldEvent> worldEventHandler = event -> {
        if (!packets.isEmpty()) {
            for (Packet packet : packets)
                mc.getNetHandler().sendPacketNoEvent(packet);
            packets.clear();
        }
    };

    private final Handler<PacketEvent> packetEventHandler = e -> {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.thePlayer.capabilities.allowFlying || mc.thePlayer.ticksExisted < 20 || Faiths.moduleManager.getModule(ModuleScaffold.class).getState())
            return;
        final PredictPlayer predictPlayer = new PredictPlayer();


        if (e.getType() == PacketEvent.Type.SEND) {
            if (!packets.isEmpty() && mc.thePlayer.ticksExisted < 100)
                packets.clear();

            if (e.getPacket() instanceof C03PacketPlayer) {
                final C03PacketPlayer c03 = (C03PacketPlayer) e.getPacket();

                if ((predictPlayer.findCollision(50) == null || !onlyVoid.getValue()) && !PlayerUtils.isBlockUnder(mc.thePlayer)) {
                    e.setCancelled(true);
                    packets.add(c03);

                    if (timer.delay(pullbackTime.getValue()) && !packets.isEmpty()) {
                        mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(lastGroundPos[0], lastGroundPos[1] - 1, lastGroundPos[2], true));
                    }

                } else {
                    lastGroundPos[0] = mc.thePlayer.posX;
                    lastGroundPos[1] = mc.thePlayer.posY;
                    lastGroundPos[2] = mc.thePlayer.posZ;

                    if (!packets.isEmpty()) {
                        for (Packet packet : packets)
                            mc.getNetHandler().sendPacketNoEvent(packet);
                        packets.clear();
                    }

                    timer.reset();
                }
            } else if ((predictPlayer.findCollision(50) == null || !onlyVoid.getValue()) && !PlayerUtils.isBlockUnder(mc.thePlayer)) {
                packets.add(e.getPacket());
                e.setCancelled(true);
            }
        } else {
            if (e.getPacket() instanceof S08PacketPlayerPosLook && packets.size() > 1) {
                for (Packet packet : packets) {
                    if (!(packet instanceof C03PacketPlayer))
                        mc.getNetHandler().sendPacketNoEvent(packet);
                }
                packets.clear();
            }
        }
    };
}
