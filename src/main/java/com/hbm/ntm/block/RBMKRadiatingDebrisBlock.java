package com.hbm.ntm.block;

import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner.DebrisBlockAction;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner.DebrisBlockActionType;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner.DebrisBlockTickPlan;
import com.hbm.ntm.neutron.RBMKDebrisBlockPlanner.DebrisNeighborKind;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

@SuppressWarnings("deprecation")
public class RBMKRadiatingDebrisBlock extends RBMKDebrisBlock {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, RBMKDebrisBlockPlanner.RADIATING_MAX_META);

    public RBMKRadiatingDebrisBlock(Properties properties) {
        super(properties, Kind.RADIATING);
        registerDefaultState(defaultBlockState().setValue(AGE, 0));
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
        DebrisNeighborKind neighborKind = neighborKind(level.getBlockState(pos.relative(direction)));
        DebrisBlockTickPlan plan = RBMKDebrisBlockPlanner.planRadiatingTick(
                pos,
                state.getValue(AGE),
                direction,
                neighborKind,
                random.nextInt(RBMKDebrisBlockPlanner.FLAME_RANDOM_DENOMINATOR) == 0,
                random.nextInt(RBMKDebrisBlockPlanner.MELTDOWN_GAS_RANDOM_DENOMINATOR) == 0,
                random.nextInt(RBMKDebrisBlockPlanner.radiatingDecayChance(neighborKind)) == 0,
                random.nextInt(RBMKDebrisBlockPlanner.RADIATING_TICK_RATE_RANDOM));
        executeActions(level, state, pos, plan.actions(), random);
        if (plan.scheduleNextTick()) {
            schedule(level, pos, plan.nextTickDelay());
        }
    }

    @Override
    protected void executeActions(ServerLevel level, BlockState state, BlockPos origin, List<DebrisBlockAction> actions,
            RandomSource random) {
        for (DebrisBlockAction action : actions) {
            if (action.type() == DebrisBlockActionType.SET_META) {
                level.setBlock(action.pos(), state.setValue(AGE, action.meta()), Block.UPDATE_ALL);
            } else {
                super.executeActions(level, state, origin, List.of(action), random);
            }
        }
    }

    @Override
    protected void spawnRbmkFlame(ServerLevel level, BlockPos pos, RandomSource random) {
        ParticleUtilAccessor.spawn(level,
                pos.getX() + random.nextDouble(),
                pos.getY() + 1.75D,
                pos.getZ() + random.nextDouble());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    private static final class ParticleUtilAccessor {
        private static void spawn(ServerLevel level, double x, double y, double z) {
            com.hbm.ntm.particle.ParticleUtil.spawnRbmkFlame(level, x, y, z,
                    RBMKDebrisBlockPlanner.FLAME_MAX_AGE);
        }
    }
}
