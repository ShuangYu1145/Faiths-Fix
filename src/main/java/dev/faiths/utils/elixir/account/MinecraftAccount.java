package dev.faiths.utils.elixir.account;

import dev.faiths.utils.ClientUtils;
import dev.faiths.utils.elixir.compat.Session;
import dev.faiths.utils.elixir.exception.LoginException;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.Packet;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static dev.faiths.utils.IMinecraft.mc;

public abstract class MinecraftAccount {
    private final String type;
    private ResourceLocation headResource;
    private BufferedImage headImage;
    private static final ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
    public MinecraftAccount(String type) {
        this.type = type;
    }

    public ResourceLocation getHeadResource() {
        return headResource;
    }

    public BufferedImage getHeadImage() {
        return headImage;
    }

    public void setHeadResource(ResourceLocation headResource) {
        this.headResource = headResource;
    }

    protected void loadHeadResource(final String name) {
        if (getHeadResource() == null) {
            threadPool.execute(() -> {
                try {
                    headImage = ImageIO.read(new URL(String.format("https://minotar.net/avatar/%s", name)));
                } catch (IOException e) {
                    ClientUtils.LOGGER.error(e);
                }
            });
        }
    }

    public final String getType() {
        return this.type;
    }

    public abstract String getName();

    public abstract void setName(final String name);

    public abstract Session getSession();

    public abstract void update() throws LoginException, IOException;

    public abstract void toRawYML(final Map<String, String> data);

    public abstract void fromRawYML(final Map<String, String> data);
}

