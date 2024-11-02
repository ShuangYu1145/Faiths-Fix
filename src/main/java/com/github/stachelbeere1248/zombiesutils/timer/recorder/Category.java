package com.github.stachelbeere1248.zombiesutils.timer.recorder;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.github.stachelbeere1248.zombiesutils.game.GameMode;
import com.github.stachelbeere1248.zombiesutils.timer.recorder.files.CategoryFile;

import java.io.File;

public class Category {
    private static String selectedCategory = ZombiesUtils.getInstance().getConfig().getDefaultCategory();
    public final CategoryFile[] categoryFiles = new CategoryFile[10];
    private final String name;

    public Category() {
        final File category;
        if (ZombiesUtils.isHypixel()) category = new File(new File("zombies", "splits"), selectedCategory);
        else category = new File(new File("zombies", "practise-splits"), selectedCategory);
        categoryFiles[0] = new CategoryFile(category, GameMode.DEAD_END);
        categoryFiles[1] = new CategoryFile(category, GameMode.DEAD_END_HARD);
        categoryFiles[2] = new CategoryFile(category, GameMode.DEAD_END_RIP);

        categoryFiles[3] = new CategoryFile(category, GameMode.BAD_BLOOD);
        categoryFiles[4] = new CategoryFile(category, GameMode.BAD_BLOOD_HARD);
        categoryFiles[5] = new CategoryFile(category, GameMode.BAD_BLOOD_RIP);

        categoryFiles[6] = new CategoryFile(category, GameMode.ALIEN_ARCADIUM);

        categoryFiles[7] = new CategoryFile(category, GameMode.PRISON);
        categoryFiles[8] = new CategoryFile(category, GameMode.PRISON_HARD);
        categoryFiles[9] = new CategoryFile(category, GameMode.PRISON_RIP);

        this.name = Category.selectedCategory;
    }

    public static void setSelectedCategory(String selectedCategory) {
        Category.selectedCategory = selectedCategory;
        ZombiesUtils.getInstance().getGameManager().getGame().ifPresent(game -> game.setCategory(new Category()));
    }

    public static String[] getCategories() {
        File dir;
        if (ZombiesUtils.isHypixel()) dir = new File("zombies" + File.separator + "splits");
        else dir = new File("zombies" + File.separator + "practise-splits");
        if (dir.isDirectory()) return dir.list();
        else return new String[0];
    }

    public CategoryFile getByGameMode( GameMode gameMode) {
        switch (gameMode) {
            case DEAD_END:
                return categoryFiles[0];
            case DEAD_END_HARD:
                return categoryFiles[1];
            case DEAD_END_RIP:
                return categoryFiles[2];
            case BAD_BLOOD:
                return categoryFiles[3];
            case BAD_BLOOD_HARD:
                return categoryFiles[4];
            case BAD_BLOOD_RIP:
                return categoryFiles[5];
            case ALIEN_ARCADIUM:
                return categoryFiles[6];
            case PRISON:
                return categoryFiles[7];
            case PRISON_HARD:
                return categoryFiles[8];
            case PRISON_RIP:
                return categoryFiles[9];
            default:
                throw new IllegalStateException("Unexpected value: " + gameMode);
        }
    }

    public String getName() {
        return name;
    }
}
