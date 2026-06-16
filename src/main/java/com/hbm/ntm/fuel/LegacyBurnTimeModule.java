package com.hbm.ntm.fuel;

import com.hbm.ntm.util.HbmItemStackUtil;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Modern counterpart to 1.7.10 ModuleBurnTime plus HBM's FuelHandler table.
 */
public final class LegacyBurnTimeModule {
    private static final int SINGLE = 200;

    private static final int MOD_LOG = 0;
    private static final int MOD_WOOD = 1;
    private static final int MOD_COAL = 2;
    private static final int MOD_LIGNITE = 3;
    private static final int MOD_COKE = 4;
    private static final int MOD_SOLID = 5;
    private static final int MOD_ROCKET = 6;
    private static final int MOD_BALEFIRE = 7;

    private final double[] modTime = fillDefaults();
    private final double[] modHeat = fillDefaults();

    public int getBurnTime(ItemStack stack) {
        return getBurnTime(stack, 1.0D);
    }

    public int getBurnTime(ItemStack stack, double def) {
        int fuel = getLegacyFuelTime(stack);
        if (fuel <= 0) {
            return 0;
        }
        return (int) (fuel * getMod(stack, modTime, def));
    }

    public int getBurnHeat(int base, ItemStack stack) {
        if (base <= 0) {
            return 0;
        }
        return (int) (base * getMod(stack, modHeat, 1.0D));
    }

    public FuelAsh getAshFromFuel(ItemStack stack) {
        for (String tag : lowerTagNames(stack)) {
            if (tag.contains("coke") || tag.contains("coal") || tag.contains("lignite")) {
                return FuelAsh.COAL;
            }
            if (tag.contains("log") || tag.contains("wood") || tag.contains("sapling")) {
                return FuelAsh.WOOD;
            }
        }
        return FuelAsh.MISC;
    }

    public List<String> getTimeDescription() {
        return List.of();
    }

    public List<String> getHeatDescription() {
        return List.of();
    }

    public LegacyBurnTimeModule setLigniteTimeMod(double mod) {
        modTime[MOD_LIGNITE] = mod;
        return this;
    }

    public LegacyBurnTimeModule setCoalTimeMod(double mod) {
        modTime[MOD_COAL] = mod;
        return this;
    }

    public LegacyBurnTimeModule setLogTimeMod(double mod) {
        modTime[MOD_LOG] = mod;
        return this;
    }

    public LegacyBurnTimeModule setWoodTimeMod(double mod) {
        modTime[MOD_WOOD] = mod;
        return this;
    }

    public LegacyBurnTimeModule setCokeTimeMod(double mod) {
        modTime[MOD_COKE] = mod;
        return this;
    }

    public LegacyBurnTimeModule setSolidTimeMod(double mod) {
        modTime[MOD_SOLID] = mod;
        return this;
    }

    public LegacyBurnTimeModule setRocketTimeMod(double mod) {
        modTime[MOD_ROCKET] = mod;
        return this;
    }

    public LegacyBurnTimeModule setBalefireTimeMod(double mod) {
        modTime[MOD_BALEFIRE] = mod;
        return this;
    }

    public LegacyBurnTimeModule setLigniteHeatMod(double mod) {
        modHeat[MOD_LIGNITE] = mod;
        return this;
    }

    public LegacyBurnTimeModule setCoalHeatMod(double mod) {
        modHeat[MOD_COAL] = mod;
        return this;
    }

    public LegacyBurnTimeModule setLogHeatMod(double mod) {
        modHeat[MOD_LOG] = mod;
        return this;
    }

    public LegacyBurnTimeModule setWoodHeatMod(double mod) {
        modHeat[MOD_WOOD] = mod;
        return this;
    }

    public LegacyBurnTimeModule setCokeHeatMod(double mod) {
        modHeat[MOD_COKE] = mod;
        return this;
    }

