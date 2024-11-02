package dev.faiths.module.world;

import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.UpdateEvent;
import dev.faiths.event.impl.WorldEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.module.combat.ModuleKillAura;
import dev.faiths.utils.MSTimer;
import dev.faiths.utils.player.Rotation;
import dev.faiths.utils.player.RotationUtils;
import dev.faiths.value.ValueBoolean;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.ArrayList;

import static dev.faiths.module.world.ModuleScaffold.getVec3;
import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleContainerAura extends CheatModule {
    public ModuleContainerAura() {
        super("ContainerAura", Category.WORLD);
    }

    public final ValueBoolean thoughtWall = new ValueBoolean("ThoughtWall", false);
    public final ValueBoolean chestOnly = new ValueBoolean("ChestOnly", false);

    private Rotation needRot;
    private EnumFacing needFacing;
    private MSTimer msTimer = new MSTimer();

    public static ArrayList<BlockPos> openedContainer = new ArrayList<>();

    @Override
    public void onEnable() {
        openedContainer.clear();
    }

    private Handler<WorldEvent> worldEventHandler = event -> {
        openedContainer.clear();
    };

    private Handler<UpdateEvent> scannerHandler = event -> {
        try {
            if (Faiths.moduleManager.getModule(ModuleKillAura.class).getState() && ModuleKillAura.target != null) return;
            if (Faiths.moduleManager.getModule(ModuleScaffold.class).getState()) return;
            BlockPos nearestContainer = null;
            double nearestDistance = Double.MAX_VALUE;
            if (mc.currentScreen instanceof GuiContainer) return;
            for (int x = -5; x < 6; x++) {
                for (int y = -5; y < 6; y++) {
                    for (int z = -5; z < 6; z++) {
                        BlockPos fixedBP = new BlockPos(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z);
                        if (checkContainerOpenable(fixedBP)) {
                            MovingObjectPosition mop = mc.theWorld.rayTraceBlocks(
                                    new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ),
                                    new Vec3(fixedBP).add(new Vec3(0.5, 0.5, 0.5)),
                                    false, true, true);
                            if (mc.thePlayer.getDistance(fixedBP.getX(), fixedBP.getY(), fixedBP.getZ()) < 4.5 &&
                                    !openedContainer.contains(fixedBP) && mc.thePlayer.getDistance(fixedBP.getX(), fixedBP.getY(), fixedBP.getZ()) <= nearestDistance &&
                                    isContainer(mc.theWorld.getBlockState(fixedBP).getBlock()) && mop != null &&
                                    mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && (mop.getBlockPos().equals(fixedBP) || thoughtWall.getValue())) {
                                nearestDistance = mc.thePlayer.getDistance(fixedBP.getX(), fixedBP.getY(), fixedBP.getZ());
                                nearestContainer = fixedBP;
                                float[] r = RotationUtils.getRotationBlock(fixedBP);
                                needRot = new Rotation(r[0], r[1]);
                                needFacing = mop.sideHit;
                            }
                        }
                    }
                }
            }

            if (nearestContainer == null) return;
            if (!msTimer.check(500L)) return;
            msTimer.reset();
            Faiths.INSTANCE.getRotationManager().setRotation(needRot, 180f, true);
            mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), nearestContainer, needFacing, getVec3(nearestContainer, needFacing));
            openedContainer.add(nearestContainer);
        } catch (Throwable e) {}
    };

    private boolean isContainer(Block block) {
        return block instanceof BlockChest || ((block instanceof BlockFurnace || block instanceof BlockBrewingStand) && !chestOnly.getValue());
    }

    private boolean checkContainerOpenable(BlockPos blockPos) {
        IBlockState blockState = mc.theWorld.getBlockState(blockPos);
        if (!(blockState.getBlock() instanceof BlockChest)) return true;
        IBlockState upBlockState = mc.theWorld.getBlockState(blockPos.add(0, 1, 0));
        if (upBlockState.getBlock().isFullBlock() && !(upBlockState.getBlock() instanceof BlockGlass)) return false;
        return true;
    }
}
