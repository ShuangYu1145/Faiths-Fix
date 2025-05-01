package dev.faiths.utils.render;

import dev.faiths.utils.render.shader.ShaderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

import static dev.faiths.utils.IMinecraft.mc;

public class GradientUtil {
    private static final ShaderUtil gradientMaskShader = new ShaderUtil("gradientMask");
    private static final ShaderUtil gradientShader = new ShaderUtil("gradient");

    public static void drawGradient(float x2, float y2, float width, float height, float alpha, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {
        ScaledResolution sr = new ScaledResolution(mc);
        RenderUtils.setAlphaLimit(0.0f);
        RenderUtils.resetColor();
        GLUtil.startBlend();
        gradientShader.init();
        gradientShader.setUniformf("location", x2 * (float)sr.getScaleFactor(), (float)Minecraft.getMinecraft().displayHeight - height * (float)sr.getScaleFactor() - y2 * (float)sr.getScaleFactor());
        gradientShader.setUniformf("rectSize", width * (float)sr.getScaleFactor(), height * (float)sr.getScaleFactor());
        gradientShader.setUniformf("color1", (float)bottomLeft.getRed() / 255.0f, (float)bottomLeft.getGreen() / 255.0f, (float)bottomLeft.getBlue() / 255.0f, alpha);
        gradientShader.setUniformf("color2", (float)topLeft.getRed() / 255.0f, (float)topLeft.getGreen() / 255.0f, (float)topLeft.getBlue() / 255.0f, alpha);
        gradientShader.setUniformf("color3", (float)bottomRight.getRed() / 255.0f, (float)bottomRight.getGreen() / 255.0f, (float)bottomRight.getBlue() / 255.0f, alpha);
        gradientShader.setUniformf("color4", (float)topRight.getRed() / 255.0f, (float)topRight.getGreen() / 255.0f, (float)topRight.getBlue() / 255.0f, alpha);
        ShaderUtil.drawQuads(x2, y2, width, height);
        gradientShader.unload();
        GLUtil.endBlend();
    }

    public static void drawGradient(float x2, float y2, float width, float height, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight) {
        ScaledResolution sr = new ScaledResolution(mc);
        RenderUtils.resetColor();
        GLUtil.startBlend();
        gradientShader.init();
        gradientShader.setUniformf("location", x2 * (float)sr.getScaleFactor(), (float)Minecraft.getMinecraft().displayHeight - height * (float)sr.getScaleFactor() - y2 * (float)sr.getScaleFactor());
        gradientShader.setUniformf("rectSize", width * (float)sr.getScaleFactor(), height * (float)sr.getScaleFactor());
        gradientShader.setUniformf("color1", (float)bottomLeft.getRed() / 255.0f, (float)bottomLeft.getGreen() / 255.0f, (float)bottomLeft.getBlue() / 255.0f, (float)bottomLeft.getAlpha() / 255.0f);
        gradientShader.setUniformf("color2", (float)topLeft.getRed() / 255.0f, (float)topLeft.getGreen() / 255.0f, (float)topLeft.getBlue() / 255.0f, (float)topLeft.getAlpha() / 255.0f);
        gradientShader.setUniformf("color3", (float)bottomRight.getRed() / 255.0f, (float)bottomRight.getGreen() / 255.0f, (float)bottomRight.getBlue() / 255.0f, (float)bottomRight.getAlpha() / 255.0f);
        gradientShader.setUniformf("color4", (float)topRight.getRed() / 255.0f, (float)topRight.getGreen() / 255.0f, (float)topRight.getBlue() / 255.0f, (float)topRight.getAlpha() / 255.0f);
        ShaderUtil.drawQuads(x2, y2, width, height);
        gradientShader.unload();
        GLUtil.endBlend();
    }

    public static void drawGradientLR(float x2, float y2, float width, float height, float alpha, Color left, Color right) {
        GradientUtil.drawGradient(x2, y2, width, height, alpha, left, left, right, right);
    }

    public static void drawGradientTB(float x2, float y2, float width, float height, float alpha, Color top, Color bottom) {
        GradientUtil.drawGradient(x2, y2, width, height, alpha, bottom, top, bottom, top);
    }

    public static void applyGradientHorizontal(float x2, float y2, float width, float height, float alpha, Color left, Color right, Runnable content) {
        GradientUtil.applyGradient(x2, y2, width, height, alpha, left, left, right, right, content);
    }

    public static void applyGradientVertical(float x2, float y2, float width, float height, float alpha, Color top, Color bottom, Runnable content) {
        GradientUtil.applyGradient(x2, y2, width, height, alpha, bottom, top, bottom, top, content);
    }

    public static void applyGradientCornerRL(float x2, float y2, float width, float height, float alpha, Color bottomLeft, Color topRight, Runnable content) {
        Color mixedColor = ColorUtil.interpolateColorC(topRight, bottomLeft, 0.5f);
        GradientUtil.applyGradient(x2, y2, width, height, alpha, bottomLeft, mixedColor, mixedColor, topRight, content);
    }

    public static void applyGradientCornerLR(float x2, float y2, float width, float height, float alpha, Color bottomRight, Color topLeft, Runnable content) {
        Color mixedColor = ColorUtil.interpolateColorC(bottomRight, topLeft, 0.5f);
        GradientUtil.applyGradient(x2, y2, width, height, alpha, mixedColor, topLeft, bottomRight, mixedColor, content);
    }

    public static void applyGradient(float x2, float y2, float width, float height, float alpha, Color bottomLeft, Color topLeft, Color bottomRight, Color topRight, Runnable content) {
        RenderUtils.resetColor();
        GLUtil.startBlend();
        gradientMaskShader.init();
        ScaledResolution sr = new ScaledResolution(mc);
        gradientMaskShader.setUniformf("location", x2 * (float)sr.getScaleFactor(), (float)Minecraft.getMinecraft().displayHeight - height * (float)sr.getScaleFactor() - y2 * (float)sr.getScaleFactor());
        gradientMaskShader.setUniformf("rectSize", width * (float)sr.getScaleFactor(), height * (float)sr.getScaleFactor());
        gradientMaskShader.setUniformf("alpha", alpha);
        gradientMaskShader.setUniformi("tex", 0);
        gradientMaskShader.setUniformf("color1", (float)bottomLeft.getRed() / 255.0f, (float)bottomLeft.getGreen() / 255.0f, (float)bottomLeft.getBlue() / 255.0f);
        gradientMaskShader.setUniformf("color2", (float)topLeft.getRed() / 255.0f, (float)topLeft.getGreen() / 255.0f, (float)topLeft.getBlue() / 255.0f);
        gradientMaskShader.setUniformf("color3", (float)bottomRight.getRed() / 255.0f, (float)bottomRight.getGreen() / 255.0f, (float)bottomRight.getBlue() / 255.0f);
        gradientMaskShader.setUniformf("color4", (float)topRight.getRed() / 255.0f, (float)topRight.getGreen() / 255.0f, (float)topRight.getBlue() / 255.0f);
        content.run();
        gradientMaskShader.unload();
        GLUtil.endBlend();
    }
}

