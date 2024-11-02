package dev.faiths.config;

import java.io.File;

public abstract class AbstractConfig {
    private final File file;
    public AbstractConfig(final File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public abstract void load();
    public abstract void save();
}
