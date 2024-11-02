package dev.faiths.module.render;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.event.impl.Render3DEvent;
import dev.faiths.event.impl.RenderNameTagEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.combat.ModuleAntiBot;
import dev.faiths.module.player.ModuleNotify;
import dev.faiths.module.world.ModuleTeams;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.utils.render.RoundedUtil;
import dev.faiths.value.ValueBoolean;
import ltd.guimc.silencefix.SilenceFixIRC;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static dev.faiths.module.render.ModuleHUD.color;
import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleNameTags extends CheatModule {
    public ValueBoolean distanceValue = new ValueBoolean("Distance", true);
    public ValueBoolean healthValue = new ValueBoolean("Health", true);
    private final DecimalFormat decimalFormat = new DecimalFormat("0.#", new DecimalFormatSymbols(Locale.ENGLISH));
    private final Map<EntityPlayer, float[]> entityPosMap = new HashMap<>();

    public ModuleNameTags() {
        super("NameTags", Category.RENDER);
    }

    private final Handler<RenderNameTagEvent> renderNameTagEventHandler = event -> {
        if (event.getEntityLivingBase() instanceof EntityPlayer) {
            event.setCancelled(true);
        }
    };

    private final Handler<Render3DEvent> render3DEventHandler = event -> {
        if (!this.entityPosMap.isEmpty()) {
            this.entityPosMap.clear();
        }
        for (final EntityPlayer player : mc.theWorld.playerEntities) {
            if ((player instanceof EntityOtherPlayerMP || mc.gameSettings.thirdPersonView != 0) && player.isEntityAlive()) {
                if (Faiths.moduleManager.getModule(ModuleAntiBot.class).isBot(player) || player == mc.thePlayer) {
                    continue;
                }
                GL11.glPushMatrix();
                final double posX = RenderUtils.interpolate(player.prevPosX, player.posX, event.getPartialTicks()) - RenderManager.viewerPosX;
                final double posY = RenderUtils.interpolate(player.prevPosY, player.posY, event.getPartialTicks()) - RenderManager.viewerPosY;
                final double posZ = RenderUtils.interpolate(player.prevPosZ, player.posZ, event.getPartialTicks()) - RenderManager.viewerPosZ;
                final double halfWidth = player.width / 2.0;
                final AxisAlignedBB axisAlignedBB = new AxisAlignedBB(posX - halfWidth, posY, posZ - halfWidth, posX + halfWidth, posY + player.height + (player.isSneaking() ? -0.2 : 0.1), posZ + halfWidth);
                final double[][] vectors = { { axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ }, { axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ }, { axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ }, { axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ }, { axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ }, { axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ }, { axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ }, { axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ } };
                final float[] position = { Float.MAX_VALUE, Float.MAX_VALUE, -1.0f, -1.0f };
                for (final double[] vec : vectors) {
                    final float[] projection = RenderUtils.project2D((float)vec[0], (float)vec[1], (float)vec[2], 2);
                    if (projection != null && projection[2] >= 0.0f && projection[2] < 1.0f) {
                        final float pX = projection[0];
                        final float pY = projection[1];
                        position[0] = Math.min(position[0], pX);
                        position[1] = Math.min(position[1], pY);
                        position[2] = Math.max(position[2], pX);
                        position[3] = Math.max(position[3], pY);
                    }
                }
                this.entityPosMap.put(player, position);
                GL11.glPopMatrix();
            }
        }
    };

    private final Handler<Render2DEvent> render2DEventHandler = event -> {
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        for (final EntityPlayer player : this.entityPosMap.keySet()) {
            final int[] counter = new int[1];
            float time = Minecraft.getSystemTime();
            final Color rainbow = Faiths.moduleManager.getModule(ModuleHUD.class).colorsetting.is("Custom") ? color.getValue()
                    : Faiths.moduleManager.getModule(ModuleHUD.class).colorsetting.is("Dynamic") ? new Color(Faiths.moduleManager.getModule(ModuleHUD.class).getArrayDynamic(time, 255))
                    : new Color(Faiths.moduleManager.getModule(ModuleHUD.class).astolfoRainbow(counter[0], 5, 107));

            if (player.getDistanceToEntity(mc.thePlayer) >= 1.0f || mc.gameSettings.thirdPersonView != 0) {
                if (!RenderUtils.isBBInFrustum(player.getEntityBoundingBox())) {
                    continue;
                }
                GL11.glPushMatrix();
                GL11.glScalef(2F / scaledResolution.getScaleFactor(), 2F / scaledResolution.getScaleFactor(), 2F / scaledResolution.getScaleFactor());
                final FontRenderer fontRenderer = mc.fontRendererObj;
                final boolean teamMate = ModuleTeams.isSameTeam(player);
//                final boolean friend = Client.INSTANCE.getFriendManager().isFriend(player);
//                final boolean target = Client.INSTANCE.getEnemyManager().isTarget(player);
                final boolean godAxe = ModuleNotify.isHeldGodAxe(player);
                final boolean kbBall = ModuleNotify.isHeldKBBall(player);
                final boolean gapple = ModuleNotify.isHeldEnchantedGApple(player);
                String tags = "";
                try {
                    if (SilenceFixIRC.Instance.ircUserMap.containsKey(player.getUniqueID())) {
                        tags += SilenceFixIRC.Instance.getUserTag(player.getUniqueID());
                    }
                } catch (Exception ignored) {}
                tags += (godAxe ? "§c(GodAxe)§r " : "") +
                        (kbBall ? "§c(KBBall)§r " : "") +
                        (gapple ? "§c(Enchanted GApple)§r " : "");
                
                final String distance = this.distanceValue.getValue() ? ("§a" + (int)mc.thePlayer.getDistanceToEntity(player) + "§am§r ") : "";
                final String playerName = player.getDisplayName().getFormattedText();
                final String name = tags + distance + ((teamMate ? "§a" : "")) + playerName;
                final String healthString = this.decimalFormat.format(player.getHealth() / 2.0f);
                final String healthAbsorption = " " + ((player.getAbsorptionAmount() <= 0.0f) ? "" : this.decimalFormat.format(player.getAbsorptionAmount() / 2.0f));
                final float[] positions = this.entityPosMap.get(player);
                final float x = positions[0];
                final float y = positions[1];
                final float x2 = positions[2];
                final float health = player.getHealth();
                final float maxHealth = player.getMaxHealth();
                final float healthPercentage = health / maxHealth;
                final float halfWidth = fontRenderer.getStringWidth(name) / 2.0f;
                final float xDif = x2 - x;
                final float middle = x + xDif / 2.0f;
                final float textHeight = fontRenderer.getHeight(name);
                float renderY = y - textHeight - 2.0f;
                final float left = middle - halfWidth - 2.0f;
                final float right = middle + halfWidth + 2.0f;
                final float healthWidth = fontRenderer.getStringWidth(healthString);
                final float healthAbsorptionWidth = fontRenderer.getStringWidth(healthAbsorption);
                final float healthBoxLeft = right;
                final int innerColor =  (RenderUtils.reAlpha(new Color(32, 32, 32), ModuleHUD.globalalpha.getValue()).getRGB());
                final int outerColor =  (RenderUtils.reAlpha(new Color(30, 30, 30), ModuleHUD.globalalpha.getValue()).getRGB());
                renderY -= 4.5f;
                RenderUtils.drawRectBordered(left, renderY - 2.5f, right, renderY + textHeight + 1.0f, 0.5, innerColor, outerColor);
          //      RoundedUtil.drawRoundOutline(left, renderY - 2.5f, textHeight, 13F, 4f,(float) 0,new Color(128,128,128,215),rainbow);
                mc.fontRendererObj.drawString(name, middle - halfWidth, renderY + 0.5f, -1);
                if (this.healthValue.getValue()) {
                    RenderUtils.drawRectBordered(healthBoxLeft, renderY - 2.5f, healthBoxLeft + healthWidth + healthAbsorptionWidth + ((player.getAbsorptionAmount() <= 0.0f) ? 0.5f : 3.0f), renderY + textHeight + 1f, 0.5, innerColor, outerColor);
                    mc.fontRendererObj.drawString(healthString, healthBoxLeft + 2.58f, renderY + 0.5f,  RenderUtils.getColorFromPercentage(health / player.getMaxHealth()));
                }
                GL11.glPopMatrix();
            }
        }
    };
}
