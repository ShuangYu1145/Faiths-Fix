package dev.faiths.module.client;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.font.CustomFont;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.Pair;
import dev.faiths.utils.megawalls.FkCounter;
import dev.faiths.utils.render.*;
import dev.faiths.utils.render.shader.KawaseBloom;
import dev.faiths.utils.render.shader.KawaseBlur;
import dev.faiths.utils.render.shader.ShaderElement;
import dev.faiths.value.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import static com.viaversion.viaversion.util.ChatColorUtil.STRIP_COLOR_PATTERN;
import static dev.faiths.module.Category.CLIENT;
import static dev.faiths.utils.IMinecraft.mc;
import static dev.faiths.utils.megawalls.FkCounter.MW_GAME_START_MESSAGE;
import static dev.faiths.utils.render.shader.ShaderElement.createFrameBuffer;
import static net.minecraft.util.EnumChatFormatting.WHITE;

import java.util.*;
import java.awt.Color;
import java.util.stream.Collectors;


@SuppressWarnings("unused")
public class ModuleHUD extends CheatModule {

    public static ValueInt globalalpha = new ValueInt("GlobalAlpha", 100, 0, 255);

    public static ValueMode colorsetting = new ValueMode("ColorSetting", new String[] { "Custom", "Rainbow", "Double" , "Fade"},
            "Dynamic");
    public static final ValueColor color = new ValueColor("Color", new Color(118, 2, 255, 255));
    public static final ValueColor color2 = new ValueColor("Color2", new Color(118, 2, 255, 255)).visible(()->colorsetting.is("Double"));
    public static ValueInt colortick = new ValueInt("ColorTick", 10, 0, 100);

    public ValueBoolean blur = new ValueBoolean("Blur", false);
    public final ValueInt iterations = new ValueInt("Blur Iterations", 2, 1, 8).visible(()-> blur.getValue());
    public final ValueInt offset = new ValueInt("Blur Offset", 3, 1, 10).visible(()-> blur.getValue());
    public ValueBoolean bloom = new ValueBoolean("Bloom (Shadow)", false);
    public final ValueInt shadowRadius = new ValueInt("Bloom Iterations", 3, 1, 8).visible(()-> bloom.getValue());
    public final ValueInt shadowOffset = new ValueInt("Bloom Offset", 1, 1, 10).visible(()-> bloom.getValue());

    public static FkCounter killCounter = new FkCounter();

    private final ValueMultiBoolean information = new ValueMultiBoolean("Information",
            new Pair("ShowFPS", true),
            new Pair("ShowBPS", true),
            new Pair("UserInfo", true),
            new Pair("ClientLogo", true),
            new Pair("Coords", true),
            new Pair("FKCounter", true));

    public static ValueBoolean scoreboardpoint = new ValueBoolean("Scoreboard-Point", true);
    public static final ValueInt scoreboardY = new ValueInt("Scoreboard-Y", 0, 0, 250);

    static final int[] counter = new int[1];

