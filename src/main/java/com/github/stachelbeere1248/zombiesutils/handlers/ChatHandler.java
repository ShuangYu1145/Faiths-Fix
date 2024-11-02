package com.github.stachelbeere1248.zombiesutils.handlers;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.github.stachelbeere1248.zombiesutils.game.enums.Difficulty;
import com.github.stachelbeere1248.zombiesutils.utils.LanguageSupport;
import dev.faiths.event.Handler;
import dev.faiths.event.Listener;
import dev.faiths.event.impl.PacketEvent;
import net.minecraft.network.play.server.S02PacketChat;

import java.util.regex.Pattern;

public class ChatHandler implements Listener {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("§[0-9A-FK-ORZ]", Pattern.CASE_INSENSITIVE);

    public ChatHandler() {
    }

    public Handler<PacketEvent> packetEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE && event.getPacket() instanceof S02PacketChat) {
            difficultyChange(((S02PacketChat) event.getPacket()).getChatComponent().getUnformattedText());
        }
    };

    public void difficultyChange(String rawMessage) {
        ZombiesUtils.getInstance().getGameManager().getGame().ifPresent(
                game -> {
                    String message = STRIP_COLOR_PATTERN.matcher(rawMessage).replaceAll("").trim();
                    if (message.contains(":")) return;
                    if (LanguageSupport.containsHard(message)) {
                        game.changeDifficulty(Difficulty.HARD);
                    } else if (LanguageSupport.containsRIP(message)) {
                        game.changeDifficulty(Difficulty.RIP);
                    } else if (LanguageSupport.isHelicopterIncoming(message)) {
                        game.helicopter();
                    }
                }
        );
    }

    @Override
    public boolean isAccessible() {
        return true;
    }
}
