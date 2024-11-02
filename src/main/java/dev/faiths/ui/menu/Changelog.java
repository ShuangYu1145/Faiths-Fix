package dev.faiths.ui.menu;
public class Changelog {
    public String[] description;
    public ChangelogType type;

    public Changelog(String[] description, ChangelogType type) {
        this.description = description;
        this.type = type;
    }

    public String[] getDescription() {
        return this.description;
    }

    public ChangelogType getType() {
        return this.type;
    }
}