    private Framebuffer bloomFramebuffer = new Framebuffer(1, 1, false);
    private Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);

    private float chatHeightTarget;
    // 当前显示的平滑值
    private static float chatHeightCurrent;
    // 平滑过渡速度 (0.1-0.3比较合适)
    private final float chatHeightLerpSpeed = 0.1f;

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
        int count = 1;
        ScaledResolution sr = new ScaledResolution(mc);
        updateChatHeight();

        if (mc.currentScreen instanceof GuiChat) {
            // 当打开的是 GuiChat 时，将 chatheight 设置为 15
            chatHeightTarget = 15;
        } else {
            // 当打开的不是 GuiChat 时，将 chatheight 设置为 0
            chatHeightTarget = 0;
        }

        final ScaledResolution scaledResolution = event.getScaledResolution();
        CustomFont fontRenderer = FontManager.sf20;
        FontRenderer mcFont = mc.fontRendererObj;


        float x = 2.0F;
        float y = (float) (sr.getScaledHeight() - 10 - getChatHeight());

        if (information.isEnabled("Coords")) {
                FontManager.sf18.drawStringDynamicWithShadow("XYZ: " + Math.round(mc.thePlayer.posX * 10.0) / 10L + " "
                        + Math.round(mc.thePlayer.posY * 10.0) / 10L + " " + Math.round(mc.thePlayer.posZ * 10.0) / 10L,
                        x, y, 1, 50);
            y -= 9.0F;
        }

        if (information.isEnabled("ClientLogo")) {
            RenderUtils.drawImage(
                    new ResourceLocation("client/icon/logo.png"),
                    2.0f,
                    4.0f,
                    64F,
                    64F,
                    color(colortick.getValue()));
        }


        if (information.isEnabled("UserInfo")) {
              String info = "Dev";



                FontManager.sf20.drawStringDynamicWithShadow(info,
                        event.getScaledResolution().getScaledWidth() - FontManager.sf20.getStringWidth(info) - 2,
                        event.getScaledResolution().getScaledHeight() - 10, 1,50);
        }

        if (information.isEnabled("ShowBPS")) {
            double bpt = Math.hypot(mc.thePlayer.posX - mc.thePlayer.lastTickPosX,
                    mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * (double) mc.timer.timerSpeed;
            double bps = bpt * 20.0;
            double roundedBPS = (double) Math.round(bps * 100.0) / 100.0;

                FontManager.sf18.drawStringDynamicWithShadow(roundedBPS + " block / sec", x, y,
                        1,50);

            y -= 9.0F;
        }

        if (information.isEnabled("ShowFPS")) {
                FontManager.sf18.drawStringDynamicWithShadow("FPS: " + mc.getDebugFPS(), x, y,
                        1,50);
            y -= 9.0F;

        }

        if (information.isEnabled("FKCounter")) {
            finals();
        }
    };

    public static Color color(int tick) {
        float time = Minecraft.getSystemTime();
        Color textColor = new Color(-1);
        switch (colorsetting.getValue()) {
            case "Fade":
                textColor = ColorUtil.fade(5, tick * 20, new Color(color.getValue().getRGB()), 1);
                break;
            case "Custom":
                textColor = color.getValue();
                break;
            case "Double":
                tick *= 200;
                textColor = new Color(RenderUtils.colorSwitch(color.getValue(),color2.getValue(), 2000, -tick / 40, 75, 2));
                break;
            case "Rainbow":
                float hue = ((System.currentTimeMillis() % 3000L) / 3000f + (tick * 0.02f)) % 1f;
                textColor = Color.getHSBColor(hue, 0.7f, 1f);
                break;
        }
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

    public void drawBlur() {
        stencilFramebuffer = createFrameBuffer(stencilFramebuffer);

        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(false);

        for (Runnable runnable : ShaderElement.getTasks()) {
            runnable.run();
        }
        ShaderElement.getTasks().clear();

        stencilFramebuffer.unbindFramebuffer();

        KawaseBlur.renderBlur(stencilFramebuffer.framebufferTexture, iterations.getValue().intValue(), offset.getValue().intValue());
    }

    public void drawBloom() {

        bloomFramebuffer = createFrameBuffer(bloomFramebuffer);
        bloomFramebuffer.framebufferClear();
        bloomFramebuffer.bindFramebuffer(false);

        for (Runnable runnable : ShaderElement.getBloomTasks()) {
            runnable.run();
        }
        ShaderElement.getBloomTasks().clear();

        bloomFramebuffer.unbindFramebuffer();

        KawaseBloom.renderBlur(bloomFramebuffer.framebufferTexture, shadowRadius.getValue().intValue(), shadowOffset.getValue().intValue());
    }
    // 获取当前平滑高度
    public static float getChatHeight() {
        return chatHeightCurrent;
    }

    // 设置目标高度
    public void setChatHeight(float height) {
        this.chatHeightTarget = height;
    }

    // 每帧更新平滑高度 (需要在游戏每帧调用)
    public void updateChatHeight() {
        chatHeightCurrent = lerp(chatHeightCurrent, chatHeightTarget, chatHeightLerpSpeed);
    }

    // 线性插值辅助方法
    private float lerp(float current, float target, float speed) {
        return current + (target - current) * speed;
    }
}
