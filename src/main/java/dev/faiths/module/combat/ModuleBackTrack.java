package dev.faiths.module.combat;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.event.impl.WorldEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.value.ValueInt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleBackTrack extends CheatModule {
    public ModuleBackTrack() {
        super("BackTrack", Category.COMBAT);
    }

    private ValueInt length = new ValueInt("BackTrackLength", 5, 1, 20);

    public static LinkedHashMap<EntityPlayer, ArrayList<AxisAlignedBB>> playerBBox = new LinkedHashMap<>();

    public static boolean hand = true;

    @Override
    public void onEnable() {
        playerBBox.clear();
    }

    private Handler<UpdateEvent> updateEventHandler = event -> {
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            playerBBox.computeIfAbsent(player, k -> new ArrayList<>());
            playerBBox.get(player).add(player.boundingBox);

            while (true) {
                if (playerBBox.get(player).size() > length.getValue()) {
                    playerBBox.get(player).remove(0);
                } else {
                    break;
                }
            }
        }
    };

    private Handler<WorldEvent> worldEventHandler = event -> {
        playerBBox.clear();
    };

    public static AxisAlignedBB getClosedBBox(EntityPlayer player) {
        if (mc.thePlayer.getDistanceToEntity(player) <= ModuleKillAura.getRange()) return predictPlayerBBox(player);
        if (!Faiths.moduleManager.getModule(ModuleBackTrack.class).getState()) return predictPlayerBBox(player);
        if (playerBBox.get(player) == null) return predictPlayerBBox(player);
        AxisAlignedBB nearestBBox = predictPlayerBBox(player);
        double nearestDistance = mc.thePlayer.getClosestDistanceToBBox(nearestBBox);

        for (AxisAlignedBB bbox : playerBBox.get(player)) {
            float closestDistanceToBBox = mc.thePlayer.getClosestDistanceToBBox(bbox);
            if (closestDistanceToBBox < nearestDistance) {
                nearestDistance = closestDistanceToBBox;
                nearestBBox = bbox;
            }
        }

        return nearestBBox;
    }

    public static AxisAlignedBB predictPlayerBBox(EntityPlayer player) {
        double x = player.posX - player.lastTickPosX;
        double y = player.posY - player.lastTickPosY;
        double z = player.posZ - player.lastTickPosZ;
        return player.boundingBox.offset(x, y, z);
    }
}
