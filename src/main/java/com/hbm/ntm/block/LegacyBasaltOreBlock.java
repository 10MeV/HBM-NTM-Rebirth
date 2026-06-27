package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.registries.RegistryObject;

public class LegacyBasaltOreBlock extends Block {
    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 4);

    public LegacyBasaltOreBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(VARIANT, 0));
    }

    public BlockState stateForVariant(int variant) {
        return defaultBlockState().setValue(VARIANT, Variant.byLegacyMeta(variant).legacyMeta());
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return asItem().getDefaultInstance();
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide && Variant.byLegacyMeta(state.getValue(VARIANT)) == Variant.ASBESTOS
                && level.getBlockState(pos.above()).isAir() && level.random.nextInt(10) == 0) {
            level.setBlock(pos.above(), ModBlocks.GAS_ASBESTOS.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        if (level.isClientSide && Variant.byLegacyMeta(state.getValue(VARIANT)) == Variant.ASBESTOS) {
            for (int i = 0; i < 5; i++) {
                level.addParticle(ParticleTypes.MYCELIUM,
                        pos.getX() + level.random.nextFloat(),
                        pos.getY() + 1.1D,
                        pos.getZ() + level.random.nextFloat(),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(VARIANT);
    }

    public enum Variant {
        SULFUR("sulfur", "sulfur"),
        FLUORITE("fluorite", "fluorite"),
        ASBESTOS("asbestos", "ingot_asbestos"),
        GEM("gem", "gem_volcanic"),
        MOLYSITE("molysite", "powder_molysite");

        private static final Variant[] VALUES = values();

        private final String serializedName;
        private final String dropItemName;

        Variant(String serializedName, String dropItemName) {
            this.serializedName = serializedName;
            this.dropItemName = dropItemName;
        }

        public int legacyMeta() {
            return ordinal();
        }

        public String textureName() {
            return "ore_basalt_" + serializedName;
        }

        public Item droppedItem() {
            RegistryObject<Item> item = ModItems.legacyItem(dropItemName);
            return item == null ? null : item.get();
        }

        public String getSerializedName() {
            return serializedName;
        }

        public static Variant byLegacyMeta(int meta) {
            if (meta < 0 || meta >= VALUES.length) {
                return SULFUR;
            }
            return VALUES[meta];
        }
    }
}
