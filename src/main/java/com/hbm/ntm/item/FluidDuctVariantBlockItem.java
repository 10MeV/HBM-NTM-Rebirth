package com.hbm.ntm.item;

import com.hbm.ntm.block.FluidDuctBoxBlock;
import java.util.Arrays;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class FluidDuctVariantBlockItem extends BlockItem {
    private final int[] creativeMetas;

    public FluidDuctVariantBlockItem(Block block, Properties properties, int... creativeMetas) {
        super(block, properties);
        this.creativeMetas = creativeMetas == null || creativeMetas.length == 0
                ? new int[] {0}
                : Arrays.stream(creativeMetas).map(FluidDuctBoxBlock::clampLegacyMetadata).toArray();
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        return super.placeBlock(context, state.setValue(FluidDuctBoxBlock.LEGACY_METADATA,
                getLegacyMetadata(context.getItemInHand())));
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId(stack));
    }

    public void addCreativeStacks(CreativeModeTab.Output output) {
        for (int meta : creativeMetas) {
            output.accept(createStack(this, meta));
        }
    }

    public int getLegacyMetadata(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int meta = tag == null ? 0 : tag.getInt(LegacyStateBlockItem.TAG_VARIANT);
        return FluidDuctBoxBlock.clampLegacyMetadata(meta);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        com.hbm.ntm.client.renderer.LegacyItemRendererBridge.accept(consumer,
                () -> com.hbm.ntm.client.renderer.FluidDuctItemRenderer.INSTANCE);
    }

    public static ItemStack createStack(FluidDuctVariantBlockItem item, int legacyMetadata) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(LegacyStateBlockItem.TAG_VARIANT,
                FluidDuctBoxBlock.clampLegacyMetadata(legacyMetadata));
        return stack;
    }
}
