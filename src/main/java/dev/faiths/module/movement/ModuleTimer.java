package dev.faiths.module.movement;

import dev.faiths.Faiths;
import dev.faiths.component.MovementComponent;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.*;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.player.ModuleBlink;
import dev.faiths.module.world.ModuleScaffold;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.DebugUtil;
import dev.faiths.utils.PacketUtils;
import dev.faiths.utils.StopWatch;
import dev.faiths.utils.player.MoveUtils;
import dev.faiths.utils.render.RoundedUtil;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.*;

import java.awt.*;
import java.util.LinkedList;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleTimer extends CheatModule {
    private final ValueFloat speed = new ValueFloat("TimerSpeed", 3.0f, 0.1f, 6f);
    private final ValueBoolean dis = new ValueBoolean("AutoDisable", true);
    private final ValueBoolean dissca = new ValueBoolean("AutoDisSca", true);
    private final ValueBoolean scaOnly = new ValueBoolean("ScaffoldOnly", true);
    private final ValueBoolean poslook = new ValueBoolean("PosLook", true);
    private final ValueBoolean debug = new ValueBoolean("Debug", true);
    private final ValueBoolean render = new ValueBoolean("Render", true);
    private final LinkedList<Packet<INetHandler>> inBus = new LinkedList<>();
    private final StopWatch stopWatch = new StopWatch();
    private int balance = 0;
    private boolean disable;
    public ModuleTimer() {
        super("BalanceTimer", Category.PLAYER,"时间管理大师");
    }


    @Override
    public void onEnable() {
        balance = 0;
        stopWatch.reset();

        if (dis.getValue()) {
            disable = false;
        }
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer == null) return;
        inBus.forEach(packet -> packet.processPacket(mc.getNetHandler()));
        inBus.clear();

       // MovementComponent.resetMove();
        mc.timer.timerSpeed = 1F;
    }


    
    public Handler<PacketEvent>PacketEventHandler =  event -> {
        Packet<?> packet = event.getPacket();
        if (packet instanceof C03PacketPlayer) {
            if (!MoveUtils.isMoving()) {
                event.setCancelled(true);
            }
            if (!event.isCancelled() || Faiths.moduleManager.getModule(ModuleBlink.class).getState()) {
                balance -= 50;
            }
            balance += (int) stopWatch.getElapsedTime();
            stopWatch.reset();
        }
        if (packet instanceof S32PacketConfirmTransaction) {
            event.setCancelled(true);
            PacketUtils.sendPacketC0F();
        }
        if (packet instanceof S12PacketEntityVelocity) {
            inBus.add((Packet<INetHandler>) packet);
            event.setCancelled(true);
        }
        if (packet instanceof S27PacketExplosion) {
            inBus.add((Packet<INetHandler>) packet);
            event.setCancelled(true);
        }
        if (packet instanceof S23PacketBlockChange) {
            inBus.add((Packet<INetHandler>) packet);
            event.setCancelled(true);
        }
        if (packet instanceof S08PacketPlayerPosLook && poslook.getValue()) {
            setState(false);
        }

    };

    
    public Handler<UpdateEvent>UpdateEventHandler = event -> {
        if (balance < 200) {
            mc.timer.timerSpeed = 1F;
            if (dis.getValue()) {
                if (disable) {
                    setState(false);
                    if (dissca.getValue()) {
                        if (Faiths.moduleManager.getModule(ModuleScaffold.class).getState()) {
                            Faiths.moduleManager.getModule(ModuleScaffold.class).setState(false);
                        }
                    }
                }
            }
        } else {
            mc.timer.timerSpeed = Faiths.moduleManager.getModule(ModuleScaffold.class).getState() && scaOnly.getValue() ? speed.getValue().floatValue() : 1F;
            if (dis.getValue()) {
                disable = true;
            }
        }

    };

    
    public Handler<MotionEvent>MotionEventHandler =  event ->  {
        if (mc.thePlayer.ticksExisted % 20 == 0 && debug.getValue()) {
            DebugUtil.log("BalanceTimer: " + balance);
        }
    };

    
    public void onWorld(WorldEvent event) {
        if (event == null) return;

        setState(false);
        stopWatch.reset();
    }

    
    public Handler<Render2DEvent>Render2DEvent = event -> {
        ScaledResolution sr = new ScaledResolution(mc);
        if (render.getValue()) {
            int startX = sr.getScaledWidth() / 2 - 68;
            int startY = sr.getScaledHeight() / 2 -20;
            int Packet = balance;
            GlStateManager.disableAlpha();
            String text = "" + Packet;
            FontManager.sf20.drawStringWithShadow(text, startX + 10 + 60 - FontManager.sf20.getStringWidth(text) / 2, startY - 20,1,50);
            RoundedUtil.drawGradientRound(startX + 10, startY, 120.0f, 3.0f, 3.0f, new Color(0, 0, 0, 200), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150));
            RoundedUtil.drawGradientRound(startX + 10,startY, Math.min(Packet / 50.0f, 120.0f), 3.0f, 3.0f,new Color(241, 59, 232, 170), new Color(241, 59, 232, 170), new Color(241, 59, 232, 170), new Color(241, 59, 232, 170));
            GlStateManager.disableAlpha();
        }
    };


}
