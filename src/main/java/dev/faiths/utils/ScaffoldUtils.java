package dev.faiths.utils;

import dev.faiths.Faiths;
import dev.faiths.utils.math.MathUtils;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.utils.player.Rotation;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import org.lwjgl.util.vector.Vector2f;

import static dev.faiths.utils.IMinecraft.mc;
import static dev.faiths.utils.player.RotationUtils.getVectorForRotation;

public class ScaffoldUtils {

    private static int flag = 1;

    public static class PlaceRotation {
        private final PlaceInfo placeInfo;
        private final Rotation rotation;

        public PlaceRotation(final PlaceInfo position, final Rotation facing) {
            this.placeInfo = position;
            this.rotation = facing;
        }

        public PlaceInfo getPlaceInfo() {
            return placeInfo;
        }

        public Rotation getRotation() {
            return rotation;
        }
    }
    public static class PlaceInfo {
        private final BlockPos blockPos;
        private final EnumFacing enumFacing;
        private final Vec3 hitVec;

        public PlaceInfo(final BlockPos position, final EnumFacing facing, final Vec3 hitVec) {
            this.blockPos = position;
            this.enumFacing = facing;
            this.hitVec = hitVec;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }

        public EnumFacing getEnumFacing() {
            return enumFacing;
        }

        public Vec3 getHitVec() {
            return hitVec;
        }
    }

    public static int getBlockSlot() {
        for (int i = 0; i < 9; i++) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && itemStack.stackSize > 2) {
                final ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                if (isBlockValid(itemBlock.getBlock())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static int getBlockCount() {
        if (mc.thePlayer == null) return 0;
        int count = 0;
        for (int i = 0; i < 9; i++) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && itemStack.stackSize > 0) {
                final ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
                if (isBlockValid(itemBlock.getBlock())) {
                    count += itemStack.stackSize;
                }
            }
        }
        return count;
    }

    private static boolean isBlockValid(final Block block) {
        return (block.isFullBlock() || block == Blocks.glass) &&
                block != Blocks.sand &&
                block != Blocks.gravel &&
                block != Blocks.dispenser &&
                block != Blocks.command_block &&
                block != Blocks.noteblock &&
                block != Blocks.furnace &&
                block != Blocks.crafting_table &&
                block != Blocks.tnt &&
                block != Blocks.dropper &&
                block != Blocks.beacon;
    }

    /**
     * Face block
     *
     * @param blockPos target block
     */
    public static float[] faceBlock(final BlockPos blockPos) {
        if (blockPos == null)
            return null;

        Rotation vecRotation = null;

        final Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        final Vec3 predictEyesPos = eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);

