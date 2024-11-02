package com.github.stachelbeere1248.zombiesutils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;

public class ResourceLoader {
    public static Optional<JsonElement> readJsonResource(final String resourcePath) {
        ResourceLocation resourceLocation = new ResourceLocation(resourcePath);
        try (Reader reader = new InputStreamReader(Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream())) {
            return Optional.ofNullable(new JsonParser().parse(reader));
        } catch (Exception e) {
            ZombiesUtils.getInstance().getLogger().error(e.fillInStackTrace());
            return Optional.empty();
        }
    }
}
