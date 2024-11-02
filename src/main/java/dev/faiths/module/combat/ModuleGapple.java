package dev.faiths.module.combat;

import dev.faiths.Faiths;
import dev.faiths.component.MovementComponent;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.event.impl.TickUpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.player.ModuleBlink;
import dev.faiths.module.render.ModuleHUD;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.ClientUtils;
import dev.faiths.utils.PacketUtils;
import dev.faiths.utils.player.BlinkUtils;
import dev.faiths.utils.render.RoundedUtil;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import io.netty.buffer.Unpooled;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.*;
import dev.faiths.utils.player.InventoryUtil;
import java.awt.*;

import static dev.faiths.module.combat.ModuleKillAura.target;
import static dev.faiths.utils.IMinecraft.mc;


@SuppressWarnings("unused")
public class ModuleGapple extends CheatModule {
    public ValueFloat duringSendTicks = new ValueFloat("DuringSendTicks", 1f, 0f,10f);
    public ValueFloat c03s = new ValueFloat("C03Eat",32f ,1f, 40f);
    public ValueFloat delay = new ValueFloat("Delay", 9f, 0f,10f);
    public ValueBoolean auto = new ValueBoolean("Auto", false);
    private int gappleSlot = -1;
    public static int storedC03 = 0;
    public static boolean eating = false;
    public static boolean sending = false;

    public static boolean restart = false;

    public ModuleGapple() {
        super("Gapple", Category.COMBAT);
    }


    @Override
    public void onEnable() {
        storedC03 = 0;
        this.gappleSlot = InventoryUtil.findItem2(36, 45, Items.golden_apple);
        if (this.gappleSlot != -1) {
            this.gappleSlot -= 36;
        }
    }

    @Override
    public void onDisable() {
        eating = false;

        sending = false;
        BlinkUtils.stopBlink();

        MovementComponent.resetMove();

        PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
        PacketUtils.sendPacketNoEvent(new C17PacketCustomPayload("NoSlowPatcher", new PacketBuffer(Unpooled.buffer())));
        PacketUtils.sendPacket(new C09PacketHeldItemChange((mc.thePlayer.inventory.currentItem)));
    }

        private Handler<TickUpdateEvent> tickUpdateEventHandler = event -> {
        if (mc.thePlayer == null || mc.thePlayer.isDead) {
            BlinkUtils.stopBlink();
            this.setState(false);
            return;
        }
        if (this.gappleSlot == -1) {
            ClientUtils.displayChatMessage("没苹果");
            this.setState(false);
            return;
        }
        if (eating) {
            MovementComponent.cancelMove();
            if (!Faiths.moduleManager.getModule(ModuleBlink.class).getState()) {
                BlinkUtils.startBlink();
            }
        } else {
            eating = true;
        }
        if (storedC03 >= c03s.getValue().intValue()) {
            eating = false;
            sending = true;
            PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(this.gappleSlot));
            PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventoryContainer.getSlot(this.gappleSlot + 36).getStack()));
            BlinkUtils.stopBlink();
            PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            sending = false;
            this.setState(false);
            ClientUtils.displayChatMessage("Eat");
            if (auto.getValue()){
                if (target.getName()!=null){
                    ClientUtils.displayChatMessage("Stop");
                    setState(true);
                    ClientUtils.displayChatMessage("Restart");
                }
            }
            return;
        }
        if ((mc.thePlayer.ticksExisted % delay.getValue().intValue()) == 0) {
            BlinkUtils.releaseC03(duringSendTicks.getValue().intValue());
        }
    };


        private Handler<PacketEvent> packetEventHandler = event -> {
        // 首先检查 event.getPacket() 是否是 C07PacketPlayerDigging 类型的实例
        if (event.getPacket() instanceof C07PacketPlayerDigging) {
            // 强制类型转换，将 event.getPacket() 转换为 C07PacketPlayerDigging 类型
            C07PacketPlayerDigging c07 = (C07PacketPlayerDigging) event.getPacket();

            // 然后检查 c07 的状态是否为 RELEASE_USE_ITEM
            if (c07.getStatus().equals(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM)) {
                event.setCancelled(true);
            }
        }
    };



    private Handler<Render2DEvent> render2DEventHandler = event -> {
        ScaledResolution sr = new ScaledResolution(mc);
        float target = (120.0f * ((float) storedC03 / c03s.getValue().intValue())) * ((float) 100 / 120);
        int startX = sr.getScaledWidth() / 2 - 68;
        int startY = sr.getScaledHeight() / 2 + 100;
        String text = "Gapple...";
        FontManager.sf18.drawString(text, startX + 10 + 60 - FontManager.sf18.getStringWidth(text) / 2, startY + 20, new Color(225, 225, 225, 100).getRGB());
        RoundedUtil.drawGradientRound(startX + 10, (float) (startY + 7.5), 120.0f, 2.0f, 3.0f, new Color(0, 0, 0, 200), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150));
        RoundedUtil.drawGradientRound(startX + 10, (float) (startY + 7.5), Math.min(target, 120.0f), 2.0f, 3.0f,new Color(241, 59, 232, 170), new Color(241, 59, 232, 170), new Color(241, 59, 232, 170), new Color(241, 59, 232, 170));
    };
}