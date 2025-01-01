package dev.faiths.module.combat;

import dev.faiths.Faiths;
import dev.faiths.component.MovementComponent;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.*;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.player.ModuleBlink;
import dev.faiths.module.render.ModuleHUD;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.ClientUtils;
import dev.faiths.utils.PacketUtils;
import dev.faiths.utils.player.BlinkUtils;
import dev.faiths.utils.render.RoundedUtil;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueInt;
import io.netty.buffer.Unpooled;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.*;
import dev.faiths.utils.player.InventoryUtil;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.status.client.C00PacketServerQuery;

import java.awt.*;
import java.util.Hashtable;
import java.util.LinkedList;

import static dev.faiths.module.combat.ModuleKillAura.target;
import static dev.faiths.utils.IMinecraft.mc;


@SuppressWarnings("unused")
public class ModuleGapple extends CheatModule {
    public ValueFloat c03s = new ValueFloat("C03Eat",32f ,1f, 40f);
    public ValueInt speed = new ValueInt("Speed", 3,3,10);

    public ValueBoolean auto = new ValueBoolean("AutoDis", false);
    private int gappleSlot = -1;

    private LinkedList<Packet<?>> packets = new LinkedList<>();

    public static int storedC03 = 0;
    public static boolean eating = false;
    public static boolean sending = false;

    public static boolean restart = false;

    public static boolean isS12;

    public ModuleGapple() {
        super("Gapple", Category.COMBAT);
    }


    @Override
    public void onEnable() {
        packets.clear();
        storedC03 = 0;
        isS12 = false;
        this.gappleSlot = InventoryUtil.findItem2(36, 45, Items.golden_apple);
        if (this.gappleSlot != -1) {
            this.gappleSlot -= 36;
        }
    }

    @Override
    public void onDisable() {
        poll();
    }

    private Handler<UpdateEvent> updateEventHandler = event -> {
        if (gappleSlot == -1) setState(false);
    };

    private Handler<MotionEvent> motionEventHandler = event -> {
        if (gappleSlot >= 0) {
            if (event.isPre()) {
                if (storedC03 > c03s.getValue().intValue()) {
                    PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(gappleSlot));
                    PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                    poll();
                    PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));

                    if (auto.getValue()) {
                        setState(false);
                    } else {
                        storedC03 = 0;
                        packets.clear();
                    }
                } else {
                    if (mc.thePlayer.ticksExisted % speed.getValue() == 0) {
                        while (!packets.isEmpty()) {
                            Packet<?> packet = packets.poll();

                            if (packet instanceof C03PacketPlayer) storedC03--;

                            if (packet instanceof C01PacketChatMessage) break;

                            PacketUtils.sendPacketNoEvent(packet);
                        }
                    }
                }
            }
            if (event.isPost()) {
                packets.add(new C01PacketChatMessage("sb"));
            }
        }
    };


    private Handler<PacketEvent> packetEventHandler = event -> {

        if (mc.thePlayer == null || mc.theWorld == null) return;


        Packet<?> packet = event.getPacket();
        if (gappleSlot >= 0) {
            if (PacketUtils.isCPacket(packet)) {
                if (packet instanceof C00PacketKeepAlive || packet instanceof C00Handshake || packet instanceof C00PacketLoginStart || packet instanceof C00PacketServerQuery || packet instanceof C01PacketChatMessage)
                    return;

                if (!(packet instanceof C08PacketPlayerBlockPlacement || packet instanceof C0EPacketClickWindow || packet instanceof C0DPacketCloseWindow || packet instanceof C07PacketPlayerDigging)) {
                    packets.add(packet);
                    event.setCancelled(true);
                }

                if (packet instanceof C03PacketPlayer) {
                    storedC03++;
                }
            } else {
                if (packet instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity velocity = (S12PacketEntityVelocity) event.getPacket();

                    if (velocity.getEntityID() == mc.thePlayer.getEntityId()) {
                        isS12 = true;
                    }
                }
            }
        }
    };



    private Handler<Render2DEvent> render2DEventHandler = event -> {
        ScaledResolution sr = new ScaledResolution(mc);
        float target = (120.0f * ((float) storedC03 / c03s.getValue().intValue())) * ((float) 100 / 120);
        int startX = sr.getScaledWidth() / 2 - 68;
        int startY = sr.getScaledHeight() / 2 + 100;
        RoundedUtil.drawGradientRound(startX + 10, (float) (startY + 7.5), 120.0f, 6.0f, 3.0f, new Color(0, 0, 0, 200), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150));
        RoundedUtil.drawGradientRound(startX + 10, (float) (startY + 7.5), Math.min(target, 120.0f), 6.0f, 3.0f,new Color(241, 239, 232, 170), new Color(179, 179, 179, 50), new Color(241, 249, 249, 170), new Color(241, 239, 232, 170));
    };

    private void poll() {
        while (!packets.isEmpty()) {
            Packet<?> p = packets.poll();

            if (p instanceof C01PacketChatMessage) continue;

            PacketUtils.sendPacketNoEvent(p);
        }
    }
}
