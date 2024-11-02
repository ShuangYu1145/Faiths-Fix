package dev.faiths.module.movement;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.BlockUtil;
import dev.faiths.value.ValueMode;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.Map;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleNoWeb extends CheatModule {
    private final ValueMode modeValue = new ValueMode("Mode", new String[]{"Vanilla", "Grim"}, "Grim");

    public ModuleNoWeb() {
        super("NoWeb", Category.MOVEMENT);
    }

    @Override
    public String getSuffix() {
        return modeValue.getValue();
    }

    private final Handler<MotionEvent> motionEventHandler = event -> {
        if (event.isPost()) return;

        if (!mc.thePlayer.isInWeb) {
            return;
        }

        switch (modeValue.getValue()) {
            case "Vanilla":
                mc.thePlayer.isInWeb = false;
                break;
            case "Grim":
                Map<BlockPos, Block> searchBlock = BlockUtil.searchBlocks(2);
                for (Map.Entry<BlockPos, Block> block : searchBlock.entrySet()) {
                    if (mc.theWorld.getBlockState(block.getKey()).getBlock() instanceof BlockWeb) {
                        mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                        mc.getNetHandler().getNetworkManager().sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, block.getKey(), EnumFacing.DOWN));
                    }
                }
                mc.thePlayer.isInWeb = false;
                break;
        }
    };
}