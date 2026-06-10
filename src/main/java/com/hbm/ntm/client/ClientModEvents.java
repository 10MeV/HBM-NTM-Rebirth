package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyModelReloadListener;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.particle.AshesParticle;
import com.hbm.ntm.client.particle.BlackPowderSmokeParticle;
import com.hbm.ntm.client.particle.BlackPowderSparkParticle;
import com.hbm.ntm.client.particle.CoolingTowerParticle;
import com.hbm.ntm.client.particle.DeadLeafParticle;
import com.hbm.ntm.client.particle.DigammaSmokeParticle;
import com.hbm.ntm.client.particle.ExplosionSmallParticle;
import com.hbm.ntm.client.particle.FlamethrowerParticle;
import com.hbm.ntm.client.particle.FluidFillParticle;
import com.hbm.ntm.client.particle.FoamParticle;
import com.hbm.ntm.client.particle.GasFlameParticle;
import com.hbm.ntm.client.particle.GibletParticle;
import com.hbm.ntm.client.particle.HadronParticle;
import com.hbm.ntm.client.particle.HazeParticle;
import com.hbm.ntm.client.particle.HbmSmokeParticle;
import com.hbm.ntm.client.particle.LargeExplodeParticle;
import com.hbm.ntm.client.particle.LegacyContrailParticle;
import com.hbm.ntm.client.particle.LegacySplashParticle;
import com.hbm.ntm.client.particle.MukeWaveParticle;
import com.hbm.ntm.client.particle.NetworkDebugParticle;
import com.hbm.ntm.client.particle.PlasmaBlastParticle;
import com.hbm.ntm.client.particle.RadiationFogParticle;
import com.hbm.ntm.client.particle.RbmkAnimatedParticle;
import com.hbm.ntm.client.particle.SchrabFogParticle;
import com.hbm.ntm.client.particle.SmokePlumeParticle;
import com.hbm.ntm.client.particle.TownAuraParticle;
import com.hbm.ntm.client.render.HbmBlackHoleEffects;
import com.hbm.ntm.client.render.HbmRenderEffects;
import com.hbm.ntm.client.particle.RocketFlameParticle;
import com.hbm.ntm.client.renderer.AssemblyFactoryRenderer;
import com.hbm.ntm.client.renderer.AssemblyMachineRenderer;
import com.hbm.ntm.client.renderer.BasicMachineRenderer;
import com.hbm.ntm.client.renderer.BulletProjectileRenderer;
import com.hbm.ntm.client.renderer.CableDiodeRenderer;
import com.hbm.ntm.client.renderer.ChemicalFactoryRenderer;
import com.hbm.ntm.client.renderer.ChemicalPlantRenderer;
import com.hbm.ntm.client.renderer.CloudFleijaRenderer;
import com.hbm.ntm.client.renderer.CloudFleijaRainbowRenderer;
import com.hbm.ntm.client.renderer.CloudSoliniumRenderer;
import com.hbm.ntm.client.renderer.CustomNukeRenderer;
import com.hbm.ntm.client.renderer.DeathBlastRenderer;
import com.hbm.ntm.client.renderer.EmpBlastRenderer;
import com.hbm.ntm.client.renderer.FalloutRainRenderer;
import com.hbm.ntm.client.renderer.FluidPipeRenderer;
import com.hbm.ntm.client.renderer.FluidPipeAnchorRenderer;
import com.hbm.ntm.client.renderer.FluidTankRenderer;
import com.hbm.ntm.client.renderer.LegacyChargeBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyDemonLampBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyLanternBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyLightBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyPylonRenderer;
import com.hbm.ntm.client.renderer.LegacyPrimedExplosiveRenderer;
import com.hbm.ntm.client.renderer.LegacyVisibleMachineRenderer;
import com.hbm.ntm.client.renderer.LiquefactorRenderer;
import com.hbm.ntm.client.renderer.MachineBatterySocketRenderer;
import com.hbm.ntm.client.renderer.MinerRocketRenderer;
import com.hbm.ntm.client.renderer.MissileRenderer;
import com.hbm.ntm.client.renderer.MovingPackageRenderer;
import com.hbm.ntm.client.renderer.MovingItemRenderer;
import com.hbm.ntm.client.renderer.NuclearDeviceRenderer;
import com.hbm.ntm.client.renderer.NukeTorexRenderer;
import com.hbm.ntm.client.renderer.OilDrillRenderer;
import com.hbm.ntm.client.renderer.PyroOvenRenderer;
import com.hbm.ntm.client.renderer.RadarRenderer;
import com.hbm.ntm.client.renderer.RadarScreenRenderer;
import com.hbm.ntm.client.renderer.RadioAutocalRenderer;
import com.hbm.ntm.client.renderer.RadioTelexRenderer;
import com.hbm.ntm.client.renderer.RadioTorchRenderer;
import com.hbm.ntm.client.renderer.RBMKPanelRenderer;
import com.hbm.ntm.client.renderer.RedCableRenderer;
import com.hbm.ntm.client.renderer.RubbleRenderer;
import com.hbm.ntm.client.renderer.ShrapnelRenderer;
import com.hbm.ntm.client.renderer.SoyuzCapsuleBlockEntityRenderer;
import com.hbm.ntm.client.renderer.SoyuzCapsuleRenderer;
import com.hbm.ntm.client.renderer.SoyuzLauncherRenderer;
import com.hbm.ntm.client.renderer.SoyuzRenderer;
import com.hbm.ntm.client.renderer.TrinketBlockEntityRenderer;
import com.hbm.ntm.client.renderer.VendingMachineRenderer;
import com.hbm.ntm.client.screen.ArmorTableScreen;
import com.hbm.ntm.client.screen.ArcWelderScreen;
import com.hbm.ntm.client.screen.AshpitScreen;
import com.hbm.ntm.client.screen.BasicMachineScreen;
import com.hbm.ntm.client.screen.AssemblyFactoryScreen;
import com.hbm.ntm.client.screen.AssemblyMachineScreen;
import com.hbm.ntm.client.screen.ChemicalFactoryScreen;
import com.hbm.ntm.client.screen.ChemicalPlantScreen;
import com.hbm.ntm.client.screen.CompressorScreen;
import com.hbm.ntm.client.screen.CustomNukeScreen;
import com.hbm.ntm.client.screen.FluidTankScreen;
import com.hbm.ntm.client.screen.GasFlareScreen;
import com.hbm.ntm.client.screen.LaunchPadScreen;
import com.hbm.ntm.client.screen.LiquefactorScreen;
import com.hbm.ntm.client.screen.MachineBatteryScreen;
import com.hbm.ntm.client.screen.MachineBatterySocketScreen;
import com.hbm.ntm.client.screen.NuclearDeviceScreen;
import com.hbm.ntm.client.screen.OilDrillScreen;
import com.hbm.ntm.client.screen.PyroOvenScreen;
import com.hbm.ntm.client.screen.RadioAutocalScreen;
import com.hbm.ntm.client.screen.RadioTelexScreen;
import com.hbm.ntm.client.screen.RadioTorchScreen;
import com.hbm.ntm.client.screen.RBMKPanelScreen;
import com.hbm.ntm.client.screen.RadarScreen;
import com.hbm.ntm.client.screen.RefineryScreen;
import com.hbm.ntm.client.screen.RemoteFluidMachineScreen;
import com.hbm.ntm.client.screen.SatelliteDockScreen;
import com.hbm.ntm.client.screen.SatelliteLinkerScreen;
import com.hbm.ntm.client.screen.SolidifierScreen;
import com.hbm.ntm.client.screen.SoyuzCapsuleScreen;
import com.hbm.ntm.client.screen.SoyuzLauncherScreen;
import com.hbm.ntm.client.screen.ToolAbilityScreen;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.block.LegacySellafieldSlakedBlock;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.FluidIconItem;
import com.hbm.ntm.item.FluidPipeBlockItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.OreByproductItem;
import com.hbm.ntm.radiation.CraterRadiationData;
import com.hbm.ntm.radiation.CraterBiomeUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModEntityTypes;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.registry.ModParticleTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.io.IOException;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private static final PerlinSimplexNoise CRATER_PLANT_NOISE =
            new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(2345L)), List.of(0));

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.BASIC_MACHINE.get(), BasicMachineScreen::new);
            MenuScreens.register(ModMenuTypes.ARMOR_TABLE.get(), ArmorTableScreen::new);
            MenuScreens.register(ModMenuTypes.ASSEMBLY_MACHINE.get(), AssemblyMachineScreen::new);
            MenuScreens.register(ModMenuTypes.CHEMICAL_PLANT.get(), ChemicalPlantScreen::new);
            MenuScreens.register(ModMenuTypes.ASSEMBLY_FACTORY.get(), AssemblyFactoryScreen::new);
            MenuScreens.register(ModMenuTypes.CHEMICAL_FACTORY.get(), ChemicalFactoryScreen::new);
            MenuScreens.register(ModMenuTypes.COMPRESSOR.get(), CompressorScreen::new);
            MenuScreens.register(ModMenuTypes.ARC_WELDER.get(), ArcWelderScreen::new);
            MenuScreens.register(ModMenuTypes.LIQUEFACTOR.get(), LiquefactorScreen::new);
            MenuScreens.register(ModMenuTypes.REFINERY.get(), RefineryScreen::new);
            MenuScreens.register(ModMenuTypes.SOLIDIFIER.get(), SolidifierScreen::new);
            MenuScreens.register(ModMenuTypes.OIL_DRILL.get(), OilDrillScreen::new);
            MenuScreens.register(ModMenuTypes.GAS_FLARE.get(), GasFlareScreen::new);
            MenuScreens.register(ModMenuTypes.PYRO_OVEN.get(), PyroOvenScreen::new);
            MenuScreens.register(ModMenuTypes.ASHPIT.get(), AshpitScreen::new);
            MenuScreens.register(ModMenuTypes.FLUID_TANK.get(), FluidTankScreen::new);
            MenuScreens.register(ModMenuTypes.REMOTE_FLUID_MACHINE.get(), RemoteFluidMachineScreen::new);
            MenuScreens.register(ModMenuTypes.MACHINE_BATTERY.get(), MachineBatteryScreen::new);
            MenuScreens.register(ModMenuTypes.MACHINE_BATTERY_SOCKET.get(), MachineBatterySocketScreen::new);
            MenuScreens.register(ModMenuTypes.NUCLEAR_DEVICE.get(), NuclearDeviceScreen::new);
            MenuScreens.register(ModMenuTypes.CUSTOM_NUKE.get(), CustomNukeScreen::new);
            MenuScreens.register(ModMenuTypes.RADAR.get(), RadarScreen::new);
            MenuScreens.register(ModMenuTypes.SATELLITE_LINKER.get(), SatelliteLinkerScreen::new);
            MenuScreens.register(ModMenuTypes.SATELLITE_DOCK.get(), SatelliteDockScreen::new);
            MenuScreens.register(ModMenuTypes.SOYUZ_CAPSULE.get(), SoyuzCapsuleScreen::new);
            MenuScreens.register(ModMenuTypes.SOYUZ_LAUNCHER.get(), SoyuzLauncherScreen::new);
            MenuScreens.register(ModMenuTypes.LAUNCH_PAD.get(), LaunchPadScreen::new);
            MenuScreens.register(ModMenuTypes.TOOL_ABILITY.get(), ToolAbilityScreen::new);
            MenuScreens.register(ModMenuTypes.RADIO_TORCH.get(), RadioTorchScreen::new);
            MenuScreens.register(ModMenuTypes.RADIO_AUTOCAL.get(), RadioAutocalScreen::new);
            MenuScreens.register(ModMenuTypes.RADIO_TELEX.get(), RadioTelexScreen::new);
            MenuScreens.register(ModMenuTypes.RBMK_PANEL.get(), RBMKPanelScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BASIC_MACHINE.get(), BasicMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RED_CABLE.get(), RedCableRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CABLE_DIODE.get(), CableDiodeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RED_CONNECTOR.get(), LegacyPylonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RED_PYLON.get(), LegacyPylonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RED_PYLON_MEDIUM.get(), LegacyPylonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RED_PYLON_LARGE.get(), LegacyPylonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SUBSTATION.get(), LegacyPylonRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_SENDER.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_RECEIVER.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_LOGIC.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_READER.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_CONTROLLER.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TORCH_COUNTER.get(), RadioTorchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_AUTOCAL.get(), RadioAutocalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RADIO_TELEX.get(), RadioTelexRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RBMK_PANEL.get(), RBMKPanelRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_PIPE.get(), FluidPipeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_PIPE_ANCHOR.get(), FluidPipeAnchorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_BATTERY_SOCKET.get(), MachineBatterySocketRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_MACHINE.get(), AssemblyMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHEMICAL_PLANT.get(), ChemicalPlantRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_FACTORY.get(), AssemblyFactoryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHEMICAL_FACTORY.get(), ChemicalFactoryRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LIQUEFACTOR.get(), LiquefactorRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.REFINERY.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_TANK.get(), FluidTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BAT9000.get(), FluidTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BIG_ASS_TANK.get(), FluidTankRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.OIL_DRILL.get(), OilDrillRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_VISIBLE_MACHINE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GAS_FLARE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHIMNEY.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASHPIT.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CATALYTIC_CRACKER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CATALYTIC_REFORMER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VACUUM_DISTILL.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FRACTION_TOWER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.HYDROTREATER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COKER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLIDIFIER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PYRO_OVEN.get(), PyroOvenRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_RADAR.get(), RadarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_RADAR_LARGE.get(), RadarRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_RADAR_SCREEN.get(), RadarScreenRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.VENDING_MACHINE.get(), VendingMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SAT_DOCK.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOYUZ_CAPSULE.get(), SoyuzCapsuleBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOYUZ_LAUNCHER.get(), SoyuzLauncherRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COMPRESSOR.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ARC_WELDER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STEAM_ENGINE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOLAR_BOILER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SMALL_COOLING_TOWER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LARGE_COOLING_TOWER.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TRINKET.get(), TrinketBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_LIGHT.get(), LegacyLightBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_DEMON_LAMP.get(), LegacyDemonLampBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_LANTERN.get(), LegacyLanternBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NUCLEAR_DEVICE.get(), NuclearDeviceRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CUSTOM_NUKE.get(), CustomNukeRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_CHARGE.get(), LegacyChargeBlockEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MOVING_ITEM.get(), MovingItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MOVING_PACKAGE.get(), MovingPackageRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NUKE_EXPLOSION_MK5.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NUKE_EXPLOSION_MK3.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NUKE_TOREX.get(), NukeTorexRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BLACK_HOLE.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VORTEX.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RAGING_VORTEX.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.QUASAR.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FALLOUT_RAIN.get(), FalloutRainRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLOUD_FLEIJA.get(), CloudFleijaRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLOUD_SOLINIUM.get(), CloudSoliniumRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLOUD_FLEIJA_RAINBOW.get(), CloudFleijaRainbowRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.EMP_BLAST.get(), EmpBlastRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BALEFIRE_EXPLOSION.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.DEATH_BLAST.get(), DeathBlastRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.FALLING_NUKE.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.LEGACY_PRIMED_EXPLOSIVE.get(), LegacyPrimedExplosiveRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_GENERIC.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_DECOY.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_INCENDIARY.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_CLUSTER.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_BUSTER.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_STRONG.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_INCENDIARY_STRONG.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_CLUSTER_STRONG.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_BUSTER_STRONG.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MISSILE_BURST.get(), MissileRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MINER_ROCKET.get(), MinerRocketRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SOYUZ.get(), SoyuzRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SOYUZ_CAPSULE.get(), SoyuzCapsuleRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.SHRAPNEL.get(), ShrapnelRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.RUBBLE.get(), RubbleRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.BULLET_PROJECTILE.get(), BulletProjectileRenderer::new);
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
                .forEach(item -> event.register((stack, tintIndex) -> ((HbmFluidContainerItem) item).getTintColor(stack, tintIndex), item));
        ModItems.CONTROL_FLUID_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(HbmFluidContainerItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) -> ((HbmFluidContainerItem) item).getTintColor(stack, tintIndex), item));
        event.register(FluidIconItem::getTintColor, ModItems.FLUID_ICON.get());
        ModItems.CONTROL_FLUID_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(FluidIdentifierItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) -> ((FluidIdentifierItem) item).getTintColor(stack, tintIndex), item));
        ModItems.ORE_BYPRODUCT_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(OreByproductItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) -> ((OreByproductItem) item).getTintColor(tintIndex), item));
        event.register((stack, tintIndex) -> {
            if (stack.getItem() instanceof FluidPipeBlockItem pipe) {
                return pipe.getTintColor(stack, tintIndex);
            }
            return 0xFFFFFF;
        }, ModItems.FLUID_DUCT.get());
        event.register((stack, tintIndex) -> {
            if (tintIndex == 0 && stack.getItem() instanceof LegacyStateBlockItem item) {
                return sellafieldLevelTint(item.getVariant(stack));
            }
            return 0xFFFFFF;
        }, ModBlocks.SELLAFIELD_SLAKED.get().asItem(), ModBlocks.SELLAFIELD_BEDROCK.get().asItem(),
                ModBlocks.ORE_SELLAFIELD_DIAMOND.get().asItem(), ModBlocks.ORE_SELLAFIELD_EMERALD.get().asItem(),
                ModBlocks.ORE_SELLAFIELD_URANIUM_SCORCHED.get().asItem(), ModBlocks.ORE_SELLAFIELD_SCHRABIDIUM.get().asItem(),
                ModBlocks.ORE_SELLAFIELD_RADGEM.get().asItem());
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex == 1 && level != null && pos != null
                    && level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe) {
                return pipe.getFluidType().getColor();
            }
            return 0xFFFFFF;
        }, ModBlocks.FLUID_DUCT_NEO.get(),
                ModBlocks.FLUID_DUCT_BOX.get(),
                ModBlocks.FLUID_DUCT_GAUGE.get(),
                ModBlocks.FLUID_DUCT_PAINTABLE.get(),
                ModBlocks.PIPE_ANCHOR.get(),
                ModBlocks.FLUID_VALVE.get(),
                ModBlocks.FLUID_SWITCH.get(),
                ModBlocks.FLUID_COUNTER_VALVE.get());
        event.register((state, level, pos, tintIndex) -> {
            int crater = craterGrassColor(level, pos);
            return crater >= 0 ? crater : fallbackGrassColor(level, pos);
        }, Blocks.GRASS_BLOCK, Blocks.GRASS, Blocks.TALL_GRASS, Blocks.FERN, Blocks.LARGE_FERN, Blocks.VINE,
                Blocks.SUGAR_CANE);
        event.register((state, level, pos, tintIndex) -> {
            int crater = craterFoliageColor(level, pos);
            return crater >= 0 ? crater : fallbackFoliageColor(level, pos);
        }, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES,
                Blocks.DARK_OAK_LEAVES, Blocks.MANGROVE_LEAVES);
        event.register((state, level, pos, tintIndex) ->
                tintIndex == 0 && state.hasProperty(LegacySellafieldSlakedBlock.LEVEL)
                        ? sellafieldLevelTint(state.getValue(LegacySellafieldSlakedBlock.LEVEL))
                        : 0xFFFFFF,
                ModBlocks.SELLAFIELD_SLAKED.get(), ModBlocks.SELLAFIELD_BEDROCK.get(),
                ModBlocks.ORE_SELLAFIELD_DIAMOND.get(), ModBlocks.ORE_SELLAFIELD_EMERALD.get(),
                ModBlocks.ORE_SELLAFIELD_URANIUM_SCORCHED.get(), ModBlocks.ORE_SELLAFIELD_SCHRABIDIUM.get(),
                ModBlocks.ORE_SELLAFIELD_RADGEM.get());
    }

    private static int sellafieldLevelTint(int level) {
        int clamped = Math.max(0, Math.min(15, level));
        int shade = Math.max(0, Math.min(255, Math.round((1.0F - clamped / 15.0F) * 255.0F)));
        return shade << 16 | shade << 8 | shade;
    }

    private static int craterGrassColor(BlockAndTintGetter level, BlockPos pos) {
        CraterRadiationData.CraterZone zone = craterZone(level, pos);
        double noise = legacyPlantNoise(pos);
        return switch (zone) {
            case OUTER -> noise < -0.1D ? 0x776F59 : 0x6F6752;
            case CRATER -> noise < -0.1D ? 0x606060 : 0x505050;
            case INNER -> noise < -0.1D ? 0x404040 : 0x303030;
            case NONE -> -1;
        };
    }

    private static int craterFoliageColor(BlockAndTintGetter level, BlockPos pos) {
        return craterZone(level, pos) != CraterRadiationData.CraterZone.NONE ? 0x6A7039 : -1;
    }

    private static int fallbackGrassColor(BlockAndTintGetter level, BlockPos pos) {
        return level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.getDefaultColor();
    }

    private static int fallbackFoliageColor(BlockAndTintGetter level, BlockPos pos) {
        return level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : FoliageColor.getDefaultColor();
    }

    private static CraterRadiationData.CraterZone craterZone(BlockAndTintGetter level, BlockPos pos) {
        ResourceKey<Biome> key = biomeKey(level, pos);
        if (CraterBiomeUtil.CRATER_OUTER.equals(key)) {
            return CraterRadiationData.CraterZone.OUTER;
        }
        if (CraterBiomeUtil.CRATER.equals(key)) {
            return CraterRadiationData.CraterZone.CRATER;
        }
        if (CraterBiomeUtil.CRATER_INNER.equals(key)) {
            return CraterRadiationData.CraterZone.INNER;
        }
        return CraterRadiationData.CraterZone.NONE;
    }

    private static ResourceKey<Biome> biomeKey(BlockAndTintGetter level, BlockPos pos) {
        if (level == null || pos == null) {
            return null;
        }
        if (level instanceof LevelReader levelReader) {
            return levelReader.getBiome(pos).unwrapKey().orElse(null);
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            return minecraft.level.getBiome(pos).unwrapKey().orElse(null);
        }
        return null;
    }

    private static double legacyPlantNoise(BlockPos pos) {
        if (pos == null) {
            return 0.0D;
        }
        return CRATER_PLANT_NOISE.getValue(pos.getX() * 0.225D, pos.getZ() * 0.225D, false);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        HbmClientKeybinds.register(event);
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        HbmRenderEffects.registerShaders(event);
        HbmBlackHoleEffects.registerShaders(event);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.RADIATION_FOG.get(), RadiationFogParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ROCKET_FLAME.get(), RocketFlameParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.GAS_FLAME.get(), GasFlameParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.CONTRAIL.get(), LegacyContrailParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.SMOKE_PLUME.get(), SmokePlumeParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.LAUNCH_SMOKE.get(), HbmSmokeParticle.LaunchSmokeProvider::new);
        event.registerSpriteSet(ModParticleTypes.EX_SMOKE.get(), HbmSmokeParticle.ExSmokeProvider::new);
        event.registerSpriteSet(ModParticleTypes.DIGAMMA_SMOKE.get(), DigammaSmokeParticle.Provider::new);
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
        event.registerSpriteSet(ModParticleTypes.LARGE_EXPLODE.get(), LargeExplodeParticle.Provider::new);
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
