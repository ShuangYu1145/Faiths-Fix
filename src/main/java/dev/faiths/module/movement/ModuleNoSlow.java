package dev.faiths.module.movement;

import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.SlowDownEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.combat.ModuleKillAura;
import dev.faiths.utils.HYTUtils;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueMode;
import io.netty.buffer.Unpooled;
import net.minecraft.item.*;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleNoSlow extends CheatModule {
    public ModuleNoSlow() {
        super("NoSlow", Category.MOVEMENT);
    }

    int usingItemTick = 0;

    private static final ValueMode mode = new ValueMode("Mode", new String[]{"Grim", "Watchdog"}, "Watchdog");
    public static ValueBoolean bow = new ValueBoolean("Bow", true).visible(() -> mode.is("Grim"));
    public static ValueBoolean food = new ValueBoolean("Food", true).visible(() -> mode.is("Grim"));
    public static ValueBoolean egapple = new ValueBoolean("EnchantedGApple", true).visible(() -> mode.is("Grim") && food.getValue());
    public static ValueBoolean sword = new ValueBoolean("Sword", true).visible(() -> mode.is("Grim"));

    private final Handler<MotionEvent> motionEventHandler = event -> {

        if (event.isPre()) {
            if (mc.thePlayer.isUsingItem()) {
                usingItemTick++;
            } else {
                usingItemTick = 0;
            }

            if (mode.is("Grim")) {if (mc.thePlayer.isUsingItem() && !mc.thePlayer.isEating() && mc.thePlayer.getItemInUseCount() < 25) {
                    mc.thePlayer.stopUsingItem();
                }
                if(mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem() && !ModuleKillAura.isBlocking){
                    mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                }
                if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow
                        && mc.thePlayer.isUsingItem() && PlayerUtils.isMoving()) {
                    mc.getNetHandler()
                            .addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                    mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("test", new PacketBuffer(Unpooled.buffer())));
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
            }
            if (mode.is("Watchdog")) {
                try {
                    if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword
                            && !ModuleKillAura.shouldBlock()) {
                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1),
                                255, mc.thePlayer.inventory.getCurrentItem(), 0.0F, 0.0F, 0.0F));

                        PacketWrapper useItem = PacketWrapper.create(29, null,
                                Via.getManager().getConnectionManager().getConnections().iterator().next());
                        useItem.write(Type.VAR_INT, 1);
                        PacketUtil.sendToServer(useItem, Protocol1_8To1_9.class, true, true);
                    }

                    if ((mc.thePlayer.isEatingOrDrinking()
                            || (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow))
                            && mc.thePlayer.ticksExisted % 3 == 0) {
                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1),
                                EnumFacing.UP.getIndex(), null, 0, 0, 0));
                    }
                } catch (final Exception ignored) {

                }
            }
        }else{
            if (mode.is("Grim")) {
                if(mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && mc.thePlayer.isUsingItem() && !ModuleKillAura.isBlocking){
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255,
                            mc.thePlayer.inventory.getCurrentItem(), 0.0F, 0.0F, 0.0F));
                    if (!mc.isSingleplayer()) {
                        PacketWrapper use = PacketWrapper.create(29, null,
                                Via.getManager().getConnectionManager().getConnections().iterator().next());
                        use.write(Type.VAR_INT, 1);
                        PacketUtil.sendToServer(use, Protocol1_8To1_9.class, true, true);
                    }
                }
            }
        }
    };

    private final Handler<PacketEvent> packetEventHandler = event -> {
        Packet<?> packet = event.getPacket();

        if (isGapple()) return;

        if (mc.thePlayer == null) {
            return;
        }

        if(mode.is("Grim")) {
            ItemStack heldItem = mc.thePlayer.getHeldItem();
            if (heldItem != null && heldItem.getItem() instanceof ItemFood && food.getValue()) {
                if (packet instanceof C08PacketPlayerBlockPlacement) {
                    C08PacketPlayerBlockPlacement currentPacket = (C08PacketPlayerBlockPlacement) packet;
                    if (currentPacket.getPlacedBlockDirection() == 255 && currentPacket.getPosition().equals(C08PacketPlayerBlockPlacement.field_179726_a) && heldItem.stackSize >= 2 && (!HYTUtils.isHoldingEnchantedGoldenApple(mc.thePlayer) || egapple.getValue())) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    }
                }

                if (packet instanceof S30PacketWindowItems && mc.thePlayer.isUsingItem()) {
                    event.setCancelled(true);
                }

                if (packet instanceof S2FPacketSetSlot && mc.thePlayer.isUsingItem()) {
                    event.setCancelled(true);
                }
            }
        }
    };

    private final Handler<SlowDownEvent> slowDownHandler = event -> {

        if (isGapple()) return;
        if (mode.is("Grim")) {
            if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow
                    && usingItemTick > 1 && bow.getValue()) {
                event.setCancelled(true);
            }
            if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && food.getValue() && mc.thePlayer.getHeldItem().stackSize >= 2 &&
                    (!HYTUtils.isHoldingEnchantedGoldenApple(mc.thePlayer) || egapple.getValue()) && mc.thePlayer.isEating() && usingItemTick > 1) {
                event.setCancelled(true);
            }
            if (mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && sword.getValue()) {
                event.setCancelled(true);
            }
        }

        if (mode.is("Watchdog")) {
            if (!(mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) && !(mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && !mc.thePlayer.onGround)) {
                event.setCancelled(true);
            }
        }
    };
}