package dev.faiths.module.world;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.ClientUtils;
import dev.faiths.utils.StopWatch;
import dev.faiths.utils.protocol.Heypixel;
import dev.faiths.value.ValueMode;
import io.netty.buffer.Unpooled;
import net.minecraft.event.ClickEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleProtocol extends CheatModule {
    public ModuleProtocol() {
        super("Protocol",Category.WORLD);
        data.put("bw-practice", new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 48, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 48, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 100, 97, 108, 117, 34, 125});
        data.put("solobw", new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 49, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 49, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 115, 111, 108, 111, 34, 125});
        data.put("doublebw", new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 50, 36, 123, 34, 101, 110, 116, 114, 121, 34, 58, 50, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 100, 111, 117, 98, 108, 101, 34, 125});
        data.put("teamsbw", new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 51, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 51, 44, 34, 115, 105, 100, 34, 58, 34, 66, 69, 68, 87, 65, 82, 47, 98, 119, 45, 116, 101, 97, 109, 34, 125});
        data.put("swsolo", new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 48, 34, 123, 34, 101, 110, 116, 114, 121, 34, 58, 48, 44, 34, 115, 105, 100, 34, 58, 34, 83, 75, 89, 87, 65, 82, 47, 110, 115, 107, 121, 119, 97, 114, 34, 125});
        data.put("swdouble", new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 49, 41, 123, 34, 101, 110, 116, 114, 121, 34, 58, 49, 44, 34, 115, 105, 100, 34, 58, 34, 83, 75, 89, 87, 65, 82, 47, 110, 115, 107, 121, 119, 97, 114, 45, 100, 111, 117, 98, 108, 101, 34, 125});
        data.put("rolewar", new byte[]{0, 0, 0, 26, 20, 71, 85, 73, 36, 109, 97, 105, 110, 109, 101, 110, 117, 64, 101, 110, 116, 114, 121, 47, 50, 33, 123, 34, 101, 110, 116, 114, 121, 34, 58, 50, 44, 34, 115, 105, 100, 34, 58, 34, 70, 73, 71, 72, 84, 47, 107, 98, 45, 103, 97, 109, 101, 34, 125});
    }

    private boolean isFirst = true;
    private final StopWatch stopWatch = new StopWatch();
    private ValueMode mode = new ValueMode("Mode", new String[]{"Heypixel", "HYT"}, "HYT");

    @Override
    public String getSuffix() {
        return mode.getValue();
    }

    private HashMap<String, byte[]> data = new HashMap<>();

    private Handler<PacketEvent> packetEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE) {
            if (mode.is("HYT")) {
                if (event.getPacket() instanceof S3FPacketCustomPayload) {
                    S3FPacketCustomPayload packet = (S3FPacketCustomPayload) event.getPacket();
                    if (packet.getChannelName().equalsIgnoreCase("germplugin-netease")) {
                        PacketBuffer buffer = packet.getBufferData();
                        byte[] data = new byte[buffer.readableBytes()];
                        buffer.readBytes(data);
                        if (new String(data, StandardCharsets.UTF_8).contains("h*0.5-min(h,w*9/16)*0.5")) {
                            mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("germmod-netease", new PacketBuffer(Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 8, 109, 97, 105, 110, 109, 101, 110, 117, 8, 109, 97, 105, 110, 109, 101, 110, 117, 8, 109, 97, 105, 110, 109, 101, 110, 117}))));
                            for (String name : this.data.keySet()) {
                                ChatComponentText textComponents = new ChatComponentText("");
                                textComponents.appendSibling(createClickableText("§8[§f" + name + "§8] ", "/germ " + name));
                                mc.thePlayer.addChatComponentMessage(textComponents);
                            }
                        }
                        event.setCancelled(true);
                    }
                }
            }
            if (event.getType() == PacketEvent.Type.SEND) {
                if (event.getPacket() instanceof C01PacketChatMessage) {
                    String message = ((C01PacketChatMessage) event.getPacket()).getMessage();
                    if (message.startsWith("/germ ")) {
                        String name = message.replace("/germ ", "");
                        if (data.containsKey(name)) {
                            mc.thePlayer.sendQueue.addToSendQueue(new C17PacketCustomPayload("germmod-netease", new PacketBuffer(Unpooled.wrappedBuffer(data.get(name)))));
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }

        if (mode.is("Heypixel")){
            Packet<?> packet = event.getPacket();
            if (packet instanceof C15PacketClientSettings) {
                event.setCancelled(true);
                Heypixel.sendMinecraftRegister();
                Heypixel.sendForgeBrand();
                Heypixel.sendJoinHeypixelCheck();
                isFirst = true;
                stopWatch.reset();
            }
            if (packet instanceof C17PacketCustomPayload) {
                C17PacketCustomPayload customPayload = (C17PacketCustomPayload) packet;
                String channelName = customPayload.getChannelName();
                System.out.println("Channel: " + channelName);
                ClientUtils.displayChatMessage("Channel: " + channelName);

                PacketBuffer data = customPayload.getBufferData();
                if (data != null && data.readableBytes() > 0) {
                    byte[] payloadBytes = new byte[data.readableBytes()];
                    data.readBytes(payloadBytes);
                    String payloadContent = new String(payloadBytes, StandardCharsets.UTF_8);
                    System.out.println("Payload content: " + payloadContent);
                    ClientUtils.displayChatMessage("Payload content: " + payloadContent);
                }
            }
        }
    };

    public boolean isNull(){
        return mc.thePlayer == null || mc.theWorld == null;
    }

    private Handler<UpdateEvent> updateEventHandler = event -> {
        try {
            if (isFirst && isNull()) {
                Heypixel.sendJoinHeypixelCheck();
                stopWatch.reset();
            } else if (stopWatch.finished(5000)) {
                Heypixel.sendKeepAlive();
                stopWatch.reset();
            }
        }
        catch (Exception ex){

        }
    };


    private IChatComponent createClickableText(String text, String command) {
        ChatComponentText clickableText = new ChatComponentText(text);
        clickableText.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return clickableText;
    }
}
