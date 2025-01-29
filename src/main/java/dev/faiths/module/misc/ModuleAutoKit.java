package dev.faiths.module.misc;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.WorldLoadEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.value.ValueInt;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;

import static dev.faiths.utils.IMinecraft.mc;

public class ModuleAutoKit
        extends CheatModule {
    public final ValueInt select = new ValueInt("Select Slot", 6, 1, 7);
    private SelectStatus status = SelectStatus.IDLE;

    public ModuleAutoKit() {
        super("AutoKit", Category.Misc, "自动职业");
    }

    @Override
    public void onEnable() {
        this.status = SelectStatus.IDLE;
    }


    private Handler<WorldLoadEvent> onWorldLoad = event -> {
        this.status = SelectStatus.IDLE;
    };

    private Handler<PacketEvent> packetEventHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE) {
            if (!SelectStatus.DONE.equals((Object) this.status)) {
                ItemStack itemStack;
                S2FPacketSetSlot setSlot;
                Packet<?> packet = event.getPacket();
                if (packet instanceof S2FPacketSetSlot && (setSlot = (S2FPacketSetSlot) packet).func_149174_e() != null && SelectStatus.IDLE.equals((Object) this.status) && setSlot.func_149173_d() >= 36 && setSlot.func_149173_d() <= 44 && (itemStack = setSlot.func_149174_e()) != null && itemStack.getItem().equals(Items.ender_eye)) {
                    int slot = setSlot.func_149173_d() - 36;
                    if (mc.thePlayer.inventory.currentItem != slot) {
                        mc.thePlayer.inventory.currentItem = slot;
                        mc.playerController.updateController();
                    }
                    mc.rightClickMouse();
                    this.status = SelectStatus.WAITING_OPEN;
                }
                if (packet instanceof S30PacketWindowItems) {
                    S30PacketWindowItems windowItems = (S30PacketWindowItems) packet;
                    if (SelectStatus.IDLE.equals((Object) this.status) && windowItems.func_148911_c() == 0) {
                        for (int i = 0; i < windowItems.getItemStacks().length; ++i) {
                            ItemStack itemStack2;
                            if (i < 36 || i > 44 || (itemStack2 = windowItems.getItemStacks()[i]) == null || !itemStack2.getItem().equals(Items.ender_eye))
                                continue;
                            int slot = i - 36;
                            if (mc.thePlayer.inventory.currentItem != slot) {
                                mc.thePlayer.inventory.currentItem = slot;
                                mc.playerController.updateController();
                            }
                            mc.rightClickMouse();
                            this.status = SelectStatus.WAITING_OPEN;
                        }
                    } else if (SelectStatus.WAITING_ITEMS.equals((Object) this.status) && windowItems.func_148911_c() != 0) {
                        for (int i = 0; i < windowItems.getItemStacks().length; ++i) {
                            ItemStack itemStack3 = windowItems.getItemStacks()[i];
                            if (itemStack3 == null || i != this.select.getValue()) continue;
                            mc.playerController.windowClick(windowItems.func_148911_c(), i, 0, 0, mc.thePlayer);
                            event.setCancelled(true);
                        }
                    }
                }
                if (event.getPacket() instanceof S2DPacketOpenWindow && SelectStatus.WAITING_OPEN.equals((Object) this.status) && ((S2DPacketOpenWindow) event.getPacket()).getWindowTitle().getFormattedText().equals("§5选择你的职业§r")) {
                    this.status = SelectStatus.WAITING_ITEMS;
                    event.setCancelled(true);
                }
            }
        }
    };

    enum SelectStatus {
        IDLE,
        WAITING_OPEN,
        WAITING_ITEMS,
        DONE;

    }
}
