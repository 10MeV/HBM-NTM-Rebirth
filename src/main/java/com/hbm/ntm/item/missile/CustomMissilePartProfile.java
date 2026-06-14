package com.hbm.ntm.item.missile;

import com.hbm.ntm.api.entity.LegacyMissileRadarProfile;
import com.hbm.ntm.explosion.CustomMissileExplosion;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public record CustomMissilePartProfile(
        MissilePartItem.PartType type,
        PartSize top,
        PartSize bottom,
        FuelType fuelType,
        float fuel,
        float consumption,
        float lift,
        CustomMissileExplosion.WarheadType warheadType,
        float strength,
        float weight,
        float inaccuracy,
        float health) {
    private static final Map<String, CustomMissilePartProfile> BY_LEGACY_NAME = profiles();

    public static Map<String, CustomMissilePartProfile> profilesByLegacyName() {
        return BY_LEGACY_NAME;
    }

    @Nullable
    public static CustomMissilePartProfile byLegacyName(String legacyName) {
        return BY_LEGACY_NAME.get(legacyName);
    }

    @Nullable
    public static CustomMissilePartProfile fromPartItem(MissilePartItem item) {
        return item == null ? null : byLegacyName(item.legacyModelKey());
    }

    @Nullable
    public static ResolvedPart resolve(ItemStack missile, String key, MissilePartItem.PartType expectedType) {
        ResourceLocation id = CustomMissileItem.getPartId(missile, key);
        return resolve(id, expectedType);
    }

    @Nullable
    public static ResolvedPart resolve(ResourceLocation id, MissilePartItem.PartType expectedType) {
        if (id == null) {
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (!(item instanceof MissilePartItem part) || part.type() != expectedType) {
            return null;
        }
        CustomMissilePartProfile profile = fromPartItem(part);
        if (profile == null || profile.type() != expectedType) {
            return null;
        }
        return new ResolvedPart(id, part.legacyModelKey(), profile);
    }

    @Nullable
    public static Assembly assemblyFromStack(ItemStack stack) {
        if (!(stack.getItem() instanceof CustomMissileItem)) {
            return null;
        }
        return new Assembly(
                resolve(stack, CustomMissileItem.TAG_CHIP, MissilePartItem.PartType.CHIP),
                resolve(stack, CustomMissileItem.TAG_WARHEAD, MissilePartItem.PartType.WARHEAD),
                resolve(stack, CustomMissileItem.TAG_FUSELAGE, MissilePartItem.PartType.FUSELAGE),
                resolve(stack, CustomMissileItem.TAG_STABILITY, MissilePartItem.PartType.FINS),
                resolve(stack, CustomMissileItem.TAG_THRUSTER, MissilePartItem.PartType.THRUSTER));
    }

    private static Map<String, CustomMissilePartProfile> profiles() {
        Map<String, CustomMissilePartProfile> profiles = new LinkedHashMap<>();

        chip(profiles, "mp_chip_1", 0.1F);
        chip(profiles, "mp_chip_2", 0.05F);
        chip(profiles, "mp_chip_3", 0.01F);
        chip(profiles, "mp_chip_4", 0.005F);
        chip(profiles, "mp_chip_5", 0.0F);

        thruster(profiles, "mp_thruster_10_kerosene", FuelType.KEROSENE, 1F, 1.5F, PartSize.SIZE_10, 10F);
        thruster(profiles, "mp_thruster_10_solid", FuelType.SOLID, 1F, 1.5F, PartSize.SIZE_10, 15F);
        thruster(profiles, "mp_thruster_10_xenon", FuelType.XENON, 1F, 1.5F, PartSize.SIZE_10, 5F);
        thruster(profiles, "mp_thruster_15_kerosene", FuelType.KEROSENE, 1F, 7.5F, PartSize.SIZE_15, 15F);
        thruster(profiles, "mp_thruster_15_kerosene_dual", FuelType.KEROSENE, 1F, 2.5F, PartSize.SIZE_15, 15F);
        thruster(profiles, "mp_thruster_15_kerosene_triple", FuelType.KEROSENE, 1F, 5F, PartSize.SIZE_15, 15F);
        thruster(profiles, "mp_thruster_15_solid", FuelType.SOLID, 1F, 5F, PartSize.SIZE_15, 20F);
        thruster(profiles, "mp_thruster_15_solid_hexdecuple", FuelType.SOLID, 1F, 5F, PartSize.SIZE_15, 25F);
        thruster(profiles, "mp_thruster_15_hydrogen", FuelType.HYDROGEN, 1F, 7.5F, PartSize.SIZE_15, 20F);
        thruster(profiles, "mp_thruster_15_hydrogen_dual", FuelType.HYDROGEN, 1F, 2.5F, PartSize.SIZE_15, 15F);
        thruster(profiles, "mp_thruster_15_balefire_short", FuelType.BALEFIRE, 1F, 5F, PartSize.SIZE_15, 25F);
        thruster(profiles, "mp_thruster_15_balefire", FuelType.BALEFIRE, 1F, 5F, PartSize.SIZE_15, 25F);
        thruster(profiles, "mp_thruster_15_balefire_large", FuelType.BALEFIRE, 1F, 7.5F, PartSize.SIZE_15, 35F);
        thruster(profiles, "mp_thruster_15_balefire_large_rad", FuelType.BALEFIRE, 1F, 7.5F, PartSize.SIZE_15, 35F);
        thruster(profiles, "mp_thruster_20_kerosene", FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20, 30F);
        thruster(profiles, "mp_thruster_20_kerosene_dual", FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20, 30F);
        thruster(profiles, "mp_thruster_20_kerosene_triple", FuelType.KEROSENE, 1F, 100F, PartSize.SIZE_20, 30F);
        thruster(profiles, "mp_thruster_20_solid", FuelType.SOLID, 1F, 100F, PartSize.SIZE_20, 35F);
        thruster(profiles, "mp_thruster_20_solid_multi", FuelType.SOLID, 1F, 100F, PartSize.SIZE_20, 35F);
        thruster(profiles, "mp_thruster_20_solid_multier", FuelType.SOLID, 1F, 100F, PartSize.SIZE_20, 35F);

        fins(profiles, "mp_stability_10_flat", 0.5F, PartSize.SIZE_10, 10F);
        fins(profiles, "mp_stability_10_cruise", 0.25F, PartSize.SIZE_10, 5F);
        fins(profiles, "mp_stability_10_space", 0.35F, PartSize.SIZE_10, 5F);
        fins(profiles, "mp_stability_15_flat", 0.5F, PartSize.SIZE_15, 10F);
        fins(profiles, "mp_stability_15_thin", 0.35F, PartSize.SIZE_15, 5F);
        fins(profiles, "mp_stability_15_soyuz", 0.25F, PartSize.SIZE_15, 15F);
        fins(profiles, "mp_stability_20_flat", 0.5F, PartSize.SIZE_20, 0F);

        fuselageGroup(profiles, FuelType.KEROSENE, 2500F, PartSize.SIZE_10, PartSize.SIZE_10,
                "mp_fuselage_10_kerosene", "mp_fuselage_10_kerosene_camo", "mp_fuselage_10_kerosene_desert",
                "mp_fuselage_10_kerosene_sky", "mp_fuselage_10_kerosene_flames",
                "mp_fuselage_10_kerosene_insulation", "mp_fuselage_10_kerosene_sleek",
                "mp_fuselage_10_kerosene_metal", "mp_fuselage_10_kerosene_taint");
        fuselageGroup(profiles, FuelType.SOLID, 2500F, PartSize.SIZE_10, PartSize.SIZE_10,
                "mp_fuselage_10_solid", "mp_fuselage_10_solid_flames", "mp_fuselage_10_solid_insulation",
                "mp_fuselage_10_solid_sleek", "mp_fuselage_10_solid_soviet_glory",
                "mp_fuselage_10_solid_cathedral", "mp_fuselage_10_solid_moonlit",
                "mp_fuselage_10_solid_battery", "mp_fuselage_10_solid_duracell");
        fuselageGroup(profiles, FuelType.XENON, 5000F, PartSize.SIZE_10, PartSize.SIZE_10,
                "mp_fuselage_10_xenon", "mp_fuselage_10_xenon_bhole");
        fuselageGroup(profiles, FuelType.KEROSENE, 5000F, PartSize.SIZE_10, PartSize.SIZE_10,
                "mp_fuselage_10_long_kerosene", "mp_fuselage_10_long_kerosene_camo",
                "mp_fuselage_10_long_kerosene_desert", "mp_fuselage_10_long_kerosene_sky",
                "mp_fuselage_10_long_kerosene_flames", "mp_fuselage_10_long_kerosene_insulation",
                "mp_fuselage_10_long_kerosene_sleek", "mp_fuselage_10_long_kerosene_metal",
                "mp_fuselage_10_long_kerosene_dash", "mp_fuselage_10_long_kerosene_taint",
                "mp_fuselage_10_long_kerosene_vap");
        fuselageGroup(profiles, FuelType.SOLID, 5000F, PartSize.SIZE_10, PartSize.SIZE_10,
                "mp_fuselage_10_long_solid", "mp_fuselage_10_long_solid_flames",
                "mp_fuselage_10_long_solid_insulation", "mp_fuselage_10_long_solid_sleek",
                "mp_fuselage_10_long_solid_soviet_glory", "mp_fuselage_10_long_solid_bullet",
                "mp_fuselage_10_long_solid_silvermoonlight");
        fuselage(profiles, "mp_fuselage_10_15_kerosene", FuelType.KEROSENE, 10000F, PartSize.SIZE_10, PartSize.SIZE_15, 40F);
        fuselage(profiles, "mp_fuselage_10_15_solid", FuelType.SOLID, 10000F, PartSize.SIZE_10, PartSize.SIZE_15, 40F);
        fuselage(profiles, "mp_fuselage_10_15_hydrogen", FuelType.HYDROGEN, 10000F, PartSize.SIZE_10, PartSize.SIZE_15, 40F);
        fuselage(profiles, "mp_fuselage_10_15_balefire", FuelType.BALEFIRE, 10000F, PartSize.SIZE_10, PartSize.SIZE_15, 40F);
        fuselageGroup(profiles, FuelType.KEROSENE, 15000F, PartSize.SIZE_15, PartSize.SIZE_15,
                "mp_fuselage_15_kerosene", "mp_fuselage_15_kerosene_camo", "mp_fuselage_15_kerosene_desert",
                "mp_fuselage_15_kerosene_sky", "mp_fuselage_15_kerosene_insulation",
                "mp_fuselage_15_kerosene_metal", "mp_fuselage_15_kerosene_decorated",
                "mp_fuselage_15_kerosene_steampunk", "mp_fuselage_15_kerosene_polite",
                "mp_fuselage_15_kerosene_blackjack", "mp_fuselage_15_kerosene_lambda",
                "mp_fuselage_15_kerosene_minuteman", "mp_fuselage_15_kerosene_pip",
                "mp_fuselage_15_kerosene_taint", "mp_fuselage_15_kerosene_yuck");
        fuselageGroup(profiles, FuelType.SOLID, 15000F, PartSize.SIZE_15, PartSize.SIZE_15,
                "mp_fuselage_15_solid", "mp_fuselage_15_solid_insulation", "mp_fuselage_15_solid_desh",
                "mp_fuselage_15_solid_soviet_glory", "mp_fuselage_15_solid_soviet_stank",
                "mp_fuselage_15_solid_faust", "mp_fuselage_15_solid_silvermoonlight",
                "mp_fuselage_15_solid_snowy", "mp_fuselage_15_solid_panorama",
                "mp_fuselage_15_solid_roses", "mp_fuselage_15_solid_mimi");
        fuselageGroup(profiles, FuelType.HYDROGEN, 15000F, PartSize.SIZE_15, PartSize.SIZE_15,
                "mp_fuselage_15_hydrogen", "mp_fuselage_15_hydrogen_cathedral");
        fuselage(profiles, "mp_fuselage_15_balefire", FuelType.BALEFIRE, 15000F, PartSize.SIZE_15, PartSize.SIZE_15, 75F);
        fuselageGroup(profiles, FuelType.KEROSENE, 20000F, PartSize.SIZE_15, PartSize.SIZE_20,
                "mp_fuselage_15_20_kerosene", "mp_fuselage_15_20_kerosene_magnusson");
        fuselage(profiles, "mp_fuselage_15_20_solid", FuelType.SOLID, 20000F, PartSize.SIZE_15, PartSize.SIZE_20, 70F);

        warhead(profiles, "mp_warhead_10_he", CustomMissileExplosion.WarheadType.HE, 15F, 1.5F, PartSize.SIZE_10, 5F);
        warhead(profiles, "mp_warhead_10_incendiary", CustomMissileExplosion.WarheadType.INC, 15F, 1.5F, PartSize.SIZE_10, 5F);
        warhead(profiles, "mp_warhead_10_buster", CustomMissileExplosion.WarheadType.BUSTER, 5F, 1.5F, PartSize.SIZE_10, 5F);
        warhead(profiles, "mp_warhead_10_nuclear", CustomMissileExplosion.WarheadType.NUCLEAR, 35F, 1.5F, PartSize.SIZE_10, 10F);
        warhead(profiles, "mp_warhead_10_nuclear_large", CustomMissileExplosion.WarheadType.NUCLEAR, 75F, 2.5F, PartSize.SIZE_10, 15F);
        warhead(profiles, "mp_warhead_10_taint", CustomMissileExplosion.WarheadType.TAINT, 15F, 1.5F, PartSize.SIZE_10, 20F);
        warhead(profiles, "mp_warhead_10_cloud", CustomMissileExplosion.WarheadType.CLOUD, 15F, 1.5F, PartSize.SIZE_10, 20F);
        warhead(profiles, "mp_warhead_15_he", CustomMissileExplosion.WarheadType.HE, 50F, 2.5F, PartSize.SIZE_15, 10F);
        warhead(profiles, "mp_warhead_15_incendiary", CustomMissileExplosion.WarheadType.INC, 35F, 2.5F, PartSize.SIZE_15, 10F);
        warhead(profiles, "mp_warhead_15_nuclear", CustomMissileExplosion.WarheadType.NUCLEAR, 125F, 5F, PartSize.SIZE_15, 15F);
        warhead(profiles, "mp_warhead_15_nuclear_shark", CustomMissileExplosion.WarheadType.NUCLEAR, 125F, 5F, PartSize.SIZE_15, 15F);
        warhead(profiles, "mp_warhead_15_nuclear_mimi", CustomMissileExplosion.WarheadType.NUCLEAR, 125F, 5F, PartSize.SIZE_15, 15F);
        warhead(profiles, "mp_warhead_15_boxcar", CustomMissileExplosion.WarheadType.TX, 250F, 7.5F, PartSize.SIZE_15, 35F);
        warhead(profiles, "mp_warhead_15_n2", CustomMissileExplosion.WarheadType.N2, 100F, 5F, PartSize.SIZE_15, 20F);
        warhead(profiles, "mp_warhead_15_balefire", CustomMissileExplosion.WarheadType.BALEFIRE, 100F, 7.5F, PartSize.SIZE_15, 15F);
        warhead(profiles, "mp_warhead_15_turbine", CustomMissileExplosion.WarheadType.TURBINE, 200F, 5F, PartSize.SIZE_15, 250F);

        return Collections.unmodifiableMap(profiles);
    }

    private static void chip(Map<String, CustomMissilePartProfile> profiles, String name, float inaccuracy) {
        profiles.put(name, new CustomMissilePartProfile(MissilePartItem.PartType.CHIP, PartSize.ANY, PartSize.ANY,
                null, 0F, 0F, 0F, null, 0F, 0F, inaccuracy, 0F));
    }

    private static void thruster(Map<String, CustomMissilePartProfile> profiles, String name, FuelType fuelType,
            float consumption, float lift, PartSize size, float health) {
        profiles.put(name, new CustomMissilePartProfile(MissilePartItem.PartType.THRUSTER, size, PartSize.NONE,
                fuelType, 0F, consumption, lift, null, 0F, 0F, 0F, health));
    }

    private static void fins(Map<String, CustomMissilePartProfile> profiles, String name, float inaccuracy,
            PartSize size, float health) {
        profiles.put(name, new CustomMissilePartProfile(MissilePartItem.PartType.FINS, size, size,
                null, 0F, 0F, 0F, null, 0F, 0F, inaccuracy, health));
    }

    private static void fuselageGroup(Map<String, CustomMissilePartProfile> profiles, FuelType fuelType, float fuel,
            PartSize top, PartSize bottom, String... names) {
        for (String name : names) {
            fuselage(profiles, name, fuelType, fuel, top, bottom, defaultFuselageHealth(fuelType, fuel, top, bottom));
        }
    }

    private static void fuselage(Map<String, CustomMissilePartProfile> profiles, String name, FuelType fuelType,
            float fuel, PartSize top, PartSize bottom, float health) {
        profiles.put(name, new CustomMissilePartProfile(MissilePartItem.PartType.FUSELAGE, top, bottom,
                fuelType, fuel, 0F, 0F, null, 0F, 0F, 0F, health));
    }

    private static float defaultFuselageHealth(FuelType fuelType, float fuel, PartSize top, PartSize bottom) {
        if (top == PartSize.SIZE_15 && bottom == PartSize.SIZE_20) {
            return 70F;
        }
        if (top == PartSize.SIZE_15) {
            return switch (fuelType) {
                case SOLID -> 60F;
                case BALEFIRE -> 75F;
                default -> 50F;
            };
        }
        if (top == PartSize.SIZE_10 && bottom == PartSize.SIZE_15) {
            return 40F;
        }
        if (fuel >= 5000F) {
            return fuelType == FuelType.SOLID ? 35F : 30F;
        }
        return fuelType == FuelType.SOLID ? 25F : 20F;
    }

    private static void warhead(Map<String, CustomMissilePartProfile> profiles, String name,
            CustomMissileExplosion.WarheadType type, float strength, float weight, PartSize size, float health) {
        profiles.put(name, new CustomMissilePartProfile(MissilePartItem.PartType.WARHEAD, PartSize.NONE, size,
                null, 0F, 0F, 0F, type, strength, weight, 0F, health));
    }

    public enum PartSize {
        ANY,
        NONE,
        SIZE_10,
        SIZE_15,
        SIZE_20
    }

    public enum FuelType {
        KEROSENE,
        SOLID,
        HYDROGEN,
        XENON,
        BALEFIRE
    }

    public record ResolvedPart(ResourceLocation itemId, String legacyName, CustomMissilePartProfile profile) {
    }

    public record Assembly(ResolvedPart chip, ResolvedPart warhead, ResolvedPart fuselage,
                           @Nullable ResolvedPart fins, ResolvedPart thruster) {
        public boolean isCompleteForLaunch() {
            return chip != null && warhead != null && fuselage != null && thruster != null
                    && warhead.profile().bottom() == fuselage.profile().top()
                    && warhead.profile().weight() <= thruster.profile().lift()
                    && thruster.profile().top() == fuselage.profile().bottom()
                    && thruster.profile().fuelType() == fuselage.profile().fuelType()
                    && (fins == null || fins.profile().top() == fuselage.profile().bottom());
        }

        public float entityHealth() {
            return 50.0F;
        }

        public float displayHealth() {
            float health = safeHealth(warhead) + safeHealth(fuselage) + safeHealth(thruster);
            return fins == null ? health : health + safeHealth(fins);
        }

        public float launchInaccuracy() {
            float chipInaccuracy = chip == null ? 1.0F : chip.profile().inaccuracy();
            float finInaccuracy = fins == null ? 1.0F : fins.profile().inaccuracy();
            return chipInaccuracy * finInaccuracy;
        }

        public LegacyMissileRadarProfile radarProfile() {
            if (fuselage == null) {
                return LegacyMissileRadarProfile.UNKNOWN;
            }
            PartSize top = fuselage.profile().top();
            PartSize bottom = fuselage.profile().bottom();
            if (top == PartSize.SIZE_10 && bottom == PartSize.SIZE_10) {
                return LegacyMissileRadarProfile.CUSTOM_10;
            }
            if (top == PartSize.SIZE_10 && bottom == PartSize.SIZE_15) {
                return LegacyMissileRadarProfile.CUSTOM_10_15;
            }
            if (top == PartSize.SIZE_15 && bottom == PartSize.SIZE_15) {
                return LegacyMissileRadarProfile.CUSTOM_15;
            }
            if (top == PartSize.SIZE_15 && bottom == PartSize.SIZE_20) {
                return LegacyMissileRadarProfile.CUSTOM_15_20;
            }
            if (top == PartSize.SIZE_20 && bottom == PartSize.SIZE_20) {
                return LegacyMissileRadarProfile.CUSTOM_20;
            }
            return LegacyMissileRadarProfile.TIER1;
        }

        private static float safeHealth(@Nullable ResolvedPart part) {
            return part == null ? 0F : part.profile().health();
        }
    }
}
