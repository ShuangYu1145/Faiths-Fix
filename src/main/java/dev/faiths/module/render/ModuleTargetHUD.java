package dev.faiths.module.render;

import com.viaversion.viaversion.libs.fastutil.objects.ObjectArrayList;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.combat.ModuleKillAura;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.MouseInputHandler;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.value.ValueInt;
import dev.faiths.value.ValueMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static dev.faiths.utils.IMinecraft.mc;
import static dev.faiths.utils.render.BlendUtils.blendColors;
import static dev.faiths.utils.render.RenderUtils.*;
import static net.minecraft.client.gui.Gui.drawScaledCustomSizeModalRect;

@SuppressWarnings("unused")
public class ModuleTargetHUD extends CheatModule {
    private static int animWidth;
    private static float f6;
    private final ValueMode mode = new ValueMode("Mode", new String[]{"Astolfo1", "OldNovoline", "Raven", "Simple", "Astolfo", "Turtle", "Exhibition", "Bingus"}, "Astolfo1");

    private final ValueInt xValue = new ValueInt("X", 50, 0, 4000);
    private final ValueInt yValue = new ValueInt("Y", 50, 0, 4000);
    private int prevX = 0, prevY = 0;

    public ModuleTargetHUD() {
        super("TargetHUD", Category.RENDER);
    }

    private final Handler<Render2DEvent> renderHandler = event -> {
        EntityLivingBase target = ModuleKillAura.target;
        if (!(target instanceof EntityPlayer)) return;
        if (mc.currentScreen instanceof GuiChat) {
            MouseInputHandler.addMouseCallback((mouseX, mouseY) -> {
                if (prevX == 0 && prevY == 0) {
                    prevX = mouseX;
                    prevY = mouseY;
                }
                int prevMouseX = prevX;
                int prevMouseY = prevY;
                prevX = mouseX;
                prevY = mouseY;
                float width = Math.max(75, mc.fontRendererObj.getStringWidth(mc.thePlayer.getName()) + 20);
                if (mouseX >= xValue.getValue() && mouseX <= xValue.getValue() + width + 50 && mouseY >= yValue.getValue() && mouseY <= yValue.getValue() + 47 && Mouse.isButtonDown(0)) {
                    int moveX = mouseX - prevMouseX;
                    int moveY = mouseY - prevMouseY;

                    if (moveX != 0 || moveY != 0) {
                        xValue.setValue(xValue.getValue() + moveX);
                        yValue.setValue(yValue.getValue() + moveY);
                    }
                }
            });
            draw(mc.thePlayer, xValue.getValue(), yValue.getValue());
            return;
        }
        prevX = 0;
        prevY = 0;
        draw((EntityPlayer) target, xValue.getValue(), yValue.getValue());
    };

    public void draw(EntityPlayer target, int x, int y) {
        String targetHUDMode = mode.getValue();
        assert targetHUDMode != null;
        if (targetHUDMode.equalsIgnoreCase("Astolfo")) {
            renderAstolfoTHUD(target, x, y);
        }

        if (targetHUDMode.equalsIgnoreCase("Turtle")) {
            renderTargetHUD(target, x, y);
        }

        if (targetHUDMode.equalsIgnoreCase("Astolfo1")) {
            renderASTHUD(target, x, y);
        }

        if (targetHUDMode.equalsIgnoreCase("Raven")) {
            renderRavenTHUD(target, x, y);
        }

        if (targetHUDMode.equalsIgnoreCase("Simple")) {
            renderSimpleTargetHUD(target, x, y);
        }

        if (targetHUDMode.equalsIgnoreCase("Exhibition")) {
            renderExTargetHUD(target, x, y);
        }

        if (targetHUDMode.equalsIgnoreCase("OldNovoline")) {
            renderOldNovoTHUD(target, x, y);
        }
        if (targetHUDMode.equalsIgnoreCase("Bingus")) {
            renderBingusTargetHUD(target, x, y);
        }
    }

