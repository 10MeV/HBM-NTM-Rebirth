package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Bomb.BombReturnCode;
import com.hbm.ntm.api.block.IBomb;
import com.hbm.ntm.entity.effect.EmpBlastEntity;
import com.hbm.ntm.explosion.ExplosionNukeGeneric;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class EmpBombBlock extends Block implements IBomb {
    private static final int EMP_STRENGTH = 50;

    public EmpBombBlock(Properties properties) {
        super(properties);
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

        LegacySoundPlayer.playSoundEffect(level, pos.getX(), pos.getY(), pos.getZ(),
                "hbm:weapon.sparkShoot", SoundSource.BLOCKS, 5.0F, level.random.nextFloat() * 0.2F + 0.9F);
        level.removeBlock(pos, false);
        ExplosionNukeGeneric.empBlast(level, pos.getX(), pos.getY(), pos.getZ(), EMP_STRENGTH);
        level.addFreshEntity(EmpBlastEntity.create(level,
                pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, EMP_STRENGTH));
        return BombReturnCode.DETONATED;
    }
}
