package dev.faiths.module.player;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.WorldEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.world.ModuleScaffold;
import dev.faiths.utils.ClientUtils;
import dev.faiths.utils.MSTimer;
import dev.faiths.utils.player.FallingPlayer;
import dev.faiths.utils.player.InventoryUtil;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.utils.player.PredictPlayer;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueInt;
import dev.faiths.value.ValueMode;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import java.util.ArrayList;

import static dev.faiths.utils.IMinecraft.mc;

public class ModuleAntiVoid extends CheatModule {
    public ValueMode mode = new ValueMode("Mode", new String[]{"Watchdog","AutoScaffold"}, "Watchdog");
    private final ValueInt pullbackTime = new ValueInt("PullbackTime", 800, 500, 2000).visible(() -> mode.is("Watchdog"));
    private final ValueBoolean onlyVoid = new ValueBoolean("OnlyVoid", false).visible(() -> mode.is("Watchdog"));
    public double[] lastGroundPos = new double[3];
    public static MSTimer timer = new MSTimer();
    public static ArrayList<Packet> packets = new ArrayList<>();
    private static final double T = 10;
    private static final double T_MIN = 0.001;
    private static final double ALPHA = 0.997;
    private int attempted;
    private boolean scaffoldEnabled;
    //    private int ticksLeft;
    private boolean calculating;


    public ModuleAntiVoid() {
        super("AntiVoid", Category.PLAYER);
    }

    private final Handler<WorldEvent> worldEventHandler = event -> {
        if (mode.is("Watchdog")) {
            if (!packets.isEmpty()) {
                for (Packet packet : packets)
                    mc.getNetHandler().sendPacketNoEvent(packet);
                packets.clear();
            }
        }
    };

    private final Handler<PacketEvent> packetEventHandler = e -> {
        if (mode.is("Watchdog")) {
            if (mc.thePlayer == null || mc.theWorld == null) return;
            if (mc.thePlayer.capabilities.allowFlying || mc.thePlayer.ticksExisted < 20 || Faiths.moduleManager.getModule(ModuleScaffold.class).getState())
                return;
            final PredictPlayer predictPlayer = new PredictPlayer();


            if (e.getType() == PacketEvent.Type.SEND) {
                if (!packets.isEmpty() && mc.thePlayer.ticksExisted < 100)
                    packets.clear();

                if (e.getPacket() instanceof C03PacketPlayer) {
                    final C03PacketPlayer c03 = (C03PacketPlayer) e.getPacket();

                    if ((predictPlayer.findCollision(50) == null || !onlyVoid.getValue()) && !PlayerUtils.isBlockUnder(mc.thePlayer)) {
                        e.setCancelled(true);
                        packets.add(c03);

                        if (timer.delay(pullbackTime.getValue()) && !packets.isEmpty()) {
                            mc.getNetHandler().sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(lastGroundPos[0], lastGroundPos[1] - 1, lastGroundPos[2], true));
                        }

                    } else {
                        lastGroundPos[0] = mc.thePlayer.posX;
                        lastGroundPos[1] = mc.thePlayer.posY;
                        lastGroundPos[2] = mc.thePlayer.posZ;

                        if (!packets.isEmpty()) {
                            for (Packet packet : packets)
                                mc.getNetHandler().sendPacketNoEvent(packet);
                            packets.clear();
                        }

                        timer.reset();
                    }
                } else if ((predictPlayer.findCollision(50) == null || !onlyVoid.getValue()) && !PlayerUtils.isBlockUnder(mc.thePlayer)) {
                    packets.add(e.getPacket());
                    e.setCancelled(true);
                }
            } else {
                if (e.getPacket() instanceof S08PacketPlayerPosLook && packets.size() > 1) {
                    for (Packet packet : packets) {
                        if (!(packet instanceof C03PacketPlayer))
                            mc.getNetHandler().sendPacketNoEvent(packet);
                    }
                    packets.clear();
                }
            }
        }
        if (mode.is("AutoScaffold")) {
            if (mc.thePlayer == null) return;
            if (mc.thePlayer.onGround) {
                if (scaffoldEnabled) {
                    Faiths.moduleManager.getModule(ModuleScaffold.class).setState(false);
                    scaffoldEnabled = false;
                }
                attempted = 0;
                calculating = false;
            }
            if (mc.thePlayer.motionY < 0.1 && new FallingPlayer(mc.thePlayer).findCollision(60) == null && !PlayerUtils.isBlockUnder(mc.thePlayer.posY + mc.thePlayer.getEyeHeight()) && mc.thePlayer.fallDistance > 3) {
                ModuleScaffold scaffold = (ModuleScaffold) Faiths.moduleManager.getModule(ModuleScaffold.class);
                ModuleStuck stuck = (ModuleStuck) Faiths.moduleManager.getModule(ModuleStuck.class);
                if (mc.thePlayer.motionY >= -1 && !scaffold.getState() && !stuck.getState()) {
                    ClientUtils.displayChatMessage("ok");
                    scaffoldEnabled = true;
                    scaffold.setState(true);
                    scaffold.bigVelocityTick = 10;
                } else if (mc.thePlayer.motionY < -1 && attempted <= 1 && !mc.thePlayer.onGround) {
                    ClientUtils.displayChatMessage("ok1");
//                ticksLeft = 10;
                    attempted += 1;

                    int findSlot = -1;
                    for (int i = 36; i <= 44; i++) {
                        if (mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem() instanceof ItemEnderPearl) {
                            ClientUtils.displayChatMessage("ok2");
                            findSlot = i;
                            break;
                        }
                    }

                    if (findSlot == -1) {
                        ClientUtils.displayChatMessage("ok3");
                        for (int i = 0; i <= 35; i++) {
                            if (mc.thePlayer.inventoryContainer.getSlot(i).getStack().getItem() instanceof ItemEnderPearl) {
                                findSlot = i;
                                break;
                            }
                        }
                        if (findSlot == -1) {
                            return;
                        }

                        InventoryUtil.swap(findSlot, 8);
                        findSlot = 44;
                    }
                    if (scaffold.getState()) {
                        ClientUtils.displayChatMessage("ok4");
                        scaffold.setState(true);
                        scaffoldEnabled = false;
                    }
                    mc.thePlayer.inventory.currentItem = findSlot - 36;

                    stuck.setState(true);
                }
            }
        }
    };
}