    public static void renderBingusTargetHUD(EntityPlayer player, int x, int y) {

        ScaledResolution sr = new ScaledResolution(mc);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0.0f);
        drawRect(0.0f, 0.0f, Math.max(mc.fontRendererObj.getStringWidth(player.getDisplayName().getFormattedText() + "[" + (int)player.getHealth() + "\u2764" + "]"), 100) + 37.0f, 33.0f, new Color(0, 0, 0, 215).getRGB());
        drawHead(((AbstractClientPlayer)player).getLocationSkin(), 29, 29, (player.hurtTime > 0) ? new Color(200, 30, 30) : new Color(255, 255, 255));
        final float width = 94.0f * (player.getHealth() / player.getMaxHealth());
        drawRect(32.0f, 27.0f, Math.max(mc.fontRendererObj.getStringWidth(player.getDisplayName().getFormattedText() + "[" + (int)player.getHealth() + "\u2764" + "]"), 100) + width - 90.0f, 4.0f,  getBlendColor(player.getHealth(),(player.getMaxHealth())).getRGB());
        GL11.glPushMatrix();
        final List<ItemStack> stuff = new ArrayList<>();
        int cock = -2;
        for (int geraltOfNigeria = 3; geraltOfNigeria >= 0; --geraltOfNigeria) {
            final ItemStack armor = player.getCurrentArmor(geraltOfNigeria);
            if (armor != null) {
                stuff.add(armor);
            }
        }
        if (player.getHeldItem() != null) {
            stuff.add(player.getHeldItem());
        }
        for (final ItemStack yes : stuff) {
            if (Minecraft.getMinecraft().theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                cock += 16;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(yes, cock + 16, 10);
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            yes.getEnchantmentTagList();
        }
        GL11.glPopMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        Minecraft.getMinecraft().fontRendererObj.drawString(player.getDisplayName().getUnformattedText(), 33.0f, 2.0f, -1);
        Minecraft.getMinecraft().fontRendererObj.drawString(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.WHITE + (int)player.getHealth() + EnumChatFormatting.RED + "\u2764" + EnumChatFormatting.GRAY + "]", Minecraft.getMinecraft().fontRendererObj.getStringWidth(player.getDisplayName().getUnformattedText()) + 35.0f, 2.0f, -1);
        GlStateManager.scale(2.0f, 2.0f, 2.0f);
        GlStateManager.popMatrix();
    }

    public static void renderOldNovoTHUD(EntityPlayer e, int x, int y) {
        ScaledResolution sr = new ScaledResolution(mc);

        if (e == null) {
            animWidth = 0;
            return;
        }
        drawRectB((float) x - 1.0f, (float) y + 4.0f, 135.0f, 45.0f, new Color(0, 0, 0, 115));
        mc.fontRendererObj.drawStringWithShadow(e.getName(), (float) x + 30.0f, (float) y + 13.0f, -1);
        drawArmorHUD(e, y +8 , x - 5);
        GL11.glPushMatrix();
        GlStateManager.translate((float) x, (float) y, 1.0f);
        GL11.glScalef(2.0f, 2.0f, 2.0f);
        GlStateManager.translate((float)(-x), (float)(-y), 1.0f);
        GL11.glPopMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GuiInventory.drawEntityOnScreen(x + 15, y + 40, 15, e.rotationYaw, -e.rotationPitch, e);

        f6 = 135.0f * e.getHealth() / e.getMaxHealth();
        if ((float)animWidth > f6) {
            animWidth = getNextPostion(animWidth, (int)f6, 100.0);
        }
        if ((float)animWidth < f6) {
            animWidth = getNextPostion(animWidth, (int)f6, 100.0);
        }
        drawRectB(x - 1, y + 47, animWidth, 2f,getBlendColor(e.getHealth(),(e.getMaxHealth())));
        for (int i = 1; i < 5; ++i) {
            e.getEquipmentInSlot(i);
        }
    }

