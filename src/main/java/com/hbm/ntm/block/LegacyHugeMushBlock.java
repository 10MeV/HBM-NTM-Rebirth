package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyHugeMushBlock extends Block {
    public LegacyHugeMushBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(ModBlocks.MUSH.get());
    }
}
