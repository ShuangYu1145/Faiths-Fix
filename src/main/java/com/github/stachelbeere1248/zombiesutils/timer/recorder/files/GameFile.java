package com.github.stachelbeere1248.zombiesutils.timer.recorder.files;


import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.github.stachelbeere1248.zombiesutils.game.enums.Map;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.FileManager;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.data.GameData;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class GameFile extends File {
    private final GameData data;

    public GameFile(String serverNumber, Map map) {
        super("zombies" + File.separator + "runs", formattedTime() + "_" + serverNumber + ".seg");
        this.data = new GameData(map);
        FileManager.createDataFile(this, this.data);
    }

    
    private static String formattedTime() {
        final LocalDateTime dateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(':', '-').replaceFirst("T", "_");
    }

    public void setSegment(int round, int ticks) {
        this.data.setSegment(round - 1, ticks);

        try {
            FileManager.writeDataToFile(this, this.data);
        } catch (Exception e) {
            ZombiesUtils.getInstance().getLogger().error(ExceptionUtils.getStackTrace(e));
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Error saving segment to run-file. Please Contact Stachelbeere1248."));
        }
    }

}
