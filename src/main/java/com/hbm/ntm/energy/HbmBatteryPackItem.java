package com.hbm.ntm.energy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class HbmBatteryPackItem extends HbmBatteryItem {
    private final String legacyTextureName;
    private final int legacyMeta;
    private final boolean capacitor;

    public HbmBatteryPackItem(Properties properties, long maxCharge, long chargeRate, long dischargeRate,
            String legacyTextureName, int legacyMeta, boolean capacitor) {
        super(properties, maxCharge, chargeRate, dischargeRate);
        this.legacyTextureName = legacyTextureName;
        this.legacyMeta = legacyMeta;
        this.capacitor = capacitor;
    }

    public String getLegacyTextureName() {
        return legacyTextureName;
    }

    public int getLegacyMeta() {
        return legacyMeta;
    }

    public boolean isCapacitor() {
        return capacitor;
    }

    @Override
    protected long getDefaultCharge(ItemStack stack) {
        return 0L;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        acceptClientExtensions("com.hbm.ntm.client.renderer.BatteryPackItemRendererBridge", consumer);
    }

    private static void acceptClientExtensions(String className, Consumer<IClientItemExtensions> consumer) {
        try {
            Class<?> bridge = Class.forName(className);
            bridge.getMethod("accept", Consumer.class).invoke(null, consumer);
        } catch (ReflectiveOperationException exception) {
            Throwable cause = exception instanceof InvocationTargetException invocation && invocation.getCause() != null
                    ? invocation.getCause()
                    : exception;
            throw new IllegalStateException("Unable to initialize battery pack client renderer", cause);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getMaxCharge(stack) > 0L && getCharge(stack) < getMaxCharge(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        long maxCharge = getMaxCharge(stack);
        long charge = stack.hasTag() ? getCharge(stack) : maxCharge;
        long chargeRate = getChargeRate(stack);
        long dischargeRate = getDischargeRate(stack);
        double percent = maxCharge <= 0L ? 0.0D : charge * 1000L / maxCharge / 10.0D;

        tooltip.add(Component.literal("Energy stored: " + shortNumber(charge) + "/" + shortNumber(maxCharge)
                + "HE (" + percent + "%)").withStyle(ChatFormatting.GREEN));
        tooltip.add(Component.literal("Charge rate: " + shortNumber(chargeRate) + "HE/t").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Discharge rate: " + shortNumber(dischargeRate) + "HE/t").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("Time for full charge: " + minutes(maxCharge, chargeRate) + "min").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("Charge lasts for: " + minutes(maxCharge, dischargeRate) + "min").withStyle(ChatFormatting.GOLD));
    }

    private static double minutes(long maxCharge, long rate) {
        return rate <= 0L ? 0.0D : maxCharge / (double) rate / 20.0D / 60.0D;
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
