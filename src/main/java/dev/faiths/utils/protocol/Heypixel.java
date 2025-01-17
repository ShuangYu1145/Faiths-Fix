package dev.faiths.utils.protocol;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

/**
 * @Author KuChaZi
 * @Date 2024/8/3 8:52
 * @ClassName: Heypixel
 */
public class Heypixel {
    private final static String SEPARATOR_OF_MAC = ":";

    public static void sendMinecraftRegister() {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeString(" pocketmoder:core heypixel:armourers_workshop bungeequeue:queue fml:loginwrapper forge:tier_sorting floodgate:custom floodgate:packet heypixel:s2cevent report:areport bungeecord:main heypixel:game_info fml:play floodgate:netease floodgate:transfer gameteam:redisteam armourers_workshop:play fml:handshake legacy:redisbungee geckolib3:main forge:split floodgate:form floodgate:skin ");
        sendCustomPayload("REGISTER", buf);
    }

    public static void sendForgeBrand() {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeString("forge");
        sendCustomPayload("MC|Brand", buf);
    }

    public static void sendKeepAlive() {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        UUID uuid = UUID.randomUUID(); // REPLACE IT
        for (char a : uuid.toString().toCharArray()) {
            buf.writeChar(a);
        }
        // 我不知道后面是什么。

        // 增加时间戳
        buf.writeBytes(getUnixTimeHeypixel());
        sendCustomPayload(HeypixelConstants.HEYPIXEL_CHECK, buf);
    }

    public static void sendJoinHeypixelCheck() {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());

        buf.writeBytes(textToByteArrayUTF8(UUID.randomUUID().toString()));
        buf.writeBytes(textToByteArrayUTF8(UUID.randomUUID().toString()));
        // 写string
        buf.writeString("[minecraft, entityculling, armourers_workshop, immediatelyfast, culllessleaves, heypixel, nochatlagforge, memoryleakfix, reeses_sodium_options, forge, rubidium, embeddiumplus, iceberg, geckolib3]");
        // 中间有个未知的
        // 游戏路径
        buf.writeString("C:\\MCLDownload\\Game\\.minecraft");
        buf.writeString("C:\\MCLDownload\\ext\\jre-v64-220420\\jdk17\\bin\\java.exe");
        // 不知道
        buf.writeString("9BFEBFBFF000806E9");
        // CPU型号
        buf.writeString("Intel(R) Core(TM) i9-14900K CPU @ 4.8GHz");
        // idn
        buf.writeString("L1HF7990133");
        // 硬盘
        buf.writeString("MG28381782722");
        // 网卡mac
        buf.writeString(randomMac4Qemu());
        // a
        buf.writeChar('a');
        // 电脑制造商
        buf.writeString("Apple");
        // b
        buf.writeChar('b');
        // unknown
        buf.writeString("unknown");
        // c
        buf.writeChar('c');
        // idn
        buf.writeString("L1HF7990133");
        // d
        buf.writeChar('d');
        // COMPUTER
        buf.writeString("SDK0K09938 WIN");
        // a
        buf.writeChar('a');
        // 硬盘
        buf.writeString("MG28381782722");
        // b
        buf.writeChar('b');
        // local disk
        buf.writeString("\\\\.\\PHYSICALDRIVE0");
        // b
        buf.writeChar('c');
        // disk
        buf.writeString("MG BG400M8 SSD 512GB");

        sendCustomPayload(HeypixelConstants.HEYPIXEL_CHECK, buf);
    }

    public static void sendFinalCheck() {
    }

    private static void sendCustomPayload(String channel, PacketBuffer buf) {
        C17PacketCustomPayload packet = new C17PacketCustomPayload(channel, buf);
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet);
    }

    public static String randomMac4Qemu() {
        Random random = new Random();
        String[] mac = {
                String.format("%02x", 0x52),
                String.format("%02x", 0x54),
                String.format("%02x", 0x00),
                String.format("%02x", random.nextInt(0xff)),
                String.format("%02x", random.nextInt(0xff)),
                String.format("%02x", random.nextInt(0xff))
        };
        return String.join(SEPARATOR_OF_MAC, mac);
    }

    public static byte[] textToByteArrayUTF8(String text) {
        // 将文本转换为字节数组
        byte[] byteArray = text.getBytes(StandardCharsets.UTF_8);

        // 如果需要将负字节转换为正字节
        byte[] result = new byte[byteArray.length];
        System.arraycopy(byteArray, 0, result, 0, byteArray.length);

        return result;
    }

    public static byte[] getUnixTimeHeypixel() {
        long timestamp = System.currentTimeMillis(); // 当前时间戳

        // 将时间戳转换为8个字节的字节数组
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(timestamp);
        byte[] timestampBytes = buffer.array();

        byte[] otherData = new byte[]{};

        byte[] result = new byte[otherData.length + timestampBytes.length];
        System.arraycopy(otherData, 0, result, 0, otherData.length);
        System.arraycopy(timestampBytes, 0, result, otherData.length, timestampBytes.length);
        return result;
    }
}
