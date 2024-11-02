package com.github.stachelbeere1248.zombiesutils.handlers;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.github.stachelbeere1248.zombiesutils.game.waves.Prefix;
import com.github.stachelbeere1248.zombiesutils.game.waves.Wave;
import com.github.stachelbeere1248.zombiesutils.game.windows.Room;
import com.github.stachelbeere1248.zombiesutils.game.windows.SLA;
import com.github.stachelbeere1248.zombiesutils.timer.Game;
import com.github.stachelbeere1248.zombiesutils.utils.Scoreboard;
import dev.faiths.event.Handler;
import dev.faiths.event.Listener;
import dev.faiths.event.impl.Render2DEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

import java.util.Objects;

public class RenderGameOverlayHandler implements Listener {
    private final byte[] clicks = new byte[20];
    private final FontRenderer fontRenderer;
    private int rl = 0;
    private int clickPointer = 0;

    public RenderGameOverlayHandler() {
        this.fontRenderer = Objects.requireNonNull(Minecraft.getMinecraft().fontRendererObj, "FontRenderer must not be null!");
    }

    private static String getTimeString(long timerTicks) {
        final long minutesPart = (timerTicks * 50) / 60000;
        final long secondsPart = ((timerTicks * 50) % 60000) / 1000;
        final long tenthSecondsPart = ((timerTicks * 50) % 1000) / 100;
        return String.format("%d:%02d.%d", minutesPart, secondsPart, tenthSecondsPart);
    }

    void toggleRL() {
        if (rl == 0) rl = ZombiesUtils.getInstance().getConfig().getOffset();
        else rl = 0;
    }

    public Handler<Render2DEvent> render2DEventHandler = this::onRenderGameOverlay;

    public void onRenderGameOverlay(Render2DEvent render2DEvent) {
        ZombiesUtils.getInstance().getGameManager().getGame().ifPresent(
                game -> {
                    renderTime(game.getTimer().getRoundTime());
                    renderSpawnTime(
                            game
                    );
                }
        );

        if (!Minecraft.getMinecraft().gameSettings.showDebugInfo) SLA.getInstance().ifPresent(sla -> renderSla(sla.getRooms()));
    }

    private void renderTime(short timerTicks) {
        if (Scoreboard.isNotZombies()) return;

        final String time = getTimeString(timerTicks);
        final int width = fontRenderer.getStringWidth(time);

        final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        final int screenWidth = scaledResolution.getScaledWidth();
        final int screenHeight = scaledResolution.getScaledHeight();

        fontRenderer.drawStringWithShadow(
                time,
                screenWidth - width,
                screenHeight - fontRenderer.FONT_HEIGHT,
                0xFFFFFF
        );
    }

    private void renderSla(Room[] rooms) {
        int y = 0;
        for (Room room : rooms) {
            if (ZombiesUtils.getInstance().getConfig().isSlaShortened() && room.getActiveWindowCount() == 0) continue;
            fontRenderer.drawStringWithShadow(
                    room.getSlaString(),
                    1,
                    1 + y * fontRenderer.FONT_HEIGHT,
                    0xFFFFFF
            );
            y++;
        }
    }
    private void renderSpawnTime(final Game game) {
        if (!ZombiesUtils.getInstance().getConfig().getSST() || Scoreboard.isNotZombies()) return;
        final ScaledResolution scaledResolution = new ScaledResolution(Minecraft.getMinecraft());
        final int screenWidth = scaledResolution.getScaledWidth();
        final int screenHeight = scaledResolution.getScaledHeight();
        final Wave[] round = ZombiesUtils.getInstance().getGameData().getRound(game.getGameMode(), game.getRound()).getWaves();
        final int roundTime = game.getTimer().getRoundTime();
        final int length = round.length + 1;
        int heightIndex = 0;
        int color = 0xFFFF55;
        boolean faded = false;

        for (Wave wave : round) {
            final short spawnTime = (short) (wave.getTime() + rl);
            if (roundTime > spawnTime) {
                if (!ZombiesUtils.getInstance().getConfig().isSpawntimeShortened()) faded = true;
                else {
                    heightIndex++;
                    continue;
                }
            } else faded = false;
            final String spawnTimeString = "  W" + (heightIndex + 1) + ": " + getTimeString(spawnTime);
            int width = fontRenderer.getStringWidth(spawnTimeString);
            fontRenderer.drawStringWithShadow(
                    spawnTimeString,
                    screenWidth - width,
                    screenHeight - fontRenderer.FONT_HEIGHT * (length - heightIndex),
                    faded ? 0x666666 : color
            );
            for (Prefix prefix : wave.getPrefixes()) {
                final String prefixString = prefix.getPrefix() + " ";
                width += fontRenderer.getStringWidth(prefixString);
                fontRenderer.drawStringWithShadow(
                        prefixString,
                        screenWidth - width,
                        screenHeight - fontRenderer.FONT_HEIGHT * (length - heightIndex),
                        faded ? prefix.getFadedColor(3,5) : prefix.getColor()
                );
            }
            if (!faded) color = 0xAAAAAA;
            heightIndex++;
        }
    }

    public int getClicks() {
        int i = 0;
        for (byte tick : clicks) {
            i += tick;
        }
        return i;
    }

    public void addClick() {
        clicks[clickPointer]++;
    }

    public void tick() {
        clickPointer = (clickPointer + 1) % 20;
        clicks[clickPointer] = 0;
    }

    @Override
    public boolean isAccessible() {
        return true;
    }
}
