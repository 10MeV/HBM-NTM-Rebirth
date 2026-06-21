package com.hbm.ntm.block;

import com.hbm.ntm.particle.ClientParticleBridge;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public abstract class LegacyGasBlock extends Block {
    private final float red;
    private final float green;
    private final float blue;

    public LegacyGasBlock(Properties properties) {
        this(properties, 1.0F, 1.0F, 1.0F);
    }

    public LegacyGasBlock(Properties properties, float red, float green, float blue) {
        super(properties);
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && state.getBlock() != oldState.getBlock()) {
            level.scheduleTick(pos, this, 10);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 10);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (!tryMove(level, pos, firstDirection(level, pos, random))
                && !tryMove(level, pos, secondDirection(level, pos, random))) {
            level.scheduleTick(pos, this, getDelay(level));
        }
    }

    protected abstract Direction firstDirection(ServerLevel level, BlockPos pos, RandomSource random);

    protected Direction secondDirection(ServerLevel level, BlockPos pos, RandomSource random) {
        return firstDirection(level, pos, random);
    }

    protected boolean tryMove(ServerLevel level, BlockPos pos, Direction direction) {
        BlockPos target = pos.relative(direction);
        if (!level.isEmptyBlock(target)) {
            return false;
        }
        level.removeBlock(pos, false);
        level.setBlock(target, defaultBlockState(), Block.UPDATE_ALL);
        level.scheduleTick(target, this, getDelay(level));
        return true;
    }

    protected int getDelay(ServerLevel level) {
        return 2;
    }

    protected Direction randomHorizontal(RandomSource random) {
        return Direction.Plane.HORIZONTAL.getRandomDirection(random);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 10);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, direction);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (ClientParticleBridge.isLocalPlayerWearing(ModItems.ASHGLASSES.get(), EquipmentSlot.HEAD)) {
            ParticleUtil.spawnLegacyColoredGasCloud(level, pos, red, green, blue);
        }
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return true;
    }
}

