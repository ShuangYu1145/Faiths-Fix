package dev.faiths.module.movement;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.player.ModuleBlink;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueMode;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleSpeed extends CheatModule {
    public ModuleSpeed() {
        super("Speed",Category.MOVEMENT);
    }

    private ValueMode mode = new ValueMode("Mode", new String[]{"Watchdog", "EntityBoost"}, "Watchdog");
    ValueBoolean faststop = new ValueBoolean("FastStop",true).visible(() -> mode.is("Watchdog"));
    ValueBoolean strafe = new ValueBoolean("GlideStrafe",true).visible(() -> mode.is("Watchdog"));
    ValueBoolean lowhop = new ValueBoolean("Lowhop",true).visible(() -> mode.is("Watchdog"));
    ValueBoolean strafe2 = new ValueBoolean("Strafe",true).visible(() -> mode.is("Watchdog") && lowhop.getValue());
    ValueFloat speed = new ValueFloat("Speed", 0.08f, 0f, 0.1f);
    ValueBoolean follow = new ValueBoolean("FollowTargetOnSpace",true).visible(() -> mode.is("EntityBoost"));
    ValueBoolean mcount = new ValueBoolean("MultiCount",true).visible(() -> mode.is("EntityBoost"));
    ValueBoolean antivoid = new ValueBoolean("AntiVoid",true).visible(() -> mode.is("EntityBoost"));
    private int offGroundticks;
    @Override
    public String getSuffix() {
        return mode.getValue();
    }

    private final Handler<UpdateEvent> updateEventHandler = event -> {
        if (mc.thePlayer.onGround) {
            offGroundticks = 0;
        } else {
            offGroundticks++;
        }
        if (mode.is("watchdog")) {
            mc.thePlayer.setSprinting(true);
            if (mc.thePlayer.motionY < 0.1 && mc.thePlayer.motionY > 0.01) {
                mc.thePlayer.motionX *= 1.005;
                mc.thePlayer.motionZ *= 1.005;
            }

            if (mc.thePlayer.motionY < 0.005 && mc.thePlayer.motionY > 0) {
                mc.thePlayer.motionX *= 1.005;
                mc.thePlayer.motionZ *= 1.005;
            }

            if (mc.thePlayer.motionY < 0.001 && mc.thePlayer.motionY > -0.03) {
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    mc.thePlayer.motionX *= 1.005;
                    mc.thePlayer.motionZ *= 1.005;
                } else {
                    mc.thePlayer.motionX *= 1.002;
                    mc.thePlayer.motionZ *= 1.002;
                }
            }
        }
    };

    private final Handler<MotionEvent> motionEventHandler = event -> {
        if (mode.is("watchdog")) {
            if (faststop.getValue() && !PlayerUtils.isMoving()) {
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionZ = 0;
            }
            if (event.isPre()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
                if (PlayerUtils.isMoving()) {
                    if (!lowhop.getValue()) {
                        if (PlayerUtils.isBlockUnder(mc.thePlayer)) {
                            if (mc.thePlayer.onGround) {
                                mc.thePlayer.jump();
                                PlayerUtils.strafe();
                            }
                            if (offGroundticks == 9 && strafe.getValue()) {
                                mc.thePlayer.motionY = -0.07;
                                PlayerUtils.strafe(0.3F);
                            }
                        } else {
                            if (mc.thePlayer.onGround) {
                                mc.thePlayer.jump();
                                PlayerUtils.strafe((float) (0.476 + PlayerUtils.getSpeedEffect() * 0.04));
                            }
                        }
                    } else {
                        switch (mc.thePlayer.offGroundTicks) {
                            case 0:
                                mc.thePlayer.jump();
                                PlayerUtils.strafe(0.485);
                                break;
                            case 5:
                                if (strafe2.getValue())
                                    PlayerUtils.strafe(0.315);
                                mc.thePlayer.motionY = PlayerUtils.predictedMotion(mc.thePlayer.motionY, 2);
                                break;
                            case 6:
                                if (strafe2.getValue())
                                    PlayerUtils.strafe();
                                break;
                        }
                    }
                }
            }
        } else if (mode.is("entityboost")) {
            if (event.isPre()) {
                entityBoost();
            }
        }
    };

    private void entityBoost() {
        EntityPlayerSP thePlayer = mc.thePlayer;
        AxisAlignedBB playerBox = mc.thePlayer.boundingBox.expand(1.0, 1.0, 1.0);
        int c = 0;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand) && entity.getEntityId() != mc.thePlayer.getEntityId() && playerBox.intersectsWith(entity.boundingBox) && entity.getEntityId() != -8 && entity.getEntityId() != -1337 && !Faiths.moduleManager.getModule(ModuleBlink.class).getState()) {
                c += 1;
                if (mcount.getValue()) {
                    break;
                }
            }
        }
        if (c > 0) {
            double strafeOffset = c * speed.getValue();
            double speedOffset = c * speed.getValue();

            if (thePlayer.movementInput.moveForward == 0 && thePlayer.movementInput.moveStrafe == 0) {
                if (thePlayer.motionX > strafeOffset) {
                    thePlayer.motionX -= strafeOffset;
                } else if (thePlayer.motionX < -strafeOffset) {
                    thePlayer.motionX += strafeOffset;
                } else {
                    thePlayer.motionX = 0.0;
                }
                if (thePlayer.motionZ > strafeOffset) {
                    thePlayer.motionZ -= strafeOffset;
                } else if (thePlayer.motionZ < -strafeOffset) {
                    thePlayer.motionZ += strafeOffset;
                } else {
                    thePlayer.motionZ = 0.0;
                }

            }
            float yaw = getYaw();

            double mx = -Math.sin(Math.toRadians(yaw));

            if (mx < 0.0) {
                if (thePlayer.motionX > strafeOffset) {
                    thePlayer.motionX -= strafeOffset;
                } else
                    thePlayer.motionX += mx * speedOffset;

            } else if (mx > 0.0) {
                if (thePlayer.motionX < -strafeOffset) {
                    thePlayer.motionX += strafeOffset;
                } else
                    thePlayer.motionX += mx * speedOffset;

            }

            double mz = Math.cos(Math.toRadians(yaw));
            if (mz < 0.0) {
                if (thePlayer.motionZ > strafeOffset) {
                    thePlayer.motionZ -= strafeOffset;
                } else
                    thePlayer.motionZ += mz * speedOffset;

            } else if (mz > 0.0) {
                if (thePlayer.motionZ < -strafeOffset) {
                    thePlayer.motionZ += strafeOffset;
                } else
                    thePlayer.motionZ += mz * speedOffset;
            }
        }
    }
    private float getYaw() {
        if(follow.getValue() && mc.gameSettings.keyBindJump.pressed){
            float yaw = Faiths.INSTANCE.getRotationManager().rotation.getX();
            if (antivoid.getValue() && isVoid(yaw)) {
                yaw += 180.0f;
            }
            return yaw;
        }else{
            return mc.thePlayer.rotationYaw;
        }
    }

    private boolean isVoid(float yaw) {
        double mx = -Math.sin(Math.toRadians(yaw));
        double mz = Math.cos(Math.toRadians(yaw));
        double posX = mc.thePlayer.posX + (1.5 * mx);
        double posZ = mc.thePlayer.posZ + (1.5 * mz);
        for (int i = 0; i < 16; i++) {
            if (!mc.theWorld.isAirBlock(new BlockPos(posX, mc.thePlayer.posY - i, posZ))) {
                return false;
            }
        }
        return true;
    }
}
