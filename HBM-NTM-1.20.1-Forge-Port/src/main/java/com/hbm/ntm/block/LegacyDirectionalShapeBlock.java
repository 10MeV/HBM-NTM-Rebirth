package com.hbm.ntm.block;

import java.util.EnumMap;
import java.util.Map;

import com.hbm.ntm.blockentity.LegacyLightBlockEntity;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class LegacyDirectionalShapeBlock extends BaseEntityBlock {
    public static final DirectionProperty FACE = DirectionProperty.create("face");
    public static final BooleanProperty TOP_BOTTOM_ROTATED = BooleanProperty.create("top_bottom_rotated");

    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);
    private final Kind kind;
    private final boolean hasCollision;

    public LegacyDirectionalShapeBlock(BlockBehaviour.Properties properties, Kind kind) {
        this(properties, kind, true);
    }

    public LegacyDirectionalShapeBlock(BlockBehaviour.Properties properties, Kind kind, boolean hasCollision) {
        super(properties);
        this.kind = kind;
        this.hasCollision = hasCollision;
        for (Direction direction : Direction.values()) {
            shapes.put(direction, kind == Kind.FLOODLIGHT ? Shapes.block() : shapeFor(direction, kind.halfX, kind.halfY, kind.halfZ));
        }
        registerDefaultState(stateDefinition.any().setValue(FACE, Direction.UP).setValue(TOP_BOTTOM_ROTATED, false));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACE, context.getClickedFace());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (kind != Kind.FLOODLIGHT || placer == null) {
            return;
        }

        BlockState current = state;
        Direction face = state.getValue(FACE);
        if (face == Direction.DOWN || face == Direction.UP) {
            int quadrant = legacyYawQuadrant(placer.getYRot());
            if (quadrant == 0 || quadrant == 2) {
                current = current.setValue(TOP_BOTTOM_ROTATED, true);
                level.setBlock(pos, current, Block.UPDATE_CLIENTS);
            }
        }

        if (level.getBlockEntity(pos) instanceof LegacyLightBlockEntity blockEntity) {
            blockEntity.setRotationFromPlacement(placer.getXRot(), placer.getYRot(), face);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACE, rotation.rotate(state.getValue(FACE)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACE, mirror.mirror(state.getValue(FACE)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACE, TOP_BOTTOM_ROTATED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACE));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return hasCollision ? getShape(state, level, pos, context) : Shapes.empty();
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LegacyLightBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.LEGACY_LIGHT.get(), LegacyLightBlockEntity::tick);
    }

    private static VoxelShape shapeFor(Direction direction, double halfX, double halfY, double halfZ) {
        double[] bounds = swizzleBounds(direction, halfX, halfY, halfZ);
        double centerX = 0.5D - direction.getStepX() * (0.5D - bounds[0]);
        double centerY = 0.5D - direction.getStepY() * (0.5D - bounds[1]);
        double centerZ = 0.5D - direction.getStepZ() * (0.5D - bounds[2]);
        return Shapes.create(
                centerX - bounds[0],
                centerY - bounds[1],
                centerZ - bounds[2],
                centerX + bounds[0],
                centerY + bounds[1],
                centerZ + bounds[2]);
    }

    private static double[] swizzleBounds(Direction direction, double halfX, double halfY, double halfZ) {
        return switch (direction) {
            case EAST, WEST -> new double[] {halfZ, halfY, halfX};
            case UP, DOWN -> new double[] {halfY, halfZ, halfX};
            default -> new double[] {halfX, halfY, halfZ};
        };
    }

    public static int legacyYawQuadrant(float yaw) {
        return (int) Math.floor(yaw * 4.0F / 360.0F + 0.5D) & 3;
    }

    public enum Kind {
        SPOTLIGHT_INCANDESCENT(0.25D, 0.20D, 0.15D),
        SPOTLIGHT_FLUORO(0.50D, 0.50D, 0.10D),
        SPOTLIGHT_HALOGEN(0.35D, 0.25D, 0.20D),
        FLOODLIGHT(0.50D, 0.50D, 0.50D);

        private final double halfX;
        private final double halfY;
        private final double halfZ;

        Kind(double halfX, double halfY, double halfZ) {
            this.halfX = halfX;
            this.halfY = halfY;
            this.halfZ = halfZ;
        }
    }
}
