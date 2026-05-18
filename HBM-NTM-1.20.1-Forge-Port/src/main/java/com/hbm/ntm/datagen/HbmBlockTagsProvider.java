package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class HbmBlockTagsProvider extends BlockTagsProvider {
    public HbmBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        ModBlocks.MACHINE_TAB_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModBlocks.MACHINE_TAB_BLOCKS.forEach(block -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get()));
        ModBlocks.NUKE_TAB_BLOCKS.forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        ModBlocks.NUKE_TAB_BLOCKS.forEach(block -> tag(BlockTags.NEEDS_IRON_TOOL).add(block.get()));
    }
}
