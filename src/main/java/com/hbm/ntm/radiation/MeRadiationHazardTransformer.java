package com.hbm.ntm.radiation;

import com.hbm.ntm.compat.Compat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class MeRadiationHazardTransformer implements HazardTransformer {
    @Override
    public void transformPost(ItemStack stack, List<HazardEntry> entries) {
        if (!isMeStorageItem(stack.getItem())) {
            return;
        }

        float radiation = readLegacyMeRadiation(stack);
        if (radiation > 0.0F) {
            entries.add(new HazardEntry(HazardType.RADIATION, radiation));
        }
    }

    private static boolean isMeStorageItem(Item item) {
        String name = item.getClass().getName();
        return name.equals("appeng.items.storage.ItemBasicStorageCell")
                || name.equals("appeng.items.tools.powered.ToolPortableCell");
    }

    private static float readLegacyMeRadiation(ItemStack stack) {
        float radiation = 0.0F;
        for (ItemStack held : Compat.scrapeItemFromME(stack)) {
            radiation += HazardRegistry.getStackRadiation(held);
        }
        return radiation;
    }
}
