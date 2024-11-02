package dev.faiths.module.player;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleChatBypass extends CheatModule {
    public String lastSend = "";
    public ModuleChatBypass() {
        super("ChatBypass", Category.PLAYER);
    }

    public final Handler<PacketEvent> packetEventHandler = event -> {
        Packet packet = event.getPacket();
        if (event.getType() == PacketEvent.Type.SEND) {
            if (packet instanceof C01PacketChatMessage) {
                lastSend = ((C01PacketChatMessage) packet).getMessage();
            }
        } else {
            if (lastSend.isEmpty()) return;
            if (packet instanceof S02PacketChat) {
                String msg = ((S02PacketChat) packet).getChatComponent().getUnformattedText();
                if (msg.contains("不合法")) {
                    mc.thePlayer.sendChatMessage(msg);
                    event.setCancelled(true);
                } else if (msg.contains("频繁发送重复")) {
                    event.setCancelled(true);
                }
            }
        }
    };
}
