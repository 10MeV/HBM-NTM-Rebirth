package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.HbmInfiniteFluidItem;
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
        ModItems.CONTROL_FLUID_ITEMS.forEach(item -> itemModel(item.get()));
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
        if (item instanceof com.hbm.ntm.item.FluidIdentifierItem) {
            getBuilder(path)
                    .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                    .texture("layer0", modLoc("item/fluid_identifier_multi"))
                    .texture("layer1", modLoc("item/fluid_identifier_overlay"));
            return;
        }
        if (item instanceof HbmFluidContainerItem container) {
            fluidContainerItem(path, container);
            return;
        }
        if (path.equals("fluid_tank_empty")) {
            generatedItem(path, "fluid_tank");
            return;
        }
        if (path.equals("fluid_tank_lead_empty")) {
            generatedItem(path, "fluid_tank_lead");
            return;
        }
        if (path.equals("fluid_barrel_empty")) {
            generatedItem(path, "fluid_barrel");
            return;
        }
        if (path.equals("fluid_pack_empty")) {
            generatedItem(path, "fluid_pack");
            return;
        }
        if (path.equals("disperser_canister_empty")) {
            generatedItem(path, "disperser_canister");
            return;
        }
        if (path.equals("glyphid_gland_empty")) {
            generatedItem(path, "glyphid_gland");
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
        if (path.equals("early_explosive_lenses")) {
            generatedItem(path, "gadget_explosive8");
            return;
        }
        if (path.equals("explosive_lenses")) {
            generatedItem(path, "man_explosive8");
            return;
        }
        if (path.equals("igniter")) {
            generatedItem(path, "trigger");
            return;
        }
        if (path.equals("fluid_duct_neo")) {
            getBuilder(path)
                    .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                    .texture("layer0", modLoc("item/duct"))
                    .texture("layer1", modLoc("item/duct_overlay"));
            return;
        }
        basicItem(item);
    }

    private void fluidContainerItem(String path, HbmFluidContainerItem container) {
        if (container instanceof HbmInfiniteFluidItem) {
            generatedItem(path, path);
            return;
        }
        switch (container.getContainerKind()) {
            case CANISTER -> layeredItem(path, "canister_empty", "canister_overlay");
            case GAS_TANK -> layeredItem(path, "gas_empty", "gas_bottle", "gas_label");
            case FLUID_TANK -> layeredItem(path, "fluid_tank", "fluid_tank_overlay");
            case LEAD_FLUID_TANK -> layeredItem(path, "fluid_tank_lead", "fluid_tank_lead_overlay");
            case FLUID_BARREL -> layeredItem(path, "fluid_barrel", "fluid_barrel_overlay");
            case FLUID_PACK -> layeredItem(path, "fluid_pack", "fluid_pack_overlay");
            case DISPERSER_CANISTER -> layeredItem(path, "disperser_canister", "disperser_canister_overlay");
            case GLYPHID_GLAND -> layeredItem(path, "glyphid_gland", "fluid_identifier_overlay");
        }
    }

    private void layeredItem(String itemPath, String layer0, String layer1) {
        getBuilder(itemPath)
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", modLoc("item/" + layer0))
                .texture("layer1", modLoc("item/" + layer1));
    }

    private void layeredItem(String itemPath, String layer0, String layer1, String layer2) {
        getBuilder(itemPath)
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", modLoc("item/" + layer0))
                .texture("layer1", modLoc("item/" + layer1))
                .texture("layer2", modLoc("item/" + layer2));
    }

    private void generatedItem(String itemPath, String texturePath) {
        getBuilder(itemPath)
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                .texture("layer0", modLoc("item/" + texturePath));
    }
}
