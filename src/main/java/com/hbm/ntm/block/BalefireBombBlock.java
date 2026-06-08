package com.hbm.ntm.block;

import com.hbm.ntm.api.block.Bomb;
import com.hbm.ntm.explosion.vnt.WeaponExplosionUtil;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class BalefireBombBlock extends HorizontalMachineBlock implements Bomb {
    public static final int DEFAULT_RANGE = 250;

    public BalefireBombBlock(Properties properties) {
        super(properties, false);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, net.minecraft.world.level.block.Block block,
            BlockPos fromPos, boolean moving) {
        if (!level.isClientSide() && level.hasNeighborSignal(pos)) {
            detonate(level, pos);
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        if (!oldState.is(state.getBlock()) && !level.isClientSide() && level.hasNeighborSignal(pos)) {
            detonate(level, pos);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide() && level.hasNeighborSignal(pos)) {
            detonate(level, pos);
        }
    }

    @Override
    public void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face,
            @Nullable LivingEntity igniter) {
        if (!level.isClientSide()) {
            detonate(level, pos);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide()) {
            WeaponExplosionUtil.spawnBalefire(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, DEFAULT_RANGE);
        }
    }

    @Override
    public BombReturnCode explode(Level level, BlockPos pos) {
        return detonate(level, pos);
    }

    private BombReturnCode detonate(Level level, BlockPos pos) {
        if (level.getBlockState(pos).getBlock() != this) {
            return BombReturnCode.ERROR_INCOMPATIBLE;
        }

        level.playSound(null, pos, ModSounds.WEAPON_FSTBMB_START.get(), SoundSource.BLOCKS, 5.0F, 1.0F);
        level.removeBlock(pos, false);
        level.gameEvent(null, GameEvent.EXPLODE, pos);
        WeaponExplosionUtil.spawnBalefire(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, DEFAULT_RANGE);
        return BombReturnCode.DETONATED;
    }
}
