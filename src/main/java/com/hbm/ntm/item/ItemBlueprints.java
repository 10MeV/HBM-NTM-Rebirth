package com.hbm.ntm.item;

import com.hbm.ntm.recipe.GenericMachineRecipe;
import com.hbm.ntm.recipe.GenericMachineRecipeRuntime;
import com.hbm.ntm.recipe.LegacyBlueprintPools;
import com.hbm.ntm.registry.ModItems;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ItemBlueprints extends Item {
    private static final String TAG_POOL = "pool";

    public ItemBlueprints(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide || !stack.hasTag()) {
            return InteractionResultHolder.pass(stack);
        }

        String pool = grabPool(stack);
        if (pool == null || pool.startsWith(LegacyBlueprintPools.PREFIX_SECRET)
                || !player.getInventory().contains(new ItemStack(Items.PAPER))) {
            return InteractionResultHolder.pass(stack);
        }

        player.getInventory().clearOrCountMatchingItems(candidate -> candidate.is(Items.PAPER), 1, player.inventoryMenu.getCraftSlots());
        player.swing(hand);
        ItemStack copy = stack.copyWithCount(1);
        if (player.getAbilities().instabuild) {
            player.drop(copy, false);
            return InteractionResultHolder.success(stack);
        }
        if (stack.getCount() < stack.getMaxStackSize()) {
            stack.grow(1);
            return InteractionResultHolder.success(stack);
        }
        if (!player.getInventory().add(copy)) {
            player.drop(copy, false);
        }
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        String pool = grabPool(stack);
        if (pool == null) {
            return;
        }
        if (pool.startsWith(LegacyBlueprintPools.PREFIX_SECRET)) {
            tooltip.add(Component.literal("Cannot be copied!").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.literal("Right-click to copy (requires paper)").withStyle(ChatFormatting.YELLOW));
        }
        if (level == null) {
            tooltip.add(Component.literal(pool).withStyle(ChatFormatting.GRAY));
            return;
        }
        for (GenericMachineRecipe.Machine machine : GenericMachineRecipe.Machine.values()) {
            for (GenericMachineRecipe recipe : GenericMachineRecipeRuntime.findByPool(level, machine, pool)) {
                tooltip.add(recipe.getDisplayName());
            }
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        String pool = grabPool(stack);
        if (pool == null) {
            return super.getName(stack);
        }
        return Component.translatable(getDescriptionId()).append(Component.literal(" [" + pool + "]"));
    }

    @Nullable
    public static String grabPool(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getItem() != ModItems.BLUEPRINTS.get() || !stack.hasTag()) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_POOL)) {
            return null;
        }
        String pool = tag.getString(TAG_POOL);
        return pool.isBlank() ? null : pool;
    }

    public static ItemStack make(String pool) {
        ItemStack stack = new ItemStack(ModItems.BLUEPRINTS.get());
        stack.getOrCreateTag().putString(TAG_POOL, pool == null ? "" : pool);
        return stack;
    }
}
