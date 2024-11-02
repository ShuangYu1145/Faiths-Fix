package com.github.stachelbeere1248.zombiesutils.timer.recorder.files;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.github.stachelbeere1248.zombiesutils.game.GameMode;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.FileManager;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.data.CategoryData;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;

public class CategoryFile extends File {
    private final CategoryData data;
    private final GameMode gameMode;

    public CategoryFile(File category,  GameMode gameMode) {
        // Game-directory -> custom category -> file named "MAP_DIFFICULTY.times"
        // Content encoded in StandardCharsets.UTF_16
        super(category, gameMode + ".times");
        this.gameMode = gameMode;
        this.data = FileManager.categoryReadOrCreate(this);
    }

    public short getBestSegment(int round) {
        return this.data.getBestSegment(round - 1);
    }

    public void setBestSegment(int round, int ticks) {
        this.data.setBestSegment(round - 1, ticks);

        try {
            FileManager.writeDataToFile(this, this.data);
        } catch (Exception e) {
            ZombiesUtils.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Error saving segment to splits-file. Please Contact Stachelbeere1248."));
        }
    }

    public int getPersonalBest(int round) {
        return data.getPersonalBest(round - 1);
    }

    public void setPersonalBest(int round, int ticks) {
        this.data.setPersonalBest(round - 1, ticks);

        try {
            FileManager.writeDataToFile(this, this.data);
        } catch (Exception e) {
            ZombiesUtils.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Error saving pb to splits-file. Please Contact Stachelbeere1248."));
        }
    }

    public GameMode getGameMode() {
        return gameMode;
    }

}
