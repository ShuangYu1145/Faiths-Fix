package dev.faiths.utils.render;

import dev.faiths.Faiths;
import dev.faiths.module.combat.ModuleBackTrack;
import dev.faiths.module.render.ModuleHUD;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static dev.faiths.utils.IMinecraft.mc;
import static dev.faiths.utils.render.BlendUtils.blendColors;
import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;
import static net.minecraft.client.renderer.GlStateManager.*;
import static net.minecraft.client.renderer.RenderGlobal.drawSelectionBoundingBox;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.*;

public class RenderUtils {
    public static int rotateDirection = 0;
    static Tessellator tessellator = Tessellator.getInstance();
    private static final Map<Integer, Boolean> glCapMap = new HashMap<>();
    static WorldRenderer worldRenderer = tessellator.getWorldRenderer();
    private static final Frustum FRUSTUM = new Frustum();
    private static final FloatBuffer windowPosition = GLAllocation.createDirectFloatBuffer(4);
    private static final IntBuffer viewport = GLAllocation.createDirectIntBuffer(16);
    private static final FloatBuffer modelMatrix = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer projectionMatrix = GLAllocation.createDirectFloatBuffer(16);
    private static final float[] BUFFER = new float[3];
    private static final Frustum frustrum = new Frustum();
    public static int getColorFromPercentage(float percentage) {
        return Color.HSBtoRGB(Math.min(1.0F, Math.max(0.0F, percentage)) / 3, 0.9F, 0.9F);
    }
    public static int darker(int color) {
        return darker(color, 0.6F);
    }
    public static int darker(final int color, final float factor) {
        final int r = (int) ((color >> 16 & 0xFF) * factor);
        final int g = (int) ((color >> 8 & 0xFF) * factor);
        final int b = (int) ((color & 0xFF) * factor);
        final int a = color >> 24 & 0xFF;

        return ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF) |
                ((a & 0xFF) << 24);
    }
    public static void drawBlockBox(final BlockPos blockPos, final Color color, final boolean outline) {
        final RenderManager renderManager = mc.getRenderManager();
        final Timer timer = mc.timer;

        final double x = blockPos.getX() - renderManager.renderPosX;
        final double y = blockPos.getY() - renderManager.renderPosY;
        final double z = blockPos.getZ() - renderManager.renderPosZ;

        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
        final Block block = mc.theWorld.getBlockState(blockPos).getBlock();

        if (block != null) {
            final EntityPlayer player = mc.thePlayer;

            final double posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) timer.renderPartialTicks;
            final double posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) timer.renderPartialTicks;
            final double posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) timer.renderPartialTicks;
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                    .expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D)
                    .offset(-posX, -posY, -posZ);
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() != 255 ? color.getAlpha() : outline ? 26 : 35);
        drawFilledBox(axisAlignedBB);

        if (outline) {
            glLineWidth(1F);
            enableGlCap(GL_LINE_SMOOTH);
            glColor(color);
            drawSelectionBoundingBox(axisAlignedBB);
        }

        GlStateManager.resetColor();
        glDepthMask(true);
        resetCaps();
    }
    public static void resetCaps() {
        glCapMap.forEach(RenderUtils::setGlState);
    }
    public static void drawFilledBox(final AxisAlignedBB axisAlignedBB) {
        final Tessellator tessellator = Tessellator.getInstance();
        final WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        tessellator.draw();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex();
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex();
        tessellator.draw();
    }
    public static void disableGlCap(final int... caps) {
        for (final int cap : caps)
            setGlCap(cap, false);
    }
    public static void glColor(final Color color) {
        final float red = color.getRed() / 255F;
        final float green = color.getGreen() / 255F;
        final float blue = color.getBlue() / 255F;
        final float alpha = color.getAlpha() / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }
    public static void glColor(final int red, final int green, final int blue, final int alpha) {
        GlStateManager.color(red / 255F, green / 255F, blue / 255F, alpha / 255F);
    }
    public static void enableGlCap(final int cap) {
        setGlCap(cap, true);
    }
    public static void setGlState(final int cap, final boolean state) {
        if (state)
            glEnable(cap);
        else
            glDisable(cap);
    }
    public static void setGlCap(final int cap, final boolean state) {
        glCapMap.put(cap, glGetBoolean(cap));
        setGlState(cap, state);
    }


    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }

    public static void setAlphaLimit(float limit) {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL_GREATER, (float) (limit * .01));
    }

    public static void enableRender3D(boolean disableDepth) {
        if (disableDepth) {
            GL11.glDepthMask(false);
            GL11.glDisable(2929);
        }

        GL11.glDisable(3008);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glLineWidth(1.0F);
    }

    public static void disableRender3D(boolean enableDepth) {
        if (enableDepth) {
            GL11.glDepthMask(true);
            GL11.glEnable(2929);
        }

        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(3008);
        GL11.glDisable(2848);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
    public static void drawRect(final float x, final float y, final float width, final float height, final Color color) {
        final boolean texture2D = GL11.glIsEnabled(GL_TEXTURE_2D);
        final boolean blend = GL11.glIsEnabled(GL_BLEND);
        if (texture2D) glDisable(GL_TEXTURE_2D);
        if (!blend) glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
        GL11.glBegin(GL_QUADS);
        glVertex2f(x + width, y);
        glVertex2f(x, y);
        glVertex2f(x, y + height);
        glVertex2f(x + width, y + height);
        GL11.glEnd();
        glDisable(GL_LINE_SMOOTH);
        if (!blend) glDisable(GL_BLEND);
        if (texture2D) glEnable(GL_TEXTURE_2D);
        GlStateManager.resetColor();
    }
    public static boolean isBBInFrustum(AxisAlignedBB aabb) {
        EntityPlayerSP player = mc.thePlayer;
        FRUSTUM.setPosition(player.posX, player.posY, player.posZ);
        return FRUSTUM.isBoundingBoxInFrustum(aabb);
    }
    public static double interpolate(double old,
                                     double now,
                                     float partialTicks) {
        return old + (now - old) * partialTicks;
    }
    public static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");
    public static String stripColor(final String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }
    public static void drawOutlinedString(FontRenderer fontRenderer, String string, float x, float y, float width, int color, int outlineColor) {
        GlStateManager.pushMatrix();
        fontRenderer.drawString(RenderUtils.stripColor(string), x - width, y, outlineColor);
        fontRenderer.drawString(RenderUtils.stripColor(string), x, y - width, outlineColor);
        fontRenderer.drawString(RenderUtils.stripColor(string), x + width, y, outlineColor);
        fontRenderer.drawString(RenderUtils.stripColor(string), x, y + width, outlineColor);
        fontRenderer.drawString(string, x, y, color);
        GlStateManager.popMatrix();
    }
    public static void drawRectBordered(final double x, final double y, final double x1, final double y1, final double width, final int internalColor, final int borderColor) {
        rectangle(x + width, y + width, x1 - width, y1 - width, internalColor);
        rectangle(x + width, y, x1 - width, y + width, borderColor);
        rectangle(x, y, x + width, y1, borderColor);
        rectangle(x1 - width, y, x1, y1, borderColor);
        rectangle(x + width, y1 - width, x1 - width, y1, borderColor);
    }
    public static void rectangle(double left, double top, double right, double bottom, final int color) {
        if (left < right) {
            final double var5 = left;
            left = right;
            right = var5;
        }
        if (top < bottom) {
            final double var5 = top;
            top = bottom;
            bottom = var5;
        }
        final float var6 = (color >> 24 & 0xFF) / 255.0f;
        final float var7 = (color >> 16 & 0xFF) / 255.0f;
        final float var8 = (color >> 8 & 0xFF) / 255.0f;
        final float var9 = (color & 0xFF) / 255.0f;
        final WorldRenderer worldRenderer = Tessellator.getInstance().getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(var7, var8, var9, var6);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(left, bottom, 0.0).endVertex();
        worldRenderer.pos(right, bottom, 0.0).endVertex();
        worldRenderer.pos(right, top, 0.0).endVertex();
        worldRenderer.pos(left, top, 0.0).endVertex();
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }
    public static float[] project2D(float x,
                                    float y,
                                    float z,
                                    int scaleFactor) {
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelMatrix);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

        if (GLU.gluProject(x, y, z,
                modelMatrix, projectionMatrix, viewport, windowPosition)) {
            BUFFER[0] = windowPosition.get(0) / scaleFactor;
            BUFFER[1] = (Display.getHeight() - windowPosition.get(1)) / scaleFactor;
            BUFFER[2] = windowPosition.get(2);
            return BUFFER;
        }

        return null;
    }
    public static int getIntFromPercentage(float percentage) {
        return Color.HSBtoRGB(percentage / 3.0f, 1.0f, 1.0f);
    }
    public static void color(int color, float alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b, alpha);
    }
    public static void color(int color) {
        float f = (float) (color >> 24 & 255) / 255.0f;
        float f1 = (float) (color >> 16 & 255) / 255.0f;
        float f2 = (float) (color >> 8 & 255) / 255.0f;
        float f3 = (float) (color & 255) / 255.0f;
        GL11.glColor4f((float) f1, (float) f2, (float) f3, (float) f);
    }
    public static void drawWindow(float x, float y,float w,float h){
        drawBorderedRect(x,y,w,h,1,new Color(37, 38, 51).getRGB(),new Color(18,18,18,160).getRGB());
        drawRect(x - 0.5,y - 1,w + 1,2, ModuleHUD.color.getValue().getRGB());
        drawRect(x - 0.5,y,w + 1,1,new Color(0,0,0,100).getRGB());
    }

    public static void drawRect(double x, double y, double width, double height, int color) {
        setup2DRendering(() -> {
            glColor(color);
            worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION);
            worldRenderer.pos(x, y + height, 0.0D).endVertex();
            worldRenderer.pos(x + width, y + height, 0.0D).endVertex();
            worldRenderer.pos(x + width, y, 0.0D).endVertex();
            worldRenderer.pos(x, y, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.resetColor();
        });
    }
    public static void setup2DRendering(Runnable runnable) {
        enableBlend();
        blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        disableAlpha();
        disableTexture2D();
        runnable.run();
        enableTexture2D();
        enableAlpha();
        disableBlend();
    }
    public static void drawBorderedRect(double x, double y, double w, double h, float lineWidth, int col1, int col2) {
        drawRect((float) x, (float) y, (float) w, (float) h, col2);

        w += x;
        h += y;

        final float f = (col1 >> 24 & 0xFF) / 255.0F, // @off
                f1 = (col1 >> 16 & 0xFF) / 255.0F,
                f2 = (col1 >> 8 & 0xFF) / 255.0F,
                f3 = (col1 & 0xFF) / 255.0F; // @on

        glEnable(3042);
        glDisable(3553);
        GL11.glBlendFunc(770, 771);
        glEnable(2848);

        GL11.glPushMatrix();
        GL11.glColor4f(f1, f2, f3, f);
        GL11.glLineWidth(lineWidth);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, h);
        GL11.glVertex2d(w, h);
        GL11.glVertex2d(w, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(w, y);
        GL11.glVertex2d(x, h);
        GL11.glVertex2d(w, h);
        GL11.glEnd();
        enableTexture2D();
        disableBlend();
        GL11.glPopMatrix();
        GL11.glColor4f(255, 1, 1, 255);
        glEnable(3553);
        glDisable(3042);
        glDisable(2848);
    }


    public static void drawRectOriginal(final float x, final float y, final float x2, final float y2, final Color color) {
        final boolean texture2D = GL11.glIsEnabled(GL_TEXTURE_2D);
        final boolean blend = GL11.glIsEnabled(GL_BLEND);
        if (texture2D) glDisable(GL_TEXTURE_2D);
        if (!blend) glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_LINE_SMOOTH);
        glColor4f(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
        GL11.glBegin(GL_QUADS);
        glVertex2f(x2, y);
        glVertex2f(x, y);
        glVertex2f(x, y2);
        glVertex2f(x2, y2);
        GL11.glEnd();
        glDisable(GL_LINE_SMOOTH);
        if (!blend) glDisable(GL_BLEND);
        if (texture2D) glEnable(GL_TEXTURE_2D);
        GlStateManager.resetColor();
    }

    public static boolean isHovering(float x, float y, float width, float height, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y && mouseX <= x + width && mouseY <= y + height;
    }

    public static void drawOutLineRect(final float x, final float y, final float width, final float height, final float size, final Color internalColor, final Color borderColor) {
        drawRect(x, y, width, height, internalColor);

        drawRect(x, y, size, height, borderColor);

        drawRect(x, y - size, width + size, size, borderColor);

        drawRect(x + width, y, size, height, borderColor);

        drawRect(x, y + height - size, width, size, borderColor);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }

    public static void glColor(int hex) {
        float alpha = (hex >> 24 & 0xFF) / 255.0F;
        float red = (hex >> 16 & 0xFF) / 255.0F;
        float green = (hex >> 8 & 0xFF) / 255.0F;
        float blue = (hex & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
    }

    public static void drawHorizontalGradientSideways(double x, double y, double width, double height, int leftColor, int rightColor) {
        GLHelper.setup2DRendering(() -> {
            glShadeModel(GL_SMOOTH);
            GLHelper.setupRendering(GL_QUADS, () -> {
                glColor(leftColor);
                glVertex2d(x, y);
                glVertex2d(x, y + height);
                glColor(rightColor);
                glVertex2d(x + width, y + height);
                glVertex2d(x + width, y);
                GlStateManager.resetColor();
            });
            glShadeModel(GL_FLAT);
        });
    }

     public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float x1 = x + width, // @off
                y1 = y + height;
        final float f = (color >> 24 & 0xFF) / 255.0F,
                f1 = (color >> 16 & 0xFF) / 255.0F,
                f2 = (color >> 8 & 0xFF) / 255.0F,
                f3 = (color & 0xFF) / 255.0F; // @on
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);

        x *= 2;
        y *= 2;
        x1 *= 2;
        y1 *= 2;

        glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(f1, f2, f3, f);
        GlStateManager.enableBlend();
        glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glBegin(GL11.GL_POLYGON);
        final float v = (float) Math.PI / 180F;

        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x + radius + MathHelper.sin(i * v) * (radius * -1), y + radius + MathHelper.cos(i * v) * (radius * -1));
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + MathHelper.sin(i * v) * (radius * -1), y1 - radius + MathHelper.cos(i * v) * (radius * -1));
        }

        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x1 - radius + MathHelper.sin(i * v) * radius, y1 - radius + MathHelper.cos(i * v) * radius);
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x1 - radius + MathHelper.sin(i * v) * radius, y + radius + MathHelper.cos(i * v) * radius);
        }

        GL11.glEnd();

        glEnable(GL11.GL_TEXTURE_2D);
        glDisable(GL11.GL_LINE_SMOOTH);
        glEnable(GL11.GL_TEXTURE_2D);

        GL11.glScaled(2, 2, 2);

        GL11.glPopAttrib();
        GL11.glColor4f(1, 1, 1, 1);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int colorTop, int colorBottom) {
        float x1 = x + width, // @off
                y1 = y + height;
        final float fTop = (colorTop >> 24 & 0xFF) / 255.0F,
                f1Top = (colorTop >> 16 & 0xFF) / 255.0F,
                f2Top = (colorTop >> 8 & 0xFF) / 255.0F,
                f3Top = (colorTop & 0xFF) / 255.0F;
        final float fBottom = (colorBottom >> 24 & 0xFF) / 255.0F,
                f1Bottom = (colorBottom >> 16 & 0xFF) / 255.0F,
                f2Bottom = (colorBottom >> 8 & 0xFF) / 255.0F,
                f3Bottom = (colorBottom & 0xFF) / 255.0F; // @on
        GL11.glPushAttrib(0);
        GL11.glScaled(0.5, 0.5, 0.5);

        x *= 2;
        y *= 2;
        x1 *= 2;
        y1 *= 2;

        glDisable(GL11.GL_TEXTURE_2D);
        glEnable(GL11.GL_LINE_SMOOTH);

        // Draw top part with colorTop
        GL11.glColor4f(f1Top, f2Top, f3Top, fTop);
        GL11.glBegin(GL11.GL_POLYGON);
        drawTopLeftCorner(x, y, radius);
        drawTopRightCorner(x1, y, radius);
        GL11.glEnd();

        // Draw bottom part with colorBottom
        GL11.glColor4f(f1Bottom, f2Bottom, f3Bottom, fBottom);
        GL11.glBegin(GL11.GL_POLYGON);
        drawBottomLeftCorner(x, y1, radius);
        drawBottomRightCorner(x1, y1, radius);
        GL11.glEnd();

        glEnable(GL11.GL_TEXTURE_2D);
        glDisable(GL11.GL_LINE_SMOOTH);

        GL11.glScaled(2, 2, 2);

        GL11.glPopAttrib();
        GL11.glColor4f(1, 1, 1, 1);
    }

    private static void drawTopLeftCorner(float x, float y, float radius) {
        final float v = (float) Math.PI / 180F;
        for (int i = 90; i >= 0; i -= 3) {
            GL11.glVertex2d(x + radius + MathHelper.sin(i * v) * (radius * -1), y + radius + MathHelper.cos(i * v) * (radius * -1));
        }
    }

    private static void drawTopRightCorner(float x, float y, float radius) {
        final float v = (float) Math.PI / 180F;
        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x - radius + MathHelper.sin(i * v) * radius, y + radius + MathHelper.cos(i * v) * radius);
        }
    }

    private static void drawBottomLeftCorner(float x, float y, float radius) {
        final float v = (float) Math.PI / 180F;
        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + MathHelper.sin(i * v) * (radius * -1), y - radius + MathHelper.cos(i * v) * (radius * -1));
        }
    }

    private static void drawBottomRightCorner(float x, float y, float radius) {
        final float v = (float) Math.PI / 180F;
        for (int i = 180; i <= 270; i += 3) {
            GL11.glVertex2d(x - radius + MathHelper.sin(i * v) * radius, y - radius + MathHelper.cos(i * v) * radius);
        }
    }

    public static void drawVerticalGradientSideways(double x, double y, double width, double height, int topColor, int bottomColor) {
        GLHelper.setup2DRendering(() -> {
            glShadeModel(GL_SMOOTH);
            GLHelper.setupRendering(GL_QUADS, () -> {
                glColor(topColor);
                glVertex2d(x + width, y);
                glVertex2d(x, y);
                glColor(bottomColor);
                glVertex2d(x, y + height);
                glVertex2d(x + width, y + height);
                GlStateManager.resetColor();
            });
            glShadeModel(GL_FLAT);
        });
    }


    public static Color reAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static int reAlpha(int n, float f) {
        try {
            Color color = new Color(n);
            float f2 = 0.003921569f * (float)color.getRed();
            float f3 = 0.003921569f * (float)color.getGreen();
            float f4 = 0.003921569f * (float)color.getBlue();
            return new Color(f2, f3, f4, f).getRGB();
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
            return n;
        }
    }

    public static float getRotateDirection() {
        rotateDirection = rotateDirection + Faiths.delta;
        if (rotateDirection > 360)
            rotateDirection = 0;
        return rotateDirection;
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height) {
        final boolean depthTest = glIsEnabled(GL_DEPTH_TEST);
        final boolean blend = glIsEnabled(GL_BLEND);
        if (depthTest) glDisable(GL_DEPTH_TEST);
        if (!blend) glEnable(GL_BLEND);
        glDepthMask(false);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(1F, 1F, 1F, 1F);
        mc.getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture(x, y, 0F, 0F, width, height, width, height);
        glDepthMask(true);
        if (!blend) glDisable(GL_BLEND);
        if (depthTest) glEnable(GL_DEPTH_TEST);
    }

    public static void drawImage(ResourceLocation image, float x, float y, float width, float height, Color color) {
        final boolean depthTest = glIsEnabled(GL_DEPTH_TEST);
        final boolean blend = glIsEnabled(GL_BLEND);
        if (depthTest) glDisable(GL_DEPTH_TEST);
        if (!blend) glEnable(GL_BLEND);
        glDepthMask(false);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(color.getRed() / 255F,color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
        mc.getTextureManager().bindTexture(image);
        drawModalRectWithCustomSizedTexture(x, y, 0F, 0F, width, height, width, height);
        glDepthMask(true);
        if (!blend) glDisable(GL_BLEND);
        if (depthTest) glEnable(GL_DEPTH_TEST);
    }


    public static void makeScissorBox(final float x, final float y, final float x2, final float y2) {
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int factor = scaledResolution.getScaleFactor();
        glScissor(
                (int) (x * factor),
                (int) ((scaledResolution.getScaledHeight() - y2) * factor),
                (int) ((x2 - x) * factor),
                (int) ((y2 - y) * factor)
        );
    }

    public static boolean isInViewFrustrum(Entity entity) {
        return isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
    }

    private static boolean isInViewFrustrum(AxisAlignedBB bb) {
        Entity current = mc.getRenderViewEntity();
        frustrum.setPosition(current.posX, current.posY, current.posZ);
        return frustrum.isBoundingBoxInFrustum(bb);
    }

    public static void newDrawRect(float left, float top, float right, float bottom, int color)
    {
        if (left < right)
        {
            float i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            float j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos((double)left, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)bottom, 0.0D).endVertex();
        worldrenderer.pos((double)right, (double)top, 0.0D).endVertex();
        worldrenderer.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void newDrawRect(double left, double top, double right, double bottom, int color)
    {
        if (left < right)
        {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            double j = top;
            top = bottom;
            bottom = j;
        }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawEntityBox(final Entity entity, final Color color, final boolean backTrack) {
        final RenderManager renderManager = mc.getRenderManager();
        final Timer timer = mc.timer;

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        enableGlCap(GL_BLEND);
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST);
        glDepthMask(false);

        final double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX;
        final double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY;
        final double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ;

        AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        AxisAlignedBB axisAlignedBB = new AxisAlignedBB(
                entityBox.minX - entity.posX + x - 0.05D,
                entityBox.minY - entity.posY + y,
                entityBox.minZ - entity.posZ + z - 0.05D,
                entityBox.maxX - entity.posX + x + 0.05D,
                entityBox.maxY - entity.posY + y + 0.15D,
                entityBox.maxZ - entity.posZ + z + 0.05D
        );
        if (backTrack && entity instanceof EntityPlayer) {
            entityBox = ModuleBackTrack.getClosedBBox((EntityPlayer) entity);
            axisAlignedBB = new AxisAlignedBB(
                    entityBox.minX - renderManager.renderPosX - 0.05D,
                    entityBox.minY - renderManager.renderPosY,
                    entityBox.minZ - renderManager.renderPosZ - 0.05D,
                    entityBox.maxX - renderManager.renderPosX + 0.05D,
                    entityBox.maxY - renderManager.renderPosY + 0.15D,
                    entityBox.maxZ - renderManager.renderPosZ + 0.05D
            );
        }

        glColor(color.getRed(), color.getGreen(), color.getBlue(), 95);
        drawFilledBox(axisAlignedBB);
        GlStateManager.resetColor();
        glDepthMask(true);
        resetCaps();
    }

    public static int getColor(int n) {
        return getColor(n, n, n, 255);
    }

    public static int getColor(int n, int n2, int n3, int n4) {
        int n5 = MathHelper.clamp_int((int)n4, (int)0, (int)255) << 24;
        n5 |= MathHelper.clamp_int((int)n, (int)0, (int)255) << 16;
        n5 |= MathHelper.clamp_int((int)n2, (int)0, (int)255) << 8;
        return n5 |= MathHelper.clamp_int((int)n3, (int)0, (int)255);
    }

    public static Color getBlendColor(double d, double d2) {
        long l = Math.round(d2 / 5.0);
        if (d >= (double)(l * 5L)) {
            return new Color(15, 255, 15);
        }
        if (d >= (double)(l * 4L)) {
            return new Color(166, 255, 0);
        }
        if (d >= (double)(l * 3L)) {
            return new Color(255, 191, 0);
        }
        if (d >= (double)(l * 2L)) {
            return new Color(255, 89, 0);
        }
        return new Color(255, 0, 0);
    }

    public static void drawOutline(float x, float y, float x2, float y2, float radius, float line, float offset, Color c1, Color c2) {
        glEnable(3042);
        glDisable(2884);
        glDisable(3553);
        glEnable(2848);
        GL11.glShadeModel(7425);
        GL11.glBlendFunc(770, 771);
        GL11.glPushMatrix();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        float edgeRadius = radius;
        float centerX = x + edgeRadius;
        float centerY = y + edgeRadius;
        int vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);
        int i;
        int colorI = 0;
        double angleRadians;
        centerX = x2;
        centerY = y2 + edgeRadius;
        vertices = (int) Math.min(Math.max(edgeRadius, 10.0F), 90.0F);
        for (i = 0; i <= vertices; ++i) {
            RenderUtils.setColor(fadeBetween(c1.getRGB(), c2.getRGB(), (long) 20L * colorI));
            angleRadians = 6.283185307179586D * (double) (i) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
            colorI++;
        }

        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = x2 + edgeRadius;
        centerY = y2 + edgeRadius;
        for (i = 0; i <= (y2 - y); ++i) {
            RenderUtils.setColor(fadeBetween(c1.getRGB(), c2.getRGB(), (long) 20L * colorI));
            GL11.glVertex2d(centerX, centerY - i);
            colorI++;
        }
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = x2;
        centerY = (y) + edgeRadius;
        for (i = 0; i <= vertices; ++i) {
            RenderUtils.setColor(fadeBetween(c1.getRGB(), c2.getRGB(), (long) 20L * colorI));
            angleRadians = 6.283185307179586D * (double) (i + 90) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
            colorI++;
        }
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = x2;
        centerY = (y);
        for (i = 0; i <= (x2 - x); ++i) {
            RenderUtils.setColor(fadeBetween(c1.getRGB(), c2.getRGB(), (long) 20L * colorI));
            GL11.glVertex2d(centerX - i, centerY);
            colorI++;
        }
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = x;
        centerY = (y + edgeRadius);
        for (i = 0; i <= vertices; ++i) {
            RenderUtils.setColor(fadeBetween(c1.getRGB(), c2.getRGB(), (long) 20L * colorI));
            angleRadians = 6.283185307179586D * (double) (i + 180) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY + Math.cos(angleRadians) * (double) edgeRadius);
            colorI++;
        }
        colorI = 0;
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = x2;
        centerY = (y2 + vertices + offset);
        for (i = 0; i <= (x2 - x); ++i) {
            RenderUtils.setColor(fadeBetween(c1.getRGB(), c2.getRGB(), (long) 20L * colorI));
            GL11.glVertex2d(centerX - i, centerY);
            colorI++;
        }
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = x;
        centerY = (y2 + edgeRadius);
        for (i = 0; i <= vertices; ++i) {
            RenderUtils.setColor(fadeBetween(c1.getRGB(), c2.getRGB(), (long) 20L * colorI));
            angleRadians = 6.283185307179586D * (double) (i + 180) / (double) (vertices * 4);
            GL11.glVertex2d((double) centerX + Math.sin(angleRadians) * (double) edgeRadius, (double) centerY - Math.cos(angleRadians) * (double) edgeRadius);
            colorI++;
        }
        GL11.glEnd();
        GL11.glLineWidth(line);
        GL11.glBegin(3);
        centerX = x - edgeRadius;
        centerY = (y2 + edgeRadius);

        for (i = 0; i <= (y2 - y); ++i) {
            RenderUtils.setColor(fadeBetween(c1.getRGB(), c2.getRGB(), (long) 20L * colorI));
            GL11.glVertex2d(centerX, centerY - i);
            colorI++;
        }
        GL11.glEnd();
        GL11.glPopMatrix();
        glDisable(3042);
        glEnable(2884);
        glEnable(3553);
        glDisable(2848);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    public static void setColor(final int color) {
        final float a = ((color >> 24) & 0xFF) / 255.0f;
        final float r = ((color >> 16) & 0xFF) / 255.0f;
        final float g = ((color >> 8) & 0xFF) / 255.0f;
        final float b = (color & 0xFF) / 255.0f;
        GL11.glColor4f(r, g, b, a);
    }

    public static int fadeTo(int startColour, int endColour, double progress) {
        double invert = 1.0 - progress;
        int r = (int) ((startColour >> 16 & 0xFF) * invert +
                (endColour >> 16 & 0xFF) * progress);
        int g = (int) ((startColour >> 8 & 0xFF) * invert +
                (endColour >> 8 & 0xFF) * progress);
        int b = (int) ((startColour & 0xFF) * invert +
                (endColour & 0xFF) * progress);
        int a = (int) ((startColour >> 24 & 0xFF) * invert +
                (endColour >> 24 & 0xFF) * progress);
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8) |
                (b & 0xFF);
    }

    public static int fadeBetween(int startColour, int endColour, double progress) {
        if (progress > 1) progress = 1 - progress % 1;
        return fadeTo(startColour, endColour, progress);
    }

    public static int fadeBetween(int startColour, int endColour, long offset) {
        return fadeBetween(startColour, endColour, ((System.currentTimeMillis() + offset) % 2000L) / 1000.0);
    }

    public static void drawGradientRoundedRectH(int left, int top, int right, int bottom, int radius, int startColor, int endColor) {
        Stencil.write(false);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        fastRoundedRect(left, top, right, bottom, radius);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        Stencil.erase(true);
        drawGradientRectH(left, top, right, bottom, startColor, endColor);
        Stencil.dispose();
    }

    public static void fastRoundedRect(float paramXStart, float paramYStart, float paramXEnd, float paramYEnd, float radius) {
        float z;
        if (paramXStart > paramXEnd) {
            z = paramXStart;
            paramXStart = paramXEnd;
            paramXEnd = z;
        }

        if (paramYStart > paramYEnd) {
            z = paramYStart;
            paramYStart = paramYEnd;
            paramYEnd = z;
        }

        double x1 = (paramXStart + radius);
        double y1 = (paramYStart + radius);
        double x2 = (paramXEnd - radius);
        double y2 = (paramYEnd - radius);

        glEnable(GL_LINE_SMOOTH);
        glLineWidth(1);

        glBegin(GL_POLYGON);

        double degree = Math.PI / 180;
        for (double i = 0; i <= 90; i += 1)
            glVertex2d(x2 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius);
        for (double i = 90; i <= 180; i += 1)
            glVertex2d(x2 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius);
        for (double i = 180; i <= 270; i += 1)
            glVertex2d(x1 + Math.sin(i * degree) * radius, y1 + Math.cos(i * degree) * radius);
        for (double i = 270; i <= 360; i += 1)
            glVertex2d(x1 + Math.sin(i * degree) * radius, y2 + Math.cos(i * degree) * radius);
        glEnd();
        glDisable(GL_LINE_SMOOTH);
    }

    public static void drawGradientRectH(int left, int top, int right, int bottom, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, top, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, bottom, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(right, bottom, 0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(right, top, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        disableBlend();
        GlStateManager.enableAlpha();
        enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static Color getHealthColor(float health, float maxHealth) {
        float[] fractions = new float[]{0.0F, 0.5F, 1.0F};
        Color[] colors = new Color[]{new Color(108, 0, 0), new Color(255, 51, 0), Color.GREEN};
        float progress = health / maxHealth;
        return blendColors(fractions, colors, progress).brighter();
    }

}
