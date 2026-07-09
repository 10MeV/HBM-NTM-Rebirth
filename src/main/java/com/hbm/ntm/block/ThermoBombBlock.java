package com.hbm.ntm.block;

import com.hbm.ntm.api.block.IBomb;
import com.hbm.ntm.explosion.ExplosionThermo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class ThermoBombBlock extends Block implements IBomb {
    private final Kind kind;

    public ThermoBombBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide() && level.hasNeighborSignal(pos)) {
            explode(level, pos);
        }
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        if (level == null || pos == null || level.isClientSide()) {
            return BombReturnCode.UNDEFINED;
        }
        if (!level.getBlockState(pos).is(this)) {
            return BombReturnCode.UNDEFINED;
        }

        level.removeBlock(pos, false);
        if (kind == Kind.ENDO) {
            ExplosionThermo.freeze(level, pos.getX(), pos.getY(), pos.getZ(), 15);
            ExplosionThermo.freezer(level, pos.getX(), pos.getY(), pos.getZ(), 20);
        } else {
            ExplosionThermo.scorch(level, pos.getX(), pos.getY(), pos.getZ(), 15);
            ExplosionThermo.setEntitiesOnFire(level, pos.getX(), pos.getY(), pos.getZ(), 20);
        }
        level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 5.0F, true, Level.ExplosionInteraction.BLOCK);
        return BombReturnCode.DETONATED;
    }

    public enum Kind {
        ENDO,
        EXO
    }
}
