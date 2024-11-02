package dev.faiths.module.render;

import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueMode;

@SuppressWarnings("unused")
public class ModuleAnimation extends CheatModule {
    public ModuleAnimation() {
        super("Animation", Category.RENDER);
    }
    @Override
    public void onEnable() {
        toggle();
    }

    public static ValueMode swordAnimation = new ValueMode("Mode",new String[]{"None","1.7", "Slide", "Sigma", "Push", "Fixed", "Reverse", "Strange", "Spin", "Screw", "Poke", "Exhi Swang", "Exhi Swong", "Exhi Swank", "Exhi Swaing"},
            "1.7");

    public static ValueBoolean swingAnimation = new ValueBoolean("Swing Animation", false);

    public static ValueFloat x = new ValueFloat("Blocking X Position", 0f, -0.5f, 0.5f);
    public static ValueFloat y = new ValueFloat("Blocking Y Position", 0f, -0.5f, 0.5f);
    public static ValueFloat z = new ValueFloat("Blocking Z Position", 0f, -0.5f, 0.5f);

    public static ValueFloat xArm = new ValueFloat("Arm X Position", 0f, -2.5f, 2.5f);
    public static ValueFloat yArm = new ValueFloat("Arm Y Position", 0f, -2.5f, 2.5f);
    public static ValueFloat zArm = new ValueFloat("Arm Z Position", 0f, -2.5f, 2.5f);

    public static ValueFloat swingSpeed = new ValueFloat("Block Animation Speed", 1f, 0f, 3.0f);

}
