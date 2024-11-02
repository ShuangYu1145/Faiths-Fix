package com.github.stachelbeere1248.zombiesutils.commands;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.Category;
import dev.faiths.command.AbstractCommand;
import dev.faiths.utils.ClientUtils;

import java.io.File;

public class CategoryCommand extends AbstractCommand {
    public CategoryCommand() {
        super("category");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1) ClientUtils.displayChatMessage("Please enter a name for the category");
        else {
            String cat = args[1];
            if (cat.contains(File.separator))
                ClientUtils.displayChatMessage("Your name must not contain '" + File.separator + "' as this is the systems separator character for folder" + File.separator + "sub-folder");
            Category.setSelectedCategory(cat);
            ZombiesUtils.getInstance().getGameManager().getGame().ifPresent(game -> game.setCategory(new Category()));
            ClientUtils.displayChatMessage("§eSet category to §c" + cat);
        }
    }
}
