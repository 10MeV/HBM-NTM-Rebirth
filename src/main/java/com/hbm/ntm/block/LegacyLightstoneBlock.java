package com.hbm.ntm.block;

import com.hbm.ntm.item.LegacyStateBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** Source-backed five-state BlockLightstone replacement. */
public final class LegacyLightstoneBlock extends Block {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 4);

    public LegacyLightstoneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(VARIANT, 0));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return asItem() instanceof LegacyStateBlockItem item
                ? LegacyStateBlockItem.createStack(item, state.getValue(VARIANT))
                : ItemStack.EMPTY;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }
}
