package dev.faiths.ui.report;

import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Window {
    private float x, y;
    private int prevMouseX, prevMouseY, moveX, moveY;
    private boolean leftMouseClicked = false, rightMouseClicked = false, expand = false;
    private boolean dragging = false;
    private Runnable runnable = null;
    public Window(float x, float y, String name) {
        this.x = x;
        this.y = y;
    }

    private boolean mouseHovered(final float x, final float y, final float width, final float height, final int mouseX,
                                 final int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    protected void renderReportGui(final int mouseX, final int mouseY) {
        moveX = mouseX - this.prevMouseX;
        moveY = mouseY - this.prevMouseY;
        this.prevMouseX = mouseX;
        this.prevMouseY = mouseY;
        if (mouseHovered(x, y, 150F, 25F, mouseX, mouseY) && Mouse.isButtonDown(0)) {
            if ((moveX != 0 || moveY != 0) && !dragging) {
                runnable = () -> {
                    this.x += moveX;
                    this.y += moveY;
                };
                dragging = true;
            }

        }

        Gui.drawRect(x, y, x + 150, y + 75, new Color(0, 0, 0, 100).getRGB());
        Gui.drawRect(x, y, x + 150, y + 2, new Color(135,53,149).getRGB());

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, 0);



        if (mouseHovered(x, y, 150F, 25F, mouseX, mouseY)) {
            if (Mouse.isButtonDown(1)) {
                if (!rightMouseClicked) {
                    rightMouseClicked = true;
                    expand = !expand;
                }
            } else {
                rightMouseClicked = false;
            }
        }

        GL11.glPopMatrix();
        if (runnable != null) {
            runnable.run();
        }
    }



    public void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        runnable = null;
    }
}
