package dev.faiths.module.player;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.HYTUtils;
import dev.faiths.utils.TimerUtil;
import dev.faiths.utils.player.ItemUtils;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueInt;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerBrewingStand;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleContainerStealer extends CheatModule {
    private final ValueBoolean postValue = new ValueBoolean("Post", false);
    private final ValueBoolean chest = new ValueBoolean("Chest", true);
    private final ValueBoolean furnace = new ValueBoolean("Furnace", true);
    private final ValueBoolean brewingStand = new ValueBoolean("BrewingStand", true);

    public static final TimerUtil timer = new TimerUtil();
    public static boolean isChest = false;
    public static TimerUtil openChestTimer = new TimerUtil();
    private final ValueInt delay = new ValueInt("StealDelay", 100, 0, 1000);
    private final ValueBoolean trash = new ValueBoolean("PickTrash", true);
    public final ValueBoolean silentValue = new ValueBoolean("Silent", true);

    private int nextDelay = 0;

    public ModuleContainerStealer() {
        super("ContainerStealer", Category.PLAYER);
    }

    private final Handler<MotionEvent> motionEventHandler = event -> {
        if (HYTUtils.isInLobby()) return;

        if ((postValue.getValue() && event.isPost()) || (!postValue.getValue() && event.isPre())) {
            if (mc.thePlayer.openContainer == null)
                return;

            if (mc.thePlayer.openContainer instanceof ContainerFurnace && furnace.getValue()) {
                ContainerFurnace container = (ContainerFurnace) mc.thePlayer.openContainer;

                if ((isFurnaceEmpty(container)) && openChestTimer.delay(100) && timer.delay(100)) {
                    mc.thePlayer.closeScreen();
                    return;
                }

                for (int i = 0; i < container.tileFurnace.getSizeInventory(); ++i) {
                    if (container.tileFurnace.getStackInSlot(i) != null) {
                        if (timer.delay(nextDelay)) {

//                            for (int j = 0; j < 21; ++j) {
                            if (isInventoryFull()) {
                                mc.playerController.windowClick(container.windowId, i, 0, 4, mc.thePlayer);
                            } else {
                                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                            }
//                            }

                            nextDelay = (int) (delay.getValue() * MathHelper.getRandomDoubleInRange(0.75, 1.25));
                            timer.reset();
                        }
                    }
                }
            }

            if (mc.thePlayer.openContainer instanceof ContainerBrewingStand && brewingStand.getValue()) {
                ContainerBrewingStand container = (ContainerBrewingStand) mc.thePlayer.openContainer;

                if ((isBrewingStandEmpty(container)) && openChestTimer.delay(100) && timer.delay(100)) {
                    mc.thePlayer.closeScreen();
                    return;
                }

                for (int i = 0; i < container.tileBrewingStand.getSizeInventory(); ++i) {
                    if (container.tileBrewingStand.getStackInSlot(i) != null) {
                        if (timer.delay(nextDelay)) {
//                            for (int j = 0; j < 21; ++j) {
                            if (isInventoryFull()) {
                                mc.playerController.windowClick(container.windowId, i, 0, 4, mc.thePlayer);
                            } else {
                                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                            }
//                            }
                            nextDelay = (int) (delay.getValue() * MathHelper.getRandomDoubleInRange(0.75, 1.25));
                            timer.reset();
                        }
                    }
                }
            }

            if (mc.thePlayer.openContainer instanceof ContainerChest && chest.getValue() && isChest) {
                ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;


                if ((isChestEmpty(container)) && openChestTimer.delay(100) && timer.delay(100)) {
                    mc.thePlayer.closeScreen();
                    return;
                }

                for (int i = 0; i < container.getLowerChestInventory().getSizeInventory(); ++i) {
                    if (container.getLowerChestInventory().getStackInSlot(i) != null) {
                        if (timer.delay(nextDelay) && (isItemUseful(container, i) || trash.getValue())) {
//                            for (int j = 0; j < 21; ++j) {
                            if (isInventoryFull()) {
                                mc.playerController.windowClick(container.windowId, i, 0, 4, mc.thePlayer);
                            } else {
                                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                            }
//                            }
                            nextDelay = (int) (delay.getValue() * MathHelper.getRandomDoubleInRange(0.75, 1.25));
                            timer.reset();
                        }
                    }
                }
            }
        }
    };

    private boolean isInventoryFull() {
        for (ItemStack is : mc.thePlayer.inventory.mainInventory) {
            if (is == null) return false;
        }

        return true;
    }

    private boolean isChestEmpty(ContainerChest c) {
        for (int i = 0; i < c.getLowerChestInventory().getSizeInventory(); ++i) {
            if (c.getLowerChestInventory().getStackInSlot(i) != null) {
                if (isItemUseful(c, i) || trash.getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isFurnaceEmpty(ContainerFurnace c) {
        for (int i = 0; i < c.tileFurnace.getSizeInventory(); ++i) {
            if (c.tileFurnace.getStackInSlot(i) != null) {
                return false;
            }
        }

        return true;
    }

    private boolean isBrewingStandEmpty(ContainerBrewingStand c) {
        for (int i = 0; i < c.tileBrewingStand.getSizeInventory(); ++i) {
            if (c.tileBrewingStand.getStackInSlot(i) != null) {
                return false;
            }
        }

        return true;
    }

    private boolean isItemUseful(ContainerChest c, int i) {
        ItemStack itemStack = c.getLowerChestInventory().getStackInSlot(i);
        Item item = itemStack.getItem();

        if (item instanceof ItemAxe || item instanceof ItemPickaxe) {
            return true;
        }

        if (item instanceof ItemFood)
            return true;
        if (item instanceof ItemBow || item == Items.arrow)
            return true;

        if (item instanceof ItemPotion && !ItemUtils.isPotionNegative(itemStack))
            return true;
        if (item instanceof ItemSword && ItemUtils.isBestSword(c, itemStack))
            return true;
        if (item instanceof ItemArmor && ItemUtils.isBestArmor(c, itemStack))
            return true;
        if (item instanceof ItemBlock)
            return true;

        return item instanceof ItemEnderPearl;
    }
}
