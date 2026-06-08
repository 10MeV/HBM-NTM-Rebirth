package com.hbm.ntm.block;

import com.hbm.ntm.radiation.ArmorUtil;
import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.radiation.RadiationUtil.ContaminationType;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class LegacyGasMeltdownBlock extends LegacyGasBlock {
    public LegacyGasMeltdownBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction radonDirection = Direction.values()[random.nextInt(Direction.values().length)];
        BlockPos radonPos = pos.relative(radonDirection);
        if (random.nextInt(7) == 0 && level.isEmptyBlock(radonPos)) {
            level.setBlock(radonPos, ModBlocks.GAS_RADON_DENSE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        if (level.canSeeSky(pos)) {
            ChunkRadiationManager.incrementRadiation(level, pos, 5.0F);
        }
        if (random.nextInt(350) == 0) {
            level.removeBlock(pos, false);
            return;
        }
        super.tick(state, level, pos, random);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }

        RadiationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, 0.5F);
        RadiationUtil.addRadiationPoisoning(living, 60 * 20, 2);
        if (!ArmorUtil.hasFineParticleProtectionAndDamageFilter(living, 1)) {
            RadiationUtil.applyAsbestos(living, 5, 1);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        level.addParticle(ModParticleTypes.TOWN_AURA.get(),
                pos.getX() + random.nextFloat(),
                pos.getY() + random.nextFloat(),
                pos.getZ() + random.nextFloat(),
                0.0D, 0.0D, 0.0D);
    }

    @Override
    protected Direction firstDirection(ServerLevel level, BlockPos pos, RandomSource random) {
        return random.nextInt(2) == 0 ? Direction.UP : Direction.DOWN;
    }

    @Override
    protected Direction secondDirection(ServerLevel level, BlockPos pos, RandomSource random) {
        return randomHorizontal(random);
    }
}

