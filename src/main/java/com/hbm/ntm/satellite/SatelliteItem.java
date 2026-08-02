package com.hbm.ntm.satellite;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Modern stack-NBT carrier for the legacy ItemSatellite metadata family.
 *
 * <p>Its ordinal order is the source ItemSatellite.EnumSatType order, which
 * deliberately differs from the SavedData satellite registry order.</p>
 */
public final class SatelliteItem extends Item implements ISatelliteChip {
    public static final String TAG_VARIANT = "hbmLegacyVariant";

    public SatelliteItem(Properties properties) {
        super(properties);
    }

    public static ItemStack stack(Item item, Variant variant) {
        ItemStack stack = new ItemStack(item);
        stack.getOrCreateTag().putInt(TAG_VARIANT, variant.ordinal());
        return stack;
    }

    public static Variant variantOf(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof SatelliteItem)) {
            return Variant.SPY;
        }
        return Variant.byOrdinal(stack.hasTag() ? stack.getTag().getInt(TAG_VARIANT) : 0);
    }

    public static float modelVariant(ItemStack stack) {
        return variantOf(stack).ordinal();
    }

    public void addCreativeStacks(CreativeModeTab.Output output) {
        for (Variant variant : Variant.values()) {
            output.accept(stack(this, variant));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable("item.hbm_ntm_rebirth.satellite." + variantOf(stack).serializedName + ".name");
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("satchip.frequency").append(": " + getFrequency(stack)));
    }

    public enum Variant {
        SPY("spy", LegacySatelliteType.MAPPER),
        SCANNER("scanner", LegacySatelliteType.SCANNER),
        RADAR("radar", LegacySatelliteType.RADAR),
        MINER_ASTRO("miner_astro", LegacySatelliteType.MINER),
        MINER_LUNAR("miner_lunar", LegacySatelliteType.LUNAR_MINER),
        PRECISION_LASER("precision_laser", LegacySatelliteType.PRECISION_LASER),
        DEATH_RAY("death_ray", LegacySatelliteType.LASER),
        XENIUM_RESONATOR("xenium_resonator", LegacySatelliteType.RESONATOR),
        RELAY("relay", LegacySatelliteType.RELAY),
        DETECTOR("detector", LegacySatelliteType.DETECTOR),
        RAY_SCAN("ray_scan", LegacySatelliteType.RAY_SCAN);

        private final String serializedName;
        private final LegacySatelliteType satelliteType;

        Variant(String serializedName, LegacySatelliteType satelliteType) {
            this.serializedName = serializedName;
            this.satelliteType = satelliteType;
        }

        public LegacySatelliteType satelliteType() {
            return satelliteType;
        }

        public static Variant byOrdinal(int ordinal) {
            Variant[] values = values();
            return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SPY;
        }
    }
}
