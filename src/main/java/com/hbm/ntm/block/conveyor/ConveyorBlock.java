package com.hbm.ntm.block.conveyor;

import com.hbm.ntm.api.block.Toolable;
import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.ConveyorPathType;
import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.entity.item.MovingItemEntity;
import com.hbm.ntm.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class ConveyorBlock extends Block implements IConveyorBelt, Toolable {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);
    public static final EnumProperty<ConveyorPathType> PATH = EnumProperty.create("path", ConveyorPathType.class);
    public static final IntegerProperty LEGACY_METADATA = IntegerProperty.create("legacy_metadata", 2, 13);
    protected static final VoxelShape LOW_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);

    public ConveyorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PATH, ConveyorPathType.STRAIGHT)
                .setValue(LEGACY_METADATA, Direction.NORTH.get3DDataValue()));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return stateFromLegacyMetadata(ConveyorMath.legacyMetadataForPlacementYaw(context.getRotation()));
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        int metadata = ConveyorMath.legacyMetadataForPlacementYaw(placer.getYRot());
        level.setBlock(pos, stateFromLegacyMetadata(metadata), 2);
    }

    @Override
    public boolean canItemStay(Level level, BlockPos pos, Vec3 itemPos) {
        return true;
    }

    @Override
    public Vec3 getTravelLocation(Level level, BlockPos pos, Vec3 itemPos, double speed) {
        return ConveyorMath.travelLocation(legacyMetadata(level.getBlockState(pos)), pos, itemPos, speed);
    }

    @Override
    public Vec3 getClosestSnappingPosition(Level level, BlockPos pos, Vec3 itemPos) {
        return ConveyorMath.closestSnappingPosition(legacyMetadata(level.getBlockState(pos)), pos, itemPos);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ItemEntity itemEntity && entity.tickCount > 10 && !entity.isRemoved()) {
            MovingItemEntity moving = new MovingItemEntity(level, itemEntity.getItem().copy());
            Vec3 snap = getClosestSnappingPosition(level, pos, entity.position());
            moving.moveTo(snap.x, snap.y, snap.z, 0.0F, 0.0F);
            level.addFreshEntity(moving);
            entity.discard();
        }
    }

    @Override
    public boolean onToolUse(Level level, Player player, BlockPos pos, Direction side, Vec3 hit, ToolType tool) {
        if (tool != ToolType.SCREWDRIVER) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        int metadata = legacyMetadata(state);
        ConveyorPathType path = ConveyorPathType.fromLegacyMetadata(metadata);
        int baseMetadata = ConveyorMath.baseLegacyMetadata(metadata);
        BlockState nextState = nextScrewdriverState(state, metadata, baseMetadata, path, player.isShiftKeyDown());
        level.setBlock(pos, nextState, 3);
        return true;
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).isEmpty()
                && onToolUse(level, player, pos, hit.getDirection(), hit.getLocation(), ToolType.SCREWDRIVER)) {
            return net.minecraft.world.InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return LOW_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return LOW_SHAPE;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    public BlockState stateFromLegacyMetadata(int metadata) {
        int clampedMetadata = Mth.clamp(metadata, 2, 13);
        int baseMetadata = ConveyorMath.baseLegacyMetadata(clampedMetadata);
        Direction facing = ConveyorMath.legacyHorizontalDirection(baseMetadata);
        ConveyorPathType path = ConveyorPathType.fromLegacyMetadata(clampedMetadata);
        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(PATH, path)
                .setValue(LEGACY_METADATA, clampedMetadata);
    }

    public int legacyMetadata(BlockState state) {
        if (state.hasProperty(LEGACY_METADATA)) {
            return state.getValue(LEGACY_METADATA);
        }
        Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.NORTH;
        ConveyorPathType path = state.hasProperty(PATH) ? state.getValue(PATH) : ConveyorPathType.STRAIGHT;
        return facing.get3DDataValue() + path.legacyOffset() * 4;
    }

    protected BlockState nextScrewdriverState(BlockState state, int metadata, int baseMetadata,
            ConveyorPathType path, boolean sneaking) {
        int newMetadata = nextBendableMetadata(metadata, baseMetadata, path, sneaking);
        if (sneaking && path == ConveyorPathType.RIGHT) {
            return ((ConveyorBlock) ModBlocks.CONVEYOR_LIFT.get()).stateFromLegacyMetadata(newMetadata);
        }
        return stateFromLegacyMetadata(newMetadata);
    }

    protected int nextBendableMetadata(int metadata, int baseMetadata, ConveyorPathType path, boolean sneaking) {
        if (!sneaking) {
            Direction rotated = ConveyorMath.legacyHorizontalDirection(baseMetadata).getClockWise();
            return rotated.get3DDataValue() + path.legacyOffset() * 4;
        }
        return path == ConveyorPathType.RIGHT ? baseMetadata : metadata + 4;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PATH, LEGACY_METADATA);
    }
}

