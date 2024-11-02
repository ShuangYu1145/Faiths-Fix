package dev.faiths.module.render;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.Render3DEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.event.impl.WorldEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.combat.ModuleKillAura;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.Pair;
import dev.faiths.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

import static dev.faiths.utils.IMinecraft.mc;
import static net.minecraft.entity.EntityList.getClassFromID;

public class ModuleFarmHunterESP extends CheatModule {
    public static ArrayList<Entity> entities = new ArrayList<>();
    public static ArrayList<Entity> mentities = new ArrayList<>();
    public static ArrayList<Pair<Long, BlockPos>> playerDespawnPos = new ArrayList<>();
    public static HashMap<Entity, Integer> offGroundTick = new HashMap<>();

    public ModuleFarmHunterESP() {
        super("FarmHunterESP", Category.RENDER);
    }

    @Override
    public void onEnable() {
        entities.clear();
        mentities.clear();
    }

    public final Handler<WorldEvent> worldEventHandler = event -> {
        entities.clear();
        mentities.clear();
    };

    public final Handler<UpdateEvent> updateEventHandler = event -> {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof IAnimals && !entities.contains(entity)) {
                if (entity.rotationPitch != 0.0) {
                    entities.add(entity);
                }
                if (!entity.onGround) {
                    if (!offGroundTick.containsKey(entity)) {
                        offGroundTick.put(entity, 1);
                    } else {
                        offGroundTick.put(entity, offGroundTick.get(entity) + 1);
                    }
                } else {
                    offGroundTick.put(entity, 0);
                }
            }
        }
        if (!Faiths.moduleManager.getModule(ModuleKillAura.class).getState()) {
            Faiths.INSTANCE.getRotationManager().setRotation(new Vector2f(mc.thePlayer.rotationYaw, 0), 180f, false, false);
        }
    };

    public final Handler<PacketEvent> packetEventHandler = event -> {
        Packet packet = event.getPacket();
        if (event.getType() == PacketEvent.Type.RECEIVE) {
            if (packet instanceof S13PacketDestroyEntities) {
                for (int entityID : ((S13PacketDestroyEntities) packet).getEntityIDs()) {
                    try {
                        Entity entity = mc.theWorld.getEntityByID(entityID);
                        if (entity instanceof EntityPlayer && mc.thePlayer.getDistanceSq(new BlockPos(entity.posX, entity.posY, entity.posZ)) <= 40) {
                            Faiths.notificationManager.pop("FarmHunterESP", "A player despawn.", NotificationType.INFO);
                            playerDespawnPos.add(new Pair<>(System.currentTimeMillis(), new BlockPos(entity.posX, entity.posY, entity.posZ)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (packet instanceof S0FPacketSpawnMob) {
                Class <? extends Entity > oclass = getClassFromID(((S0FPacketSpawnMob) packet).getEntityType());
                if (!(IAnimals.class.isAssignableFrom(oclass))) return;
                for (Pair<Long, BlockPos> posPair : playerDespawnPos) {
                    if (Math.abs(posPair.getKey() - System.currentTimeMillis()) > 500) continue;
                    double distance = posPair.getValue().distanceSqToCenter(((S0FPacketSpawnMob) packet).getX(), ((S0FPacketSpawnMob) packet).getY(), ((S0FPacketSpawnMob) packet).getZ());
                    if (distance <= 1) {
                        event.setCancelled(true);
                        packet.processPacket(mc.getNetHandler());
                        try {
                            mentities.add(mc.theWorld.getEntityByID(((S0FPacketSpawnMob) packet).getEntityID()));
                        } catch (Exception ignored) {}
                        Faiths.notificationManager.pop("FarmHunterESP", "Found a similar fake animal", NotificationType.INFO);
                    } else {
                        for (Entity entity : mc.theWorld.loadedEntityList) {
                            if (entity instanceof IAnimals && !entities.contains(entity) && !mentities.contains(entity) && entity.getDistanceSqToCenter(posPair.getValue()) <= 1) {
                                mentities.add(entity);
                                Faiths.notificationManager.pop("FarmHunterESP", "Found a similar fake animal", NotificationType.INFO);
                            }
                        }
                    }
                }
            } else if (packet instanceof S14PacketEntity) {
                Entity entity = ((S14PacketEntity) packet).getEntity(mc.theWorld);
                if (mentities.contains(entity) || mc.thePlayer.getDistanceToEntity(entity) >= 30 || !(entity instanceof IAnimals) || entity instanceof EntityPlayer) return;
                if (System.currentTimeMillis() - entity.lastSyncTime >= 100) {
                    return;
                }
                double bpt = Math.hypot(((S14PacketEntity) packet).getPosX() / 32D,
                        ((S14PacketEntity) packet).getPosZ() / 32D);
                if (bpt >= 0.45) {
                    mentities.add(entity);
                    Faiths.notificationManager.pop("FarmHunterESP", "A animal moves too quickly, bps=" + bpt * 20D, NotificationType.INFO);
                }
            }
        }
    };

    public final Handler<Render3DEvent> render3DEventHandler = event -> {
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!entities.contains(entity) && !mentities.contains(entity)) continue;
            RenderUtils.drawEntityBox(entity, (entities.contains(entity)?new Color(255, 43, 28):new Color(138, 56, 231, 255)), false);
            GlStateManager.disableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GL11.glEnable(2848);

            // Draw line
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glLineWidth(1.5f);
            GL11.glBegin(1);
            GL11.glVertex3d(0.0D, mc.thePlayer.getEyeHeight(), 0.0D);
            GL11.glVertex3d(entity.posX - mc.getRenderManager().renderPosX, entity.posY - mc.getRenderManager().renderPosY, entity.posZ - mc.getRenderManager().renderPosZ);
            GL11.glEnd();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            // Draw line end

            GL11.glDisable(2848);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.enableAlpha();
        }
    };
}
