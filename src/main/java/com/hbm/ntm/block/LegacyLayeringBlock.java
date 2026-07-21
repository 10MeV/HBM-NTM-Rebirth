package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Forge 1.20.1 state rewrite of the old metadata-based {@code BlockLayering}.  The thin collision box is
 * deliberately one 1.7.10 metadata step shorter than the rendered/selected height.
 */
@SuppressWarnings("deprecation")
public class LegacyLayeringBlock extends Block {
    public static final IntegerProperty LAYERS = IntegerProperty.create("layers", 1, 8);
    private static final VoxelShape[] SHAPES = new VoxelShape[9];
    private static final VoxelShape[] COLLISION_SHAPES = new VoxelShape[9];

    static {
        for (int layers = 1; layers <= 8; layers++) {
            SHAPES[layers] = Block.box(0.0D, 0.0D, 0.0D, 16.0D, layers * 2.0D, 16.0D);
            COLLISION_SHAPES[layers] = layers == 1 ? Shapes.empty()
                    : Block.box(0.0D, 0.0D, 0.0D, 16.0D, (layers - 1) * 2.0D, 16.0D);
        }
    }

    private final Kind kind;

    public LegacyLayeringBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(LAYERS, 1));
    }

    public boolean isFoam() {
        return kind == Kind.FOAM;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(LAYERS)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPES[state.getValue(LAYERS)];
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        if (below.is(Blocks.ICE) || below.is(Blocks.PACKED_ICE)) {
            return false;
        }
        if (below.is(BlockTags.LEAVES)
                || below.getBlock() instanceof RBMKDebrisBlock
                || below.getBlock() instanceof ZirnoxDestroyedBlock) {
            return true;
        }
        if (below.is(this) && below.getValue(LAYERS) == 8) {
            return true;
        }
        return below.isSolidRender(level, belowPos) && below.blocksMotion();
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!state.canSurvive(level, pos)) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return isFoam();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
    }

    public enum Kind implements StringRepresentable {
        FOAM("foam"),
        BORON_SAND("boron_sand");

        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
