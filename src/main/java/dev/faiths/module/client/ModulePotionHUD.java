package dev.faiths.module.client;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.font.CustomFont;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.render.RoundedUtil;
import dev.faiths.utils.render.shader.ShaderElement;
import dev.faiths.value.ValueInt;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import static dev.faiths.module.client.ModuleHUD.globalalpha;
import static dev.faiths.utils.IMinecraft.mc;

public class ModulePotionHUD extends CheatModule {

    public ModulePotionHUD() {
        super("PotionHUD", Category.CLIENT, "药水显示");
        this.lastUpdateTime = System.currentTimeMillis();
    }

    // 位置设置
    private final ValueInt xValue = new ValueInt("X", 100, 0, 4000);
    private final ValueInt yValue = new ValueInt("Y", 100, 0, 4000);

    // 动画控制
    private long lastUpdateTime;
    private float currentAlpha = 0f;
    private final float fadeSpeed = 0.03f;

    // 渲染相关
    CustomFont fontRenderer = FontManager.bold20;
    float yPos = 0F;
    float width = 0F;
    float currentHeight = 0F;
    float lerpSpeed = 0.01F;
    float tempYPos = 0F;
    float tempWidth = 0F;

    // 拖拽相关
    private boolean isDragging = false;
    private int dragStartX = 0, dragStartY = 0;

    private String intToRomanByGreedy(int num) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        while (i < values.length && num >= 0) {
            while (values[i] <= num) {
                num -= values[i];
                stringBuilder.append(symbols[i]);
            }
            i++;
        }
        return stringBuilder.toString();
    }

    private int applyAlpha(int color, float alpha) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int alphaValue = (int)(alpha * 255);
        return (alphaValue << 24) | (red << 16) | (green << 8) | blue;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.currentAlpha = 0f; // 重置为完全透明
        this.lastUpdateTime = System.currentTimeMillis();
    }

    private final Handler<Render2DEvent> renderHandler = event -> {
        ScaledResolution sr = new ScaledResolution(mc);

        // 计算实际HUD尺寸
        float actualWidth = tempWidth + 5;
        float actualHeight = currentHeight;

        // 转换为屏幕坐标
        int hudX = xValue.getValue();
        int hudY = yValue.getValue();
        int hudRight = hudX + (int)actualWidth;
        int hudBottom = hudY + (int)actualHeight;

        // 获取当前鼠标位置(考虑缩放)
        int mouseX = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
        int mouseY = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;

        // 拖拽逻辑
        if (mc.currentScreen instanceof GuiChat) {
            if (Mouse.isButtonDown(0)) {
                if (!isDragging && mouseX >= hudX && mouseX <= hudRight && mouseY >= hudY && mouseY <= hudBottom) {
                    // 开始拖拽
                    isDragging = true;
                    dragStartX = mouseX - hudX;
                    dragStartY = mouseY - hudY;
                }

                if (isDragging) {
                    // 更新位置
                    xValue.setValue(mouseX - dragStartX);
                    yValue.setValue(mouseY - dragStartY);
                }
            } else {
                isDragging = false;
            }
        } else {
            isDragging = false;
        }

        // 绘制HUD
        draw();
    };

    public void draw() {
        // 更新透明度（渐显效果）
        long currentTime = System.currentTimeMillis();
        long deltaTime = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        if (currentAlpha < 1.0f) {
            currentAlpha = Math.min(1.0f, currentAlpha + fadeSpeed * (deltaTime / 16.6f));
        }

        yPos = 0F;
        width = 0F;
        tempYPos = 0F;
        tempWidth = 0F;
        GL11.glPushMatrix();
        GL11.glTranslatef(xValue.getValue(), yValue.getValue(), 0F);

        // 先绘制背景
        for (final PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
            final Potion potion = Potion.potionTypes[effect.getPotionID()];
            final String number = intToRomanByGreedy(effect.getAmplifier());
            final String name = I18n.format(potion.getName()) + " " + number;
            final float stringWidth = fontRenderer.getStringWidth(name)
                    + fontRenderer.getStringWidth("§7" + Potion.getDurationString(effect));
            if (tempWidth < stringWidth)
                tempWidth = stringWidth;
            tempYPos += fontRenderer.getFontHeight() + 15;
        }
        currentHeight += (tempYPos - currentHeight) * lerpSpeed;

        // 绘制带透明度的背景
        RoundedUtil.drawRound(-20, -10, tempWidth + 5, currentHeight, 4,
                new Color(0, 0, 0, (int)(globalalpha.getValue() * currentAlpha)));
        ShaderElement.addBlurTask(() -> RoundedUtil.drawRound(xValue.getValue() - 20, yValue.getValue() - 10,
                tempWidth + 5, currentHeight, 4, new Color(0, 0, 0)));
        ShaderElement.addBloomTask(() -> RoundedUtil.drawRound(xValue.getValue() - 20, yValue.getValue() - 10,
                tempWidth + 5, currentHeight, 4, new Color(0, 0, 0)));

        // 绘制内容
        yPos = 0F;
        for (final PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
            final Potion potion = Potion.potionTypes[effect.getPotionID()];
            final String number = intToRomanByGreedy(effect.getAmplifier());
            final String name = I18n.format(potion.getName()) + " " + number;
            final float stringWidth = fontRenderer.getStringWidth(name)
                    + fontRenderer.getStringWidth("§7" + Potion.getDurationString(effect));
            if (width < stringWidth)
                width = stringWidth;

            // 绘制文字
            int potionColor = applyAlpha(potion.getLiquidColor(), currentAlpha);
            fontRenderer.drawString(name, 2f, yPos - 7f, potionColor, false);

            int durationColor = applyAlpha(0xFFFFFF, currentAlpha);
            fontRenderer.drawStringWithShadow("§7" + Potion.getDurationString(effect),
                    2f, yPos + 4, durationColor);

            // 绘制图标
            if (potion.hasStatusIcon()) {
                GL11.glPushMatrix();
                final boolean is2949 = GL11.glIsEnabled(2929);
                final boolean is3042 = GL11.glIsEnabled(3042);
                if (is2949) GL11.glDisable(2929);
                if (!is3042) GL11.glEnable(3042);
                GL11.glDepthMask(false);
                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, currentAlpha);

                final int statusIconIndex = potion.getStatusIconIndex();
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                mc.ingameGUI.drawTexturedModalRect(
                        -20F, yPos - 5,
                        statusIconIndex % 8 * 18,
                        198 + statusIconIndex / 8 * 18,
                        18, 18);

                GL11.glDepthMask(true);
                if (!is3042) GL11.glDisable(3042);
                if (is2949) GL11.glEnable(2929);
                GL11.glPopMatrix();
            }
            yPos += fontRenderer.getFontHeight() + 15;
        }
        GL11.glPopMatrix();
    }
}