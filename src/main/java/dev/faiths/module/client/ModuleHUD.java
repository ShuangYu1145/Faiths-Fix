package dev.faiths.module.client;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.font.CustomFont;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.MouseInputHandler;
import dev.faiths.utils.Pair;
import dev.faiths.utils.megawalls.FkCounter;
import dev.faiths.utils.render.*;
import dev.faiths.value.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import static com.viaversion.viaversion.util.ChatColorUtil.STRIP_COLOR_PATTERN;
import static dev.faiths.module.Category.CLIENT;
import static dev.faiths.utils.IMinecraft.mc;
import static dev.faiths.utils.megawalls.FkCounter.MW_GAME_START_MESSAGE;
import static net.minecraft.util.EnumChatFormatting.WHITE;

import java.util.*;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.awt.Color;
import java.util.stream.Collectors;


@SuppressWarnings("unused")
public class ModuleHUD extends CheatModule {
    public ValueBoolean facyfont = new ValueBoolean("ClientFont", false);
    public static ValueMode colorsetting = new ValueMode("ColorSetting", new String[] { "Custom", "Rainbow", "Dynamic", "Double" },
            "Dynamic");
    public static ValueInt globalalpha = new ValueInt("GlobalAlpha", 100, 0, 255);

    public static final ValueColor color = new ValueColor("Color", new Color(118, 2, 255, 255));

    public static final ValueColor color2 = new ValueColor("Color2", new Color(118, 2, 255, 255));

    public static ValueInt colortick = new ValueInt("ColorTick", 10, 0, 100);

    public static ValueBoolean glow = new ValueBoolean("Glow", false);


    private final ValueMultiBoolean modulelist = new ValueMultiBoolean("ModuleList",
            new Pair("OutLine", true),
            new Pair("Background", true));

    public static FkCounter killCounter = new FkCounter();

    private final ValueMultiBoolean information = new ValueMultiBoolean("Information",
            new Pair("ShowFPS", true),
            new Pair("ShowBPS", true),
            new Pair("UserInfo", true),
            new Pair("ClientName", true),
            new Pair("ClientLogo", true),
            new Pair("Coords", true),
            new Pair("FKCounter", true),
            new Pair("PotionHUD", true));

    private final ValueInt potionX = new ValueInt("PotionHUD-X", 50, 0, 4000);
    private final ValueInt potionY = new ValueInt("PotionHUD-Y", 50, 0, 4000);
    public static ValueBoolean scoreboardpoint = new ValueBoolean("Scoreboard-Point", true);
    public static final ValueInt scoreboardY = new ValueInt("Scoreboard-Y", 0, 0, 250);
    private int prevX = 0, prevY = 0;

    public static int chatheight = 0;

    static final int[] counter = new int[1];

    public ModuleHUD() {
        super("HUD", CLIENT,"HUD");
    }

    @Override
    public void onEnable() {
        Faiths.moduleManager.resetCopiedModules();
    }

    @Override
    public void onDisable() {
        Faiths.moduleManager.resetCopiedModules();
    }

    public float easeOut(float t, final float d) {
        return (t / d - (t = 1F)) * t * t + 1;
    }

