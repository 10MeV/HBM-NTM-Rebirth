package com.hbm.ntm;

import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.datagen.HbmDataGenerators;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModCreativeTabs;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.radiation.HazmatRegistry;
import com.hbm.ntm.radiation.ItemRadiationRegistry;
import com.hbm.ntm.recipe.ModRecipes;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(HbmNtm.MOD_ID)
public class HbmNtm {
    public static final String MOD_ID = "hbm";
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
        ModCreativeTabs.register(modBus);

        modBus.addListener(this::commonSetup);
        modBus.addListener(HbmDataGenerators::gatherData);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HbmCommonConfig.SPEC, "hbm-common.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            HazmatRegistry.registerDefaults();
            ItemRadiationRegistry.registerDefaults();
            HbmFluids.bootstrap();
            ModMessages.register();
        });
        if (HbmCommonConfig.LOG_STARTUP.get()) {
            LOGGER.info("HBM NTM migration scaffold loaded. Source semantics: 1.7.10 first, 1.20.1 reference second.");
        }
    }
}
