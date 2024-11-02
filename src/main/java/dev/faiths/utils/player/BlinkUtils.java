package dev.faiths.utils.player;

import dev.faiths.module.combat.ModuleGapple;
import dev.faiths.module.player.ModuleBlink;
import dev.faiths.utils.IMinecraft;
import dev.faiths.utils.PacketUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;

import java.util.concurrent.LinkedBlockingQueue;

public class BlinkUtils implements IMinecraft {
    private static boolean blinking = false;
    private static boolean cantSlowRelease = false;
    public static LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    public static boolean isBlinkPacket(Packet<?> packet) {
        return packet instanceof C03PacketPlayer ||
                packet instanceof C0FPacketConfirmTransaction ||
                packet instanceof C0APacketAnimation ||
                packet instanceof C08PacketPlayerBlockPlacement ||
                packet instanceof C02PacketUseEntity ||
                packet instanceof C09PacketHeldItemChange ||
                packet instanceof C0EPacketClickWindow ||
                packet instanceof C0DPacketCloseWindow ||
                packet instanceof C07PacketPlayerDigging ||
                packet instanceof C0BPacketEntityAction;
    }
    public static void startBlink() {
        packets.clear();
    }
    public static void stopBlink() {
        releaseAll();
    }
    public static boolean isPacketShouldDelay(Packet<?> packet) {
        if (!blinking) return false;
        if (isBlinkPacket(packet)) {
            packets.add(packet);
            if (packet instanceof C03PacketPlayer) {
                ModuleGapple.storedC03++;
            }
            return true;
        }
        return false;
    }
    public void releasePacketByAmount(int amount) {
        if (cantSlowRelease) {
            return;
        }
        for (int i = 0; i < amount; i++) {
            Packet<?> packet = packets.poll();
            PacketUtils.sendPacketNoEvent(packet);
            if (packet instanceof C03PacketPlayer) {
                // 由于我们不需要在后续代码中使用 c03 变量，所以不需要进行强制类型转换
                ModuleGapple.storedC03--;
            }
        }
    }
    public static void releaseC03(int amount) {
        if (cantSlowRelease) {
            return;
        }
        int i = 0;
        for (int j = 0; j < packets.size(); j++) {
            Packet<?> packet = packets.poll();
            PacketUtils.sendPacketNoEvent(packet);
            if (packet instanceof C03PacketPlayer) {
                ModuleGapple.storedC03--;
                i++;
            }
            if (i >= amount) {
                break;
            }
        }
    }
    public void releaseC03render(int amount) {
        if (cantSlowRelease) {
            return;
        }
        int i = 0;
        for (int j = 0; j < packets.size(); j++) {
            Packet<?> packet = packets.poll();
            PacketUtils.sendPacketNoEvent(packet);
            if (packet instanceof C03PacketPlayer) {
                C03PacketPlayer c03 = (C03PacketPlayer) packet;
                ModuleGapple.storedC03--;
                i++;
                ModuleGapple.storedC03--;
            }
            if (i >= amount) {
                break;
            }
        }
    }
    private static void releaseAll() {
        for (Packet<?> packet : packets) {
            PacketUtils.sendPacketNoEvent(packet);
        }
        packets.clear();
    }
}
