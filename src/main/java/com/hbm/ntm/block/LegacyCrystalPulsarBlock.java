package com.hbm.ntm.block;

import com.hbm.ntm.explosion.ExplosionChaos;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

/** 1.7.10 CrystalPulsar: a neighbour update hardens nearby dark crystals. */
@SuppressWarnings("deprecation")
public class LegacyCrystalPulsarBlock extends Block {
    private static final int HARDEN_RADIUS = 10;

    public LegacyCrystalPulsarBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        // The old condition is "adjacent virus || !world.isRemote".  Thus every
        // server-side neighbour update invokes the hardening pass; client calls
        // are retained only when adjacent to a virus and are harmlessly ignored
        // by the server-authoritative modern ExplosionChaos bridge.
        if (!level.isClientSide || hasAdjacentVirus(level, pos)) {
            ExplosionChaos.hardenVirus(level, pos.getX(), pos.getY(), pos.getZ(), HARDEN_RADIUS);
        }
    }

    private static boolean hasAdjacentVirus(Level level, BlockPos pos) {
        RegistryObject<? extends Block> virus = ModBlocks.legacyBlock("crystal_virus");
        if (virus == null || !virus.isPresent()) {
            return false;
        }
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(virus.get())) {
                return true;
            }
        }
        return false;
    }
}
