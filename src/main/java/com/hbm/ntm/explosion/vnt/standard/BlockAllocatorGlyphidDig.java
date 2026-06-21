package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import java.util.Objects;

public class BlockAllocatorGlyphidDig extends BlockAllocatorBulkie {
    private static final RegistryObject<? extends Block> GLYPHID_SPAWNER = requireLegacyBlock("glyphid_spawner");

    public BlockAllocatorGlyphidDig(double maximumResistance) {
        this(maximumResistance, 16);
    }

    public BlockAllocatorGlyphidDig(double maximumResistance, int resolution) {
        super(maximumResistance, resolution, BlockAllocatorGlyphidDig::isDigImmune);
    }

    private static boolean isDigImmune(BlockState state) {
        return state.is(GLYPHID_SPAWNER.get());
    }

    private static RegistryObject<? extends Block> requireLegacyBlock(String name) {
        RegistryObject<? extends Block> block = ModBlocks.legacyBlock(name);
        return Objects.requireNonNull(block, "Missing legacy block hbm_ntm_rebirth:" + name);
    }
}
