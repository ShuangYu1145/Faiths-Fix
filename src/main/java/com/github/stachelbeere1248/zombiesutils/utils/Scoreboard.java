package com.github.stachelbeere1248.zombiesutils.utils;

import com.github.stachelbeere1248.zombiesutils.ZombiesUtils;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Scoreboard {
    @SuppressWarnings("UnnecessaryUnicodeEscape")
    private static final Pattern SIDEBAR_EMOJI_PATTERN = Pattern.compile("[\uD83D\uDD2B\uD83C\uDF6B\uD83D\uDCA3\uD83D\uDC7D\uD83D\uDD2E\uD83D\uDC0D\uD83D\uDC7E\uD83C\uDF20\uD83C\uDF6D\u26BD\uD83C\uDFC0\uD83D\uDC79\uD83C\uDF81\uD83C\uDF89\uD83C\uDF82]+");
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("§[0-9A-FK-ORZ]", Pattern.CASE_INSENSITIVE);
    private static final Pattern SERVER_NUMBER_PATTERN = Pattern.compile(".*([mLM][0-9A-Z]+)");

    private static String title = null;
    private static List<String> lines = null;

    /**
     * Overrides {@link #title} and {@link #lines} if a valid scoreboard is present
     */
    public static void refresh() {
        // Null-checks
        if ((Minecraft.getMinecraft() == null)
                || (Minecraft.getMinecraft().theWorld == null)
                || Minecraft.getMinecraft().isSingleplayer()
        ) return;
        if (Minecraft.getMinecraft().thePlayer == null) return;
        net.minecraft.scoreboard.Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        ScoreObjective sidebar = scoreboard.getObjectiveInDisplaySlot(1);
        if (sidebar == null) return;

        // get
        title = STRIP_COLOR_PATTERN.matcher(sidebar.getDisplayName().trim()).replaceAll("");
        Collection<Score> scoreCollection = scoreboard.getSortedScores(sidebar);
        List<Score> filteredScores = scoreCollection
                .stream()
                .filter(input -> input.getPlayerName() != null && !input.getPlayerName().startsWith("#"))
                .collect(Collectors.toList());

        List<Score> scores = (filteredScores.size() > 15) ? Lists.newArrayList(Iterables.skip(filteredScores, scoreCollection.size() - 15)) : filteredScores;
        scores = Lists.reverse(scores);

        lines = new ArrayList<>(scores.size());
        for (Score score : scores) {
            ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            String scoreboardLine = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()).trim();
            lines.add(STRIP_COLOR_PATTERN.matcher(SIDEBAR_EMOJI_PATTERN.matcher(scoreboardLine).replaceAll("")).replaceAll(""));
        }
    }

    public static int getRound() {
        String line;
        try {
            line = lines.get(2);
        } catch (IndexOutOfBoundsException | NullPointerException ignored) {
            return 0;
        }
        final Pattern ROUND_LINE_PATTERN = LanguageSupport.roundPattern(ZombiesUtils.getInstance().getConfig().getLanguage());

        String string = ROUND_LINE_PATTERN.matcher(line).replaceAll("$1");

        byte round;
        try {
            round = Byte.parseByte(string);
            ZombiesUtils.getInstance().getLogger().debug("Round: " + round);
            return round;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static Optional<String> getServerNumber() {
        String line;
        try {
            line = lines.get(0);
        } catch (IndexOutOfBoundsException | NullPointerException ignored) {
            return Optional.empty();
        }

        String string = SERVER_NUMBER_PATTERN.matcher(line).replaceAll("$1");
        ZombiesUtils.getInstance().getLogger().debug("Servernumber: " + string);
        return Optional.ofNullable(string);
    }

    public static boolean isNotZombies() {
        return (!"ZOMBIES".equals(title));
    }
}