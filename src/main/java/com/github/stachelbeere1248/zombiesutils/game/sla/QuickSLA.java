package com.github.stachelbeere1248.zombiesutils.game.sla;

import com.github.stachelbeere1248.zombiesutils.game.enums.Map;
import com.github.stachelbeere1248.zombiesutils.game.windows.SLA;

@SuppressWarnings("SpellCheckingInspection")
public class QuickSLA {
    public static void mogi_a() {
        SLA.instance = new SLA(Map.BAD_BLOOD);
        SLA.instance.rotate(3);
        SLA.instance.setOffset(new int[]{-3, 35, -9});
    }

    public static void ghxula() {
        SLA.instance = new SLA(Map.DEAD_END);
        SLA.instance.rotate(1);
        SLA.instance.setOffset(new int[]{27, 35, 5});
    }

    public static void ghxulaGarden() {
        SLA.instance = new SLA(Map.DEAD_END);
        SLA.instance.rotate(1);
        SLA.instance.setOffset(new int[]{13, 53, -8});
    }
}
