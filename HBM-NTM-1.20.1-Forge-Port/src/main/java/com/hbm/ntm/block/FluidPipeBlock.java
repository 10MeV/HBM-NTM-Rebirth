package com.hbm.ntm.block;

import com.hbm.ntm.api.fluid.IFluidIdentifierItem;
import com.hbm.ntm.blockentity.FluidPipeBlockEntity;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.item.FluidPipeBlockItem;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.HbmServerKeybinds;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class FluidPipeBlock extends HbmFluidNodeBlock {
    private static final VoxelShape CORE = box(5.0D, 5.0D, 5.0D, 11.0D, 11.0D, 11.0D);
    private static final VoxelShape NORTH_ARM = box(5.0D, 5.0D, 0.0D, 11.0D, 11.0D, 5.0D);
    private static final VoxelShape EAST_ARM = box(11.0D, 5.0D, 5.0D, 16.0D, 11.0D, 11.0D);
    private static final VoxelShape SOUTH_ARM = box(5.0D, 5.0D, 11.0D, 11.0D, 11.0D, 16.0D);
    private static final VoxelShape WEST_ARM = box(0.0D, 5.0D, 5.0D, 5.0D, 11.0D, 11.0D);
    private static final VoxelShape UP_ARM = box(5.0D, 11.0D, 5.0D, 11.0D, 16.0D, 11.0D);
    private static final VoxelShape DOWN_ARM = box(5.0D, 0.0D, 5.0D, 11.0D, 5.0D, 11.0D);

    public FluidPipeBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidPipeBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (!(held.getItem() instanceof IFluidIdentifierItem identifier)
                || !(level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        FluidType target = identifier.getIdentifiedFluid(level, pos, held);
        boolean toolAlt = player instanceof ServerPlayer serverPlayer
                && HbmServerKeybinds.isPressed(serverPlayer, HbmKeybind.TOOL_ALT);
        boolean toolCtrl = player instanceof ServerPlayer serverPlayer
                && HbmServerKeybinds.isPressed(serverPlayer, HbmKeybind.TOOL_CTRL);

        if (toolAlt && target != pipe.getFluidType()
                && identifier.setIdentifiedFluid(held, pipe.getFluidType(), true)) {
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                    0.25F, 0.75F);
            return InteractionResult.CONSUME;
        }

        if (toolCtrl || player.isShiftKeyDown()) {
            changeConnectedPipeTypes(level, pos, pipe.getFluidType(), target, 64);
        } else {
            pipe.setFluidType(target);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe
                && asItem() instanceof FluidPipeBlockItem item) {
            return FluidPipeBlockItem.createStack(item, pipe.getFluidType());
        }
        return super.getCloneItemStack(level, pos, state);
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
        VoxelShape shape = CORE;
        if (state.getValue(NORTH)) {
            shape = Shapes.or(shape, NORTH_ARM);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.or(shape, EAST_ARM);
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.or(shape, SOUTH_ARM);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.or(shape, WEST_ARM);
        }
        if (state.getValue(UP)) {
            shape = Shapes.or(shape, UP_ARM);
        }
        if (state.getValue(DOWN)) {
            shape = Shapes.or(shape, DOWN_ARM);
        }
        return shape;
    }

    private static int changeConnectedPipeTypes(Level level, BlockPos start, FluidType previous, FluidType target,
            int maxDistance) {
        if (previous == target) {
            return 0;
        }

        Queue<PipeVisit> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(new PipeVisit(start.immutable(), 0));
        int changed = 0;

        while (!queue.isEmpty()) {
            PipeVisit visit = queue.remove();
            if (!visited.add(visit.pos())) {
                continue;
            }
            if (!(level.getBlockEntity(visit.pos()) instanceof FluidPipeBlockEntity pipe)
                    || pipe.getFluidType() != previous) {
                continue;
            }

            pipe.setFluidType(target);
            changed++;

            if (visit.distance() >= maxDistance) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                queue.add(new PipeVisit(visit.pos().relative(direction), visit.distance() + 1));
            }
        }
        return changed;
    }

    private record PipeVisit(BlockPos pos, int distance) {
    }
}

