package dev.faiths.module.render;

import com.google.common.collect.Lists;
import dev.faiths.Faiths;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.PacketEvent;
import dev.faiths.event.impl.Render2DEvent;
import dev.faiths.event.impl.Render3DEvent;
import dev.faiths.event.impl.TickUpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.ui.font.FontManager;
import dev.faiths.utils.MSTimer;
import dev.faiths.utils.render.RenderUtils;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueInt;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleXRay extends CheatModule {
    public static int alpha;
    public static boolean isEnabled;
    public static java.util.List<Integer> blockIdList = Lists.newArrayList(10, 11, 8, 9, 14, 15, 16, 21, 41, 42, 46, 48, 52, 56, 57, 61, 62, 73, 74, 84, 89, 103, 116, 117, 118, 120, 129, 133, 137, 145, 152, 153, 154);
    public static List<BlockPos> blockPosList = new CopyOnWriteArrayList<>();
    private MSTimer timer = new MSTimer();

    Block[] _extreme_var0;

    public static HashMap<BlockPos, Block> blockDataList;

    /*
    @Property("opacity")
    private final IntProperty opacity = PropertyFactory.createInt(160).minimum(0).maximum(255);
    @Property("esp")
    private final BooleanProperty esp = PropertyFactory.booleanTrue();
    @Property("tracers")
    private final BooleanProperty tracers = PropertyFactory.booleanTrue();
    @Property("distance")
    private final IntProperty distance = PropertyFactory.createInt(42).minimum(16).maximum(64);
    @Property("extremerange")
    private static final IntProperty extremerange = PropertyFactory.createInt(4).minimum(0).maximum(8);

    @Property("orerange")
    private final IntProperty orerange = PropertyFactory.createInt(24).minimum(0).maximum(24);
    @Property("chunk-update")
    private final BooleanProperty update = PropertyFactory.booleanFalse();
    @Property("delay")
    private DoubleProperty delay = PropertyFactory.createDouble(10.0).minimum(1.0).maximum(30.0);
     */

    private final ValueInt opacity = new ValueInt("Opacity", 160, 0, 255);
    private final ValueBoolean esp = new ValueBoolean("ESP", true);
    private final ValueBoolean tracers = new ValueBoolean("Tracers", true);
    private final ValueInt distance = new ValueInt("Distance", 42, 16, 64);
    private static final ValueInt extremerange = new ValueInt("ExtremeRange", 4, 0, 8);
    private final ValueInt orerange = new ValueInt("Orerange", 24, 0, 24);
    private final ValueBoolean update = new ValueBoolean("ChunkUpdate", true);
    private final ValueFloat delay = new ValueFloat("Delay", 10f, 1f, 30f);

    public ModuleXRay() {
        super("XRay", Category.RENDER);
        this._extreme_var0 = new Block[] { Blocks.diamond_ore, Blocks.gold_ore, Blocks.iron_ore, Blocks.coal_ore, Blocks.redstone_ore, Blocks.gold_ore };
    }

    @Override
    public void onEnable() {
        onToggle(true);
    }

    @Override
    public void onDisable() {
        onToggle(false);
        timer.reset();
    }

    private void onToggle(boolean enabled) {
        try {
            try {
                ModuleXRay.blockIdList.addAll(blockIdList);
            }
            catch (Exception var3) {
                var3.printStackTrace();
            }
            doExtreme();
            final int posX = (int)mc.thePlayer.posX;
            final int posY = (int)mc.thePlayer.posY;
            final int posZ = (int)mc.thePlayer.posZ;
            mc.renderGlobal.markBlockRangeForRenderUpdate(posX - 900, posY - 900, posZ - 900, posX + 900, posY + 900, posZ + 900);
            blockDataList.clear();
            blockIdList.clear();
            blockPosList.clear();
            mc.renderGlobal.loadRenderers();
            isEnabled = enabled;
        } catch (Exception ignored) {

        }
    }

    public final Handler<TickUpdateEvent> tickUpdateEventHandler = event -> {
        if (alpha != opacity.getValue()) {
            mc.renderGlobal.loadRenderers();
            alpha = opacity.getValue();
            doExtreme();
        } else if (update.getValue()) {
            if (timer.delay(1000 * delay.getValue())) {
                doExtreme();
                this.timer.reset();
            }
        }
    };

    public void doExtreme() {
        for (int var1 = extremerange.getValue(), var2 = -var1; var2 < var1; ++var2) {
            for (int var3 = var1; var3 > -var1; --var3) {
                for (int var4 = -var1; var4 < var1; ++var4) {
                    if (mc.thePlayer.getDistanceSq(mc.thePlayer.posX + var2, mc.thePlayer.posY + var3, mc.thePlayer.posZ + var4) <= orerange.getValue().doubleValue()) {
                        final Block var5 = mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + var2, mc.thePlayer.posY + var3, mc.thePlayer.posZ + var4)).getBlock();
                        boolean var6 = false;
                        final Block[] blockArray;
                        final Block[] var7 = blockArray = this._extreme_var0;
                        for (int n = var7.length, n2 = 0; n2 < n; ++n2) {
                            final Block var8 = blockArray[n2];
                            if (var5.equals(var8)) {
                                var6 = true;
                                break;
                            }
                        }
                        var6 = (var6 && (var5.getBlockHardness(mc.theWorld, BlockPos.ORIGIN) != -1.0f || mc.playerController.isInCreativeMode()));
                        boolean dont = false;
                        for (final BlockPos pos : ModuleXRay.blockPosList) {
                            if (mc.thePlayer.posX + var2 == pos.getX() && mc.thePlayer.posY + var3 == pos.getY() && mc.thePlayer.posZ + var4 == pos.getZ()) {
                                dont = true;
                                break;
                            }
                        }
                        final BlockPos pos2 = new BlockPos(mc.thePlayer.posX + var2, mc.thePlayer.posY + var3, mc.thePlayer.posZ + var4);
                        if (var6 && !dont) {
                            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(pos2, 255, mc.thePlayer.inventory.getCurrentItem(), 0.0F, 0.0F, 0.0F));
                            if (!ModuleXRay.blockPosList.contains(pos2)) {
                                ModuleXRay.blockPosList.add(pos2);
                            }
                        }
                    }
                }
            }
        }
    }

    public final Handler<PacketEvent> packetEventHandler = e -> {
        if (e.getType() == PacketEvent.Type.RECEIVE) {
            if (e.getPacket() instanceof S23PacketBlockChange) {
                final S23PacketBlockChange packet = (S23PacketBlockChange)e.getPacket();
                final BlockPos position = packet.getBlockPosition();
                final IBlockState blockState = packet.getBlockState();
                final Block block = blockState.getBlock();
                if ((block instanceof BlockOre || block instanceof BlockRedstoneOre) && !blockPosList.contains(position)) {
                    blockPosList.add(position);
                    blockDataList.put(position, block);
                }
            }
            if (e.getPacket() instanceof S22PacketMultiBlockChange) {
                final S22PacketMultiBlockChange packet2 = (S22PacketMultiBlockChange)e.getPacket();
                for (final S22PacketMultiBlockChange.BlockUpdateData changedBlock : packet2.getChangedBlocks()) {
                    final BlockPos pos = changedBlock.getPos();
                    final Block block2 = changedBlock.getBlockState().getBlock();
                    if ((block2 instanceof BlockOre || block2 instanceof BlockRedstoneOre) && !blockPosList.contains(pos)) {
                        blockPosList.add(pos);
                        blockDataList.put(pos, block2);
                    }
                }
            }
        }
    };

    public void drawOutlinedString(String str, float x, float y, int internalCol) {
        mc.fontRendererObj.drawString(stripColorCodes(str), x - 0.5f, y, 0x000000, false);
        mc.fontRendererObj.drawString(stripColorCodes(str), x + 0.5f, y, 0x000000, false);
        mc.fontRendererObj.drawString(stripColorCodes(str), x, y - 0.5f, 0x000000, false);
        mc.fontRendererObj.drawString(stripColorCodes(str), x, y + 0.5f, 0x000000, false);
        mc.fontRendererObj.drawString(str, x, y, internalCol, false);
    }

    public String stripColorCodes(String input) {
        return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    private final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-ORX]");
    public final Handler<Render2DEvent> render2DEventHandler = event -> {
        if (update.getValue()) {
            ScaledResolution sc = new ScaledResolution(mc);
            this.timer.delay(1000L * delay.getValue());
            final double elapsedTime = this.timer.getDifference();
            final double percentage = elapsedTime / (1000L * delay.getValue());
            FontManager.sf20.drawString("Refreshing...", sc.getScaledWidth() / 2F - 15, sc.getScaledHeight() / 2F + 30,-1);
            RenderUtils.drawRect(sc.getScaledWidth() / 2F - 47, sc.getScaledHeight() / 2F + 15,94,5,Color.GRAY);
            RenderUtils.drawRect(sc.getScaledWidth() / 2F - 47, sc.getScaledHeight() / 2F + 15,94 * percentage,5,-1);
        }
    };

    public final Handler<Render3DEvent> render3DEventHandler = event -> {
        if (esp.getValue()) {
            for (final BlockPos pos : blockPosList) {
                if (this.getDistance(pos.getX(), pos.getZ()) <= distance.getValue().doubleValue()) {
                    final Block block = mc.theWorld.getBlockState(pos).getBlock();
                    if (block == Blocks.diamond_ore) {
                        this.render3D(pos, 0, 255, 255);
                    }
                    else if (block == Blocks.iron_ore) {
                        this.render3D(pos, 225, 225, 225);
                    }
                    else if (block == Blocks.lapis_ore) {
                        this.render3D(pos, 0, 0, 255);
                    }
                    else if (block == Blocks.redstone_ore) {
                        this.render3D(pos, 255, 0, 0);
                    }
                    else if (block == Blocks.coal_ore) {
                        this.render3D(pos, 0, 30, 30);
                    }
                    else if (block == Blocks.emerald_ore) {
                        this.render3D(pos, 0, 255, 0);
                    }
                    else {
                        if (block != Blocks.gold_ore) {
                            continue;
                        }
                        this.render3D(pos, 255, 255, 0);
                    }
                }
            }
        }
    };

    private void render3D(BlockPos pos, int red, int green, int blue) {
        if (esp.getValue()) {
            RenderUtils.drawBlockBox(pos, new Color(red, green, blue), true);
        }
    }

    public double getDistance(final double x, final double z) {
        final double d0 = mc.thePlayer.posX - x;
        final double d2 = mc.thePlayer.posZ - z;
        return MathHelper.sqrt_double(d0 * d0 + d2 * d2);
    }


    public static boolean showESP() {
        return Faiths.moduleManager.getModule(ModuleXRay.class).esp.getValue();
    }

    public static int getDistance() {
        return Faiths.moduleManager.getModule(ModuleXRay.class).distance.getValue();
    }
}
