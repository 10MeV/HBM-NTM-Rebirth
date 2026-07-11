package com.hbm.ntm.block;

import com.hbm.ntm.item.LegacyStateBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class LegacyCokeBlock extends Block {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 2);

    public LegacyCokeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 5;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 10;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return asItem() instanceof LegacyStateBlockItem item
                ? LegacyStateBlockItem.createStack(item, state.getValue(VARIANT))
                : super.getCloneItemStack(level, pos, state);
    }

    public enum Variant {
        COAL("coal"),
        LIGNITE("lignite"),
        PETROLEUM("petroleum");

        private static final Variant[] VALUES = values();

        private final String serializedName;

        Variant(String serializedName) {
            this.serializedName = serializedName;
        }

        public int legacyMeta() {
            return ordinal();
        }

        public String serializedName() {
            return serializedName;
        }

        public String textureName() {
            return "block_coke." + serializedName;
        }

        public String modelName() {
            return "block_coke_" + serializedName;
        }

        public static Variant byLegacyMeta(int meta) {
            if (meta < 0 || meta >= VALUES.length) {
                return COAL;
            }
            return VALUES[meta];
        }
    }
}
