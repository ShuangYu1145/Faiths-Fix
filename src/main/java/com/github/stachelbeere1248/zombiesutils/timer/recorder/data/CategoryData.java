package com.github.stachelbeere1248.zombiesutils.timer.recorder.data;

import com.github.stachelbeere1248.zombiesutils.game.enums.Map;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.ISplitsData;
import com.google.gson.Gson;

import java.util.Arrays;

public class CategoryData implements ISplitsData {
    private final short[] bestSegments; //in ticks, max ~27 min
    private final int[] personalBests; //in ticks,

    public CategoryData(Map map) throws IllegalStateException {
        switch (map) {
            case ALIEN_ARCADIUM:
                bestSegments = new short[105];
                personalBests = new int[105];
                break;
            case DEAD_END:
            case BAD_BLOOD:
                bestSegments = new short[30];
                personalBests = new int[30];
                break;
            case PRISON:
                bestSegments = new short[30];
                personalBests = new int[31];
                break;
            default:
                throw new IllegalStateException("Not a map: " + map);
        }
        Arrays.fill(bestSegments, (short) 0);
        Arrays.fill(personalBests, 0);
    }

    @Override
    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this, CategoryData.class);
    }

    public short getBestSegment(int index) {
        return bestSegments[index];
    }

    public int getPersonalBest(int index) {
        return personalBests[index];
    }

    public void setBestSegment(int index, int ticks) {
        bestSegments[index] = (short) ticks;
    }

    public void setPersonalBest(int index, int ticks) {
        personalBests[index] = ticks;
    }
}