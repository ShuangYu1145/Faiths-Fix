package com.github.stachelbeere1248.zombiesutils.timer.recorder.data;

import com.github.stachelbeere1248.zombiesutils.game.enums.Map;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.ISplitsData;
import com.google.gson.Gson;

import java.util.Arrays;

public class GameData implements ISplitsData {
    private final short[] segments;

    public GameData( Map map) throws IllegalStateException {
        switch (map) {
            case ALIEN_ARCADIUM:
                segments = new short[105];
                break;
            case DEAD_END:
            case BAD_BLOOD:
                segments = new short[30];
                break;
            case PRISON:
                segments = new short[31];
                break;
            default:
                throw new IllegalStateException("Not a map: " + map);
        }
        Arrays.fill(segments, (short) 0);
    }

    @Override
    
    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this.segments);
    }

    public void setSegment(int index, int ticks) {
        segments[index] = (short) ticks;
    }
}
