package com.hbm.ntm.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class LegacyGlyphidSpawnerBlock extends Block {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 2);

    public LegacyGlyphidSpawnerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 0));
    }

    public static BlockState withLegacyVariant(BlockState state, int variant) {
        return state.hasProperty(VARIANT) ? state.setValue(VARIANT, Math.max(0, Math.min(2, variant))) : state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }
}
