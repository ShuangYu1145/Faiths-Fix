package dev.faiths.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture;
import static org.lwjgl.opengl.GL11.*;

public class ImageUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();  // 1.8.9 获取 Minecraft 实例的方法
    private static final String IMAGE_CACHE_PREFIX = "url_image_";
    private static final ConcurrentHashMap<String, ResourceLocation> textureCache = new ConcurrentHashMap<>();
    private static String lastImageUrl = "";  // 存储上一个使用的图片 URL
    private static long lastUpdateTime = 0;   // 上次纹理更新的时间戳

    private static final long UPDATE_INTERVAL = 1000; // 设置一个1秒的间隔来避免频繁更新纹理

    /**
     * 绘制从URL加载的图片
     *
     * @param imageUrl 图片URL
     * @param x        绘制位置X坐标
     * @param y        绘制位置Y坐标
     * @param width    图片宽度
     * @param height   图片高度
     */
    public static void drawImageFromUrl(String imageUrl, float x, float y, float width, float height) {
        // 检查是否与上次URL不同，并且更新间隔已过
        if (imageUrl.equals(lastImageUrl) && System.currentTimeMillis() - lastUpdateTime < UPDATE_INTERVAL) {
            // 如果 URL 相同并且间隔时间不足，不更新纹理
            return;
        }

        ResourceLocation resourceLocation = textureCache.get(imageUrl);

        // 如果纹理已缓存，直接使用
        if (resourceLocation != null && mc.getTextureManager().getTexture(resourceLocation) != null) {
            renderImage(resourceLocation, x, y, width, height);
            return;
        }

        // 异步加载图片
        AtomicReference<ResourceLocation> refResourceLocation = new AtomicReference<>();
        new Thread(() -> {
            BufferedImage image = loadImageFromUrl(imageUrl);
            if (image != null) {
                // 图片加载完成后，在主线程中更新纹理
                mc.addScheduledTask(() -> {
                    // 将图片加载为动态纹理
                    DynamicTexture dynamicTexture = new DynamicTexture(image);
                    ResourceLocation newResourceLocation = new ResourceLocation(IMAGE_CACHE_PREFIX + imageUrl.hashCode());

                    // 更新 AtomicReference 中的 resourceLocation
                    refResourceLocation.set(newResourceLocation);

                    // 将加载的纹理缓存到 textureCache
                    textureCache.put(imageUrl, newResourceLocation);

                    mc.getTextureManager().loadTexture(newResourceLocation, dynamicTexture);
                    renderImage(newResourceLocation, x, y, width, height);

                    // 更新最后的 URL 和更新时间
                    lastImageUrl = imageUrl;
                    lastUpdateTime = System.currentTimeMillis();
                });
            }
        }).start();
    }

    /**
     * 从URL加载图片
     *
     * @param imageUrl 图片的URL地址
     * @return 加载的图片，或者null如果加载失败
     */
    private static BufferedImage loadImageFromUrl(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            return ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 渲染已加载的图片
     *
     * @param resourceLocation 纹理的资源位置
     * @param x                绘制位置X坐标
     * @param y                绘制位置Y坐标
     * @param width            图片宽度
     * @param height           图片高度
     */
    private static void renderImage(ResourceLocation resourceLocation, float x, float y, float width, float height) {
        final boolean depthTest = glIsEnabled(GL_DEPTH_TEST);
        final boolean blend = glIsEnabled(GL_BLEND);
        if (depthTest) glDisable(GL_DEPTH_TEST);
        if (!blend) glEnable(GL_BLEND);
        glDepthMask(false);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(1F, 1F, 1F, 1F);

        // 绑定并绘制纹理
        mc.getTextureManager().bindTexture(resourceLocation);
        drawModalRectWithCustomSizedTexture(x, y, 0F, 0F, width, height, width, height);

        glDepthMask(true);
        if (!blend) glDisable(GL_BLEND);
        if (depthTest) glEnable(GL_DEPTH_TEST);
    }
}