        for(double xSearch = 0.1D; xSearch < 0.9D; xSearch += 0.1D) {
            for(double ySearch = 0.1D; ySearch < 0.9D; ySearch += 0.1D) {
                for (double zSearch = 0.1D; zSearch < 0.9D; zSearch += 0.1D) {
                    final Vec3 posVec = new Vec3(blockPos).addVector(xSearch, ySearch, zSearch);
                    final double dist = eyesPos.distanceTo(posVec);

                    final double diffX = posVec.xCoord - eyesPos.xCoord;
                    final double diffY = posVec.yCoord - eyesPos.yCoord;
                    final double diffZ = posVec.zCoord - eyesPos.zCoord;

                    final double diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);

                    final Rotation rotation = new Rotation(
                            MathHelper.wrapAngleTo180_float((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F),
                            MathHelper.wrapAngleTo180_float((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)))
                    );

                    final Vec3 rotationVector = getVectorForRotation(rotation);
                    final Vec3 vector = eyesPos.addVector(rotationVector.xCoord * dist, rotationVector.yCoord * dist,
                            rotationVector.zCoord * dist);
                    final Vec3 predictVector = predictEyesPos
                            .addVector(rotationVector.xCoord * dist, rotationVector.yCoord * dist, rotationVector.zCoord * dist);
                    final MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false,
                            false, true);
                    final MovingObjectPosition predictObj = mc.theWorld.rayTraceBlocks(predictEyesPos, predictVector, false,
                            false, true);

                    if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.getBlockPos().equals(blockPos) &&
                            predictObj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && predictObj.getBlockPos().equals(blockPos)) {
                        final Rotation currentVec = rotation;

                        if (vecRotation == null || Faiths.INSTANCE.getRotationManager().getRotationDifference(vecRotation, new Vector2f(PlayerUtils.getMoveYaw(mc.thePlayer.rotationYaw) - 180f, Faiths.INSTANCE.getRotationManager().lastServerRotation.y)) > Faiths.INSTANCE.getRotationManager().getRotationDifference(currentVec, new Vector2f(PlayerUtils.getMoveYaw(mc.thePlayer.rotationYaw) - 180f, Faiths.INSTANCE.getRotationManager().lastServerRotation.y))) {
                            vecRotation = currentVec;
                        }
                    }
                }
            }
        }

        final Rotation rotation = new Rotation(PlayerUtils.getMoveYaw(mc.thePlayer.rotationYaw) - 180f, MathUtils.getRandomInRange(79.5f, 83.5f));
        final Vec3 rotationVector = getVectorForRotation(rotation);
        final Vec3 vector = eyesPos.addVector(rotationVector.xCoord * 100.0, rotationVector.yCoord * 100.0,
                rotationVector.zCoord * 100.0);
        final Vec3 predictVector = predictEyesPos
                .addVector(rotationVector.xCoord * 100.0, rotationVector.yCoord * 100.0, rotationVector.zCoord * 100.0);
        final MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false,
                false, true);
        final MovingObjectPosition predictObj = mc.theWorld.rayTraceBlocks(predictEyesPos, predictVector, false,
                false, true);

        if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.getBlockPos().equals(blockPos) &&
                predictObj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && predictObj.getBlockPos().equals(blockPos)) {
            final Rotation currentVec = rotation;

            if (vecRotation == null || Faiths.INSTANCE.getRotationManager().getRotationDifference(vecRotation, new Vector2f(PlayerUtils.getMoveYaw(mc.thePlayer.rotationYaw) - 180f, Faiths.INSTANCE.getRotationManager().lastServerRotation.y)) > Faiths.INSTANCE.getRotationManager().getRotationDifference(currentVec, new Vector2f(PlayerUtils.getMoveYaw(mc.thePlayer.rotationYaw) - 180f, Faiths.INSTANCE.getRotationManager().lastServerRotation.y))) {
                vecRotation = currentVec;
            }
        }

        return new float[]{vecRotation.getYaw(), vecRotation.getPitch()};
    }

    /**
     * Face block
     *
     * @param blockPos target block
     * @param facing target facing
     */
    public static Rotation faceBlock(final BlockPos blockPos, final EnumFacing facing) {
        if (blockPos == null)
            return null;

        Rotation vecRotation = null;
        Vector2f preferRot;
        preferRot = (flag == 1) ? new Vector2f(mc.thePlayer.rotationYaw - 160f, 90f) : new Vector2f(mc.thePlayer.rotationYaw - 200f, -90f);
        if (flag == 1) {
            flag = 0;
        } else {
            flag = 1;
        }

        for(double xSearch = 0.1D; xSearch < 0.9D; xSearch += 0.1D) {
            for(double ySearch = 0.1D; ySearch < 0.9D; ySearch += 0.1D) {
                for (double zSearch = 0.1D; zSearch < 0.9D; zSearch += 0.1D) {
                    final Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
                    final Vec3 posVec = new Vec3(blockPos).addVector(xSearch, ySearch, zSearch);
                    final double dist = eyesPos.distanceTo(posVec);

                    final double diffX = posVec.xCoord - eyesPos.xCoord;
                    final double diffY = posVec.yCoord - eyesPos.yCoord;
                    final double diffZ = posVec.zCoord - eyesPos.zCoord;

                    final double diffXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);

                    final Rotation rotation = new Rotation(
                            MathHelper.wrapAngleTo180_float((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F),
                            MathHelper.wrapAngleTo180_float((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)))
                    );

                    final Vec3 rotationVector = getVectorForRotation(rotation);
                    final Vec3 vector = eyesPos.addVector(rotationVector.xCoord * dist, rotationVector.yCoord * dist,
                            rotationVector.zCoord * dist);
                    final MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(eyesPos, vector, false,
                            false, true);

                    if (obj.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && obj.getBlockPos().equals(blockPos) && obj.sideHit == facing) {
                        final Rotation currentVec = rotation;

                        if (vecRotation == null || Faiths.INSTANCE.getRotationManager().getRotationDifference(currentVec, preferRot) < Faiths.INSTANCE.getRotationManager().getRotationDifference(vecRotation, preferRot))
                            vecRotation = currentVec;
                    }
                }
            }
        }

        return vecRotation;
    }
}
