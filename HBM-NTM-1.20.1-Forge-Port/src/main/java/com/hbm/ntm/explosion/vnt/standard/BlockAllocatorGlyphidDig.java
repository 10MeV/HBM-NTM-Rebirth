package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public class BlockAllocatorGlyphidDig extends BlockAllocatorBulkie {
    public BlockAllocatorGlyphidDig(double maximumResistance) {
        this(maximumResistance, 16);
    }

    public BlockAllocatorGlyphidDig(double maximumResistance, int resolution) {
        super(maximumResistance, resolution, BlockAllocatorGlyphidDig::isDigImmune);
    }

    private static boolean isDigImmune(BlockState state) {
        RegistryObject<? extends Block> glyphidSpawner = ModBlocks.legacyBlock("glyphid_spawner");
        return glyphidSpawner != null && glyphidSpawner.isPresent() && state.is(glyphidSpawner.get());
    }
}
