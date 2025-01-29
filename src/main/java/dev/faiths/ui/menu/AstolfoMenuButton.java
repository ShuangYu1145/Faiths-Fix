package dev.faiths.ui.menu;

import dev.faiths.Faiths;
import dev.faiths.module.client.ModuleHUD;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.render.RoundedUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

import static dev.faiths.module.client.ModuleHUD.globalalpha;
import static net.minecraft.client.renderer.GlStateManager.resetColor;

public class AstolfoMenuButton extends Gui
{
    protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");

    /** Button width in pixels */
    protected int width;

    /** Button height in pixels */
    protected int height;

    /** The x position of this control. */
    public int xPosition;

    /** The y position of this control. */
    public int yPosition;

    /** The string displayed on this control. */
    public String displayString;
    public int id;

    /** True if this control is enabled, false to disable. */
    public boolean enabled;

    /** Hides the button completely if false. */
    public boolean visible;
    protected boolean hovered;

    protected float alpha;

    public AstolfoMenuButton(int buttonId, int x, int y, String buttonText)
    {
        this(buttonId, x, y, 100, 20, buttonText);
    }

    public AstolfoMenuButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText)
    {
        this.width = 200;
        this.height = 20;
        this.enabled = true;
        this.visible = true;
        this.id = buttonId;
        this.xPosition = x;
        this.yPosition = y;
        this.width = widthIn;
        this.height = heightIn;
        this.displayString = buttonText;
    }

    /**
     * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over
     * this button.
     */
    protected int getHoverState(boolean mouseOver)
    {
        int i = 1;

        if (!this.enabled)
        {
            i = 0;
        }
        else if (mouseOver)
        {
            i = 2;
        }

        return i;
    }

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (visible) {
            hovered = (mouseX >= xPosition && mouseY >= yPosition &&
                    mouseX < xPosition + width && mouseY < yPosition + height);
            final float deltaTime = Faiths.delta;

            if (enabled && hovered) {
                alpha += 0.3F * deltaTime;
                if (alpha >= 210) alpha = 210;
            } else {
                alpha -= 0.3F * deltaTime;
                if (alpha <= 120) alpha = 120;
            }



            try {

//                Gui.drawRect(xPosition, yPosition,
//                        xPosition + width, yPosition + height,
//                        enabled ? new Color(0F, 0F, 0F, alpha / 255F).getRGB() :
//                                new Color(0.5F, 0.5F, 0.5F, 0.5F).getRGB());
//



                RoundedUtil.drawRound(xPosition, yPosition,
                        width, height, 4,
                        enabled ? new Color(0, 0, 0, alpha / 255F) : new Color(0, 0, 0, globalalpha.getValue()));


//                RoundedUtil.drawRoundOutline(xPosition, yPosition,
//                        width, height, 4,0.5f,
//                        enabled ? new Color(0, 0, 0, alpha / 255F) : new Color(0, 0, 0, 150),ModuleHUD.color(ModuleHUD.colortick.getValue()));

            } catch (final Exception ignored) {
                
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            mouseDragged(mc, mouseX, mouseY);

            if (Faiths.moduleManager.getModule(ModuleHUD.class).facyfont.getValue()) {
                FontManager.bold18.drawStringDynamic(displayString.toUpperCase(), (float) ((xPosition + width / 2) - mc.fontRendererObj.getStringWidth(displayString.toUpperCase()) / 2) - 1,
                        yPosition + (height - 5) / 2F,1,50);
            } else {
                mc.fontRendererObj.drawStringDynamic(displayString.toUpperCase(), (float) ((xPosition + width / 2) - mc.fontRendererObj.getStringWidth(displayString.toUpperCase()) / 2),
                        yPosition + (height - 5) / 2F, 1,50);
            }

            resetColor();
        }
//        if (this.visible)
//        {
//            FontRenderer fontrenderer = mc.fontRendererObj;
//            mc.getTextureManager().bindTexture(buttonTextures);
//            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
//            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
//            int i = this.getHoverState(this.hovered);
//            GlStateManager.enableBlend();
//            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//            GlStateManager.blendFunc(770, 771);
//            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 46 + i * 20, this.width / 2, this.height);
//            this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
//            this.mouseDragged(mc, mouseX, mouseY);
//            int j = 14737632;
//
//            if (!this.enabled)
//            {
//                j = 10526880;
//            }
//            else if (this.hovered)
//            {
//                j = 16777120;
//            }
//
//            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, j);
//        }
    }

    /**
     * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
     */
    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY)
    {
    }

    /**
     * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
     */
    public void mouseReleased(int mouseX, int mouseY)
    {
    }

    /**
     * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent
     * e).
     */
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
    }

    /**
     * Whether the mouse cursor is currently over the button.
     */
    public boolean isMouseOver()
    {
        return this.hovered;
    }

    public void drawButtonForegroundLayer(int mouseX, int mouseY)
    {
    }

    public void playPressSound(SoundHandler soundHandlerIn)
    {
        soundHandlerIn.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
    }

    public int getButtonWidth()
    {
        return this.width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }
}
