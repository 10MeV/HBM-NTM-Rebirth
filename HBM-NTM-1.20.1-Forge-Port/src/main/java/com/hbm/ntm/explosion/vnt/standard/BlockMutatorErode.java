package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockMutator;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class BlockMutatorErode implements BlockMutator {
    private final Map<Block, Block> replacements = new LinkedHashMap<>();
    private final boolean always;

    public BlockMutatorErode() {
        this(false);
    }

    public BlockMutatorErode(boolean always) {
        this.always = always;
        addReplacement("concrete", Blocks.GRAVEL);
        addReplacement("concrete_smooth", Blocks.GRAVEL);
        addReplacement("brick_concrete", "brick_concrete_broken");
        addReplacement("brick_concrete_broken", Blocks.GRAVEL);
    }

    @Override
    public void mutatePre(ExplosionVnt explosion, BlockState state, BlockPos pos) {
        Block replacement = replacements.get(state.getBlock());
        if (replacement != null && (always || explosion.level().random.nextFloat() < 0.6F)) {
            explosion.level().setBlock(pos, replacement.defaultBlockState(), 3);
        }
    }

    private void addReplacement(String from, String to) {
        RegistryObject<? extends Block> fromBlock = ModBlocks.legacyBlock(from);
        RegistryObject<? extends Block> toBlock = ModBlocks.legacyBlock(to);
        if (fromBlock != null && toBlock != null) {
            replacements.put(fromBlock.get(), toBlock.get());
        }
    }

    private void addReplacement(String from, Block to) {
        RegistryObject<? extends Block> fromBlock = ModBlocks.legacyBlock(from);
        if (fromBlock != null) {
            replacements.put(fromBlock.get(), to);
        }
    }
}
