package dev.faiths.module.movement;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.combat.ModuleKillAura;
import dev.faiths.module.world.ModuleScaffold;
import dev.faiths.value.ValueBoolean;
import net.minecraft.util.MathHelper;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleSprint extends CheatModule {

    public ModuleSprint() {
        super("Sprint", Category.MOVEMENT,"疾跑");
    }
    public static ValueBoolean allDirection = new ValueBoolean("AllDirection", false);
    ValueBoolean disableBackwards = new ValueBoolean("Disable Backwards", true);
    private float limit;

    private final Handler<UpdateEvent> tickUpdateEventHandler = event -> {
        if(Faiths.moduleManager.getModule(ModuleScaffold.class).getState())return;
        if(Faiths.moduleManager.getModule(ModuleTargetStrafe.class).getState() && (!mc.thePlayer.onGround || mc.thePlayer.onGroundTicks < 3)) return;
        if ((mc.thePlayer.moveForward > 0 || allDirection.getValue()) && mc.thePlayer.getFoodStats().getFoodLevel() > 6) {
            mc.thePlayer.setSprinting(true);
        }
    };

    public boolean disableBackwards() {
        limit = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - mc.thePlayer.lastReportedYaw);
        double limitVal = 125;
        if (!disableBackwards.getValue()) {
            return false;
        }
        if (exceptions()) {
            return false;
        }
        if ((limit <= -limitVal || limit >= limitVal)) {
            return true;
        }
        if (Faiths.moduleManager.getModule(ModuleBHop.class).getState() && ModuleKillAura.target != null && mc.thePlayer.moveForward <= 0.5) {
            return true;
        }
        return false;
    }


    private boolean exceptions() {
        return Faiths.moduleManager.getModule(ModuleScaffold.class).getState() || mc.thePlayer.hurtTime > 0 || !mc.thePlayer.onGround;
    }

}
