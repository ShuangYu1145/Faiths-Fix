package dev.faiths.command.impl;

import dev.faiths.Faiths;
import dev.faiths.command.AbstractCommand;
import dev.faiths.config.impl.ModuleConfig;
import dev.faiths.ui.notifiction.NotificationType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.client.Minecraft.logger;

public class ConfigCommand extends AbstractCommand {

    public ConfigCommand() {
        super("config", "setting", "settings", "profile");
    }

    @Override
    public void execute(String[] args) {
        if (args.length > 1) {
            if ("load".equalsIgnoreCase(args[1])) {
                if (args.length > 2) {
                    ModuleConfig scriptFile = new ModuleConfig(new File(Faiths.configManager.configsDir, args[2] + ".yml"));
                    if (scriptFile.getFile().exists()) {
                        chat("§9Loading Config...");
                        scriptFile.load();
                        chat("§6Config applied successfully.");
                        playEdit();
                        return;
                    }
                    chat("§cConfig file does not exist!");
                } else {
                    chatSyntax("config load <name>");
                }
                return;
            }
            if ("save".equalsIgnoreCase(args[1])) {
                if (args.length > 2) {
                    ModuleConfig cfg = new ModuleConfig(new File(Faiths.configManager.configsDir, args[2] + ".yml"));
                    try {
                        for (ModuleConfig config : Faiths.configManager.configs) {
                            if (cfg.getFile().exists()) {
                                Faiths.configManager.configs.remove(config);
                                config.getFile().delete();
                            }
                        }
                        saveConfig(cfg);
                        Faiths.configManager.configs.add(cfg);
                        chat("§9Creating config...");
                        chat("§9Saving config...");
                        chat("§6Config saved successfully.");
                    } catch (Throwable throwable) {
                        chat("§cFailed to create local config: §3" + throwable.getMessage());
                    }
                    return;
                }
                chatSyntax("config save <Name>");
                return;
            }
            if ("delete".equalsIgnoreCase(args[1])) {
                if (args.length > 2) {
                    ModuleConfig scriptFile = new ModuleConfig(new File(Faiths.configManager.configsDir, args[2] + ".json"));
                    if (scriptFile.getFile().exists()) {
                        scriptFile.getFile().delete();
                        Faiths.configManager.configs.removeIf(config -> scriptFile.getFile().getName().equals(config.getFile().getName()));
                        chat("§6Config file deleted successfully.");
                        return;
                    }
                    chat("§cConfig file does not exist!");
                    return;
                }
                chatSyntax("config delete <name>");
                return;
            }
            if ("list".equalsIgnoreCase(args[1])) {
                File[] configs = Faiths.configManager.configsDir.listFiles();
                if (configs != null) {
                    chat("§cConfig:" + (configs.length == 0 ? "Empty" : ""));
                    for (File file : configs) chat("> " + file.getName().split(".json")[0]);
                }
                return;
            }
        }
        chatSyntax("config <load/save/list/delete>");
    }

    /**
     * Save one config
     *
     * @param config to save
     */
    private void saveConfig(ModuleConfig config) {
        try {
            if (!config.getFile().exists()) config.getFile().createNewFile();
            config.save();
            logger.info("[ConfigManager] Saved config: " + config.getFile().getName() + ".");
            Faiths.notificationManager.pop("[ConfigManager] Saved config: " + config.getFile().getName() + ".",1000,NotificationType.INFO);
        } catch (Throwable t) {
            logger.error("[ConfigManager] Failed to save config file: " + config.getFile().getName() + ".", t);
            Faiths.notificationManager.pop("[ConfigManager] Failed to save config file: " + config.getFile().getName() + ".",1000,NotificationType.ERROR);
        }
    }

    @Override
    public List<String> tabComplete(String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 0) return result;
        switch (args.length) {
            case 1:
                result.addAll(Arrays.stream(new String[]{"load", "save", "delete", "list"}).filter(cmd -> cmd.startsWith(args[0])).collect(Collectors.toList()));
                break;
            case 2:
                if (!"list".equalsIgnoreCase(args[1])) {
                    try {
                        if (!Faiths.configManager.configs.isEmpty()) {
                            result.addAll(Faiths.configManager.configs.stream().map(config -> config.getFile().getName().split(".json")[0]).filter(name -> name.startsWith(args[1])).collect(Collectors.toList()));
                        }
                    } catch (Exception e) {
                        if (!Faiths.configManager.configs.isEmpty()) {
                            result.addAll(Faiths.configManager.configs.stream().map(config -> config.getFile().getName().split(".json")[0]).collect(Collectors.toList()));
                        }
                    }
                }
                break;
        }
        return result;
    }
}
