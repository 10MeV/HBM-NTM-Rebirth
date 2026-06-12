package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class LegacyTaintBlock extends Block {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 15);
    private static final VoxelShape COLLISION_SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public LegacyTaintBlock(Properties properties) {
        super(properties.randomTicks());
        registerDefaultState(stateDefinition.any().setValue(LEVEL, 0));
    }

    public static BlockState stateForLevel(int level) {
        net.minecraftforge.registries.RegistryObject<? extends Block> taint =
                com.hbm.ntm.registry.ModBlocks.legacyBlock("taint");
        if (taint == null) {
            throw new IllegalStateException("Missing legacy block hbm_ntm_rebirth:taint");
        }
        return taint.get().defaultBlockState()
                .setValue(LEVEL, Math.max(0, Math.min(15, level)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        int taintLevel = state.getValue(LEVEL);
        if (taintLevel >= 15) {
            return;
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) > 4 || random.nextFloat() > 0.25F) {
                        continue;
                    }
                    cursor.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    if (level.isOutsideBuildHeight(cursor)) {
                        continue;
                    }
                    BlockState target = level.getBlockState(cursor);
                    if (target.isAir() || target.is(Blocks.BEDROCK)) {
                        continue;
                    }
                    int targetLevel = taintLevel + (hasAdjacentAir(level, cursor) ? 1 : 3);
                    if (targetLevel > 15) {
                        continue;
                    }
                    if (target.is(this) && target.getValue(LEVEL) >= targetLevel) {
                        continue;
                    }
                    BlockState taintState = defaultBlockState().setValue(LEVEL, targetLevel);
                    level.setBlock(cursor, taintState, Block.UPDATE_ALL);
                    if (random.nextFloat() < 0.25F && FallingBlock.isFree(level.getBlockState(cursor.below()))) {
                        FallingBlockEntity.fall(level, cursor.immutable(), taintState);
                    }
                }
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(0.6D, 1.0D, 0.6D));
        if (!level.isClientSide && entity instanceof LivingEntity living && level.random.nextInt(50) == 0) {
            int amplifier = 15 - state.getValue(LEVEL);
            living.addEffect(new MobEffectInstance(ModEffects.TAINT.get(), 15 * 20, amplifier));
        }
    }

    private boolean hasAdjacentAir(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).isAir()) {
                return true;
            }
        }
        return false;
    }
}
