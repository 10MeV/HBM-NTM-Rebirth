package com.hbm.blocks.generic;

import com.hbm.ntm.block.LegacyHazardSourceBlock;
import com.hbm.ntm.particle.ParticleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * Legacy 1.7.10 package bridge for generic radioactive hazard resource blocks.
 */
@Deprecated(forRemoval = false)
public class BlockHazard extends LegacyHazardSourceBlock {
    private ExtDisplayEffect extEffect;
    private boolean beaconable;

    public BlockHazard() {
        this("", defaultProperties());
    }

    public BlockHazard(BlockBehaviour.Properties properties) {
        this("", properties);
    }

    public BlockHazard(String legacyName, BlockBehaviour.Properties properties) {
        super(legacyName, properties, Effect.NONE);
    }

    public BlockHazard setDisplayEffect(ExtDisplayEffect extEffect) {
        this.extEffect = extEffect;
        return this;
    }

    public BlockHazard makeBeaconable() {
        this.beaconable = true;
        return this;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (extEffect == null) {
            return;
        }

        switch (extEffect) {
            case RADFOG -> ParticleUtil.spawnTownAuraOnOpenFaces(level, pos, random);
            case SCHRAB -> ParticleUtil.spawnSchrabFogOnOpenFaces(level, pos, random);
            case FLAMES -> spawnFlamesOnOpenFaces(level, pos, random);
            case LAVAPOP -> level.addParticle(ParticleTypes.LAVA,
                    pos.getX() + random.nextFloat(),
                    pos.getY() + 1.1D,
                    pos.getZ() + random.nextFloat(),
                    0.0D, 0.0D, 0.0D);
            case SPARKS -> {
            }
        }
    }

    private static void spawnFlamesOnOpenFaces(Level level, BlockPos pos, RandomSource random) {
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN || !level.isEmptyBlock(pos.relative(direction))) {
                continue;
            }

            double x = pos.getX() + 0.5D + direction.getStepX() + random.nextDouble() * 3.0D - 1.5D;
            double y = pos.getY() + 0.5D + direction.getStepY() + random.nextDouble() * 3.0D - 1.5D;
            double z = pos.getZ() + 0.5D + direction.getStepZ() + random.nextDouble() * 3.0D - 1.5D;

            if (direction.getStepX() != 0) {
                x = pos.getX() + 0.5D + direction.getStepX() * 0.5D
                        + random.nextDouble() * direction.getStepX();
            }
            if (direction.getStepY() != 0) {
                y = pos.getY() + 0.5D + direction.getStepY() * 0.5D
                        + random.nextDouble() * direction.getStepY();
            }
            if (direction.getStepZ() != 0) {
                z = pos.getZ() + 0.5D + direction.getStepZ() * 0.5D
                        + random.nextDouble() * direction.getStepZ();
            }

            level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.1D, 0.0D);
        }
    }

    private static BlockBehaviour.Properties defaultProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops();
    }

    public enum ExtDisplayEffect {
        RADFOG,
        SPARKS,
        SCHRAB,
        FLAMES,
        LAVAPOP
    }
}
