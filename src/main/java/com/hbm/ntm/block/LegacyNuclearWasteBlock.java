package com.hbm.ntm.block;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyNuclearWasteBlock extends RadiatingHazardBlock {
    private final String legacyName;

    public LegacyNuclearWasteBlock(String legacyName, Properties properties) {
        super(legacyName, properties);
        this.legacyName = legacyName;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction direction = Direction.values()[random.nextInt(6)];
        BlockPos target = pos.relative(direction);
        if (random.nextInt(2) == 0 && level.isEmptyBlock(target)) {
            level.setBlock(target, ModBlocks.GAS_RADON_DENSE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        super.tick(state, level, pos, random);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        ParticleUtil.spawnTownAuraOnOpenFaces(level, pos, random);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (!level.isClientSide && "block_waste".equals(legacyName) && entity instanceof LivingEntity living) {
            RadiationUtil.addRadiationPoisoning(living, 30 * 20, 2);
        }
    }
}
