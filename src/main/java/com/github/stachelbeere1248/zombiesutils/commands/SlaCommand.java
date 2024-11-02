package com.github.stachelbeere1248.zombiesutils.commands;

import com.github.stachelbeere1248.zombiesutils.game.enums.Map;
import com.github.stachelbeere1248.zombiesutils.game.sla.QuickSLA;
import com.github.stachelbeere1248.zombiesutils.game.windows.SLA;
import dev.faiths.command.AbstractCommand;
import dev.faiths.utils.ClientUtils;

public class SlaCommand extends AbstractCommand {
    public SlaCommand() {
        super("sla");
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 1) ClientUtils.displayChatMessage(
                "[Missing option] options: off, offset, rotate, mirror, map, quick");
        else switch (args[1]) {
            case "off":
                SLA.drop();
                ClientUtils.displayChatMessage("SLA data deleted");
                break;
            case "offset":
                if (args.length == 2) SLA.getInstance().ifPresent(SLA::resetOffset);
                else if (args.length != 5) ClientUtils.displayChatMessage(
                        "An offset should have three coordinates!");
                else {
                    try {
                        int x = Integer.parseInt(args[2]);
                        int y = Integer.parseInt(args[3]);
                        int z = Integer.parseInt(args[4]);
                        SLA.getInstance().ifPresent(sla -> sla.setOffset(new int[]{x, y, z}));
                    } catch (NumberFormatException ignored) {
                        ClientUtils.displayChatMessage("Invalid Integer:" + args[1]);
                    }
                }
                break;
            case "rotate":
                if (args.length == 2) SLA.getInstance().ifPresent(sla -> sla.rotate(1));
                else {
                    int rotations = 0;
                    try {
                        rotations = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ignored) {
                        ClientUtils.displayChatMessage("Invalid Integer:" + args[2]);
                    }
                    int finalRotations = rotations;
                    SLA.getInstance().ifPresent(sla -> sla.rotate(finalRotations));
                }
                break;
            case "mirror":
                switch (args[2]) {
                    case "x":
                        SLA.getInstance().ifPresent(SLA::mirrorX);
                        break;
                    case "z":
                        SLA.getInstance().ifPresent(SLA::mirrorZ);
                        break;
                    default:
                        ClientUtils.displayChatMessage("Invalid option: available: x, z");
                }
                break;
            case "map":
                switch (args[2]) {
                    case "de":
                        SLA.instance = new SLA(Map.DEAD_END);
                        break;
                    case "bb":
                        SLA.instance = new SLA(Map.BAD_BLOOD);
                        break;
                    case "aa":
                        SLA.instance = new SLA(Map.ALIEN_ARCADIUM);
                        break;
                    case "p":
                        SLA.instance = new SLA(Map.PRISON);
                        break;
                    default:
                        ClientUtils.displayChatMessage(
                                "[Invalid option] options: de, bb, aa, p");
                }
                break;
            case "quick":
                switch (args[2]) {
                    //noinspection SpellCheckingInspection
                    case "mogi_a":
                        QuickSLA.mogi_a();
                        break;
                    //noinspection SpellCheckingInspection
                    case "ghxula":
                        QuickSLA.ghxula();
                        break;
                    //noinspection SpellCheckingInspection
                    case "ghxula-garden":
                        QuickSLA.ghxulaGarden();
                        break;
                    default:
                        //noinspection SpellCheckingInspection
                        ClientUtils.displayChatMessage(
                                "[Invalid option] options: mogi_a, ghxula, ghxula-garden");
                }
                break;
            default:
                ClientUtils.displayChatMessage(
                        "[Invalid option] options: off, offset, rotate, mirror, map");
        }
    }
}
