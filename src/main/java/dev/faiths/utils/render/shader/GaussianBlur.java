package dev.faiths.utils.render.shader;

import dev.faiths.utils.math.MathUtils;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.utils.render.StencilUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static dev.faiths.utils.IMinecraft.mc;

public class GaussianBlur {
    private static final ShaderUtil gaussianBlur = new ShaderUtil("xylitol/shader/gaussian.frag");
    private static Framebuffer framebuffer = new Framebuffer(1, 1, false);

    private static void setupUniforms(float dir1, float dir2, float radius) {
        gaussianBlur.setUniformi("textureIn", 0);
        gaussianBlur.setUniformf("texelSize", 1.0f / (float)mc.displayWidth, 1.0f / (float)mc.displayHeight);
        gaussianBlur.setUniformf("direction", dir1, dir2);
        gaussianBlur.setUniformf("radius", radius);
        FloatBuffer weightBuffer = BufferUtils.createFloatBuffer(256);
        int i = 0;
        while ((float)i <= radius) {
            weightBuffer.put(MathUtils.calculateGaussianValue(i, radius / 2.0f));
            ++i;
        }
        weightBuffer.rewind();
        OpenGlHelper.glUniform1(gaussianBlur.getUniform("weights"), weightBuffer);
    }

    public static void startBlur() {
        StencilUtil.write(false);
    }

    public static void endBlur(float radius, float compression) {
        StencilUtil.erase(true);
        framebuffer = ShaderElement.createFrameBuffer(framebuffer);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(false);
        gaussianBlur.init();
        GaussianBlur.setupUniforms(compression, 0.0f, radius);
        RenderUtils.bindTexture(mc.getFramebuffer().framebufferTexture);
        ShaderUtil.drawQuads();
        framebuffer.unbindFramebuffer();
        gaussianBlur.unload();
        mc.getFramebuffer().bindFramebuffer(false);
        gaussianBlur.init();
        GaussianBlur.setupUniforms(0.0f, compression, radius);
        RenderUtils.bindTexture(GaussianBlur.framebuffer.framebufferTexture);
        ShaderUtil.drawQuads();
        gaussianBlur.unload();
        StencilUtil.dispose();
        RenderUtils.resetColor();
        GlStateManager.bindTexture(0);
    }
}

