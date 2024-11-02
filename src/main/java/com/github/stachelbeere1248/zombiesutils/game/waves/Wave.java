package com.github.stachelbeere1248.zombiesutils.game.waves;

@SuppressWarnings("DuplicatedCode")
public class Wave {
    private final short ticks;
    private final Prefix[] prefixes;

    public Wave(final short ticks, final Prefix[] prefixes) {
        this.ticks = ticks;
        this.prefixes = prefixes;
    }
    public Wave(final short ticks) {
        this.ticks = ticks;
        this.prefixes = new Prefix[]{Prefix.WINDOW};
    }

    public short getTime() {
        return this.ticks;
    }

    public Prefix[] getPrefixes() {
        return this.prefixes;
    }
}