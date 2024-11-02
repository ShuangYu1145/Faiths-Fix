package com.github.stachelbeere1248.zombiesutils.commands;

import dev.faiths.Faiths;

public class CommandRegistry {
    public static void registerAll() {
        Faiths.commandManager.registerCommand(new CategoryCommand());
        Faiths.commandManager.registerCommand(new SlaCommand());
        Faiths.commandManager.registerCommand(new ZombiesUtilsCommand());
        Faiths.commandManager.registerCommand(new QuickZombiesCommand());
    }
}
