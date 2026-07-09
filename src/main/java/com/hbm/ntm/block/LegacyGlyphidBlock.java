package com.hbm.ntm.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class LegacyGlyphidBlock extends Block {
    public static final IntegerProperty VARIANT = LegacyGlyphidSpawnerBlock.VARIANT;

    public LegacyGlyphidBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 0));
    }

    public static BlockState withLegacyVariant(BlockState state, int variant) {
        return LegacyGlyphidSpawnerBlock.withLegacyVariant(state, variant);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }
}
