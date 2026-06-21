package com.hbm.ntm.block;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
public class LegacyOreBlock extends RadiatingHazardBlock {
    private final String legacyName;

    public LegacyOreBlock(String legacyName, Properties properties) {
        super(legacyName, properties);
        this.legacyName = legacyName;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        if (level.isClientSide || !(entity instanceof LivingEntity living)) {
            return;
        }

        if ("block_trinitite".equals(legacyName) || "block_waste".equals(legacyName)) {
            RadiationUtil.addRadiationPoisoning(living, 30 * 20, 2);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if ("block_trinitite".equals(legacyName)) {
            ParticleUtil.spawnTownAuraOnOpenFaces(level, pos, random);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.isClientSide || !"ore_oil".equals(legacyName)) {
            return;
        }

        Block oilEmpty = ModBlocks.legacyBlock("ore_oil_empty").get();
        BlockPos below = pos.below();
        if (level.getBlockState(below).is(oilEmpty)) {
            level.setBlock(pos, oilEmpty.defaultBlockState(), Block.UPDATE_ALL);
            level.setBlock(below, state, Block.UPDATE_ALL);
        }
    }
}
