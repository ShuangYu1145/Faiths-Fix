package com.github.stachelbeere1248.zombiesutils.timer;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;

public class RecordMessageSender {
    private final StringBuilder recordMessage;
    private final int roundTime;
    private final int gameTime;
    private final int oldPB;
    private final int oldBS;
    private final int round;
    private final String deltaString;
    private final String timeString;
    private final boolean cumulative;
    private String copyString;
    public RecordMessageSender(final String categoryName, final int round, final boolean cumulative, final int newTime, final int oldTime) {
        this.recordMessage = new StringBuilder(
                "§l§a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬\n§e Category: §d" + categoryName
        );
        if (cumulative) {
            this.gameTime = newTime;
            this.oldPB = oldTime;
            this.roundTime = 0;
            this.oldBS = 0;
        } else {
            this.gameTime = 0;
            this.oldPB = 0;
            this.roundTime = newTime;
            this.oldBS = oldTime;
        }
        this.deltaString = oldTime  != 0 ? formattedDelta(newTime, oldTime) : "";
        this.timeString = formattedTime(newTime);
        this.cumulative = cumulative;
        this.round = round;
    }

    public void sendRecordMessage() {
        final ChatComponentText message = new ChatComponentText(
                this.recordMessage
                .append("\n§l§a▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬")
                .toString()
        );
        message.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, this.copyString)));
        Minecraft.getMinecraft().thePlayer.addChatMessage(message);
    }

    public void gameSplit() {
        final String announcement = gameTime < oldPB && ZombiesUtils.getInstance().getConfig().getAnnouncePB() ?
                "\n§e§l***§6§l NEW PERSONAL BEST! §e§l***" : "";
        this.recordMessage.append(announcement)
                .append("\n§cRound ")
                .append(round)
                .append("§e finished at §a")
                .append(timeString)
                .append(" §9")
                .append(deltaString)
                .append("§e!");
        this.copyString = deltaString.isEmpty() ?
                String.format("Round %d finished at %s!", round, timeString) :
                String.format("Round %d finished at %s (%s)!", round, timeString, deltaString);
    }

    public void roundSplit() {
        final String announcement = roundTime < oldBS && ZombiesUtils.getInstance().getConfig().getAnnouncePB() ?
                "\n§e§l***§6§l NEW BEST SEGMENT! §e§l***" : "";
        final String timeString = formattedTime(roundTime);
        final String deltaString = oldBS != 0 ? formattedDelta(roundTime, oldBS) : "";
        this.recordMessage.append(announcement)
                .append("\n§cRound ")
                .append(round)
                .append("§e took §a")
                .append(timeString)
                .append(" §9")
                .append(deltaString)
                .append("§e!");
        this.copyString = deltaString.isEmpty() ?
                String.format("Round %d took %s!", round, timeString) :
                String.format("Round %d took %s (%s)!", round, timeString, deltaString);
    }
    public void helicopterSplit() {
        final String announcement = gameTime < oldPB && ZombiesUtils.getInstance().getConfig().getAnnouncePB() ?
                "\n§e§l***§6§l NEW PERSONAL BEST! §e§l***" : "";
        this.recordMessage.append(announcement)
                .append("§8§lHelicopter §r§ecalled at §a")
                .append(timeString)
                .append(" §9")
                .append(deltaString)
                .append("§e!");
        this.copyString = deltaString.isEmpty() ?
                String.format("Helicopter called at %s!", timeString) :
                String.format("Helicopter called at %s (%s)!", timeString, deltaString);
    }

    private String formattedTime(int time) {
        time *= 50;
        return String.format("%d:%02d.%d%d",
                time / 60000,
                (time % 60000) / 1000,
                (time % 1000) / 100,
                (time % 100) / 10
        );
    }

    private String formattedDelta(int newTime, int oldTime) {
        final double delta = (double) (newTime - oldTime) / 20;
        return String.format("%+.2f", delta);
    }
}
