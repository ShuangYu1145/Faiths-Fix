package dev.faiths.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;
import net.optifine.util.TextureUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

public class UrlImage {
    private ResourceLocation resourceLocation;
    private BufferedImage bufferedImage;
    private boolean textureLoaded = false;

    private String currentUrl;
    private String lastLoadedUrl = "";

    private double x, y, width, height;
    private final Type type;

    public UrlImage(String url, double x, double y, double width, double height, Type type) {
        this.type = type;
        setBounds(x, y, width, height);
        setUrl(url);
    }

    public void setBounds(double x, double y, double width, double height) {
        this.x = x; this.y = y;
        this.width = width; this.height = height;
    }

    public void setUrl(String url) {
        if (url == null || url.equals(this.currentUrl)) return;
        this.currentUrl = url;
        loadImageAsync(url);
    }

    private void loadImageAsync(String url) {
        CompletableFuture.runAsync(() -> {
            try (InputStream stream = new URL(url).openStream()) {
                BufferedImage img = ImageIO.read(stream);
                if (img != null) {
                    this.bufferedImage = img;
                    this.textureLoaded = false;
                    System.out.println("[UrlImage] Image loaded from URL: " + url);
                }
            } catch (Exception e) {
                System.err.println("[UrlImage] Failed to load image from: " + url);
                e.printStackTrace();
            }
        });
    }

    private void ensureTextureLoaded() {
        if (this.textureLoaded || this.bufferedImage == null || currentUrl.equals(lastLoadedUrl)) return;

        lastLoadedUrl = currentUrl;

        Minecraft.getMinecraft().addScheduledTask(() -> {
            try {
                DynamicTexture dynamicTexture = new DynamicTexture(this.bufferedImage);
                this.resourceLocation = new ResourceLocation("urlimage", "tex_" + System.nanoTime());
                Minecraft.getMinecraft().getTextureManager().loadTexture(this.resourceLocation, dynamicTexture);
                this.textureLoaded = true;
                System.out.println("[UrlImage] Texture loaded to GPU.");
            } catch (Exception e) {
                System.err.println("[UrlImage] Failed to create texture from BufferedImage");
                e.printStackTrace();
            }
        });
    }

    public void draw() {
        ensureTextureLoaded();
        if (!textureLoaded || resourceLocation == null) return;

        if (type == Type.Normal) {
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }

        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);

        ITextureObject texture = Minecraft.getMinecraft().getTextureManager().getTexture(resourceLocation);
        if (texture != null) {
            TextureUtils.bindTexture(texture.getGlTextureId());
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        }

        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
    }

    public enum Type {
        NoColor, Normal
    }
}
