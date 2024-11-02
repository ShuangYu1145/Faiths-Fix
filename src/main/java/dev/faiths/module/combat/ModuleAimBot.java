package dev.faiths.module.combat;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.Render3DEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.world.ModuleTeams;
import dev.faiths.utils.MSTimer;
import dev.faiths.utils.player.Rotation;
import dev.faiths.utils.player.RotationUtils;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueInt;
import dev.faiths.value.ValueMode;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.server.S3APacketTabComplete;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Random;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleAimBot extends CheatModule {
    public final ValueBoolean autoFire = new ValueBoolean("AutoFire", true);
    public final ValueInt fireDelay = new ValueInt("FireDelay", 20, 0, 1000).visible(autoFire::getValue);
    public final ValueBoolean dynamicPredict = new ValueBoolean("DynamicPredict", true);
    public final ValueFloat predict = new ValueFloat("Predict", 1.2f, 0f, 8f).visible(() -> !dynamicPredict.getValue());
    public final ValueBoolean thoughWall = new ValueBoolean("ThoughWall", false);
    public final ValueMode mode = new ValueMode("Mode", new String[]{"Zombies", "Players"}, "Zombies");
    public final ValueBoolean teams = new ValueBoolean("Teams", true);
    public final ValueBoolean safe = new ValueBoolean("Safe", false);
    public final ValueBoolean swap = new ValueBoolean("SwapWeapon", false);
    public final ValueInt weaponCount = new ValueInt("WeaponCount", 2, 1, 5).visible(swap::getValue);
    public final ValueInt visualSlot = new ValueInt("VisualSlot", 2, 1, 9).visible(swap::getValue);
    public final ValueBoolean aa = new ValueBoolean("AntiAim", false);

    private MSTimer timerUtil = new MSTimer();
    private MSTimer timer = new MSTimer();
    private Random random = new Random(System.currentTimeMillis());
    private long ping, time;
    private boolean pinging;
    private int swapSlot = 1;
    private boolean flagHit;
    public static float lastX, lastY;
    public static float prevX, prevY;

    private Entity getClosestTarget() {
        float distance = 50f;
        Entity target = null;
        switch (mode.getValue()) {
            case "Zombies":
                for (Entity entity: mc.theWorld.getLoadedEntityList()) {
                    float[] rot = RotationUtils.getRotations(entity);
                    if (entity instanceof IAnimals &&
                            /*!entity.isInvisible() &&*/
                            entity.getDistanceToEntity(mc.thePlayer) <= distance &&
                            canEntityBeSeen(entity) &&
                            ((EntityLivingBase) entity).getHealth() > 0 &&
                            (!safe.getValue() || Faiths.INSTANCE.getRotationManager().getRotationDifference(new Rotation(rot[0], rot[1]), new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) <= mc.gameSettings.fovSetting / 4f) &&
                            !(entity instanceof EntityVillager) &&
                            Math.abs(entity.motionY) <= 1) {
                        distance = entity.getDistanceToEntity(mc.thePlayer);
                        target = entity;
                    }
                }

                if (target == null && thoughWall.getValue()) {
                    for (Entity entity: mc.theWorld.getLoadedEntityList()) {
                        float[] rot = RotationUtils.getRotations(entity);
                        if (entity instanceof IAnimals &&
                                /*!entity.isInvisible() &&*/
                                entity.getDistanceToEntity(mc.thePlayer) <= distance &&
                                ((EntityLivingBase) entity).getHealth() > 0 &&
                                (!safe.getValue() || Faiths.INSTANCE.getRotationManager().getRotationDifference(new Rotation(rot[0], rot[1]), new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) <= mc.gameSettings.fovSetting / 4f) &&
                                !(entity instanceof EntityVillager) &&
                                Math.abs(entity.motionY) <= 1) {
                            distance = entity.getDistanceToEntity(mc.thePlayer);
                            target = entity;
                        }
                    }
                }
                break;

            case "Players":
                for (Entity entity: mc.theWorld.getLoadedEntityList()) {
                    float[] rot = RotationUtils.getRotations(entity);
                    if (entity instanceof EntityPlayer &&
                            entity != mc.thePlayer &&
                            (!ModuleTeams.isSameTeam(entity) || !teams.getValue()) &&
                            /*!entity.isInvisible() &&*/
                            entity.getDistanceToEntity(mc.thePlayer) <= distance && canEntityBeSeen(entity) &&
                            ((EntityLivingBase) entity).getHealth() > 0 &&
                            (!safe.getValue() || Faiths.INSTANCE.getRotationManager().getRotationDifference(new Rotation(rot[0], rot[1]), new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) <= mc.gameSettings.fovSetting / 4f)) {
                        distance = entity.getDistanceToEntity(mc.thePlayer);
                        target = entity;
                    }
                }

                if (target == null && thoughWall.getValue()) {
                    for (Entity entity: mc.theWorld.getLoadedEntityList()) {
                        float[] rot = RotationUtils.getRotations(entity);
                        if (entity instanceof EntityPlayer &&
                                entity != mc.thePlayer &&
                                (!ModuleTeams.isSameTeam(entity)  || !teams.getValue()) &&
                                /*!entity.isInvisible() &&*/
                                entity.getDistanceToEntity(mc.thePlayer) <= distance &&
                                ((EntityLivingBase) entity).getHealth() > 0 &&
                                (!safe.getValue() || Faiths.INSTANCE.getRotationManager().getRotationDifference(new Rotation(rot[0], rot[1]), new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)) <= mc.gameSettings.fovSetting / 4f)) {
                            distance = entity.getDistanceToEntity(mc.thePlayer);
                            target = entity;
                        }
                    }
                }
        }
        return target;
    }

    public ModuleAimBot() {
        super("AimBot", Category.COMBAT);
        lastX = 0;
        lastY = 0;
    }

    private final Handler<MotionEvent> eventHandler = event -> {
        if (timer.delay(10000)) {
            timer.reset();
            time = System.currentTimeMillis();
            mc.getNetHandler().addToSendQueue(new C14PacketTabComplete("/heheshabiNMZLQuanNiMaLeGeBiDeQian" + new Random().nextInt(10)));
            pinging = true;
        }

        Entity target = getClosestTarget();
        if (target != null) {
            double[] targetRot = getRotationsNeeded(target);
            Faiths.INSTANCE.getRotationManager().setRotation(new Vector2f((float) targetRot[0], (float) targetRot[1]), 180f,
                    true, safe.getValue());
            lastX = (float) targetRot[0];
            lastY = (float) targetRot[1];
            if (autoFire.getValue() && Faiths.INSTANCE.getRotationManager().getRotationDifference(new Rotation(lastX, lastY)) <= 5 && timerUtil.check(fireDelay.getValue())) {
                mc.addScheduledTask(() -> {
                    if (swap.getValue()) {
                        mc.thePlayer.inventory.currentItem = visualSlot.getValue() - 1;
                        swapSlot += 1;
                        if (swapSlot > weaponCount.getValue()) {
                            swapSlot = 1;
                        }
                        mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(swapSlot));
                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(swapSlot)));
                        mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(visualSlot.getValue() - 1));
                    } else {
                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                    }
                });
                timerUtil.reset();
            }
        } else if (aa.getValue()) {
            Faiths.INSTANCE.getRotationManager().setRotation(new Vector2f(mc.thePlayer.rotationYaw - 180, 90), 180f,
                    false, false);
            // prevX = lastX;
            // prevY = lastY;
            // lastX = mc.thePlayer.rotationYaw - 180;
            // lastY = 90;
            // event.setYaw(lastX);
            // event.setPitch(lastY);
        }
    };

    public double[] getRotationsNeeded(Entity entity) {
        if (entity == null) return null;

        double[] targetPos = getPredictPos(entity);
        Vector2f vector2f = RotationUtils.getRotations(targetPos[0], targetPos[1], targetPos[2]);

        return new double[]{vector2f.x, vector2f.y};
    }


    public double[] getPredictPos(Entity entity) {
        double x = entity.posX + calcPredict(entity.posX, entity.lastTickPosX);
        double y = entity.posY + (calcPredict(entity.posY, entity.lastTickPosY) / 2.0) + entity.getEyeHeight();
        double z = entity.posZ + calcPredict(entity.posZ, entity.lastTickPosZ); // @on
        for (float i = 0; i < 1; i = i + 0.01f) {
            y -= entity.getEyeHeight() * 0.01;
            if (mc.theWorld.rayTraceBlocks(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), new Vec3(x, y, z)) == null) {
                return new double[]{x, y, z};
            }
        }
        return new double[]{x, y, z};
    }

    public double calcPredict(double a, double lastTick) {
        if (a - lastTick >= getPredict() * 2.0) return getPredict() * 2.0;
        if (a - lastTick <= getPredict() * -2.0) return getPredict() * -2.0;
        return (a - lastTick) * getPredict();
    }

    public double getPredict() {
        if (dynamicPredict.getValue()) {
            return ((double) getPing()) / 50.0;
        } else {
            return predict.getValue();
        }
    }

    public boolean canEntityBeSeen(Entity entity) {
        double[] d1 = getPredictPos(entity);
        return mc.theWorld.rayTraceBlocks(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ), new Vec3(d1[0], d1[1], d1[2])) == null;
    }

    public int toRGBAHex(float r, float g, float b, float a) {
        return ((int) (a * 255.0f) & 255) << 24 | ((int) (r * 255.0f) & 255) << 16 | ((int) (g * 255.0f) & 255) << 8 | (int) (b * 255.0f) & 255;
    }

    public long getPing() {
        return ping;
    }

    private final Handler<PacketEvent> packetEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE) {
            final Packet<?> packet = event.getPacket();
            if (packet instanceof S3APacketTabComplete && pinging) {
                ping = System.currentTimeMillis() - time;
                pinging = false;
            }
        }
    };

    private final Handler<Render3DEvent> render3DEventHandler = event -> {
        Color targetcolor = new Color(56, 199, 231);
        Color targetedcolor = new Color(138, 56, 231);
        Color color = new Color(255, 43, 28);
        Entity target = getClosestTarget();
        float[] colors = new float[]{(float) color.getRed(), color.getGreen(), color.getBlue()};
        for (Entity entity: mc.theWorld.getLoadedEntityList()) {
            float[] rot = RotationUtils.getRotations(entity);
            if (entity instanceof IAnimals && !(entity instanceof EntityVillager)) {
                RenderUtils.drawEntityBox(entity, (entity == target)?targetedcolor : targetcolor, false);
            }
        }


        if (target == null) return;

        double[] pos = getPredictPos(target);

        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GL11.glEnable(2848);

        // Draw box
        AxisAlignedBB bb = new AxisAlignedBB(pos[0] - 0.2, pos[1] - 0.2, pos[2] - 0.2, pos[0] + 0.2, pos[1] + 0.2, pos[2] + 0.2);
        RenderUtils.glColor(toRGBAHex(colors[0] / 255.0f, colors[1] / 255.0f, colors[2] / 255.0f, 0.8f));
        RenderUtils.drawFilledBox(bb);
        // Draw box end

        // Draw line
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.5f);
        GL11.glBegin(1);
        GL11.glVertex3d(0.0D, mc.thePlayer.getEyeHeight(), 0.0D);
        GL11.glVertex3d(pos[0] - mc.getRenderManager().renderPosX, pos[1] - mc.getRenderManager().renderPosY, pos[2] - mc.getRenderManager().renderPosZ);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        // Draw line end

        GL11.glDisable(2848);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
    };
}
