package dev.faiths.module.movement;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.MoveEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.combat.ModuleKillAura;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import net.minecraft.util.AxisAlignedBB;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleTargetStrafe extends CheatModule {

    private final ValueBoolean thirdPersonViewValue = new ValueBoolean("ThirdPersonView", false);
    private final ValueBoolean render = new ValueBoolean("Render", false);
    private final ValueFloat radiusValue = new ValueFloat("Radius", 0.1F, 0.5F, 5.0F);
    private double direction = -1.0;

    public void setSpeed(MoveEvent moveEvent, double moveSpeed, float yaw, double pseudoStrafe, double pseudoForward) {
        double forward = pseudoForward;
        double strafe = pseudoStrafe;

        if (forward == 0.0 && strafe == 0.0) {
            moveEvent.setX(0.0);
            moveEvent.setZ(0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (forward > 0.0) ? -45 : 45;
                } else if (strafe < 0.0) {
                    yaw += (forward > 0.0) ? 45 : -45;
                }
                strafe = 0.0;
                forward = (forward > 0.0) ? 1.0 : -1.0;
            }

            double cos = Math.cos(Math.toRadians(yaw + 90.0));
            double sin = Math.sin(Math.toRadians(yaw + 90.0));
            double mx = -Math.sin(Math.toRadians(yaw));
            double mz = Math.cos(Math.toRadians(yaw));

            moveEvent.setX(forward * moveSpeed * cos + strafe * moveSpeed * sin);
            moveEvent.setZ(forward * moveSpeed * sin - strafe * moveSpeed * cos);
        }
    }

    private final Handler<MoveEvent> moveHandler = event -> {
        if (mc.thePlayer.isCollidedHorizontally || checkVoid()) direction = -direction;
        if (mc.gameSettings.keyBindLeft.isKeyDown()) direction = 1.0;
        if (mc.gameSettings.keyBindRight.isKeyDown()) direction = -1.0;
        if (!isVoid(0, 0) && canStrafe()) {
//            final boolean jump = mc.thePlayer.movementInput.jump;
//            if (jump) mc.thePlayer.setSprinting(false);
            setSpeed(
                event,
                Math.sqrt(Math.pow(event.getX(), 2.0) + Math.pow(event.getZ(), 2.0)) * (!mc.thePlayer.onGround ? 0.6 : 0.8),
                    mc.thePlayer.movementYaw,
                direction,
                mc.thePlayer.getDistanceToEntity(ModuleKillAura.target) <= radiusValue.getValue() ? 0.0 : 1.0
            );
        }
        if (!thirdPersonViewValue.getValue()) return;
        mc.gameSettings.thirdPersonView = canStrafe() ? 1 : 0;
    };

    public ModuleTargetStrafe() {
        super("TargetStrafe", Category.MOVEMENT);
    }

    private boolean canStrafe() {
        return Faiths.moduleManager.getModule(ModuleKillAura.class).getState() && ModuleKillAura.target != null && !mc.thePlayer.isSneaking();
    }

    private boolean checkVoid() {
        for (int x = -1; x <= -1; x++) {
            for (int z = -1; z <= -1; z++) {
                if (isVoid(x, z)) return true;
            }
        }
        return false;
    }

    private boolean isVoid(int xPos, int zPos) {
        if (mc.thePlayer.posY < 0.0) {
            return true;
        }
        int off = 0;
        while (off < mc.thePlayer.posY) {
            AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(xPos, -off, zPos);
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                off += 2;
                continue;
            }
            return false;
        }
        return true;
    }
}
