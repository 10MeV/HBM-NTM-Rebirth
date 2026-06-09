package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.energy.HbmBatteryPackItem;
import com.hbm.ntm.energy.HbmSelfChargingBatteryItem;
import com.hbm.ntm.item.HbmAbilitySwordItem;
import com.hbm.ntm.item.HbmAbilityToolItem;
import com.hbm.ntm.item.HbmFluidContainerItem;
import com.hbm.ntm.item.HbmInfiniteFluidItem;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

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
        ModItems.SATELLITE_TAB_ITEMS.forEach(item -> itemModel(item.get()));
        ModItems.CONSUMABLE_TAB_ITEMS.forEach(item -> itemModel(item.get()));
        ModItems.HIDDEN_RECIPE_ITEMS.forEach(item -> itemModel(item.get()));
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
        if (path.equals("blueprints")) {
            generatedItem(path, "blueprints");
            return;
        }
        if (path.equals("pollution_detector")) {
            generatedItem(path, "pollution_detector");
            return;
        }
        if (path.equals("missile_soyuz")) {
            generatedItem(path, "soyuz_0");
            return;
        }
        if (path.equals("missile_soyuz_lander")) {
            generatedItem(path, "soyuz_lander");
            return;
        }
        if (path.equals("designator_range")) {
            generatedItem(path, "designator_range_alt");
            return;
        }
        if (path.equals("fluid_icon")) {
            layeredItem(path, "fluid_icon", "fluid_identifier_overlay");
            return;
        }
        if (path.equals("niter")) {
            generatedItem(path, "salpeter");
            return;
        }
        if (path.equals("plate_cast_ferrouranium")) {
            generatedItem(path, "ferrouranium_plate");
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
        if (path.startsWith("arc_electrode_burnt_")) {
            generatedItem(path, "arc_electrode_burnt." + path.substring("arc_electrode_burnt_".length()));
            return;
        }
        if (path.startsWith("arc_electrode_")) {
            generatedItem(path, "arc_electrode." + path.substring("arc_electrode_".length()));
            return;
        }
        if (path.startsWith("pa_coil_")) {
            generatedItem(path, "pa_coil." + path.substring("pa_coil_".length()));
            return;
        }
        if (path.startsWith("bolt_")) {
            generatedItem(path, "bolt");
            return;
        }
        if (path.startsWith("shell_")) {
            generatedItem(path, "shell");
            return;
        }
        if (path.startsWith("pipes_") && !path.equals("pipes_steel")) {
            generatedItem(path, "pipe");
            return;
        }
        if (path.equals("wire_fine_mingrade")) {
            generatedItem(path, "wire_red_copper");
            return;
        }
        if (path.equals("wire_fine_copper")) {
            generatedItem(path, "wire_copper");
            return;
        }
        if (path.equals("wire_fine_tungsten")) {
            generatedItem(path, "wire_tungsten");
            return;
        }
        if (path.startsWith("plate_cast_")) {
            generatedItem(path, "plate_cast");
            return;
        }
        if (path.startsWith("plate_welded_")) {
            generatedItem(path, "plate_welded");
            return;
        }
        if (path.equals("pellet_charged")) {
            generatedItem(path, "pellets_charged");
            return;
        }
        if (path.equals("magnetron")) {
            generatedItem(path, "magnetron_alt");
            return;
        }
        if (path.equals("rod_zirnox_natural_uranium_fuel_depleted")) {
            generatedItem(path, "rod_zirnox_uranium_fuel_depleted");
            return;
        }
        if (path.equals("ingot_weaponsteel")) {
            generatedItem(path, "ingot_gunsteel");
            return;
        }
        if (path.equals("plate_weaponsteel")) {
            generatedItem(path, "plate_gunsteel");
            return;
        }
        if (path.startsWith("coke_")) {
            generatedItem(path, "coke." + path.substring("coke_".length()));
            return;
        }
        if (path.startsWith("briquette_")) {
            generatedItem(path, "briquette." + path.substring("briquette_".length()));
            return;
        }
        if (path.startsWith("oil_tar_")) {
            generatedItem(path, "oil_tar." + path.substring("oil_tar_".length()));
            return;
        }
        if (path.startsWith("powder_ash_")) {
            generatedItem(path, "powder_ash." + path.substring("powder_ash_".length()));
            return;
        }
        if (path.startsWith("chunk_ore_")) {
            generatedItem(path, "chunk_ore." + path.substring("chunk_ore_".length()));
            return;
        }
        if (path.startsWith("plant_item_")) {
            generatedItem(path, "plant_item." + path.substring("plant_item_".length()));
            return;
        }
        if (path.startsWith("parts_legendary_")) {
            generatedItem(path, "parts_legendary." + path.substring("parts_legendary_".length()));
            return;
        }
        if (path.startsWith("part_generic_")) {
            generatedItem(path, partGenericTexture(path));
            return;
        }
        if (path.startsWith("item_expensive_")) {
            generatedItem(path, "item_expensive." + path.substring("item_expensive_".length()));
            return;
        }
        if (path.startsWith("ore_byproduct_")) {
            generatedItem(path, "byproduct");
            return;
        }
        if (path.startsWith("stamp_book_")) {
            generatedItem(path, "stamp_book");
            return;
        }
        if (path.startsWith("page_of_")) {
            generatedItem(path, "page_of_");
            return;
        }
        if (path.startsWith("casing_")) {
            generatedItem(path, "casing." + path.substring("casing_".length()));
            return;
        }
        if (path.startsWith("fuel_additive_")) {
            generatedItem(path, "fuel_additive." + path.substring("fuel_additive_".length()));
            return;
        }
        if (path.equals("singularity_counter_resonant")) {
            generatedItem(path, "singularity_alt");
            return;
        }
        if (path.equals("singularity_super_heated")) {
            generatedItem(path, "singularity_5");
            return;
        }
        if (path.equals("singularity_spark")) {
            generatedItem(path, "singularity_spark_alt");
            return;
        }
        if (path.equals("black_hole")) {
            generatedItem(path, "singularity_4");
            return;
        }
        if (path.startsWith("circuit_")) {
            generatedItem(path, circuitTexture(path));
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
        if (path.equals("fluid_duct") || path.equals("fluid_duct_neo") || path.equals("fluid_valve") || path.equals("fluid_switch")
                || path.equals("fluid_counter_valve") || path.equals("fluid_duct_box")
                || path.equals("fluid_duct_gauge") || path.equals("fluid_duct_paintable")) {
            getBuilder(path)
                    .parent(new ModelFile.UncheckedModelFile("minecraft:item/generated"))
                    .texture("layer0", modLoc("item/duct"))
                    .texture("layer1", modLoc("item/duct_overlay"));
            return;
        }
        if (path.equals("fluid_duct_paintable_block_exhaust")) {
            getBuilder(path)
                    .parent(new ModelFile.UncheckedModelFile(modLoc("block/fluid_duct_paintable_block_exhaust_overlay")));
            return;
        }
        if (path.equals("pipe_anchor")) {
            getBuilder(path)
                    .parent(new ModelFile.UncheckedModelFile(modLoc("block/network/pipe_anchor")));
            return;
        }
        String abilityTexture = abilityTexture(path);
        if (abilityTexture != null) {
            handheldItem(path, abilityTexture);
            return;
        }
        if (item instanceof HbmAbilityToolItem || item instanceof HbmAbilitySwordItem) {
            handheldItem(path, path);
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

    private void handheldItem(String itemPath, String texturePath) {
        getBuilder(itemPath)
                .parent(new ModelFile.UncheckedModelFile("minecraft:item/handheld"))
                .texture("layer0", modLoc("item/" + texturePath));
    }

    private static String circuitTexture(String itemPath) {
        return "circuit." + itemPath.substring("circuit_".length());
    }

    private static String partGenericTexture(String itemPath) {
        return switch (itemPath) {
            case "part_generic_lde" -> "low_density_element";
            case "part_generic_hde" -> "heavy_duty_element";
            case "part_generic_glass_polarized" -> "glass_polarized";
            default -> itemPath.substring("part_generic_".length());
        };
    }

    @Nullable
    private static String abilityTexture(String itemPath) {
        return switch (itemPath) {
            case "elec_sword" -> "elec_sword_anim";
            case "elec_pickaxe" -> "elec_drill_anim";
            case "elec_axe" -> "elec_chainsaw_anim";
            case "elec_shovel" -> "elec_shovel_anim";
            default -> null;
        };
    }
}
