package com.github.stachelbeere1248.zombiesutils.game.enums;

import com.github.stachelbeere1248.zombiesutils.utils.Scoreboard;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public enum Map {
    DEAD_END, BAD_BLOOD, ALIEN_ARCADIUM, PRISON;

    public static Optional<Map> getMap() {
        World world = Minecraft.getMinecraft().theWorld;
        BlockPos pos = new BlockPos(44,71,0);
        if (!world.isBlockLoaded(pos) || Scoreboard.isNotZombies()) return Optional.empty();
        Block block = world.getBlockState(pos).getBlock();

        if (block.equals(Blocks.air)) return Optional.of(Map.DEAD_END);
        if (block.equals(Blocks.wool)) return Optional.of(Map.BAD_BLOOD);
        if (block.equals(Blocks.stone_slab)) return Optional.of(Map.ALIEN_ARCADIUM);
        if (block.equals(Blocks.wooden_slab)) return Optional.of(Map.PRISON);

        else return Optional.empty();
    }
}