    public LegacyBurnTimeModule setSolidHeatMod(double mod) {
        modHeat[MOD_SOLID] = mod;
        return this;
    }

    public LegacyBurnTimeModule setRocketHeatMod(double mod) {
        modHeat[MOD_ROCKET] = mod;
        return this;
    }

    public LegacyBurnTimeModule setBalefireHeatMod(double mod) {
        modHeat[MOD_BALEFIRE] = mod;
        return this;
    }

    private double getMod(ItemStack stack, double[] mod, double def) {
        if (stack == null || stack.isEmpty()) {
            return 0.0D;
        }
        String name = itemName(stack);
        if (name != null) {
            if (name.equals("solid_fuel") || name.equals("solid_fuel_presto")
                    || name.equals("solid_fuel_presto_triplet")) {
                return mod[MOD_SOLID];
            }
            if (name.equals("solid_fuel_bf") || name.equals("solid_fuel_presto_bf")
                    || name.equals("solid_fuel_presto_triplet_bf")) {
                return mod[MOD_BALEFIRE];
            }
            if (name.equals("rocket_fuel")) {
                return mod[MOD_ROCKET];
            }
        }
        for (String tag : lowerTagNames(stack)) {
            if (tag.contains("coke")) {
                return mod[MOD_COKE];
            }
            if (tag.contains("coal")) {
                return mod[MOD_COAL];
            }
            if (tag.contains("lignite")) {
                return mod[MOD_LIGNITE];
            }
            if (tag.contains("log")) {
                return mod[MOD_LOG];
            }
            if (tag.contains("wood")) {
                return mod[MOD_WOOD];
            }
        }
        return def;
    }

    private static int getLegacyFuelTime(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        String name = itemName(stack);
        if (name != null) {
            int explicit = switch (name) {
                case "solid_fuel" -> SINGLE * 16;
                case "solid_fuel_presto" -> SINGLE * 40;
                case "solid_fuel_presto_triplet" -> SINGLE * 200;
                case "solid_fuel_bf" -> SINGLE * 160;
                case "solid_fuel_presto_bf" -> SINGLE * 400;
                case "solid_fuel_presto_triplet_bf" -> SINGLE * 2000;
                case "rocket_fuel" -> SINGLE * 32;
                case "biomass" -> SINGLE * 2;
                case "biomass_compressed" -> SINGLE * 4;
                case "powder_coal" -> SINGLE * 8;
                case "scrap" -> SINGLE / 4;
                case "dust" -> SINGLE / 8;
                case "block_scrap" -> SINGLE * 2;
                case "powder_fire" -> 6400;
                case "lignite", "powder_lignite" -> 1200;
                case "coke" -> SINGLE * 16;
                case "block_coke" -> SINGLE * 160;
                case "book_guide" -> SINGLE;
                case "coal_infernal" -> 4800;
                case "crystal_coal" -> 6400;
                case "powder_sawdust" -> SINGLE / 2;
                case "briquette_coal" -> SINGLE * 10;
                case "briquette_lignite" -> SINGLE * 8;
                case "briquette_wood" -> SINGLE * 2;
                case "powder_ash_wood", "powder_ash_misc", "powder_ash_soot" -> SINGLE / 2;
                case "powder_ash_coal", "powder_ash_fly" -> SINGLE;
                default -> 0;
            };
            if (explicit > 0) {
                return explicit;
            }
        }
        return ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
    }

    private static List<String> lowerTagNames(ItemStack stack) {
        return HbmItemStackUtil.getTagNames(stack).stream()
                .map(String::toLowerCase)
                .toList();
    }

    @Nullable
    private static String itemName(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (id == null) {
            return null;
        }
        return id.getPath();
    }

    private static double[] fillDefaults() {
        double[] values = new double[8];
        java.util.Arrays.fill(values, 1.0D);
        return values;
    }

    public enum FuelAsh {
        WOOD,
        COAL,
        MISC
    }
}
