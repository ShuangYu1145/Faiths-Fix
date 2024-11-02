package com.github.stachelbeere1248.zombiesutils.config;

public class ZombiesUtilsConfig {
    public ZombiesUtilsConfig() {}
    public short getOffset() {
        return (short) -28;
    }
    public boolean isSlaToggled() {
        return true;
    }
    public boolean isSlaShortened() {
        return true;
    }
    public boolean isSpawntimeShortened() {
        return false;
    }
    public String getChatMacro() {
        return "T";
    }
    public String getDefaultCategory() {
        return "general";
    }
    public String getLanguage() {
        return "EN";
    }
    public int[] getAuditory() {
        return new int[]{-40, -20, 0};
    }
    public boolean getSST() {
        return true;
    }
    public boolean getAnnouncePB() {
        return true;
    }
}
