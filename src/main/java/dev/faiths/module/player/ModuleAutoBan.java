package dev.faiths.module.player;

import cn.hutool.core.util.RandomUtil;
import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.notifiction.NotificationType;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleAutoBan extends CheatModule {
    public ModuleAutoBan() {
        super("AutoBan", Category.PLAYER);
    }

    public final Handler<UpdateEvent> eventHandler = event -> {
        if (mc.thePlayer != null && mc.theWorld != null && !mc.thePlayer.isSneaking()) {
            mc.thePlayer.motionY = RandomUtil.randomDouble(-10, 10);
            mc.thePlayer.motionX = RandomUtil.randomDouble(-10, 10);
            mc.thePlayer.motionZ = RandomUtil.randomDouble(-10, 10);
            mc.thePlayer.capabilities.isFlying = true;
            mc.thePlayer.capabilities.isCreativeMode = true;
            for (int i = 0; i < 50; i++) {
                mc.getNetHandler().sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                mc.getNetHandler().addToSendQueue(new C0FPacketConfirmTransaction(0, (short) RandomUtil.randomInt(-200, -1), true));
            }
            Faiths.notificationManager.pop("AutoBan", "If you are on an anti-cheat server, you will be banned!", 5000, NotificationType.WARNING);
            Faiths.notificationManager.pop("AutoBan", "Sneak Now!!!", 100, NotificationType.INFO);
        }

    };
}
