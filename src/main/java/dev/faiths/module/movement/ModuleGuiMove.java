package dev.faiths.module.movement;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.*;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.clickgui.AstolfoGui;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.value.ValueBoolean;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleGuiMove extends CheatModule {
    public static ArrayList<Packet> packetListYes = new ArrayList<>();
    public static ArrayList<Packet> packetPlayers = new ArrayList<>();
    public static ArrayList<Packet> packetsQueue = new ArrayList<>();
    public static boolean blinking = false;
    public static boolean incontainer = false;
    ValueBoolean nomove = new ValueBoolean("NoMoveClick", false);
    ValueBoolean safeblink = new ValueBoolean("SafeBlink", false);
    ValueBoolean blink = new ValueBoolean("Blink", false);

    public ModuleGuiMove() {
        super("GuiMove", Category.MOVEMENT);
    }

    public static KeyBinding[] keyBindings = new KeyBinding[] {
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindJump
    };

    private final Handler<ClickWindowEvent> clickWindowEventHandler = event -> {
        if(nomove.getValue() && PlayerUtils.isMoving()){
            event.setCancelled(true);
        }
    };

    private final Handler<Render2DEvent> render2DEventHandler = event -> {
        if (mc.currentScreen instanceof GuiContainer || mc.currentScreen instanceof AstolfoGui || mc.currentScreen == null) {
            for (KeyBinding keyBinding : keyBindings) {
                keyBinding.setPressed(Keyboard.isKeyDown(keyBinding.getKeyCode()));
            }
        }
    };

    private final Handler<UpdateEvent> updateEventHandler = event -> {
        if (safeblink.getValue()) {
            if ((mc.thePlayer.ticksExisted % 15) != 0 && incontainer)
                blinking = true;
            if ((mc.thePlayer.ticksExisted % 15) == 7 && !packetListYes.isEmpty()) {
                packetListYes.forEach(mc.getNetHandler()::sendPacketNoEvent);
                packetListYes.clear();
            } else if ((mc.thePlayer.ticksExisted % 15) == 0 && !packetPlayers.isEmpty()) {
                packetPlayers.forEach(mc.getNetHandler()::sendPacketNoEvent);
                packetPlayers.clear();
                blinking = false;
            }
        }
    };

    private final Handler<WorldEvent> worldEventHandler = event -> {
        incontainer = false;
        packetPlayers.clear();
        packetListYes.clear();
        blinking = false;
    };

    private final Handler<PacketEvent> packetEventHandler = event -> {
        Packet packet = event.getPacket();
        if (safeblink.getValue()) {
            if ((packet instanceof C03PacketPlayer || packet instanceof C0FPacketConfirmTransaction) && blinking) {
                packetPlayers.add(packet);
                event.setCancelled(true);
            }

            if (incontainer) {
                if (packet instanceof C0EPacketClickWindow && safeblink.getValue()) {
                    packetListYes.add(packet);
                    event.setCancelled(true);
                }

                if (packet instanceof C0DPacketCloseWindow && safeblink.getValue()) {
                    packetListYes.add(packet);
                    event.setCancelled(true);
                }
            }

            if (packet instanceof S2DPacketOpenWindow || (packet instanceof C16PacketClientStatus && ((C16PacketClientStatus) packet).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)) {
                incontainer = true;
                while (true) {
                    Packet packet1 = packetPlayers.get(0);
                    mc.getNetHandler().sendPacketNoEvent(packet1);
                    if (packet1 instanceof C0DPacketCloseWindow) {
                        break;
                    }
                }
            }

            if (packet instanceof S2EPacketCloseWindow || packet instanceof C0DPacketCloseWindow) {
                incontainer = false;
            }
        }
        if (blink.getValue()) {
            if ((packet instanceof C16PacketClientStatus && ((C16PacketClientStatus) packet).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT) || event.getPacket() instanceof C0EPacketClickWindow) {
                event.setCancelled(true);
                packetsQueue.add(event.getPacket());
            }
            if (event.getPacket() instanceof C0DPacketCloseWindow) {
                if (!packetsQueue.isEmpty()) {
                    packetsQueue.forEach(mc.getNetHandler()::sendPacketNoEvent);
                    packetsQueue.clear();
                }
            }
            if (packet instanceof S2EPacketCloseWindow) {
                packetsQueue.clear();
            }
        }
    };
}
