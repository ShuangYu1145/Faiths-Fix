package dev.faiths.utils;

import java.awt.Desktop;
import java.net.URI;

public class MiscUtils {
    public static void showURL(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            ClientUtils.LOGGER.error(e);
        }
    }
}
