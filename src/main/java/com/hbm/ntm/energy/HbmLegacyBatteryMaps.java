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
    private static final List<RegistryObject<Item>> BATTERY_PACK_BY_META = List.of(
            ModItems.BATTERY_REDSTONE,
            ModItems.BATTERY_LEAD,
            ModItems.BATTERY_LITHIUM,
            ModItems.BATTERY_SODIUM,
            ModItems.BATTERY_SCHRABIDIUM,
            ModItems.BATTERY_QUANTUM,
            ModItems.CAPACITOR_COPPER,
            ModItems.CAPACITOR_GOLD,
            ModItems.CAPACITOR_NIOBIUM,
            ModItems.CAPACITOR_TANTALUM,
            ModItems.CAPACITOR_BISMUTH,
            ModItems.CAPACITOR_SPARK
    );

    private static final List<RegistryObject<Item>> SELF_CHARGING_BY_META = List.of(
            ModItems.BATTERY_SC_EMPTY,
            ModItems.BATTERY_SC_WASTE,
            ModItems.BATTERY_SC_RA226,
            ModItems.BATTERY_SC_TC99,
            ModItems.BATTERY_SC_CO60,
            ModItems.BATTERY_SC_PU238,
            ModItems.BATTERY_SC_PO210,
            ModItems.BATTERY_SC_AU198,
            ModItems.BATTERY_SC_PB209,
            ModItems.BATTERY_SC_AM241
    );

    public static Optional<RegistryObject<Item>> batteryPackByLegacyMeta(int legacyMeta) {
        return LegacyMetaItemMappings.item(LegacyMetaItemMappings.BATTERY_PACK, legacyMeta);
    }

    public static Optional<RegistryObject<Item>> selfChargingByLegacyMeta(int legacyMeta) {
        return LegacyMetaItemMappings.item(LegacyMetaItemMappings.BATTERY_SC, legacyMeta);
    }

    public static List<RegistryObject<Item>> batteryPacksByLegacyMeta() {
        return BATTERY_PACK_BY_META;
    }

    public static List<RegistryObject<Item>> selfChargingByLegacyMeta() {
        return SELF_CHARGING_BY_META;
    }

    public static List<ItemStack> legacyMachineRecipeBatteryDisplayStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        addStack(stacks, ModItems.BATTERY_POTATO);
        addStack(stacks, ModItems.BATTERY_POTATOS);
        addStack(stacks, ModItems.ENERGY_CORE);
        addStacks(stacks, BATTERY_PACK_BY_META);
        addStacks(stacks, SELF_CHARGING_BY_META);
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