    static float easingHealth = 0F;
    public static void renderRavenTHUD(EntityPlayer e, int x, int y) {
        final DecimalFormat DF_1 = new DecimalFormat("0.0");
        ScaledResolution sr = new ScaledResolution(mc);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0f);
        if(easingHealth < 0) easingHealth = e.getHealth();
        RenderUtils.drawRoundedRect(0,0,70f+mc.fontRendererObj.getStringWidth(e.getName()),40f,12f,new Color(0,0,0, 92).getRGB());
        RenderUtils.drawOutline(8,0,62f+mc.fontRendererObj.getStringWidth(e.getName()),24f,8f,2f,6f,new Color(133 , 206 , 251),new Color(3 , 169 , 244));
        mc.fontRendererObj.drawStringWithShadow(e.getName(),7f,10f,new Color(244 , 67 , 54).getRGB());
        mc.fontRendererObj.drawStringWithShadow(e.getHealth() > mc.thePlayer.getHealth() ? "L" : "W",mc.fontRendererObj.getStringWidth(e.getName()) + 55f,10f,e.getHealth() > mc.thePlayer.getHealth() ? new Color(244 , 67 , 54).getRGB() : new Color(0 , 255 , 0).getRGB());
        mc.fontRendererObj.drawStringWithShadow(DF_1.format(e.getHealth()),7f+mc.fontRendererObj.getStringWidth(e.getName())+4f,10f,RenderUtils.getHealthColor(e.getHealth(),e.getMaxHealth()).getRGB());

        RenderUtils.drawGradientRoundedRectH(6,10+15, (int) ((70f+mc.fontRendererObj.getStringWidth(e.getName())-5f) * (easingHealth / e.getMaxHealth())),10+15+5,2,new Color(133 , 206 , 251).getRGB(),new Color(3 , 169 , 244).getRGB());
        easingHealth += (float) ((e.getHealth() - easingHealth) / Math.pow(1.2F, 10.0F - 4f));
        GlStateManager.resetColor();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void renderASTHUD(EntityPlayer e, int x, int y) {
        ScaledResolution sr = new ScaledResolution(mc);
        GL11.glPushMatrix();
        float width2 = Math.max(75, mc.fontRendererObj.getStringWidth(e.getName()) + 20);
        String healthStr2 = Math.round(e.getHealth() * 10) / 10d + " ❤";
        GL11.glTranslatef(x, y, 0);
        drawRect(0, 0, 55 + width2, 47, new Color(0, 0, 0, 100).getRGB());

        mc.fontRendererObj.drawStringWithShadow(e.getName(), 35, 3f, Color.WHITE.getRGB());

        boolean isNaN = Float.isNaN(e.getHealth());
        float health = isNaN ? 20 : e.getHealth();
        float maxHealth = isNaN ? 20 : e.getMaxHealth();
        float healthPercent = clampValue(health / maxHealth, 0, 1);

        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0, 2.0, 2.0);
        mc.fontRendererObj.drawStringWithShadow(healthStr2, 18, 7.5f, ModuleHUD.color.getValue().hashCode());
        GlStateManager.popMatrix();

        drawRect(36, 36.5f, 9 + width2, 8f, reAlpha(ModuleHUD.color.getValue().hashCode(), 0.35f));

        float barWidth = (43 + width2 - 2) - 37;
        float drawPercent = 7 + (barWidth / 100) * (healthPercent * 100);

        if (!(drawPercent + e.hurtTime > (int) (55 + width2)))
            drawRect(36, 36.5f, drawPercent + e.hurtTime, 8f, ModuleHUD.color.getValue());
        drawRect(36, 36.5f, drawPercent, 8f, ModuleHUD.color.getValue());

        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();

