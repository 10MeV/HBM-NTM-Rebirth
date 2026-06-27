package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.worldgen.LegacyOreSetFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, HbmNtm.MOD_ID);

    public static final RegistryObject<Feature<LegacyOreSetFeature.Configuration>> LEGACY_ORE_SET =
            register("legacy_ore_set", new LegacyOreSetFeature(LegacyOreSetFeature.Configuration.CODEC));

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> RegistryObject<F> register(String name, F feature) {
        return FEATURES.register(name, () -> feature);
    }

    private ModFeatures() {
    }
}
