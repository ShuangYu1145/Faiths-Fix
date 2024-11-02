package com.github.stachelbeere1248.zombiesutils.game;

import com.github.stachelbeere1248.zombiesutils.game.enums.Difficulty;
import com.github.stachelbeere1248.zombiesutils.game.enums.Map;

public enum GameMode {
    DEAD_END(Map.DEAD_END, Difficulty.NORMAL), DEAD_END_HARD(Map.DEAD_END, Difficulty.HARD), DEAD_END_RIP(Map.DEAD_END, Difficulty.RIP),
    BAD_BLOOD(Map.BAD_BLOOD, Difficulty.NORMAL), BAD_BLOOD_HARD(Map.BAD_BLOOD, Difficulty.HARD), BAD_BLOOD_RIP(Map.BAD_BLOOD, Difficulty.RIP),
    ALIEN_ARCADIUM(Map.ALIEN_ARCADIUM, Difficulty.NORMAL),
    PRISON(Map.PRISON, Difficulty.NORMAL), PRISON_HARD(Map.PRISON, Difficulty.HARD), PRISON_RIP(Map.PRISON, Difficulty.RIP);
    private final Map map;
    private final Difficulty difficulty;
    GameMode(final Map map, final Difficulty difficulty) {
        this.map = map;
        this.difficulty = difficulty;
    }

    public Map getMap() {
        return this.map;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }
    public GameMode appliedDifficulty(final Difficulty difficulty) {
        switch (this.map) {
            case DEAD_END:
                switch (difficulty) {
                    case NORMAL:
                        return DEAD_END;
                    case HARD:
                        return DEAD_END_HARD;
                    case RIP:
                        return DEAD_END_RIP;
                }
            case BAD_BLOOD:
                switch (difficulty) {
                    case NORMAL:
                        return BAD_BLOOD;
                    case HARD:
                        return BAD_BLOOD_HARD;
                    case RIP:
                        return BAD_BLOOD_RIP;
                }
            case PRISON:
                switch (difficulty) {
                    case NORMAL:
                        return PRISON;
                    case HARD:
                        return PRISON_HARD;
                    case RIP:
                        return PRISON_RIP;
                }
            case ALIEN_ARCADIUM:
                return ALIEN_ARCADIUM;
            default:
                throw new IllegalStateException("Invalid Map: " + this.map);
        }
    }
    public boolean isMap(Map map) {
        return this.getMap() == map;
    }

    public static GameMode getNormalForMap(final Map map) {
        switch (map) {
            case DEAD_END:
                return DEAD_END;
            case BAD_BLOOD:
                return BAD_BLOOD;
            case ALIEN_ARCADIUM:
                return ALIEN_ARCADIUM;
            case PRISON:
                return PRISON;
            default:
                throw new IllegalStateException("Unexpected value: " + map);
        }
    }
}
