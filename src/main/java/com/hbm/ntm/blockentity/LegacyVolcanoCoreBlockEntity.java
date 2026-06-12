package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.LegacyVolcanoCoreBlock;
import com.hbm.ntm.entity.projectile.ShrapnelEntity;
import com.hbm.ntm.explosion.ExplosionNT;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

public class LegacyVolcanoCoreBlockEntity extends BlockEntity {
    private static final String TAG_TIMER = "timer";
    private static final List<ExplosionNT.ExAttrib> VOLCANO_EXPLOSION = List.of(
            ExplosionNT.ExAttrib.NODROP,
            ExplosionNT.ExAttrib.LAVA_V,
            ExplosionNT.ExAttrib.NOSOUND,
            ExplosionNT.ExAttrib.ALLMOD,
            ExplosionNT.ExAttrib.NOHURT);
    private static final List<ExplosionNT.ExAttrib> RAD_VOLCANO_EXPLOSION = List.of(
            ExplosionNT.ExAttrib.NODROP,
            ExplosionNT.ExAttrib.LAVA_R,
            ExplosionNT.ExAttrib.NOSOUND,
            ExplosionNT.ExAttrib.ALLMOD,
            ExplosionNT.ExAttrib.NOHURT);

    private int volcanoTimer;

    public LegacyVolcanoCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEGACY_VOLCANO_CORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LegacyVolcanoCoreBlockEntity blockEntity) {
        blockEntity.tickServer(level, pos, state);
    }

    private void tickServer(Level level, BlockPos pos, BlockState state) {
        volcanoTimer++;
        if (volcanoTimer % 10 == 0) {
            if (hasVerticalChannel(state)) {
                blastMagmaChannel(level, pos, state);
                raiseMagma(level, pos, state);
            }

            double magmaChamber = magmaChamberSize(state);
            if (magmaChamber > 0.0D) {
                blastMagmaChamber(level, pos, state, magmaChamber);
            }

            if (isSmoldering(state)) {
                meltSurface(level, pos, state, 50, 50.0D, 10.0D);
            }
            if (!isSmoldering(state)) {
                spawnBlobs(level, pos, state);
                ParticleUtil.spawnVanillaExtVolcano(level, pos.getX() + 0.5D, pos.getY() + 10.0D, pos.getZ() + 0.5D);
            }

            surroundLava(level, pos, state);
        }

        if (volcanoTimer >= getUpdateRate(state)) {
            volcanoTimer = 0;
            if (shouldGrow(level, pos, state)) {
                level.setBlock(pos.above(), state, 3);
                level.setBlock(pos, lavaState(state), 3);
                setChanged();
                return;
            }
            if (isExtinguishing(state)) {
                level.setBlock(pos, lavaState(state), 3);
                setChanged();
                return;
            }
        }
        setChanged();
    }

    private void blastMagmaChannel(Level level, BlockPos pos, BlockState state) {
        new ExplosionNT(level, null, pos.getX() + 0.5D, pos.getY() + level.random.nextInt(15) + 1.5D,
                pos.getZ() + 0.5D, 7.0F)
                .addAllAttrib(explosionAttributes(state))
                .explode();

        int minY = level.getMinBuildHeight();
        int span = Math.max(1, pos.getY() - minY + 1);
        double y = minY + level.random.nextInt(span);
        new ExplosionNT(level, null, pos.getX() + 0.5D + level.random.nextGaussian() * 3.0D, y,
                pos.getZ() + 0.5D + level.random.nextGaussian() * 3.0D, 10.0F)
                .addAllAttrib(explosionAttributes(state))
                .explode();
    }

    private void blastMagmaChamber(Level level, BlockPos pos, BlockState state, double size) {
        for (int i = 0; i < 2; i++) {
            double dist = size / (double) (i + 1);
            new ExplosionNT(level, null,
                    pos.getX() + 0.5D + level.random.nextGaussian() * dist,
                    pos.getY() + 0.5D + level.random.nextGaussian() * dist,
                    pos.getZ() + 0.5D + level.random.nextGaussian() * dist,
                    7.0F)
                    .addAllAttrib(explosionAttributes(state))
                    .explode();
        }
    }

    private void meltSurface(Level level, BlockPos pos, BlockState state, int count, double radius, double depth) {
        for (int i = 0; i < count; i++) {
            int x = (int) Math.floor(pos.getX() + level.random.nextGaussian() * radius);
            int z = (int) Math.floor(pos.getZ() + level.random.nextGaussian() * radius);
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) + 1;
            int y = surfaceY - (int) Math.floor(Math.abs(level.random.nextGaussian() * depth));
            BlockPos target = new BlockPos(x, y, z);
            BlockState targetState = level.getBlockState(target);

            if (!targetState.isAir() && targetState.getExplosionResistance(level, target, null) < Blocks.OBSIDIAN.getExplosionResistance()) {
                level.setBlock(target, targetState.isCollisionShapeFullBlock(level, target) ? lavaState(state) : Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private void raiseMagma(Level level, BlockPos pos, BlockState state) {
        int x = pos.getX() - 10 + level.random.nextInt(21);
        int y = pos.getY() + level.random.nextInt(11);
        int z = pos.getZ() - 10 + level.random.nextInt(21);
        BlockPos target = new BlockPos(x, y, z);
        if (level.getBlockState(target).isAir() && level.getBlockState(target.below()).is(lavaBlock(state))) {
            level.setBlock(target, lavaState(state), 3);
        }
    }

    private void surroundLava(Level level, BlockPos pos, BlockState state) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        level.setBlock(pos.offset(dx, dy, dz), lavaState(state), 3);
                    }
                }
            }
        }
    }

    private void spawnBlobs(Level level, BlockPos pos, BlockState state) {
        for (int i = 0; i < 3; i++) {
            ShrapnelEntity shrapnel = new ShrapnelEntity(level);
            shrapnel.setPos(pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D);
            shrapnel.setDeltaMovement(level.random.nextGaussian() * 0.2D, 1.0D + level.random.nextDouble(), level.random.nextGaussian() * 0.2D);
            if (isRadioactive(state)) {
                shrapnel.setRadVolcano();
            } else {
                shrapnel.setVolcano();
            }
            level.addFreshEntity(shrapnel);
        }
    }

    private static boolean shouldGrow(Level level, BlockPos pos, BlockState state) {
        return isGrowing(state) && pos.getY() < Math.min(200, level.getMaxBuildHeight() - 1);
    }

    private static boolean isGrowing(BlockState state) {
        return LegacyVolcanoCoreBlock.isGrowing(mode(state));
    }

    private static boolean isExtinguishing(BlockState state) {
        return LegacyVolcanoCoreBlock.isExtinguishing(mode(state));
    }

    private static boolean isSmoldering(BlockState state) {
        return mode(state) == LegacyVolcanoCoreBlock.META_SMOLDERING;
    }

    private static boolean hasVerticalChannel(BlockState state) {
        return !isSmoldering(state);
    }

    private static double magmaChamberSize(BlockState state) {
        return isSmoldering(state) ? 15.0D : 0.0D;
    }

    private static int getUpdateRate(BlockState state) {
        return switch (mode(state)) {
            case LegacyVolcanoCoreBlock.META_STATIC_EXTINGUISHING -> 60 * 60 * 20;
            case LegacyVolcanoCoreBlock.META_GROWING_ACTIVE, LegacyVolcanoCoreBlock.META_GROWING_EXTINGUISHING -> 60 * 60 * 20 / 250;
            default -> 10;
        };
    }

    private static int mode(BlockState state) {
        return state.hasProperty(LegacyVolcanoCoreBlock.MODE) ? state.getValue(LegacyVolcanoCoreBlock.MODE) : 0;
    }

    private static boolean isRadioactive(BlockState state) {
        return state.is(ModBlocks.VOLCANO_RAD_CORE.get());
    }

    private static Block lavaBlock(BlockState state) {
        return isRadioactive(state) ? ModBlocks.RAD_LAVA_BLOCK.get() : ModBlocks.VOLCANIC_LAVA_BLOCK.get();
    }

    private static BlockState lavaState(BlockState state) {
        return lavaBlock(state).defaultBlockState();
    }

    private static List<ExplosionNT.ExAttrib> explosionAttributes(BlockState state) {
        return isRadioactive(state) ? RAD_VOLCANO_EXPLOSION : VOLCANO_EXPLOSION;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_TIMER, volcanoTimer);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        volcanoTimer = tag.getInt(TAG_TIMER);
    }
}
