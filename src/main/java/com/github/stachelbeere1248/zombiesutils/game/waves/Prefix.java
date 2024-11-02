package com.github.stachelbeere1248.zombiesutils.game.waves;

import java.util.stream.IntStream;

public enum Prefix {
    BOSS(0xCC5555, "B"),
    BLAZES(0xEFB61F, "F"),
    SLIME(0x88FF88,"S"),
    OLD_ONE(0x55AA55, "O1"),
    GIANT(0x00FFFF,"G"),
    UFO(0x30D5C8,"U"),
    POLICE(0x16537E,"P"),
    CELL(0xFF8234,"C"),
    WINDOW(0xAAAAAA,"W");

    private final int color;
    private final String prefix;

    Prefix(final int color, final String prefix) {
        this.color = color;
        this.prefix = prefix;
    }
    public int getColor() {
        return color;
    }
    public String getPrefix() {
        return prefix;
    }

    public int getFadedColor(final int fact, final int div) {
        final int normalColor = this.getColor();
        final int B = normalColor % 0xFF;
        final int G = (normalColor - B) % (0xFF * 0xFF);
        final int R = (normalColor - (B + G));
        return IntStream.of(R, G, B).map(i -> (i * fact) / div).sum();
    }

}
