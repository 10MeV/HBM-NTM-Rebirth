package com.hbm.ntm.entity.item;

import com.hbm.ntm.api.conveyor.ConveyorMath;
import com.hbm.ntm.api.conveyor.IConveyorBelt;
import com.hbm.ntm.api.conveyor.IEnterableBlock;
import com.hbm.ntm.util.HbmModelRenderDistances;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

public abstract class MovingConveyorObjectEntity extends Entity {
    // EntityMovingConveyorObject used its own short client interpolation window.
    private int turnProgress;
    private double syncPosX;
    private double syncPosY;
    private double syncPosZ;
    private double velocityX;
    private double velocityY;
    private double velocityZ;

    protected MovingConveyorObjectEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return HbmModelRenderDistances.shouldRenderAtSqrDistance(distance);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void tick() {
        if (level().isClientSide) {
            applyClientPositionInterpolation();
        }
        super.tick();

        if (level().isClientSide) {
            return;
        }

        if (tickCount <= 5) {
            return;
        }

        BlockPos currentPos = blockPosition();
        IConveyorBelt belt = ConveyorMath.conveyorAt(level(), currentPos);
        boolean isOnConveyor = belt != null && belt.canItemStay(level(), currentPos, position());

        if (!isOnConveyor) {
            if (onLeaveConveyor()) {
                return;
            }
        } else {
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

    /**
     * 1.20.1 replacement for EntityMovingConveyorObject#setVelocity.
     */
    @Override
    public void lerpMotion(double x, double y, double z) {
        velocityX = x;
        velocityY = y;
        velocityZ = z;
        setDeltaMovement(x, y, z);
    }

    /**
     * Preserves the legacy extra two-tick smoothing window for moving belt objects.
     */
    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean teleport) {
        syncPosX = x;
        syncPosY = y;
        syncPosZ = z;
        turnProgress = steps + 2;
        setDeltaMovement(velocityX, velocityY, velocityZ);
    }

    private void applyClientPositionInterpolation() {
        if (turnProgress > 0) {
            setPos(getX() + (syncPosX - getX()) / turnProgress,
                    getY() + (syncPosY - getY()) / turnProgress,
                    getZ() + (syncPosZ - getZ()) / turnProgress);
            --turnProgress;
        } else {
            setPos(getX(), getY(), getZ());
        }
    }

    private void tryEnterBlock(BlockPos lastPos, BlockPos newPos) {
        BlockState newState = level().getBlockState(newPos);

        IEnterableBlock enterable = ConveyorMath.enterableAt(level(), newPos);
        if (enterable != null) {
            Direction side = ConveyorMath.entryDirection(lastPos, newPos);
            // A diagonal or multi-block transition was ForgeDirection.UNKNOWN in 1.7.10.
            // Null preserves that sentinel where modern Direction has no UNKNOWN value.
            enterBlock(enterable, newPos, side);
            return;
        }

        if (!newState.blocksMotion()) {
            BlockPos below = newPos.below();
            IEnterableBlock belowEnterable = ConveyorMath.enterableAt(level(), below);
            if (belowEnterable != null) {
                enterBlockFalling(belowEnterable, newPos);
            }
        }
    }

}
