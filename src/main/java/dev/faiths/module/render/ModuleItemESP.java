package dev.faiths.module.render;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.Render3DEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import net.minecraft.entity.*;
import net.minecraft.entity.item.*;
import net.minecraft.util.*;
import org.lwjgl.opengl.*;

import java.util.regex.Pattern;

import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.gui.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.enchantment.*;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleItemESP extends CheatModule
{
    public ModuleItemESP() {
        super("ItemESP", Category.RENDER);
    }

    private final Handler<Render3DEvent> renderHandler = event -> {
        if (mc.thePlayer != null && mc.theWorld != null) {
            for (final Object o : mc.theWorld.getLoadedEntityList()) {
                final Entity entity = (Entity)o;
                final RenderManager renderManager = mc.getRenderManager();
                final double posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - renderManager.renderPosX;
                final double posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - renderManager.renderPosY;
                final double posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - renderManager.renderPosZ;
                if (entity instanceof EntityItem) {
                    String enhancement = "";
                    String sharpness = "";

                    if(EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, ((EntityItem)entity).getEntityItem()) != 0){
                        enhancement = EnumChatFormatting.AQUA + " Protection:" + EnumChatFormatting.RED + EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, ((EntityItem)entity).getEntityItem());
                    }

                    if(EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, ((EntityItem)entity).getEntityItem()) != 0){
                        enhancement = EnumChatFormatting.AQUA + " Sharpness:" + EnumChatFormatting.RED + EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, ((EntityItem)entity).getEntityItem());
                    }

                    if(((EntityItem)entity).getEntityItem().getItem() == Items.golden_apple){
                        if(((EntityItem)entity).getEntityItem().getItem().hasEffect(((EntityItem)entity).getEntityItem())) {
                            enhancement = EnumChatFormatting.RED + " Enchanted";
                        }
                    }

                    final String var3 = (((EntityItem)entity).getEntityItem().stackSize > 1) ? (EnumChatFormatting.RESET + " x" + ((EntityItem)entity).getEntityItem().stackSize) : "";
                    if (!this.canRenderer(((EntityItem)entity).getEntityItem())) {
                        continue;
                    }
                    GL11.glEnable(32823);
                    GL11.glPolygonOffset(1.0f, -1100000.0f);
                    this.renderLivingLabel(entity, ((EntityItem) entity).getEntityItem().getDisplayName() + var3 + enhancement, posX, posY, posZ, 160);
                    GL11.glDisable(32823);
                    GL11.glPolygonOffset(1.0f, 1100000.0f);
                }
            }
        }
    };

    protected void renderLivingLabel(final Entity entityIn, final String strin, final double x, final double y, final double z, final int maxDistance) {
        final double d0 = entityIn.getDistanceSqToEntity(mc.thePlayer);
        if (d0 <= maxDistance * maxDistance) {
            final FontRenderer fontrenderer = mc.fontRendererObj;
            float var12 = mc.thePlayer.getDistanceToEntity(entityIn) / 6.0f;
            if (var12 < 1.1f) {
                var12 = 1.1f;
            }
            float var13 = (float)(var12 * 1.1);
            var13 /= 100.0f;
            GlStateManager.pushMatrix();
            RenderHelper.enableStandardItemLighting();
            GlStateManager.translate((float)x + 0.0f, (float)y + entityIn.height + 0.5f, (float)z);
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
            if (mc.gameSettings.thirdPersonView == 2) {
                GL11.glRotatef(mc.getRenderManager().playerViewX, -1.0f, 0.0f, 0.0f);
            }
            else {
                GL11.glRotatef(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
            }
            GlStateManager.scale(-var13, -var13, var13);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            final Tessellator tessellator = Tessellator.getInstance();
            tessellator.getWorldRenderer();
            final byte b0 = 0;
            final int n = fontrenderer.getStringWidth(strin) / 2;
            GlStateManager.disableTexture2D();
            GlStateManager.enableTexture2D();
            new ScaledResolution(mc);
            drawOutlinedString(strin, -fontrenderer.getStringWidth(strin) / 2, b0, -1);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GL11.glScaled(0.6000000238418579, 0.6000000238418579, 0.6000000238418579);
            GL11.glScaled(1.0, 1.0, 1.0);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            RenderHelper.disableStandardItemLighting();
            GL11.glScaled(1.5, 1.5, 1.5);
            GlStateManager.popMatrix();
            GL11.glPolygonOffset(1.0f, 1000000.0f);
            GL11.glDisable(32823);
        }
    }

    public void drawOutlinedString(String str, float x, float y, int internalCol) {
        mc.fontRendererObj.drawString(stripColorCodes(str), x - 0.5f, y, 0x000000, false);
        mc.fontRendererObj.drawString(stripColorCodes(str), x + 0.5f, y, 0x000000, false);
        mc.fontRendererObj.drawString(stripColorCodes(str), x, y - 0.5f, 0x000000, false);
        mc.fontRendererObj.drawString(stripColorCodes(str), x, y + 0.5f, 0x000000, false);
        mc.fontRendererObj.drawString(str, x, y, internalCol, false);
    }

    public String stripColorCodes(String input) {
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    private final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-ORX]");

    public boolean canRenderer(final ItemStack item) {
        final Item item2 = item.getItem();
        final int var3 = Item.getIdFromItem(item2);
        if (!(item.getItem() instanceof ItemAppleGold) && !(item.getItem() instanceof ItemPotion) && !(item.getItem() instanceof ItemBucketMilk)) {
            item.getItem();
            if (var3 != Item.getIdFromItem(Items.diamond)) {
                item.getItem();
                if (var3 != Item.getIdFromItem(Items.diamond_sword)) {
                    item.getItem();
                    if (var3 != Item.getIdFromItem(Items.diamond_boots)) {
                        item.getItem();
                        if (var3 != Item.getIdFromItem(Items.diamond_helmet)) {
                            item.getItem();
                            if (var3 != Item.getIdFromItem(Items.diamond_chestplate)) {
                                item.getItem();
                                if (var3 != Item.getIdFromItem(Items.diamond_leggings)) {
                                    if (item.getItem() instanceof ItemArmor) {
                                        item.getItem();
                                        if (var3 == Item.getIdFromItem(Items.iron_leggings) && EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, item) != 0) {
                                            return true;
                                        }
                                    }
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}
