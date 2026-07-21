package com.hbm.ntm;

import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.bullet.BulletConfigSyncRegistry;
import com.hbm.ntm.compat.CompatRecipeRegistry;
import com.hbm.ntm.damage.DamageResistanceConfig;
import com.hbm.ntm.datagen.HbmDataGenerators;
import com.hbm.ntm.entity.logic.ExplosionChunkLoading;
import com.hbm.ntm.entity.mob.EntityCyberCrab;
import com.hbm.ntm.entity.mob.EntityCreeperNuclear;
import com.hbm.ntm.entity.mob.EntityCreeperTainted;
import com.hbm.ntm.entity.mob.EntityDuck;
import com.hbm.ntm.entity.mob.EntityFBI;
import com.hbm.ntm.entity.mob.EntityFBIDrone;
import com.hbm.ntm.entity.mob.EntityUndeadSoldier;
import com.hbm.ntm.entity.mob.EntityRADBeast;
import com.hbm.ntm.entity.mob.EntityParasiteMaggot;
import com.hbm.ntm.entity.mob.EntityTaintCrab;
import com.hbm.ntm.entity.mob.EntityTeslaCrab;
import com.hbm.entity.mob.glyphid.EntityGlyphidDigger;
import com.hbm.entity.mob.glyphid.EntityGlyphidBrawler;
import com.hbm.entity.mob.glyphid.EntityGlyphidBombardier;
import com.hbm.entity.mob.glyphid.EntityGlyphidBlaster;
import com.hbm.entity.mob.glyphid.EntityGlyphidBehemoth;
import com.hbm.entity.mob.glyphid.EntityGlyphidBrenda;
import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.entity.mob.glyphid.EntityGlyphidNuclear;
import com.hbm.entity.mob.glyphid.EntityGlyphidScout;
import com.hbm.ntm.fluid.HbmCompatFluidRegistry;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluidContainerConfig;
import com.hbm.ntm.fluid.HbmFluidForgeAliasConfig;
import com.hbm.ntm.fluid.HbmFluidForgeMappings;
import com.hbm.ntm.fluid.HbmFluidTypeConfig;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.gametest.EnergyMk2GameTests;
import com.hbm.ntm.gametest.ConveyorGameTests;
import com.hbm.ntm.gametest.DroneLogisticsGameTests;
import com.hbm.ntm.gametest.AbilityGameTests;
import com.hbm.ntm.gametest.OilSpotGameTests;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModCreativeTabs;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModFeatures;
import com.hbm.ntm.registry.ModFluids;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.LegacyHbmStatistics;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.registry.ModParticleTypes;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.neutron.RBMKDialRuntime;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.util.AchievementHandler;
import com.hbm.ntm.util.LegacyPolaroidVariant;
import com.hbm.ntm.radiation.HazmatResistanceConfig;
import com.hbm.ntm.radiation.ItemRadiationRegistry;
import com.hbm.ntm.radiation.LegacyFalloutConversions;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.HbmFluidContainerIngredient;
import com.hbm.ntm.recipe.DroneVariantIngredient;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
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
        // 1.7.10 MainRegistry chose the process-local polaroid ID during
        // PreLoad, before it registered ItemCustomLore consumers.
        LegacyPolaroidVariant.bootstrap();
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModFluids.register(modBus);
        ModItems.register(modBus);
        LegacyHbmStatistics.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenuTypes.register(modBus);
        ModRecipes.register(modBus);
        ModSounds.register(modBus);
        ModEffects.register(modBus);
        ModEntityTypes.register(modBus);
        ModFeatures.register(modBus);
        ModParticleTypes.PARTICLE_TYPES.register(modBus);
        ModCreativeTabs.register(modBus);
        RBMKDialRuntime.bootstrap();
        HbmFluidContainerIngredient.register();
        DroneVariantIngredient.register();

        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerEntityAttributes);
        modBus.addListener(HbmDataGenerators::gatherData);
        modBus.addListener(EnergyMk2GameTests::register);
        modBus.addListener(ConveyorGameTests::register);
        modBus.addListener(DroneLogisticsGameTests::register);
        modBus.addListener(AbilityGameTests::register);
        modBus.addListener(OilSpotGameTests::register);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, HbmCommonConfig.SPEC, "hbm_ntm_rebirth-common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, HbmClientConfig.SPEC, "hbm_ntm_rebirth-client.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 1.7.10 MainRegistry performs ArmorUtil registration before loading hbmRadResist.json.
            // This replays the legacy public external protection list into the single Hazmat registry.
            com.hbm.util.ArmorUtil.register();
            HazmatResistanceConfig.LoadReport hazmatReport = HazmatResistanceConfig.initialize(FMLPaths.CONFIGDIR.get());
            LOGGER.info("Loaded {}.", hazmatReport.summary());
            hazmatReport.warnings().forEach(warning -> LOGGER.warn("Hazmat resistance config: {}", warning));
            ItemRadiationRegistry.registerDefaults();
            DamageResistanceConfig.LoadReport damageReport = DamageResistanceConfig.initialize(FMLPaths.CONFIGDIR.get());
            LOGGER.info("Loaded {}.", damageReport.summary());
            damageReport.warnings().forEach(warning -> LOGGER.warn("Damage resistance config: {}", warning));
            LegacyFalloutConversions.LoadReport falloutReport = LegacyFalloutConversions.initialize(FMLPaths.CONFIGDIR.get());
            LOGGER.info("Loaded {}.", falloutReport.summary());
            falloutReport.warnings().forEach(warning -> LOGGER.warn("Fallout conversion config: {}", warning));
            var fluidTraitReport = HbmFluids.bootstrap(FMLPaths.CONFIGDIR.get());
            var fluidTypeReport = HbmFluidTypeConfig.loadReport();
            LOGGER.info("Loaded {}.", fluidTypeReport.summary());
            fluidTypeReport.warnings().forEach(warning -> LOGGER.warn("Fluid type config: {}", warning));
            LOGGER.info("Loaded {}.", HbmCompatFluidRegistry.diagnostics().summary());
            LOGGER.info("Loaded {}.", CompatRecipeRegistry.diagnostics().summary());
            LOGGER.info("Loaded {}.", HbmFluidContainerRegistry.diagnostics().summary());
            LOGGER.info("Loaded {}.", HbmFluidContainerConfig.loadReport().summary());
            HbmFluidContainerConfig.loadReport().warnings().forEach(warning -> LOGGER.warn("Fluid container config: {}", warning));
            LOGGER.info("Loaded {}.", HbmFluidForgeAliasConfig.loadReport().summary());
            HbmFluidForgeAliasConfig.loadReport().warnings().forEach(warning -> LOGGER.warn("Forge fluid alias config: {}", warning));
            LOGGER.info("Loaded {}.", HbmFluidForgeMappings.diagnostics().summary());
            LOGGER.info("Loaded {}.", fluidTraitReport.summary());
            fluidTraitReport.warnings().forEach(warning -> LOGGER.warn("Fluid trait config: {}", warning));
            BulletConfigSyncRegistry.bootstrap();
            LOGGER.info("Loaded {} legacy synced bullet configs.", BulletConfigSyncRegistry.syncedConfigs().size());
            ExplosionChunkLoading.registerValidationCallback();
            ModMessages.register();
            ModMessages.logProtocolAudit();
            HbmBatteryTransfer.setCreativeBatteryPredicate(stack -> stack.is(ModItems.BATTERY_CREATIVE.get()));
            // MainRegistry invokes Satellite.register() after the legacy item
            // registry is available.  Besides the modern type lookup, that
            // call populates the public old-package itemToClass table used by
            // Satellite#getIDFromItem and externally registered satellites.
            com.hbm.saveddata.satellites.Satellite.register();
            ModItems.registerToolStacks();
            AchievementHandler.register();
        });
        if (HbmCommonConfig.startupLoggingEnabled()) {
            LOGGER.info("HBM NTM migration scaffold loaded. Source semantics: 1.7.10 first, 1.20.1 reference second.");
        }
    }

    private void registerEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.NUCLEAR_CREEPER.get(), EntityCreeperNuclear.createAttributes().build());
        event.put(ModEntityTypes.TAINTED_CREEPER.get(), EntityCreeperTainted.createAttributes().build());
        event.put(ModEntityTypes.CYBER_CRAB.get(), EntityCyberCrab.createAttributes().build());
        event.put(ModEntityTypes.TESLA_CRAB.get(), EntityTeslaCrab.createAttributes().build());
        event.put(ModEntityTypes.TAINT_CRAB.get(), EntityTaintCrab.createAttributes().build());
        event.put(ModEntityTypes.DUCK.get(), EntityDuck.createAttributes().build());
        event.put(ModEntityTypes.FBI_DRONE.get(), EntityFBIDrone.createAttributes().build());
        event.put(ModEntityTypes.FBI.get(), EntityFBI.createAttributes().build());
        event.put(ModEntityTypes.UNDEAD_SOLDIER.get(), EntityUndeadSoldier.createAttributes().build());
        event.put(ModEntityTypes.RAD_BEAST.get(), EntityRADBeast.createAttributes().build());
        event.put(ModEntityTypes.GLYPHID.get(), EntityGlyphid.createAttributes().build());
        event.put(ModEntityTypes.GLYPHID_SCOUT.get(), EntityGlyphidScout.createAttributes().build());
        event.put(ModEntityTypes.GLYPHID_DIGGER.get(), EntityGlyphidDigger.createAttributes().build());
        event.put(ModEntityTypes.GLYPHID_BRAWLER.get(), EntityGlyphidBrawler.createAttributes().build());
        event.put(ModEntityTypes.GLYPHID_BOMBARDIER.get(), EntityGlyphidBombardier.createAttributes().build());
        event.put(ModEntityTypes.GLYPHID_BLASTER.get(), EntityGlyphidBlaster.createAttributes().build());
        event.put(ModEntityTypes.GLYPHID_BEHEMOTH.get(), EntityGlyphidBehemoth.createAttributes().build());
        event.put(ModEntityTypes.GLYPHID_BRENDA.get(), EntityGlyphidBrenda.createAttributes().build());
        event.put(ModEntityTypes.GLYPHID_NUCLEAR.get(), EntityGlyphidNuclear.createAttributes().build());
        event.put(ModEntityTypes.PARASITE_MAGGOT.get(), EntityParasiteMaggot.createAttributes().build());
    }
}
