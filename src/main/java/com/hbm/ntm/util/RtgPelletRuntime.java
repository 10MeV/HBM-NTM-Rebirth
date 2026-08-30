package com.hbm.ntm.util;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.config.RtgConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;

public final class RtgPelletRuntime {
    public static final String TAG_PELLET_DEPLETION = "PELLET_DEPLETION";
    public static final int HEAT_MAX_DECAYING = 600;
    public static final int HEAT_MAX_STATIC = 200;

    private static final Map<String, PelletSpec> SPECS = new LinkedHashMap<>();

    static {
        register("pellet_rtg_radium", 3, "pellet_rtg_depleted_lead",
                lifespan(16.0F, HalfLifeType.LONG) * 3L / 2L);
        register("pellet_rtg_weak", 5, "pellet_rtg_depleted_lead",
                lifespan(1.0F, HalfLifeType.LONG) * 3L / 2L);
        register("pellet_rtg", 10, "pellet_rtg_depleted_lead",
                lifespan(87.7F, HalfLifeType.MEDIUM) * 3L / 2L);
        register("pellet_rtg_strontium", 15, "pellet_rtg_depleted_zirconium",
                lifespan(29.0F, HalfLifeType.MEDIUM) * 3L / 2L);
        register("pellet_rtg_cobalt", 15, "pellet_rtg_depleted_nickel",
                lifespan(5.3F, HalfLifeType.MEDIUM) * 3L / 2L);
        register("pellet_rtg_actinium", 20, "pellet_rtg_depleted_lead",
                lifespan(21.8F, HalfLifeType.MEDIUM) * 3L / 2L);
        register("pellet_rtg_americium", 20, "pellet_rtg_depleted_neptunium",
                lifespan(4.7F, HalfLifeType.LONG) * 3L / 2L);
        register("pellet_rtg_polonium", 50, "pellet_rtg_depleted_lead",
                lifespan(138.0F, HalfLifeType.SHORT) * 3L / 2L);
        register("pellet_rtg_gold", 100, 200, "pellet_rtg_depleted_mercury",
                lifespan(2.7F, HalfLifeType.SHORT) * 3L / 2L);
        register("pellet_rtg_lead", 200, 600, "pellet_rtg_depleted_bismuth",
                lifespan(0.3F, HalfLifeType.SHORT) * 3L / 2L);
    }

    public static boolean isPellet(ItemStack stack) {
        return spec(stack) != null;
    }

    public static boolean isPellet(Item item) {
        return spec(item) != null;
    }

    public static int heat(ItemStack stack) {
        PelletSpec spec = spec(stack);
        if (spec == null) {
            return 0;
        }
        if (!RtgConfig.scaleRtgPower()) {
            return spec.baseHeat();
        }
        return scaledHeat(stack, spec);
    }

    public static int baseHeat(ItemStack stack) {
        PelletSpec spec = spec(stack);
        return spec == null ? 0 : spec.baseHeat();
    }

    public static int baseHeat(Item item) {
        PelletSpec spec = spec(item);
        return spec == null ? 0 : spec.baseHeat();
    }

    public static int scaledHeat(ItemStack stack) {
        PelletSpec spec = spec(stack);
        return spec == null ? 0 : scaledHeat(stack, spec);
    }

    public static long lifespan(ItemStack stack) {
        PelletSpec spec = spec(stack);
        return spec == null ? 0L : lifespan(stack, spec);
    }

    public static long maxLifespan(ItemStack stack) {
        PelletSpec spec = spec(stack);
        return spec == null ? 0L : spec.lifespan();
    }

    public static long maxLifespan(Item item) {
        PelletSpec spec = spec(item);
        return spec == null ? 0L : spec.lifespan();
    }

    public static boolean doesDecay(ItemStack stack) {
        return spec(stack) != null;
    }

    public static boolean doesDecay(Item item) {
        return spec(item) != null;
    }

    public static ItemStack decayItem(ItemStack stack) {
        PelletSpec spec = spec(stack);
        return spec == null ? ItemStack.EMPTY : itemStack(spec.decayItem(), 1);
    }

    public static ItemStack decayItem(Item item) {
        PelletSpec spec = spec(item);
        return spec == null ? ItemStack.EMPTY : itemStack(spec.decayItem(), 1);
    }

    public static double durabilityForDisplay(ItemStack stack) {
        PelletSpec spec = spec(stack);
        if (spec == null || spec.lifespan() <= 0L) {
            return 0.0D;
        }
        return 1.0D - (double) lifespan(stack, spec) / (double) spec.lifespan();
    }

    public static ItemStack handleDecay(ItemStack stack) {
        PelletSpec spec = spec(stack);
        return spec == null ? stack : handleDecay(stack, spec);
    }

    public static void decay(ItemStack stack) {
        PelletSpec spec = spec(stack);
        if (spec != null) {
            decay(stack, spec);
        }
    }

