package com.hbm.ntm.item;

import com.hbm.ntm.block.Mk2PileDeviceBlock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** NBT is the modern replacement for pile_device's legacy item damage 0/1/2. */
public final class Mk2PileDeviceBlockItem extends BlockItem {
    public static final String TAG_KIND = "hbmPileDeviceKind";
    public Mk2PileDeviceBlockItem(Block block, Properties properties) { super(block, properties); }

    @Override protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        return super.placeBlock(context, state.setValue(Mk2PileDeviceBlock.KIND, kind(context.getItemInHand())));
    }
    @Override public Component getName(ItemStack stack) {
        return Component.translatable("block.hbm_ntm_rebirth.pile_device." + kind(stack).getSerializedName());
    }
    public void addCreativeStacks(CreativeModeTab.Output output) {
        for (Mk2PileDeviceBlock.Kind kind : Mk2PileDeviceBlock.Kind.values()) output.accept(stack(this, kind));
    }
    public static ItemStack stack(Mk2PileDeviceBlockItem item, Mk2PileDeviceBlock.Kind kind) {
        ItemStack stack = new ItemStack(item); stack.getOrCreateTag().putString(TAG_KIND, kind.getSerializedName()); return stack;
    }
    public static Mk2PileDeviceBlock.Kind kind(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) for (Mk2PileDeviceBlock.Kind kind : Mk2PileDeviceBlock.Kind.values())
            if (kind.getSerializedName().equals(tag.getString(TAG_KIND))) return kind;
        return Mk2PileDeviceBlock.Kind.LOADER;
    }
}
