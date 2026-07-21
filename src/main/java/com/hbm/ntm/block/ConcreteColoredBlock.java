package com.hbm.ntm.block;

import com.hbm.ntm.item.LegacyStateBlockItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;

/**
 * Modern state carrier for 1.7.10 BlockConcreteColored metadata 0..15.
 * The metadata order is deliberately the old inverted-dye order, not the
 * vanilla dye enum order.
 */
public class ConcreteColoredBlock extends Block {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 15);

    public ConcreteColoredBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public ItemStack getCloneItemStack(net.minecraft.world.level.BlockGetter level, BlockPos pos, BlockState state) {
        return asItem() instanceof LegacyStateBlockItem item
                ? LegacyStateBlockItem.createStack(item, state.getValue(VARIANT))
                : super.getCloneItemStack(level, pos, state);
    }

    public enum Variant {
        WHITE("white", MapColor.SNOW),
        ORANGE("orange", MapColor.COLOR_ORANGE),
        MAGENTA("magenta", MapColor.COLOR_MAGENTA),
        LIGHT_BLUE("light_blue", MapColor.COLOR_LIGHT_BLUE),
        YELLOW("yellow", MapColor.COLOR_YELLOW),
        LIME("lime", MapColor.COLOR_LIGHT_GREEN),
        PINK("pink", MapColor.COLOR_PINK),
        GRAY("gray", MapColor.COLOR_GRAY),
        SILVER("silver", MapColor.COLOR_LIGHT_GRAY),
        CYAN("cyan", MapColor.COLOR_CYAN),
        PURPLE("purple", MapColor.COLOR_PURPLE),
        BLUE("blue", MapColor.COLOR_BLUE),
        BROWN("brown", MapColor.COLOR_BROWN),
        GREEN("green", MapColor.COLOR_GREEN),
        RED("red", MapColor.COLOR_RED),
        BLACK("black", MapColor.COLOR_BLACK);

        private static final Variant[] VALUES = values();

        private final String serializedName;
        private final MapColor mapColor;

        Variant(String serializedName, MapColor mapColor) {
            this.serializedName = serializedName;
            this.mapColor = mapColor;
        }

        public int legacyMeta() {
            return ordinal();
        }

        public String serializedName() {
            return serializedName;
        }

        public String modelName() {
            return "concrete_colored_" + serializedName;
        }

        public String textureName() {
            return "concrete_" + serializedName;
        }

        public MapColor mapColor() {
            return mapColor;
        }

        public static Variant byLegacyMeta(int meta) {
            if (meta < 0 || meta >= VALUES.length) {
                return WHITE;
            }
            return VALUES[meta];
        }
    }
}
