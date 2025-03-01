package dev.faiths.hackerdetector.checks;

import dev.faiths.Faiths;
import dev.faiths.hackerdetector.data.PlayerDataSamples;
import dev.faiths.hackerdetector.utils.Vector3D;
import dev.faiths.hackerdetector.utils.ViolationLevelTracker;
import dev.faiths.module.world.ModuleHackerDetector;
import dev.faiths.utils.DebugUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Abstract class to hold util methods for the checks
 */
public abstract class Check implements ICheck {

    protected static final Minecraft mc = Minecraft.getMinecraft();
    private static final HashSet<String> flagMessages = new HashSet<>();

    @Override
    public final void checkViolationLevel(EntityPlayer player, boolean failedCheck, ViolationLevelTracker... trackers) {
        for (final ViolationLevelTracker tracker : trackers) {
            if (tracker.isFlagging(failedCheck)) {
                this.printFlagMessage(player);
            }
        }
    }

    private void printFlagMessage(EntityPlayer player) {
        final String cheatType = this.getCheatName() + (this.getFlagType().isEmpty() ? "" : " (" + this.getFlagType() + ")");
        final String msg = player.getName()
                + EnumChatFormatting.YELLOW + " flags "
                + EnumChatFormatting.RED + cheatType;
        if(Faiths.moduleManager.getModule(ModuleHackerDetector.class).getState()) {
            DebugUtil.log(msg);
        }
    }

    /**
     * Returns the base speed for a player sprinting without jumping.
     * Without speed, jumping while sprinting is ~29% faster than normal sprinting
     * That makes normal sprinting ~21% slower than sprint while jumping
     * Result is in meters per tick
     */
    protected static double getBaseSprintingSpeed(EntityPlayer player) {
        int speedAmplifier = 0;
        if (player.isPotionActive(Potion.moveSpeed)) {
            final PotionEffect activePotionEffect = player.getActivePotionEffect(Potion.moveSpeed);
            speedAmplifier = activePotionEffect.getAmplifier() + 1;
        }
        return getBaseSprintingSpeed(speedAmplifier);
    }

    protected static double getBaseSprintingSpeed(int speedAmplifier) {
        return 0.2806d * (1.0d + 0.2d * speedAmplifier);
    }

    /**
     * Returns the amount of ticks for a certain player to break a certain block
     * The enchantements of the tool must be readable from the client for this to be accurate
     * This accounts for the 5 ticks cooldown there is after breaking a bloc
     * Returns -1 if the block isn't harvestable
     */
    protected static int getTimeToHarvestBlock(float blockStrength) {
        if (blockStrength <= 0) {
            return -1;
        }
        int i = 1;
        if (blockStrength < 1.0F) {
            float breakProgression = 0F;
            while (breakProgression < 1.0f) {
                i++;
                breakProgression += blockStrength;
            }
        }
        return i == 1 ? i : i + 5;
    }

    protected static boolean isPlayerLookingAtBlock(EntityPlayer player, PlayerDataSamples data, BlockPos pos) {
        final Vector3D eyesToBlockCenter;
        if (player == mc.thePlayer) {
            eyesToBlockCenter = new Vector3D(
                    pos.getX() + 0.5D - player.posX,
                    pos.getY() + 0.5D - (player.posY + player.getEyeHeight()),
                    pos.getZ() + 0.5D - player.posZ);
        } else {
            eyesToBlockCenter = new Vector3D(
                    pos.getX() + 0.5D - data.serverPosXList.get(0),
                    pos.getY() + 0.5D - (data.serverPosYList.get(0) + player.getEyeHeight()),
                    pos.getZ() + 0.5D - data.serverPosZList.get(0));
        }
        /* (0.5*sqrt(3) + 4.5)^2 | Squared distance from center of the block to the corner + player's break reach */
        final double distSq = eyesToBlockCenter.normSquared();
        if (distSq > 28.79422863D) {
            return false;
            /* The player somehow has their head inside the block */
        } else if (distSq < 0.25) {
            return true;
        }
        /* Checks if the player's look vect is within a cone that starts from the player's eyes and
         * whose base is a circle centered on the block's center and with 0.5*sqrt(3) radius */
        final Vector3D lookVect;
        if (player == mc.thePlayer) {
            lookVect = Vector3D.getPlayersLookVec(player);
        } else {
            lookVect = Vector3D.getVectorFromRotation(data.serverPitchList.get(0), data.serverYawHeadList.get(0));
        }
        final double angleWithVector = lookVect.getAngleWithVector(eyesToBlockCenter);
        final double maxAngle = Math.toDegrees(Math.atan(0.5D * Math.sqrt(3D / distSq))) * 1.33D;
        return angleWithVector < maxAngle;
    }

