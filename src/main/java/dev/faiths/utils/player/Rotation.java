package dev.faiths.utils.player;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.RandomUtils;
import org.lwjgl.util.vector.Vector2f;
import java.util.concurrent.ThreadLocalRandom;

public class Rotation {
    float yaw;
    float pitch;
    public double distanceSq;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Vector2f toVec2f() {
        return new Vector2f(this.yaw, this.pitch);
    }

    public void toPlayer(EntityPlayer player) {
        if (Float.isNaN(yaw) || Float.isNaN(pitch)) return;
        fixedSensitivity(Minecraft.getMinecraft().gameSettings.mouseSensitivity);
        player.rotationYaw = yaw;
        player.rotationPitch = pitch;
    }

    public void fixedSensitivity(Float sensitivity) {
        float f = sensitivity * 0.6F + 0.2F;
        float gcd = f * f * f * 1.2F;
        yaw -= yaw % gcd;
        pitch -= pitch % gcd;
    }
    
    public void setYaw(final float yaw) {
        this.yaw = yaw;
    }

    
    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }

    
    public void setDistanceSq(final double distanceSq) {
        this.distanceSq = distanceSq;
    }

    
    public float getYaw() {
        return this.yaw;
    }

    
    public float getPitch() {
        return this.pitch;
    }

    
    public double getDistanceSq() {
        return this.distanceSq;
    }
    
}
