package com.hbm.ntm.energy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class HbmSelfChargingBatteryItem extends HbmBatteryItem {
    private final String legacyTexturePath;
    private final int legacyMeta;

    public HbmSelfChargingBatteryItem(Properties properties, long power, String legacyTexturePath, int legacyMeta) {
        super(properties, power, 0L, power);
        this.legacyTexturePath = legacyTexturePath;
        this.legacyMeta = legacyMeta;
    }

    public String getLegacyTexturePath() {
        return legacyTexturePath;
    }

    public int getLegacyMeta() {
        return legacyMeta;
    }

    public boolean isLoaded() {
        return getMaxCharge(ItemStack.EMPTY) > 0L;
    }

    @Override
    protected long getDefaultCharge(ItemStack stack) {
        return getMaxCharge(stack);
    }

    @Override
    public long getCharge(ItemStack stack) {
        return getMaxCharge(stack);
    }

    @Override
    public void setCharge(ItemStack stack, long charge) {
    }

    @Override
    public long chargeBattery(ItemStack stack, long amount) {
        return 0L;
    }

    @Override
    public long dischargeBattery(ItemStack stack, long amount) {
        return Math.max(0L, Math.min(amount, getDischargeRate(stack)));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        long power = getMaxCharge(stack);
        if (power > 0L) {
            tooltip.add(Component.literal("Discharge rate: " + shortNumber(power) + "HE/t").withStyle(ChatFormatting.YELLOW));
        }
    }

    private static String shortNumber(long value) {
        double result;
        String suffix;
        long abs = Math.abs(value);
        if (abs >= 1_000_000_000_000_000_000L) {
            result = value / 1_000_000_000_000_000_000.0D;
            suffix = "E";
        } else if (abs >= 1_000_000_000_000_000L) {
            result = value / 1_000_000_000_000_000.0D;
            suffix = "P";
        } else if (abs >= 1_000_000_000_000L) {
            result = value / 1_000_000_000_000.0D;
            suffix = "T";
        } else if (abs >= 1_000_000_000L) {
            result = value / 1_000_000_000.0D;
            suffix = "G";
        } else if (abs >= 1_000_000L) {
            result = value / 1_000_000.0D;
            suffix = "M";
        } else if (abs >= 1_000L) {
            result = value / 1_000.0D;
            suffix = "k";
        } else {
            return Long.toString(value);
        }

        double rounded = result <= -100.0D ? Math.round(result * 10.0D) / 10.0D : Math.round(result * 100.0D) / 100.0D;
        return String.format(Locale.US, "%s%s", rounded, suffix);
    }
}
