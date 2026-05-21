package com.hbm.ntm.energy;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HbmBatteryItem extends Item {
    public static final String DEFAULT_CHARGE_TAG = "charge";

    private final long maxCharge;
    private final long chargeRate;
    private final long dischargeRate;

    public HbmBatteryItem(Properties properties, long maxCharge, long chargeRate, long dischargeRate) {
        super(properties.stacksTo(1));
        this.maxCharge = Math.max(0L, maxCharge);
        this.chargeRate = Math.max(0L, chargeRate);
        this.dischargeRate = Math.max(0L, dischargeRate);
    }

    public long getMaxCharge(ItemStack stack) {
        return maxCharge;
    }

    public long getChargeRate(ItemStack stack) {
        return chargeRate;
    }

    public long getDischargeRate(ItemStack stack) {
        return dischargeRate;
    }

    public String getChargeTagName(ItemStack stack) {
        return DEFAULT_CHARGE_TAG;
    }

    protected long getDefaultCharge(ItemStack stack) {
        return getChargeRate(stack) > 0L ? 0L : getMaxCharge(stack);
    }

    public long getCharge(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        String chargeTag = getChargeTagName(stack);
        if (!tag.contains(chargeTag)) {
            long defaultCharge = getDefaultCharge(stack);
            tag.putLong(chargeTag, defaultCharge);
            return defaultCharge;
        }
        return tag.getLong(chargeTag);
    }

    public void setCharge(ItemStack stack, long charge) {
        stack.getOrCreateTag().putLong(getChargeTagName(stack), Math.max(0L, Math.min(charge, getMaxCharge(stack))));
    }

    public long chargeBattery(ItemStack stack, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        long charge = getCharge(stack);
        long accepted = Math.min(amount, Math.max(0L, getMaxCharge(stack) - charge));
        accepted = Math.min(accepted, getChargeRate(stack));
        if (accepted > 0L) {
            setCharge(stack, charge + accepted);
        }
        return accepted;
    }

    public long dischargeBattery(ItemStack stack, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        long extracted = Math.min(amount, getCharge(stack));
        extracted = Math.min(extracted, getDischargeRate(stack));
        if (extracted > 0L) {
            setCharge(stack, getCharge(stack) - extracted);
        }
        return extracted;
    }

    public ItemStack getEmptyBattery(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setTag(new CompoundTag());
        setCharge(copy, 0L);
        return copy;
    }

    public ItemStack getFullBattery(ItemStack stack) {
        ItemStack copy = stack.copy();
        setCharge(copy, getMaxCharge(copy));
        return copy;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new HbmBatteryItemCapabilityProvider(stack, this);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getMaxCharge(stack) > 0L && getCharge(stack) < getMaxCharge(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (getMaxCharge(stack) <= 0L) {
            return 0;
        }
        return Math.round(13.0F * getCharge(stack) / (float) getMaxCharge(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x4DFFE0;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("desc.item.battery.charge", getCharge(stack), getMaxCharge(stack))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("desc.item.battery.chargeRate", getChargeRate(stack))
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("desc.item.battery.dischargeRate", getDischargeRate(stack))
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
