package dev.faiths.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

public class GlowUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final HashMap<Integer, Integer> shadowCache = new HashMap<>();

    public static void drawGlow(float x, float y, float width, float height, int blurRadius, Color color) {
        if (!mc.gameSettings.ofFastRender) {
            glPushMatrix();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.01f);
            width = width + blurRadius * 2;
            height = height + blurRadius * 2;
            x = x - blurRadius;
            y = y - blurRadius;

            float _X = x - 0.25f;
            float _Y = y + 0.25f;

            int identifier = (int) (width * height + width + color.hashCode() * blurRadius + blurRadius);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GlStateManager.enableBlend();

            int texId = -1;
            if (shadowCache.containsKey(identifier)) {
                texId = shadowCache.get(identifier);

                GlStateManager.bindTexture(texId);
            } else {
                if (width <= 0) width = 1;
                if (height <= 0) height = 1;

                BufferedImage original = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB_PRE);

                Graphics g = original.getGraphics();
                g.setColor(color);
                g.fillRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2));
                g.dispose();

                GaussianFilter op = new GaussianFilter(blurRadius);

                BufferedImage blurred = op.filter(original, null);


                texId = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), blurred, true, false);

                shadowCache.put(identifier, texId);
            }

            GL11.glColor4f(1f, 1f, 1f, 1f);

            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 0); // top left
            GL11.glVertex2f(_X, _Y);

            GL11.glTexCoord2f(0, 1); // bottom left
            GL11.glVertex2f(_X, _Y + height);

            GL11.glTexCoord2f(1, 1); // bottom right
            GL11.glVertex2f(_X + width, _Y + height);

            GL11.glTexCoord2f(1, 0); // top right
            GL11.glVertex2f(_X + width, _Y);
            GL11.glEnd();

            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.resetColor();

            glEnable(GL_CULL_FACE);
            glPopMatrix();
        }
    }
    public static void drawGlow(float x, float y, float width, float height, final int blurRadius, final Color color, final Runnable cutMethod) {
        GL11.glPushMatrix();
        GlStateManager.alphaFunc(516, 0.01f);
        width += blurRadius * 2;
        height += blurRadius * 2;
        x -= blurRadius;
        y -= blurRadius;
        final float _X = x - 0.25f;
        final float _Y = y + 0.25f;
        final int identifier = (int)(width * height + width + color.hashCode() * blurRadius + blurRadius);
        StencilUtil.write(false);
        cutMethod.run();
        StencilUtil.erase(false);
        GL11.glEnable(3553);
        GL11.glDisable(2884);
        GL11.glEnable(3008);
        GlStateManager.enableBlend();
        int texId = -1;
        if (GlowUtils.shadowCache.containsKey(identifier)) {
            texId = GlowUtils.shadowCache.get(identifier);
            GlStateManager.bindTexture(texId);
        }
        else {
            if (width <= 0.0f) {
                width = 1.0f;
            }
            if (height <= 0.0f) {
                height = 1.0f;
            }
            final BufferedImage original = new BufferedImage((int)width, (int)height, 3);
            final Graphics g = original.getGraphics();
            g.setColor(color);
            g.fillRect(blurRadius, blurRadius, (int)(width - blurRadius * 2), (int)(height - blurRadius * 2));
            g.dispose();
            final GaussianFilter op = new GaussianFilter((float)blurRadius);
            final BufferedImage blurred = op.filter(original, null);
            texId = TextureUtil.uploadTextureImageAllocate(TextureUtil.glGenTextures(), blurred, true, false);
            GlowUtils.shadowCache.put(identifier, texId);
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glBegin(7);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2f(_X, _Y);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2f(_X, _Y + height);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2f(_X + width, _Y + height);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2f(_X + width, _Y);
        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
        StencilUtil.dispose();
        GL11.glEnable(2884);
        GL11.glPopMatrix();
    }
}
