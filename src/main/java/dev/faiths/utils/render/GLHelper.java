package dev.faiths.utils.render;

import org.lwjgl.opengl.GL11;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;

public class GLHelper {
	
    public static void setupRendering(int mode, Runnable runnable) {
        glBegin(mode);
        runnable.run();
        glEnd();
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

    public static void setup3DRendering(boolean disableDepth, Runnable runnable) {
        enableBlend();
        blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        disableTexture2D();
        if (disableDepth)
            disableDepth();
        runnable.run();
        if (disableDepth)
            enableDepth();
        enableTexture2D();
        disableBlend();
    }

    public static void setupScale(double x, double y, double scale, Runnable runnable) {
        pushMatrix();
        translate(x, y, 0);
        scale(scale, scale, 1);
        translate(-x, -y, 0);
        runnable.run();
        popMatrix();
    }

    public static void setupRotate(double x, double y, float angle, Runnable runnable) {
        pushMatrix();
        translate(x, y, 0);
        rotate(angle, 0, 0, -1);
        translate(-x, -y, 0);
        runnable.run();
        popMatrix();
    }

    public static void setupTranslate(double x, double y, double z, Runnable runnable) {
        pushMatrix();
        translate(x, y, z);
        runnable.run();
        popMatrix();
    }
	
}