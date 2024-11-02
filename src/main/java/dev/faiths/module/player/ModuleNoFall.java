package dev.faiths.module.player;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.world.ModuleHypixelUtils;
import dev.faiths.utils.DebugUtil;
import dev.faiths.utils.ServerUtils;
import dev.faiths.utils.Servers;
import dev.faiths.utils.TimerUtil;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.value.ValueInt;
import dev.faiths.value.ValueMode;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S39PacketPlayerAbilities;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleNoFall extends CheatModule {
    public ValueMode mode = new ValueMode("Mode", new String[]{"Packet"}, "Packet");
    public ValueInt delay = new ValueInt("Delay", 100, 0, 1000).visible(() -> mode.is("Packet"));
    public boolean flag = false;
    public double lastFallDistance = 0;
    public long lastIllegalTeleport = System.currentTimeMillis();

    public ModuleNoFall() {
        super("NoFall",Category.PLAYER);
    }
    private TimerUtil timer = new TimerUtil();

    public static boolean bbCheck() {
        return bbCheck(0.0);
    }

    public static boolean bbCheck(double d) {
        double d2;
        AxisAlignedBB axisAlignedBB = mc.thePlayer.getEntityBoundingBox();
        for (d2 = 0.0; d2 < d; d2 += (double)mc.thePlayer.height) {
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, axisAlignedBB.offset(0.0, -d2, 0.0)).isEmpty()) continue;
            return false;
        }
        for (d2 = 0.0; d2 < axisAlignedBB.minY; d2 += (double)mc.thePlayer.height) {
            if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, axisAlignedBB.offset(0.0, -d2, 0.0)).isEmpty()) continue;
            return true;
        }
        return false;
    }

    public static boolean damageCheck(double fallDistance) {
        PotionEffect potionEffect = mc.thePlayer.getActivePotionEffect(Potion.jump);
        float f = potionEffect != null ? (float)(potionEffect.getAmplifier() + 1) : 0.0f;
        return MathHelper.ceiling_float_int((float)(fallDistance - 3.0f - f)) > 0;
    }
    public static boolean isInVoid() {
        for (int i = 0; i <= 128; i++) {
            if (PlayerUtils.isOnGround(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getSuffix() {
        if (Math.abs(System.currentTimeMillis() - lastIllegalTeleport) < 10000) {
            return "Disabled, left " + (10000 - Math.abs(System.currentTimeMillis() - lastIllegalTeleport)) + "ms";
        }
        return mode.getValue();
    }

    private final Handler<MotionEvent> eventHandler = event -> {
        if (Math.abs(System.currentTimeMillis() - lastIllegalTeleport) < 10000) return;
        if (event.isPre() && mode.is("Jump") && ServerUtils.isHypixel()) {
            if (!mc.thePlayer.onGround) {
                lastFallDistance = mc.thePlayer.fallDistance;
            } else {
                if (!flag && damageCheck(lastFallDistance)) {
                    event.setGround(false);
                    if (!mc.gameSettings.keyBindJump.isKeyDown()) mc.thePlayer.jump();
                    flag = true;
                } else {
                    flag = false;
                    lastFallDistance = 0;
                }
            }
        }
    };
    
    private final Handler<PacketEvent> packetHandler = event -> {
        if(mc.thePlayer == null || mc.theWorld == null)return;
        if(event.getType() == PacketEvent.Type.SEND && mode.is("Packet") && ServerUtils.isHypixel()){
            if(isInVoid())return;
            if (Math.abs(System.currentTimeMillis() - lastIllegalTeleport) < 10000) return;

            Packet<?> packet = event.getPacket();

            if(packet instanceof C03PacketPlayer){
                if (((C03PacketPlayer) packet).isOnGround() || !damageCheck(mc.thePlayer.fallDistance) || !bbCheck() || !this.timer.delay(this.delay.getValue().longValue()))
                    return;
                timer.reset();
                mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer(true));
                mc.thePlayer.fallDistance = 0.0f;
            }
        }
        if (event.getType() == PacketEvent.Type.RECEIVE && ServerUtils.isHypixel()) {
            if (mc.thePlayer == null) return;
            if (event.getPacket() instanceof S08PacketPlayerPosLook &&
                    damageCheck(((S08PacketPlayerPosLook) event.getPacket()).getY() - mc.thePlayer.posY)) {
                if (isInVoid()) return;
                if (mc.thePlayer.capabilities.allowFlying) return;
                if (mc.thePlayer.ticksExisted <= 20) return;
                if (ModuleHypixelUtils.getCurrentServer() == Servers.NONE || ModuleHypixelUtils.getCurrentServer() == Servers.PRE) return;
                DebugUtil.log("NoFall", EnumChatFormatting.RED + "Unusual Teleport! Maybe staff coming! Disable nofall 10000ms! Current server: " + ModuleHypixelUtils.getCurrentServer().name());
                lastIllegalTeleport = System.currentTimeMillis();
            }
            if (event.getPacket() instanceof S39PacketPlayerAbilities && ((S39PacketPlayerAbilities) event.getPacket()).isAllowFlying()) {
                lastIllegalTeleport = 0L;
            }
        }
    };
}
