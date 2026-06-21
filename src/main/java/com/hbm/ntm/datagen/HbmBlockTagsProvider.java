package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

import java.util.concurrent.CompletableFuture;

public class HbmBlockTagsProvider extends BlockTagsProvider {
    public HbmBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ModBlocks.MACHINE_TAB_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModBlocks.MACHINE_TAB_BLOCKS.forEach(block -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get()));
        ModBlocks.TURRET_TAB_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModBlocks.TURRET_TAB_BLOCKS.forEach(block -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get()));
        ModBlocks.HIDDEN_MACHINE_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModBlocks.HIDDEN_MACHINE_BLOCKS.forEach(block -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get()));
        ModBlocks.SATELLITE_TAB_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModBlocks.SATELLITE_TAB_BLOCKS.forEach(block -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get()));
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.DUMMY_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.DUMMY_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.STEEL_SCAFFOLD.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.STEEL_SCAFFOLD.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.STEEL_BEAM.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.STEEL_BEAM.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.STEEL_GRATE.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.STEEL_GRATE.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.STEEL_GRATE_WIDE.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.STEEL_GRATE_WIDE.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.CHAIN.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.STRUCT_ICF_CORE.get(),
                ModBlocks.ICF_COMPONENT_SCAFFOLD.get(),
                ModBlocks.ICF_COMPONENT_VESSEL.get(),
                ModBlocks.ICF_COMPONENT_VESSEL_WELDED.get(),
                ModBlocks.ICF_COMPONENT_STRUCTURE.get(),
                ModBlocks.ICF_COMPONENT_STRUCTURE_BOLTED.get(),
                ModBlocks.ICF_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.STRUCT_ICF_CORE.get(),
                ModBlocks.ICF_COMPONENT_SCAFFOLD.get(),
                ModBlocks.ICF_COMPONENT_VESSEL.get(),
                ModBlocks.ICF_COMPONENT_VESSEL_WELDED.get(),
                ModBlocks.ICF_COMPONENT_STRUCTURE.get(),
                ModBlocks.ICF_COMPONENT_STRUCTURE_BOLTED.get(),
                ModBlocks.ICF_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.STRUCT_TORUS_CORE.get(),
                ModBlocks.FUSION_COMPONENT_BSCCO.get(),
                ModBlocks.FUSION_COMPONENT_BSCCO_WELDED.get(),
                ModBlocks.FUSION_COMPONENT_BLANKET.get(),
                ModBlocks.FUSION_COMPONENT_MOTOR.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.STRUCT_TORUS_CORE.get(),
                ModBlocks.FUSION_COMPONENT_BSCCO.get(),
                ModBlocks.FUSION_COMPONENT_BSCCO_WELDED.get(),
                ModBlocks.FUSION_COMPONENT_BLANKET.get(),
                ModBlocks.FUSION_COMPONENT_MOTOR.get());
        ModBlocks.CAP_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModBlocks.CAP_BLOCKS.forEach(block -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get()));
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.BLOCK_SLAG_BROKEN.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.BLOCK_SLAG_BROKEN.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.PRIBRIS.get(), ModBlocks.PRIBRIS_BURNING.get(),
                ModBlocks.PRIBRIS_RADIATING.get(), ModBlocks.PRIBRIS_DIGAMMA.get(), ModBlocks.VOLCANIC_LAVA_BLOCK.get(),
                ModBlocks.RAD_LAVA_BLOCK.get(), ModBlocks.SELLAFIELD.get(), ModBlocks.SELLAFIELD_SLAKED.get(),
                ModBlocks.SELLAFIELD_BEDROCK.get(), ModBlocks.ORE_SELLAFIELD_DIAMOND.get(),
                ModBlocks.ORE_SELLAFIELD_EMERALD.get(), ModBlocks.ORE_SELLAFIELD_URANIUM_SCORCHED.get(),
                ModBlocks.ORE_SELLAFIELD_SCHRABIDIUM.get(), ModBlocks.ORE_SELLAFIELD_RADGEM.get(),
                ModBlocks.GLASS_TRINITITE.get(), ModBlocks.REINFORCED_LAMINATE.get(),
                ModBlocks.REINFORCED_LAMINATE_PANE.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.PRIBRIS.get(), ModBlocks.PRIBRIS_BURNING.get(),
                ModBlocks.PRIBRIS_RADIATING.get(), ModBlocks.PRIBRIS_DIGAMMA.get(), ModBlocks.VOLCANIC_LAVA_BLOCK.get(),
                ModBlocks.RAD_LAVA_BLOCK.get(), ModBlocks.SELLAFIELD.get(), ModBlocks.SELLAFIELD_SLAKED.get(),
                ModBlocks.SELLAFIELD_BEDROCK.get(), ModBlocks.ORE_SELLAFIELD_DIAMOND.get(),
                ModBlocks.ORE_SELLAFIELD_EMERALD.get(), ModBlocks.ORE_SELLAFIELD_URANIUM_SCORCHED.get(),
                ModBlocks.ORE_SELLAFIELD_SCHRABIDIUM.get(), ModBlocks.ORE_SELLAFIELD_RADGEM.get(),
                ModBlocks.GLASS_TRINITITE.get(), ModBlocks.REINFORCED_LAMINATE.get(),
                ModBlocks.REINFORCED_LAMINATE_PANE.get());
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.ASH_DIGAMMA.get(), ModBlocks.WASTE_TRINITITE.get(),
                ModBlocks.WASTE_TRINITITE_RED.get(), ModBlocks.FROZEN_GRASS.get(), ModBlocks.FROZEN_DIRT.get(),
                ModBlocks.TEKTITE.get(), ModBlocks.ORE_TEKTITE_OSMIRIDIUM.get(), ModBlocks.MOON_TURF.get(),
                ModBlocks.SAND_BORON.get(), ModBlocks.SAND_LEAD.get(), ModBlocks.SAND_URANIUM.get(),
                ModBlocks.SAND_POLONIUM.get(), ModBlocks.SAND_QUARTZ.get());
        addLegacyMineable(BlockTags.MINEABLE_WITH_SHOVEL, "dirt_dead", "dirt_oily", "sand_dirty", "sand_dirty_red");
        addLegacyMineable(BlockTags.MINEABLE_WITH_PICKAXE, "stone_cracked",
                "stone_depth", "ore_depth_cinnebar", "ore_depth_zirconium", "ore_depth_borax",
                "cluster_depth_iron", "cluster_depth_titanium", "cluster_depth_tungsten",
                "depth_brick", "depth_tiles", "depth_nether_brick", "depth_nether_tiles", "depth_dnt",
                "stone_depth_nether", "ore_depth_nether_neodymium");
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.FROZEN_LOG.get(), ModBlocks.FROZEN_PLANKS.get());
        ModBlocks.CONVEYOR_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModBlocks.CONVEYOR_BLOCKS.forEach(block -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get()));
        tag(BlockTags.MINEABLE_WITH_AXE).add(
                ModBlocks.RED_PYLON_MEDIUM_WOOD.get(),
                ModBlocks.RED_PYLON_MEDIUM_WOOD_TRANSFORMER.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ModBlocks.RED_CONNECTOR.get(),
                ModBlocks.RED_CONNECTOR_SUPER.get(),
                ModBlocks.RED_PYLON.get(),
                ModBlocks.RED_PYLON_MEDIUM_STEEL.get(),
                ModBlocks.RED_PYLON_MEDIUM_STEEL_TRANSFORMER.get(),
                ModBlocks.RED_PYLON_LARGE.get(),
                ModBlocks.SUBSTATION.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(
                ModBlocks.RED_CONNECTOR.get(),
                ModBlocks.RED_CONNECTOR_SUPER.get(),
                ModBlocks.RED_PYLON.get(),
                ModBlocks.RED_PYLON_MEDIUM_STEEL.get(),
                ModBlocks.RED_PYLON_MEDIUM_STEEL_TRANSFORMER.get(),
                ModBlocks.RED_PYLON_LARGE.get(),
                ModBlocks.SUBSTATION.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
                ModBlocks.BLOCK_GRAPHITE.get(),
                ModBlocks.BLOCK_GRAPHITE_DRILLED.get(),
                ModBlocks.BLOCK_GRAPHITE_FUEL.get(),
                ModBlocks.BLOCK_GRAPHITE_PLUTONIUM.get(),
                ModBlocks.BLOCK_GRAPHITE_ROD.get(),
                ModBlocks.BLOCK_GRAPHITE_SOURCE.get(),
                ModBlocks.BLOCK_GRAPHITE_LITHIUM.get(),
                ModBlocks.BLOCK_GRAPHITE_TRITIUM.get(),
                ModBlocks.BLOCK_GRAPHITE_DETECTOR.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(
                ModBlocks.BLOCK_GRAPHITE.get(),
                ModBlocks.BLOCK_GRAPHITE_DRILLED.get(),
                ModBlocks.BLOCK_GRAPHITE_FUEL.get(),
                ModBlocks.BLOCK_GRAPHITE_PLUTONIUM.get(),
                ModBlocks.BLOCK_GRAPHITE_ROD.get(),
                ModBlocks.BLOCK_GRAPHITE_SOURCE.get(),
                ModBlocks.BLOCK_GRAPHITE_LITHIUM.get(),
                ModBlocks.BLOCK_GRAPHITE_TRITIUM.get(),
                ModBlocks.BLOCK_GRAPHITE_DETECTOR.get());
        ModBlocks.NUKE_TAB_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModBlocks.NUKE_TAB_BLOCKS.forEach(block -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get()));
        tag(forgeBlockTag("glass")).add(Blocks.GLASS, Blocks.WHITE_STAINED_GLASS,
                Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS,
                Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.PINK_STAINED_GLASS,
                Blocks.GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS,
                Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS,
                Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS,
                ModBlocks.GLASS_BORON.get(), ModBlocks.GLASS_LEAD.get(), ModBlocks.GLASS_URANIUM.get(),
                ModBlocks.GLASS_POLONIUM.get(), ModBlocks.GLASS_QUARTZ.get(), ModBlocks.GLASS_TRINITITE.get(),
                ModBlocks.REINFORCED_LAMINATE.get());
        tag(forgeBlockTag("glass_panes")).add(ModBlocks.REINFORCED_LAMINATE_PANE.get());

        addLegacyForgeOreTag("uranium", "ore_uranium", "ore_uranium_scorched", "ore_nether_uranium", "ore_nether_uranium_scorched", "ore_gneiss_uranium", "ore_gneiss_uranium_scorched");
        addLegacyForgeOreTag("thorium", "ore_thorium");
        addLegacyForgeOreTag("schrabidium", "ore_schrabidium", "ore_nether_schrabidium", "ore_gneiss_schrabidium");
        addLegacyForgeOreTag("lignite", "ore_lignite");
        addLegacyForgeOreTag("asbestos", "ore_asbestos", "ore_gneiss_asbestos");
        addLegacyForgeOreTag("coal", "ore_nether_coal");
        addLegacyForgeTag("storage_blocks/lead", "block_lead");
        addLegacyForgeTag("storage_blocks/niobium", "block_niobium");
    }

    private void addLegacyForgeOreTag(String material, String... blockNames) {
        TagKey<Block> materialTag = forgeBlockTag("ores/" + material);
        for (String blockName : blockNames) {
            RegistryObject<? extends Block> block = ModBlocks.legacyBlock(blockName);
            if (block != null) {
                tag(materialTag).add(block.get());
                tag(forgeBlockTag("ores")).add(block.get());
            }
        }
    }

    private void addLegacyForgeTag(String path, String... blockNames) {
        TagKey<Block> tag = forgeBlockTag(path);
        for (String blockName : blockNames) {
            RegistryObject<? extends Block> block = ModBlocks.legacyBlock(blockName);
            if (block != null) {
                tag(tag).add(block.get());
            }
        }
    }

    private void addLegacyMineable(TagKey<Block> tagKey, String... blockNames) {
        for (String blockName : blockNames) {
            RegistryObject<? extends Block> block = ModBlocks.legacyBlock(blockName);
            if (block != null) {
                tag(tagKey).add(block.get());
            }
        }
    }

    static TagKey<Block> forgeBlockTag(String path) {
        return BlockTags.create(new ResourceLocation("forge", path));
    }
}
