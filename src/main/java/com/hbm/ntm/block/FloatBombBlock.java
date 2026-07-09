package com.hbm.ntm.block;

import com.hbm.ntm.api.block.IBomb;
import com.hbm.ntm.explosion.ExplosionChaos;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class FloatBombBlock extends Block implements IBomb {
    public FloatBombBlock(Properties properties) {
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
        ExplosionChaos.floater(level, pos.getX(), pos.getY(), pos.getZ(), 15, 50);
        ExplosionChaos.move(level, pos.getX(), pos.getY(), pos.getZ(), 15, 0, 50, 0);
        return BombReturnCode.DETONATED;
    }
}
