package com.hbm.ntm.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LegacyGrateBlock extends Block {
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", 0, 9);

    private static final VoxelShape[] SHAPES = new VoxelShape[10];

    static {
        for (int height = 0; height < SHAPES.length; height++) {
            double y = height == 9 ? -0.125D : height * 0.125D;
            SHAPES[height] = Shapes.box(0.0D, y, 0.0D, 1.0D, y + 0.125D, 1.0D);
        }
    }

    private final boolean wide;

    public LegacyGrateBlock(BlockBehaviour.Properties properties, boolean wide) {
        super(properties);
        this.wide = wide;
        registerDefaultState(stateDefinition.any().setValue(HEIGHT, 0));
    }

    public boolean wide() {
        return wide;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        int height;
        Direction face = context.getClickedFace();
        if (face == Direction.DOWN) {
            height = 7;
        } else if (face == Direction.UP) {
            height = 0;
        } else {
            height = Mth.clamp((int) Math.floor((context.getClickLocation().y - context.getClickedPos().getY()) * 8.0D),
                    0, 7);
        }

        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            if (height == 0 && canHangBelow(context)) {
                height = 9;
            } else if (height == 7 && canFitBelowAboveBlock(context)) {
                height = 8;
            }
        }
        return defaultBlockState().setValue(HEIGHT, height);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (wide && context instanceof EntityCollisionContext entityContext
                && (entityContext.getEntity() instanceof ItemEntity
                        || entityContext.getEntity() instanceof ExperienceOrb)) {
            return Shapes.empty();
        }
        return shapeFor(state);
    }

    public boolean isFaceSturdy(BlockState state, BlockGetter level, BlockPos pos, Direction direction,
            SupportType supportType) {
        int height = state.getValue(HEIGHT);
        return (direction == Direction.UP && height == 7) || (direction == Direction.DOWN && height == 0);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
            BlockPos pos, BlockPos neighborPos) {
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos neighborPos,
            boolean moving) {
        if (!level.isClientSide && !canSurviveExtended(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        super.neighborChanged(state, level, pos, block, neighborPos, moving);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HEIGHT);
    }

    private static VoxelShape shapeFor(BlockState state) {
        int height = state.hasProperty(HEIGHT) ? state.getValue(HEIGHT) : 0;
        return SHAPES[Mth.clamp(height, 0, SHAPES.length - 1)];
    }

    private static boolean canHangBelow(BlockPlaceContext context) {
        return canHangBelow(context.getLevel(), context.getClickedPos().below());
    }

    private static boolean canFitBelowAboveBlock(BlockPlaceContext context) {
        return canFitBelow(context.getLevel(), context.getClickedPos().above());
    }

    private static boolean canHangBelow(BlockGetter level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        return shape.isEmpty() || shape.bounds().maxY < 0.95D;
    }

    private static boolean canFitBelow(BlockGetter level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        return shape.isEmpty() || shape.bounds().minY > 0.05D;
    }

    private static boolean canSurviveExtended(BlockState state, BlockGetter level, BlockPos pos) {
        int height = state.getValue(HEIGHT);
        if (height == 9) {
            return canHangBelow(level, pos.below());
        }
        if (height == 8) {
            return canFitBelow(level, pos.above());
        }
        return true;
    }
}
