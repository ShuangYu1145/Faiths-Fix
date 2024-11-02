package dev.faiths.hackerdetector.checks;

import dev.faiths.hackerdetector.data.PlayerDataSamples;
import dev.faiths.hackerdetector.utils.ViolationLevelTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;

public class KeepSprintACheck extends Check {

    @Override
    public String getCheatName() {
        return "KeepSprint";
    }

    @Override
    public String getCheatDescription() {
        return "The player's sprint doesn't turn off when using items (blocking sword, eating, drinking, using bow...)";
    }

    //@Override
    //public String getFlagType() {
    //    return "A";
    //}

    @Override
    public boolean canSendReport() {
        return true;
    }

    @Override
    public void performCheck(EntityPlayer player, PlayerDataSamples data) {
        super.checkViolationLevel(player, this.check(player, data), data.keepsprintAVL);
    }

    @Override
    public boolean check(EntityPlayer player, PlayerDataSamples data) {
        // If the player is moving slower than the base running speed, we consider it is keepsprint
        if (data.isNotMovingXZ() || player.isRiding()) return false;
        if (data.useItemTime > 5) {
            final boolean invalidSprint;
            if (data.usedItemIsConsumable) {
                if (data.useItemTime > 32) return false;
                if (data.sprintTime > 32) {
                    invalidSprint = true;
                } else {
                    if (data.sprintTime == data.useItemTime && data.useItemTime < 12) return false;
                    if (Math.abs(data.getMoveLookAngleDiff()) > 135d) return false; // rubber band
                    invalidSprint = data.sprintTime > data.useItemTime + 3 || data.lastEatTime > 32 && data.sprintTime > 5;
                }
            } else {
                if (player.getHeldItem().getItem() instanceof ItemSword) return false;
                invalidSprint = data.sprintTime > 5;
            }
            if (invalidSprint && data.getSpeedXZSq() < 6.25D) {
                data.keepsprintAVL.add(2);
                return true;
            } else if (data.sprintTime == 0) {
                data.keepsprintAVL.substract(3);
            }
        }
        return false;
    }

    public static ViolationLevelTracker newVL() {
        return new ViolationLevelTracker(5);
    }

}
