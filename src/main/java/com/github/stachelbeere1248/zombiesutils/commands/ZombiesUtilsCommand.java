package com.github.stachelbeere1248.zombiesutils.commands;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.github.stachelbeere1248.zombiesutils.utils.InvalidMapException;
import com.github.stachelbeere1248.zombiesutils.utils.ScoardboardException;
import com.github.stachelbeere1248.zombiesutils.utils.Scoreboard;
import dev.faiths.command.AbstractCommand;
import dev.faiths.utils.ClientUtils;

public class ZombiesUtilsCommand extends AbstractCommand {
    public ZombiesUtilsCommand() {
        super("zombiesutils");
    }

    @Override
    public void execute(String [] args) {
        if (args.length == 0) ClientUtils.displayChatMessage(
                "[Missing option] options: timer");
        else switch (args[0]) {
            case "timer":
                switch (args[1]) {
                    case "kill":
                        String serverNumber = Scoreboard.getServerNumber().orElse("");
                        if (args.length == 3) serverNumber = args[2];
                        ZombiesUtils.getInstance().getGameManager().endGame(serverNumber, false);
                        break;
                    case "killall":
                        ZombiesUtils.getInstance().getGameManager().killAll();
                    case "split":
                        try {
                            ZombiesUtils.getInstance().getGameManager().splitOrNew(Integer.parseInt(args[2]));
                        } catch (NumberFormatException | NullPointerException ignored) {
                            ClientUtils.displayChatMessage("t");
                        } catch (ScoardboardException | InvalidMapException e) {
                            ZombiesUtils.getInstance().getLogger().error(e.getStackTrace());
                        }
                        break;
                    default:
                        ClientUtils.displayChatMessage(
                                "[Invalid option] options: kill, killall, split");
                }
                break;
            default:
                ClientUtils.displayChatMessage(
                        "[Invalid option] options: timer");
        }
    }
}
