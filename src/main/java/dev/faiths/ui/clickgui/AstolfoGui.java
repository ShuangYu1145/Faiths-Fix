package dev.faiths.ui.clickgui;

import dev.faiths.module.Category;
import dev.faiths.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.opengl.Display;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AstolfoGui extends GuiScreen {
    private final List<Window> windows = new ArrayList<>();

    public AstolfoGui() {
        float x = 50, y = 50;
        for (final Category category : Category.values()) {
            windows.add(new Window(category, x, y));
            x += 125;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        this.windows.forEach(window -> {
            window.mouseReleased(mouseX, mouseY, state);
        });
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtils.drawRect(0, 0, Display.getWidth(), Display.getHeight(), new Color(0, 0, 0, 150));
        this.windows.forEach(window -> {
            window.renderWindow(mouseX, mouseY);
        });
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
