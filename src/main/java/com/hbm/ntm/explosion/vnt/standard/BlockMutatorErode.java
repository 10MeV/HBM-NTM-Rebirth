package com.hbm.ntm.explosion.vnt.standard;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.interfaces.BlockMutator;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BlockMutatorErode implements BlockMutator {
    private final Map<Block, Block> replacements = new LinkedHashMap<>();
    private final Set<BlockPos> checked = new HashSet<>();
    private final Set<BlockPos> eroded = new HashSet<>();
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
    public boolean suppressDrops(ExplosionVnt explosion, BlockState state, BlockPos pos) {
        Block replacement = replacements.get(state.getBlock());
        if (replacement == null) {
            return false;
        }

        BlockPos key = pos.immutable();
        checked.add(key);
        boolean shouldErode = always || explosion.level().random.nextFloat() < 0.6F;
        if (shouldErode) {
            eroded.add(key);
        }
        return shouldErode;
    }

    @Override
    public void mutatePre(ExplosionVnt explosion, BlockState state, BlockPos pos) {
        Block replacement = replacements.get(state.getBlock());
        if (replacement == null) {
            return;
        }

        BlockPos key = pos.immutable();
        if (checked.remove(key)) {
            if (eroded.remove(key)) {
                explosion.level().setBlock(pos, replacement.defaultBlockState(), 3);
            }
            return;
        }

        if (always || explosion.level().random.nextFloat() < 0.6F) {
            explosion.level().setBlock(pos, replacement.defaultBlockState(), 3);
        }
    }

    public boolean canErode(BlockState state) {
        return replacements.containsKey(state.getBlock());
    }

    private void addReplacement(String from, String to) {
        RegistryObject<? extends Block> fromBlock = ModBlocks.legacyBlock(from);
        RegistryObject<? extends Block> toBlock = ModBlocks.legacyBlock(to);
        replacements.put(
                Objects.requireNonNull(fromBlock, "Missing legacy block hbm_ntm_rebirth:" + from).get(),
                Objects.requireNonNull(toBlock, "Missing legacy block hbm_ntm_rebirth:" + to).get());
    }

    private void addReplacement(String from, Block to) {
        RegistryObject<? extends Block> fromBlock = ModBlocks.legacyBlock(from);
        replacements.put(Objects.requireNonNull(fromBlock, "Missing legacy block hbm_ntm_rebirth:" + from).get(), to);
    }
}
