package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.RedCableBlockEntity;
import com.hbm.ntm.blockentity.HbmEnergyNodeBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class RedCableBlock extends HbmEnergyNodeBlock {
    public static final EnumProperty<CenterVisual> CENTER = EnumProperty.create("center", CenterVisual.class);
    private static final VoxelShape CORE = box(5.5D, 5.5D, 5.5D, 10.5D, 10.5D, 10.5D);
    private static final VoxelShape NORTH_ARM = box(5.5D, 5.5D, 0.0D, 10.5D, 10.5D, 5.5D);
    private static final VoxelShape EAST_ARM = box(10.5D, 5.5D, 5.5D, 16.0D, 10.5D, 10.5D);
    private static final VoxelShape SOUTH_ARM = box(5.5D, 5.5D, 10.5D, 10.5D, 10.5D, 16.0D);
    private static final VoxelShape WEST_ARM = box(0.0D, 5.5D, 5.5D, 5.5D, 10.5D, 10.5D);
    private static final VoxelShape UP_ARM = box(5.5D, 10.5D, 5.5D, 10.5D, 16.0D, 10.5D);
    private static final VoxelShape DOWN_ARM = box(5.5D, 0.0D, 5.5D, 10.5D, 5.5D, 10.5D);
    private static final VoxelShape[] SHAPES_BY_MASK = buildShapesByMask();

    public RedCableBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CENTER, CenterVisual.JUNCTION));
    }

    public boolean usesBlockEntityRenderer(BlockState state) {
        return LegacyMachineRenderShapes.renderChunkBakedStaticsInBer();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RedCableBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide || type != ModBlockEntities.RED_CABLE.get()) {
            return null;
        }
        return (tickLevel, tickPos, tickState, blockEntity) ->
                HbmEnergyNodeBlockEntity.serverTick(tickLevel, tickPos, tickState,
                        (RedCableBlockEntity) blockEntity);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return LegacyMachineRenderShapes.chunkBakedStaticOrEntity();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    private static VoxelShape shapeForState(BlockState state) {
        return SHAPES_BY_MASK[connectionMask(state)];
    }

    @Override
    protected BlockState getConnectionState(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState result = super.getConnectionState(state, level, pos);
        return result.setValue(CENTER, computeCenterVisual(
                result.getValue(NORTH), result.getValue(SOUTH),
                result.getValue(EAST), result.getValue(WEST),
                result.getValue(UP), result.getValue(DOWN)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CENTER);
    }

    private static VoxelShape[] buildShapesByMask() {
        VoxelShape[] shapes = new VoxelShape[64];
        for (int mask = 0; mask < shapes.length; mask++) {
            shapes[mask] = buildShape(mask);
        }
        return shapes;
    }

    private static VoxelShape buildShape(int mask) {
        VoxelShape shape = CORE;
        if ((mask & 1) != 0) {
            shape = Shapes.or(shape, NORTH_ARM);
        }
        if ((mask & 32) != 0) {
            shape = Shapes.or(shape, EAST_ARM);
        }
        if ((mask & 2) != 0) {
            shape = Shapes.or(shape, SOUTH_ARM);
        }
        if ((mask & 16) != 0) {
            shape = Shapes.or(shape, WEST_ARM);
        }
        if ((mask & 8) != 0) {
            shape = Shapes.or(shape, UP_ARM);
        }
        if ((mask & 4) != 0) {
            shape = Shapes.or(shape, DOWN_ARM);
        }
        return shape;
    }

    private static int connectionMask(BlockState state) {
        return (state.getValue(EAST) ? 32 : 0)
                | (state.getValue(WEST) ? 16 : 0)
                | (state.getValue(UP) ? 8 : 0)
                | (state.getValue(DOWN) ? 4 : 0)
                | (state.getValue(SOUTH) ? 2 : 0)
                | (state.getValue(NORTH) ? 1 : 0);
    }

    private static CenterVisual computeCenterVisual(boolean north, boolean south, boolean east, boolean west,
            boolean up, boolean down) {
        if (north && south && !east && !west && !up && !down) {
            return CenterVisual.STRAIGHT_Z;
        }
        if (east && west && !north && !south && !up && !down) {
            return CenterVisual.STRAIGHT_X;
        }
        if (up && down && !north && !south && !east && !west) {
            return CenterVisual.STRAIGHT_Y;
        }
        return CenterVisual.JUNCTION;
    }

    public enum CenterVisual implements StringRepresentable {
        JUNCTION("junction"),
        STRAIGHT_Z("straight_z"),
        STRAIGHT_X("straight_x"),
        STRAIGHT_Y("straight_y");

        private final String serializedName;

        CenterVisual(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }
    }
}

