package com.hbm.ntm.item;

import com.hbm.ntm.block.CrashedBombBlock;
import com.hbm.ntm.block.CrashedBombType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Preserves the four old item-damage variants as an explicit stack tag. */
public class CrashedBombBlockItem extends BlockItem {
    public static final String TAG_VARIANT = "hbmLegacyVariant";

    public CrashedBombBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        return super.placeBlock(context, state.setValue(CrashedBombBlock.TYPE, getType(context.getItemInHand())));
    }

    public void addCreativeStacks(CreativeModeTab.Output output) {
        for (CrashedBombType type : CrashedBombType.values()) {
            output.accept(createStack(this, type));
        }
    }

    public static ItemStack createStack(CrashedBombBlockItem item, CrashedBombType type) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(TAG_VARIANT, type.ordinal());
        return stack;
    }

    public static CrashedBombType getType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return CrashedBombType.byLegacyOrdinal(tag == null ? 0 : tag.getInt(TAG_VARIANT));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptCrashedBomb", consumer);
    }
}
