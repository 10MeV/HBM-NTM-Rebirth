package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HbmNtm.MOD_ID);

    public static final RegistryObject<CreativeModeTab> PARTS = CREATIVE_TABS.register("parts",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.parts"))
                    .icon(() -> ModItems.URANIUM_INGOT.get().getDefaultInstance())
                    .displayItems((parameters, output) -> ModItems.PARTS_TAB_ITEMS.forEach(item -> output.accept(item.get())))
                    .build());

    public static final RegistryObject<CreativeModeTab> MACHINES = CREATIVE_TABS.register("machines",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.machines"))
                    .icon(() -> ModBlocks.MACHINE_PRESS.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> ModBlocks.MACHINE_TAB_BLOCKS.forEach(block -> output.accept(block.get())))
                    .build());

    public static final RegistryObject<CreativeModeTab> CONSUMABLES = CREATIVE_TABS.register("consumables",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.consumables"))
                    .icon(() -> ModItems.GEIGER_COUNTER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> ModItems.CONSUMABLE_TAB_ITEMS.forEach(item -> output.accept(item.get())))
                    .build());

    public static final RegistryObject<CreativeModeTab> BLOCKS = CREATIVE_TABS.register("blocks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.blocks"))
                    .icon(() -> ModBlocks.WASTE_EARTH.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> ModBlocks.BLOCK_TAB_BLOCKS.forEach(block -> output.accept(block.get())))
                    .build());

    public static final RegistryObject<CreativeModeTab> NUKES = CREATIVE_TABS.register("nukes",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm.nukes"))
                    .icon(() -> ModBlocks.NUKE_GADGET.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> ModBlocks.NUKE_TAB_BLOCKS.forEach(block -> output.accept(block.get())))
                    .build());

    public static void register(IEventBus modBus) {
        CREATIVE_TABS.register(modBus);
    }

    private ModCreativeTabs() {
    }
}
