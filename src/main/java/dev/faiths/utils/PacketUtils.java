package dev.faiths.utils;

import dev.faiths.utils.math.MathUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;

import static dev.faiths.utils.IMinecraft.mc;

public final class PacketUtils {

    public static void sendPacket(Packet<?> packet, boolean silent) {
        if (mc.thePlayer != null) {
            mc.getNetHandler().getNetworkManager().sendPacket(packet, silent);
        }
    }
    public static void send(final Packet<?> packet) {
        mc.getNetHandler().addToSendQueue(packet);
    }
    public static void sendPacketC0F() {
        sendPacket((Packet<?>) new C0FPacketConfirmTransaction(MathUtils.getRandomInRange(102, 1000024123), (short) MathUtils.getRandomInRange(102, 1000024123), true));
    }
    public static void sendC0F(int windowId, short uid, boolean accepted, boolean silent) {
        if (silent) {
            sendNoEvent(new C0FPacketConfirmTransaction(windowId, uid, accepted));
        } else {
            send(new C0FPacketConfirmTransaction(windowId, uid, accepted));
        }

    }

    public static void sendPacketNoEvent(Packet packet) {
        sendPacket(packet, true);
    }
    public static void sendNoEvent(final Packet<?> packet) {
        mc.getNetHandler().addToSendQueue(packet);
    }

    public static void sendPacket(Packet packet) {
        sendPacket(packet, false);
    }

    public static boolean isPacketValid(final Packet packet) {
        return !(packet instanceof C00PacketLoginStart) && !(packet instanceof C00Handshake) && !(packet instanceof C00PacketServerQuery) && !(packet instanceof C01PacketPing);
    }
    public static boolean isCPacket(Packet<?> packet) {
        return packet.getClass().getSimpleName().startsWith("C");
    }

}
