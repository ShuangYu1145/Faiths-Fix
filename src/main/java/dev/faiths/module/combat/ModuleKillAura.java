package dev.faiths.module.combat;

import com.google.common.base.Predicates;
import com.viaversion.viarewind.protocol.protocol1_8to1_9.Protocol1_8To1_9;
import com.viaversion.viarewind.utils.PacketUtil;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.Render3DEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.player.ModuleBlink;
import dev.faiths.module.render.ModuleFarmHunterESP;
import dev.faiths.module.world.ModuleBedBreaker;
import dev.faiths.module.world.ModuleScaffold;
import dev.faiths.module.world.ModuleTeams;
import dev.faiths.utils.Pair;
import dev.faiths.utils.TimerUtil;
import dev.faiths.utils.player.RotationUtils;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.value.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.*;
import net.viamcp.fixes.AttackOrder;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleKillAura extends CheatModule {
    public ValueMode priority = new ValueMode("Priority", new String[] { "Range", "Fov", "Angle", "Health" }, "Range");
    public ValueMode mode = new ValueMode("Mode", new String[] { "Single", "Switch", "Multi" }, "Single");
    public ValueMode attackMode = new ValueMode("Attack Mode", new String[] { "Pre", "Post" }, "Post");

    public ValueMode rotMode = new ValueMode("Rotation Mode", new String[] { "Normal", "MegaWalls" }, "Normal");

    public static ValueMode abMode = new ValueMode("AutoBlock mode", new String[] { "Grim", "Watchdog", "Fake", "Off" },
            "Watchdog");

    public ValueInt cps = new ValueInt("CPS", 13, 1, 20);

    public static ValueMode rangemode = new ValueMode("RangeAlgorithm", new String[] {"Strict", "Custom" }, "Strict");

    public static ValueFloat range = new ValueFloat("Range", 3.00f, 2.00f, 6.00f).visible(()->rangemode.is("Custom"));
    public static ValueFloat blockRange = new ValueFloat("Block Range", 4.00f, 2.00f, 6.00f);

    public ValueFloat scanRange = new ValueFloat("Scan Range", 5.00f, 2.00f, 6.00f);

    public ValueInt switchDelay = new ValueInt("Switch delay", 500, 0, 1000);
    public static ValueBoolean moveFixValue = new ValueBoolean("Movement Fix", false);
    public static ValueBoolean disableOnScaffold = new ValueBoolean("Disable On Scaffold", false);
    public static ValueBoolean disable = new ValueBoolean("Disable On Teleport", false);
    public static ValueBoolean strictvalue = new ValueBoolean("Strict", false).visible(() -> moveFixValue.getValue());
    public static ValueBoolean rayCastValue = new ValueBoolean("RayCast", true);
    private final ValueMultiBoolean targetsvalue = new ValueMultiBoolean("Targets",
            new Pair("Invisibles", true),
            new Pair("Mobs", false),
            new Pair("Players", true),
            new Pair("Animals", false),
            new Pair("FakeAnimals", false));

    private final TimerUtil switchTimer = new TimerUtil();
    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil rotationtimer = new TimerUtil();
    public List<Entity> targets = new ArrayList<Entity>();
    public static EntityLivingBase target;
    public static boolean isBlocking = false;
    public static boolean renderBlocking = false;

    public int index = 0;
    public static Vector2f currentRotation = new Vector2f(0, 0);

    public ModuleKillAura() {
        super("KillAura", Category.COMBAT, Keyboard.KEY_R);
    }

    private boolean heldSword() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    @Override
    public String getSuffix() {
        return mode.getValue();
    }

    @Override
    public void onEnable() {
        switchTimer.reset();
        isBlocking = false;
        renderBlocking = false;
        target = null;
        targets.clear();
        if (isBlocking && !abMode.getValue().equals("Off")) {
            stopBlocking(true);
        }
        index = 0;

    }

    @Override
    public void onDisable() {
        if (mc.thePlayer == null)
            return;
        target = null;
        targets.clear();
        if (isBlocking && !abMode.getValue().equals("Off")) {
            stopBlocking(true);
        }
        isBlocking = false;
        renderBlocking = false;
        index = 0;
    }

    static float getRange(){
        return rangemode.is("Strict") ? 3f : range.getValue();
    }

    private final Handler<UpdateEvent> updateEventHandler = event -> {
        if (!targets.isEmpty()) {
            if (this.index >= targets.size()) {
                this.index = 0;
            }
            if (mc.thePlayer.getClosestDistanceToEntity(targets.get(this.index)) <= getRange()) {
                target = (EntityLivingBase) targets.get(this.index);
            } else {
                target = (EntityLivingBase) targets.get(0);
            }
        }
        if (disableOnScaffold.getValue() && Faiths.moduleManager.getModule(ModuleScaffold.class).getState()) {
            target = null;
        }

        if (target != null && mc.thePlayer.getClosestDistanceToEntity(target) <= getRange()) {
            float[] rotation = getRot();
            Faiths.INSTANCE.getRotationManager().setRotation(new Vector2f(rotation[0], rotation[1]), 180f,
                    moveFixValue.getValue(), strictvalue.getValue());
        }
    };

    public static float random(float min, float max) {
        Random random = new Random();
        float range = max - min;
        float scaled = random.nextFloat() * range;
        float shifted = scaled + min;
        return shifted;
    }

    public static float[] cahgnle(float[] vector) {
        vector[0] %= 360.0F;

        for (vector[1] %= 360.0F; vector[0] <= -180.0F; vector[0] += 360.0F) {
        }

        while (vector[1] <= -180.0F) {
            vector[1] += 360.0F;
        }

        while (vector[0] > 180.0F) {
            vector[0] -= 360.0F;
        }

        while (vector[1] > 180.0F) {
            vector[1] -= 360.0F;
        }

        return vector;
    }

    public static Vec3 getVectorForRotation(float yaw, float pitch) {
        // radians
        float yawCos = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-pitch * 0.017453292F);
        float pitchSin = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    public List<MovingObjectPosition> rayCastByRotation(float yaw, float pitch) {
        ArrayList<MovingObjectPosition> targets1 = new ArrayList<MovingObjectPosition>();
        Entity entity = mc.getRenderViewEntity();

        if (entity != null && mc.theWorld != null) {
            float reach = target.getCollisionBorderSize();
            float f = 1.0F;
            Vec3 eyeVec = entity.getPositionEyes(1.0F);
            Vec3 lookVec = getVectorForRotation(yaw, pitch);
            Vec3 vec32 = eyeVec.addVector(lookVec.xCoord * (double) reach, lookVec.yCoord * (double) reach,
                    lookVec.zCoord * (double) reach);

            List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity,
                    entity.getEntityBoundingBox()
                            .addCoord(lookVec.xCoord * (double) reach, lookVec.yCoord * (double) reach,
                                    lookVec.zCoord * (double) reach)
                            .expand(f, f, f),
                    Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));

            for (Entity entity1 : list) {
                float f1 = entity1.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(eyeVec, vec32);
                if (movingobjectposition != null) {
                    movingobjectposition.entityHit = entity1;
                    targets1.add(new MovingObjectPosition(entity1, movingobjectposition.hitVec));
                }
            }
        }

        if (entity != null) {
            targets1.sort((o1, o2) -> {
                Vec3 eyeVec = entity.getPositionEyes(1.0F);
                return (int) ((eyeVec.distanceTo(o1.hitVec) - eyeVec.distanceTo(o2.hitVec)) * 100.0);
            });
        }
        return targets1;
    }

    private float[] Zenith(float[] dst, float[] src) {
        float[] smoothedAngle = cahgnle(new float[] { src[0] - dst[0], src[1] - dst[1] });
        float horizontalSpeed = mode.is("Switch") ? random(180.0f, 180.0f)
                : random(15.0F, 25.0F);
        float verticalSpeed = mode.is("Switch") ? random(180.0f, 180.0f) : random(25.0f, 35.0f);
        if (target != null) {
            for (MovingObjectPosition obj : this.rayCastByRotation(src[0], src[1])) {
                if (obj.entityHit != null && obj.entityHit != mc.thePlayer
                        && isValid(obj.entityHit, range.getValue())) {
                    verticalSpeed = (float) ((double) verticalSpeed * 0.3D);
                    break;
                }
            }
        }
        smoothedAngle[0] = src[0] - smoothedAngle[0] / 180.0F * (horizontalSpeed / 2.0F);
        smoothedAngle[1] = src[1];
        smoothedAngle[0] = RotationUtils.changeRotation(smoothedAngle[0], dst[0], horizontalSpeed);
        smoothedAngle[1] = RotationUtils.changeRotation(smoothedAngle[1], Math.max(Math.min(dst[1], 90.0F), -90.0F),
                verticalSpeed);
        return smoothedAngle;
    }

    public float[] getRotationsToPos(double x, double z, double y) {
        final double diffX = x - mc.thePlayer.posX;
        final double diffZ = z - mc.thePlayer.posZ;
        final double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(y, dist) * 180.0 / Math.PI));
        return new float[] { yaw, pitch };
    }

    public float[] getRotation(EntityLivingBase ent) {
        double y;
        final double x = ent.posX;
        final double z = ent.posZ;
        if (ent instanceof EntityEnderman) {
            y = ent.posY - mc.thePlayer.posY;
        } else {
            double targetY = (double) mc.thePlayer.getEyeHeight() - (1.65 + 1.2);
            y = ent.posY + (double) ent.getEyeHeight() - 1.5 < mc.thePlayer.posY + targetY
                    ? ent.posY + (double) ent.getEyeHeight() - mc.thePlayer.posY
                            + ((double) mc.thePlayer.getEyeHeight() - 3.0)
                    : (ent.posY - 1.5 > mc.thePlayer.posY + targetY
                            ? ent.posY - 3.0 - mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight()
                            : targetY);
        }
        return getRotationsToPos(x, z, y);
    }

    private float yaw, pitch;
    float[] rotations;

    private float[] getRot() {
        float[] rot;
        if (target instanceof EntityPlayer) {
            rot = RotationUtils.getHVHRotation(ModuleBackTrack.getClosedBBox((EntityPlayer) target), range.getValue());
        } else {
            rot = RotationUtils.getHVHRotation(target.boundingBox, range.getValue());
        }
        switch (rotMode.getValue()) {
            case "Normal":
                if (target instanceof EntityPlayer) {
                    rot = RotationUtils.getAngles(ModuleBackTrack.getClosedBBox((EntityPlayer) target));
                } else {
                    rot = RotationUtils.getAngles(target.boundingBox);
                }
                break;
            case "MegaWalls":
                rotations = getRotation((EntityLivingBase) target);
                float a = MathHelper.wrapAngleTo180_float(rotations[0] - yaw);

                float[] srcRotations = new float[] { yaw, pitch };
                float[] targetRotations = new float[] { yaw + a, Math.max(Math.min(rotations[1], 90.0F), -90.0F) };
                float[] smoothedAim = this.Zenith(targetRotations, srcRotations);

                yaw = smoothedAim[0];
                pitch = Math.max(Math.min(smoothedAim[1], 90.0F), -90.0F);
                rot = smoothedAim;
                break;
        }
        return rot;
    }

    private final Handler<MotionEvent> motionEventHandler = event -> {
        // if ((getModule(Blink.class).getState() &&
        // !getModule(Blink.class).auraValue.getValue()) ||
        // getModule(Scaffold.class).getState() || getModule(Stuck.class).getState())
        // return;
        //
        // this.setSuffix(mode.getValue());

        if (event.isPost()) {
            if (mc.thePlayer.isDead || mc.thePlayer.isSpectator()) {
                return;
            }

            targets = getTargets(Double.valueOf(scanRange.getValue()));

            if (targets.isEmpty()) {
                target = null;
            }

            sortTargets();

            if (targets.size() > 1 && mode.getValue().equals("Switch") || mode.getValue().equals("Multi")) {
                if (switchTimer.delay(switchDelay.getValue().longValue()) || mode.getValue().equals("Multi")) {
                    ++this.index;
                    switchTimer.reset();
                }
            }

            if (targets.size() > 1 && mode.getValue().equals("Single") && target != null) {
                if (mc.thePlayer.getClosestDistanceToEntity(target) > scanRange.getValue()) {
                    ++index;
                } else if (target.isDead) {
                    ++index;
                }
            }

        }
    };

    public static boolean shouldAttack() {
        final MovingObjectPosition movingObjectPosition = mc.objectMouseOver;
        if (disableOnScaffold.getValue() && Faiths.moduleManager.getModule(ModuleScaffold.class).getState()) return false;

        return ((mc.thePlayer.canEntityBeSeen(target) ? mc.thePlayer.getClosestDistanceToEntity(target)
                : getEntityRange(target)) <= getRange())
                && (((!rayCastValue.getValue()) || !mc.thePlayer.canEntityBeSeen(target)) ||
                        (rayCastValue.getValue() && movingObjectPosition != null
                                && movingObjectPosition.entityHit == target));
    }

    public static float getEntityRange(Entity entity) {
        if (entity instanceof EntityPlayer) {
            return mc.thePlayer.getClosestDistanceToBBox(ModuleBackTrack.getClosedBBox((EntityPlayer) entity));
        }

        return mc.thePlayer.getDistanceToEntity(target);
    }

    public static boolean shouldBlock() {
        return target != null && mc.thePlayer.getClosestDistanceToEntity(target) <= blockRange.getValue()
                && !abMode.is("Off") && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    private void attack(MotionEvent eventMotion) {
        if (shouldAttack() && attackTimer.hasTimeElapsed(700L / cps.getValue().intValue())) {
            // mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            AttackOrder.sendFixedAttack(mc.thePlayer, target);
            attackTimer.reset();
        }
    }
    boolean blocked;
    private final Handler<MotionEvent> motionEventHandler1 = event -> {

        if (event.isPre()) {

            if ((shouldBlock() && !isBlocking) || (abMode.is("Grim") && shouldBlock())) {
                doBlock();
            }

            // preReleaseBlock
            if (targets.isEmpty() && attackMode.is("Pre") && !abMode.is("Watchdog") && isBlocking) {
                stopBlocking(true);
            }

            // preAttack
            if (attackMode.is("Pre") && target != null) {
                attack(event);
            }

            if (abMode.is("Watchdog") && shouldBlock()) {
                doBlock();
            }

            if (isBlocking && abMode.is("Grim") && blocked && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                blocked = false;
            }

        } else {

            if (isBlocking && abMode.is("Grim") && !blocked && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                mc.getNetHandler().getNetworkManager()
                        .sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));

                    blocked = true;
//                    if (!mc.isSingleplayer()) {
//                        PacketWrapper useItemMainHand = PacketWrapper.create(29, null,
//                                Via.getManager().getConnectionManager().getConnections().iterator().next());
//                        useItemMainHand.write(Type.VAR_INT, 0);
//                        PacketUtil.sendToServer(useItemMainHand, Protocol1_8To1_9.class, true, true);
//                    }
            }

            if (abMode.is("Watchdog") && !shouldBlock() && isBlocking) {
                stopBlocking(true);
            }

        }

        if (event.isPost() && attackMode.is("Post")) {
            if (target != null) {
                attack(event);
            }

            if (targets.isEmpty() && isBlocking) {
                stopBlocking(true);
                blocked = false;
            }

        }
    };

    void stopBlocking(boolean render) {
        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && renderBlocking) {
            switch (abMode.getValue()) {
                case "Grim":
                case "Watchdog":
                    mc.gameSettings.keyBindUseItem.pressed = false;
                    mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    if (target != null) {
                        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
                    }
                    break;
                case "Fake":
                    break;
            }
            if (render)
                renderBlocking = false;
            isBlocking = false;
        }
    }

    void doBlock() {
        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword && !Faiths.moduleManager.getModule(ModuleBlink.class).getState()) {
            if (ModuleBedBreaker.skipAb) {
                ModuleBedBreaker.skipAb = false;
                return;
            }
            switch (abMode.getValue()) {
                case "Watchdog":
                    mc.getNetHandler()
                            .addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                    if (target != null) {
                        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
                    }
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 255,
                            mc.thePlayer.inventory.getCurrentItem(), 0.0F, 0.0F, 0.0F));
                    if (!mc.isSingleplayer()) {
                        PacketWrapper use = PacketWrapper.create(29, null,
                                Via.getManager().getConnectionManager().getConnections().iterator().next());
                        use.write(Type.VAR_INT, 1);
                        PacketUtil.sendToServer(use, Protocol1_8To1_9.class, true, true);
                    }
                case "Fake":
                    break;
                case "Grim":
                    break;
            }
            if (!abMode.is("Fake"))
                isBlocking = true;

            renderBlocking = true;
        }
    }

    public List<Entity> getTargets(Double value) {
        return Minecraft.getMinecraft().theWorld.loadedEntityList.stream()
                .filter(e -> {
                    double distance;
                    if (e instanceof EntityPlayer) {
                        distance = mc.thePlayer.getClosestDistanceToBBox(ModuleBackTrack.getClosedBBox((EntityPlayer) e));
                    } else {
                        distance = mc.thePlayer.getClosestDistanceToEntity(e);
                    }
                    return distance <= value && isValid(e, value);
                })
                .collect(Collectors.toList());
    }

    public boolean isValid(Entity entity, double range) {
        if (mc.thePlayer.getClosestDistanceToEntity(entity) > range)
            return false;
        if (entity.isInvisible() && !targetsvalue.isEnabled("Invisibles"))
            return false;
        if (!entity.isEntityAlive())
            return false;
        if (entity == Minecraft.getMinecraft().thePlayer || entity.isDead
                || Minecraft.getMinecraft().thePlayer.getHealth() == 0F)
            return false;
        if ((entity instanceof EntityMob || entity instanceof EntityGhast || entity instanceof EntityGolem
                || entity instanceof EntityDragon || entity instanceof EntitySlime) && targetsvalue.isEnabled("Mobs") && !ModuleTeams.isOnSameTeam(entity))
            return true;
        if ((entity instanceof EntitySquid || entity instanceof EntityBat || entity instanceof EntityVillager)
                && targetsvalue.isEnabled("Animals"))
            return true;
        if (entity instanceof EntityAnimal && targetsvalue.isEnabled("Animals"))
            return true;
        if (Faiths.moduleManager.getModule(ModuleFarmHunterESP.class).getState() && (ModuleFarmHunterESP.entities.contains(entity) || ModuleFarmHunterESP.mentities.contains(entity)) && targetsvalue.isEnabled("FakeAnimals"))
            return true;
        if (entity.getEntityId() == -8 || entity.getEntityId() == -1337) {
            return false;
        }
        if (ModuleTeams.isSameTeam(entity))
            return false;

        if (entity instanceof EntityArmorStand || entity instanceof EntityArrow || entity instanceof EntityItem) return false;

        if(Faiths.moduleManager.getModule(ModuleAntiBot.class).isBot(entity))return false;

        return entity instanceof EntityPlayer && targetsvalue.isEnabled("Players");
    }

    public boolean isLookingAtEntity(Vector2f rotations, Entity target) {
        double range = this.range.getValue();
        Vec3 src = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 rotationVec = getVectorForRotation(rotations.y, rotations.x);
        Vec3 dest = src.addVector(rotationVec.xCoord * range, rotationVec.yCoord * range, rotationVec.zCoord * range);
        MovingObjectPosition obj = mc.theWorld.rayTraceBlocks(src, dest, false, false, true);
        if (obj == null) {
            return false;
        }
        return target.getEntityBoundingBox().expand(0.1f, 0.1f, 0.1f).calculateIntercept(src, dest) != null;
    }

    private void sortTargets() {
        if (!targets.isEmpty()) {
            EntityPlayerSP thePlayer = mc.thePlayer;
            switch (priority.getValue()) {
                case "Range":
                    targets.sort((o1, o2) -> Float.valueOf(o1.getClosestDistanceToEntity(thePlayer))
                            .compareTo(o2.getClosestDistanceToEntity(thePlayer)));
                    break;
                case "Fov":
                    targets.sort(Comparator.comparingDouble(o -> this.getDistanceBetweenAngles(thePlayer.rotationPitch,
                            RotationUtils.getRotationsNeeded(o)[0])));
                    break;
                case "Angle":
                    targets.sort((o1, o2) -> {
                        float[] rot1 = RotationUtils.getRotationsNeeded(o1);
                        float[] rot2 = RotationUtils.getRotationsNeeded(o2);
                        return (int) (thePlayer.rotationYaw - rot1[0] - (thePlayer.rotationYaw - rot2[0]));
                    });
                    break;
                case "Health":
                    targets.sort((o1,
                            o2) -> (int) (((EntityLivingBase) o1).getHealth() - ((EntityLivingBase) o2).getHealth()));
                    break;
            }
        }
    }

    private Handler<PacketEvent> packetEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE && disable.getValue()) {
            if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) event.getPacket();
                if (mc.thePlayer.getDistance(packet.getX(), packet.getY(), packet.getZ()) >= 10) {
                    toggle();
                }
            }
        }

        try {
            if (event.getType() == PacketEvent.Type.SEND && event.getPacket() instanceof C02PacketUseEntity) {
                C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
                /*if (packet.getEntityFromWorld(mc.theWorld) instanceof AbstractClientPlayer &&
                        IRC.connected && IRC.userRankList.get(IRC.selfName).equals("Stable") &&
                        IRC.playerMap.containsValue(((AbstractClientPlayer) packet.getEntityFromWorld(mc.theWorld)).getNameClear()) &&
                        !IRC.userRankList.get(IRC.getUsernameInGame(((AbstractClientPlayer) packet.getEntityFromWorld(mc.theWorld)).getNameClear())).equals("Stable")) {
                    event.setCancelled(RandomUtil.randomInt(0, 100, true, true) % 10 == 0);
                }*/
            }
        } catch (Exception ignored) {}
    };

    private float getDistanceBetweenAngles(float angle1, float angle2) {
        float agl = Math.abs(angle1 - angle2) % 360.0f;
        if (agl > 180.0f) {
            agl = 0.0f;
        }
        return agl - 1;
    }

    private Handler<Render3DEvent> render3DEventHandler = event -> {
        if (target != null) {
            RenderUtils.drawEntityBox(target, new Color(255, 43, 28), true);
        }
    };
}
