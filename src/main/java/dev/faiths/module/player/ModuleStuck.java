package dev.faiths.module.player;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.world.ModuleScaffold;
import dev.faiths.utils.player.Rotation;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import javax.vecmath.Vector2f;

import java.util.LinkedList;
import java.util.Objects;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleStuck extends CheatModule {
    public ModuleStuck() {
        super("Stuck", Category.PLAYER);
    }

    private double x, y, z, motionX, motionY, motionZ;
    private boolean onGround = false;
    private Vector2f rotation;
    private boolean delayingC0F = false;
    public boolean thrown = false;
    private boolean closing = false;
    private boolean s08Flag = false;

    private LinkedList<Packet> packets = new LinkedList<>();

    private int c08s;


    @Override
    public void onEnable() {
        Faiths.moduleManager.getModule(ModuleBlink.class).setState(false);
        Faiths.moduleManager.getModule(ModuleScaffold.class).setState(false);
        if (mc.thePlayer == null) return;
        onGround = mc.thePlayer.onGround;
        x = mc.thePlayer.posX;
        y = mc.thePlayer.posY;
        z = mc.thePlayer.posZ;
        motionX = mc.thePlayer.motionX;
        motionY = mc.thePlayer.motionY;
        motionZ = mc.thePlayer.motionZ;
        rotation = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);

        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float gcd = f * f * f * 1.2F;

        rotation.x -= rotation.x % gcd;
        rotation.y -= rotation.y % gcd;

        delayingC0F = true;
        thrown = false;
        c08s = 0;
        s08Flag = false;
    }

    @Override
    public void onDisable() {
//        if (antiSB.getValue() && !mc.thePlayer.onGround) {
//            DebugUtil.log("You can't disable this module now!");
//            this.setState(true);
//        }
        //if (!closing) {
        //    DebugUtil.log("You can't disable this module now!");
        //    this.setState(true);
        //}
        c08s = 0;

        mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX + 10000, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround), true);
        while (!Faiths.moduleManager.getModule(ModuleStuck.class).packets.isEmpty()) {
            mc.getNetHandler().getNetworkManager().sendPacket(Faiths.moduleManager.getModule(ModuleStuck.class).packets.poll(), true);
            if (packets.size() % 2 == 1 && !s08Flag) {
                mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 10000, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.onGround), true);
            }
        }
        delayingC0F = false;
//        return true;
    }

    private Handler<PacketEvent> packetEventHandler = event -> {
        ItemStack stack = mc.thePlayer.getHeldItem();

        if (event.getPacket() instanceof S18PacketEntityTeleport) {
            S18PacketEntityTeleport packet = (S18PacketEntityTeleport) event.getPacket();
            if (packet.getEntityId() == mc.thePlayer.getEntityId()) {
                toggle();
            }
        }

        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            onS08();
        }

        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            c08s++;
            if (stack == null) {
                event.setCancelled(true);
                return;
            }

            C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) event.getPacket();
            Vector2f current = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);

            float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float gcd = f * f * f * 1.2F;

            current.x -= current.x % gcd;
            current.y -= current.y % gcd;
            if (rotation.equals(current)) {
                return;
            }
            rotation = current;

            event.setCancelled(stack.getItem() instanceof ItemEnderPearl);
            if (stack.getItem() instanceof ItemFood) {
                if (!Objects.equals(packet.getPosition(), new BlockPos(-1, -1, -1))) {
                    event.setCancelled(true);
                }
            }
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(current.x, current.y, onGround));
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
        }
        if (event.getPacket() instanceof C03PacketPlayer) {
            event.setCancelled(true);
        }
        if (event.getPacket() instanceof C0FPacketConfirmTransaction) {
            event.setCancelled(true);
            packets.add((C0FPacketConfirmTransaction) event.getPacket());
        }

        if (event.getPacket() instanceof C07PacketPlayerDigging) {

            if (stack != null && stack.getItem() instanceof ItemBow) {
                event.setCancelled(true);
                Vector2f current = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);

                float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
                float gcd = f * f * f * 1.2F;

                current.x -= current.x % gcd;
                current.y -= current.y % gcd;
                if (rotation.equals(current)) {
                    return;
                }
                rotation = current;
                mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(current.x, current.y, onGround));
                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            }
        }
    };


    private Handler<UpdateEvent> updateEventHandler = event -> {
        if (c08s >= 20) {
            c08s = 0;
            new Thread(() -> {
                setState(false);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                setState(true);
            }).start();
        }
        //PacketUtil.sendPacketNoEvent(new CPacketSteerBoat(true, true));
        mc.thePlayer.motionX = 0;
        mc.thePlayer.motionY = 0;
        mc.thePlayer.motionZ = 0;
        mc.thePlayer.setPosition(x, y, z);
    };

    public static boolean isStuck() {
        return false;
    }

    public static void onS08() {
        Faiths.moduleManager.getModule(ModuleStuck.class).s08Flag = true;

        Faiths.moduleManager.getModule(ModuleStuck.class).closing = true;
        Faiths.moduleManager.getModule(ModuleStuck.class).setState(false);
        Faiths.moduleManager.getModule(ModuleStuck.class).closing = false;
    }

    public static void throwPearl(Vector2f current) {
        if (!Faiths.moduleManager.getModule(ModuleStuck.class).getState()) return;

        mc.thePlayer.rotationYaw = current.x;
        mc.thePlayer.rotationPitch = current.y;

        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float gcd = f * f * f * 1.2F;

        current.x -= current.x % gcd;
        current.y -= current.y % gcd;
        if (!Faiths.moduleManager.getModule(ModuleStuck.class).rotation.equals(current)) {
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(current.x, current.y, Faiths.moduleManager.getModule(ModuleStuck.class).onGround));
        }
        Faiths.moduleManager.getModule(ModuleStuck.class).rotation = current;
        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
    }
}
