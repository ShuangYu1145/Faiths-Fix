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
import dev.faiths.module.render.ModuleHUD;
import dev.faiths.module.world.ModuleContainerAura;
import dev.faiths.value.ValueBoolean;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;

import static dev.faiths.utils.IMinecraft.mc;

public class ModuleHyt extends CheatModule {
    public ModuleHyt() {
        super("Hyt",Category.FUN);
    }

    @Override
    public void onEnable() {
    }

    private ValueBoolean AutoSkyWars = new ValueBoolean("AutoSkyWars", true);

    private void AutoSkyWars(PacketEvent e) {
        if (e.getPacket() instanceof S02PacketChat) {
            S02PacketChat packet = (S02PacketChat) e.getPacket();

            if (packet.getChatComponent().getUnformattedText().contains("你现在是观察者状态. 按E打开菜单.")) {

                Faiths.moduleManager.getModule(ModuleKillAura.class).setState(false);
                Faiths.moduleManager.getModule(ModuleSpeed.class).setState(false);
                Faiths.moduleManager.getModule(ModuleInvManager.class).setState(false);
                Faiths.moduleManager.getModule(ModuleContainerStealer.class).setState(false);
                Faiths.moduleManager.getModule(ModuleContainerAura.class).setState(false);
                Faiths.moduleManager.getModule(ModuleGapple.class).setState(false);

                mc.getNetHandler().sendPacketNoEvent(new C01PacketChatMessage("/hub"));
            }
        }
    }

    private final Handler<PacketEvent> packetEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE) {
            if (AutoSkyWars.getValue()) {
                AutoSkyWars(event);
            }
        }
    };
}
