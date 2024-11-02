package dev.faiths.module.movement;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.combat.ModuleVelocity;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.utils.DebugUtil;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueInt;
import dev.faiths.value.ValueMode;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

import static dev.faiths.utils.IMinecraft.mc;

public class ModuleLongJump extends CheatModule {
    public ValueFloat speed = new ValueFloat("Speed", 1.5f, 0.1f, 2f);
    public ValueMode verticalMode = new ValueMode("VerticalMode", new String[]{"None", "Normal", "SameY"}, "None");
    public ValueFloat verticalSpeed = new ValueFloat("VerticalSpeed", 0.5f, 0.1f, 2f).visible(() -> verticalMode.is("Normal"));
    public ValueFloat verticalMotion = new ValueFloat("VerticalMotion", 0.01f, 0.01f, 0.6f);
    public ValueBoolean longer = new ValueBoolean("Longer", true).visible(() -> verticalMode.is("Normal") || verticalMode.is("None"));
    public ValueBoolean fakeGround = new ValueBoolean("FakeGround", false).visible(() -> (verticalMode.is("Normal") || verticalMode.is("None")) && longer.getValue());
    public ValueInt longerTick = new ValueInt("LongerTick", 20, 10, 30);
    public ValueBoolean autoDisable = new ValueBoolean("AutoDisable", true);


    private int lastSlot = -1;
    private int ticks = -1;
    private int sameY$ticks = -1;
    private int offGroundTicks = 0;
    private boolean setSpeed;
    private boolean sentPlace;
    private int initTicks;
    private boolean thrown;

    public ModuleLongJump() {
        super("LongJump", Category.MOVEMENT);
    }

    @Override
    public String getSuffix() {
        return "FireBall";
    }

    private int lastExplosionTick;
    private Handler<PacketEvent> packetEventHandler = event -> {
        Packet<?> packet = event.getPacket();
        if (event.getType() == PacketEvent.Type.SEND && packet instanceof C08PacketPlayerBlockPlacement
                && ((C08PacketPlayerBlockPlacement) event.getPacket()).getStack() != null
                && ((C08PacketPlayerBlockPlacement) event.getPacket()).getStack().getItem() instanceof ItemFireball) {
            thrown = true;
            if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.pressed && !verticalMode.is("SameY")) {
                mc.thePlayer.jump();
            }
        }
        if (event.getType() == PacketEvent.Type.RECEIVE) {
            if (mc.thePlayer == null || mc.theWorld == null) {
                return;
            }

            if (packet instanceof S12PacketEntityVelocity) {
                if (((S12PacketEntityVelocity) event.getPacket()).getEntityID() != mc.thePlayer.getEntityId()) {
                    return;
                }
                if (thrown) {
                    ticks = 0;
                    sameY$ticks = 0;
                    setSpeed = true;
                    thrown = false;

                    if (verticalMode.is("SameY"))
                        event.setCancelled(true);
                    lastExplosionTick = mc.thePlayer.ticksExisted;
                }
            } else if (packet instanceof S27PacketExplosion) {
                if (verticalMode.is("SameY"))
                    event.setCancelled(true);
                lastExplosionTick = mc.thePlayer.ticksExisted;
            }
        }
    };

    private Handler<MotionEvent> motionEventHandler = event -> {
        if (event.isPre()) {
            if (mc.thePlayer == null || mc.theWorld == null) {
                return;
            }

            if (mc.thePlayer.onGround)
                offGroundTicks = 0;
            else
                offGroundTicks++;

            switch (initTicks) {
                case 0:
                    int fireballSlot = getFireball();
                    if (fireballSlot != -1 && fireballSlot != mc.thePlayer.inventory.currentItem) {
                        lastSlot = mc.thePlayer.inventory.currentItem;
                        mc.thePlayer.inventory.currentItem = fireballSlot;
                    }
                    event.setYaw(mc.thePlayer.rotationYaw - 180);
                    event.setPitch(71);
                    break;
                case 1:
                    if (!sentPlace) {
                        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()));
                        sentPlace = true;
                    }
                    break;
                case 2:
                    if (lastSlot != -1) {
                        mc.thePlayer.inventory.currentItem = lastSlot;
                        lastSlot = -1;
                    }
                    break;
            }

            if (initTicks < 3) {
                initTicks++;
            }
            if (mc.thePlayer.ticksExisted - lastExplosionTick >= 0) DebugUtil.log("LongJump", "tick = " + (mc.thePlayer.ticksExisted - lastExplosionTick));

            switch (verticalMode.getValue()) {
                case "None":
                case "Normal":
                    if (longer.getValue()) {
                        if(mc.thePlayer.ticksExisted - lastExplosionTick >= 1) {
                            mc.thePlayer.motionY += 0.0278964;
                            if (mc.thePlayer.onGround)
                                toggle();
                            if ((mc.thePlayer.ticksExisted - lastExplosionTick) == longerTick.getValue()) {
                                if (fakeGround.getValue())
                                    event.setGround(true);
                                mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY, 0);
                            }
                        }
                    } else if (ticks > 1) {
                        if (autoDisable.getValue())
                            toggle();
                    }

                    if (setSpeed) {
                        this.setSpeed();
                        ticks++;
                    }

                    if (setSpeed) {
                        if (ticks > 1) {
                            setSpeed = false;
                            ticks = 0;
                            return;
                        }
                        ticks++;
                        setSpeed();
                    }
                    break;
                case "SameY":
                    if (sameY$ticks == -1) break;

                    if (sameY$ticks == 0)
                        PlayerUtils.strafe(speed.getValue());
                    mc.thePlayer.motionY = verticalMotion.getValue();

                    sameY$ticks++;
                    if (sameY$ticks >= 31) {
                        sameY$ticks = -1;
                        if (autoDisable.getValue())
                            toggle();
                    }
                    break;
            }
        }
    };

    @Override
    public void onDisable() {
        if (lastSlot != -1) {
            mc.thePlayer.inventory.currentItem = lastSlot;
        }
        Faiths.moduleManager.getModule(ModuleVelocity.class).setState(true);
        ticks = sameY$ticks = lastSlot = -1;
        setSpeed = sentPlace = false;
        initTicks = 0;
    }

    @Override
    public void onEnable() {
        if (getFireball() == -1) {
            Faiths.notificationManager.pop("Could not find Fireball", "LongJump", NotificationType.INFO);
            return;
        }
        Faiths.moduleManager.getModule(ModuleVelocity.class).setState(false);
        initTicks = 0;
        offGroundTicks = 0;
        lastExplosionTick = Integer.MAX_VALUE;
    }

    private void setSpeed() {
        if (verticalMode.is("Normal"))
            mc.thePlayer.motionY = verticalSpeed.getValue();
        PlayerUtils.strafe(speed.getValue());
    }

    private int getFireball() {
        int a = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null && getStackInSlot.getItem() == Items.fire_charge) {
                a = i;
                break;
            }
        }
        return a;
    }
}
