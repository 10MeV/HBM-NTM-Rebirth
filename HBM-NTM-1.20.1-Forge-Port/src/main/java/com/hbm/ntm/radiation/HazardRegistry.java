package com.hbm.ntm.radiation;

import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class HazardRegistry {
    private static final Map<Item, List<HazardEntry>> ITEM_HAZARDS = new IdentityHashMap<>();

    public static void registerDefaults() {
        register(ModItems.URANIUM_INGOT.get(), HazardType.RADIATION, RadiationConstants.U * RadiationConstants.INGOT);
        register(ModItems.URANIUM_233_INGOT.get(), HazardType.RADIATION, RadiationConstants.U233 * RadiationConstants.INGOT);
        register(ModItems.URANIUM_235_INGOT.get(), HazardType.RADIATION, RadiationConstants.U235 * RadiationConstants.INGOT);
        register(ModItems.URANIUM_238_INGOT.get(), HazardType.RADIATION, RadiationConstants.U238 * RadiationConstants.INGOT);
        register(ModItems.THORIUM_232_INGOT.get(), HazardType.RADIATION, RadiationConstants.TH232 * RadiationConstants.INGOT);
        register(ModItems.PLUTONIUM_INGOT.get(), HazardType.RADIATION, RadiationConstants.PU * RadiationConstants.INGOT);
        register(ModItems.PLUTONIUM_238_INGOT.get(), HazardType.RADIATION, RadiationConstants.PU238 * RadiationConstants.INGOT);
        register(ModItems.PLUTONIUM_239_INGOT.get(), HazardType.RADIATION, RadiationConstants.PU239 * RadiationConstants.INGOT);
        register(ModItems.PLUTONIUM_240_INGOT.get(), HazardType.RADIATION, RadiationConstants.PU240 * RadiationConstants.INGOT);
        register(ModItems.PLUTONIUM_241_INGOT.get(), HazardType.RADIATION, RadiationConstants.PU241 * RadiationConstants.INGOT);
        register(ModItems.NEPTUNIUM_INGOT.get(), HazardType.RADIATION, RadiationConstants.NP237 * RadiationConstants.INGOT);
        register(ModItems.POLONIUM_INGOT.get(), HazardType.RADIATION, RadiationConstants.PO210 * RadiationConstants.INGOT);
        register(ModItems.SCHRABIDIUM_INGOT.get(), HazardType.RADIATION, RadiationConstants.SA326 * RadiationConstants.INGOT);

        register(ModItems.URANIUM_POWDER.get(), HazardType.RADIATION, RadiationConstants.U * RadiationConstants.POWDER_MULTIPLIER);
        register(ModItems.PLUTONIUM_POWDER.get(), HazardType.RADIATION, RadiationConstants.PU * RadiationConstants.POWDER_MULTIPLIER);
        register(ModItems.THORIUM_POWDER.get(), HazardType.RADIATION, RadiationConstants.TH232 * RadiationConstants.POWDER_MULTIPLIER);

        register(ModBlocks.NUKE_GADGET.get().asItem(), HazardType.RADIATION, RadiationConstants.PU239 * 10.0F);
        register(ModBlocks.NUKE_BOY.get().asItem(), HazardType.RADIATION, RadiationConstants.U235 * 3.0F);
        register(ModBlocks.NUKE_MAN.get().asItem(), HazardType.RADIATION, RadiationConstants.PU239 * 10.0F);
        register(ModBlocks.NUKE_MIKE.get().asItem(), HazardType.RADIATION, RadiationConstants.U238 * 10.0F);
        register(ModBlocks.NUKE_TSAR.get().asItem(), HazardType.RADIATION, RadiationConstants.PU239 * 15.0F);
        register(ModBlocks.NUKE_FLEIJA.get().asItem(), HazardType.RADIATION, RadiationConstants.SA326);
        register(ModBlocks.NUKE_SOLINIUM.get().asItem(), HazardType.RADIATION, RadiationConstants.SA326 * 8.0F);
        register(ModBlocks.NUKE_FSTBMB.get().asItem(), HazardType.DIGAMMA, 0.01F);
    }

    public static void register(Item item, HazardType type, float level) {
        if (level <= 0.0F) {
            return;
        }
        ITEM_HAZARDS.computeIfAbsent(item, key -> new ArrayList<>()).add(new HazardEntry(type, level));
    }

    public static List<HazardEntry> getHazards(ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of();
        }
        return ITEM_HAZARDS.getOrDefault(stack.getItem(), List.of());
    }

    public static float getHazardLevel(ItemStack stack, HazardType type) {
        float level = 0.0F;
        for (HazardEntry entry : getHazards(stack)) {
            if (entry.type() == type) {
                level += entry.level();
            }
        }
        return level;
    }

    public static float getStackHazardLevel(ItemStack stack, HazardType type) {
        return getHazardLevel(stack, type) * stack.getCount();
    }

    public static float getStackRadiation(ItemStack stack) {
        return getStackHazardLevel(stack, HazardType.RADIATION);
    }

    private HazardRegistry() {
    }
}
