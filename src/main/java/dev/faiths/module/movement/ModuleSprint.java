package dev.faiths.module.movement;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.world.ModuleScaffold;
import dev.faiths.value.ValueBoolean;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleSprint extends CheatModule {

    public ModuleSprint() {
        super("Sprint", Category.MOVEMENT);
    }
    public static ValueBoolean allDirection = new ValueBoolean("AllDirection", false);

    private final Handler<UpdateEvent> tickUpdateEventHandler = event -> {
        if(Faiths.moduleManager.getModule(ModuleScaffold.class).getState())return;
        if(Faiths.moduleManager.getModule(ModuleTargetStrafe.class).getState() && (!mc.thePlayer.onGround || mc.thePlayer.onGroundTicks < 3)) return;
        if ((mc.thePlayer.moveForward > 0 || allDirection.getValue()) && mc.thePlayer.getFoodStats().getFoodLevel() > 6) {
            mc.thePlayer.setSprinting(true);
        }
    };
}
