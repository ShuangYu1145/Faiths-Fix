package dev.faiths.ui.menu;

import com.google.common.collect.Lists;
import dev.faiths.module.client.ModuleHUD;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.animation.normal.Animation;
import dev.faiths.utils.animation.normal.Direction;
import dev.faiths.utils.animation.normal.other.DecelerateAnimation;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import static net.minecraft.client.gui.GuiMainMenu.progress;
import static net.minecraft.client.gui.GuiMainMenu.startTime;


public class GuiWelcome extends GuiScreen {

    private Animation fadeAnimation;
    protected List<AstolfoMenuButton> buttons = Lists.newArrayList();

    


    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        LocalTime currentTime = LocalTime.now();
        String greeting = getGreeting(currentTime);

        if(fadeAnimation == null) {
            fadeAnimation = new DecelerateAnimation(3000, 1);
            fadeAnimation.setDirection(Direction.FORWARDS);
            fadeAnimation.reset();
        }

        SimpleDateFormat formatter= new SimpleDateFormat("HH:mm");
        Date date = new Date(System.currentTimeMillis());

        if (progress < 1) {
            drawbg();
        }

        if (!(progress < 1)) {
            drawbg();
        }


        FontManager.sf72.drawString(formatter.format(date), 5, 5, new Color(255, 255, 255, (int) (fadeAnimation.getValue() * 255)));
        FontManager.sf40.drawString(greeting + " " + "福瑞控", 5, FontManager.sf72.getFontHeight(), new Color(255, 255, 255, (int) (fadeAnimation.getValue() * 255)));
        
        String clickText = "点击任意位置进入";
        int textWidth = FontManager.sf40.getStringWidth(clickText);
        FontManager.sf40.drawString(clickText, (this.width - textWidth) / 2f, this.height / 2f, new Color(255, 255, 255, (int) (fadeAnimation.getValue() * 255)));

        super.drawScreen(mouseX, mouseY, partialTicks);
        
        String openSourceText = "本客户端完全免费，已在Github/ShuangYu1145/Faiths-Fix开源";
        int openSourceTextWidth = FontManager.sf19.getStringWidth(openSourceText);
        FontManager.sf19.drawString(openSourceText, (this.width - openSourceTextWidth) / 2f, this.height - 20, new Color(255, 255, 255, (int) (fadeAnimation.getValue() * 255)));
    }//啾啾写的


    public static String getGreeting(LocalTime time) {
        int hour = time.getHour();
        if (hour >= 5 && hour < 8) {
            return "早上好";
        } else if (hour >= 8 && hour < 12) {
            return "上午好";
        } else if (hour >= 12 && hour < 14) {
            return "中午好";
        } else if (hour >= 14 && hour < 18) {
            return "下午好";
        } else if (hour >= 18 && hour < 20) {
            return "傍晚好";
        } else {
            return "晚上好";
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            for (AstolfoMenuButton button : buttons) {
                if (button.mousePressed(this.mc, mouseX, mouseY)) {
                    button.playPressSound(mc.getSoundHandler());
                    return;
                }
            }
            this.mc.displayGuiScreen(new GuiMainMenu());
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void drawbg() {
        long currentTime1 = System.currentTimeMillis();
        long elapsedTime = currentTime1 - startTime;
        float totalTimeInSeconds = 1f;
        progress = Math.min(1.0f, elapsedTime / (totalTimeInSeconds * 1000f));
        double trueAnim = 1 - Math.pow(1 - progress, 8);
        GlStateManager.translate((1 - trueAnim) * (this.width / 2D), (1 - trueAnim) * (this.height / 2D), 0D);
        GlStateManager.scale(trueAnim, trueAnim, trueAnim);
        Color c = new Color(ModuleHUD.color.getValue().getRGB());
        Color c2 = new Color(ModuleHUD.color2.getValue().getRGB());

        mc.getTextureManager().bindTexture(new ResourceLocation("client/bg.png"));
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferbuilder = tessellator.getWorldRenderer();
        bufferbuilder.begin(9, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(0, this.height, 0.0).tex(0.0, 1.0).color(c.getRed(), c.getGreen(), c.getBlue(), (int) (fadeAnimation.getValue() * 255)).endVertex();
        bufferbuilder.pos(this.width, this.height, 0.0).tex(1.0, 1.0).color(c2.getRed(), c2.getGreen(), c2.getBlue(), (int) (fadeAnimation.getValue() * 255)).endVertex();
        bufferbuilder.pos(this.width, 0, 0.0).tex(1.0, 0.0).color(c.getRed(), c.getGreen(), c.getBlue(),(int) (fadeAnimation.getValue() * 255)).endVertex();
        bufferbuilder.pos(0, 0, 0.0).tex(0.0, 0.0).color(c2.getRed(), c2.getGreen(), c2.getBlue(), (int) (fadeAnimation.getValue() * 255)).endVertex();
        GlStateManager.shadeModel(7425);
        GlStateManager.depthMask(false);
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.shadeModel(7424);
    }
}
