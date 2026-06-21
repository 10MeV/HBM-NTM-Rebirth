package com.hbm.ntm.item;

import com.hbm.ntm.neutron.RBMKFuelRodRegistry;
import com.hbm.ntm.neutron.RBMKFuelRodSpec;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import com.hbm.ntm.neutron.RBMKItemPlanner;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RBMKFuelRodItem extends Item {
    private final RBMKFuelRodRegistry.Entry entry;

    public RBMKFuelRodItem(Properties properties, RBMKFuelRodRegistry.Entry entry) {
        super(properties.stacksTo(1));
        this.entry = entry;
    }

    public String getLegacyRodId() {
        return entry.legacyRodId();
    }

    public RBMKFuelRodSpec getSpec() {
        return entry.spec();
    }

    public RBMKFuelRodState getState(ItemStack stack) {
        RBMKFuelRodState state = RBMKFuelRodState.fresh(entry.spec());
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            state.load(tag, entry.spec());
        }
        return state;
    }

    public void setState(ItemStack stack, RBMKFuelRodState state) {
        CompoundTag tag = stack.getOrCreateTag();
        (state == null ? RBMKFuelRodState.fresh(entry.spec()) : state).save(tag);
    }

    public void setLegacyDefaultState(ItemStack stack) {
        RBMKFuelRodState.fresh(entry.spec()).saveLegacyDefaults(stack.getOrCreateTag());
    }

    public void ensureLegacyDefaultState(ItemStack stack) {
        if (!stack.hasTag()) {
            setLegacyDefaultState(stack);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return RBMKItemPlanner.fuelRodDurability(entry.spec(), getState(stack)).showBar();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        double value = RBMKItemPlanner.fuelRodDurability(entry.spec(), getState(stack)).displayValue();
        return Math.round((float) (13.0D * Math.max(0.0D, Math.min(1.0D, value))));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55FF55;
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return new ItemStack(ModItems.RBMK_FUEL_EMPTY.get());
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        ensureLegacyDefaultState(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        RBMKItemPlanner.FuelRodTooltipPlan plan = RBMKItemPlanner.fuelRodTooltip(entry, getState(stack));
        for (RBMKItemPlanner.TooltipLine line : plan.lines()) {
            tooltip.add(applyStyle(component(line), line.style()));
        }
    }

    private static MutableComponent component(RBMKItemPlanner.TooltipLine line) {
        if (!line.literal().isEmpty()) {
            return Component.literal(line.literal());
        }
        if (!line.argumentTranslationKey().isEmpty()) {
            return Component.translatable(line.translationKey(), Component.translatable(line.argumentTranslationKey()));
        }
        if (!line.argument().isEmpty()) {
            return Component.translatable(line.translationKey(), line.argument());
        }
        return Component.translatable(line.translationKey());
    }

    private static MutableComponent applyStyle(MutableComponent component, RBMKItemPlanner.TooltipStyle style) {
        return switch (style) {
            case ITALIC -> component.withStyle(ChatFormatting.ITALIC);
            case DARK_GRAY_ITALIC -> component.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
            case GOLD -> component.withStyle(ChatFormatting.GOLD);
            case RED -> component.withStyle(ChatFormatting.RED);
            case GREEN -> component.withStyle(ChatFormatting.GREEN);
            case DARK_PURPLE -> component.withStyle(ChatFormatting.DARK_PURPLE);
            case BLUE -> component.withStyle(ChatFormatting.BLUE);
            case YELLOW -> component.withStyle(ChatFormatting.YELLOW);
            case DARK_RED -> component.withStyle(ChatFormatting.DARK_RED);
            case DARK_GREEN -> component.withStyle(ChatFormatting.DARK_GREEN);
            case DARK_GRAY -> component.withStyle(ChatFormatting.DARK_GRAY);
        };
    }
}
