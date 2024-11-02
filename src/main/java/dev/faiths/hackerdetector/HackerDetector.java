package dev.faiths.hackerdetector;

import dev.faiths.hackerdetector.checks.*;
import dev.faiths.hackerdetector.data.BrokenBlock;
import dev.faiths.hackerdetector.data.PlayerDataSamples;
import dev.faiths.hackerdetector.data.TickingBlockMap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.*;

public class HackerDetector {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static HackerDetector INSTANCE;
    private final List<ICheck> checkList = new ArrayList<>();
    private long timeElapsedTemp = 0L;
    private long timeElapsed = 0L;
    private int playersChecked = 0;
    private int playersCheckedTemp = 0;
    /** Data about blocks broken during this tick */
    private final List<BrokenBlock> brokenBlocksList = new ArrayList<>();
    private final TickingBlockMap recentPlacedBlocks = new TickingBlockMap();
    private final Queue<Runnable> scheduledTasks = new ArrayDeque<>();
    public final Set<String> playersToLog = new HashSet<>();


    public HackerDetector() {
        INSTANCE = this;
        this.checkList.add(new AutoblockCheck());
        this.checkList.add(new KeepSprintACheck());
        //this.checkList.add(new KeepSprintBCheck());
        this.checkList.add(new KillAuraACheck(recentPlacedBlocks));
        this.checkList.add(new KillAuraBCheck());
        this.checkList.add(new NoSlowdownCheck());
        this.checkList.add(new ScaffoldCheck());
    }

    public void onTick(int phase) {
        if (phase == 0) {
            this.playersCheckedTemp = 0;
            final long timeStart = System.nanoTime();
            this.onTickStart();
            this.timeElapsedTemp += System.nanoTime() - timeStart;
            return;
        }
        if (phase == 1) {
            final long timeStart = System.nanoTime();
            this.onTickEnd();
            this.timeElapsedTemp += System.nanoTime() - timeStart;
            if (mc.thePlayer != null && mc.thePlayer.ticksExisted % 20 == 0) {
                this.timeElapsed = this.timeElapsedTemp;
                this.timeElapsedTemp = 0L;
            }
            this.playersChecked = this.playersCheckedTemp;
        }
    }

    private void onTickStart() {

        if (mc.theWorld == null || mc.thePlayer == null || !mc.theWorld.isRemote) {
            synchronized (this.scheduledTasks) {
                this.scheduledTasks.clear();
            }
            return;
        }

        final List<EntityPlayer> playerList = new ArrayList<>(mc.theWorld.playerEntities.size());

        for (final EntityPlayer player : mc.theWorld.playerEntities) {
            if (player.ticksExisted >= 20 && !player.isDead && isValidPlayer(player.getUniqueID())) {
                // this includes the watchdog bot above the player
                playerList.add(player);
                player.getPlayerDataSamples().onTickStart();
            }
        }

        synchronized (this.scheduledTasks) {
            while (!this.scheduledTasks.isEmpty()) {
                this.scheduledTasks.poll().run();
            }
        }

        for (final EntityPlayer player : playerList) {
            this.performChecksOnPlayer(player);
        }

    }

    public static boolean isValidPlayer(UUID uuid) {
        final int v = uuid.version();
        return v == 1 || v == 4;
    }

    private void onTickEnd() {
        this.brokenBlocksList.clear();
        this.recentPlacedBlocks.onTick();
    }

    private void performChecksOnPlayer(EntityPlayer player) {
        if (player == mc.thePlayer) {
            return;
        }
        final PlayerDataSamples data = player.getPlayerDataSamples();
        if (data.checkedThisTick) return;
        data.onTick(player);
        for (final ICheck check : this.checkList) {
            check.performCheck(player, data);
        }
        data.onPostChecks();
        this.playersCheckedTemp++;
    }

    public static void addScheduledTask(Runnable runnable) {
        if (runnable == null) return;
        synchronized (INSTANCE.scheduledTasks) {
            INSTANCE.scheduledTasks.add(runnable);
        }
    }

    public static void addBrokenBlock(Block block, BlockPos blockPos, String tool) {
        HackerDetector.INSTANCE.brokenBlocksList.add(new BrokenBlock(block, blockPos, tool));
    }

    public static void addPlacedBlock(BlockPos pos, IBlockState state) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }
        final double xDiff = Math.abs(mc.thePlayer.posX - pos.getX());
        final double zDiff = Math.abs(mc.thePlayer.posZ - pos.getZ());
        if (xDiff > 70D || zDiff > 70D) {
            return;
        }
        if (!state.getBlock().isFullBlock() || !state.getBlock().canCollideCheck(state, false)) {
            return;
        }
        if (mc.theWorld.getBlockState(pos).getBlock().getMaterial() == Material.air) {
            INSTANCE.recentPlacedBlocks.add(pos);
        }
    }

    public static void onPlayerBlockPacket(BlockPos pos, int placedBlockDirectionIn, Block block) {
        if (block == null || !block.isFullBlock() || !block.canCollideCheck(block.getDefaultState(), false)) {
            return;
        }
        final EnumFacing enumfacing = EnumFacing.getFront(placedBlockDirectionIn);
        if (enumfacing == null) return;
        INSTANCE.recentPlacedBlocks.add(pos.add(enumfacing.getDirectionVec()));
    }

}