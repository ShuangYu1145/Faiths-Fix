package dev.faiths.module.world;

import dev.faiths.event.Handler;
import dev.faiths.event.impl.MotionEvent;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.event.impl.TickUpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.player.PlayerUtils;
import dev.faiths.utils.player.RotationUtils;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.value.ValueInt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDirectional;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleBedBreaker extends CheatModule {
    public static double currentDamage;
    private BlockPos currentPos;
    private int bestToolSlot = -1;
    public static boolean skipAb = false;

    static BlockPos whiteListed = new BlockPos(0, 0, 0);
    private final List<BlockPos> targets = new ArrayList<>();
    private final ValueInt radius = new ValueInt("Radius", 4, 3, 5);

    public ModuleBedBreaker() {
        super("BedBreaker", Category.WORLD);
    }

    protected final Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos((float) (Math.toRadians(-yaw) - (float) Math.PI));
        float f1 = MathHelper.sin((float) (Math.toRadians(-yaw) - (float) Math.PI));
        float f2 = -MathHelper.cos((float) Math.toRadians(-pitch));
        float f3 = MathHelper.sin((float) Math.toRadians(-pitch));
        return new Vec3(f1 * f2, f3, f * f2);
    }

    public Vec3 getPositionEyes(float partialTicks) {
        return new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
    }

    private boolean bedAround() {
        for (int x = -radius.getValue(); x < radius.getValue() + 1; x++) {
            for (int z = -radius.getValue(); z < radius.getValue() + 1; z++) {
                for (int y = -3; y < 5; y++) {
                    BlockPos pos = new BlockPos(mc.thePlayer.posX - x, mc.thePlayer.posY + y, mc.thePlayer.posZ - z);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    if (!isWhitelisted(pos) && mc.theWorld.getBlockState(pos).getBlock() == Blocks.bed && mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private final Handler<TickUpdateEvent> tickUpdateEventHandler = event -> {
        if (currentPos != null) {
            if (currentDamage == 0) {
                bestToolSlot = mc.thePlayer.getBestToolSlot(currentPos);
                if (bestToolSlot != -1 && bestToolSlot != mc.thePlayer.inventory.currentItem) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(bestToolSlot));
                }
                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, currentPos, EnumFacing.UP));
                skipAb = true;
                if (bestToolSlot != -1 && bestToolSlot != mc.thePlayer.inventory.currentItem) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
            }

            Block block = mc.theWorld.getBlockState(currentPos).getBlock();
            currentDamage += PlayerUtils.getPlayerRelativeBlockHardness(block, currentPos, (bestToolSlot==-1?mc.thePlayer.inventory.currentItem:bestToolSlot));

            if (this.currentDamage >= 1.1F) {
                if (bestToolSlot != -1 && bestToolSlot != mc.thePlayer.inventory.currentItem) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(bestToolSlot));
                }
                mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentPos, EnumFacing.UP));
                mc.playerController.onPlayerDestroyBlock(currentPos, EnumFacing.UP);
                this.currentDamage = 0.0F;
                currentPos = null;
                if (bestToolSlot != -1 && bestToolSlot != mc.thePlayer.inventory.currentItem) {
                    mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
            }

            mc.thePlayer.swingItem();
            mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), currentPos, (int) (this.currentDamage * 10.0F) - 1);
        }
    };
    
    private final Handler<MotionEvent> motionEventHandler = event -> {
        if (currentPos != null) {
            if (!bedAround()) {
                currentPos = null;
                currentDamage = 0;
            }
            Vector2f rotations = RotationUtils.getRotations(currentPos.getX() + 0.5, currentPos.getY() + 0.5, currentPos.getZ() + 0.5);
            if (currentDamage <= 0.1 || currentDamage >= 0.9) {
                event.setYaw(rotations.x);
                event.setPitch(rotations.y);
                mc.thePlayer.rotationYawHead = rotations.x;
                mc.thePlayer.renderArmPitch = rotations.y;
                skipAb = true;
            }
        } else {
            for (int x = -radius.getValue(); x < radius.getValue() + 1; x++) {
                for (int z = -radius.getValue(); z < radius.getValue() + 1; z++) {
                    for (int y = -3; y < 5; y++) {
                        BlockPos pos = new BlockPos(mc.thePlayer.posX - x, mc.thePlayer.posY + y, mc.thePlayer.posZ - z);
                        Block block = mc.theWorld.getBlockState(pos).getBlock();
                        if (!isWhitelisted(pos) && mc.theWorld.getBlockState(pos).getBlock() ==
                                Blocks.bed && mc.theWorld.getBlockState(pos).getValue(BlockBed.PART) ==
                                BlockBed.EnumPartType.HEAD) {
                            List<Block> breakQueue = Arrays.asList(Blocks.stained_glass, Blocks.wool, Blocks.stained_hardened_clay, Blocks.planks, Blocks.log, Blocks.log2, Blocks.end_stone, Blocks.obsidian, Blocks.bed);
                            targets.clear();
                            if(mc.theWorld.getBlockState(pos).getValue(BlockDirectional.FACING) == EnumFacing.EAST){
                                targets.add(pos.east());
                                targets.add(pos.west().west());
                                targets.add(pos.north().west());
                                targets.add(pos.north());
                                targets.add(pos.south());
                                targets.add(pos.south().west());
                                targets.add(pos.up().west());
                                targets.add(pos.up());
                            }
                            if(mc.theWorld.getBlockState(pos).getValue(BlockDirectional.FACING) == EnumFacing.WEST){
                                targets.add(pos.west());
                                targets.add(pos.east().east());
                                targets.add(pos.north().east());
                                targets.add(pos.north());
                                targets.add(pos.south());
                                targets.add(pos.south().east());
                                targets.add(pos.up().east());
                                targets.add(pos.up());
                            }
                            if(mc.theWorld.getBlockState(pos).getValue(BlockDirectional.FACING) == EnumFacing.NORTH){
                                targets.add(pos.north());
                                targets.add(pos.south().south());
                                targets.add(pos.east());
                                targets.add(pos.west());
                                targets.add(pos.west().south());
                                targets.add(pos.south().east());
                                targets.add(pos.up().south());
                                targets.add(pos.up());
                            }
                            if(mc.theWorld.getBlockState(pos).getValue(BlockDirectional.FACING) == EnumFacing.SOUTH){
                                targets.add(pos.south());
                                targets.add(pos.north().north());
                                targets.add(pos.east());
                                targets.add(pos.west());
                                targets.add(pos.west().north());
                                targets.add(pos.north().east());
                                targets.add(pos.up().north());
                                targets.add(pos.up());
                            }
                            for(BlockPos blockPos : targets){
                                if(mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.air)
                                {
                                    currentPos = pos;
                                    currentDamage = 0;
                                    return;
                                }
                            }
                            targets.sort(Comparator.comparingInt(p -> {
                                Block blockAtPos = mc.theWorld.getBlockState(p).getBlock();
                                int index = breakQueue.indexOf(blockAtPos);
                                return index == -1 ? Integer.MAX_VALUE : index;
                            }));
                            currentPos = targets.get(0);
                            currentDamage = 0;

                        }
                    }
                }
            }
        }
    };

    private final Handler<Render2DEvent> render2DEventHandler = event -> {
        if (currentDamage != 0) {
            ScaledResolution sc = new ScaledResolution(mc);
            final double percentage = currentDamage;
            FontManager.sf20.drawString("Breaking...", sc.getScaledWidth() / 2F - 15, sc.getScaledHeight() / 2F + 30,-1);
            RenderUtils.drawRect(sc.getScaledWidth() / 2F - 47, sc.getScaledHeight() / 2F + 15,94,5, Color.GRAY);
            RenderUtils.drawRect(sc.getScaledWidth() / 2F - 47, sc.getScaledHeight() / 2F + 15,94 * percentage,5,-1);
        }
    };


    public BlockPos getWhiteListed() {
        return whiteListed;
    }

    public static void setWhiteListed(BlockPos _whiteListed) {
        whiteListed = _whiteListed;
    }

    private boolean isWhitelisted(BlockPos pos) {
        if (pos == null || whiteListed == null) return false;
        return pos.getX() == whiteListed.getX() && pos.getY() == whiteListed.getY() && pos.getZ() == whiteListed.getZ();
    }
}
