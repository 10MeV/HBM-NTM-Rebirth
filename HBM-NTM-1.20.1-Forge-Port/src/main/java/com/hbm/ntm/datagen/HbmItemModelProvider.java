package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class HbmItemModelProvider extends ItemModelProvider {
    public HbmItemModelProvider(PackOutput output, String modId, ExistingFileHelper existingFileHelper) {
        super(output, modId, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ModItems.PARTS_TAB_ITEMS.forEach(item -> itemModel(item.get()));
        ModItems.CONTROL_TAB_ITEMS.forEach(item -> itemModel(item.get()));
        ModItems.NUKE_TAB_ITEMS.forEach(item -> itemModel(item.get()));
        ModItems.CONSUMABLE_TAB_ITEMS.forEach(item -> itemModel(item.get()));
        itemModel(ModItems.CONVEYOR_WAND.get());
    }

    private void itemModel(Item item) {
        String path = ForgeRegistries.ITEMS.getKey(item).getPath();
        if (item instanceof HbmBatteryPackItem) {
            getBuilder(path)
                    .parent(new ModelFile.UncheckedModelFile(new ResourceLocation("builtin/entity")));
            return;
        }
        if (item == ModItems.BATTERY_CREATIVE.get()) {
            getBuilder(path)
                    .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                    .texture("layer0", modLoc("item/battery_creative_new"));
            return;
        }
        if (item instanceof HbmSelfChargingBatteryItem battery) {
            getBuilder(path)
                    .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                    .texture("layer0", modLoc("item/" + battery.getLegacyTexturePath()));
            return;
        }
        if (path.startsWith("wire_dense_")) {
            generatedItem(path, "wire_dense");
            return;
        }
        if (path.startsWith("plate_cast_")) {
            generatedItem(path, "plate_cast");
            return;
        }
        if (path.equals("pellet_charged")) {
            generatedItem(path, "pellets_charged");
            return;
        }
        if (path.equals("circuit_chip_quantum")) {
            generatedItem(path, "circuit.chip_quantum");
            return;
        }
        basicItem(item);
    }

    private void generatedItem(String itemPath, String texturePath) {
        getBuilder(itemPath)
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", modLoc("item/" + texturePath));
    }
}