    /**
     * Returns true if the coordinates provided (Vec3) are inside the hitbox of the player of position playerX, playerY, playerZ
     */
    protected static boolean isInsideHitbox(double playerX, double playerY, double playerZ, Vec3 vec) {
        return vec.xCoord > playerX - 0.4 && vec.xCoord < playerX + 0.4
                && vec.yCoord > playerY - 0.1 && vec.yCoord < playerY + 1.9
                && vec.zCoord > playerZ - 0.4 && vec.zCoord < playerZ + 0.4;
    }

    /**
     * Returns true if the coordinates provided are inside the hitbox of the player
     */
    protected static boolean isInsideHitbox(EntityPlayer player, Vec3 vec) {
        return vec.xCoord > player.posX - 0.4 && vec.xCoord < player.posX + 0.4
                && vec.yCoord > player.posY - 0.1 && vec.yCoord < player.posY + 1.9
                && vec.zCoord > player.posZ - 0.4 && vec.zCoord < player.posZ + 0.4;
    }

    /**
     * Returns true if the coordinates provided are inside the hitbox of the player
     */
    protected static boolean isInsideHitbox(EntityPlayer player, double x, double y, double z) {
        return x > player.posX - 0.4 && x < player.posX + 0.4 && y > player.posY - 0.1 && y < player.posY + 1.9 && z > player.posZ - 0.4 && z < player.posZ + 0.4;
    }

    /**
     * Returns true if the coordinates (x,y,z) provided are inside the hitbox of the player of coordinates (playerX,playerY,playerZ)
     */
    protected static boolean isInsideHitbox(double playerX, double playerY, double playerZ, double x, double y, double z) {
        return x > playerX - 0.4 && x < playerX + 0.4 && y > playerY - 0.1 && y < playerY + 1.9 && z > playerZ - 0.4 && z < playerZ + 0.4;
    }

    /**
     * Given a player's position, two points in space, A and B, this method returns the coordinates
     * of the point where the ray going from A to B hits the player's hitbox. Returns null if it doesn't hit the box.
     */
    protected static Vec3 getHitVectOnPlayer(EntityPlayer player, Vec3 vecA, Vec3 vecB) {
        return getHitVectOnPlayer(player.posX, player.posY, player.posZ, vecA, vecB);
    }