    private String intToRomanByGreedy(int num) {
        int[] values = { 1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1 };
        String[] symbols = { "M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I" };
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

    private final Handler<PacketEvent> packetEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE) {
            Packet packet = event.getPacket();
            if (packet instanceof S02PacketChat) {
                if (((S02PacketChat) packet).getChatComponent().getUnformattedText().equals(MW_GAME_START_MESSAGE)) {
                    killCounter = new FkCounter();
                }

                if (killCounter != null) {
                    killCounter.onChatMessage(((S02PacketChat) packet).getChatComponent());
                }
            }
        }
    };

    public void finals() {
        ArrayList<String> messages = new ArrayList<String>();
        if (mc.ingameGUI.getChatGUI().getChatOpen()) {
            messages.add(EnumChatFormatting.RED + "RED" + WHITE + ": " + killCounter.getPlayers(0).entrySet().stream().map((entry) -> String.valueOf((new StringBuilder(String.valueOf(entry.getKey()))).append(" (").append(entry.getValue()).append(")"))).collect(Collectors.joining(", ")));
            messages.add(EnumChatFormatting.GREEN + "GREEN" + WHITE + ": " + killCounter.getPlayers(1).entrySet().stream().map((entry) -> String.valueOf((new StringBuilder(String.valueOf(entry.getKey()))).append(" (").append(entry.getValue()).append(")"))).collect(Collectors.joining(", ")));
            messages.add(EnumChatFormatting.YELLOW + "YELLOW" + WHITE + ": " + killCounter.getPlayers(2).entrySet().stream().map((entry) -> String.valueOf((new StringBuilder(String.valueOf(entry.getKey()))).append(" (").append(entry.getValue()).append(")"))).collect(Collectors.joining(", ")));
            messages.add(EnumChatFormatting.BLUE + "BLUE" + WHITE + ": " + killCounter.getPlayers(3).entrySet().stream().map((entry) -> String.valueOf((new StringBuilder(String.valueOf(entry.getKey()))).append(" (").append(entry.getValue()).append(")"))).collect(Collectors.joining(", ")));
        } else {
            messages.add(EnumChatFormatting.RED + "RED" + WHITE + ": " + killCounter.getKills(0));
            messages.add(EnumChatFormatting.GREEN + "GREEN" + WHITE + ": " + killCounter.getKills(1));
            messages.add(EnumChatFormatting.YELLOW + "YELLOW" + WHITE + ": " + killCounter.getKills(2));
            messages.add(EnumChatFormatting.BLUE + "BLUE" + WHITE + ": " + killCounter.getKills(3));
        }

        int y = 15;// + 80;

        for (Iterator var4 = messages.iterator(); var4.hasNext(); y = (int) ((float) y + 9.0F)) {
            String text = (String) var4.next();
            drawOutlinedString(text, 4.0F, (float) y + 50f, -1);
        }
    }

    public void drawOutlinedString(String str, float x, float y, int internalCol) {
        mc.fontRendererObj.drawString(stripColorCodes(str), x - 0.5f, y, 0x000000, false);
        mc.fontRendererObj.drawString(stripColorCodes(str), x + 0.5f, y, 0x000000, false);
        mc.fontRendererObj.drawString(stripColorCodes(str), x, y - 0.5f, 0x000000, false);
        mc.fontRendererObj.drawString(stripColorCodes(str), x, y + 0.5f, 0x000000, false);
        mc.fontRendererObj.drawString(str, x, y, internalCol, false);
    }



    public String stripColorCodes(String input) {
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    private final Handler<Render2DEvent> renderHandler = event -> {
        ScaledResolution sr = new ScaledResolution(mc);

        if (mc.currentScreen instanceof GuiChat) {
            // 当打开的是 GuiChat 时，将 chatheight 设置为 15
            chatheight = 15;
        } else {
            // 当打开的不是 GuiChat 时，将 chatheight 设置为 0
            chatheight = 0;
        }

        final List<CheatModule> modules = new ArrayList<>(Faiths.moduleManager.getModules());
        modules.removeIf(module -> (!module.getState() && module.slide == 0F) || module.isHidden());
        final ScaledResolution scaledResolution = event.getScaledResolution();
        final float[] posY = new float[1];
        float time = Minecraft.getSystemTime();
        int index = 0;
        GL11.glPushMatrix();
        GL11.glTranslatef(scaledResolution.getScaledWidth(), 0F, 0F);
        CustomFont fontRenderer = FontManager.sf20;
        FontRenderer mcFont = mc.fontRendererObj;

  //      modules.sort(Comparator.comparingDouble(m -> m.height));

        if (facyfont.getValue()) {

            modules.sort((o1, o2) -> Float.valueOf(
                    (fontRenderer.getStringWidth(o2.getCNName() + o2.getSuffix()) + (o2.suffixIsNotEmpty() ? 2 : 0)))
                    .compareTo((float) (fontRenderer.getStringWidth(o1.getCNName() + o1.getSuffix())
                                                + (o1.suffixIsNotEmpty() ? 2 : 0))));
        } else {
            modules.sort((o1, o2) -> Float.valueOf(
                    (mcFont.getStringWidth(o2.getCNName() + o2.getSuffix()) + (o2.suffixIsNotEmpty() ? 2F : 0)))
                    .compareTo((mcFont.getStringWidth(o1.getCNName() + o1.getSuffix())
                            + (o1.suffixIsNotEmpty() ? 2F : 0))));
        }

        for (final CheatModule module : modules) {
            if (facyfont.getValue()) {

                final float width = fontRenderer.getStringWidth(module.getCNName() + module.getSuffix())
                        + (module.suffixIsNotEmpty() ? 2 : 0);
                final String displayText = module.getCNName();

                if (module.getState()) {
                    if (module.slide < width) {
                        module.slide = easeOut(module.slideStep, width) * width;
                        module.slideStep += Faiths.delta / 4F;
                    }
                    final float size = modules.size() * 8.0E-2F;
                    if (module.height < posY[0]) {
                        module.height += (size -
                                Math.min((module.height * 0.01f), (size - module.height * 0.001F)))
                                * Faiths.delta;
                        module.height = Math.min(posY[0], (module.height));
                    } else {
                        module.height -= (size -
                                Math.min((module.height * 0.01f), (size - module.height * 0.001f)))
                                * Faiths.delta;
                        module.height = Math.max(module.height, (posY[0]));
                    }
                } else if (module.slide > 0F) {
                    module.slide = easeOut(module.slideStep, width) * width;
                    module.slideStep -= Faiths.delta / 4F;
                }
                if (module.slide < 0F)
                    module.slide = 0F;
                if (module.slide > width)
                    module.slide = width;
                if (module.slideStep < 0F)
                    module.slide = 0F;
                if (module.slideStep > width)
                    module.slide = width;
                final float slide = -module.slide - 2f;
                if (modulelist.isEnabled("OutLine")) {
                    RenderUtils.drawRectOriginal(slide - 3, module.height, slide - 2,
                            module.height + fontRenderer.getHeight() + 4F, ModuleHUD.color(ModuleHUD.colortick.getValue()));

                    if (index > 0) {
                        RenderUtils
                                .drawRectOriginal(
                                        slide - 3 - fontRenderer.getStringWidth(modules.get(index - 1).getCNName())
                                                - fontRenderer.getStringWidth(modules.get(index - 1).getSuffix())
                                                - (modules.get(index - 1).suffixIsNotEmpty() ? 2 : 0)
                                                + fontRenderer.getStringWidth(displayText)
                                                + fontRenderer.getStringWidth(module.getSuffix())
                                                + (module.suffixIsNotEmpty() ? 2 : 0),
                                        module.height, slide - 2F, module.height + 1, ModuleHUD.color(ModuleHUD.colortick.getValue()));
                    }
                    if (index == modules.size() - 1)
                        RenderUtils.drawRectOriginal(slide - 3F, module.height + fontRenderer.getHeight() + 4F, 0F,
                                module.height + fontRenderer.getHeight() + 5F, ModuleHUD.color(ModuleHUD.colortick.getValue()));
                }

                if (modulelist.isEnabled("Background")) {

                    RenderUtils.drawRectOriginal(slide - 2, module.height, 0F,
                            module.height + fontRenderer.getHeight() + 4F, new Color(0, 0, 0, globalalpha.getValue()));

                }
                fontRenderer.drawString(displayText, slide, module.height + 2.5F,
                         ModuleHUD.color(ModuleHUD.colortick.getValue()).getRGB(), false);
                fontRenderer.drawString(module.getSuffix(), slide + fontRenderer.getStringWidth(displayText) + 2, module.height + 3F, new Color(160, 160, 160).getRGB(), false);
                counter[0]++;
                posY[0] += fontRenderer.getHeight() + 4;
            } else {
                final FontRenderer font = mc.fontRendererObj;
                final float width = font.getStringWidth(module.getCNName() + module.getSuffix())
                        + (module.suffixIsNotEmpty() ? 2 : 0);
                final String displayText = module.getCNName();
                if (module.getState()) {
                    if (module.slide < width) {
                        module.slide = easeOut(module.slideStep, width) * width;
                        module.slideStep += Faiths.delta / 4F;
                    }
                    final float size = modules.size() * 8.0E-2F;
                    if (module.height < posY[0]) {
                        module.height += (size -
                                Math.min((module.height * 0.01f), (size - module.height * 0.001F)))
                                * Faiths.delta;
                        module.height = Math.min(posY[0], (module.height));
                    } else {
                        module.height -= (size -
                                Math.min((module.height * 0.01f), (size - module.height * 0.001f)))
                                * Faiths.delta;
                        module.height = Math.max(module.height, (posY[0]));
                    }
                } else if (module.slide > 0F) {
                    module.slide = easeOut(module.slideStep, width) * width;
                    module.slideStep -= Faiths.delta / 4F;
                }
                if (module.slide < 0F)
                    module.slide = 0F;
                if (module.slide > width)
                    module.slide = width;
                if (module.slideStep < 0F)
                    module.slide = 0F;
                if (module.slideStep > width)
                    module.slide = width;
//1
                final float slide = -module.slide - 2f;
                if (modulelist.isEnabled("OutLine")) {
                    RenderUtils.drawRectOriginal(slide - 3, module.height, slide - 2,
                            module.height + font.FONT_HEIGHT + 4F, ModuleHUD.color(ModuleHUD.colortick.getValue()));

                    if (index > 0) {
                        RenderUtils
                                .drawRectOriginal(
                                        slide - 3 - font.getStringWidth(modules.get(index - 1).getCNName())
                                                - font.getStringWidth(modules.get(index - 1).getSuffix())
                                                - (modules.get(index - 1).suffixIsNotEmpty() ? 2 : 0)
                                                + font.getStringWidth(displayText)
                                                + font.getStringWidth(module.getSuffix())
                                                + (module.suffixIsNotEmpty() ? 2 : 0),
                                        module.height, slide - 2F, module.height + 1, ModuleHUD.color(ModuleHUD.colortick.getValue()));
                    }
                    if (index == modules.size() - 1)
                        RenderUtils.drawRectOriginal(slide - 3F, module.height + font.FONT_HEIGHT + 4F, 0F,
                                module.height + font.FONT_HEIGHT + 5F, ModuleHUD.color(ModuleHUD.colortick.getValue()));
                }

                if (modulelist.isEnabled("Background")) {
                    RenderUtils.drawRectOriginal(slide - 2, module.height, 0F, module.height + font.FONT_HEIGHT + 4F,
                            new Color(0, 0, 0, globalalpha.getValue()));

                }
                font.drawString(displayText, slide, module.height + 2.5F,
                        ModuleHUD.color(ModuleHUD.colortick.getValue()).getRGB(), true);
                font.drawString(module.getSuffix(), slide + font.getStringWidth(displayText) + 2, module.height + 3F,
                        new Color(160, 160, 160).getRGB(), true);
                counter[0]++;
                posY[0] += font.FONT_HEIGHT + 4;
            }
            time -= 300;
            index += 1;
        }

        GL11.glPopMatrix();

        float x = 2.0F;
        float y = (float) (sr.getScaledHeight() - 10 - chatheight);

        if (information.isEnabled("Coords")) {
            if (facyfont.getValue()) {
                FontManager.sf18.drawString("XYZ: " + Math.round(mc.thePlayer.posX * 10.0) / 10L + " "
                        + Math.round(mc.thePlayer.posY * 10.0) / 10L + " " + Math.round(mc.thePlayer.posZ * 10.0) / 10L,
                        x, y, -1, false);
            } else {
                mc.fontRendererObj.drawStringWithShadow("XYZ: " + Math.round(mc.thePlayer.posX * 10.0) / 10L + " "
                        + Math.round(mc.thePlayer.posY * 10.0) / 10L + " " + Math.round(mc.thePlayer.posZ * 10.0) / 10L,
                        x, y, -1);
            }
            y -= 9.0F;
        }

        if (information.isEnabled("Scoreboard")) {
            if (facyfont.getValue()) {

            } else {

            }
        }

        if (information.isEnabled("ClientName")) {
            final String name = "Faiths" + " | " + mc.thePlayer.getName() + " | " + "FPS:" + mc.getDebugFPS();
            if (facyfont.getValue()) {
           FontManager.sf24.drawStringDynamicWithShadow(name, 3f, 5f,1,50);
                if (glow.getValue()) {
                    GlowUtils.drawGlow(2.0f, 3.5f, FontManager.sf24.getStringWidth(name) + 2, FontManager.sf24.getHeight() + 4, 4,new Color(0, 0, 0,globalalpha.getValue()));
                }
                RoundedUtil.drawRound(2.0f, 3.5f, FontManager.sf24.getStringWidth(name) + 2, FontManager.sf24.getHeight() + 4, 4 ,new Color(0, 0, 0, globalalpha.getValue()));
            } else {
                // for (int i = 0; i < name.length(); ++i) {
                RoundedUtil.drawRound(2.0f, 3.5f, mc.fontRendererObj.getStringWidth(name) + 1, mc.fontRendererObj.getHeight(name) + 1, 4 ,new Color(0, 0, 0, globalalpha.getValue()));
                mc.fontRendererObj.drawStringWithShadow(name, 2.5f, 4.5f,
                        ModuleHUD.color(ModuleHUD.colortick.getValue()).getRGB());
             // }
                if (glow.getValue()) {
                    GlowUtils.drawGlow(2.0f, 3.5f, mc.fontRendererObj.getStringWidth(name) + 1, mc.fontRendererObj.getHeight(name) + 1, 4,new Color(0, 0, 0,globalalpha.getValue()));
                }
            }
        }

        if (information.isEnabled("ClientLogo")) {
            RenderUtils.drawImage(
                    new ResourceLocation("client/icon/logo.png"),
                    2.0f,
                    4.0f,
                    64F,
                    64F,
                    color(ModuleHUD.colortick.getValue()));
        }


        if (information.isEnabled("PotionHUD")) {
            float yPos = 0F;
            float width = 0F;

            GL11.glPushMatrix();
            GL11.glTranslatef(potionX.getValue(), potionY.getValue(), 0F);

            final PotionEffect fakePotionEffect = new PotionEffect(Potion.waterBreathing.getId(), 9999, 1);
            if (mc.currentScreen instanceof GuiChat) {
                if (mc.thePlayer.getActivePotionEffects().isEmpty()) {
                    mc.thePlayer.addPotionEffect(fakePotionEffect);
                }
            }

            final List<Runnable> drawables = new ArrayList<>();

            for (final PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {

                if (facyfont.getValue()) {
                    final Potion potion = Potion.potionTypes[effect.getPotionID()];
                    final String number = intToRomanByGreedy(effect.getAmplifier());
                    final String name = I18n.format(potion.getName()) + " " + number;
                    final float stringWidth = fontRenderer.getStringWidth(name)
                            + fontRenderer.getStringWidth("§7" + Potion.getDurationString(effect));

                    if (width < stringWidth)
                        width = stringWidth;
                    final float finalY = yPos;
                    drawables.add(() -> {
                        fontRenderer.drawString(name, 2f, finalY - 7f, potion.getLiquidColor(), false);
                        fontRenderer.drawStringWithShadow("§7" + Potion.getDurationString(effect), 2f, finalY + 4, -1);
                        if (potion.hasStatusIcon()) {
                            GL11.glPushMatrix();
                            final boolean is2949 = GL11.glIsEnabled(2929);
                            final boolean is3042 = GL11.glIsEnabled(3042);
                            if (is2949)
                                GL11.glDisable(2929);
                            if (!is3042)
                                GL11.glEnable(3042);
                            GL11.glDepthMask(false);
                            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                            final int statusIconIndex = potion.getStatusIconIndex();
                            mc.getTextureManager()
                                    .bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                            mc.ingameGUI.drawTexturedModalRect(
                                    -20F,
                                    finalY - 5,
                                    statusIconIndex % 8 * 18,
                                    198 + statusIconIndex / 8 * 18,
                                    18,
                                    18);
                            GL11.glDepthMask(true);
                            if (!is3042)
                                GL11.glDisable(3042);
                            if (is2949)
                                GL11.glEnable(2929);
                            GL11.glPopMatrix();
                        }
                    });
                    yPos += fontRenderer.getFontHeight() + 15;
                } else {
                    final Potion potion = Potion.potionTypes[effect.getPotionID()];
                    final String number = intToRomanByGreedy(effect.getAmplifier());
                    final String name = I18n.format(potion.getName()) + " " + number;
                    final float stringWidth = mc.fontRendererObj.getStringWidth(name)
                            + mc.fontRendererObj.getStringWidth("§7" + Potion.getDurationString(effect));

                    if (width < stringWidth)
                        width = stringWidth;
                    final float finalY = yPos;
                    drawables.add(() -> {
                        mc.fontRendererObj.drawString(name, 2f, finalY - 7f, potion.getLiquidColor(), true);
                        mc.fontRendererObj.drawStringWithShadow("§7" + Potion.getDurationString(effect), 2f, finalY + 4, -1);
                        if (potion.hasStatusIcon()) {
                            GL11.glPushMatrix();
                            final boolean is2949 = GL11.glIsEnabled(2929);
                            final boolean is3042 = GL11.glIsEnabled(3042);
                            if (is2949)
                                GL11.glDisable(2929);
                            if (!is3042)
                                GL11.glEnable(3042);
                            GL11.glDepthMask(false);
                            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                            final int statusIconIndex = potion.getStatusIconIndex();
                            mc.getTextureManager()
                                    .bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                            mc.ingameGUI.drawTexturedModalRect(
                                    -20F,
                                    finalY - 5,
                                    statusIconIndex % 8 * 18,
                                    198 + statusIconIndex / 8 * 18,
                                    18,
                                    18);
                            GL11.glDepthMask(true);
                            if (!is3042)
                                GL11.glDisable(3042);
                            if (is2949)
                                GL11.glEnable(2929);
                            GL11.glPopMatrix();
                        }
                    });
                    yPos += mc.fontRendererObj.FONT_HEIGHT + 15;
                }
            }

         //   RenderUtils.drawRect(-20, -10, width + 5, yPos, new Color(0, 0, 0, globalalpha.getValue()).getRGB());
        //    BlurUtil.blurArea(-20, -10, width + 5, yPos, 10f,4);

            RoundedUtil.drawRound(-20, -10, width + 5, yPos, 4 ,new Color(0, 0, 0, globalalpha.getValue()));

            if (glow.getValue()) {
                GlowUtils.drawGlow(-20, -10, width + 5, yPos, 4,new Color(0, 0, 0,globalalpha.getValue()));
            }

            drawables.forEach(Runnable::run);

            if (mc.currentScreen instanceof GuiChat) {
                final float finalWidth = width;
                final float finalYPos = yPos;
                MouseInputHandler.addMouseCallback((mouseX, mouseY) -> {
                    if (prevX == 0 && prevY == 0) {
                        prevX = mouseX;
                        prevY = mouseY;
                    }
                    int prevMouseX = prevX;
                    int prevMouseY = prevY;
                    prevX = mouseX;
                    prevY = mouseY;
                    if (mouseX >= potionX.getValue() - 20 && mouseX <= potionX.getValue() + finalWidth - 20
                            && mouseY >= potionY.getValue() - 10 && mouseY <= potionY.getValue() + finalYPos - 10
                            && Mouse.isButtonDown(0)) {
                        int moveX = mouseX - prevMouseX;
                        int moveY = mouseY - prevMouseY;

                        if (moveX != 0 || moveY != 0) {
                            potionX.setValue(potionX.getValue() + moveX);
                            potionY.setValue(potionY.getValue() + moveY);
                        }
                    }
                });
            } else {
                mc.thePlayer.removePotionEffect(fakePotionEffect.getPotionID());
                prevX = 0;
                prevY = 0;
            }

            GL11.glPopMatrix();
        }

        if (information.isEnabled("UserInfo")) {
              String info = "Dev";


            if (facyfont.getValue()) {
                FontManager.sf20.drawString(info,
                        event.getScaledResolution().getScaledWidth() - FontManager.sf20.getStringWidth(info) - 2,
                        event.getScaledResolution().getScaledHeight() - 10, -1, false);
            } else {
                mc.fontRendererObj.drawStringWithShadow(info,
                        event.getScaledResolution().getScaledWidth() - mc.fontRendererObj.getStringWidth(info) - 2,
                        event.getScaledResolution().getScaledHeight() - 10, -1);
            }
        }

        if (information.isEnabled("ShowBPS")) {
            double bpt = Math.hypot(mc.thePlayer.posX - mc.thePlayer.lastTickPosX,
                    mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * (double) mc.timer.timerSpeed;
            double bps = bpt * 20.0;
            double roundedBPS = (double) Math.round(bps * 100.0) / 100.0;
            if (facyfont.getValue()) {
                FontManager.sf18.drawString(roundedBPS + " block / sec", x, y,
                        ModuleHUD.color(ModuleHUD.colortick.getValue()).getRGB(), false);
            } else {
                mc.fontRendererObj.drawStringWithShadow(roundedBPS + " block / sec", x, y,
                        ModuleHUD.color(ModuleHUD.colortick.getValue()).getRGB());
            }
            y -= 9.0F;
        }

        if (information.isEnabled("ShowFPS")) {
            if (facyfont.getValue()) {
                FontManager.sf18.drawString("FPS: " + mc.getDebugFPS(), x, y,
                        ModuleHUD.color(ModuleHUD.colortick.getValue()).getRGB(), false);
            } else {
                mc.fontRendererObj.drawStringWithShadow("FPS: " + mc.getDebugFPS(), x, y,
                        ModuleHUD.color(ModuleHUD.colortick.getValue()).getRGB());
            }
            y -= 9.0F;

        }

        if (information.isEnabled("FKCounter")) {
            finals();
        }
    };

    public static Color color(int tick) {
        float time = Minecraft.getSystemTime();
        final Color textColor = colorsetting.is("Custom") ? color.getValue()
                : colorsetting.is("Dynamic") ? new Color(getArrayDynamic(ModuleHUD.colortick.getValue(), 255))
                : colorsetting.is("Rainbow") ? new Color(astolfoRainbow(counter[0], 5, 107))
                : new Color(RenderUtils.colorSwitch(color.getValue(), color2.getValue(), 2000.0f, -(tick * 200) / 40, 75L, 2.0));
        return textColor;
    }


    public static int astolfoRainbow(int delay, int offset, int index) {
        double rainbowDelay = Math.ceil(System.currentTimeMillis() + (long) (delay * index)) / offset;
        return Color.getHSBColor(
                (double) ((float) ((rainbowDelay %= 360.0) / 360.0)) < 0.5 ? -((float) (rainbowDelay / 360.0))
                        : (float) (rainbowDelay / 360.0),
                0.5F, 1).getRGB();
    }

    public static int getArrayDynamic(float counter, int alpha) {
        float brightness = 1.0F
                - MathHelper.abs(MathHelper.sin(counter % 6000F / 6000F * (float) Math.PI * 2.0F) * 0.6F);
        final float[] hudHSB = getHSB(color.getValue().getRGB());
        Color color = Color.getHSBColor(hudHSB[0], hudHSB[1], brightness);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB();
    }

    public static float[] getHSB(final int value) {
        float[] hsbValues = new float[3];

        float saturation, brightness;
        float hue;

        int cMax = Math.max(value >>> 16 & 0xFF, value >>> 8 & 0xFF);
        if ((value & 0xFF) > cMax)
            cMax = value & 0xFF;

        int cMin = Math.min(value >>> 16 & 0xFF, value >>> 8 & 0xFF);
        if ((value & 0xFF) < cMin)
            cMin = value & 0xFF;

        brightness = (float) cMax / 255.0F;
        saturation = cMax != 0 ? (float) (cMax - cMin) / (float) cMax : 0;

        if (saturation == 0) {
            hue = 0;
        } else {
            float redC = (float) (cMax - (value >>> 16 & 0xFF)) / (float) (cMax - cMin), // @off
                    greenC = (float) (cMax - (value >>> 8 & 0xFF)) / (float) (cMax - cMin),
                    blueC = (float) (cMax - (value & 0xFF)) / (float) (cMax - cMin); // @on

            hue = ((value >>> 16 & 0xFF) == cMax ? blueC - greenC
                    : (value >>> 8 & 0xFF) == cMax ? 2.0F + redC - blueC : 4.0F + greenC - redC) / 6.0F;

            if (hue < 0)
                hue += 1.0F;
        }

        hsbValues[0] = hue;
        hsbValues[1] = saturation;
        hsbValues[2] = brightness;

        return hsbValues;
    }
}
