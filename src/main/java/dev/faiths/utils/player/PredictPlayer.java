package dev.faiths.utils.player;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.Arrays;
import java.util.List;

import static dev.faiths.utils.IMinecraft.mc;

public class PredictPlayer {

    private double x;
    private double y;
    private double z;
    private double motionX;
    private double motionY;
    private double motionZ;
    private float yaw;
    private float strafe;
    private float forward;

    public PredictPlayer() {
        this(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ, mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward);
    }

    public PredictPlayer(EntityPlayerSP player, boolean predict) {
        this(predict ? player.posX + player.motionX : player.posX,
                predict ? player.posY + player.motionY : player.posY,
                predict ? player.posZ + player.motionZ : player.posZ,
                player.motionX, player.motionY, player.motionZ, player.rotationYaw, player.moveStrafing, player.moveForward);
    }

    public PredictPlayer(double x, double y, double z, double motionX, double motionY, double motionZ, float yaw, float strafe, float forward) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.yaw = yaw;
        this.strafe = strafe;
        this.forward = forward;
    }

    private void calculateForTick() {
        strafe *= 0.98f;
        forward *= 0.98f;

        float v = strafe * strafe + forward * forward;
        if (v >= 0.0001f) {
            v = mc.thePlayer.jumpMovementFactor / Math.max((float) Math.sqrt(v), 1.0f);

            strafe *= v;
            forward *= v;

            double f1 = Math.sin(Math.toRadians(yaw));
            double f2 = Math.cos(Math.toRadians(yaw));

            motionX += (strafe * f2 - forward * f1);
            motionZ += (forward * f2 + strafe * f1);
        }

        motionY -= 0.08;
        motionX *= 0.91;
        motionY *= 0.9800000190734863;
        motionY *= 0.91;
        motionZ *= 0.91;

        x += motionX;
        y += motionY;
        z += motionZ;
    }

    public CollisionResult findCollision(int ticks) {
        for (int i = 0; i < ticks; i++) {
            Vec3 start = new Vec3(x, y, z);
            calculateForTick();
            Vec3 end = new Vec3(x, y, z);

            for (Vec3 offset : offsets) {
                BlockPos pos = rayTrace(start.addVector(offset.xCoord, offset.yCoord, offset.zCoord), end);
                if (pos != null) {
                    return new CollisionResult(pos, i);
                }
            }
        }
        return null;
    }

    private BlockPos rayTrace(Vec3 start, Vec3 end) {
        MovingObjectPosition result = mc.theWorld.rayTraceBlocks(start, end, true);
        if (result == null || result.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || result.sideHit != EnumFacing.UP) {
            return null;
        }
        return result.getBlockPos();
    }

    private final List<Vec3> offsets = Arrays.asList(
            new Vec3(0.0, 0.0, 0.0),
            new Vec3(0.3, 0.0, 0.3),
            new Vec3(-0.3, 0.0, 0.3),
            new Vec3(0.3, 0.0, -0.3),
            new Vec3(-0.3, 0.0, -0.3),
            new Vec3(0.3, 0.0, 0.15),
            new Vec3(-0.3, 0.0, 0.15),
            new Vec3(0.15, 0.0, 0.3),
            new Vec3(0.15, 0.0, -0.3)
    );

    public static class CollisionResult {
        public final BlockPos pos;
        public final int tick;

        public CollisionResult(BlockPos pos, int tick) {
            this.pos = pos;
            this.tick = tick;
        }
    }
}