    /**
     * Given a player's coordinates X Y Z, two points in space, A and B, this method returns the coordinates
     * of the point where the ray going from A to B hits the player's hitbox. Returns null if it doesn't hit the box.
     */
    protected static Vec3 getHitVectOnPlayer(double playerX, double playerY, double playerZ, Vec3 vecA, Vec3 vecB) {

        final double boxMinX = playerX - 0.4;
        final double boxMaxX = playerX + 0.4;
        final double boxMinY = playerY - 0.1;
        final double boxMaxY = playerY + 1.9;
        final double boxMinZ = playerZ - 0.4;
        final double boxMaxZ = playerZ + 0.4;

        Vec3 interMinX = vecA.getIntermediateWithXValue(vecB, boxMinX);
        Vec3 interMaxX = vecA.getIntermediateWithXValue(vecB, boxMaxX);
        Vec3 interMinY = vecA.getIntermediateWithYValue(vecB, boxMinY);
        Vec3 interMaxY = vecA.getIntermediateWithYValue(vecB, boxMaxY);
        Vec3 interMinZ = vecA.getIntermediateWithZValue(vecB, boxMinZ);
        Vec3 interMaxZ = vecA.getIntermediateWithZValue(vecB, boxMaxZ);

        if (interMinX == null || interMinX.yCoord < boxMinY || interMinX.yCoord > boxMaxY || interMinX.zCoord < boxMinZ || interMinX.zCoord > boxMaxZ) {
            interMinX = null;
        }

        if (interMaxX == null || interMaxX.yCoord < boxMinY || interMaxX.yCoord > boxMaxY || interMaxX.zCoord < boxMinZ || interMaxX.zCoord > boxMaxZ) {
            interMaxX = null;
        }

        if (interMinY == null || interMinY.xCoord < boxMinX || interMinY.xCoord > boxMaxX || interMinY.zCoord < boxMinZ || interMinY.zCoord > boxMaxZ) {
            interMinY = null;
        }

        if (interMaxY == null || interMaxY.xCoord < boxMinX || interMaxY.xCoord > boxMaxX || interMaxY.zCoord < boxMinZ || interMaxY.zCoord > boxMaxZ) {
            interMaxY = null;
        }

        if (interMinZ == null || interMinZ.xCoord < boxMinX || interMinZ.xCoord > boxMaxX || interMinZ.yCoord < boxMinY || interMinZ.yCoord > boxMaxY) {
            interMinZ = null;
        }

        if (interMaxZ == null || interMaxZ.xCoord < boxMinX || interMaxZ.xCoord > boxMaxX || interMaxZ.yCoord < boxMinY || interMaxZ.yCoord > boxMaxY) {
            interMaxZ = null;
        }

        Vec3 closestHitVect = null;

        if (interMinX != null) {
            closestHitVect = interMinX;
        }

        if (interMaxX != null && (closestHitVect == null || vecA.squareDistanceTo(interMaxX) < vecA.squareDistanceTo(closestHitVect))) {
            closestHitVect = interMaxX;
        }

        if (interMinY != null && (closestHitVect == null || vecA.squareDistanceTo(interMinY) < vecA.squareDistanceTo(closestHitVect))) {
            closestHitVect = interMinY;
        }

        if (interMaxY != null && (closestHitVect == null || vecA.squareDistanceTo(interMaxY) < vecA.squareDistanceTo(closestHitVect))) {
            closestHitVect = interMaxY;
        }

        if (interMinZ != null && (closestHitVect == null || vecA.squareDistanceTo(interMinZ) < vecA.squareDistanceTo(closestHitVect))) {
            closestHitVect = interMinZ;
        }

        if (interMaxZ != null && (closestHitVect == null || vecA.squareDistanceTo(interMaxZ) < vecA.squareDistanceTo(closestHitVect))) {
            closestHitVect = interMaxZ;
        }

        return closestHitVect;

    }

    /**
     * Given a player's coordinates X Y Z, two points in space, A and B, this method returns the distance
     * that the ray between A and B spent inside the players hitbox. Returns 0 if it doesn't hit the box.
     */
    protected static double getDistanceInHitbox(EntityPlayer player, Vec3 vecA, Vec3 vecB) {
        return getDistanceInHitbox(player.posX, player.posY, player.posZ, vecA, vecB);
    }

