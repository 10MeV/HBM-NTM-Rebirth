package com.hbm.ntm.block;

import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationData;
import com.hbm.ntm.radiation.RadiationUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class BalefireBlock extends FireBlock {
    public BalefireBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, net.minecraft.core.Direction.UP)
                || canNeighborBurn(level, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            return;
        }
        if (!canSurvive(state, level, pos)) {
            level.removeBlock(pos, false);
            return;
        }
        int age = state.getValue(AGE);
        if (age < 15) {
            level.scheduleTick(pos, this, 30 + random.nextInt(10));
            level.setBlock(pos, state.setValue(AGE, Math.min(15, age + random.nextInt(3) / 2)), 4);
        }
        if (age < 15) {
            tryCatchFire(level, pos.east(), 500, random, age);
            tryCatchFire(level, pos.west(), 500, random, age);
            tryCatchFire(level, pos.below(), 300, random, age);
            tryCatchFire(level, pos.above(), 300, random, age);
            tryCatchFire(level, pos.north(), 500, random, age);
            tryCatchFire(level, pos.south(), 500, random, age);
            spreadFromNeighbors(level, pos, random, age);
        }
    }

    private void spreadFromNeighbors(ServerLevel level, BlockPos pos, RandomSource random, int age) {
        for (BlockPos spreadPos : BlockPos.betweenClosed(pos.offset(-3, -1, -3), pos.offset(3, 4, 3))) {
            if (spreadPos.equals(pos)) {
                continue;
            }
            int limit = 100;
            if (spreadPos.getY() > pos.getY() + 1) {
                limit += (spreadPos.getY() - (pos.getY() + 1)) * 100;
            }
            BlockState spreadState = level.getBlockState(spreadPos);
            if (spreadState.is(this) && spreadState.getValue(AGE) > age + 1) {
                level.setBlock(spreadPos, defaultBlockState().setValue(AGE, age + 1), 3);
                continue;
            }
            if (!spreadState.isAir()) {
                continue;
            }
            int chance = getNeighborFireChance(level, spreadPos);
            if (chance <= 0) {
                continue;
            }
            int adjustedChance = (chance + 40 + level.getDifficulty().getId() * 7) / (age + 30);
            if (adjustedChance > 0 && random.nextInt(limit) <= adjustedChance) {
                level.setBlock(spreadPos, defaultBlockState().setValue(AGE, age + 1), 3);
            }
        }
    }

    private void tryCatchFire(ServerLevel level, BlockPos pos, int chance, RandomSource random, int age) {
        BlockState state = level.getBlockState(pos);
        int flammability = state.getFlammability(level, pos, net.minecraft.core.Direction.UP);
        if (random.nextInt(chance) < flammability) {
            boolean tnt = state.getBlock() instanceof TntBlock;
            level.setBlock(pos, defaultBlockState().setValue(AGE, Math.min(15, age + 1)), 3);
            if (tnt) {
                TntBlock.explode(level, pos);
            }
        }
    }

    private boolean canNeighborBurn(LevelReader level, BlockPos pos) {
        return canCatchFire(level, pos.east())
                || canCatchFire(level, pos.west())
                || canCatchFire(level, pos.below())
                || canCatchFire(level, pos.above())
                || canCatchFire(level, pos.north())
                || canCatchFire(level, pos.south());
    }

    private int getNeighborFireChance(LevelReader level, BlockPos pos) {
        int chance = 0;
        chance = Math.max(chance, fireChance(level, pos.east()));
        chance = Math.max(chance, fireChance(level, pos.west()));
        chance = Math.max(chance, fireChance(level, pos.below()));
        chance = Math.max(chance, fireChance(level, pos.above()));
        chance = Math.max(chance, fireChance(level, pos.north()));
        chance = Math.max(chance, fireChance(level, pos.south()));
        return chance;
    }

    private boolean canCatchFire(LevelReader level, BlockPos pos) {
        return fireChance(level, pos) > 0;
    }

    private int fireChance(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isFlammable(level, pos, net.minecraft.core.Direction.UP) ? state.getFireSpreadSpeed(level, pos, net.minecraft.core.Direction.UP) : 0;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.setSecondsOnFire(10);
        if (entity instanceof LivingEntity living) {
            RadiationData.setBalefire(living, Math.max(RadiationData.getBalefire(living), 100));
            RadiationUtil.contaminate(living, HazardType.RADIATION, RadiationUtil.ContaminationType.CREATIVE, 5.0F);
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return true;
    }
}
