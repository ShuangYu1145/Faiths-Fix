package dev.faiths.utils.render;

import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class BlurUtil {
    private static Minecraft mc = Minecraft.getMinecraft();
    private static ShaderGroup shaderGroup;
    private static Framebuffer frbuffer;
    private static Framebuffer framebuffer;
    private static double lastFactor;
    private static double lastWidth;
    private static double lastHeight;
    private static double lastX;
    private static double lastY;
    private static double lastW;
    private static double lastH;
    private static double lastStrength;
    private static ResourceLocation blurShader;

    public static void init() {
        try {
            shaderGroup = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), blurShader);
            shaderGroup.createBindFramebuffers(BlurUtil.mc.displayWidth, BlurUtil.mc.displayHeight);
            framebuffer = BlurUtil.shaderGroup.mainFramebuffer;
            frbuffer = shaderGroup.getFramebufferRaw("result");
        }
        catch (JsonSyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void setValues(double strength, double x2, double y2, double w2, double h, double width, double height) {
        if (strength == lastStrength && lastX == x2 && lastY == y2 && lastW == w2 && lastH == h) {
            return;
        }
        lastStrength = strength;
        lastX = x2;
        lastY = y2;
        lastW = w2;
        lastH = h;
        for (int i = 0; i < 2; ++i) {
            shaderGroup.getShaders().get(i).getShaderManager().getShaderUniform("Radius").set((float)strength);
            shaderGroup.getShaders().get(i).getShaderManager().getShaderUniform("BlurXY").set((float)x2, (float)(height - y2 - h));
            shaderGroup.getShaders().get(i).getShaderManager().getShaderUniform("BlurCoord").set((float)w2, (float)h);
        }
    }

    public static void blurArea(float x2, float y2, float x22, float y22, float blurStrength) {
        int height;
        int width;
        ScaledResolution scaledResolution;
        int scaleFactor;
        float z;
        if (!OpenGlHelper.isFramebufferEnabled()) {
            return;
        }
        if (x2 > x22) {
            z = x2;
            x2 = x22;
            x22 = z;
        }
        if (y2 > y22) {
            z = y2;
            y2 = y22;
            y22 = z;
        }
        if (BlurUtil.sizeHasChanged(scaleFactor = (scaledResolution = new ScaledResolution(mc)).getScaleFactor(), width = scaledResolution.getScaledWidth(), height = scaledResolution.getScaledHeight()) || framebuffer == null || frbuffer == null || shaderGroup == null) {
            BlurUtil.init();
        }
        lastFactor = scaleFactor;
        lastWidth = width;
        lastHeight = height;
        float _w = x22 - x2;
        float _h = y22 - y2;
        BlurUtil.setValues(blurStrength, x2, y2, _w, _h, width, height);
        framebuffer.bindFramebuffer(true);
        shaderGroup.loadShaderGroup(BlurUtil.mc.timer.renderPartialTicks);
        mc.getFramebuffer().bindFramebuffer(true);
        StencilUtil.write(false);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderUtils.quickDrawRect(x2, y2, x22, y22);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        StencilUtil.erase(true);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.pushMatrix();
        GlStateManager.colorMask(true, true, true, false);
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        frbuffer.bindFramebufferTexture();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        float f = width;
        float f1 = height;
        float f2 = (float)BlurUtil.frbuffer.framebufferWidth / (float)BlurUtil.frbuffer.framebufferTextureWidth;
        float f3 = (float)BlurUtil.frbuffer.framebufferHeight / (float)BlurUtil.frbuffer.framebufferTextureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0, f1, 0.0).tex(0.0, 0.0).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(f, f1, 0.0).tex(f2, 0.0).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(f, 0.0, 0.0).tex(f2, f3).color(255, 255, 255, 255).endVertex();
        worldrenderer.pos(0.0, 0.0, 0.0).tex(0.0, f3).color(255, 255, 255, 255).endVertex();
        tessellator.draw();
        frbuffer.unbindFramebufferTexture();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        StencilUtil.dispose();
        GlStateManager.enableAlpha();
    }
    public static void blurArea(float x2, float y2, float x22, float y22, float blurStrength, float cornerRadius) {
        int height;
        int width;
        ScaledResolution scaledResolution;
        int scaleFactor;
        float z;
        if (!OpenGlHelper.isFramebufferEnabled()) {
            return;
        }
        if (x2 > x22) {
            z = x2;
            x2 = x22;
            x22 = z;
        }
        if (y2 > y22) {
            z = y2;
            y2 = y22;
            y22 = z;
        }
        if (BlurUtil.sizeHasChanged(scaleFactor = (scaledResolution = new ScaledResolution(mc)).getScaleFactor(), width = scaledResolution.getScaledWidth(), height = scaledResolution.getScaledHeight()) || framebuffer == null || frbuffer == null || shaderGroup == null) {
            BlurUtil.init();
        }
        lastFactor = scaleFactor;
        lastWidth = width;
        lastHeight = height;
        float _w = x22 - x2;
        float _h = y22 - y2;
        BlurUtil.setValues(blurStrength, x2, y2, _w, _h, width, height);
        framebuffer.bindFramebuffer(true);
        shaderGroup.loadShaderGroup(BlurUtil.mc.timer.renderPartialTicks);
        mc.getFramebuffer().bindFramebuffer(true);
        StencilUtil.write(false);
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        // 绘制圆角矩形
        float x = x2;
        float y = y2;
        float w = x22 - x2;
        float h = y22 - y2;
        GL11.glBegin(GL11.GL_POLYGON);
        int numSegments = 30; // 圆角的线段数，可根据需要调整
        for (int i = 0; i < numSegments; i++) {
            float theta = 2.0f * 3.1415926f * (float) i / numSegments;
            float x1 = x + w - cornerRadius + cornerRadius * (1.0f - (float) Math.cos(theta));
            float y1 = y + h - cornerRadius + cornerRadius * (float) Math.sin(theta);
            GL11.glVertex2f(x1, y1);
        }
        GL11.glEnd();

        // 继续绘制矩形的四个边
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x + cornerRadius, y);
        GL11.glVertex2f(x + w - cornerRadius, y);
        GL11.glVertex2f(x + w, y + cornerRadius);
        GL11.glVertex2f(x + w, y + h - cornerRadius);
        GL11.glVertex2f(x + w - cornerRadius, y + h);
        GL11.glVertex2f(x + cornerRadius, y + h);
        GL11.glVertex2f(x, y + h - cornerRadius);
        GL11.glVertex2f(x, y + cornerRadius);
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        StencilUtil.erase(true);
        GlStateManager.enableBlend();
    }
    private static boolean sizeHasChanged(int scaleFactor, int width, int height) {
        return lastFactor != (double)scaleFactor || lastWidth != (double)width || lastHeight != (double)height;
    }

    static {
        lastStrength = 5.0;
        blurShader = new ResourceLocation("client/shaders/blur.json");
    }
}

