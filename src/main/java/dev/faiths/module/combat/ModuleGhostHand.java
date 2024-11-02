package dev.faiths.module.combat;

import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;

@SuppressWarnings("unused")
public class ModuleGhostHand extends CheatModule {
    public ModuleGhostHand() {
        super("GhostHand",Category.COMBAT);
    }

    public static boolean shouldHitThrough(Entity e) {
        if ((Minecraft.getMinecraft().thePlayer.getHeldItem() != null && (Minecraft.getMinecraft().thePlayer.getHeldItem().getItem().getUnlocalizedName().contains("pickaxe") || Minecraft.getMinecraft().thePlayer.getHeldItem().getItem().getUnlocalizedName().contains("hatchet") ||Minecraft.getMinecraft().thePlayer.getHeldItem().getItem().getUnlocalizedName().contains("shovel") || Minecraft.getMinecraft().thePlayer.getHeldItem().getItem().getUnlocalizedName().contains("hoe")) && !Minecraft.getMinecraft().thePlayer.getHeldItem().getItem().getUnlocalizedName().contains("shovelDiamond"))) {
            return true;
        }
        if (e.getDisplayName().getUnformattedText().length() < 4) {
            return false;
        }
        int color1 = 32;
        int color2 = 32;
        String name1 = e.getDisplayName().getFormattedText();
        String name2 = Minecraft.getMinecraft().thePlayer.getDisplayName().getFormattedText();
        if (name1.startsWith("\u00a7r\u00a76[\u00a72S\u00a76] ")) {
            name1 = name1.replace("\u00a7r\u00a76[\u00a72S\u00a76] ", "");
        }
        if (name2.startsWith("\u00a7r\u00a76[\u00a72S\u00a76] ")) {
            name2 = name2.replace("\u00a7r\u00a76[\u00a72S\u00a76] ", "");
        }
        String title = Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1) == null ? " " : Minecraft.getMinecraft().theWorld.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName();
        if (name1.charAt(2) == '\u00a7') {
            color1 = name1.charAt(3);
        } else if (name1.charAt(0) == '\u00a7' && name1.charAt(1) != 'r') {
            color1 = name1.charAt(1);
        }
        if (name2.charAt(2) == '\u00a7') {
            color2 = name2.charAt(3);
        } else if (name2.charAt(0) == '\u00a7' && name2.charAt(2) != '\u00a7' && name2.charAt(1) == 'r' && title.charAt(0) == '\u00a7') {
            color2 = title.charAt(1);
        }
        return color1 != 32 && color2 != 32 && color1 == color2;
    }

}
