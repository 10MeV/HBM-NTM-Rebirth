package com.hbm.ntm.block;

import com.hbm.ntm.item.LegacyStateBlockItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConcreteColoredExtBlock extends Block {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 7);

    public ConcreteColoredExtBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable("tile.nospawn").withStyle(ChatFormatting.RED));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return asItem() instanceof LegacyStateBlockItem item
                ? LegacyStateBlockItem.createStack(item, state.getValue(VARIANT))
                : super.getCloneItemStack(level, pos, state);
    }

    public enum Variant {
        MACHINE("machine"),
        MACHINE_STRIPE("machine_stripe"),
        INDIGO("indigo"),
        PURPLE("purple"),
        PINK("pink"),
        HAZARD("hazard"),
        SAND("sand"),
        BRONZE("bronze");

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
            return "concrete_colored_ext." + serializedName;
        }

        public String modelName() {
            return "concrete_colored_ext_" + serializedName;
        }

        public static Variant byLegacyMeta(int meta) {
            if (meta < 0 || meta >= VALUES.length) {
                return MACHINE;
            }
            return VALUES[meta];
        }
    }
}
