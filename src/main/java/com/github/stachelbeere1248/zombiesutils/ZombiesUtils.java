package com.github.stachelbeere1248.zombiesutils;

import com.github.stachelbeere1248.zombiesutils.commands.CommandRegistry;
import com.github.stachelbeere1248.zombiesutils.config.ZombiesUtilsConfig;
import com.github.stachelbeere1248.zombiesutils.game.GameData;
import com.github.stachelbeere1248.zombiesutils.handlers.Handlers;
import com.github.stachelbeere1248.zombiesutils.timer.GameManager;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

public class ZombiesUtils {
    private static ZombiesUtils instance;
    private final GameManager gameManager;
    private ZombiesUtilsConfig config;
    private Handlers handlers;
    private Logger logger;
    private GameData gameData;

    public ZombiesUtils() {
        gameManager = new GameManager();

        instance = this;
    }

    public static ZombiesUtils getInstance() {
        return instance;
    }

    public static boolean isHypixel() {
        String ip = Minecraft.getMinecraft().getCurrentServerData().serverIP;
        return (ip.equals("localhost") || ip.contains("hypixel") || ip.contains("nyaproxy"));
    }

    public void init() {
        this.logger = Minecraft.logger;
        this.config = new ZombiesUtilsConfig();
        handlers = new Handlers();
        handlers.registerAll();
        CommandRegistry.registerAll();
        gameData = new GameData();
    }

    public Logger getLogger() {
        return logger;
    }

    public Handlers getHandlers() {
        return handlers;
    }

    public ZombiesUtilsConfig getConfig() {
        return config;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public GameData getGameData() {
        return gameData;
    }
}
