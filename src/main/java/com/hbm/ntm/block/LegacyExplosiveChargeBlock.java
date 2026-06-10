package com.hbm.ntm.block;

import com.hbm.ntm.api.block.ChainExplodable;
import com.hbm.ntm.entity.item.LegacyPrimedExplosiveEntity;
import com.hbm.ntm.explosion.ExplosionNT;
import com.hbm.ntm.explosion.NuclearExplosionUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class LegacyExplosiveChargeBlock extends Block implements ChainExplodable, DetConnectibleBlock, RemoteDetonatableBlock {
    private final Kind kind;

    public LegacyExplosiveChargeBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            detonate(level, pos);
        }
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.removeBlock(pos, false);
        if (!level.isClientSide) {
            level.addFreshEntity(LegacyPrimedExplosiveEntity.create(level,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, this, 0, false));
        }
    }

    @Override
    public void explodeEntity(Level level, Vec3 position, @Nullable Entity source) {
        if (!level.isClientSide) {
            explodeAt(level, position.x, position.y, position.z);
        }
    }

    @Override
    public BombReturnCode detonateFromRemote(Level level, BlockPos pos) {
        if (level == null || level.isClientSide) {
            return BombReturnCode.UNDEFINED;
        }
        return detonate(level, pos) ? BombReturnCode.DETONATED : BombReturnCode.ERROR_NO_BOMB;
    }

    private boolean detonate(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() != this) {
            return false;
        }
        level.removeBlock(pos, false);
        explodeAt(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        return true;
    }

    private void explodeAt(Level level, double x, double y, double z) {
        if (kind == Kind.NUCLEAR) {
            NuclearExplosionUtil.spawnMissileNuclear(level, x, y, z);
        } else {
            new ExplosionNT(level, null, x, y, z, 15.0F)
                    .overrideResolution(64)
                    .explode();
        }
    }

    public enum Kind {
        CONVENTIONAL,
        NUCLEAR
    }
}
