package com.github.stachelbeere1248.zombiesutils.timer.recorder;

import com.github.stachelbeere1248.zombiesutils.timer.recorder.data.CategoryData;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.files.CategoryFile;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileManager {
    private static CategoryData readDataFromFile(File file) throws FileNotFoundException, JsonSyntaxException {
        if (!file.exists()) throw new FileNotFoundException();

        String dataJson;
        Gson gson = new Gson();
        try {
            dataJson = FileUtils.readFileToString(file, StandardCharsets.UTF_16);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (dataJson == null || dataJson.trim().isEmpty()) throw new JsonSyntaxException("File empty");

        return gson.fromJson(dataJson, CategoryData.class);
    }

    public static void createDataFile(File file, ISplitsData data) {
        try {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
            writeDataToFile(file, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeDataToFile(File file, ISplitsData data) throws IOException {
        FileUtils.writeStringToFile(file, data.toJSON(), StandardCharsets.UTF_16);
    }

    public static CategoryData categoryReadOrCreate(CategoryFile file) {
        CategoryData data;
        try {
            data = FileManager.readDataFromFile(file);
        } catch (FileNotFoundException | JsonSyntaxException ignored) {
            data = new CategoryData(file.getGameMode().getMap());
            FileManager.createDataFile(file, data);
        }
        return data;
    }
}
