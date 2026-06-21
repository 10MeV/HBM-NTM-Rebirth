package com.hbm.ntm.block;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class LegacyCoriumFiniteBlock extends Block {
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 5);
    public static final int LEGACY_PLACED_META = 4;
    public static final int LEGACY_RBMK_META = 5;
    private static final int QUANTA_PER_BLOCK = 5;
    private static final int TICK_RATE = 30;

    public LegacyCoriumFiniteBlock(Properties properties) {
        super(properties.randomTicks());
        registerDefaultState(stateDefinition.any().setValue(LEVEL, LEGACY_PLACED_META));
    }

    public BlockState legacyState(int meta) {
        return defaultBlockState().setValue(LEVEL, Math.max(0, Math.min(5, meta)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && state.getBlock() != oldState.getBlock()) {
            level.scheduleTick(pos, this, TICK_RATE);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, TICK_RATE);
        }
        return state;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return legacyState(LEGACY_PLACED_META);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        spreadFiniteCorium(state, level, pos);
        solidifySurfaceCorium(level, pos, random);
        if (level.getBlockState(pos).is(this)) {
            level.scheduleTick(pos, this, TICK_RATE);
        }
    }

    private void spreadFiniteCorium(BlockState state, ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(this)) {
            return;
        }

        int remaining = quanta(state);
        BlockPos below = pos.below();
        if (below.getY() >= level.getMinBuildHeight()) {
            int belowQuanta = getQuantaValueBelow(level, below, QUANTA_PER_BLOCK);
            if (belowQuanta >= 0) {
                int amount = remaining + belowQuanta;
                if (amount > QUANTA_PER_BLOCK) {
                    setCorium(level, below, QUANTA_PER_BLOCK);
                    remaining = amount - QUANTA_PER_BLOCK;
                } else {
                    setCorium(level, below, amount);
                    level.removeBlock(pos, false);
                    return;
                }
            }
        }

        if (remaining <= 0) {
            level.removeBlock(pos, false);
            return;
        }

        int lowerThan = remaining - 1;
        int targets = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (getQuantaValueBelow(level, pos.relative(direction), lowerThan) >= 0) {
                targets++;
            }
        }

        if (targets > 0) {
            int each = Math.max(1, (remaining - 1) / targets);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos target = pos.relative(direction);
                int targetQuanta = getQuantaValueBelow(level, target, lowerThan);
                if (targetQuanta < 0) {
                    continue;
                }
                int moved = Math.min(each, remaining);
                setCorium(level, target, targetQuanta + moved);
                remaining -= moved;
                if (remaining <= 0) {
                    break;
                }
            }
        }

        if (remaining <= 0) {
            level.removeBlock(pos, false);
        } else {
            setCorium(level, pos, remaining);
        }
    }

    private int getQuantaValueBelow(ServerLevel level, BlockPos pos, int lowerThan) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this)) {
            int quanta = quanta(state);
            return quanta < lowerThan ? quanta : -1;
        }
        return displaceIfPossible(level, pos, state) ? 0 : -1;
    }

    private void setCorium(ServerLevel level, BlockPos pos, int quanta) {
        int clamped = Math.max(1, Math.min(LEGACY_RBMK_META + 1, quanta));
        level.setBlock(pos, legacyState(clamped - 1), Block.UPDATE_ALL);
        level.scheduleTick(pos, this, TICK_RATE);
    }

    private void solidifySurfaceCorium(ServerLevel level, BlockPos pos, RandomSource random) {
        BlockState current = level.getBlockState(pos);
        if (!current.is(this) || random.nextInt(10) != 0 || level.getBlockState(pos.below()).is(this)) {
            return;
        }
        BlockState solid = random.nextInt(3) == 0
                ? ModBlocks.legacyBlock("block_corium").get().defaultBlockState()
                : ModBlocks.legacyBlock("block_corium_cobble").get().defaultBlockState();
        level.setBlock(pos, solid, Block.UPDATE_ALL);
    }

    public boolean canDisplace(BlockGetter level, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return true;
        }
        FluidState fluid = state.getFluidState();
        if (!fluid.isEmpty()) {
            return true;
        }
        float resistance = (float) (Math.sqrt(Math.max(0.0F, state.getExplosionResistance(level, pos, null))) * 3.0D);
        return resistance < 1.0F || RandomSource.create().nextInt((int) resistance) == 0;
    }

    public boolean displaceIfPossible(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getFluidState().isEmpty() && canDisplace(level, pos, state);
    }

    private static int quanta(BlockState state) {
        return state.getValue(LEVEL) + 1;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(0.25D, 0.05D, 0.25D));
        entity.setSecondsOnFire(3);
        EntityDamageUtil.attackEntityFromNt(entity, ModDamageSources.radiation(level), 2.0F, true);
        if (entity instanceof LivingEntity living) {
            RadiationUtil.contaminate(living, HazardType.RADIATION, RadiationUtil.ContaminationType.CREATIVE, 1.0F);
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return true;
    }
}
