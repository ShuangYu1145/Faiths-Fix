package dev.faiths.module.movement;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.event.impl.MoveInputEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.combat.ModuleKillAura;
import dev.faiths.module.world.ModuleBedBreaker;
import dev.faiths.module.world.ModuleScaffold;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.utils.player.RotationUtil;
import dev.faiths.utils.player.RotationUtils;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueMode;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import static dev.faiths.utils.IMinecraft.mc;

public class ModuleBHop extends CheatModule {
    public ModuleBHop() {
        super("Bhop",Category.MOVEMENT,"");
    }

    private ValueMode mode = new ValueMode("Mode", new String[]{"Strafe", "Ground", "8 tick", "7 tick"}, "Ground");
    ValueFloat speedSetting = new ValueFloat("Speed", 0.9F,0.5F, 8.0F);
    ValueBoolean liquidDisable = new ValueBoolean("Disable in liquid", true);
    ValueBoolean sneakDisable = new ValueBoolean("Disable while sneaking", true);
    ValueBoolean rotateYaw = new ValueBoolean("Rotate yaw", false);
    ValueBoolean airStrafe = new ValueBoolean("4 Tick AirStrafe", false);

    public boolean hopping, lowhop, didMove, collided, setRotation;


    private int Strafies;

    private final Handler<MoveInputEvent> moveInputEventHandler = event -> {
        if (!mc.thePlayer.onGround || mc.thePlayer.capabilities.isFlying) {
            return;
        }
        if (hopping) {
            mc.thePlayer.movementInput.jump = false;
        }
    };

