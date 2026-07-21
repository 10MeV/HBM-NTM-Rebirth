package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/** Source-backed no-drop {@code plant_dead} carrier used by OilSpot and legacy plant ticks. */
public final class LegacyDeadPlantBlock extends BushBlock {
    public static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);

    public LegacyDeadPlantBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TYPE, Type.BIGFLOWER));
    }

    public BlockState stateFor(Type type) {
        return defaultBlockState().setValue(TYPE, type);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(ModBlocks.WASTE_EARTH.get())
                || isLegacySoil(state, "dirt_dead")
                || isLegacySoil(state, "dirt_oily");
    }

    private static boolean isLegacySoil(BlockState state, String name) {
        return ModBlocks.legacyBlock(name) != null && state.is(ModBlocks.legacyBlock(name).get());
    }

    /** Exact {@code BlockDeadPlant.EnumDeadPlantType} metadata order. */
    public enum Type implements StringRepresentable {
        GENERIC,
        GRASS,
        FLOWER,
        BIGFLOWER,
        FERN;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }
}