    /**
     * Given a player's coordinates X Y Z, two points in space, A and B, this method returns the distance
     * that the ray between A and B spent inside the players hitbox. Returns 0 if it doesn't hit the box.
     */
    protected static double getDistanceInHitbox(double playerX, double playerY, double playerZ, Vec3 vecA, Vec3 vecB) {

        final double boxMinX = playerX - 0.4;
        final double boxMaxX = playerX + 0.4;
        final double boxMinY = playerY - 0.1;
        final double boxMaxY = playerY + 1.9;
        final double boxMinZ = playerZ - 0.4;
        final double boxMaxZ = playerZ + 0.4;

        Vec3 interMinX = vecA.getIntermediateWithXValue(vecB, boxMinX);
        Vec3 interMaxX = vecA.getIntermediateWithXValue(vecB, boxMaxX);
        Vec3 interMinY = vecA.getIntermediateWithYValue(vecB, boxMinY);
        Vec3 interMaxY = vecA.getIntermediateWithYValue(vecB, boxMaxY);
        Vec3 interMinZ = vecA.getIntermediateWithZValue(vecB, boxMinZ);
        Vec3 interMaxZ = vecA.getIntermediateWithZValue(vecB, boxMaxZ);

        if (interMinX == null || interMinX.yCoord < boxMinY || interMinX.yCoord > boxMaxY || interMinX.zCoord < boxMinZ || interMinX.zCoord > boxMaxZ) {
            interMinX = null;
        }

        if (interMaxX == null || interMaxX.yCoord < boxMinY || interMaxX.yCoord > boxMaxY || interMaxX.zCoord < boxMinZ || interMaxX.zCoord > boxMaxZ) {
            interMaxX = null;
        }

        if (interMinY == null || interMinY.xCoord < boxMinX || interMinY.xCoord > boxMaxX || interMinY.zCoord < boxMinZ || interMinY.zCoord > boxMaxZ) {
            interMinY = null;
        }

        if (interMaxY == null || interMaxY.xCoord < boxMinX || interMaxY.xCoord > boxMaxX || interMaxY.zCoord < boxMinZ || interMaxY.zCoord > boxMaxZ) {
            interMaxY = null;
        }

        if (interMinZ == null || interMinZ.xCoord < boxMinX || interMinZ.xCoord > boxMaxX || interMinZ.yCoord < boxMinY || interMinZ.yCoord > boxMaxY) {
            interMinZ = null;
        }

        if (interMaxZ == null || interMaxZ.xCoord < boxMinX || interMaxZ.xCoord > boxMaxX || interMaxZ.yCoord < boxMinY || interMaxZ.yCoord > boxMaxY) {
            interMaxZ = null;
        }

        Vec3 closestHitVect = null;
        Vec3 furthestHitVect = null;

        if (interMinX != null) {
            closestHitVect = interMinX;
            furthestHitVect = interMinX;
        }

        if (interMaxX != null) {
            if (closestHitVect == null) {
                closestHitVect = interMaxX;
                furthestHitVect = interMaxX;
            } else if (vecA.squareDistanceTo(interMaxX) < vecA.squareDistanceTo(closestHitVect)) {
                closestHitVect = interMaxX;
            } else {
                furthestHitVect = interMaxX;
            }
        }

        if (interMinY != null) {
            if (closestHitVect == null) {
                closestHitVect = interMinY;
                furthestHitVect = interMinY;
            } else if (vecA.squareDistanceTo(interMinY) < vecA.squareDistanceTo(closestHitVect)) {
                closestHitVect = interMinY;
            } else {
                furthestHitVect = interMinY;
            }
        }

        if (interMaxY != null) {
            if (closestHitVect == null) {
                closestHitVect = interMaxY;
                furthestHitVect = interMaxY;
            } else if (vecA.squareDistanceTo(interMaxY) < vecA.squareDistanceTo(closestHitVect)) {
                closestHitVect = interMaxY;
            } else {
                furthestHitVect = interMaxY;
            }
        }

        if (interMinZ != null) {
            if (closestHitVect == null) {
                closestHitVect = interMinZ;
                furthestHitVect = interMinZ;
            } else if (vecA.squareDistanceTo(interMinZ) < vecA.squareDistanceTo(closestHitVect)) {
                closestHitVect = interMinZ;
            } else {
                furthestHitVect = interMinZ;
            }
        }

        if (interMaxZ != null) {
            if (closestHitVect == null) {
                closestHitVect = interMaxZ;
                furthestHitVect = interMaxZ;
            } else if (vecA.squareDistanceTo(interMaxZ) < vecA.squareDistanceTo(closestHitVect)) {
                closestHitVect = interMaxZ;
            } else {
                furthestHitVect = interMaxZ;
            }
        }

        if (closestHitVect == null) {
            return 0D;
        }

        return closestHitVect.distanceTo(furthestHitVect);

    }

    protected static List<EntityPlayer> getPlayersInAABBexcluding(Entity entity, AxisAlignedBB aabb, Predicate<? super EntityPlayer> predicate) {
        final List<EntityPlayer> list = new ArrayList<>();
        for (final EntityPlayer player : mc.theWorld.playerEntities) {
            if (player != entity && player.getEntityBoundingBox().intersectsWith(aabb) && predicate.test(player)) {
                list.add(player);
            }
        }
        return list;
    }

    public static void clearFlagMessages() {
        flagMessages.clear();
    }

}
