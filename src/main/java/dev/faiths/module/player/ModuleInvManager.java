package dev.faiths.module.player;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.AttackEvent;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.movement.ModuleGuiMove;
import dev.faiths.module.world.ModuleScaffold;
import dev.faiths.utils.MSTimer;
import dev.faiths.utils.player.ItemUtils;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueInt;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleInvManager extends CheatModule {
    private final ValueInt delay = new ValueInt("Delay", 100, 50, 500);

    private final ValueBoolean legit = new ValueBoolean("Legit", false);

    private final ValueInt swordSlot = new ValueInt("SwordSlot", 1, 1, 9);
    private final ValueInt pickaxeSlot = new ValueInt("PickaxeSlot", 2, 1, 9);
    private final ValueInt axeSlot = new ValueInt("AxeSlot", 3, 1, 9);
    private final ValueInt shovelSlot = new ValueInt("ShovelSlot", 4, 1, 9);
    private final ValueInt blockSlot = new ValueInt("Block Slot", 5, 1, 9);
    private final ValueInt potionSlot = new ValueInt("PotionSlot", 6, 1, 9);
    private final ValueInt foodSlot = new ValueInt("FoodSlot", 9, 1, 9);
    private final ValueBoolean extraPacket = new ValueBoolean("ExtraPacket", false);

    private final int INVENTORY_ROWS = 4, INVENTORY_COLUMNS = 9, ARMOR_SLOTS = 4;
    private final int INVENTORY_SLOTS = (INVENTORY_ROWS * INVENTORY_COLUMNS) + ARMOR_SLOTS;

    private final MSTimer stopwatch = new MSTimer();
    private int chestTicks, attackTicks, placeTicks;
    private boolean moved, open;
    private long nextClick;

    public ModuleInvManager() {
        super("InvManager", Category.PLAYER,"背包整理");
    }

    public final Handler<MotionEvent> onPreMotionEvent = event -> {
        if (event.isPost()) return;

        if (mc.thePlayer.ticksExisted <= 40) {
            return;
        }

        if (mc.currentScreen instanceof GuiChest) {
            this.chestTicks = 0;
        } else {
            this.chestTicks++;
        }

        this.attackTicks++;
        this.placeTicks++;

        // Calls stopwatch.reset() to simulate opening an inventory, checks for an open inventory to be legit.
        if (legit.getValue() && !(mc.currentScreen instanceof GuiInventory)) {
            this.stopwatch.reset();
            return;
        }

        if (!this.stopwatch.finished(this.nextClick) || this.chestTicks < 10 || this.attackTicks < 10 || this.placeTicks < 10) {
            this.closeInventory();
            return;
        }

        if (!Faiths.moduleManager.getModule(ModuleGuiMove.class).getState() && !(mc.currentScreen instanceof GuiInventory)) {
            return;
        }

        this.moved = false;

        int helmet = -1;
        int chestplate = -1;
        int leggings = -1;
        int boots = -1;

        int sword = -1;
        int pickaxe = -1;
        int axe = -1;
        int shovel = -1;
        int block = -1;
        int potion = -1;
        int food = -1;

        for (int i = 0; i < INVENTORY_SLOTS; i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null) {
                continue;
            }

            final Item item = stack.getItem();

            if (!ItemUtils.useful(stack)) {
                this.throwItem(i);
                if (extraPacket.getValue()){
                    continue;
                }
            }

            if (item instanceof ItemArmor) {
                final ItemArmor armor = (ItemArmor) item;
                final int reduction = this.armorReduction(stack);

                switch (armor.armorType) {
                    case 0:
                        if (helmet == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(helmet))) {
                            helmet = i;
                        }
                        break;

                    case 1:
                        if (chestplate == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(chestplate))) {
                            chestplate = i;
                        }
                        break;

                    case 2:
                        if (leggings == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(leggings))) {
                            leggings = i;
                        }
                        break;

                    case 3:
                        if (boots == -1 || reduction > armorReduction(mc.thePlayer.inventory.getStackInSlot(boots))) {
                            boots = i;
                        }
                        break;
                }
            }

            if (item instanceof ItemSword) {
                if (sword == -1 || damage(stack) > damage(mc.thePlayer.inventory.getStackInSlot(sword))) {
                    sword = i;
                }

                if (i != sword) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemPickaxe) {
                if (pickaxe == -1 || mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(pickaxe))) {
                    pickaxe = i;
                }

                if (i != pickaxe) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemAxe) {
                if (axe == -1 || mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(axe))) {
                    axe = i;
                }

                if (i != axe) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemSpade) {
                if (shovel == -1 || mineSpeed(stack) > mineSpeed(mc.thePlayer.inventory.getStackInSlot(shovel))) {
                    shovel = i;
                }

                if (i != shovel) {
                    this.throwItem(i);
                }
            }

            if (item instanceof ItemBlock) {
                if (block == -1) {
                    block = i;
                } else {
                    final ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(block);

                    if (currentStack != null && stack.stackSize > currentStack.stackSize) {
                        block = i;
                    }
                }
            }

            if (item instanceof ItemPotion) {
                if (potion == -1) {
                    potion = i;
                } else {
                    final ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(potion);

                    if (currentStack == null) {
                        continue;
                    }

                    final ItemPotion currentItemPotion = (ItemPotion) currentStack.getItem();
                    final ItemPotion itemPotion = (ItemPotion) item;

                    boolean foundCurrent = false;

                    for (final PotionEffect e : mc.thePlayer.getActivePotionEffects()) {
                        if (e.getPotionID() == currentItemPotion.getEffects(currentStack).get(0).getPotionID() && e.getDuration() > 0) {
                            foundCurrent = true;
                            if (!extraPacket.getValue()) {
                                break;
                            }
                        }
                    }

                    boolean found = false;

                    for (final PotionEffect e : mc.thePlayer.getActivePotionEffects()) {
                        if (e.getPotionID() == itemPotion.getEffects(stack).get(0).getPotionID() && e.getDuration() > 0) {
                            found = true;
                            break;
                        }
                    }

                    if (itemPotion.getEffects(stack) != null && currentItemPotion.getEffects(currentStack) != null) {
                        if ((PlayerUtils.potionRanking(itemPotion.getEffects(stack).get(0).getPotionID()) > PlayerUtils.potionRanking(currentItemPotion.getEffects(currentStack).get(0).getPotionID()) || foundCurrent) && !found) {
                            potion = i;
                        }
                    }
                }
            }

            if (item instanceof ItemFood) {
                if (food == -1) {
                    food = i;
                } else {
                    final ItemStack currentStack = mc.thePlayer.inventory.getStackInSlot(food);

                    if (currentStack == null) {
                        continue;
                    }

                    final ItemFood currentItemFood = (ItemFood) currentStack.getItem();
                    final ItemFood itemFood = (ItemFood) item;

                    if (itemFood.getSaturationModifier(stack) > currentItemFood.getSaturationModifier(currentStack)) {
                        food = i;
                    }
                }
            }
        }

        for (int i = 0; i < INVENTORY_SLOTS; i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null) {
                continue;
            }

            final Item item = stack.getItem();

            if (item instanceof ItemArmor) {
                final ItemArmor armor = (ItemArmor) item;

                switch (armor.armorType) {
                    case 0:
                        if (i != helmet) {
                            this.throwItem(i);
                        }
                        break;

                    case 1:
                        if (i != chestplate) {
                            this.throwItem(i);
                        }
                        break;

                    case 2:
                        if (i != leggings) {
                            this.throwItem(i);
                        }
                        break;

                    case 3:
                        if (i != boots) {
                            this.throwItem(i);
                        }
                        break;
                }
            }
        }

        if (helmet != -1 && helmet != 39) {
            this.equipItem(helmet);
        }

        if (chestplate != -1 && chestplate != 38) {
            this.equipItem(chestplate);
        }

        if (leggings != -1 && leggings != 37) {
            this.equipItem(leggings);
        }

        if (boots != -1 && boots != 36) {
            this.equipItem(boots);
        }

        if (sword != -1 && sword != this.swordSlot.getValue().intValue() - 1) {
            this.moveItem(sword, this.swordSlot.getValue().intValue() - 37);
        }

        if (pickaxe != -1 && pickaxe != this.pickaxeSlot.getValue().intValue() - 1) {
            this.moveItem(pickaxe, this.pickaxeSlot.getValue().intValue() - 37);
        }

        if (axe != -1 && axe != this.axeSlot.getValue().intValue() - 1) {
            this.moveItem(axe, this.axeSlot.getValue().intValue() - 37);
        }

        if (shovel != -1 && shovel != this.shovelSlot.getValue().intValue() - 1) {
            this.moveItem(shovel, this.shovelSlot.getValue().intValue() - 37);
        }

        if (block != -1 && block != this.blockSlot.getValue().intValue() - 1 && !Faiths.moduleManager.getModule(ModuleScaffold.class).getState()) {
            this.moveItem(block, this.blockSlot.getValue().intValue() - 37);
        }

        if (potion != -1 && potion != this.potionSlot.getValue().intValue() - 1) {
            this.moveItem(potion, this.potionSlot.getValue().intValue() - 37);
        }

        if (food != -1 && food != this.foodSlot.getValue().intValue() - 1) {
            this.moveItem(food, this.foodSlot.getValue().intValue() - 37);
        }

        if (this.canOpenInventory() && !this.moved) {
            this.closeInventory();
        }
    };

    public final Handler<AttackEvent> onAttack = event -> {
        this.attackTicks = 0;
    };

    @Override
    public void onDisable() {
        if (this.canOpenInventory()) {
            this.closeInventory();
        }
    }

    private void openInventory() {
        if (!this.open) {
            mc.thePlayer.sendQueue.addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
            this.open = true;
        }
    }

    private void closeInventory() {
        if (this.open) {
            mc.thePlayer.sendQueue.addToSendQueue(new C0DPacketCloseWindow(mc.thePlayer.inventoryContainer.windowId));
            this.open = false;
        }
    }

    private boolean canOpenInventory() {
        return Faiths.moduleManager.getModule(ModuleGuiMove.class).getState() && !(mc.currentScreen instanceof GuiInventory);
    }

    private void throwItem(final int slot) {
        if ((!this.moved || extraPacket.getValue())) {

            if (this.canOpenInventory()) {
                this.openInventory();
            }

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 1, 4, mc.thePlayer);

            this.nextClick = this.delay.getValue();
            this.stopwatch.reset();
            this.moved = true;
        }
    }

    private void moveItem(final int slot, final int destination) {
        if ((!this.moved || extraPacket.getValue())) {

            if (this.canOpenInventory()) {
                this.openInventory();
            }

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), this.slot(destination), 2, mc.thePlayer);

            this.nextClick = this.delay.getValue();
            this.stopwatch.reset();
            this.moved = true;
        }
    }

    private void equipItem(final int slot) {
        if ((!this.moved || extraPacket.getValue())) {

            if (this.canOpenInventory()) {
                this.openInventory();
            }

            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, this.slot(slot), 0, 1, mc.thePlayer);

            this.nextClick = this.delay.getValue();
            this.stopwatch.reset();
            this.moved = true;
        }
    }

    private float damage(final ItemStack stack) {
        final ItemSword sword = (ItemSword) stack.getItem();
        final int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
        return (float) (sword.getDamageVsEntity() + level * 1.25);
    }

    private float mineSpeed(final ItemStack stack) {
        final Item item = stack.getItem();
        int level = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);

        switch (level) {
            case 1:
                level = 30;
                break;

            case 2:
                level = 69;
                break;

            case 3:
                level = 120;
                break;

            case 4:
                level = 186;
                break;

            case 5:
                level = 271;
                break;

            default:
                level = 0;
                break;
        }

        if (item instanceof ItemPickaxe) {
            final ItemPickaxe pickaxe = (ItemPickaxe) item;
            return pickaxe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemSpade) {
            final ItemSpade shovel = (ItemSpade) item;
            return shovel.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        } else if (item instanceof ItemAxe) {
            final ItemAxe axe = (ItemAxe) item;
            return axe.getToolMaterial().getEfficiencyOnProperMaterial() + level;
        }

        return 0;
    }

    private int armorReduction(final ItemStack stack) {
        final ItemArmor armor = (ItemArmor) stack.getItem();
        return (armor.damageReduceAmount * 114514191) + EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[]{stack}, DamageSource.generic);
    }

    private int slot(final int slot) {
        if (slot >= 36) {
            return 8 - (slot - 36);
        }

        if (slot < 9) {
            return slot + 36;
        }

        return slot;
    }

    public final Handler<PacketEvent> onPacketSend = event -> {
        if (event.getType() == PacketEvent.Type.SEND && event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            this.placeTicks = 0;
        }
    };
}
