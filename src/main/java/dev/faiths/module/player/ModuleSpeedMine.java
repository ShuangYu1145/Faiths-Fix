package dev.faiths.module.player;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.TickUpdateEvent;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueInt;
import dev.faiths.value.ValueMode;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleSpeedMine extends CheatModule {
    public ValueMode mode = new ValueMode("Mode", new String[]{"Packet", "Percentage"}, "Percentage");
    private ValueInt percent = new ValueInt("Percent",10,1,100).visible(()->mode.is("Percentage"));
    private ValueFloat speed = new ValueFloat("Speed",1.1f,1f,3f).visible(()->mode.is("Packet"));
    public ModuleSpeedMine() {
        super("SpeedMine", Category.PLAYER);
    }
    private EnumFacing facing;
    private BlockPos pos;
    private boolean boost = false;
    private float damage = 0.0F;
    float f;

    @Override
    public String getSuffix() {
        return mode.is("Percentage") ? percent.getValue() + "%" : mode.getValue();
    }


    private final Handler<TickUpdateEvent> tickUpdateEventHandler = event -> {
        if(mode.is("Percentage")) {
            if (mc.playerController.isInCreativeMode()) {
                return;
            }
            if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                return;
            }
            mc.playerController.blockHitDelay = 0;
            if (mc.playerController.getIsHittingBlock() && mc.playerController.curBlockDamageMP < (f = 0.3f * percent.getValue() / 100.0f)) {
                mc.playerController.curBlockDamageMP = f;
            }
        }
    };

    private final Handler<PacketEvent> packetHandler = event -> {
        if(event.getType() == PacketEvent.Type.SEND) {
            if (mode.is("Packet")) {
                if (event.getPacket() instanceof C07PacketPlayerDigging) {
                    if (((C07PacketPlayerDigging) event.getPacket()).getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        boost = true;
                        pos = ((C07PacketPlayerDigging) event.getPacket()).getPosition();
                        facing = ((C07PacketPlayerDigging) event.getPacket()).getFacing();
                        damage = 0.0F;
                    } else if ((((C07PacketPlayerDigging) event.getPacket()).getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK)
                            || (((C07PacketPlayerDigging) event.getPacket()).getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK)) {
                        boost = false;
                        pos = null;
                        facing = null;
                    }
                }
            }
        }
    };

    private final Handler<UpdateEvent> updateEventHandler = event -> {
        if (mode.is("Packet")) {
            if (mc.playerController.extendedReach()) {
                mc.playerController.blockHitDelay = 0;
            } else if (pos != null && boost) {
                IBlockState blockState = mc.theWorld.getBlockState(pos);
                damage += blockState.getBlock().getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, pos) * speed.getValue();
                if (damage >= 1.0F) {
                    mc.theWorld.setBlockState(pos, Blocks.air.getDefaultState(), 11);
                    mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, pos, facing));
                    mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, facing));
                    damage = 0.0F;
                    boost = false;
                }
            }
        }
    };
}
