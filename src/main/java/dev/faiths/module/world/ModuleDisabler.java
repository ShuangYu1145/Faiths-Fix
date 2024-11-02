package dev.faiths.module.world;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.event.impl.WorldEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.movement.ModuleSpeed;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.DebugUtil;
import dev.faiths.utils.ServerUtils;
import dev.faiths.utils.Servers;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueMode;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S09PacketHeldItemChange;

import java.util.ArrayList;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleDisabler extends CheatModule {
    private ValueMode mode = new ValueMode("Mode", new String[]{"GrimPost","Watchdog"}, "Watchdog");
    private ValueBoolean c0fFix = new ValueBoolean("C0FFix", false).visible(()->mode.is("Grim"));
    private ValueBoolean lowhop = new ValueBoolean("Lowhop", false).visible(()->mode.is("Watchdog"));
    public ModuleDisabler() {
        super("Disabler",Category.WORLD);
        INSTANCE = this;
    }

    @Override
    public String getSuffix() {
        return mode.getValue();
    }
    private int testTicks;
    private WorldClient lastWorld = null;
    private int lastSlot;
    private boolean disabled;
    static ModuleDisabler INSTANCE;
    private boolean stuck = false;
    private boolean jump = false;
    private long lastLoadWorldTime = 0L;
    private boolean lastTickSentC0F = false;
    private boolean spoofed = false;
    private boolean lastGround = true;

    private double storageX = 0.0;
    private double storageY = 0.0;
    private double storageZ = 0.0;
    private boolean flagStop = false;

    private double x, y, z, motionX, motionY, motionZ;
    private boolean flag1 = false;

    public static ArrayList<C0FPacketConfirmTransaction> c0fStorage = new ArrayList<>();

    @Override
    public void onEnable() {
        jump = false;
        testTicks = 0;
    }

    private final Handler<WorldEvent> worldEventHandler = event -> {
        c0fStorage.clear();
        lastLoadWorldTime = System.currentTimeMillis();
        stuck = false;
        spoofed = false;
        jump = true;
        testTicks = 0;
    };

    public static boolean getGrimPost() {
        return mc.thePlayer != null && mc.theWorld != null && Faiths.moduleManager.getModule(ModuleDisabler.class).mode.is("GrimPost") && Faiths.moduleManager.getModule(ModuleDisabler.class).getState() && mc.thePlayer.ticksExisted > 30;
    }

    public static boolean shouldProcess() {
        return true;
    }

    private Handler<UpdateEvent> updateEventHandler = event -> {
        lastTickSentC0F = false;
    };

    private Handler<PacketEvent> packetEventHandler = event -> {
        Packet<?> packet = event.getPacket();
        if (event.getType() == PacketEvent.Type.SEND) {
            if (packet instanceof C09PacketHeldItemChange) {
                if (lastSlot == ((C09PacketHeldItemChange) packet).getSlotId()) {
                    event.setCancelled(true);
                }
                lastSlot = ((C09PacketHeldItemChange) packet).getSlotId();
            }
            if (c0fFix.getValue() && System.currentTimeMillis() - lastLoadWorldTime >= 2000) {
                if (event.getPacket() instanceof C0FPacketConfirmTransaction) {
                    if (!lastTickSentC0F) {
                        if (!c0fStorage.isEmpty()) {
                            c0fStorage.add((C0FPacketConfirmTransaction) event.getPacket());
                            event.setCancelled(true);
                            mc.getNetHandler().addToSendQueue(c0fStorage.get(0), true);
                            c0fStorage.remove(0);
                            lastTickSentC0F = true;
                        }
                    } else {
                        c0fStorage.add((C0FPacketConfirmTransaction) event.getPacket());
                        event.setCancelled(true);
                        DebugUtil.log("multi c0f in 1 client tick, blink.");
                    }
                }
            }
            if(mode.is("Watchdog")){
                if (event.getPacket() instanceof C03PacketPlayer && (!lowhop.getValue())) {
                    final C03PacketPlayer wrapper = ((C03PacketPlayer) event.getPacket());

                    if (!wrapper.isMoving() && !wrapper.rotating && wrapper.isOnGround() && lastGround) {
                        event.setCancelled(true);
                    }
                    lastGround = wrapper.isOnGround();
                }
            }
        } else {
            if (packet instanceof S09PacketHeldItemChange) {
                lastSlot = ((S09PacketHeldItemChange) packet).getHeldItemHotbarIndex();
            }
            if(mode.is("Watchdog") && lowhop.getValue() && disabled){
                if(mc.thePlayer.ticksExisted <= 200) {
                    if(mc.thePlayer.ticksExisted == 4) {
                        mc.thePlayer.motionY = mc.thePlayer.motionZ = mc.thePlayer.motionX = 0;
                    }
                }
                if(event.getPacket() instanceof S08PacketPlayerPosLook) {
                    testTicks++;
                    if(testTicks == 20) {
                        mc.thePlayer.jump();
                        disabled = false;
                        testTicks = 0;
                        Faiths.notificationManager.pop("Disabler","Disabled Watchdog Motion Checks Successfully", 3000, NotificationType.SUCCESS);
                    }
                    mc.thePlayer.motionY = mc.thePlayer.motionZ = mc.thePlayer.motionX = 0;
                }
            }
        }
    };

    private Handler<MotionEvent> motionEventHandler = event -> {
        if (lowhop.getValue() && mode.is("Watchdog")) {
            if(mc.thePlayer.onGround && jump) {
                mc.thePlayer.jump();
                mc.thePlayer.jumpTicks = 0;
            } else if (jump) {
                Faiths.notificationManager.pop("Disabler", "Disabler is working,do not move", 3000, NotificationType.WARNING);
                jump = false;
                disabled = true;
            } else if(disabled && mc.thePlayer.offGroundTicks >= 10) {
                if ((!ServerUtils.isHypixel() || ModuleHypixelUtils.getCurrentServer() == Servers.NONE) && jump) {
                    Faiths.notificationManager.pop("Disabler", "Skip disabler.", NotificationType.INFO);
                    jump = false;
                    disabled = false;
                    testTicks = 0;
                    return;
                }
                if(mc.thePlayer.offGroundTicks % 2 == 0) {
                    event.setX(event.getX() + 0.095);
                    PlayerUtils.stop();
                }
                if(Faiths.moduleManager.getModule(ModuleSpeed.class).getState()) {
                    Faiths.notificationManager.pop("Disabler","Disabled module Speed due to disabler is working", 3000, NotificationType.INFO);
                    Faiths.moduleManager.getModule(ModuleSpeed.class).setState(false);
                }
                mc.thePlayer.motionY = 0;
            }
        }
    };
}
