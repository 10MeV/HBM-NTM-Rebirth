package com.hbm.ntm.registry;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.energy.HbmBatteryItem;
import com.hbm.ntm.item.ConveyorWandItem;
import com.hbm.ntm.item.DepletedFuelItem;
import com.hbm.ntm.item.FluidIdentifierItem;
import com.hbm.ntm.item.FluidPipeBlockItem;
import com.hbm.ntm.item.FluidDuctVariantBlockItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.HbmInfiniteFluidItem;
import com.hbm.ntm.item.FoundryMoldItem;
import com.hbm.ntm.item.FoundryScrapsItem;
import com.hbm.ntm.item.GuideBookItem;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.LegacyStateMultiblockBlockItem;
import com.hbm.ntm.item.MarshmallowItem;
import com.hbm.ntm.item.RBMKPelletItem;
import com.hbm.ntm.item.SirenCassetteItem;
import com.hbm.ntm.item.TrinketBlockItem;
import com.hbm.ntm.satellite.SoyuzRocketItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, HbmNtm.MOD_ID);

    public static final RegistryObject<CreativeModeTab> PARTS = CREATIVE_TABS.register("parts",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm_ntm_rebirth.parts"))
                    .icon(() -> ModItems.URANIUM_INGOT.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        CreativeModeTab.Output dedupedOutput = deduplicating(output);
                        ModItems.PARTS_TAB_ITEMS.forEach(item -> {
                            if (item.get() instanceof DepletedFuelItem depletedFuel) {
                                DepletedFuelItem.addCreativeStacks(dedupedOutput, depletedFuel);
                            } else {
                                dedupedOutput.accept(item.get());
                            }
                        });
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> MACHINES = CREATIVE_TABS.register("machines",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm_ntm_rebirth.machines"))
                    .icon(() -> ModBlocks.MACHINE_PRESS.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        CreativeModeTab.Output dedupedOutput = deduplicating(output);
                        ModBlocks.MACHINE_TAB_BLOCKS.forEach(block -> acceptBlockItem(dedupedOutput, block.get().asItem()));
                        ModBlocks.MACHINE_TAB_EXTRA_BLOCKS.forEach(block -> acceptBlockItem(dedupedOutput, block.get().asItem()));
                        if (ModItems.CONVEYOR_WAND.get() instanceof ConveyorWandItem conveyorWand) {
                            ConveyorWandItem.addCreativeStacks(dedupedOutput, conveyorWand);
                        }
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> CONSUMABLES = CREATIVE_TABS.register("consumables",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm_ntm_rebirth.consumables"))
                    .icon(() -> ModItems.GEIGER_COUNTER.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        CreativeModeTab.Output dedupedOutput = deduplicating(output);
                        ModItems.CONSUMABLE_TAB_ITEMS.forEach(item -> {
                            if (item.get() instanceof HbmBatteryItem battery) {
                                battery.addCreativeStacks(dedupedOutput, item.get().getDefaultInstance());
                            } else if (item.get() instanceof MarshmallowItem) {
                                MarshmallowItem.addCreativeStacks(dedupedOutput, item.get());
                            } else if (item.get() instanceof GuideBookItem) {
                                GuideBookItem.addCreativeStacks(dedupedOutput, item.get());
                            } else {
                                dedupedOutput.accept(item.get());
                            }
                        });
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> CONTROL = CREATIVE_TABS.register("control",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm_ntm_rebirth.control"))
                    .icon(() -> ModItems.legacyItem("plate_fuel_u235").get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        CreativeModeTab.Output dedupedOutput = deduplicating(output);
                        ModItems.CONTROL_TAB_ITEMS.forEach(item -> acceptItem(dedupedOutput, item));
                        ModItems.CONTROL_FLUID_ITEMS.forEach(item -> acceptItem(dedupedOutput, item));
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> BLOCKS = CREATIVE_TABS.register("blocks",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm_ntm_rebirth.blocks"))
                    .icon(() -> ModBlocks.WASTE_EARTH.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        CreativeModeTab.Output dedupedOutput = deduplicating(output);
                        ModBlocks.BLOCK_TAB_BLOCKS.forEach(block -> acceptBlockItem(dedupedOutput, block.get().asItem()));
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> NUKES = CREATIVE_TABS.register("nukes",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm_ntm_rebirth.nukes"))
                    .icon(() -> ModBlocks.NUKE_GADGET.get().asItem().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        CreativeModeTab.Output dedupedOutput = deduplicating(output);
                        ModBlocks.NUKE_TAB_BLOCKS.forEach(block -> acceptBlockItem(dedupedOutput, block.get().asItem()));
                        ModItems.NUKE_TAB_ITEMS.forEach(item -> dedupedOutput.accept(item.get()));
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> WEAPONS = CREATIVE_TABS.register("weapons",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm_ntm_rebirth.weapons"))
                    .icon(() -> ModItems.AMMO_STANDARD_G12.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        CreativeModeTab.Output dedupedOutput = deduplicating(output);
                        ModItems.WEAPON_TAB_ITEMS.forEach(item -> acceptItem(dedupedOutput, item));
                        ModBlocks.TURRET_TAB_BLOCKS.forEach(block -> acceptBlockItem(dedupedOutput, block.get().asItem()));
                    })
                    .build());

    public static final RegistryObject<CreativeModeTab> MISSILES = CREATIVE_TABS.register("missiles",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.hbm_ntm_rebirth.missiles"))
                    .icon(() -> ModItems.MISSILE_GENERIC.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        CreativeModeTab.Output dedupedOutput = deduplicating(output);
                        ModItems.MISSILE_TAB_ITEMS.forEach(item -> acceptItem(dedupedOutput, item));
                        ModItems.SATELLITE_TAB_ITEMS.forEach(item -> acceptItem(dedupedOutput, item));
                        ModBlocks.SATELLITE_TAB_BLOCKS.forEach(block -> acceptBlockItem(dedupedOutput, block.get().asItem()));
                    })
                    .build());

    public static void register(IEventBus modBus) {
        CREATIVE_TABS.register(modBus);
    }

    private static void acceptBlockItem(CreativeModeTab.Output output, Item item) {
        if (item == Items.AIR) {
            return;
        }
        if (item instanceof TrinketBlockItem trinket) {
            TrinketBlockItem.addCreativeStacks(output, trinket);
        } else if (item instanceof LegacyStateMultiblockBlockItem stateItem) {
            stateItem.addCreativeStacks(output);
        } else if (item instanceof LegacyStateBlockItem stateItem) {
            stateItem.addCreativeStacks(output);
        } else if (item instanceof FluidDuctVariantBlockItem duct) {
            duct.addCreativeStacks(output);
        } else {
            acceptSingleStack(output, item);
        }
    }

    private static void acceptItem(CreativeModeTab.Output output, RegistryObject<? extends Item> item) {
        if (item.get() instanceof HbmBatteryItem battery) {
            battery.addCreativeStacks(output, item.get().getDefaultInstance());
        } else if (item.get() instanceof FluidIdentifierItem identifier) {
            identifier.addCreativeStacks(output);
        } else if (item.get() instanceof FluidPipeBlockItem pipe) {
            pipe.addCreativeStacks(output);
        } else if (item.get() instanceof HbmInfiniteFluidItem) {
            output.accept(item.get().getDefaultInstance());
            } else if (item.get() instanceof HbmFluidContainerItem container) {
                container.addCreativeStacks(output);
        } else if (item.get() instanceof FoundryMoldItem mold) {
            FoundryMoldItem.addCreativeStacks(output, mold);
        } else if (item.get() instanceof FoundryScrapsItem scraps) {
            FoundryScrapsItem.addCreativeStacks(output, scraps);
        } else if (item.get() instanceof RBMKPelletItem pellet) {
            RBMKPelletItem.addCreativeStacks(output, pellet);
        } else if (item.get() instanceof SirenCassetteItem cassette) {
            SirenCassetteItem.addCreativeStacks(output, cassette);
            } else if (item.get() instanceof SoyuzRocketItem soyuz) {
                SoyuzRocketItem.addCreativeStacks(output, soyuz);
            } else {
            acceptSingleStack(output, item.get());
        }
    }

    private static void acceptSingleStack(CreativeModeTab.Output output, Item item) {
        ItemStack stack = item.getDefaultInstance();
        if (stack.isEmpty()) {
            return;
        }
        stack.setCount(1);
        output.accept(stack);
    }

    private static CreativeModeTab.Output deduplicating(CreativeModeTab.Output output) {
        List<ItemStack> accepted = new ArrayList<>();
        return new CreativeModeTab.Output() {
            @Override
            public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
                if (stack.isEmpty()) {
                    return;
                }
                ItemStack normalized = stack.copy();
                normalized.setCount(1);
                for (ItemStack previous : accepted) {
                    if (ItemStack.matches(previous, normalized)) {
                        return;
                    }
                }
                accepted.add(normalized.copy());
                output.accept(stack, visibility);
            }
        };
    }

    private ModCreativeTabs() {
    }
}
