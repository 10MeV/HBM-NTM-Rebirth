package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.DroneWaypointBlock;
import com.hbm.ntm.drone.DroneLogisticsNetwork;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Request-network-only waypoint. Legacy source has no click handler; height remains saved at its default 5. */
public class DroneRequestWaypointBlockEntity extends BlockEntity {
    private int height = 5;

    public DroneRequestWaypointBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRONE_REQUEST_WAYPOINT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DroneRequestWaypointBlockEntity waypoint) {
        if (!level.isClientSide && level.getGameTime() % 20L == 0L) {
            Direction facing = state.getValue(DroneWaypointBlock.FACING);
            BlockPos point = pos.relative(facing, waypoint.height);
            DroneLogisticsNetwork.forLevel((ServerLevel) level).publish((ServerLevel) level, point,
                    DroneLogisticsNetwork.NodeKind.WAYPOINT, !level.hasNeighborSignal(pos), java.util.List.of(), java.util.List.of());
        }
    }

    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.putInt("height", height); }
    @Override public void load(CompoundTag tag) { super.load(tag); height = tag.contains("height") ? Math.max(1, Math.min(15, tag.getInt("height"))) : 5; }
    @Override public CompoundTag getUpdateTag() { CompoundTag tag = new CompoundTag(); tag.putInt("height", height); return tag; }
    @Override public void handleUpdateTag(CompoundTag tag) { height = tag.contains("height") ? Math.max(1, Math.min(15, tag.getInt("height"))) : 5; }
    @Override public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override public void onDataPacket(net.minecraft.network.Connection connection, ClientboundBlockEntityDataPacket packet) { handleUpdateTag(packet.getTag()); }
}
