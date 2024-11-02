package dev.faiths.ui.report;

import net.minecraft.client.gui.GuiScreen;

public class gui extends GuiScreen {
    private Window gui;

    public gui(String name) {
        gui = new Window(100, 100, name);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        gui.mouseReleased(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
           gui.renderReportGui(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}