package com.github.stachelbeere1248.zombiesutils.mixin;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.github.stachelbeere1248.zombiesutils.utils.InvalidMapException;
import com.github.stachelbeere1248.zombiesutils.utils.LanguageSupport;
import com.github.stachelbeere1248.zombiesutils.utils.ScoardboardException;
import com.github.stachelbeere1248.zombiesutils.utils.Scoreboard;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.ChatComponentText;

public class MixinNetHandlerPlayClient {
    public static boolean zombies_utils$alienUfoOpened;
    public static void zombies_utils$handleSound(S29PacketSoundEffect packet) {
        if (Scoreboard.isNotZombies()) return;
        final String soundEffect = packet.getSoundName();

        if (!(
                soundEffect.equals("mob.wither.spawn")
                || (soundEffect.equals("mob.guardian.curse")
                && !zombies_utils$alienUfoOpened)
        )) return;

        zombies_utils$alienUfoOpened = soundEffect.equals("mob.guardian.curse");

        try {
            ZombiesUtils.getInstance().getGameManager().splitOrNew(Scoreboard.getRound());
        } catch (ScoardboardException | InvalidMapException e) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§cFailed to start or split timer. Please send a log to Stachelbeere1248."));
            ZombiesUtils.getInstance().getLogger().error(e.fillInStackTrace());
        }
    }

    public static void zombies_utils$handleTitle(S45PacketTitle packet) {
        if (packet.getType() != S45PacketTitle.Type.TITLE) return;
        if (Scoreboard.isNotZombies()) return;
        final String message = packet.getMessage().getUnformattedText().trim();
        String serverNumber;
            serverNumber = Scoreboard.getServerNumber().orElse("");
        if (LanguageSupport.isWin(message)) ZombiesUtils.getInstance().getGameManager().endGame(serverNumber,true);
        if (LanguageSupport.isLoss(message)) ZombiesUtils.getInstance().getGameManager().endGame(serverNumber, false);
    }

}
