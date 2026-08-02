package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.worldgen.LegacyFlowerPatchFeature;
import com.hbm.ntm.worldgen.LegacyOreSetFeature;
import com.hbm.ntm.worldgen.LegacyReedPatchFeature;
import com.hbm.ntm.worldgen.LegacySellafieldCraterFeature;
import com.hbm.ntm.worldgen.LegacySurfaceFixturesFeature;
import com.hbm.ntm.worldgen.GlyphidHiveFeature;
import com.hbm.ntm.worldgen.GroundMeteoriteFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, HbmNtm.MOD_ID);

    public static final RegistryObject<Feature<LegacyOreSetFeature.Configuration>> LEGACY_ORE_SET =
            register("legacy_ore_set", new LegacyOreSetFeature(LegacyOreSetFeature.Configuration.CODEC));
    public static final RegistryObject<Feature<LegacyFlowerPatchFeature.Configuration>> LEGACY_FLOWER_PATCH =
            register("legacy_flower_patch", new LegacyFlowerPatchFeature(LegacyFlowerPatchFeature.Configuration.CODEC));
    public static final RegistryObject<Feature<LegacyReedPatchFeature.Configuration>> LEGACY_REED_PATCH =
            register("legacy_reed_patch", new LegacyReedPatchFeature(LegacyReedPatchFeature.Configuration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GLYPHID_HIVE =
            register("glyphid_hive", new GlyphidHiveFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GROUND_METEORITE =
            register("ground_meteorite", new GroundMeteoriteFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> LEGACY_SELLAFIELD_CRATER =
            register("legacy_sellafield_crater", new LegacySellafieldCraterFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> LEGACY_SURFACE_FIXTURES =
            register("legacy_surface_fixtures", new LegacySurfaceFixturesFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> RegistryObject<F> register(String name, F feature) {
        return FEATURES.register(name, () -> feature);
    }

    private ModFeatures() {
    }
}
