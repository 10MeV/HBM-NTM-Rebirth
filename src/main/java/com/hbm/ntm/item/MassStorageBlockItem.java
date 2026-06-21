package com.hbm.ntm.item;

import com.hbm.ntm.block.MassStorageBlock;
import com.hbm.ntm.blockentity.MassStorageBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class MassStorageBlockItem extends LegacyStateBlockItem {
    public MassStorageBlockItem(Block block, Properties properties) {
        super(block, properties, MassStorageBlock.VARIANT, 4, MassStorageBlockItem::variantName);
    }

    @Override
    public void addCreativeStacks(CreativeModeTab.Output output) {
        output.accept(createStack(this, 3));
        output.accept(createStack(this, 0));
        output.accept(createStack(this, 1));
        output.accept(createStack(this, 2));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("slot" + MassStorageBlockEntity.SLOT_FILTER, Tag.TAG_COMPOUND)) {
            return;
        }
        ItemStack type = ItemStack.of(tag.getCompound("slot" + MassStorageBlockEntity.SLOT_FILTER));
        if (type.isEmpty()) {
            return;
        }
        tooltip.add(type.getHoverName().copy().withStyle(ChatFormatting.GOLD));
        int stockpile = tag.getInt(MassStorageBlockEntity.LEGACY_STACK_TAG);
        tooltip.add(Component.literal(String.format(Locale.US, "%,d", stockpile) + " / "
                + String.format(Locale.US, "%,d", MassStorageBlock.capacity(getVariant(stack)))));
    }

    private static Component variantName(int variant) {
        return Component.translatable("block.hbm_ntm_rebirth.mass_storage");
    }
}
