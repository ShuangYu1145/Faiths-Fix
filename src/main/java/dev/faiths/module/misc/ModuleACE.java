package dev.faiths.module.misc;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.event.impl.WorldEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.sound.SoundUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.ResourceLocation;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleACE extends CheatModule {

    private int killCount = 0;
    private int resetTicks = 0;

    private EntityLivingBase target;

    public ModuleACE() {
        super("ACE", Category.Misc , "ACE");
    }

    @Override
    public void onEnable() {
        killCount = 0;
        resetTicks = 0;
    }

    private final Handler<UpdateEvent> updateEventHandler = event -> {
        if (killCount > 0) {
            resetTicks++;
            if (resetTicks >= 20 * 60) {
                killCount = 0;
                resetTicks = 0;
                Faiths.notificationManager.pop("Kill Count Reset", NotificationType.INFO);
            }
        }

        if (target != null && !mc.theWorld.loadedEntityList.contains(target)) {
            killCount++;
            resetTicks = 0;
            playKillSound();
            Faiths.notificationManager.pop(killCount + " Kill", NotificationType.INFO);

            target = null;

            if (killCount == 5) {
                Faiths.notificationManager.pop("王牌精锐", NotificationType.WARNING);
            }
        }
    };

    private final Handler<PacketEvent> attackEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE) return;
        if (!(event.getPacket() instanceof C02PacketUseEntity)) return;
        C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
        if (packet.getEntityFromWorld(mc.theWorld) instanceof EntityLivingBase) {  // 检查是否为生物实体
            target = (EntityLivingBase) packet.getEntityFromWorld(mc.theWorld);
        }
    };

    private final Handler<WorldEvent> worldEventHandler = event -> {
        target = null;
    };

    private void playKillSound() {
        String soundFile;

        if (killCount == 1) {
            soundFile = "1.wav";
        } else if (killCount == 2) {
            soundFile = "2.wav";
        } else if (killCount == 3) {
            soundFile = "3.wav";
        } else if (killCount == 4) {
            soundFile = "4.wav";
        } else if (killCount == 5) {
            soundFile = "5.wav";
        } else {
            soundFile = "5.wav";
        }

        SoundUtil.playSound(new ResourceLocation("client/ace/" + soundFile), 1f);
    }
}