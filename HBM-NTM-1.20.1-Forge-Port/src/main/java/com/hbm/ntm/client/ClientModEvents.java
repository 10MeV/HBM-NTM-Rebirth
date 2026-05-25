package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyModelReloadListener;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.particle.AshesParticle;
import com.hbm.ntm.client.particle.BlackPowderSmokeParticle;
import com.hbm.ntm.client.particle.BlackPowderSparkParticle;
import com.hbm.ntm.client.particle.CoolingTowerParticle;
import com.hbm.ntm.client.particle.DeadLeafParticle;
import com.hbm.ntm.client.particle.ExplosionSmallParticle;
import com.hbm.ntm.client.particle.FlamethrowerParticle;
import com.hbm.ntm.client.particle.FluidFillParticle;
import com.hbm.ntm.client.particle.FoamParticle;
import com.hbm.ntm.client.particle.GasFlameParticle;
import com.hbm.ntm.client.particle.GibletParticle;
import com.hbm.ntm.client.particle.HadronParticle;
import com.hbm.ntm.client.particle.HazeParticle;
import com.hbm.ntm.client.particle.HbmSmokeParticle;
import com.hbm.ntm.client.particle.LegacySplashParticle;
import com.hbm.ntm.client.particle.MukeWaveParticle;
import com.hbm.ntm.client.particle.NetworkDebugParticle;
import com.hbm.ntm.client.particle.PlasmaBlastParticle;
import com.hbm.ntm.client.particle.RadiationFogParticle;
import com.hbm.ntm.client.particle.RbmkAnimatedParticle;
import com.hbm.ntm.client.particle.SchrabFogParticle;
import com.hbm.ntm.client.particle.SmokePlumeParticle;
import com.hbm.ntm.client.particle.TownAuraParticle;
import com.hbm.ntm.client.particle.RocketFlameParticle;
import com.hbm.ntm.client.renderer.AssemblyMachineRenderer;
import com.hbm.ntm.client.renderer.BasicMachineRenderer;
import com.hbm.ntm.client.renderer.ChemicalPlantRenderer;
import com.hbm.ntm.client.renderer.CloudFleijaRenderer;
import com.hbm.ntm.client.renderer.CloudFleijaRainbowRenderer;
import com.hbm.ntm.client.renderer.CloudSoliniumRenderer;
import com.hbm.ntm.client.renderer.FluidTankRenderer;
import com.hbm.ntm.client.renderer.LegacyDemonLampBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyLanternBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyLightBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyVisibleMachineRenderer;
import com.hbm.ntm.client.renderer.LiquefactorRenderer;
import com.hbm.ntm.client.renderer.MachineBatterySocketRenderer;
import com.hbm.ntm.client.renderer.MovingPackageRenderer;
import com.hbm.ntm.client.renderer.MovingItemRenderer;
import com.hbm.ntm.client.renderer.NuclearDeviceRenderer;
import com.hbm.ntm.client.renderer.NukeTorexRenderer;
import com.hbm.ntm.client.renderer.RubbleRenderer;
import com.hbm.ntm.client.renderer.ShrapnelRenderer;
import com.hbm.ntm.client.renderer.TrinketBlockEntityRenderer;
import com.hbm.ntm.client.screen.BasicMachineScreen;
import com.hbm.ntm.client.screen.AssemblyMachineScreen;
import com.hbm.ntm.client.screen.ChemicalPlantScreen;
import com.hbm.ntm.client.screen.FluidTankScreen;
import com.hbm.ntm.client.screen.LiquefactorScreen;
import com.hbm.ntm.client.screen.MachineBatteryScreen;
import com.hbm.ntm.client.screen.MachineBatterySocketScreen;
import com.hbm.ntm.client.screen.NuclearDeviceScreen;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.FluidPipeBlockItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.registry.ModParticleTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.BASIC_MACHINE.get(), BasicMachineScreen::new);
            MenuScreens.register(ModMenuTypes.ASSEMBLY_MACHINE.get(), AssemblyMachineScreen::new);
            MenuScreens.register(ModMenuTypes.CHEMICAL_PLANT.get(), ChemicalPlantScreen::new);
            MenuScreens.register(ModMenuTypes.LIQUEFACTOR.get(), LiquefactorScreen::new);
            MenuScreens.register(ModMenuTypes.FLUID_TANK.get(), FluidTankScreen::new);
            MenuScreens.register(ModMenuTypes.MACHINE_BATTERY.get(), MachineBatteryScreen::new);
            MenuScreens.register(ModMenuTypes.MACHINE_BATTERY_SOCKET.get(), MachineBatterySocketScreen::new);
            MenuScreens.register(ModMenuTypes.NUCLEAR_DEVICE.get(), NuclearDeviceScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BASIC_MACHINE.get(), BasicMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_BATTERY_SOCKET.get(), MachineBatterySocketRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_MACHINE.get(), AssemblyMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHEMICAL_PLANT.get(), ChemicalPlantRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LIQUEFACTOR.get(), LiquefactorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_TANK.get(), FluidTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_VISIBLE_MACHINE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TRINKET.get(), TrinketBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_LIGHT.get(), LegacyLightBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_DEMON_LAMP.get(), LegacyDemonLampBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_LANTERN.get(), LegacyLanternBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUCLEAR_DEVICE.get(), NuclearDeviceRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MOVING_ITEM.get(), MovingItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MOVING_PACKAGE.get(), MovingPackageRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NUKE_EXPLOSION_MK5.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NUKE_EXPLOSION_MK3.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NUKE_TOREX.get(), NukeTorexRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FALLOUT_RAIN.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLOUD_FLEIJA.get(), CloudFleijaRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLOUD_SOLINIUM.get(), CloudSoliniumRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLOUD_FLEIJA_RAINBOW.get(), CloudFleijaRainbowRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BALEFIRE_EXPLOSION.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FALLING_NUKE.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SHRAPNEL.get(), ShrapnelRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RUBBLE.get(), RubbleRenderer::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        TrinketBlockEntityRenderer.registerAdditionalModels();
        ObjModelLibrary.registerAdditionalModels(event);
    }

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new LegacyModelReloadListener());
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ModItems.PARTS_TAB_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(DepletedFuelItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) ->
                        DepletedFuelItem.isHot(stack) ? DepletedFuelItem.HOT_TINT : 0xFFFFFF, item));
        ModItems.CONSUMABLE_TAB_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(HbmFluidContainerItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) -> ((HbmFluidContainerItem) item).getTintColor(stack), item));
        ModItems.CONTROL_FLUID_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(FluidIdentifierItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) -> ((FluidIdentifierItem) item).getTintColor(stack, tintIndex), item));
        event.register((stack, tintIndex) -> {
            if (stack.getItem() instanceof FluidPipeBlockItem pipe) {
                return pipe.getTintColor(stack, tintIndex);
            }
            return 0xFFFFFF;
        }, ModBlocks.FLUID_DUCT_NEO.get().asItem());
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        HbmClientKeybinds.register(event);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.RADIATION_FOG.get(), RadiationFogParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ROCKET_FLAME.get(), RocketFlameParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.GAS_FLAME.get(), GasFlameParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.CONTRAIL.get(), HbmSmokeParticle.ContrailProvider::new);
        event.registerSpriteSet(ModParticleTypes.SMOKE_PLUME.get(), SmokePlumeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.LAUNCH_SMOKE.get(), HbmSmokeParticle.LaunchSmokeProvider::new);
        event.registerSpriteSet(ModParticleTypes.EX_SMOKE.get(), HbmSmokeParticle.ExSmokeProvider::new);
        event.registerSpriteSet(ModParticleTypes.FOAM.get(), FoamParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FLAMETHROWER.get(), FlamethrowerParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FLAMETHROWER_BALEFIRE.get(),
                sprites -> new FlamethrowerParticle.Provider(sprites, FlamethrowerParticle.META_BALEFIRE));
        event.registerSpriteSet(ModParticleTypes.FLAMETHROWER_DIGAMMA.get(),
                sprites -> new FlamethrowerParticle.Provider(sprites, FlamethrowerParticle.META_DIGAMMA));
        event.registerSpriteSet(ModParticleTypes.FLAMETHROWER_OXY.get(),
                sprites -> new FlamethrowerParticle.Provider(sprites, FlamethrowerParticle.META_OXY));
        event.registerSpriteSet(ModParticleTypes.FLAMETHROWER_BLACK.get(),
                sprites -> new FlamethrowerParticle.Provider(sprites, FlamethrowerParticle.META_BLACK));
        event.registerSpriteSet(ModParticleTypes.BLACK_POWDER_SPARK.get(), BlackPowderSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.EXPLOSION_SMALL.get(), ExplosionSmallParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.BLACK_POWDER_SMOKE.get(), BlackPowderSmokeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ASHES.get(), AshesParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.MUKE_WAVE.get(), MukeWaveParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.TOWN_AURA.get(), TownAuraParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SCHRAB_FOG.get(), SchrabFogParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.DEAD_LEAF.get(), DeadLeafParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.GIBLET_MEAT.get(), sprites -> new GibletParticle.Provider(sprites, GibletParticle.TYPE_MEAT));
        event.registerSpriteSet(ModParticleTypes.GIBLET_SLIME.get(), sprites -> new GibletParticle.Provider(sprites, GibletParticle.TYPE_SLIME));
        event.registerSpriteSet(ModParticleTypes.GIBLET_METAL.get(), sprites -> new GibletParticle.Provider(sprites, GibletParticle.TYPE_METAL));
        event.registerSpriteSet(ModParticleTypes.HADRON.get(), HadronParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.RBMK_FLAME.get(), RbmkAnimatedParticle.FlameProvider::new);
        event.registerSpriteSet(ModParticleTypes.RBMK_STEAM.get(), RbmkAnimatedParticle.SteamProvider::new);
        event.registerSpriteSet(ModParticleTypes.RBMK_MUSH.get(), RbmkAnimatedParticle.MushProvider::new);
        event.registerSpriteSet(ModParticleTypes.COOLING_TOWER.get(), CoolingTowerParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SPLASH.get(), LegacySplashParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FLUID_FILL.get(), FluidFillParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.NETWORK_POWER.get(), NetworkDebugParticle.PowerProvider::new);
        event.registerSpriteSet(ModParticleTypes.NETWORK_FLUID.get(), NetworkDebugParticle.FluidProvider::new);
        event.registerSpriteSet(ModParticleTypes.PLASMA_BLAST.get(), PlasmaBlastParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.HAZE.get(), HazeParticle.Provider::new);
    }

    private ClientModEvents() {
    }
}
