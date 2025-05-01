package dev.faiths.utils.render.shader;

import dev.faiths.utils.render.GLUtil;
import dev.faiths.utils.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;
import java.util.List;

import static dev.faiths.utils.IMinecraft.mc;

public class ShaderElement {
    public static ShaderUtil kawaseDown = new ShaderUtil("kawaseDown");
    public static ShaderUtil kawaseUp = new ShaderUtil("kawaseUp");
    private static final Tessellator tessellator = Tessellator.getInstance();
    private static final WorldRenderer worldrenderer = tessellator.getWorldRenderer();
    public static Framebuffer framebuffer = new Framebuffer(1, 1, false);
    private static final List<Framebuffer> framebufferList = new ArrayList<Framebuffer>();
    private static int currentIterations;
    private static final ArrayList<Runnable> tasks;
    private static final ArrayList<Runnable> bloomTasks;

    public static ArrayList<Runnable> getTasks() {
        return tasks;
    }

    public static void addBlurTask(Runnable context) {
        tasks.add(context);
    }

    public static ArrayList<Runnable> getBloomTasks() {
        return bloomTasks;
    }

    public static void addBloomTask(Runnable context) {
        bloomTasks.add(context);
    }

    public static void setupUniforms(float offset) {
        kawaseDown.setUniformf("offset", offset, offset);
        kawaseUp.setUniformf("offset", offset, offset);
    }

    public static void drawRect(double x, double y, double width, double height, int color) {
        GLUtil.setup2DRendering(() -> {
            RenderHelper.glColor(color);
            worldrenderer.begin(7, DefaultVertexFormats.POSITION);
            worldrenderer.pos(x, y + height, 0.0).endVertex();
            worldrenderer.pos(x + width, y + height, 0.0).endVertex();
            worldrenderer.pos(x + width, y, 0.0).endVertex();
            worldrenderer.pos(x, y, 0.0).endVertex();
            tessellator.draw();
            GlStateManager.resetColor();
        });
    }

    private static void initFramebuffers(float iterations) {
        for (Framebuffer framebuffer : framebufferList) {
            framebuffer.deleteFramebuffer();
        }
        framebufferList.clear();
        framebuffer = ShaderElement.createFrameBuffer(null);
        framebufferList.add(framebuffer);
        int i = 1;
        while ((float)i <= iterations) {
            Framebuffer currentBuffer = new Framebuffer((int)((double)mc.displayWidth / Math.pow(2.0, i)), (int)((double)mc.displayHeight / Math.pow(2.0, i)), false);
            currentBuffer.setFramebufferFilter(9729);
            GlStateManager.bindTexture(currentBuffer.framebufferTexture);
            GL11.glTexParameteri(3553, 10242, 33648);
            GL11.glTexParameteri(3553, 10243, 33648);
            GlStateManager.bindTexture(0);
            framebufferList.add(currentBuffer);
            ++i;
        }
    }

    public static void renderBlur(int stencilFrameBufferTexture, int iterations, int offset) {
        int i;
        if (currentIterations != iterations || ShaderElement.framebuffer.framebufferWidth != mc.displayWidth || ShaderElement.framebuffer.framebufferHeight != mc.displayHeight) {
            ShaderElement.initFramebuffers(iterations);
            currentIterations = iterations;
        }
        ShaderElement.renderFBO(framebufferList.get(1), mc.getFramebuffer().framebufferTexture, kawaseDown, offset);
        for (i = 1; i < iterations; ++i) {
            ShaderElement.renderFBO(framebufferList.get(i + 1), ShaderElement.framebufferList.get(i).framebufferTexture, kawaseDown, offset);
        }
        for (i = iterations; i > 1; --i) {
            ShaderElement.renderFBO(framebufferList.get(i - 1), ShaderElement.framebufferList.get(i).framebufferTexture, kawaseUp, offset);
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
        GL13.glActiveTexture(34000);
        RenderUtils.bindTexture(stencilFrameBufferTexture);
        GL13.glActiveTexture(33984);
        RenderUtils.bindTexture(ShaderElement.framebufferList.get(1).framebufferTexture);
        ShaderUtil.drawQuads();
        kawaseUp.unload();
        mc.getFramebuffer().bindFramebuffer(true);
        ShaderElement.bindTexture(ShaderElement.framebufferList.get(0).framebufferTexture);
        RenderUtils.setAlphaLimit(0.0f);
        GLUtil.startBlend();
        ShaderUtil.drawQuads();
        GlStateManager.bindTexture(0);
    }

    private static void renderFBO(Framebuffer framebuffer, int framebufferTexture, ShaderUtil shader, float offset) {
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        shader.init();
        ShaderElement.bindTexture(framebufferTexture);
        shader.setUniformf("offset", offset, offset);
        shader.setUniformi("inTexture", 0);
        shader.setUniformi("check", 0);
        shader.setUniformf("halfpixel", 1.0f / (float)framebuffer.framebufferWidth, 1.0f / (float)framebuffer.framebufferHeight);
        shader.setUniformf("iResolution", framebuffer.framebufferWidth, framebuffer.framebufferHeight);
        ShaderUtil.drawQuads();
        shader.unload();
    }

    public static void bindTexture(int texture) {
        GL11.glBindTexture(3553, texture);
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer) {
        return ShaderElement.createFrameBuffer(framebuffer, false);
    }

    public static boolean needsNewFramebuffer(Framebuffer framebuffer) {
        return framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight;
    }

    public static Framebuffer createFrameBuffer(Framebuffer framebuffer, boolean depth) {
        if (ShaderElement.needsNewFramebuffer(framebuffer)) {
            if (framebuffer != null) {
                framebuffer.deleteFramebuffer();
            }
            return new Framebuffer(mc.displayWidth, mc.displayHeight, depth);
        }
        return framebuffer;
    }

    public static void blurArea(double x, double y, double v, double v1) {
        ShaderElement.addBlurTask(() -> ShaderElement.drawRect(x, y, v, v1, -1));
    }

    static {
        tasks = new ArrayList();
        bloomTasks = new ArrayList();
    }
}

