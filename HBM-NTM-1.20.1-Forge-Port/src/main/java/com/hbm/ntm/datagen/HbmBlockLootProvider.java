package com.hbm.ntm.datagen;

import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;

public class HbmBlockLootProvider extends BlockLootSubProvider {
    public HbmBlockLootProvider() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        ModBlocks.MACHINE_TAB_BLOCKS.forEach(block -> dropSelf(block.get()));
        ModBlocks.BLOCK_TAB_BLOCKS.forEach(block -> dropSelf(block.get()));
        ModBlocks.NUKE_TAB_BLOCKS.forEach(block -> dropSelf(block.get()));
        add(ModBlocks.GAS_MELTDOWN.get(), noDrop());
        add(ModBlocks.FALLOUT.get(), block -> createSingleItemTable(ModItems.legacyItem("fallout").get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
