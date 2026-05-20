package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class HbmItemModelProvider extends ItemModelProvider {
    public HbmItemModelProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, modId, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ModItems.PARTS_TAB_ITEMS.forEach(item -> basicItem(item.get()));
        ModItems.CONTROL_TAB_ITEMS.forEach(item -> basicItem(item.get()));
        ModItems.NUKE_TAB_ITEMS.forEach(item -> basicItem(item.get()));
        ModItems.CONSUMABLE_TAB_ITEMS.forEach(item -> basicItem(item.get()));
    }
}
