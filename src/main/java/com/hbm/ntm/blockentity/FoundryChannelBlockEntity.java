package com.hbm.ntm.blockentity;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.uninos.networkproviders.FoundryNode;
import com.hbm.ntm.uninos.networkproviders.FoundryNodespace;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FoundryChannelBlockEntity extends FoundryBaseBlockEntity {
    private static final String TAG_FLOW = "flow";
    private int nextUpdate;
    private int lastFlow;

    public FoundryChannelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOUNDRY_CHANNEL.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryChannelBlockEntity channel) {
        if (level.isClientSide) {
            return;
        }
        channel.ensureNode(level);
        int oldAmount = channel.amount;
        Object oldType = channel.type;
        channel.tickServer(level);
        if (oldAmount != channel.amount || oldType != channel.type || level.getGameTime() % 20L == 0L) {
            channel.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            ensureNode(level);
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide) {
            FoundryNodespace.destroyNode(level, worldPosition);
        }
        super.setRemoved();
    }

    @Override
    public int getCapacity() {
        return MaterialShapes.INGOT.q(2);
    }

    @Override
    public boolean canAcceptPartialPour(Level level, BlockPos pos, net.minecraft.world.phys.Vec3 hit,
            Direction side, MaterialStack stack) {
        return networkAccepts(level, stack) && super.canAcceptPartialPour(level, pos, hit, side, stack);
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        ensureNode(level);
        return super.flow(level, pos, side, stack);
    }

    @Override
    public MaterialStack pour(Level level, BlockPos pos, net.minecraft.world.phys.Vec3 hit,
            Direction side, MaterialStack stack) {
        ensureNode(level);
        return super.pour(level, pos, hit, side, stack);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByte(TAG_FLOW, (byte) lastFlow);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lastFlow = tag.getByte(TAG_FLOW);
    }

    private void tickServer(Level level) {
        if (type == null && amount != 0) {
            amount = 0;
        }
        nextUpdate--;
        if (nextUpdate <= 0 && amount > 0 && type != null) {
            boolean hasOp = false;
            nextUpdate = 5;
            List<Direction> directions = horizontalDirections();
            if (lastFlow > 0) {
                Direction last = Direction.from3DDataValue(lastFlow);
                directions.remove(last);
                directions.add(last);
            }
            for (Direction direction : directions) {
                BlockPos targetPos = worldPosition.relative(direction);
                BlockState targetState = level.getBlockState(targetPos);
                if (targetState.is(ModBlocks.FOUNDRY_CHANNEL.get())) {
                    continue;
                }
                ICrucibleAcceptor acceptor = acceptorAt(level, targetPos);
                if (acceptor != null && acceptor.canAcceptPartialFlow(level, targetPos, direction.getOpposite(),
                        new MaterialStack(type, amount))) {
                    MaterialStack left = acceptor.flow(level, targetPos, direction.getOpposite(),
                            new MaterialStack(type, amount));
                    if (left == null) {
                        type = null;
                        amount = 0;
                    } else {
                        amount = left.amount;
                    }
                    hasOp = true;
                    break;
                }
            }
            if (!hasOp) {
                equalizeChannels(level, directions);
            }
        }
        if (amount == 0) {
            type = null;
            lastFlow = 0;
            nextUpdate = 5;
        }
    }

    private void equalizeChannels(Level level, List<Direction> directions) {
        for (Direction direction : directions) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(direction));
            if (be instanceof FoundryChannelBlockEntity other
                    && (other.type == null || other.type == type || other.amount == 0)) {
                other.type = type;
                other.lastFlow = direction.getOpposite().get3DDataValue();
                if (level.random.nextInt(5) == 0 || amount == 1) {
                    int buffer = amount;
                    amount = other.amount;
                    other.amount = buffer;
                } else {
                    int diff = amount - other.amount;
                    if (diff > 0) {
                        diff /= 2;
                        amount -= diff;
                        other.amount += diff;
                    }
                }
                other.setChangedAndUpdate();
            }
        }
    }

    private boolean networkAccepts(Level level, MaterialStack stack) {
        if (stack == null || stack.material == null) {
            return false;
        }
        for (BlockPos pos : connectedChannelPositions(level)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FoundryChannelBlockEntity channel && channel.type != null
                    && channel.type != stack.material) {
                return false;
            }
        }
        return true;
    }

    private Set<BlockPos> connectedChannelPositions(Level level) {
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(worldPosition);
        visited.add(worldPosition);
        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(direction);
                if (!visited.contains(next) && level.getBlockState(next).is(ModBlocks.FOUNDRY_CHANNEL.get())) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return visited;
    }

    private void ensureNode(Level level) {
        if (FoundryNodespace.getNode(level, worldPosition) == null) {
            FoundryNodespace.createNode(level, new FoundryNode(worldPosition,
                    EnumSet.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)));
        }
    }

    private static ICrucibleAcceptor acceptorAt(Level level, BlockPos pos) {
        Block block = level.getBlockState(pos).getBlock();
        if (block instanceof ICrucibleAcceptor acceptor) {
            return acceptor;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof ICrucibleAcceptor acceptor ? acceptor : null;
    }

    private static List<Direction> horizontalDirections() {
        List<Direction> directions = new ArrayList<>(List.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        Collections.shuffle(directions);
        return directions;
    }
}
