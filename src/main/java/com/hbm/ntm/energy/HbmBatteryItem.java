package com.hbm.ntm.energy;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class HbmBatteryItem extends Item implements HbmChargeableItem {
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
        return getMaxCharge(stack);
    }

    public long getCharge(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0L;
        }
        if (stack.hasTag()) {
            return clampCharge(stack, stack.getTag().getLong(getChargeTagName(stack)));
        }
        CompoundTag tag = stack.getOrCreateTag();
        String chargeTag = getChargeTagName(stack);
        long defaultCharge = getDefaultCharge(stack);
        long clampedDefault = clampCharge(stack, defaultCharge);
        tag.putLong(chargeTag, clampedDefault);
        return clampedDefault;
    }

    public void setCharge(ItemStack stack, long charge) {
        if (!stack.isEmpty()) {
            stack.getOrCreateTag().putLong(getChargeTagName(stack), clampCharge(stack, charge));
        }
    }

    public long chargeBattery(ItemStack stack, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        long before = getCharge(stack);
        setCharge(stack, before + amount);
        return getCharge(stack) - before;
    }

    public long dischargeBattery(ItemStack stack, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        long before = getCharge(stack);
        setCharge(stack, before - amount);
        return before - getCharge(stack);
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

    public void addCreativeStacks(CreativeModeTab.Output output, ItemStack stack) {
        boolean added = false;
        if (getChargeRate(stack) > 0L) {
            output.accept(getEmptyBattery(stack));
            added = true;
        }
        if (getDischargeRate(stack) > 0L) {
            output.accept(getFullBattery(stack));
            added = true;
        }
        if (!added) {
            output.accept(stack);
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new HbmBatteryItemCapabilityProvider(stack, this);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getMaxCharge(stack) > 0L;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (getMaxCharge(stack) <= 0L) {
            return 0;
        }
        double fraction = Math.max(0.0D, Math.min(1.0D, (double) getCharge(stack) / (double) getMaxCharge(stack)));
        return Math.round(13.0F * (float) fraction);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        double fraction = getMaxCharge(stack) <= 0L ? 0.0D
                : Math.max(0.0D, Math.min(1.0D, (double) getCharge(stack) / (double) getMaxCharge(stack)));
        return Mth.hsvToRgb((float) fraction / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        long charge = stack.hasTag() ? getCharge(stack) : getMaxCharge(stack);
        tooltip.add(Component.literal("Energy stored: " + shortNumber(charge) + "/" + shortNumber(getMaxCharge(stack)) + "HE"));
        tooltip.add(Component.literal("Charge rate: " + shortNumber(getChargeRate(stack)) + "HE/t"));
        tooltip.add(Component.literal("Discharge rate: " + shortNumber(getDischargeRate(stack)) + "HE/t"));
    }

    protected long clampCharge(ItemStack stack, long charge) {
        return Math.max(0L, Math.min(charge, getMaxCharge(stack)));
    }

    private static String shortNumber(long value) {
        double result;
        String suffix;
        double abs = Math.abs((double) value);
        if (abs >= 1_000_000_000_000_000_000.0D) {
            result = value / 1_000_000_000_000_000_000.0D;
            suffix = "E";
        } else if (abs >= 1_000_000_000_000_000.0D) {
            result = value / 1_000_000_000_000_000.0D;
            suffix = "P";
        } else if (abs >= 1_000_000_000_000.0D) {
            result = value / 1_000_000_000_000.0D;
            suffix = "T";
        } else if (abs >= 1_000_000_000.0D) {
            result = value / 1_000_000_000.0D;
            suffix = "G";
        } else if (abs >= 1_000_000.0D) {
            result = value / 1_000_000.0D;
            suffix = "M";
        } else if (abs >= 1_000.0D) {
            result = value / 1_000.0D;
            suffix = "k";
        } else {
            return Long.toString(value);
        }

        double rounded = result <= -100.0D ? Math.round(result * 10.0D) / 10.0D : Math.round(result * 100.0D) / 100.0D;
        return String.format(Locale.US, "%s%s", rounded, suffix);
    }
}
