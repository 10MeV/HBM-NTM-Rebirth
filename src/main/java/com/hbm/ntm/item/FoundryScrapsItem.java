package com.hbm.ntm.item;

import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.inventory.material.NTMMaterial.SmeltingBehavior;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class FoundryScrapsItem extends Item {
    private static final String TAG_MATERIAL = "mat";
    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_LIQUID = "liquid";

    public FoundryScrapsItem(Properties properties) {
        super(properties);
    }

    public static ItemStack create(MaterialStack stack) {
        return create(stack, false);
    }

    public static ItemStack create(MaterialStack stack, boolean liquid) {
        if (stack == null || stack.material == null || stack.amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = new ItemStack(ModItems.FOUNDRY_SCRAPS.get());
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putInt(TAG_MATERIAL, stack.material.id);
        tag.putInt(TAG_AMOUNT, stack.amount);
        if (liquid) {
            tag.putBoolean(TAG_LIQUID, true);
        }
        return itemStack;
    }

    @Nullable
    public static MaterialStack getMaterial(ItemStack stack) {
        if (!(stack.getItem() instanceof FoundryScrapsItem) || !stack.hasTag()) {
            return null;
        }
        NTMMaterial material = Mats.matById.get(stack.getTag().getInt(TAG_MATERIAL));
        if (material == null) {
            return null;
        }
        int amount = stack.getTag().contains(TAG_AMOUNT) ? stack.getTag().getInt(TAG_AMOUNT)
                : MaterialShapes.INGOT.q(1);
        return new MaterialStack(material, amount);
    }

    public static void addCreativeStacks(CreativeModeTab.Output output, FoundryScrapsItem item) {
        for (NTMMaterial material : Mats.orderedList) {
            if (material.smeltable == SmeltingBehavior.SMELTABLE
                    || material.smeltable == SmeltingBehavior.ADDITIVE) {
                output.accept(create(new MaterialStack(material, MaterialShapes.INGOT.q(1))));
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        MaterialStack material = getMaterial(stack);
        if (material == null) {
            return super.getName(stack);
        }
        return Component.translatable("item.hbm_ntm_rebirth.scraps",
                Component.translatable(material.material.getUnlocalizedName()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        MaterialStack material = getMaterial(stack);
        if (material == null) {
            return;
        }
        tooltip.add(Component.literal(Mats.formatAmount(material.amount, flag.isAdvanced()))
                .withStyle(ChatFormatting.GRAY));
        if (stack.hasTag() && stack.getTag().getBoolean(TAG_LIQUID)
                && material.material.smeltable == SmeltingBehavior.ADDITIVE) {
            tooltip.add(Component.literal("Additive, not castable!").withStyle(ChatFormatting.DARK_RED));
        }
    }
}