        GlStateManager.resetColor();
        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1, 1);
        GuiInventory.drawEntityOnScreen(17, 46, (int) (42 / e.height), 0, 0, e);
        GL11.glPopMatrix();
    }

    public static void renderTargetHUD(EntityPlayer player, int x, int y) {

        ScaledResolution sr = new ScaledResolution(mc);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0f);

        // 背景

        drawRect(0f, 0f, 35f + Math.max(mc.fontRendererObj.getStringWidth(player.getDisplayName().getUnformattedText()),100), 33f, new Color(0, 0, 0, 150).getRGB());
        // 头像
        drawHead(((AbstractClientPlayer) player).getLocationSkin() , 29, 29 ,
                player.hurtTime > 0 ? new Color(252 ,157 , 154) : new Color(255 ,255 ,255));

        // 血量
        float width = 94f * (player.getHealth() / player.getMaxHealth());
        drawRect(33f , 30.5f , Math.max(mc.fontRendererObj.getStringWidth(player.getDisplayName().getUnformattedText()),100) + width - 95f , 1.5f , getBlendColor(player.getHealth(),(player.getMaxHealth())).getRGB());


        Minecraft.getMinecraft().fontRendererObj.drawString(player.getDisplayName().getUnformattedText(),  33.5f, 2f, -1);

        GlStateManager.scale(2f , 2f , 2f);

        Minecraft.getMinecraft().fontRendererObj.drawString("\u2764",  30f, 12f / 2f, getBlendColor(player.getHealth(),(player.getMaxHealth())).getRGB());
        Minecraft.getMinecraft().fontRendererObj.drawString("" + (int)(player.getHealth()),  33f / 2f, 13f / 2f, getBlendColor(player.getHealth(),(player.getMaxHealth())).getRGB());
        GlStateManager.scale(0.5f , 0.5f , 0.5f);

        GlStateManager.popMatrix();
    }

    public static void drawHead(ResourceLocation skin , int width, int height , Color color) {
        GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        mc.getTextureManager().bindTexture(skin);
        drawScaledCustomSizeModalRect(2, 2, 8F, 8F, 8, 8, width, height, 64F, 64F);
    }

    public static void renderAstolfoTHUD(EntityPlayer e, int x, int y) {
        ScaledResolution sr = new ScaledResolution(mc);
        int n15 = ModuleHUD.color.getValue().hashCode();
        if (e == null) {
            animWidth = 0;
            return;
        }
        drawRectB((float) x - 1.0f, (float) y + 4.0f, 200.0f, 45.0f, new Color(0, 0, 0, 190));
        mc.fontRendererObj.drawStringWithShadow(e.getName(), (float) x + 22.0f, (float) y + 6.0f, -1);
        GL11.glPushMatrix();
        GlStateManager.translate((float) x, (float) y, 1.0f);
        GL11.glScalef(2.0f, 2.0f, 2.0f);
        GlStateManager.translate((float)(-x), (float)(-y), 1.0f);
        mc.fontRendererObj.drawStringWithShadow((double) Math.round((double) (e.getHealth() / 2.0f) * 10.0) / 10.0 + " \u2764 ", (float) x + 10.0f, (float) y + 9.0f, n15);
        GL11.glPopMatrix();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GuiInventory.drawEntityOnScreen(x + 10, y + 44, 20, e.rotationYaw, -e.rotationPitch, e);
        f6 = 200.0f * e.getHealth() / e.getMaxHealth();
        if ((float)animWidth > f6) {
            animWidth = getNextPostion(animWidth, (int) f6, 200.0);
        }
        if ((float)animWidth < f6) {
            animWidth = getNextPostion(animWidth, (int) f6, 200.0);
        }
        drawRectB(x - 1, y + 46, animWidth, 3.0f, ModuleHUD.color.getValue());
        for (int i = 1; i < 5; ++i) {
            e.getEquipmentInSlot(i);
        }
    }

    public static void renderSimpleTargetHUD(EntityPlayer e, int x, int y) {
        ScaledResolution sr = new ScaledResolution(mc);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        mc.fontRendererObj.drawStringWithShadow(e.getName(), sr.getScaledWidth() / 2f - (mc.fontRendererObj.getStringWidth(e.getName().replaceAll("\247.", "")) / 2f), y-13, 0xffffffff);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/icons.png"));

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDepthMask(false);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GlStateManager.color(1, 1, 1);
        int i = 0;
        while (i < e.getMaxHealth() / 2.0f) {
            mc.ingameGUI.drawTexturedModalRect(
                    ((float) sr.getScaledWidth() / 2) - e.getMaxHealth() / 2.0f * 9.5f / 2.0f + (i * 10),
                    (y), 16, 0, 9, 9);
            ++i;
        }

        i = 0;
        while (i < e.getHealth() / 2.0f) {
            mc.ingameGUI.drawTexturedModalRect(
                    ((float) sr.getScaledWidth() / 2) - e.getMaxHealth() / 2.0f * 9.5f / 2.0f + (i * 10),
                    y, 52, 0, 9, 9);
            ++i;
        }

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GlStateManager.disableBlend();
        GlStateManager.color(1, 1, 1);
        RenderHelper.disableStandardItemLighting();
    }

    public static void renderExTargetHUD(EntityPlayer player, int x, int y) {

        ScaledResolution sr = new ScaledResolution(mc);

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0f);

        // Draws the skeet rectangles.
        skeetRect(0, -2.0,  FontManager.sf18.getStringWidth(player.getName()) > 70.0f ? (double) (124.0f +  FontManager.sf18.getStringWidth(player.getName()) - 70.0f) : 124.0, 38.0, 1.0);
        skeetRectSmall(0.0f, -2.0f, 124.0f, 38.0f, 1.0);
        // Draws name.
        FontManager.sf18.drawString(player.getName(), 42.3f, 0.3f, -1,true);
        // Gets health.
        final float health = player.getHealth();
        // Gets health and absorption
        final float healthWithAbsorption = player.getHealth() + player.getAbsorptionAmount();
        // Color stuff for the healthBar.
        final float[] fractions = new float[]{0.0F, 0.5F, 1.0F};
        final Color[] colors = new Color[]{Color.RED, Color.YELLOW, Color.GREEN};
        // Max health.
        final float progress = health / player.getMaxHealth();
        // Color.
        final Color healthColor = health >= 0.0f ? blendColors(fractions, colors, progress).brighter() : Color.RED;
        // Round.
        double cockWidth = 0.0;
        cockWidth = round(cockWidth, (int) 5.0);
        if (cockWidth < 50.0) {
            cockWidth = 50.0;
        }
        // Healthbar + absorption
        final double healthBarPos = cockWidth * (double) progress;
        rectangle(42.5, 10.3, 103, 13.5, healthColor.darker().darker().darker().darker().getRGB());
        rectangle(42.5, 10.3, 53.0 + healthBarPos + 0.5, 13.5, healthColor.getRGB());
        if (player.getAbsorptionAmount() > 0.0f) {
            rectangle(97.5 - (double) player.getAbsorptionAmount(), 10.3, 103.5, 13.5, new Color(137, 112, 9).getRGB());
        }
        // Draws rect around health bar.
        rectangleBordered(42.0, 9.8f, 54.0 + cockWidth, 14.0, 0.5, 0, Color.BLACK.getRGB());
        // Draws the lines between the healthbar to make it look like boxes.
        for (int dist = 1; dist < 10; ++dist) {
            final double cock = cockWidth / 8.5 * (double) dist;
            rectangle(43.5 + cock, 9.8, 43.5 + cock + 0.5, 14.0, Color.BLACK.getRGB());
        }
        // Draw targets hp number and distance number.
        GlStateManager.scale(0.5, 0.5, 0.5);
        final int distance = (int) mc.thePlayer.getDistanceToEntity(player);
        final String nice = "HP: " + (int) healthWithAbsorption + " | Dist: " + distance;
        mc.fontRendererObj.drawString(nice, 85.3f, 32.3f, -1, true);
        GlStateManager.scale(2.0, 2.0, 2.0);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        // Draw targets armor and tools and weapons and shows the enchants.
        GL11.glPushMatrix();
        final List<ItemStack> stuff = new ArrayList<>();

        int cock = -2;
        for (int geraltOfNigeria = 3; geraltOfNigeria >= 0; --geraltOfNigeria) {
            final ItemStack armor = player.getCurrentArmor(geraltOfNigeria);
            if (armor != null) {
                stuff.add(armor);
            }
        }
        if (player.getHeldItem() != null) {
            stuff.add(player.getHeldItem());
        }

        for (final ItemStack yes : stuff) {
            if (Minecraft.getMinecraft().theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                cock += 16;
            }
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(yes, cock + 28, 20);
            Minecraft.getMinecraft().getRenderItem().renderItemOverlays(Minecraft.getMinecraft().fontRendererObj, yes, cock + 28, 20);
            renderEnchantText(yes, cock + 28, (20 + 0.5f));
            GlStateManager.disableBlend();
            GlStateManager.scale(0.5, 0.5, 0.5);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            yes.getEnchantmentTagList();
        }
        GL11.glPopMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        // Draws targets model.
        GlStateManager.scale(0.31, 0.31, 0.31);
        GlStateManager.translate(73.0f, 102.0f, 40.0f);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        drawModel(player.rotationYaw, player.rotationPitch, player);
        GlStateManager.popMatrix();
    }

    public static void rectangleBordered(double x, double y, double x1, double y1, double width, int internalColor, int borderColor) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x + width, y, x1 - width, y + width, borderColor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x, y, x + width, y1, borderColor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x1 - width, y, x1, y1, borderColor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void drawRectB(float f, float f2, float f3, float f4, Color color) {
        drawRect(f, f2, f3, f4, color.getRGB());
    }

    public static double round(final double value, final int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static int getNextPostion(int n, int n2, double d) {
        int n3 = n;
        if (n3 < n2) {
            int n4 = (int)((double)(n2 - n3) / d);
            if (n4 < 1) {
                n4 = 1;
            }
            n3 += n4;
        } else if (n3 > n2) {
            int n5 = (int)((double)(n3 - n2) / d);
            if (n5 < 1) {
                n5 = 1;
            }
            n3 -= n5;
        }
        return n3;
    }

    public static float clampValue(final float value, final float floor, final float cap) {
        if (value < floor) {
            return floor;
        }
        return Math.min(value, cap);
    }

    public static void skeetRect(final double x, final double y, final double x1, final double y1, final double size) {
        rectangleBordered(x, y - 4.0, x1 + size, y1 + size, 0.5, new Color(60, 60, 60).getRGB(), new Color(10, 10, 10).getRGB());
        rectangleBordered(x + 1.0, y - 3.0, x1 + size - 1.0, y1 + size - 1.0, 1.0, new Color(40, 40, 40).getRGB(), new Color(40, 40, 40).getRGB());
        rectangleBordered(x + 2.5, y - 1.5, x1 + size - 2.5, y1 + size - 2.5, 0.5, new Color(40, 40, 40).getRGB(), new Color(60, 60, 60).getRGB());
        rectangleBordered(x + 2.5, y - 1.5, x1 + size - 2.5, y1 + size - 2.5, 0.5, new Color(22, 22, 22).getRGB(), new Color(255, 255, 255, 0).getRGB());
    }

    public static void skeetRectSmall(final double x, final double y, final double x1, final double y1, final double size) {
        rectangleBordered(x + 4.35, y + 0.5, x1 + size - 84.5, y1 + size - 4.35, 0.5, new Color(48, 48, 48).getRGB(), new Color(10, 10, 10).getRGB());
        rectangleBordered(x + 5.0, y + 1.0, x1 + size - 85.0, y1 + size - 5.0, 0.5, new Color(17, 17, 17).getRGB(), new Color(255, 255, 255, 0).getRGB());
    }

    public static void drawModel(final float yaw, final float pitch, final EntityLivingBase entityLivingBase) {
        GlStateManager.resetColor();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, 0.0f, 50.0f);
        GlStateManager.scale(-50.0f, 50.0f, 50.0f);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        final float renderYawOffset = entityLivingBase.renderYawOffset;
        final float rotationYaw = entityLivingBase.rotationYaw;
        final float rotationPitch = entityLivingBase.rotationPitch;
        final float prevRotationYawHead = entityLivingBase.prevRotationYawHead;
        final float rotationYawHead = entityLivingBase.rotationYawHead;
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((float) (-Math.atan(pitch / 40.0f) * 20.0), 1.0f, 0.0f, 0.0f);
        entityLivingBase.renderYawOffset = yaw - 0.4f;
        entityLivingBase.rotationYaw = yaw - 0.2f;
        entityLivingBase.rotationPitch = pitch;
        entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw;
        entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw;
        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        final RenderManager renderManager = mc.getRenderManager();
        renderManager.setPlayerViewY(180.0f);
        renderManager.setRenderShadow(false);
        renderManager.renderEntityWithPosYaw(entityLivingBase, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        renderManager.setRenderShadow(true);
        entityLivingBase.renderYawOffset = renderYawOffset;
        entityLivingBase.rotationYaw = rotationYaw;
        entityLivingBase.rotationPitch = rotationPitch;
        entityLivingBase.prevRotationYawHead = prevRotationYawHead;
        entityLivingBase.rotationYawHead = rotationYawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.resetColor();
    }

    public static void renderEnchantText(ItemStack stack, int x, float y) {
        RenderHelper.disableStandardItemLighting();
        float enchantmentY = y + 24f;
        if (stack.getItem() instanceof ItemArmor) {
            int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
            int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            int thornLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
            if (protectionLevel > 0) {
                drawEnchantTag("P" + getColor(protectionLevel) + protectionLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel > 0) {
                drawEnchantTag("U" + getColor(unbreakingLevel) + unbreakingLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (thornLevel > 0) {
                drawEnchantTag("T" + getColor(thornLevel) + thornLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getItem() instanceof ItemBow) {
            int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
            int flameLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);
            int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (powerLevel > 0) {
                drawEnchantTag("Pow" + getColor(powerLevel) + powerLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (punchLevel > 0) {
                drawEnchantTag("Pun" + getColor(punchLevel) + punchLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (flameLevel > 0) {
                drawEnchantTag("F" + getColor(flameLevel) + flameLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel > 0) {
                drawEnchantTag("U" + getColor(unbreakingLevel) + unbreakingLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getItem() instanceof ItemSword) {
            int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
            int knockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
            int fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
            int unbreakingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
            if (sharpnessLevel > 0) {
                drawEnchantTag("S" +  getColor(sharpnessLevel) + sharpnessLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (knockbackLevel > 0) {
                drawEnchantTag("K" + getColor(knockbackLevel) + knockbackLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (fireAspectLevel > 0) {
                drawEnchantTag("F" + getColor(fireAspectLevel) + fireAspectLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
            if (unbreakingLevel > 0) {
                drawEnchantTag("U" + getColor(unbreakingLevel) + unbreakingLevel, x * 2, enchantmentY);
                enchantmentY += 8;
            }
        }
        if (stack.getRarity() == EnumRarity.EPIC) {
            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            drawOutlinedStringCock(Minecraft.getMinecraft().fontRendererObj, "God", x * 2, enchantmentY, new Color(255, 255, 0).getRGB(), new Color(100, 100, 0, 200).getRGB());
            GL11.glScalef(1.0f, 1.0f, 1.0f);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();
        }
    }

    private static void drawEnchantTag(String text, int x, float y) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        drawOutlinedStringCock(Minecraft.getMinecraft().fontRendererObj, text, x, y, -1, new Color(0, 0, 0, 220).darker().getRGB());
        GL11.glScalef(1.0f, 1.0f, 1.0f);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static void drawOutlinedStringCock(FontRenderer fr, String s, float x, float y, int color, int outlineColor) {
        fr.drawString(stripColor(s), x - 1.0f, y, outlineColor);
        fr.drawString(stripColor(s), x, y - 1.0f, outlineColor);
        fr.drawString(stripColor(s), x + 1.0f, y, outlineColor);
        fr.drawString(stripColor(s), x, y + 1.0f, outlineColor);
        fr.drawString(s, x, y, color);
    }

    public static void drawArmorHUD(EntityPlayer player, int y, int x) {
        GL11.glPushMatrix();

        Minecraft mc = Minecraft.getMinecraft();
        final List<ItemStack> stuff = new ObjectArrayList<>();

        for (int index = 3; index >= 0; --index) {
            final ItemStack armor = player.inventory.armorInventory[index];

            if (armor != null) {
                stuff.add(armor);
            }
        }

        if (player.getCurrentEquippedItem() != null) {
            stuff.add(player.getCurrentEquippedItem());
        }

        int split = -3;

        for (ItemStack item : stuff) {
            if (mc.theWorld != null) {
                RenderHelper.enableGUIStandardItemLighting();
                split += 16;
            }

            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            GlStateManager.enableBlend();
            mc.getRenderItem().zLevel = -150.0F;
            mc.getRenderItem().renderItemAndEffectIntoGUI(item, split + x + 18, y + 17);
            mc.getRenderItem().zLevel = 0.0F;
            GlStateManager.enableBlend();
            final float z = 0.5F;
            GlStateManager.scale(z, z, z);
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.scale(2.0f, 2.0f, 2.0f);
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
        }
        GL11.glPopMatrix();
    }
}
