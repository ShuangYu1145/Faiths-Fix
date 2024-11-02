package dev.faiths.command.impl;

import dev.faiths.command.AbstractCommand;
import dev.faiths.utils.ClientUtils;
import ltd.guimc.silencefix.Messages;
import ltd.guimc.silencefix.SilenceFixIRC;

import java.util.Arrays;

public class SFCommand extends AbstractCommand {
    public SFCommand() {
        super("sf");
    }

    @Override
    public void execute(final String[] args) {
        if (args.length > 1) {
            switch (args[1]) {
                case "register":
                    String username = args[2];
                    String password = args[3];
                    String qq = args[4];
                    String ver = args[5];
                    SilenceFixIRC.Instance.sendPacket(Messages.createRegister(username, password, SilenceFixIRC.Instance.hwid, qq, ver));
                    break;
                case "login":
                    username = args[2];
                    password = args[3];
                    SilenceFixIRC.Instance.sendPacket(Messages.createLogin(username, password, SilenceFixIRC.Instance.hwid));
                    break;
                case "send":
                    String msg = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    SilenceFixIRC.Instance.sendPacket(Messages.createChat(msg));
                    break;
                case "verify":
                    qq = args[2];
                    SilenceFixIRC.Instance.sendPacket(Messages.createRequestEmailCode(qq));
                    break;
                case "list":
                    SilenceFixIRC.Instance.sendPacket(Messages.createQueryClients());
                    break;
                case "reconnect":
                    try {
                        SilenceFixIRC.Instance.connect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case "debug":
                    ClientUtils.displayChatMessage(SilenceFixIRC.Instance.ircUserMap.toString());
                    break;
                default:
                    chatSyntax(new String[]{"register <username> <password> <qq> <verifyKey (use .sf verify <qq> to get)>", "login <username> <password>", "send <message>", "verify <qq>", "reconnect"});
                    break;
            }
            return;
        }
        chatSyntax(new String[]{"list", "register <username> <password> <qq> <verifyKey (use .sf verify <qq> to get)>", "login <username> <password>", "send <message>", "verify <qq>", "reconnect"});
    }
}
