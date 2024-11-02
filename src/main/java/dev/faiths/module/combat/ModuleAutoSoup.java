package dev.faiths.module.combat;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.EntityHealthUpdateEvent;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.TimerUtil;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueInt;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.apache.commons.lang3.RandomUtils;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleAutoSoup extends CheatModule {
    private final ValueBoolean postValue = new ValueBoolean("Post",true);
    private final ValueInt health = new ValueInt("Health", 15, 0, 20);
    private final ValueInt minDelay = new ValueInt("Min Delay", 300, 0, 1000);
    private final ValueInt maxDelay = new ValueInt("Max Delay", 500, 0, 1000);
    private final ValueBoolean dropBowl = new ValueBoolean("Drop Bowl",  true);
    private final ValueBoolean Legit = new ValueBoolean("Legit",false);
    private final TimerUtil timer = new TimerUtil();
    private boolean switchBack;
    private long decidedTimer;
    private int soup = -37;

    public ModuleAutoSoup(){
        super("AutoSoup", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        switchBack = false;
        soup = -37;
    }

    private final Handler<MotionEvent> motionEventHandler = event -> {
        if ((postValue.getValue() && event.isPost()) || (!postValue.getValue() && event.isPre())) {
            if (switchBack) {
                mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                if (dropBowl.getValue()) {
                    mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                }
                if (Legit.getValue()) {
                    mc.playerController.updateController();
                } else {
                    mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
                switchBack = false;
                return;
            }

            if (timer.delay(decidedTimer)) {
                if (mc.thePlayer.ticksExisted > 10 && mc.thePlayer.getHealth() < health.getValue().intValue()) {
                    soup = PlayerUtils.findSoup() - 36;

                    if (soup != -37) {
                        if (Legit.getValue()) {
                            mc.thePlayer.inventory.currentItem = soup;
                            mc.gameSettings.keyBindUseItem.setPressed(true);
                        } else {
                            mc.getNetHandler().getNetworkManager().sendPacket(new C09PacketHeldItemChange(soup));
                            mc.getNetHandler().getNetworkManager().sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(soup)));
                        }
                        switchBack = true;
                    } else {
                        int soupInInventory = PlayerUtils.findItem(9, 36, Items.mushroom_stew);
                        if (soupInInventory != -1 && PlayerUtils.hasSpaceHotBar()) {

                            boolean openInventory = !(mc.currentScreen instanceof GuiInventory);
                            if (openInventory) {
                                mc.thePlayer.setSprinting(false);
                                mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
                            }

                            mc.playerController.windowClick(0, soupInInventory, 0, 1, mc.thePlayer);

                            if (openInventory) {
                                mc.getNetHandler().addToSendQueue(new C0DPacketCloseWindow());
                            }
                        }
                    }

                    final int delayFirst = (int) Math.floor(Math.min(minDelay.getValue().intValue(), maxDelay.getValue().intValue()));
                    final int delaySecond = (int) Math.ceil(Math.max(minDelay.getValue().intValue(), maxDelay.getValue().intValue()));

                    decidedTimer = RandomUtils.nextInt(delayFirst, delaySecond);
                    timer.reset();
                }
            }
        }
    };
}
