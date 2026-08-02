package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.DroneWaypointBlock;
import com.hbm.ntm.drone.DroneLinkable;
import com.hbm.ntm.entity.item.DeliveryDroneEntity;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/** Legacy patrol waypoint, including its height-adjusted endpoint and debug presentation. */
public class DroneWaypointBlockEntity extends BlockEntity implements DroneLinkable {
    private int height = 5;
    private BlockPos nextTarget;

    public DroneWaypointBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRONE_WAYPOINT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DroneWaypointBlockEntity waypoint) {
        if (level.isClientSide) {
            waypoint.clientTick();
        } else {
            // TileEntityDroneWaypoint checked the height-offset one-block dock volume every
            // server tick, and only redirected a drone that had come to rest there.
            waypoint.redirectDockedPatrolDrones();
        }
    }

    public int height() { return height; }
    public BlockPos nextTarget() { return nextTarget; }

    public void addHeight(int amount) {
        height = Math.max(1, Math.min(15, height + amount));
        sync();
    }

    @Override
    public BlockPos dronePoint() {
        return worldPosition.relative(facing(), height);
    }

    @Override
    public void setNextDroneTarget(BlockPos target) {
        nextTarget = target.immutable();
        sync();
    }

    private Direction facing() {
        return getBlockState().hasProperty(DroneWaypointBlock.FACING)
                ? getBlockState().getValue(DroneWaypointBlock.FACING) : Direction.UP;
    }

    private void redirectDockedPatrolDrones() {
        if (level == null || nextTarget == null) return;
        BlockPos point = dronePoint();
        AABB dock = new AABB(point.getX(), point.getY(), point.getZ(),
                point.getX() + 1, point.getY() + 1, point.getZ() + 1);
        for (DeliveryDroneEntity drone : level.getEntitiesOfClass(DeliveryDroneEntity.class, dock,
                entity -> entity.getDeltaMovement().length() < 0.05D)) {
            drone.setTarget(nextTarget.getX() + 0.5D, nextTarget.getY(), nextTarget.getZ() + 0.5D);
        }
    }

    private void clientTick() {
        if (level == null || nextTarget == null || level.getGameTime() % 2L != 0L) {
            return;
        }
        BlockPos point = dronePoint();
        double x = point.getX() + 0.5D;
        double y = point.getY() + 0.5D;
        double z = point.getZ() + 0.5D;
        level.addParticle(net.minecraft.core.particles.DustParticleOptions.REDSTONE, x, y, z, 0.0D, 0.0D, 0.0D);
        ParticleUtil.spawnDroneLine(level, x, y, z,
                nextTarget.getX() - point.getX(), nextTarget.getY() - point.getY(),
                nextTarget.getZ() - point.getZ(), 0x0000FF);
    }

    private void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("height", height);
        if (nextTarget != null) {
            tag.putLong("next", nextTarget.asLong());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        height = tag.contains("height") ? Math.max(1, Math.min(15, tag.getInt("height"))) : 5;
        nextTarget = tag.contains("next") ? BlockPos.of(tag.getLong("next")) : null;
    }

    @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("height", height);
        if (nextTarget != null) tag.putLong("next", nextTarget.asLong());
        return tag;
    }
    @Override public void handleUpdateTag(CompoundTag tag) {
        height = tag.contains("height") ? Math.max(1, Math.min(15, tag.getInt("height"))) : 5;
        nextTarget = tag.contains("next") ? BlockPos.of(tag.getLong("next")) : null;
    }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet) { handleUpdateTag(packet.getTag()); }
}
