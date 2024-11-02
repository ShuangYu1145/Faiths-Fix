package dev.faiths.module.player;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.event.impl.TickUpdateEvent;
import dev.faiths.event.impl.WorldLoadEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.font.FontManager;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.value.ValueBoolean;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.world.WorldSettings;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleBlink extends CheatModule {
    public final ValueBoolean showFakePlayer = new ValueBoolean("FakePlayer", true);
    public final ValueBoolean allowBreak = new ValueBoolean("AllowBreak", false);
    public final ValueBoolean antiaim = new ValueBoolean("AntiAim", false);
    private LinkedList<List<Packet<?>>> packets = new LinkedList<>();
    public static EntityOtherPlayerMP fakePlayer;
    private int ticks;
    public ModuleBlink() {
        super("Blink", Category.PLAYER);
    }
    private int maxTicksBeforeRelease;
    private static boolean fakePlayerDisabled;

    @Override
    public void onEnable() {
        if (mc.thePlayer == null) return;
        if (Faiths.moduleManager.getModule(ModuleAntiVoid.class).getState()) {
            Faiths.notificationManager.pop("You can use antivoid and blink in the same time", 5000, NotificationType.WARNING);
            toggle();
        }
        maxTicksBeforeRelease = 180;
        packets.clear();
        packets.add(new ArrayList<>());
        ticks = 0;
        fakePlayer = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
        fakePlayer.clonePlayer(mc.thePlayer, true);
        fakePlayer.copyLocationAndAnglesFrom(mc.thePlayer);
        fakePlayer.rotationYawHead = mc.thePlayer.rotationYawHead;
        if (allowBreak.getValue()) {
            mc.thePlayer.capabilities.allowEdit = true;
            mc.thePlayer.setGameType(WorldSettings.GameType.SURVIVAL);
        }
        if (showFakePlayer.getValue()) {
            mc.theWorld.addEntityToWorld(-1337, fakePlayer);
            fakePlayerDisabled = true;
        }
    }

    @SuppressWarnings("unused")
    private final Handler<WorldLoadEvent> worldLoadEventHandler = event -> {
        this.setState(false);
    };

    @Override
    public void onDisable() {
        packets.forEach(this::sendTick);
        packets.clear();
        try {
            if (fakePlayer != null && fakePlayerDisabled)
                mc.theWorld.removeEntity(fakePlayer);
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unused")
    private final Handler<PacketEvent> packetHandler = event -> {

        Packet<?> packet = event.getPacket();

        if (event.getType() == PacketEvent.Type.SEND) {
            mc.addScheduledTask(() -> {
                if (packets.isEmpty()) {
                    packets.add(new LinkedList<Packet<?>>());
                }
                packets.getLast().add(packet);
            });
            event.setCancelled(true);
        }

        if (antiaim.getValue()) {
            checkNearbyEntities();
        }
    };


    @SuppressWarnings("unused")
    private final Handler<TickUpdateEvent> tickUpdateEventHandler = event -> {
        ticks++;
        packets.add(new ArrayList<>());
    };

    private void poll() {
        if (packets.isEmpty()) return;
        this.sendTick(packets.getFirst());
        packets.removeFirst();
    }

    private void sendTick(List<Packet<?>> tick) {
        if (mc.getNetHandler() != null) {
            tick.forEach(packet -> {
                mc.getNetHandler().getNetworkManager().sendPacketWithoutHigherPacket(packet);
                this.handleFakePlayerPacket(packet);
            });
        }
    }

    private void checkNearbyEntities() {
        double checkRadiusPlayer = 5.0; // 用于检测玩家的半径
        double checkRadius = 6.0; // 减少检测投掷物的半径，以提前反应
        if (mc.theWorld != null && fakePlayer != null) {
            // 针对玩家检查
            List<Entity> nearbyPlayers = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                    fakePlayer, fakePlayer.getEntityBoundingBox().expand(checkRadiusPlayer, checkRadiusPlayer, checkRadiusPlayer)
            );
            boolean isPlayerNearby = nearbyPlayers.stream()
                    .anyMatch(entity -> entity instanceof EntityPlayer && entity != fakePlayer && entity != mc.thePlayer);

            // 针对投掷物和弓箭检查
            List<Entity> nearbyEntities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                    fakePlayer, fakePlayer.getEntityBoundingBox().expand(checkRadius, checkRadius, checkRadius)
            );
            boolean isDangerousEntityNearby = nearbyEntities.stream()
                    .anyMatch(entity -> (entity instanceof EntityArrow &&
                            entity.motionX * entity.motionX + entity.motionY * entity.motionY + entity.motionZ * entity.motionZ > 0.1) // 判断箭是否正在飞行
                            || entity instanceof EntityFireball || entity instanceof EntitySnowball || entity instanceof EntityTNTPrimed || entity instanceof EntityEgg);

            // 如果检测到直接朝你飞行的箭或其他危险实体，触发瞬移
            if (isDangerousEntityNearby || isPlayerNearby) {
                blinkShortDistance();
            }
        }
    }

    private void blinkShortDistance() {
        final int blinkPacketCount = 6; // 设置所需触发瞬移的数据包数量
        if (packets.size() < blinkPacketCount * 2) {
            // 如果没有足够的数据包，则不进行瞬移
            return;
        }

        for (int i = 0; i < blinkPacketCount; i++) { // 只发送设定数量的数据包（在此例中为5个）
            if (!packets.isEmpty()) {
                sendTick(packets.getFirst());
                packets.removeFirst();
            } else {
                break;
            }
        }
    }

    @SuppressWarnings("unused")
	private Handler<Render2DEvent> render2DEventHandler = event -> {
        if (ticks >= maxTicksBeforeRelease) {
            poll();
            ticks -= 1;
        }

        ScaledResolution sc = new ScaledResolution(mc);
        float strength = ticks / maxTicksBeforeRelease;
        //RenderUtils.drawRect(sc.getScaledWidth() / 2F - 50F, 35F, 100F, 20F, new Color(0, 0, 0, 140).getRGB());
        FontManager.sf20.drawString(((maxTicksBeforeRelease - ticks) / 20) + "s left...", sc.getScaledWidth() / 2F - 15, sc.getScaledHeight() / 2F + 30,-1);
        RenderUtils.drawRect(sc.getScaledWidth() / 2F - 47, sc.getScaledHeight() / 2F + 15,94,5,Color.GRAY);
        RenderUtils.drawRect(sc.getScaledWidth() / 2F - 47, sc.getScaledHeight() / 2F + 15,ticks / 2 + 5,5,-1);
    };

    private void handleFakePlayerPacket(Packet<?> packet) {
        if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition) {
            C03PacketPlayer.C04PacketPlayerPosition position = (C03PacketPlayer.C04PacketPlayerPosition) packet;

            fakePlayer.setPositionAndRotation2(
                    position.x,
                    position.y,
                    position.z,
                    fakePlayer.rotationYaw,
                    fakePlayer.rotationPitch,
                    3,
                    true
            );
            fakePlayer.onGround = position.isOnGround();
        } else if (packet instanceof C03PacketPlayer.C05PacketPlayerLook) {
            C03PacketPlayer.C05PacketPlayerLook rotation = (C03PacketPlayer.C05PacketPlayerLook) packet;
            fakePlayer.setPositionAndRotation2(
                    fakePlayer.posX,
                    fakePlayer.posY,
                    fakePlayer.posZ,
                    rotation.getYaw(),
                    rotation.getPitch(),
                    3,
                    true
            );
            fakePlayer.onGround = rotation.isOnGround();

            fakePlayer.rotationYawHead = rotation.getYaw();
            fakePlayer.rotationYaw = rotation.getYaw();
            fakePlayer.rotationPitch = rotation.getPitch();
        } else if (packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            C03PacketPlayer.C06PacketPlayerPosLook positionRotation = (C03PacketPlayer.C06PacketPlayerPosLook) packet;

            fakePlayer.setPositionAndRotation2(
                    positionRotation.x,
                    positionRotation.y,
                    positionRotation.z,
                    positionRotation.getYaw(),
                    positionRotation.getPitch(),
                    3,
                    true
            );
            fakePlayer.onGround = positionRotation.isOnGround();

            fakePlayer.rotationYawHead = positionRotation.getYaw();
            fakePlayer.rotationYaw = positionRotation.getYaw();
            fakePlayer.rotationPitch = positionRotation.getPitch();
        } else if (packet instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction action = (C0BPacketEntityAction) packet;
            if (action.getAction() == C0BPacketEntityAction.Action.START_SPRINTING) {
                fakePlayer.setSprinting(true);
            } else if (action.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING) {
                fakePlayer.setSprinting(false);
            } else if (action.getAction() == C0BPacketEntityAction.Action.START_SNEAKING) {
                fakePlayer.setSneaking(true);
            } else if (action.getAction() == C0BPacketEntityAction.Action.STOP_SNEAKING) {
                fakePlayer.setSneaking(false);
            }
        } else if (packet instanceof C0APacketAnimation) {
            fakePlayer.swingItem();
        }
    }
}