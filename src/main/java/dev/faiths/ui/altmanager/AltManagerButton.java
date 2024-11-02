package dev.faiths.ui.altmanager;

import dev.faiths.Faiths;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

import static net.minecraft.client.renderer.GlStateManager.resetColor;

public class AltManagerButton extends GuiButton {

    public AltManagerButton(int buttonId, int x, int y, String buttonText) {
        super(buttonId, x, y, buttonText);
    }

    public AltManagerButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
    }

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            hovered = (mouseX >= xPosition && mouseY >= yPosition &&
                    mouseX < xPosition + width && mouseY < yPosition + height);
            final float deltaTime = Faiths.delta;

            if (enabled && hovered) {
                alpha += 0.3F * deltaTime;
                if (alpha >= 210)
                    alpha = 210;
            } else {
                alpha -= 0.3F * deltaTime;
                if (alpha <= 120)
                    alpha = 120;
            }

            final Color hoveredColor = hovered ? new Color(148, 63, 137) : new Color(38, 36, 35);
            final Color textColor = new Color(148, 63, 137);

            if (enabled) {
                drawOutLineRect(xPosition, yPosition,
                        width, height,
                        0.5F, new Color(26, 23, 22).getRGB(), hoveredColor.getRGB());

                mc.getTextureManager().bindTexture(buttonTextures);
                mouseDragged(mc, mouseX, mouseY);

                final FontRenderer fontRenderer = mc.fontRendererObj;
                fontRenderer.drawStringWithShadow(displayString,
                        (float) ((xPosition + width / 2) - fontRenderer.getStringWidth(displayString) / 2),
                        yPosition + (height - 5) / 2F, hovered ? textColor.getRGB() : -1);
            } else {
                drawOutLineRect(xPosition, yPosition,
                        width, height,
                        0.5F, new Color(26, 23, 22, 150).getRGB(), new Color(26, 23, 22, 150).getRGB());

                mc.getTextureManager().bindTexture(buttonTextures);
                mouseDragged(mc, mouseX, mouseY);

                final FontRenderer fontRenderer = mc.fontRendererObj;
                fontRenderer.drawStringWithShadow("§o" + displayString,
                        (float) ((xPosition + width / 2) - fontRenderer.getStringWidth(displayString) / 2),
                        yPosition + (height - 5) / 2F, Color.gray.getRGB());
            }

            resetColor();
        }
    }
}
