package dev.faiths.config;

import dev.faiths.Faiths;
import dev.faiths.config.impl.AccountsConfig;
import dev.faiths.config.impl.ModuleConfig;
import dev.faiths.utils.ClientUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.faiths.utils.IMinecraft.mc;

public class ConfigManager {
    private final File dir = new File(mc.mcDataDir, Faiths.NAME);
    public final File configsDir = new File(dir, "configs");
    public final ModuleConfig modulesConfig = new ModuleConfig(new File(dir, "modules.yml"));
    public final AccountsConfig accountsConfig = new AccountsConfig(new File(dir, "accounts.yml"));
    public final List<ModuleConfig> configs = new ArrayList<>();

    public ConfigManager() {
        if (!dir.exists()) dir.mkdir();
        if (!configsDir.exists()) configsDir.mkdir();
        final File[] configFiles = configsDir.listFiles();
        if (configFiles != null) {
            if(configFiles.length > 0) {
                Arrays.stream(configFiles).forEach(file -> {
                    if (file.getName().endsWith(".yml")) {
                        configs.add(new ModuleConfig(file));
                    }
                });
            }
        }
    }

    public void loadConfigs() {
        loadConfig(modulesConfig);
        loadConfig(accountsConfig);
    }

    public void reloadConfigs() {
        loadConfig(modulesConfig);
    }

    public void saveConfigs() {
        saveConfig(modulesConfig);
        saveConfig(accountsConfig);
    }

    public void loadConfig(final AbstractConfig config) {
        if (!config.getFile().exists()) {
            ClientUtils.LOGGER.info(String.format("[ConfigManager] Skipped loading config: %s.", config.getFile().getName()));
            saveConfig(config);
            return;
        }
        try {
            config.load();
            ClientUtils.LOGGER.info(String.format("[ConfigManager] Loaded config: %s.", config.getFile().getName()));
        } catch (final Throwable throwable) {
            ClientUtils.LOGGER.error(String.format("[ConfigManager] Failed to load config file: %s.", config.getFile().getName()), throwable);
        }
    }

    public void saveConfig(final AbstractConfig config) {
        try {
            if (!config.getFile().exists()) config.getFile().createNewFile();
            config.save();
        //    ClientUtils.LOGGER.info(String.format("[ConfigManager] Saved config: %s.", config.getFile().getName()));
        } catch (final Throwable throwable) {
            ClientUtils.LOGGER.error(String.format("[ConfigManager] Failed to save config file: %s.", config.getFile().getName()), throwable);
        }
    }
}
