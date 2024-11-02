package dev.faiths.module.render;

import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.clickgui.AstolfoGui;

import static dev.faiths.utils.IMinecraft.mc;

import org.lwjgl.input.Keyboard;

@SuppressWarnings("unused")
public class ModuleClickGui extends CheatModule {
    private final AstolfoGui astolfoGui = new AstolfoGui();
    public ModuleClickGui() {
        super("ClickGui", Category.RENDER, Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(astolfoGui);
        toggle();
    }

}
