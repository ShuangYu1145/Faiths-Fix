package dev.faiths.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class ServerUtils {

    private static Minecraft mc = Minecraft.getMinecraft();
    private static boolean hypixel;
    private static boolean fakeHypixel;

    public static boolean serverIs(Servers server) {
        /*if (!mc.isSingleplayer() && ModuleHypixelUtils.getCurrentServer() != null) {
            return ModuleHypixelUtils.getCurrentServer().equals(server);
        }*/

        return false;
    }

    public static void checkHypixel(ServerData serverData) {
        if (serverData.serverIP.toLowerCase().contains("hypixel.net") && !hostModified("hypixel") && !fakeHypixel) {
            hypixel = true;
        } else {
            hypixel = false;
            // ModuleHypixelUtils.setCurrentServer(Servers.NONE);
        }
    }

    public static boolean isHypixel() {
        return mc.theWorld != null && mc.ingameGUI.getTabList() != null && mc.ingameGUI.getTabList().getHeader().getUnformattedText().toLowerCase(Locale.ROOT).contains("hypixel.net");
    }

    public static boolean hostModified(String server) {
        Path path = Paths.get(System.getenv("SystemDrive") + "\\Windows\\System32\\drivers\\etc\\hosts");

        if (Files.notExists(path)) {
            return false;
        } else {
            try {
                return Files.lines(path).anyMatch(s -> s.toLowerCase().contains(server));
            } catch (IOException e) {
                mc.getNetHandler().getNetworkManager().closeChannel(new ChatComponentText(EnumChatFormatting.RED + "Connection error! Contact staff"));
                return true;
            }
        }
    }
}
