package com.hbm.ntm.blockentity;

import api.hbm.block.ICrucibleAcceptor;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FoundryTankBlockEntity extends FoundryBaseBlockEntity {
    private int nextUpdate;

    public FoundryTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOUNDRY_TANK.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryTankBlockEntity tank) {
        if (level.isClientSide) {
            return;
        }
        int oldAmount = tank.amount;
        Object oldType = tank.type;
        tank.tickServer(level);
        if (oldAmount != tank.amount || oldType != tank.type || level.getGameTime() % 20L == 0L) {
            tank.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public int getCapacity() {
        return MaterialShapes.BLOCK.q(4);
    }

    @Override
    public boolean canAcceptPartialFlow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return false;
    }

    @Override
    public MaterialStack flow(Level level, BlockPos pos, Direction side, MaterialStack stack) {
        return stack;
    }

    private void tickServer(Level level) {
        if (type == null && amount != 0) {
            amount = 0;
        }
        nextUpdate--;
        if (nextUpdate <= 0 && amount > 0 && type != null) {
            boolean hasOp = drainDown(level);
            nextUpdate = level.random.nextInt(6) + 5;
            List<Direction> directions = horizontalDirections();
            if (!hasOp) {
                hasOp = flowToAcceptors(level, directions);
            }
            if (!hasOp) {
                equalizeTanks(level, directions);
            }
        }
        cleanupMaterial();
    }

    private boolean drainDown(Level level) {
        BlockEntity be = level.getBlockEntity(worldPosition.below());
        if (be instanceof FoundryTankBlockEntity tank
                && (tank.type == null || tank.type == type) && tank.amount < tank.getCapacity()) {
            tank.type = type;
            int toFill = Math.min(amount, tank.getCapacity() - tank.amount);
            amount -= toFill;
            tank.amount += toFill;
            tank.setChangedAndUpdate();
            return true;
        }
        return false;
    }

    private boolean flowToAcceptors(Level level, List<Direction> directions) {
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
                return true;
            }
        }
        return false;
    }

    private void equalizeTanks(Level level, List<Direction> directions) {
        for (Direction direction : directions) {
            BlockEntity be = level.getBlockEntity(worldPosition.relative(direction));
            if (be instanceof FoundryTankBlockEntity other
                    && (other.type == null || other.type == type || other.amount == 0)) {
                other.type = type;
                if (level.random.nextInt(5) == 0) {
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
