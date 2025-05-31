package dev.faiths.utils.sound;

import net.minecraft.util.ResourceLocation;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;

import static dev.faiths.utils.IMinecraft.mc;


public final class SoundUtil {

    public static void playSound(String sound) {
        SoundUtil.playSound(sound, 1.0f, 1.0f);
    }

    public static void playSound(String sound, float volume, float pitch) {
        mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, sound, volume, pitch, false);
    }

    private SoundUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void playSound(ResourceLocation location, float volume) {
        Multithreading.runAsync((() -> {
            try {
                BufferedInputStream bufferedInputStream = new BufferedInputStream(mc.getResourceManager().getResource(location).getInputStream());
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedInputStream);

                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);

                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);

                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }));
    }
}