    public static boolean isRegularPellet(ItemStack stack) {
        return isNamed(stack, "pellet_rtg");
    }

    public static int updateHeat(ItemStackHandler items, int firstSlot, int lastSlotInclusive) {
        int total = 0;
        for (int slot = firstSlot; slot <= lastSlotInclusive; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            PelletSpec spec = spec(stack);
            if (spec == null) {
                continue;
            }
            total += heat(stack);
            ItemStack decayed = handleDecay(stack);
            if (decayed != stack || RtgConfig.doRtgsDecay()) {
                items.setStackInSlot(slot, decayed);
            }
        }
        return total;
    }

    public static int heatMax() {
        return RtgConfig.doRtgsDecay() ? HEAT_MAX_DECAYING : HEAT_MAX_STATIC;
    }

    public static List<Component> acceptedPelletTooltip() {
        return acceptedPelletTooltip(5);
    }

    public static List<Component> acceptedPelletTooltip(int outputMultiplier) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatableWithFallback("desc.gui.rtg.pellets", "Accepted Pellets:"));
        SPECS.forEach((name, spec) -> lines.add(Component.translatableWithFallback(
                "desc.gui.rtg.pelletPower", "%s (%s HE/tick)",
                Component.translatableWithFallback("item." + HbmNtm.MOD_ID + "." + name, name),
                spec.baseHeat() * outputMultiplier)));
        return lines;
    }

    public static List<FuelSpec> acceptedFuelSpecs() {
        List<FuelSpec> specs = new ArrayList<>();
        SPECS.forEach((name, spec) -> {
            ItemStack stack = itemStack(name);
            if (!stack.isEmpty()) {
                specs.add(new FuelSpec(stack, spec.baseHeat(), spec.baseHeat() * 5));
            }
        });
        return specs;
    }

    private static ItemStack handleDecay(ItemStack stack, PelletSpec spec) {
        if (!RtgConfig.doRtgsDecay()) {
            return stack;
        }
        long lifespan = lifespan(stack, spec);
        if (lifespan <= 0L) {
            return itemStack(spec.decayItem(), 1);
        }
        decay(stack, spec);
        return stack;
    }

    private static void decay(ItemStack stack, PelletSpec spec) {
        if (stack.hasTag()) {
            stack.getOrCreateTag().putLong(TAG_PELLET_DEPLETION, lifespan(stack, spec) - 1L);
        } else {
            stack.getOrCreateTag().putLong(TAG_PELLET_DEPLETION, spec.lifespan());
        }
    }

    private static long lifespan(ItemStack stack, PelletSpec spec) {
        if (!stack.hasTag()) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong(TAG_PELLET_DEPLETION, spec.lifespan());
            return spec.lifespan();
        }
        return stack.getOrCreateTag().getLong(TAG_PELLET_DEPLETION);
    }

    private static int scaledHeat(ItemStack stack, PelletSpec spec) {
        if (spec.lifespan() <= 0L) {
            return spec.baseHeat();
        }
        return (int) Math.ceil(spec.baseHeat() * ((double) lifespan(stack, spec) / (double) spec.lifespan()));
    }

    private static PelletSpec spec(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return spec(stack.getItem());
    }

    private static PelletSpec spec(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        return id == null ? null : SPECS.get(id.getPath());
    }

    private static boolean isNamed(ItemStack stack, String name) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id != null && name.equals(id.getPath());
    }

    private static void register(String name, int heat, String decayItem, long lifespan) {
        register(name, heat, heat, decayItem, lifespan);
    }

    private static void register(String name, int staticHeat, int decayingHeat, String decayItem, long lifespan) {
        SPECS.put(name, new PelletSpec(staticHeat, decayingHeat, decayItem, lifespan));
    }

    private static long lifespan(float halfLife, HalfLifeType type) {
        return switch (type) {
            case LONG -> (long) (48000 * 100 * 100 * halfLife);
            case MEDIUM -> (long) (48000 * 100 * halfLife);
            case SHORT -> (long) (48000 * halfLife);
        };
    }

    private static ItemStack itemStack(String name) {
        return itemStack(name, 1);
    }

    private static ItemStack itemStack(String name, int count) {
        ResourceLocation id = new ResourceLocation(HbmNtm.MOD_ID, name);
        if (!ForgeRegistries.ITEMS.containsKey(id)) {
            return ItemStack.EMPTY;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
    }

    public record FuelSpec(ItemStack input, int heat, int powerPerTick) {
    }

    private record PelletSpec(int staticHeat, int decayingHeat, String decayItem, long lifespan) {
        private int baseHeat() {
            return RtgConfig.doRtgsDecay() ? decayingHeat : staticHeat;
        }
    }

    private enum HalfLifeType {
        SHORT,
        MEDIUM,
        LONG
    }

    private RtgPelletRuntime() {
    }
}
