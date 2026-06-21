package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

final class LegacyVntBlockStateMapper {

    static BlockState fromLegacyMeta(Block block, int meta) {
        if (isLegacyBlock(block, "block_slag") && meta == 1) {
            return ModBlocks.BLOCK_SLAG_BROKEN.get().defaultBlockState();
        }
        return block.defaultBlockState();
    }

    private static boolean isLegacyBlock(Block block, String legacyName) {
        RegistryObject<? extends Block> legacyBlock = ModBlocks.legacyBlock(legacyName);
        return legacyBlock != null && block == legacyBlock.get();
    }

    private LegacyVntBlockStateMapper() {
    }
}
