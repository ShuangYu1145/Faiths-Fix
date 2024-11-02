package com.github.stachelbeere1248.zombiesutils.timer;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.github.stachelbeere1248.zombiesutils.game.GameMode;
import com.github.stachelbeere1248.zombiesutils.game.enums.Difficulty;
import com.github.stachelbeere1248.zombiesutils.game.enums.Map;
import com.github.stachelbeere1248.zombiesutils.game.windows.SLA;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.Category;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.files.CategoryFile;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.files.GameFile;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class Game {
    private final Timer timer;
    private final GameFile gameFile;
    private final boolean roundOneRecorded;
    private GameMode gameMode;
    private Category category;
    private int round;
    private boolean escaping;

    public Game(final Map map, final String serverNumber) {
        this.gameMode = GameMode.getNormalForMap(map);
        this.timer = new Timer();
        this.gameFile = new GameFile(serverNumber, map);
        this.category = new Category();
        this.round = 1;
        this.roundOneRecorded = true;
        if (ZombiesUtils.getInstance().getConfig().isSlaToggled()) SLA.instance = new SLA(map);
    }
    public Game(final Map map, final String serverNumber, final int round) {
        this.gameMode = GameMode.getNormalForMap(map);
        this.timer = new Timer();
        this.gameFile = new GameFile(serverNumber, map);
        this.category = new Category();
        this.round = round;
        this.roundOneRecorded = (round == 1);
        if (ZombiesUtils.getInstance().getConfig().isSlaToggled()) SLA.instance = new SLA(map);

    }

    public Timer getTimer() {
      return this.timer;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
    public void changeDifficulty(final Difficulty difficulty) {
        this.gameMode = this.gameMode.appliedDifficulty(difficulty);
    }
    public int getRound() {
        return round;
    }
    public GameMode getGameMode() {
        return gameMode;
    }

    public void pass(int round) {
        if ((round == 0) || (this.round == round + 1) || (this.timer.getRoundTime() < 100)) {
            ZombiesUtils.getInstance().getLogger().debug("SPLIT CANCELLED");
            return;
        }
        try {
            record();
        } catch (Exception e) {
            ZombiesUtils.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§cError recording splits"));
        }
        this.timer.split();
        this.round = round + 1;
    }
    public void helicopter() {
        if (!gameMode.isMap(Map.PRISON)) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§cEscaping without playing prison???"));
            ZombiesUtils.getInstance().getLogger().error(Thread.currentThread().getStackTrace());
            return;
        }
        this.escaping = true;
        this.pass(30);
    }

    private void record() {
            this.compareSegment();
            if (this.roundOneRecorded) this.compareBest();
            this.gameFile.setSegment(this.round, this.timer.getRoundTime());
    }

    public void compareSegment() throws IndexOutOfBoundsException {
        if (this.escaping) return;
        final CategoryFile categoryFile = this.category.getByGameMode(this.gameMode);
        final short bestSegment = categoryFile.getBestSegment(round);
        final int roundTime = this.getTimer().getRoundTime();

        if (bestSegment == (short) 0) categoryFile.setBestSegment(round, roundTime);
        else if (roundTime < bestSegment) categoryFile.setBestSegment(round, roundTime);
        final RecordMessageSender recordMessageSender = new RecordMessageSender(this.category.getName(), round, false, roundTime, bestSegment);
        recordMessageSender.roundSplit();
        recordMessageSender.sendRecordMessage();
    }

    public void compareBest() throws IndexOutOfBoundsException {
        final CategoryFile categoryFile = this.category.getByGameMode(this.gameMode);
        final int round = this.escaping ? 31 : this.round;
        final int personalBest = categoryFile.getPersonalBest(round);
        final int gameTime = this.timer.getGameTime();

        if (personalBest == 0) categoryFile.setPersonalBest(round, gameTime);
        else if (gameTime < personalBest) categoryFile.setPersonalBest(round, gameTime);
        final RecordMessageSender recordMessageSender = new RecordMessageSender(category.getName(), round, true, gameTime, personalBest);
        if (!escaping) recordMessageSender.gameSplit();
        else recordMessageSender.helicopterSplit();
        recordMessageSender.sendRecordMessage();
    }
}
