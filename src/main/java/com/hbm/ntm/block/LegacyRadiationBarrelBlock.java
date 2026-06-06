package com.hbm.ntm.block;

import com.hbm.ntm.radiation.ChunkRadiationManager;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

@SuppressWarnings("deprecation")
public class LegacyRadiationBarrelBlock extends Block {
    private static final VoxelShape SHAPE = box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private final float chunkRadiationPerTick;

    public LegacyRadiationBarrelBlock(Properties properties, float chunkRadiationPerTick) {
        super(properties);
        this.chunkRadiationPerTick = chunkRadiationPerTick;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && state.getBlock() != oldState.getBlock()) {
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        ChunkRadiationManager.incrementRadiation(level, pos, chunkRadiationPerTick);
        level.scheduleTick(pos, this, 20);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        super.onBlockExploded(state, level, pos, explosion);
        if (!level.isClientSide && this == ModBlocks.YELLOW_BARREL.get()) {
            ChunkRadiationManager.incrementRadiation(level, pos, 35.0F);
            for (int i = -5; i <= 5; i++) {
                for (int j = -5; j <= 5; j++) {
                    for (int k = -5; k <= 5; k++) {
                        BlockPos target = pos.offset(i, j, k);
                        if (level.random.nextInt(5) == 0 && level.isEmptyBlock(target)) {
                            level.setBlock(target, ModBlocks.GAS_RADON_DENSE.get().defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        level.addParticle(ModParticleTypes.TOWN_AURA.get(),
                pos.getX() + random.nextFloat() * 0.5F + 0.25F,
                pos.getY() + 1.1F,
                pos.getZ() + random.nextFloat() * 0.5F + 0.25F,
                0.0D, 0.0D, 0.0D);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }
}

