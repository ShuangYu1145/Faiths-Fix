package dev.faiths.utils.render.shader;

import dev.faiths.utils.render.GLUtil;
import dev.faiths.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static dev.faiths.utils.IMinecraft.mc;

public class KawaseBloom {
    public static ShaderUtil kawaseDown = new ShaderUtil("kawaseDownBloom");
    public static ShaderUtil kawaseUp = new ShaderUtil("kawaseUpBloom");
    public static Framebuffer framebuffer = new Framebuffer(1, 1, true);
    public static Framebuffer stencilFramebuffer = new Framebuffer(1, 1, false);
    private static int currentIterations;
    private static final List<Framebuffer> framebufferList;

    private static void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();
        framebuffer = ShaderElement.createFrameBuffer(null, true);
        framebufferList.add(framebuffer);
        int i = 1;
        while ((float)i <= iterations) {
            Framebuffer currentBuffer = new Framebuffer((int)((double)mc.displayWidth / Math.pow(2.0, i)), (int)((double)mc.displayHeight / Math.pow(2.0, i)), true);
            currentBuffer.setFramebufferFilter(9729);
            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(3553, 10242, 33648);
            GL11.glTexParameteri(3553, 10243, 33648);
            GlStateManager.bindTexture(0);
            framebufferList.add(currentBuffer);
            ++i;
        }
    }

    public static void shadow2(Runnable drawMod) {
        stencilFramebuffer = ShaderElement.createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(false);
        drawMod.run();
        stencilFramebuffer.unbindFramebuffer();
        KawaseBloom.renderBlur(KawaseBloom.stencilFramebuffer.framebufferTexture, 4, 4);
    }

    public static void shadow(Runnable drawMod) {
        stencilFramebuffer = ShaderElement.createFrameBuffer(stencilFramebuffer);
        stencilFramebuffer.framebufferClear();
        stencilFramebuffer.bindFramebuffer(false);
        drawMod.run();
        stencilFramebuffer.unbindFramebuffer();
        KawaseBloom.renderBlur(KawaseBloom.stencilFramebuffer.framebufferTexture, 2, 3);
    }

    public static void renderBlur(int framebufferTexture, int iterations, int offset) {
        int i;
        if (currentIterations != iterations || KawaseBloom.framebuffer.framebufferWidth != mc.displayWidth || KawaseBloom.framebuffer.framebufferHeight != mc.displayHeight) {
            KawaseBloom.initFramebuffers(iterations);
            currentIterations = iterations;
        }
        RenderUtils.setAlphaLimit(0.0f);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(1, 1);
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        KawaseBloom.renderFBO(framebufferList.get(1), framebufferTexture, kawaseDown, offset);
        for (i = 1; i < iterations; ++i) {
            KawaseBloom.renderFBO(framebufferList.get(i + 1), KawaseBloom.framebufferList.get(i).framebufferTexture, kawaseDown, offset);
        }
        for (i = iterations; i > 1; --i) {
            KawaseBloom.renderFBO(framebufferList.get(i - 1), KawaseBloom.framebufferList.get(i).framebufferTexture, kawaseUp, offset);
        }
        Framebuffer lastBuffer = framebufferList.get(0);
        lastBuffer.framebufferClear();
        lastBuffer.bindFramebuffer(false);
        kawaseUp.init();
        kawaseUp.setUniformf("offset", offset, offset);
        kawaseUp.setUniformi("inTexture", 0);
        kawaseUp.setUniformi("check", 1);
        kawaseUp.setUniformi("textureToCheck", 16);
        kawaseUp.setUniformf("halfpixel", 1.0f / (float)lastBuffer.framebufferWidth, 1.0f / (float)lastBuffer.framebufferHeight);
        kawaseUp.setUniformf("iResolution", lastBuffer.framebufferWidth, lastBuffer.framebufferHeight);
        GlStateManager.setActiveTexture(34000);
        RenderUtils.bindTexture(framebufferTexture);
        GlStateManager.setActiveTexture(33984);
        RenderUtils.bindTexture(KawaseBloom.framebufferList.get(1).framebufferTexture);
        ShaderUtil.drawQuads();
        kawaseUp.unload();
        GlStateManager.clearColor(0.0f, 0.0f, 0.0f, 0.0f);
        mc.getFramebuffer().bindFramebuffer(false);
        RenderUtils.bindTexture(KawaseBloom.framebufferList.get(0).framebufferTexture);
        RenderUtils.setAlphaLimit(0.0f);
        GLUtil.startBlend();
        ShaderUtil.drawQuads();
        GlStateManager.bindTexture(0);
        RenderUtils.setAlphaLimit(0.0f);
        GLUtil.endBlend();
        GlStateManager.disableBlend();
    }

    private static void renderFBO(Framebuffer framebuffer, int framebufferTexture, ShaderUtil shader, float offset) {
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        shader.init();
        RenderUtils.bindTexture(framebufferTexture);
        shader.setUniformf("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformi("check", 0);
        shader.setUniformf("halfpixel", 1.0f / (float)framebuffer.framebufferWidth, 1.0f / (float)framebuffer.framebufferHeight);
        shader.setUniformf("iResolution", framebuffer.framebufferWidth, framebuffer.framebufferHeight);
        ShaderUtil.drawQuads();
        shader.unload();
    }

    static {
        framebufferList = new ArrayList<Framebuffer>();
    }
}

