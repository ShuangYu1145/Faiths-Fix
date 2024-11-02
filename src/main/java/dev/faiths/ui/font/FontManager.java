package dev.faiths.ui.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.BaseFontRender;
import net.optifine.util.FontUtils;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class FontManager {
    private static final HashMap<String, Font> loadedFonts = new HashMap<>();
    public static HashMap<String, BaseFontRender> fonts = new HashMap<>();

    public static BaseFontRender getFont(String name) {
        return fonts.get(name.toLowerCase(Locale.ROOT));
    }

    public static CustomFont sf40;
    public static CustomFont sf24;
    public static CustomFont sf20;
    public static CustomFont sf18;
    public static CustomFont sf15;
    public static CustomFont sf14;
    public static CustomFont sf12;
    public static CustomFont bold20;
    public static CustomFont bold19;
    public static CustomFont bold18;
    public static CustomFont bold15;
    public static CustomFont bold14;
    public static CustomFont bold13;
    public static CustomFont comfortaa20;
    public static CustomFont comfortaa18;
    public static CustomFont robotolight20;
    public static CustomFont robotolight18;
    public static CustomFont robotoregular20;
    public static CustomFont robotoregular18;
    public static CustomFont robotoregular15;

    public static void init() {
        sf40 = new CustomFont(getFont("msyh.ttf", 40));
        sf24 = new CustomFont(getFont("msyh.ttf", 24));
        sf20 = new CustomFont(getFont("msyh.ttf", 20));
        sf18 = new CustomFont(getFont("msyh.ttf", 18));
        sf15 = new CustomFont(getFont("msyh.ttf", 15));
        sf14 = new CustomFont(getFont("msyh.ttf", 14));
        bold20 = new CustomFont(getFont("msyh.ttf", Font.BOLD, 20));
        bold19 = new CustomFont(getFont("msyh.ttf", Font.BOLD, 19));
        bold18 = new CustomFont(getFont("msyh.ttf", Font.BOLD, 18));
        bold15 = new CustomFont(getFont("msyh.ttf", Font.BOLD, 15));
        bold14 = new CustomFont(getFont("msyh.ttf", Font.BOLD, 14));
        bold13 = new CustomFont(getFont("msyh.ttf", Font.BOLD, 13));
        fonts.put("minecraft", Minecraft.getMinecraft().fontRendererObj);
    }

    public static Font getFont(String fontName, float size) {
        return getFont(fontName, Font.PLAIN, size);
    }

    public static Font getFont(String fontName, int type, float size) {
        Font font;

        try {
            InputStream is = FontUtils.class.getResourceAsStream("/assets/minecraft/client/font/" + fontName);
            font = Font.createFont(type, is);
            font = font.deriveFont(type, size);
        } catch (Exception ex) {
            System.out.println("Error while loading font " + fontName + " - " + size + "!");
            font = new Font("Arial", type, 0).deriveFont(size);
        }

        return font;
    }
}