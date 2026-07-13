package com.hbm.ntm.blockentity;

import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.block.LegacyGasBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LegacyChlorineSealBlockEntity extends BlockEntity {
    public LegacyChlorineSealBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.CHLORINE_SEAL.get(), pos, state); }
    public static void serverTick(Level level, BlockPos pos, BlockState state, LegacyChlorineSealBlockEntity seal) {
        if (level.hasNeighborSignal(pos)) seal.spread(pos, 0);
    }
    private void spread(BlockPos pos, int index) {
        if (index > 50) return;
        BlockState target = level.getBlockState(pos);
        if (target.isAir() || target.canBeReplaced() || target.getBlock() instanceof LegacyGasBlock) {
            level.setBlock(pos, ModBlocks.CHLORINE_GAS.get().defaultBlockState(), 3);
        }
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.CHLORINE_GAS.get()) && !state.is(ModBlocks.VENT_CHLORINE_SEAL.get())) return;
        Direction direction = Direction.values()[level.random.nextInt(6)];
        spread(pos.relative(direction), index + 1);
    }
}
