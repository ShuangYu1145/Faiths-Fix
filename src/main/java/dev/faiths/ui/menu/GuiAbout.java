package dev.faiths.ui.menu;

import dev.faiths.Faiths;
import dev.faiths.module.client.ModuleHUD;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.animation.normal.Animation;
import dev.faiths.utils.animation.normal.Direction;
import dev.faiths.utils.animation.normal.other.DecelerateAnimation;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.utils.render.RoundedUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.File;

import static net.minecraft.client.gui.GuiMainMenu.progress;
import static net.minecraft.client.gui.GuiMainMenu.startTime;

public class GuiAbout extends GuiScreen {

    private Animation fadeAnimation;

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        if(fadeAnimation == null) {
            fadeAnimation = new DecelerateAnimation(3000, 1);
            fadeAnimation.setDirection(Direction.FORWARDS);
            fadeAnimation.reset();
        }


        if (progress < 1) {
            drawbg();
        }

        if (!(progress < 1)) {
            drawbg();
        }

        String a = "Faiths";

    //    RoundedUtil.drawGradientRoundLR(width / 2.55F, 35, FontManager.sf60.getStringWidth(a) + 10, FontManager.sf60.getHeight() +10, 4 , new Color(250,208,196, (int) (fadeAnimation.getValue() * 200)),new Color(255,209,255, (int) (fadeAnimation.getValue() * 200)));
        FontManager.sf60.drawString(a, width / 2.3F, 35, new Color(0, 0, 0, (int) (fadeAnimation.getValue() * 255)));
        FontManager.sf12.drawString("By ShuangYuTeam", width / 2.2F, 70, new Color(0, 0, 0, (int) (fadeAnimation.getValue() * 255)));


        RoundedUtil.drawRound(width / 2.55F, 100,FontManager.sf60.getStringWidth(a) + 60, 150 , 4 , true , new Color(255,255,255, 170));

        FontManager.sf24.drawString("Faiths-Fix", width / 2.5F, 107, new Color(0, 0, 0, (int) (fadeAnimation.getValue() * 255)));
        FontManager.sf19.drawString("版本", width / 2.5F + 0.5F, 122, new Color(0, 0, 0, (int) (fadeAnimation.getValue() * 255)));
        FontManager.sf19.drawString(Faiths.VERSION, width / 1.8F, 122, new Color(0, 0, 0, (int) (fadeAnimation.getValue() * 170)));

        FontManager.sf19.drawString("github.com/ShuangYu1145/Faiths-Fix", this.width - fontRendererObj.getStringWidth("github.com/ShuangYu1145/Faiths-Fix") + 10, this.height - 10, new Color(0, 0, 0, (int) (fadeAnimation.getValue() * 255)));
        FontManager.sf19.drawString("本项目完全免费且开源", 2, this.height - 10, new Color(0, 0, 0, (int) (fadeAnimation.getValue() * 255)));

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void drawbg() {
        long currentTime1 = System.currentTimeMillis();
        long elapsedTime = currentTime1 - startTime;
        float totalTimeInSeconds = 1f;
        progress = Math.min(1.0f, elapsedTime / (totalTimeInSeconds * 1000f));
        double trueAnim = 1 - Math.pow(1 - progress, 8);
        GlStateManager.translate((1 - trueAnim) * (this.width / 2D), (1 - trueAnim) * (this.height / 2D), 0D);
        GlStateManager.scale(trueAnim, trueAnim, trueAnim);

     //   RenderUtils.drawRect(0,0,width,height,Color.WHITE);

        RoundedUtil.drawGradientRoundLR(0,0,width,height, 0 , new Color(250,208,196, (int) (fadeAnimation.getValue() * 200)),new Color(255,209,255, (int) (fadeAnimation.getValue() * 200)));

        GlStateManager.depthMask(true);
        GlStateManager.shadeModel(7424);
    }
}
