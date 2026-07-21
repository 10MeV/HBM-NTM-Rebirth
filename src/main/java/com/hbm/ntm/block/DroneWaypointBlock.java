package com.hbm.ntm.block;

import com.hbm.ntm.blockentity.DroneRequestWaypointBlockEntity;
import com.hbm.ntm.blockentity.DroneWaypointBlockEntity;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayBlockProvider;
import com.hbm.ntm.item.DroneLinkerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


/** Shared mountable legacy waypoint block; request variants deliberately have no height click interaction. */
public class DroneWaypointBlock extends Block implements EntityBlock, LegacyLookOverlayBlockProvider {
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    private final boolean requestNetwork;

    public DroneWaypointBlock(Properties properties, boolean requestNetwork) {
        super(properties);
        this.requestNetwork = requestNetwork;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return requestNetwork ? new DroneRequestWaypointBlockEntity(pos, state) : new DroneWaypointBlockEntity(pos, state);
    }

    @Override public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return (tickLevel, tickPos, tickState, entity) -> {
            if (entity instanceof DroneWaypointBlockEntity waypoint) DroneWaypointBlockEntity.tick(tickLevel, tickPos, tickState, waypoint);
            if (entity instanceof DroneRequestWaypointBlockEntity waypoint) DroneRequestWaypointBlockEntity.tick(tickLevel, tickPos, tickState, waypoint);
        };
    }

    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        BlockState support = level.getBlockState(supportPos);
        // DroneWaypoint also accepted legacy normal-render blocks whose mounted face was
        // not explicitly marked solid; retain that placement/survival alternative.
        return support.isFaceSturdy(level, supportPos, facing) || support.isSolidRender(level, supportPos);
    }
    @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,
            @Nullable net.minecraft.world.level.BlockGetter level, java.util.List<net.minecraft.network.chat.Component> tooltip,
            net.minecraft.world.item.TooltipFlag flag) { if (!requestNetwork) LegacyStandardInfoTooltip.append(tooltip, "drone_waypoint"); }

    @Override public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!canSurvive(state, level, pos)) level.destroyBlock(pos, true);
    }

    @Override public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.getItemInHand(hand).getItem() instanceof DroneLinkerItem) return InteractionResult.PASS;
        if (!requestNetwork && level.getBlockEntity(pos) instanceof DroneWaypointBlockEntity waypoint) {
            if (!level.isClientSide) waypoint.addHeight(player.isShiftKeyDown() ? -1 : 1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public @Nullable LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos, BlockState viewedState) {
        // DroneWaypointRequest did not implement ILookOverlay in 1.7.10.
        if (requestNetwork || !(level.getBlockEntity(viewedPos) instanceof DroneWaypointBlockEntity waypoint)) {
            return null;
        }
        List<Component> lines = new ArrayList<>();
        // DroneWaypoint#printHook always rendered this legacy hard-coded status line.
        lines.add(Component.literal("Waypoint distance: " + waypoint.height()));
        BlockPos next = waypoint.nextTarget();
        if (next != null) {
            lines.add(Component.literal("Next waypoint: " + next.getX() + " / " + next.getY() + " / " + next.getZ()));
        }
        return LegacyLookOverlay.forBlockState(viewedState, lines);
    }

    @Override public VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case DOWN -> Block.box(6, 6, 6, 10, 16, 10);
            case UP -> Block.box(6, 0, 6, 10, 16, 10);
            case NORTH -> Block.box(6, 6, 6, 10, 10, 16);
            case SOUTH -> Block.box(6, 6, 0, 10, 10, 10);
            case WEST -> Block.box(6, 6, 6, 16, 10, 10);
            case EAST -> Block.box(0, 6, 6, 10, 10, 10);
        };
    }

    @Override public VoxelShape getCollisionShape(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, CollisionContext context) { return Shapes.empty(); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
}