    private final Handler<MotionEvent> motionEventHandler = e -> {
        if (e.isPre()) {
            if (((mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) && liquidDisable.getValue()) || (mc.thePlayer.isSneaking() && sneakDisable.getValue())) {
                return;
            }
            if (Faiths.moduleManager.getModule(ModuleBedBreaker.class).getState() && Faiths.moduleManager.getModule(ModuleBedBreaker.class).disableBHop.getValue() && Faiths.moduleManager.getModule(ModuleBedBreaker.class).currentPos != null && RotationUtil.inRange(Faiths.moduleManager.getModule(ModuleBedBreaker.class).currentPos, Faiths.moduleManager.getModule(ModuleBedBreaker.class).radius.getValue())) {
                return;
            }
//            if (ModuleManager.scaffold.moduleEnabled && (ModuleManager.tower.canTower() || ModuleManager.scaffold.fastScaffoldKeepY)) {
//                return;
//            }
            if (!PlayerUtils.isMoving()) {
                return;
            }
            if (!mode.is("Strafe")) {
                if (mc.thePlayer.isCollidedHorizontally) {
                    collided = true;
                } else if (mc.thePlayer.onGround) {
                    collided = false;
                }
                if (mc.thePlayer.onGround) {
                    if (mc.thePlayer.moveForward <= -0.5 && mc.thePlayer.moveStrafing == 0 && ModuleKillAura.target == null && !PlayerUtils.noSlowingBackWithBow() && !Faiths.moduleManager.getModule(ModuleScaffold.class).getState() && !mc.thePlayer.isCollidedHorizontally) {
                        setRotation = true;
                    }
                    mc.thePlayer.jump();
                    double horizontalSpeed = PlayerUtils.getHorizontalSpeed();
                    double speedModifier = 0.48;
                    final int speedAmplifier = PlayerUtils.getSpeedAmplifier();
                    switch (speedAmplifier) {
                        case 1:
                            speedModifier = 0.5;
                            break;
                        case 2:
                            speedModifier = 0.52;
                            break;
                        case 3:
                            speedModifier = 0.58;
                            break;
                    }
                    double additionalSpeed = speedModifier * ((speedSetting.getValue() - 1.0) / 3.0 + 1.0);
                    if (horizontalSpeed < additionalSpeed) {
                        horizontalSpeed = additionalSpeed;
                    }
                    if (PlayerUtils.isMoving() && !PlayerUtils.noSlowingBackWithBow() && !Faiths.moduleManager.getModule(ModuleSprint.class).disableBackwards()) {
                        PlayerUtils.setSpeed(horizontalSpeed);
                        didMove = true;
                    }
                    hopping = true;
                }
            }
            if (mode.is("Strafe")) {
                if (PlayerUtils.isMoving()) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump();
                    }
                    mc.thePlayer.setSprinting(true);
                    PlayerUtils.setSpeed(PlayerUtils.getHorizontalSpeed() + 0.005 * speedSetting.getValue());
                    hopping = true;
                }
            }
            if (mode.is("7 tick")) {
                if (didMove) {
                    int simpleY = (int) Math.round((e.getY() % 1) * 10000);

                    if (mc.thePlayer.hurtTime == 0 && !collided) {
                        switch (simpleY) {
                            case 4200:
                                mc.thePlayer.motionY = 0.39;
                                lowhop = true;
                                break;
                            case 1138:
                                mc.thePlayer.motionY = mc.thePlayer.motionY - 0.13;
                                lowhop = false;
                                break;
                            case 2031:
                                mc.thePlayer.motionY = mc.thePlayer.motionY - 0.2;
                                didMove = false;
                                break;
                        }
                    }
                }
            }
            if (mode.is("8 tick")) {
                if (didMove) {
                    int simpleY = (int) Math.round((e.getY() % 1) * 10000);

                    if (mc.thePlayer.hurtTime == 0 && !collided) {
                        switch (simpleY) {
                            case 13:
                                mc.thePlayer.motionY = mc.thePlayer.motionY - 0.02483;
                                break;
                            case 2000:
                                mc.thePlayer.motionY = mc.thePlayer.motionY - 0.1913;
                                didMove = false;
                                break;
                        }
                    }
                }
            }
            if (mode.is("9 tick")) {
                    if (didMove) {
                        int simpleY = (int) Math.round((e.getY() % 1) * 10000);

                        if (mc.thePlayer.hurtTime == 0 && !collided) {
                            switch (simpleY) {
                                case 13 :
                                    mc.thePlayer.motionY = mc.thePlayer.motionY - 0.02483;
                                    break;
                                case 2000:
                                    mc.thePlayer.motionY = mc.thePlayer.motionY - 0.16874;
                            }
                        }
                    }
            }
        }
    };


    private final Handler<UpdateEvent> updateEventHandler = e -> {

            if (canstrafe()) {
                Strafies = mc.thePlayer.onGround ? 0 : Strafies + 1;

                if (mc.thePlayer.fallDistance > 1 || mc.thePlayer.onGround) {
                    Strafies = 0;
                    return;
                }

                if (Strafies == 1) {
                    strafe();
                }

                if (!blockRelativeToPlayer(0, mc.thePlayer.motionY, 0).getUnlocalizedName().contains("air") && Strafies > 2) {
                    strafe();
                }

                if (airStrafe.getValue() && Strafies >= 2 && (!blockRelativeToPlayer(0, mc.thePlayer.motionY * 3, 0).getUnlocalizedName().contains("air") || Strafies == 9) && !Faiths.moduleManager.getModule(ModuleScaffold.class).getState()) {
                    mc.thePlayer.motionY += 0.0754;
                    strafe();
                }
            }
        };

    private boolean canstrafe() {
        return mc.thePlayer.hurtTime == 0
                && !mc.thePlayer.isUsingItem()
                && mc.gameSettings.keyBindForward.isKeyDown();
    }

    private void strafe() {
        PlayerUtils.setSpeed(PlayerUtils.getHorizontalSpeed());
    }

    private Block blockRelativeToPlayer(double offsetX, double offsetY, double offsetZ) {
        Vec3 pos = mc.thePlayer.getPositionVector();
        double x = pos.xCoord + offsetX;
        double y = pos.yCoord + offsetY;
        double z = pos.zCoord + offsetZ;

        BlockPos blockPos = new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));
        return mc.theWorld.getBlockState(blockPos).getBlock();
    }

    @Override
    public void onDisable() {
        hopping = false;
        Strafies = 0;
    }
}
