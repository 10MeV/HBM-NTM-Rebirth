package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyModelReloadListener;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.particle.FlamethrowerParticle;
import com.hbm.ntm.client.particle.FoamParticle;
import com.hbm.ntm.client.particle.HbmSmokeParticle;
import com.hbm.ntm.client.particle.RadiationFogParticle;
import com.hbm.ntm.client.particle.RocketFlameParticle;
import com.hbm.ntm.client.renderer.AssemblyMachineRenderer;
import com.hbm.ntm.client.renderer.BasicMachineRenderer;
import com.hbm.ntm.client.renderer.LegacyDemonLampBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyLanternBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyLightBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyVisibleMachineRenderer;
import com.hbm.ntm.client.renderer.MachineBatterySocketRenderer;
import com.hbm.ntm.client.renderer.MovingPackageRenderer;
import com.hbm.ntm.client.renderer.MovingItemRenderer;
import com.hbm.ntm.client.renderer.TrinketBlockEntityRenderer;
import com.hbm.ntm.client.screen.BasicMachineScreen;
import com.hbm.ntm.client.screen.MachineBatteryScreen;
import com.hbm.ntm.client.screen.MachineBatterySocketScreen;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.registry.ModBlockEntities;
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
            MenuScreens.register(ModMenuTypes.MACHINE_BATTERY.get(), MachineBatteryScreen::new);
            MenuScreens.register(ModMenuTypes.MACHINE_BATTERY_SOCKET.get(), MachineBatterySocketScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BASIC_MACHINE.get(), BasicMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MACHINE_BATTERY_SOCKET.get(), MachineBatterySocketRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ASSEMBLY_MACHINE.get(), AssemblyMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_VISIBLE_MACHINE.get(), LegacyVisibleMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TRINKET.get(), TrinketBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_LIGHT.get(), LegacyLightBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_DEMON_LAMP.get(), LegacyDemonLampBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_LANTERN.get(), LegacyLanternBlockEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MOVING_ITEM.get(), MovingItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MOVING_PACKAGE.get(), MovingPackageRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.NUKE_EXPLOSION_MK5.get(), NoopRenderer::new);
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
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        HbmClientKeybinds.register(event);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.RADIATION_FOG.get(), RadiationFogParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.ROCKET_FLAME.get(), RocketFlameParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.CONTRAIL.get(), HbmSmokeParticle.ContrailProvider::new);
        event.registerSpriteSet(ModParticleTypes.LAUNCH_SMOKE.get(), HbmSmokeParticle.LaunchSmokeProvider::new);
        event.registerSpriteSet(ModParticleTypes.EX_SMOKE.get(), HbmSmokeParticle.ExSmokeProvider::new);
        event.registerSpriteSet(ModParticleTypes.FOAM.get(), FoamParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.FLAMETHROWER.get(), FlamethrowerParticle.Provider::new);
    }

    private ClientModEvents() {
    }
}
