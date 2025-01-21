package dev.faiths.module.fun;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.combat.ModuleGapple;
import dev.faiths.module.combat.ModuleKillAura;
import dev.faiths.module.movement.ModuleSpeed;
import dev.faiths.module.player.ModuleContainerStealer;
import dev.faiths.module.player.ModuleInvManager;
import dev.faiths.module.player.ModuleStuck;
import dev.faiths.module.world.ModuleContainerAura;
import dev.faiths.module.world.ModuleScaffold;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.tasks.FutureTask;
import dev.faiths.value.ValueBoolean;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.util.ScreenShotHelper;

import static dev.faiths.utils.IMinecraft.mc;

public class ModuleHyt extends CheatModule {
    public ModuleHyt() {
        super("Hyt",Category.FUN);
    }

    @Override
    public void onEnable() {
    }

    private ValueBoolean AutoSkyWars = new ValueBoolean("AutoSkyWars", true);
    private ValueBoolean XinXinCheck = new ValueBoolean("XinXinCheck", true);
    private ValueBoolean AutoGG = new ValueBoolean("AutoGG", true);
    private ValueBoolean AutoScreenshot = new ValueBoolean("AutoScreenshot", true);

    private void AutoSkyWars(PacketEvent e) {
        if (e.getPacket() instanceof S02PacketChat) {
            S02PacketChat packet = (S02PacketChat) e.getPacket();

            //死了
            if (packet.getChatComponent().getUnformattedText().contains("你现在是观察者状态. 按E打开菜单.")) {

                Faiths.moduleManager.getModule(ModuleKillAura.class).setState(false);
                Faiths.moduleManager.getModule(ModuleSpeed.class).setState(false);
                Faiths.moduleManager.getModule(ModuleInvManager.class).setState(false);
                Faiths.moduleManager.getModule(ModuleContainerStealer.class).setState(false);
                Faiths.moduleManager.getModule(ModuleContainerAura.class).setState(false);
                Faiths.moduleManager.getModule(ModuleScaffold.class).setState(false);
                Faiths.moduleManager.getModule(ModuleStuck.class).setState(false);
                Faiths.moduleManager.getModule(ModuleGapple.class).setState(false);

                mc.getNetHandler().sendPacketNoEvent(new C01PacketChatMessage("/hub 100"));
            }

            //开始
            if (packet.getChatComponent().getUnformattedText().contains("开始倒计时: 1 秒")) {

                Faiths.moduleManager.getModule(ModuleInvManager.class).setState(true);
                Faiths.moduleManager.getModule(ModuleContainerStealer.class).setState(true);
                Faiths.moduleManager.getModule(ModuleContainerAura.class).setState(true);

            }
        }
    }

    public static boolean XinXinCheck(PacketEvent e) {
        if (e.getPacket() instanceof S02PacketChat) {
            S02PacketChat packet = (S02PacketChat) e.getPacket();
            if (packet.getChatComponent().getUnformattedText().contains("SilenceFix")) {
                String senderName = ((S02PacketChat) packet).getChatComponent().getUnformattedText().split(":")[0];

                Faiths.notificationManager.pop("诶呦我去", "欣欣来了！！！！！！！！", 5000, NotificationType.WARNING);
                Faiths.notificationManager.pop("我是欣欣！！！！！", senderName, 5000, NotificationType.WARNING);
            }
        }
        return false;
    }

    //起床战争>> 恭喜 ！绿之队队获得胜利!

    public static boolean AutoGG(PacketEvent e) {
        if (e.getPacket() instanceof S02PacketChat) {
            S02PacketChat packet = (S02PacketChat) e.getPacket();
            if (packet.getChatComponent().getUnformattedText().contains("起床战争>> 恭喜 ！")) {

                mc.getNetHandler().sendPacketNoEvent(new C01PacketChatMessage("@GG"));
                Faiths.notificationManager.pop("AutoGG", "你胜利了", 5000, NotificationType.WARNING);
            }
        }
        return false;
    }

    public static boolean AutoScreenshot(PacketEvent e) {
        if (e.getPacket() instanceof S45PacketTitle) {
            S45PacketTitle packet = (S45PacketTitle) e.getPacket();

            if (packet.getMessage().getUnformattedText().contains("VICTORY")) {
                Faiths.INSTANCE.getTaskManager().queue(new FutureTask(1200) {
                    @Override
                    public void execute() {
                        ScreenShotHelper.safeSaveScreenshot();
                    }

                    @Override
                    public void run() {
                    }
                });
            }

            if (packet.getMessage().getUnformattedText().contains("恭喜!")) {
                Faiths.INSTANCE.getTaskManager().queue(new FutureTask(1200) {
                    @Override
                    public void execute() {
                        ScreenShotHelper.safeSaveScreenshot();
                    }

                    @Override
                    public void run() {
                    }
                });
            }
        }
        return false;
    }



    private final Handler<PacketEvent> packetEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE) {
            if (AutoSkyWars.getValue()) {
                AutoSkyWars(event);
            }

            if (XinXinCheck.getValue()) {
                XinXinCheck(event);
            }

            if (AutoGG.getValue()) {
                AutoGG(event);
            }

            if (AutoScreenshot.getValue()) {
                AutoScreenshot(event);
            }
        }
    };
}
