package com.hbm.ntm;

import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.damage.DamageResistanceConfig;
import com.hbm.ntm.datagen.HbmDataGenerators;
import com.hbm.ntm.entity.logic.ExplosionChunkLoading;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModCreativeTabs;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.registry.ModParticleTypes;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.radiation.HazmatRegistry;
import com.hbm.ntm.radiation.ItemRadiationRegistry;
import com.hbm.ntm.radiation.LegacyFalloutConversions;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.HbmFluidContainerIngredient;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

@Mod(HbmNtm.MOD_ID)
public class HbmNtm {
    public static final String MOD_ID = "hbm_ntm_rebirth";
    public static final Logger LOGGER = LogUtils.getLogger();

    public HbmNtm() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModFluids.register(modBus);
        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenuTypes.register(modBus);
        ModRecipes.register(modBus);
        ModSounds.register(modBus);
        ModEffects.register(modBus);
        ModEntityTypes.register(modBus);
        ModParticleTypes.PARTICLE_TYPES.register(modBus);
        ModCreativeTabs.register(modBus);
        HbmFluidContainerIngredient.register();

        modBus.addListener(this::commonSetup);
        modBus.addListener(HbmDataGenerators::gatherData);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HbmCommonConfig.SPEC, "hbm_ntm_rebirth-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HbmClientConfig.SPEC, "hbm_ntm_rebirth-client.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            HazmatRegistry.registerDefaults();
            ItemRadiationRegistry.registerDefaults();
            DamageResistanceConfig.LoadReport damageReport = DamageResistanceConfig.initialize(FMLPaths.CONFIGDIR.get());
            LOGGER.info("Loaded {}.", damageReport.summary());
            damageReport.warnings().forEach(warning -> LOGGER.warn("Damage resistance config: missing migrated {}", warning));
            LegacyFalloutConversions.LoadReport falloutReport = LegacyFalloutConversions.initialize(FMLPaths.CONFIGDIR.get());
            LOGGER.info("Loaded {}.", falloutReport.summary());
            falloutReport.warnings().forEach(warning -> LOGGER.warn("Fallout conversion config: {}", warning));
            HbmFluids.bootstrap();
            ExplosionChunkLoading.registerValidationCallback();
            ModMessages.register();
            ModMessages.logProtocolAudit();
            HbmBatteryTransfer.setCreativeBatteryPredicate(stack -> stack.is(ModItems.BATTERY_CREATIVE.get()));
        });
        if (HbmCommonConfig.LOG_STARTUP.get()) {
            LOGGER.info("HBM NTM migration scaffold loaded. Source semantics: 1.7.10 first, 1.20.1 reference second.");
        }
    }
}
