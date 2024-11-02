package dev.faiths.module.player;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.BlockPlaceEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.player.InventoryUtil;
import dev.faiths.value.ValueBoolean;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import wtf.pub.CalculateThread;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleAutoPearl extends CheatModule {
    private ValueBoolean noAutoThrow = new ValueBoolean("noAutoThrow", false);
    private int bestPearlSlot;
    private boolean flag;
    private boolean flag2;
    private CalculateThread calculateThread1;

    public ModuleAutoPearl() {
        super("AutoPearl", Category.WORLD);
    }

    private Handler<UpdateEvent> updateEventHandler = event -> {
        if (mc.thePlayer.fallDistance >= 6 && isVoid() && noAutoThrow.getValue()) {
            Faiths.moduleManager.getModule(ModuleStuck.class).setState(true);
        }
        if (noAutoThrow.getValue()) return;
        if (mc.thePlayer.onGround && !Faiths.moduleManager.getModule(ModuleStuck.class).getState() && mc.thePlayer.fallDistance == 0 && flag2) {
            flag2 = false;
        }
        try {
            if (mc.thePlayer.fallDistance >= 6 && isVoid() && !flag2) {
                for (int slot = InventoryUtil.INCLUDE_ARMOR_BEGIN; slot < InventoryUtil.END; slot++) {
                    ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(slot).getStack();

                    if (stack != null) {
                        if (stack.getItem() instanceof ItemEnderPearl) {
                            if (slot >= InventoryUtil.ONLY_HOT_BAR_BEGIN) {
                                this.bestPearlSlot = slot;

                                if (bestPearlSlot - 36 != -37) {
                                    mc.thePlayer.inventory.currentItem = bestPearlSlot - 36;
                                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                                }
                            }
                        }
                    }
                }
                if(bestPearlSlot == 0) return;
                if(!(mc.thePlayer.inventoryContainer.getSlot(bestPearlSlot).getStack().getItem() instanceof ItemEnderPearl)){
                    return;
                }
                Faiths.moduleManager.getModule(ModuleStuck.class).setState(true);
                calculateThread1 = new CalculateThread(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY,
                        mc.thePlayer.posZ,
                        0, 0
                );
                calculateThread1.start();
                flag = true;
                flag2 = true;
            }
        } catch (Exception ignored) {}
    };

    private Handler<BlockPlaceEvent> eventHandler = event -> {
        if (flag && !calculateThread1.isAlive()) {
            ModuleStuck.throwPearl(calculateThread1.solution);
            flag = false;
        }
    };

    private boolean isVoid() {
        BlockPos oBP = mc.thePlayer.getPosition();
        for (int i = 0; i < 40; i++) {
            if (!mc.theWorld.isAirBlock(oBP.add(0, -i, 0))) {
                return false;
            }
        }

        return true;
    }
}
