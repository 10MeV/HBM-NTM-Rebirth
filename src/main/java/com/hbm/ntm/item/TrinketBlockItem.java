package com.hbm.ntm.item;

import com.hbm.ntm.block.TrinketVariant;
import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import com.hbm.ntm.client.renderer.TrinketItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import java.util.List;

public class TrinketBlockItem extends BlockItem {
    private final TrinketVariant.Kind kind;

    public TrinketBlockItem(Block block, Properties properties, TrinketVariant.Kind kind) {
        super(block, properties);
        this.kind = kind;
    }

    public TrinketVariant.Kind kind() {
        return kind;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> TrinketItemRenderer.INSTANCE);
    }

    public static int getVariant(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TrinketVariant.TAG_VARIANT);
    }

    public static ItemStack createStack(Item item, int variant) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(TrinketVariant.TAG_VARIANT, Math.max(0, variant));
        return stack;
    }

    public static void addCreativeStacks(CreativeModeTab.Output output, TrinketBlockItem item) {
        int count = TrinketVariant.variantCount(item.kind);
        for (int variant = TrinketVariant.firstCreativeVariant(item.kind); variant < count; variant++) {
            output.accept(createStack(item, variant));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        int variant = TrinketVariant.clamp(kind, getVariant(stack));
        if (variant == 0) {
            return super.getName(stack);
        }
        return Component.translatable(getDescriptionId(stack))
                .append(" - ")
                .append(Component.translatable(TrinketVariant.displayKey(kind, variant)));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int variant = TrinketVariant.clamp(kind, getVariant(stack));
        if (variant > 0) {
            tooltip.add(Component.translatable(TrinketVariant.displayKey(kind, variant)).withStyle(ChatFormatting.GRAY));
        }
    }
}
