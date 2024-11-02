package dev.faiths.command.impl;

import dev.faiths.Faiths;
import dev.faiths.command.AbstractCommand;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.notifiction.NotificationType;
import org.lwjgl.input.Keyboard;

public class ReloadCommand extends AbstractCommand {
    public ReloadCommand() {
        super("reload","re");
    }

    @Override
    public void execute(final String[] args) {
        Faiths.configManager.reloadConfigs();
        Faiths.notificationManager.pop("Config",
                "Successfully reload config", NotificationType.SUCCESS);
    }

}
