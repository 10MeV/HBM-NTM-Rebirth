package com.hbm.ntm.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Shared 1.7.10 pylon-style wire node behavior.
 */
public interface HbmLegacyWireNode extends HbmEnergyNodeHost {
    HbmLegacyWireConnections getWireConnections();

    HbmLegacyPowerNodeShapes.WireConnectionType getWireConnectionType();

    double getMaxWireLength();

    default Level getWireLevel() {
        return this instanceof BlockEntity blockEntity ? blockEntity.getLevel() : null;
    }

    default BlockPos getWirePos() {
        return this instanceof BlockEntity blockEntity ? blockEntity.getBlockPos() : null;
    }

    default Vec3 getWireConnectionPoint() {
        BlockPos pos = getWirePos();
        return pos == null ? Vec3.ZERO : Vec3.atCenterOf(pos);
    }

    default List<Vec3> getWireMountPoints() {
        return List.of(getWireConnectionPoint());
    }

    default HbmLegacyPowerNodeShapes.WireConnectionResult canConnectWireTo(HbmLegacyWireNode other) {
        if (other == null) {
            return HbmLegacyPowerNodeShapes.WireConnectionResult.TYPE_MISMATCH;
        }
        BlockPos selfPos = getWirePos();
        BlockPos otherPos = other.getWirePos();
        if (selfPos == null || otherPos == null) {
            return HbmLegacyPowerNodeShapes.WireConnectionResult.TYPE_MISMATCH;
        }
        return HbmLegacyPowerNodeShapes.canConnectWire(
                getWireConnectionType(),
                selfPos,
                getWireConnectionPoint(),
                getMaxWireLength(),
                other.getWireConnectionType(),
                otherPos,
                other.getWireConnectionPoint(),
                other.getMaxWireLength());
    }

    default HbmLegacyPowerNodeShapes.WireConnectionResult connectWireTo(HbmLegacyWireNode other) {
        HbmLegacyPowerNodeShapes.WireConnectionResult result = canConnectWireTo(other);
        if (result == HbmLegacyPowerNodeShapes.WireConnectionResult.OK) {
            addWireConnection(other.getWirePos());
            other.addWireConnection(getWirePos());
        }
        return result;
    }

    default boolean addWireConnection(BlockPos remotePos) {
        if (remotePos == null) {
            return false;
        }
        getWireConnections().add(remotePos);
        refreshEnergyNode();
        onWireConnectionsChanged();
        return true;
    }

    default boolean removeWireConnection(BlockPos remotePos) {
        if (!getWireConnections().remove(remotePos)) {
            return false;
        }
        refreshEnergyNode();
        onWireConnectionsChanged();
        return true;
    }

    default void disconnectAllWires() {
        Level level = getWireLevel();
        BlockPos selfPos = getWirePos();
        if (level != null && selfPos != null) {
            for (BlockPos remotePos : getWireConnections().connected()) {
                BlockEntity remote = level.getBlockEntity(remotePos);
                if (remote == (Object) this) {
                    continue;
                }
                if (remote instanceof HbmLegacyWireNode wireNode) {
                    wireNode.removeWireConnection(selfPos);
                }
            }
        }
        getWireConnections().clear();
        removeEnergyNode();
        onWireConnectionsChanged();
    }

    default boolean setWireColor(int color) {
        if (!getWireConnections().setColor(color)) {
            return false;
        }
        onWireConnectionsChanged();
        return true;
    }

    default void saveWireConnections(CompoundTag tag) {
        getWireConnections().save(tag);
    }

    default void loadWireConnections(CompoundTag tag) {
        getWireConnections().load(tag);
    }

    default void onWireConnectionsChanged() {
        if (this instanceof BlockEntity blockEntity) {
            blockEntity.setChanged();
            Level level = blockEntity.getLevel();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(),
                        blockEntity.getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }
}
