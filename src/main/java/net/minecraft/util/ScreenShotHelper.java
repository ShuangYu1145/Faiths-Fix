package net.minecraft.util;

import cn.hutool.core.swing.clipboard.ImageSelection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.shader.Framebuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static dev.faiths.utils.ClientUtils.LOGGER;

public class ScreenShotHelper
{
    private static final Logger logger = LogManager.getLogger();
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
    private static boolean screenshotFlag = false;

    /** A buffer to hold pixel values returned by OpenGL. */
    private static IntBuffer pixelBuffer;

    /**
     * The built-up array that contains all the pixel values returned by OpenGL.
     */
    private static int[] pixelValues;

    /**
     * Saves a screenshot in the game directory with a time-stamped filename.  Args: gameDirectory,
     * requestedWidthInPixels, requestedHeightInPixels, frameBuffer
     */
    public static void saveScreenshot(File gameDirectory, int width, int height, Framebuffer buffer)
    {
        saveScreenshot(gameDirectory, (String)null, width, height, buffer);
    }

    /**
     * Saves a screenshot in the game directory with the given file name (or null to generate a time-stamped name).
     * Args: gameDirectory, fileName, requestedWidthInPixels, requestedHeightInPixels, frameBuffer
     */
    public static void saveScreenshot(File gameDirectory, String screenshotName, int width, int height, Framebuffer buffer) {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null && mc.theWorld != null) {
                mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText( EnumChatFormatting.GRAY + "[" + EnumChatFormatting.DARK_PURPLE + "ScreenShot Helper" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.GOLD + "Taking Screenshot"));
            }
            final File file1 = new File(gameDirectory, "screenshots");
            file1.mkdir();

            if (OpenGlHelper.isFramebufferEnabled()) {
                width = buffer.framebufferTextureWidth;
                height = buffer.framebufferTextureHeight;
            }

            final int i = width * height;

            if (pixelBuffer == null || pixelBuffer.capacity() < i) {
                pixelBuffer = BufferUtils.createIntBuffer(i);
                pixelValues = new int[i];
            }

            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
            pixelBuffer.clear();

            if (OpenGlHelper.isFramebufferEnabled()) {
                GlStateManager.bindTexture(buffer.framebufferTexture);
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
            } else {
                GL11.glReadPixels(0, 0, buffer.framebufferWidth, buffer.framebufferHeight, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
            }

            new Thread("ss") {

                @Override
                public void run() {
                    pixelBuffer.get(pixelValues);
                    TextureUtil.processPixelValues(pixelValues, buffer.framebufferWidth, buffer.framebufferHeight);

                    BufferedImage bufferedimage = null;

                    if (OpenGlHelper.isFramebufferEnabled()) {
                        bufferedimage = new BufferedImage(buffer.framebufferWidth, buffer.framebufferHeight, 1);
                        final int j = buffer.framebufferTextureHeight - buffer.framebufferHeight;

                        for (int k = j; k < buffer.framebufferTextureHeight; ++k) {
                            for (int l = 0; l < buffer.framebufferWidth; ++l) {
                                bufferedimage.setRGB(l, k - j, pixelValues[k * buffer.framebufferTextureWidth + l]);
                            }
                        }
                    } else {
                        bufferedimage = new BufferedImage(buffer.framebufferWidth, buffer.framebufferHeight, 1);
                        bufferedimage.setRGB(0, 0, buffer.framebufferWidth, buffer.framebufferHeight, pixelValues, 0, buffer.framebufferWidth);
                    }


                    final File file2;

                    if (screenshotName == null) {
                        file2 = getTimestampedPNGFileForDirectory(file1);
                    } else {
                        file2 = new File(file1, screenshotName);
                    }

                    try {
                        ImageIO.write(bufferedimage, "png", file2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (mc.thePlayer != null && mc.theWorld != null) {
                        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.DARK_PURPLE + "ScreenShot Helper" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.GOLD + "Screenshot Taken"));
                    }

                    try {
                        ImageSelection imgSel = new ImageSelection(bufferedimage);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
                        mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.GRAY + "[" + EnumChatFormatting.DARK_PURPLE + "ScreenShot Helper" + EnumChatFormatting.GRAY + "] " + EnumChatFormatting.GOLD + "Copied."));
                    } catch (Exception ignored) {
                    }

                    super.run();
                }
            }.start();
        } catch (Exception exception) {
            LOGGER.warn("Couldn't save screenshot", exception);
        }
    }

    /**
     * Creates a unique PNG file in the given directory named by a timestamp.  Handles cases where the timestamp alone
     * is not enough to create a uniquely named file, though it still might suffer from an unlikely race condition where
     * the filename was unique when this method was called, but another process or thread created a file at the same
     * path immediately after this method returned.
     */
    private static File getTimestampedPNGFileForDirectory(File gameDirectory)
    {
        String s = dateFormat.format(new Date()).toString();
        int i = 1;

        while (true)
        {
            File file1 = new File(gameDirectory, s + (i == 1 ? "" : "_" + i) + ".png");

            if (!file1.exists())
            {
                return file1;
            }

            ++i;
        }
    }

    private static void resize(int p_resize_0_, int p_resize_1_)
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.displayWidth = Math.max(1, p_resize_0_);
        minecraft.displayHeight = Math.max(1, p_resize_1_);

        if (minecraft.currentScreen != null)
        {
            ScaledResolution scaledresolution = new ScaledResolution(minecraft);
            minecraft.currentScreen.onResize(minecraft, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight());
        }

        updateFramebufferSize();
    }

    private static void updateFramebufferSize()
    {
        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.getFramebuffer().createBindFramebuffer(minecraft.displayWidth, minecraft.displayHeight);

        if (minecraft.entityRenderer != null)
        {
            minecraft.entityRenderer.updateShaderGroupSize(minecraft.displayWidth, minecraft.displayHeight);
        }
    }

    public static void checkScreenshotFlag() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && screenshotFlag) {
            screenshotFlag = false;
            saveScreenshot(mc.mcDataDir, mc.displayWidth, mc.displayHeight, mc.framebufferMc);
        }
    }

    public static void safeSaveScreenshot() {
        screenshotFlag = true;
    }
}
