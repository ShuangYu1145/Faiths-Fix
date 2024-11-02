package dev.faiths.module.combat;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.LivingUpdateEvent;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.DebugUtil;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueInt;
import dev.faiths.value.ValueMode;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemFood;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.vialoadingbase.ViaLoadingBase;
import net.viamcp.fixes.AttackOrder;

import javax.vecmath.Vector2d;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleVelocity extends CheatModule {
    public ModuleVelocity() {
        super("Velocity", Category.COMBAT);
    }

    private ValueMode mode = new ValueMode("Mode", new String[]{"Cancel", "Watchdog","WatchDog2", "Grim"}, "Watchdog");
    private ValueInt horizontal = new ValueInt("Horizontal", 0, 0, 100).visible(() -> mode.is("Cancel"));
    private ValueInt vertical = new ValueInt("Vertical", 0, 0, 100).visible(() -> mode.is("Cancel"));
    private ValueBoolean explosion = new ValueBoolean("Explosion",true).visible(() -> !mode.is("Grim"));
    private ValueBoolean lagbackCheck = new ValueBoolean("Lagback",true).visible(() -> mode.is("Watchdog"));
    private ValueBoolean debug = new ValueBoolean("Debug",false);
    private ValueBoolean nousingitem = new ValueBoolean("NoConsumable",false).visible(() -> mode.is("Grim"));
    public boolean velocityInput;
    private boolean attacked;
    private double reduceXZ;
    public float velocityYaw;
    private int lastVelocityTick = 0;
    private int lagbackTimes = 0;
    private long lastLagbackTime = System.currentTimeMillis();

    @Override
    public String getSuffix() {
        return mode.getValue();
    }

    private final Handler<LivingUpdateEvent> livingUpdateEventHandler = event -> {
        if(mode.is("Grim")){
                if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() > 47) {
                    if (velocityInput) {
                        if (attacked) {
                            mc.thePlayer.motionX *= reduceXZ;
                            mc.thePlayer.motionZ *= reduceXZ;
                            attacked = false;
                        }
                        if (mc.thePlayer.hurtTime == 0) {
                            velocityInput = false;
                        }
                    }
                } else {
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                        mc.thePlayer.addVelocity(-1.3E-10, -1.3E-10, -1.3E-10);
                        mc.thePlayer.setSprinting(false);
                    }
                }
        }
    };

    private final Handler<PacketEvent> packetHandler = event -> {
        if (event.getType() == PacketEvent.Type.RECEIVE) {
            final Packet<?> packet = event.getPacket();

            if (packet instanceof S12PacketEntityVelocity) {
                if (mode.is("Cancel")) {
                    if (((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId()) {
                        if (horizontal.getValue().equals(0) && vertical.getValue().equals(0)) {
                            event.setCancelled(true);
                        } else {
                            ((S12PacketEntityVelocity) packet).setMotionX(((S12PacketEntityVelocity) packet).getMotionX() * horizontal.getValue() / 100);
                            ((S12PacketEntityVelocity) packet).setMotionY(((S12PacketEntityVelocity) packet).getMotionY() * vertical.getValue() / 100);
                            ((S12PacketEntityVelocity) packet).setMotionZ(((S12PacketEntityVelocity) packet).getMotionZ() * horizontal.getValue() / 100);
                        }
                    }
                }

                if(mode.is("Watchdog")){
                    if (((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId()) {
                        lastVelocityTick = mc.thePlayer.ticksExisted;
                        event.setCancelled(true);
                        if (mc.thePlayer.onGround || ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0D < .2 || ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0D > .41995) {
                            mc.thePlayer.motionY = ((S12PacketEntityVelocity) packet).getMotionY() / 8000.0D;
                        }
                        if (debug.getValue()) {
                            DebugUtil.log("§cKnockback tick: " + mc.thePlayer.ticksExisted);
                        }
                    }
                }

                if(mode.is("WatchDog2") && ((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId()){
                    // Check if this is a regular velocity update
                        if (mc.thePlayer.onGround) {
                            ((S12PacketEntityVelocity) packet).setMotionX((int) (mc.thePlayer.motionX * 8000));
                            ((S12PacketEntityVelocity) packet).setMotionZ((int) (mc.thePlayer.motionZ * 8000));
                        } else {
                            event.setCancelled(true);
                        }
                    }

                if(mode.is("Grim")){
                    if(nousingitem.getValue() && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && mc.thePlayer.isUsingItem())return;

                    if (((S12PacketEntityVelocity) packet).getEntityID() == mc.thePlayer.getEntityId()) {
                        double x = ((S12PacketEntityVelocity) packet).getMotionX() / 8000D;
                        double z = ((S12PacketEntityVelocity) packet).getMotionZ() / 8000D;
                        float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90;
                        double speed = Math.sqrt(x * x + z * z);
                        float yawDiff = Math.abs(Faiths.INSTANCE.getRotationManager().getAngleDifference(yaw, mc.thePlayer.rotationYaw));
                        double horizontalStrength = new Vector2d(((S12PacketEntityVelocity) packet).getMotionX(), ((S12PacketEntityVelocity) packet).getMotionZ()).length();
                        if (horizontalStrength <= 1000) return;
                        velocityInput = true;
                        Entity entity = null;
                        reduceXZ = 0;

                        Entity target = ModuleKillAura.target;
                        if (target != null && ModuleKillAura.shouldAttack()) {
                            entity = ModuleKillAura.target;
                        }

                        boolean state = mc.thePlayer.serverSprintState;

                        if (entity != null) {
                            if (!state) {
                                mc.skipTicks = 1;
                                mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer(mc.thePlayer.onGround));
                                mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                            }
                            int count = 12;
                            for (int i = 1; i <= count; i++) {
                                AttackOrder.sendFixedAttack(mc.thePlayer, entity);
                            }
                            if (!state) {
                                mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                            }
                            attacked = true;
                            reduceXZ = 0.07776;
                            velocityYaw = yaw;
                            if (debug.getValue()) {
                                DebugUtil.log(true,"Yaw: " + Math.round(velocityYaw * 100) / 100f + ", Diff: " + Math.round(yawDiff * 100) / 100f + ", Speed: " + Math.round(speed * 100) / 100f);
                            }
                        }
                    }
                }
            }

            if (packet instanceof S27PacketExplosion) {
                S27PacketExplosion wrappedPacket = ((S27PacketExplosion) packet);
                if(!mode.is("Grim") && explosion.getValue() && (wrappedPacket.func_149149_c() >= 0.02 || wrappedPacket.func_149144_d() >= 0.02 || wrappedPacket.func_149147_e() >= 0.02)) {
                    event.setCancelled(true);
                    lastVelocityTick = mc.thePlayer.ticksExisted;
                    if (debug.getValue()) {
                       DebugUtil.log("§cRecevied explosion packet");
                    }
                }
            }

            if (packet instanceof S08PacketPlayerPosLook && mode.is("Watchdog") && mc.thePlayer.ticksExisted >= 40 && mc.thePlayer.ticksExisted - lastVelocityTick <= 20) {
                if (System.currentTimeMillis() - lastLagbackTime <= 4000) {
                    lagbackTimes += 1;
                } else {
                    lagbackTimes = 1;
                }
                lastLagbackTime = System.currentTimeMillis();
            }
        }
    };
}
