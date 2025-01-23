package dev.faiths.module.world;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.AttackEvent;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.HYTUtils;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemSword;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleAutoTool extends CheatModule {

    private int oldSlot;
    private int tick;

    /* constructors */
    public ModuleAutoTool() {
        super("AutoTool", Category.WORLD,"自动工具");
    }

    /* methods */
    private final Handler<MotionEvent> motionEventHandler = event -> {
        if((event.isPre())) {
            if (mc.playerController.isBreakingBlock()) {
                tick++;
                if (tick == 1) {
                    oldSlot = mc.thePlayer.inventory.currentItem;
                }
                mc.thePlayer.updateTool(mc.objectMouseOver.getBlockPos());
            } else if (tick > 0) {
                    mc.thePlayer.inventory.currentItem = oldSlot;
                tick = 0;
            }
        }
    };

    private final Handler<AttackEvent> attackHandler = event -> {
        if (mc.thePlayer.inventory.getCurrentItem() == null || (mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemAxe)) {
            mc.thePlayer.inventory.currentItem = 0;
        }
    };
}