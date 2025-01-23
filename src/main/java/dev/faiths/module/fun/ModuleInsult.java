package dev.faiths.module.fun;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.event.impl.WorldEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.value.ValueMode;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;

import java.util.ArrayList;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleInsult extends CheatModule {
    public ValueMode priority = new ValueMode("Mode", new String[] { "xinxin" , "nzhm" }, "xinxin");
    private EntityPlayer target;
    private ArrayList<String> xinxinAbuseContent = new ArrayList<>();

    public ModuleInsult() {
        super("Insult", Category.FUN,"自动发L");

        xinxinAbuseContent.add("@[欣欣公益14.50]起床同距离同装备嘴强对刀客户端 免费获取点击右边的代码xinxin.liq ><" +  Minecraft.getSystemTime() % 1000L);
        xinxinAbuseContent.add("@[欣欣提醒]别用欣欣的水影 诈骗且垃记 而且不稳定 免费获取点击右边代码xinxin.liq >" +  Minecraft.getSystemTime() % 1000L + "<");
        xinxinAbuseContent.add("@[SilenceFix14.50]免费嘴强客户端点击右边代码xinxin.liq无法获取");
        xinxinAbuseContent.add("@[欣欣公益14.50]全网最垃记的起床跑吃跑喝 使用report绕过思路 依托答辩 免费获取点击右边代码xinxin.liq >" +  Minecraft.getSystemTime() % 1000L + "<");
        xinxinAbuseContent.add("@[欣欣公益14.50]你的付费客户端可以打过欣欣公益 免费获取点击右边代码xinxin.liq >" +  Minecraft.getSystemTime() % 1000L + "<");
        xinxinAbuseContent.add("@[欣欣公益14.50]全网最拉记的时间管理大师 用了就ban免费获取点击右边代码xinxin.liq >" +  Minecraft.getSystemTime() % 1000L + "<");
    }

    private final Handler<UpdateEvent> updateEventHandler = event -> {
        if (target != null && !mc.theWorld.loadedEntityList.contains(target)) {
            switch (priority.getValue()) {
                case "xinxin":
                    mc.thePlayer.sendChatMessage(xinxinAbuseContent.get((int)(Math.random() * xinxinAbuseContent.size())).replace("%s", target.getDisplayName().getUnformattedTextForChat()));
                    break;
                case "nzhm":
                    mc.thePlayer.sendChatMessage(xinxinAbuseContent.get((int)(Math.random() * xinxinAbuseContent.size())).replace("%s", target.getDisplayName().getUnformattedTextForChat()));
                    break;
            }
            target = null;
            Faiths.notificationManager.pop("Send", NotificationType.INFO);
        }
    };

    private final Handler<PacketEvent> attackEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE) return;
        if (!(event.getPacket() instanceof C02PacketUseEntity)) return;
        C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
        if (packet.getEntityFromWorld(mc.theWorld) instanceof EntityPlayer) {
            target = (EntityPlayer) packet.getEntityFromWorld(mc.theWorld);
        }
    };

    private final Handler<WorldEvent> worldEventHandler = event -> {
        target = null;
    };
}
