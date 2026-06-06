package com.hbm.ntm.entity.item;

import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public abstract class MovingConveyorObjectEntity extends Entity {
    protected MovingConveyorObjectEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            return;
        }

        if (tickCount <= 5) {
            return;
        }

        BlockPos currentPos = blockPosition();
        BlockState currentState = level().getBlockState(currentPos);
        Block currentBlock = currentState.getBlock();
        boolean isOnConveyor = currentBlock instanceof IConveyorBelt belt
                && belt.canItemStay(level(), currentPos, position());

        if (!isOnConveyor) {
            if (onLeaveConveyor()) {
                return;
            }
        } else {
            IConveyorBelt belt = (IConveyorBelt) currentBlock;
            Vec3 target = belt.getTravelLocation(level(), currentPos, position(), getMoveSpeed());
            setDeltaMovement(target.subtract(position()));
        }

        BlockPos lastPos = blockPosition();
        move(MoverType.SELF, getDeltaMovement());
        BlockPos newPos = blockPosition();

        if (!lastPos.equals(newPos)) {
            tryEnterBlock(lastPos, newPos);
        }
    }

    public abstract void enterBlock(IEnterableBlock enterable, BlockPos pos, Direction side);

    public void enterBlockFalling(IEnterableBlock enterable, BlockPos pos) {
        enterBlock(enterable, pos.below(), Direction.UP);
    }

    public abstract boolean onLeaveConveyor();

    public double getMoveSpeed() {
        return ConveyorMath.baseSpeed();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void tryEnterBlock(BlockPos lastPos, BlockPos newPos) {
        BlockState newState = level().getBlockState(newPos);
        Block newBlock = newState.getBlock();

        if (newBlock instanceof IEnterableBlock enterable) {
            Direction side = ConveyorMath.entryDirection(lastPos, newPos);
            if (side != null) {
                enterBlock(enterable, newPos, side);
            }
            return;
        }

        if (newState.getCollisionShape(level(), newPos).isEmpty()) {
            BlockPos below = newPos.below();
            Block belowBlock = level().getBlockState(below).getBlock();
            if (belowBlock instanceof IEnterableBlock enterable) {
                enterBlockFalling(enterable, newPos);
            }
        }
    }

}
