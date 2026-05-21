package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.renderer.BasicMachineRenderer;
import com.hbm.ntm.client.renderer.LegacyDemonLampBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyLanternBlockEntityRenderer;
import com.hbm.ntm.client.renderer.LegacyLightBlockEntityRenderer;
import com.hbm.ntm.client.renderer.TrinketBlockEntityRenderer;
import com.hbm.ntm.client.screen.BasicMachineScreen;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModMenuTypes.BASIC_MACHINE.get(), BasicMachineScreen::new));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.BASIC_MACHINE.get(), BasicMachineRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TRINKET.get(), TrinketBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_LIGHT.get(), LegacyLightBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_DEMON_LAMP.get(), LegacyDemonLampBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LEGACY_LANTERN.get(), LegacyLanternBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        TrinketBlockEntityRenderer.registerAdditionalModels();
        ObjModelLibrary.registerAdditionalModels(event);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ModItems.PARTS_TAB_ITEMS.stream()
                .map(RegistryObject::get)
                .filter(DepletedFuelItem.class::isInstance)
                .forEach(item -> event.register((stack, tintIndex) ->
                        DepletedFuelItem.isHot(stack) ? DepletedFuelItem.HOT_TINT : 0xFFFFFF, item));
    }

    private ClientModEvents() {
    }
}
