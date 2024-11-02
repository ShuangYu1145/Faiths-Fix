package com.github.stachelbeere1248.zombiesutils.game;

import com.github.stachelbeere1248.zombiesutils.ResourceLoader;
import com.github.stachelbeere1248.zombiesutils.game.waves.Round;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class GameData {
    private final Round[][] roundData;
    public GameData() {
        roundData = new Round[10][];
        roundData[0] = readFromFile("data/rounds/DEAD_END_NORMAL.json");
        roundData[1] = readFromFile("data/rounds/DEAD_END_HARD.json");
        roundData[2] = readFromFile("data/rounds/DEAD_END_RIP.json");
        roundData[3] = readFromFile("data/rounds/BAD_BLOOD_NORMAL.json");
        roundData[4] = readFromFile("data/rounds/BAD_BLOOD_HARD.json");
        roundData[5] = readFromFile("data/rounds/BAD_BLOOD_RIP.json");
        roundData[6] = readFromFile("data/rounds/ALIEN_ARCADIUM.json");
        roundData[7] = readFromFile("data/rounds/PRISON_NORMAL.json");
        roundData[8] = readFromFile("data/rounds/PRISON_HARD.json");
        roundData[9] = readFromFile("data/rounds/PRISON_RIP.json");
    }

    public Round getRound(GameMode gameMode, int round) {
        final Round[] selected;
        switch (gameMode) {
            case DEAD_END:
                return roundData[0][round-1];
            case DEAD_END_HARD:
                return roundData[1][round-1];
            case DEAD_END_RIP:
                return roundData[2][round-1];
            case BAD_BLOOD:
                return roundData[3][round-1];
            case BAD_BLOOD_HARD:
                return roundData[4][round-1];
            case BAD_BLOOD_RIP:
                return roundData[5][round-1];
            case ALIEN_ARCADIUM:
                return roundData[6][round-1];
            case PRISON:
                return roundData[7][round-1];
            case PRISON_HARD:
                return roundData[8][round-1];
            case PRISON_RIP:
                return roundData[9][round-1];
            default:
                throw new IllegalStateException("Invalid GameMode: " + gameMode);
        }
    }

    private Round[] readFromFile(final String resourcePath) {
        final JsonElement roundsJsonElement = ResourceLoader.readJsonResource(resourcePath).orElseThrow(RuntimeException::new);
        return new Gson().fromJson(roundsJsonElement, Round[].class);
    }
}
