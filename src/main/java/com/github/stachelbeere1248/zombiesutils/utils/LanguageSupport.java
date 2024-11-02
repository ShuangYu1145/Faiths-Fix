package com.github.stachelbeere1248.zombiesutils.utils;

import java.util.Arrays;
import java.util.regex.Pattern;

@SuppressWarnings("SpellCheckingInspection")
public class LanguageSupport {
    private static final String[] LANGUAGES = {
            "EN",
            "FR",
            "DE"
    };

    public static boolean isLoss(String input) {
        final String[] words = {
                "§cGame Over!",
                "§cPartie terminée!",
                "§cDas Spiel ist vorbei!"
        };
        return Arrays.asList(words).contains(input);
    }

    public static boolean isWin(String input) {
        final String[] words = {
                "§aYou Win!",
                "§aVous avez gagné!",
                "§aDu gewinnst!"
        };
        return Arrays.asList(words).contains(input);
    }

    public static boolean containsHard(String input) {
        final String[] words = {
                "Hard Difficulty",
                "Difficulté Hard",
                "Hard Schwierigkeitsgrad",
                "困难",
                "困難"
        };
        return Arrays.stream(words).anyMatch(input::contains);
    }

    public static boolean containsRIP(String input) {
        final String[] words = {
                "RIP Difficulty",
                "Difficulté RIP",
                "RIP Schwierigkeitsgrad",
                "安息"
        };
        return Arrays.stream(words).anyMatch(input::contains);
    }
    public static boolean isHelicopterIncoming(String input) {
        final String[] words = {
                "The Helicopter is on its way! Hold out for 120 more seconds!"
        };
        return Arrays.stream(words).anyMatch(input::contains);
    }

    public static Pattern roundPattern(String language) {
        switch (language) {
            case "EN":
                return Pattern.compile("Round ([0-9]{1,3})");
            case "FR":
                return Pattern.compile("Manche ([0-9]{1,3})");
            case "DE":
                return Pattern.compile("Runde ([0-9]{1,3})");
            default:
                throw new IllegalStateException("Unexpected value: " + language);
        }
    }
    public static String[] getLanguages() {
        return LANGUAGES;
    }
}
