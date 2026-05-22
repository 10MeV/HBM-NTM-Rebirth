package com.hbm.ntm.datagen;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.concurrent.CompletableFuture;

public class HbmItemTagsProvider extends ItemTagsProvider {
    public HbmItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, HbmNtm.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(HbmBlockTagsProvider.forgeBlockTag("ores"), forgeItemTag("ores"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/uranium"), forgeItemTag("ores/uranium"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/thorium"), forgeItemTag("ores/thorium"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/schrabidium"), forgeItemTag("ores/schrabidium"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/lignite"), forgeItemTag("ores/lignite"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/asbestos"), forgeItemTag("ores/asbestos"));
        copy(HbmBlockTagsProvider.forgeBlockTag("ores/coal"), forgeItemTag("ores/coal"));

        addLegacyForgeTag("dusts/lignite", "powder_lignite");
        addLegacyForgeTag("dusts", "powder_lignite");
        addLegacyForgeTag("dusts/iron", "powder_iron");
        addLegacyForgeTag("dusts/lithium", "powder_lithium");
        addLegacyForgeTag("dusts/cobalt", "powder_cobalt");
        addLegacyForgeTag("dusts/sodium", "powder_sodium");
        addLegacyForgeTag("dusts/schrabidium", "powder_schrabidium");
        addLegacyForgeTag("dusts/spark_mix", "powder_spark_mix");
        addLegacyForgeTag("dusts", "powder_iron", "powder_lithium", "powder_cobalt", "powder_sodium", "powder_schrabidium", "powder_spark_mix");
        addLegacyForgeTag("ingots/lead", "ingot_lead");
        addLegacyForgeTag("ingots/bismuth", "ingot_bismuth");
        addLegacyForgeTag("ingots/niobium", "ingot_niobium");
        addLegacyForgeTag("ingots/tantalum", "ingot_tantalium");
        addLegacyForgeTag("ingots/tantalium", "ingot_tantalium");
        addLegacyForgeTag("ingots/cft", "ingot_cft");
        addLegacyForgeTag("ingots/polymer", "ingot_polymer");
        addLegacyForgeTag("ingots/bakelite", "ingot_bakelite");
        addLegacyForgeTag("ingots/polycarbonate", "ingot_pc");
        addLegacyForgeTag("ingots/pc", "ingot_pc");
        addLegacyForgeTag("ingots/pvc", "ingot_pvc");
        addLegacyForgeTag("ingots/any_plastic", "ingot_polymer", "ingot_bakelite");
        addLegacyForgeTag("ingots/plastic", "ingot_polymer", "ingot_bakelite");
        addLegacyForgeTag("ingots/any_hardplastic", "ingot_pc", "ingot_pvc");
        addLegacyForgeTag("ingots/hard_plastic", "ingot_pc", "ingot_pvc");
        addLegacyForgeTag("ingots", "ingot_lead", "ingot_bismuth", "ingot_niobium", "ingot_tantalium", "ingot_cft",
                "ingot_polymer", "ingot_bakelite", "ingot_pc", "ingot_pvc");
        addLegacyForgeTag("wires/gold", "wire_gold");
        addLegacyForgeTag("wires", "wire_gold");
        addLegacyForgeTag("dense_wires/gold", "wire_dense_gold");
        addLegacyForgeTag("dense_wires/niobium", "wire_dense_niobium");
        addLegacyForgeTag("dense_wires/bscco", "wire_dense_bscco");
        addLegacyForgeTag("dense_wires", "wire_dense_gold", "wire_dense_niobium", "wire_dense_bscco");
        addLegacyForgeTag("cast_plates/bismuth_bronze", "plate_cast_bismuth_bronze");
        addLegacyForgeTag("cast_plates/arsenic_bronze", "plate_cast_arsenic_bronze");
        addLegacyForgeTag("cast_plates/any_bismoid_bronze", "plate_cast_bismuth_bronze", "plate_cast_arsenic_bronze");
        addLegacyForgeTag("cast_plates/combine_steel", "plate_cast_combine_steel");
        addLegacyForgeTag("cast_plates", "plate_cast_bismuth_bronze", "plate_cast_arsenic_bronze", "plate_cast_combine_steel");
        addLegacyForgeTag("circuits/chip_quantum", "circuit_chip_quantum");
        addLegacyForgeTag("circuits", "circuit_chip_quantum");
        addLegacyForgeTag("gems/coal", net.minecraft.world.item.Items.COAL, net.minecraft.world.item.Items.CHARCOAL);
        addLegacyForgeTag("gems/lignite", "lignite", "coal_infernal");
    }

    private void addLegacyForgeTag(String path, String... itemNames) {
        TagKey<Item> tag = forgeItemTag(path);
        for (String itemName : itemNames) {
            RegistryObject<Item> item = ModItems.legacyItem(itemName);
            if (item != null) {
                tag(tag).add(item.get());
            }
        }
    }

    private void addLegacyForgeTag(String path, Item... items) {
        tag(forgeItemTag(path)).add(items);
    }

    static TagKey<Item> forgeItemTag(String path) {
        return ItemTags.create(new ResourceLocation("forge", path));
    }
}
