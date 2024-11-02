package dev.faiths.module.combat;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.world.ModuleScaffold;
import dev.faiths.utils.TimerUtil;
import dev.faiths.utils.player.InventoryUtil;
import dev.faiths.utils.player.Rotation;
import dev.faiths.utils.player.RotationUtils;
import dev.faiths.value.ValueFloat;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

import org.lwjgl.util.vector.Vector2f;

import java.util.List;
import java.util.stream.Collectors;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleSnowballAura extends CheatModule {
    private final ValueFloat range = new ValueFloat("Range", 8F, 4F, 8F);
    private final TimerUtil timer = new TimerUtil();
    private int lastSlot = -1;
    public ModuleSnowballAura() {
        super("SnowballAura", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastSlot = -1;
    }

    @Override
    public void onDisable() {
        lastSlot = -1;
    }

    private final Handler<MotionEvent> motionHandler = event -> {
        if (mc.thePlayer == null || mc.theWorld == null || mc.getNetHandler() == null || event.isPre() || Faiths.moduleManager.getModule(ModuleScaffold.class).getState()) return;
        final ModuleKillAura killAura = Faiths.moduleManager.getModule(ModuleKillAura.class);
        final int snowballSlot = InventoryUtil.findItem2(36, 45, Items.snowball);
        final int eggSlot = InventoryUtil.findItem2(36, 45, Items.egg);
        final List<Entity> targets = Faiths.moduleManager.getModule(ModuleKillAura.class).getTargets(range.getValue().doubleValue() + 2.0).stream().filter(entity -> mc.thePlayer.canEntityBeSeen(entity)).collect(Collectors.toList());
        targets.sort((o1, o2) -> Float.compare(o1.getClosestDistanceToEntity(mc.thePlayer), o2.getClosestDistanceToEntity(mc.thePlayer)));
        Entity target;
        if (targets.isEmpty()) {
            return;
        }
        int targetSlot = snowballSlot;
        if (targetSlot != -1 && eggSlot != -1) {
            targetSlot = mc.thePlayer.inventoryContainer.getSlot(snowballSlot).getStack().stackSize > mc.thePlayer.inventoryContainer.getSlot(eggSlot).getStack().stackSize ? snowballSlot : eggSlot;
        } else if (snowballSlot == -1) {
            targetSlot = eggSlot;
        }
        target = targets.get(0);
        final float[] rotation = RotationUtils.getAngles(target);
        if (timer.hasTimeElapsed(440) && mc.thePlayer.getDistanceToEntity(target) <= range.getValue().doubleValue() && targetSlot - 36 >= 0 && targetSlot - 36 <= 8) {
            Faiths.INSTANCE.getRotationManager().setRotation(new Vector2f(rotation[0], rotation[1]), 180F, true);
        }
        if (timer.hasTimeElapsed(500) && mc.thePlayer.getDistanceToEntity(target) <= range.getValue().doubleValue() && targetSlot - 36 >= 0 && targetSlot - 36 <= 8) {
            if (targetSlot - 36 != mc.thePlayer.inventory.currentItem) {
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(targetSlot - 36));
            }
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(targetSlot - 36)));
            if (targetSlot - 36 != mc.thePlayer.inventory.currentItem) {
                mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            }
            timer.reset();
        }
//        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem());
//        mc.thePlayer.inventory.currentItem = lastSlot;
    };
}
