package dev.faiths.module.render;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.render.BlendUtils;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.utils.vector.Vector3d;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import javax.vecmath.Vector4d;
import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleESP2D extends CheatModule {
    public static List collectedEntities = new ArrayList();
    private final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private final FloatBuffer modelview = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer vector = GLAllocation.createDirectFloatBuffer(4);
    private final int backgroundColor = new Color(0, 0, 0, 120).getRGB();
    private final int black = Color.BLACK.getRGB();
    private final DecimalFormat dFormat = new DecimalFormat("0.0");

    public ModuleESP2D() {
        super("ESP2D", Category.RENDER);
    }

    private Handler<Render2DEvent> eventHandler = event -> {
        GL11.glPushMatrix();
        this.collectEntities();
        float partialTicks = event.getPartialTicks();
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int scaleFactor = scaledResolution.getScaleFactor();
        double scaling = (double)scaleFactor / Math.pow((double)scaleFactor, 2.0D);
        GL11.glScaled(scaling, scaling, scaling);
        int black = this.black;
        int background = this.backgroundColor;
        float scale = 0.65F;
        float upscale = 1.0F / scale;
        FontRenderer fr = mc.fontRendererObj;
        RenderManager renderMng = mc.getRenderManager();
        EntityRenderer entityRenderer = mc.entityRenderer;
        boolean outline = true;
        boolean health = true;
        boolean armor = true;
        int i = 0;

        for(int collectedEntitiesSize = collectedEntities.size(); i < collectedEntitiesSize; ++i) {
            Entity entity = (Entity)collectedEntities.get(i);
            int color = getColor(entity).getRGB();
            if (RenderUtils.isInViewFrustrum(entity)) {
                AxisAlignedBB aabb = getAxisAlignedBB(entity, partialTicks);
                List vectors = Arrays.asList(new Vector3d(aabb.minX, aabb.minY, aabb.minZ), new Vector3d(aabb.minX, aabb.maxY, aabb.minZ), new Vector3d(aabb.maxX, aabb.minY, aabb.minZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.minZ), new Vector3d(aabb.minX, aabb.minY, aabb.maxZ), new Vector3d(aabb.minX, aabb.maxY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.minY, aabb.maxZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ));
                entityRenderer.setupCameraTransform(partialTicks, 0);
                Vector4d position = null;
                Iterator var38 = vectors.iterator();

                while(var38.hasNext()) {
                    Vector3d vector = (Vector3d)var38.next();
                    vector = this.project2D(scaleFactor, vector.getX() - renderMng.viewerPosX, vector.getY() - renderMng.viewerPosY, vector.getZ() - renderMng.viewerPosZ);
                    if (vector != null && vector.getZ() >= 0.0D && vector.getZ() < 1.0D) {
                        if (position == null) {
                            position = new Vector4d(vector.getX(), vector.getY(), vector.getZ(), 0.0D);
                        }

                        position.x = Math.min(vector.getX(), position.x);
                        position.y = Math.min(vector.getY(), position.y);
                        position.z = Math.max(vector.getX(), position.z);
                        position.w = Math.max(vector.getY(), position.w);
                    }
                }

                if (position != null) {
                    entityRenderer.setupOverlayRendering();
                    double posX = position.x;
                    double posY = position.y;
                    double endPosX = position.z;
                    double endPosY = position.w;
                    if (outline) {
                        RenderUtils.newDrawRect(posX - 1.0D, posY, posX + 0.5D, endPosY + 0.5D, black);
                        RenderUtils.newDrawRect(posX - 1.0D, posY - 0.5D, endPosX + 0.5D, posY + 0.5D + 0.5D, black);
                        RenderUtils.newDrawRect(endPosX - 0.5D - 0.5D, posY, endPosX + 0.5D, endPosY + 0.5D, black);
                        RenderUtils.newDrawRect(posX - 1.0D, endPosY - 0.5D - 0.5D, endPosX + 0.5D, endPosY + 0.5D, black);
                        RenderUtils.newDrawRect(posX - 0.5D, posY, posX + 0.5D - 0.5D, endPosY, color);
                        RenderUtils.newDrawRect(posX, endPosY - 0.5D, endPosX, endPosY, color);
                        RenderUtils.newDrawRect(posX - 0.5D, posY, endPosX, posY + 0.5D, color);
                        RenderUtils.newDrawRect(endPosX - 0.5D, posY, endPosX, endPosY, color);
                    }

                    boolean living = entity instanceof EntityLivingBase;
                    boolean isPlayer = entity instanceof EntityPlayer;
                    EntityLivingBase entityLivingBase;
                    float armorValue;
                    float itemDurability;
                    double durabilityWidth;
                    double textWidth;
                    float tagY;
                    if (living) {
                        entityLivingBase = (EntityLivingBase)entity;
                        if (health) {
                            armorValue = entityLivingBase.getHealth();
                            itemDurability = entityLivingBase.getMaxHealth();
                            if (armorValue > itemDurability)
                                armorValue = itemDurability;

                            durabilityWidth = (double)(armorValue / itemDurability);
                            textWidth = (endPosY - posY) * durabilityWidth;
                            String healthDisplay = dFormat.format(entityLivingBase.getHealth()) + " §c❤";
                            String healthPercent = ((int) ((entityLivingBase.getHealth() / itemDurability) * 100F)) + "%";
                            if (entity == mc.thePlayer || isHovering(posX, endPosX, posY, endPosY, scaledResolution))
                                drawScaledString(healthPercent, posX - 4.0 - mc.fontRendererObj.getStringWidth(healthPercent), (endPosY - textWidth) - mc.fontRendererObj.FONT_HEIGHT / 2F, 1, -1);
                            RenderUtils.newDrawRect(posX - 3.5D, posY - 0.5D, posX - 1.5D, endPosY + 0.5D, background);
                            if (armorValue > 0.0F) {
                                int healthColor = BlendUtils.getHealthColor(armorValue, itemDurability).getRGB();
                                double deltaY = endPosY - posY;
                                RenderUtils.newDrawRect(posX - 3.0D, endPosY, posX - 2.0D, endPosY - textWidth, healthColor);
                                tagY = entityLivingBase.getAbsorptionAmount();
                                if (tagY > 0.0F)
                                    RenderUtils.newDrawRect(posX - 3.0D, endPosY, posX - 2.0D, endPosY - (endPosY - posY) / 6.0D * (double)tagY / 2.0D, (new Color(Potion.absorption.getLiquidColor())).getRGB());
                            }
                        }
                    }

                    if (armor) {
                        if (living) {
                            entityLivingBase = (EntityLivingBase)entity;
                            final double constHeight = (endPosY - posY) / 4.0;
                            for (int m = 4; m > 0; m--) {
                                ItemStack armorStack = entityLivingBase.getEquipmentInSlot(m);
                                double theHeight = constHeight + 0.25D;
                                if (armorStack != null && armorStack.getItem() != null) {
                                    RenderUtils.newDrawRect(endPosX + 1.5D, endPosY + 0.5D - theHeight * m, endPosX + 3.5D, endPosY + 0.5D - theHeight * (m - 1), background);
                                    RenderUtils.newDrawRect(endPosX + 2.0D,
                                            endPosY + 0.5D - theHeight * (m - 1) - 0.25D,
                                            endPosX + 3.0D,
                                            endPosY + 0.5D - theHeight * (m - 1) - 0.25D - (constHeight - 0.25D) * MathHelper.clamp_double((double)getItemDurability(armorStack) / (double) armorStack.getMaxDamage(), 0D, 1D), new Color(0, 255, 255).getRGB());
                                }
                            }
                        }
                    }

                    if (living && (entity == mc.thePlayer || isHovering(posX, endPosX, posY, endPosY, scaledResolution))) {
                        entityLivingBase = (EntityLivingBase) entity;
                        double yDist = (double)(endPosY - posY) / 4.0D;
                        for (int j = 4; j > 0; j--) {
                            ItemStack armorStack = entityLivingBase.getEquipmentInSlot(j);
                            if (armorStack != null && armorStack.getItem() != null) {
                                renderItemStack(armorStack, endPosX + (armor ? 4.0D : 2.0D), posY + (yDist * (4 - j)) + (yDist / 2.0D) - 5.0D);
                                drawScaledCenteredString(getItemDurability(armorStack) + "", endPosX + (armor ? 4.0D : 2.0D) + 4.5D, posY + (yDist * (4 - j)) + (yDist / 2.0D) + 4.0D, 1, -1);
                            }
                        }
                    }
                }
            }
        }

        GL11.glPopMatrix();
        GlStateManager.enableBlend();
        GlStateManager.resetColor();
        entityRenderer.setupOverlayRendering();
    };

    private AxisAlignedBB getAxisAlignedBB(Entity entity, float partialTicks) {
        double x = RenderUtils.interpolate(entity.lastTickPosX, entity.posX, partialTicks);
        double y = RenderUtils.interpolate(entity.lastTickPosY, entity.posY, partialTicks);
        double z = RenderUtils.interpolate(entity.lastTickPosZ, entity.posZ, partialTicks);
        double width = (double) entity.width / 1.5D;
        double height = (double) entity.height + (entity.isSneaking() ? -0.3D : 0.2D);
        AxisAlignedBB aabb = new AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width);
        return aabb;
    }

    private Color getColor(Entity entity) {
        return new Color(255, 255, 255);
    }

    private boolean isHovering(double minX, double maxX, double minY, double maxY, ScaledResolution sc) {
        return sc.getScaledWidth() / 2 >= minX && sc.getScaledWidth() / 2 < maxX && sc.getScaledHeight() / 2 >= minY && sc.getScaledHeight() / 2 < maxY;
    }

    private void drawScaledString(String text, double x, double y, double scale, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, x);
        GlStateManager.scale(scale, scale, scale);
        mc.fontRendererObj.drawStringWithShadow(text, 0, 0, color);
        GlStateManager.popMatrix();
    }

    private void drawScaledCenteredString(String text, double x, double y, double scale, int color) {
        drawScaledString(text, x - mc.fontRendererObj.getStringWidth(text) / 2F * scale, y, scale, color);
    }

    private void renderItemStack(ItemStack stack, double x, double y) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, x);
        GlStateManager.scale(0.5D, 0.5D, 0.5D);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void collectEntities() {
        collectedEntities.clear();
        List playerEntities = mc.theWorld.loadedEntityList;
        int i = 0;

        for(int playerEntitiesSize = playerEntities.size(); i < playerEntitiesSize; ++i) {
            Entity entity = (Entity)playerEntities.get(i);
            if (entity instanceof EntityPlayer) {
                if (entity instanceof EntityPlayerSP) {
                    if (mc.gameSettings.thirdPersonView != 0) {
                        collectedEntities.add(entity);
                    } else {
                        continue;
                    }
                }
                collectedEntities.add(entity);
            }
        }

    }

    private Vector3d project2D(int scaleFactor, double x, double y, double z) {
        GL11.glGetFloat(2982, this.modelview);
        GL11.glGetFloat(2983, this.projection);
        GL11.glGetInteger(2978, this.viewport);
        return GLU.gluProject((float)x, (float)y, (float)z, this.modelview, this.projection, this.viewport, this.vector) ? new Vector3d((double)(this.vector.get(0) / (float)scaleFactor), (double)(((float) Display.getHeight() - this.vector.get(1)) / (float)scaleFactor), (double)this.vector.get(2)) : null;
    }

    public static int getItemDurability(ItemStack stack) {
        return stack == null ? 0 : (stack.getMaxDamage() - stack.getItemDamage());
    }
}
