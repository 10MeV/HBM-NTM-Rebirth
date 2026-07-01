package com.hbm.ntm.energy;

import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class HbmLegacyBatteryMaps {
    public static Optional<RegistryObject<Item>> batteryPackByLegacyMeta(int legacyMeta) {
        return LegacyMetaItemMappings.item(LegacyMetaItemMappings.BATTERY_PACK, legacyMeta);
    }

    public static Optional<RegistryObject<Item>> selfChargingByLegacyMeta(int legacyMeta) {
        return LegacyMetaItemMappings.item(LegacyMetaItemMappings.BATTERY_SC, legacyMeta);
    }

    public static List<RegistryObject<Item>> batteryPacksByLegacyMeta() {
        return LegacyMetaItemMappings.variants(LegacyMetaItemMappings.BATTERY_PACK);
    }

    public static List<RegistryObject<Item>> selfChargingByLegacyMeta() {
        return LegacyMetaItemMappings.variants(LegacyMetaItemMappings.BATTERY_SC);
    }

    public static List<ItemStack> legacyMachineRecipeBatteryDisplayStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        addStack(stacks, ModItems.BATTERY_POTATO);
        addStack(stacks, ModItems.BATTERY_POTATOS);
        addStack(stacks, ModItems.ENERGY_CORE);
        addStacks(stacks, batteryPacksByLegacyMeta());
        addStacks(stacks, selfChargingByLegacyMeta());
        addStack(stacks, ModItems.BATTERY_CREATIVE);
        return List.copyOf(stacks);
    }

    private static void addStacks(List<ItemStack> stacks, List<RegistryObject<Item>> items) {
        for (RegistryObject<Item> item : items) {
            addStack(stacks, item);
        }
    }

    private static void addStack(List<ItemStack> stacks, RegistryObject<Item> item) {
        stacks.add(new ItemStack(item.get()));
    }

    private HbmLegacyBatteryMaps() {
    }
}
