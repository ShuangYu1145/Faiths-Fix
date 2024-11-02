package com.github.stachelbeere1248.zombiesutils.commands;

import dev.faiths.command.AbstractCommand;
import dev.faiths.utils.ClientUtils;
import net.minecraft.client.Minecraft;

public class QuickZombiesCommand extends AbstractCommand {
    public QuickZombiesCommand() {
        super("qz");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1) {
            ClientUtils.displayChatMessage(
                    "[Missing option] options: de, bb, aa, p");
        }
        else switch (args[1]) {
            case "de":
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/play arcade_zombies_dead_end");
                break;
            case "bb":
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/play arcade_zombies_bad_blood");
                break;
            case "aa":
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/play arcade_zombies_alien_arcadium");
                break;
            case "p":
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/play arcade_zombies_prison");
                break;
            default:
                ClientUtils.displayChatMessage(
                        "[Invalid option] options: de, bb, aa, p");

        }
    }
}
