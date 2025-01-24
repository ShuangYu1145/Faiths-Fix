package dev.faiths.ui.font;

import java.awt.*;
import java.io.InputStream;

import static jdk.jfr.internal.SecuritySupport.getResourceAsStream;

public class FontManager {
    public static CustomFont p40;
    public static CustomFont p24;
    public static CustomFont p20;
    public static CustomFont p18;
    public static CustomFont p15;
    public static CustomFont p14;
    public static CustomFont sf40;
    public static CustomFont sf24;
    public static CustomFont sf20;
    public static CustomFont sf19;
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
    public static CustomFont bold12;

    public static void init() {

        sf40 = new CustomFont(getFont("MiSans-Regular.ttf", 40));
        sf24 = new CustomFont(getFont("MiSans-Regular.ttf", 24));
        sf20 = new CustomFont(getFont("MiSans-Regular.ttf", 20));
        sf19 = new CustomFont(getFont("MiSans-Regular.ttf", 19));
        sf18 = new CustomFont(getFont("MiSans-Regular.ttf", 18));
        sf15 = new CustomFont(getFont("MiSans-Regular.ttf", 15));
        sf14 = new CustomFont(getFont("MiSans-Regular.ttf", 14));
        p40 = new CustomFont(getFont("Paw.ttf", 40));
        p24 = new CustomFont(getFont("Paw.ttf", 24));
        p20 = new CustomFont(getFont("Paw.ttf", 20));
        p18 = new CustomFont(getFont("Paw.ttf", 18));
        p15 = new CustomFont(getFont("Paw.ttf", 15));
        p14 = new CustomFont(getFont("Paw.ttf", 14));
        bold20 = new CustomFont(getFont("MiSans-Demibold.ttf", 20));
        bold19 = new CustomFont(getFont("MiSans-Demibold.ttf", 19));
        bold18 = new CustomFont(getFont("MiSans-Demibold.ttf", 18));
        bold15 = new CustomFont(getFont("MiSans-Demibold.ttf", 15));
        bold14 = new CustomFont(getFont("MiSans-Demibold.ttf", 14));
        bold13 = new CustomFont(getFont("MiSans-Demibold.ttf", 13));
        bold12 = new CustomFont(getFont("MiSans-Demibold.ttf", 12));

    }

    private static Font getFont(String fontName, float fontSize) {
        Font font = null;
        try {
            InputStream inputStream = getResourceAsStream("/assets/minecraft/client/font/" + fontName);
            assert (inputStream != null);
            font = Font.createFont(0, inputStream);
            font = font.deriveFont(fontSize);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return font;
    }
}

