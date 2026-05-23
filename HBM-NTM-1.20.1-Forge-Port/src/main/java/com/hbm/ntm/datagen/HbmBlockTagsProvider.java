package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
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
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.DUMMY_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.DUMMY_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.PRIBRIS_DIGAMMA.get(), ModBlocks.VOLCANIC_LAVA_BLOCK.get(),
                ModBlocks.RAD_LAVA_BLOCK.get());
        tag(BlockTags.NEEDS_IRON_TOOL).add(ModBlocks.PRIBRIS_DIGAMMA.get(), ModBlocks.VOLCANIC_LAVA_BLOCK.get(),
                ModBlocks.RAD_LAVA_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(ModBlocks.ASH_DIGAMMA.get());
        ModBlocks.CONVEYOR_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModBlocks.CONVEYOR_BLOCKS.forEach(block -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get()));
        ModBlocks.NUKE_TAB_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModBlocks.NUKE_TAB_BLOCKS.forEach(block -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get()));
        tag(forgeBlockTag("glass")).add(ModBlocks.GLASS_BORON.get());

        addLegacyForgeOreTag("uranium", "ore_uranium", "ore_uranium_scorched", "ore_nether_uranium", "ore_nether_uranium_scorched", "ore_gneiss_uranium", "ore_gneiss_uranium_scorched");
        addLegacyForgeOreTag("thorium", "ore_thorium");
        addLegacyForgeOreTag("schrabidium", "ore_schrabidium", "ore_nether_schrabidium", "ore_gneiss_schrabidium");
        addLegacyForgeOreTag("lignite", "ore_lignite");
        addLegacyForgeOreTag("asbestos", "ore_asbestos", "ore_gneiss_asbestos");
        addLegacyForgeOreTag("coal", "ore_nether_coal");
        addLegacyForgeTag("storage_blocks/lead", "block_lead");
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

    static TagKey<Block> forgeBlockTag(String path) {
        return BlockTags.create(new ResourceLocation("forge", path));
    }
}
