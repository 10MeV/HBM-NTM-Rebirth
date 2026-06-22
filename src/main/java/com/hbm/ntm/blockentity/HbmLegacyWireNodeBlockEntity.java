package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmLegacyWireRenderMath;
import com.hbm.ntm.energy.HbmLegacyPowerNodeShapes;
import com.hbm.ntm.energy.HbmLegacyWireConnections;
import com.hbm.ntm.energy.HbmLegacyWireNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.7.10 TileEntityPylonBase-style remote wire node with old NBT and node lifecycle.
 */
public abstract class HbmLegacyWireNodeBlockEntity extends HbmEnergyNodeBlockEntity implements HbmLegacyWireNode {
    private static final double WIRE_RENDER_PADDING = 1.0D;
    private static final double PYLON_BODY_RENDER_PADDING = 16.0D;

    private final HbmLegacyWireConnections wireConnections = new HbmLegacyWireConnections();

    protected HbmLegacyWireNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static <T extends HbmLegacyWireNodeBlockEntity> void serverTick(Level level, BlockPos pos,
            BlockState state, T blockEntity) {
        if (!level.isClientSide) {
            blockEntity.refreshEnergyNodeState();
        }
    }

    @Override
    public HbmLegacyWireConnections getWireConnections() {
        return wireConnections;
    }

    @Override
    protected HbmEnergyNode createEnergyNode() {
        return HbmLegacyPowerNodeShapes.remoteOnlyPylon(worldPosition, wireConnections);
    }

    protected List<Vec3> getWireMounts() {
        return List.of(Vec3.atCenterOf(worldPosition));
    }

    @Override
    public List<Vec3> getWireMountPoints() {
        return List.copyOf(getWireMounts());
    }

    protected Direction getLegacyHorizontalFacing() {
        BlockState state = getBlockState();
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
    }

    protected Direction getLegacyFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return getLegacyHorizontalFacing();
    }

    @Override
    public Vec3 getWireConnectionPoint() {
        List<Vec3> mounts = getWireMounts();
        return mounts.isEmpty() ? Vec3.atCenterOf(worldPosition) : mounts.get(0);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        saveWireConnections(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        loadWireConnections(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) {
            load(tag);
        }
    }

    @Override
    public AABB getRenderBoundingBox() {
        RenderBoxBuilder box = new RenderBoxBuilder(new AABB(worldPosition).inflate(PYLON_BODY_RENDER_PADDING));
        List<Vec3> selfMounts = getWireMounts();
        for (Vec3 mount : selfMounts) {
            box.include(mount);
        }
        Level level = getLevel();
        if (level != null && !selfMounts.isEmpty()) {
            for (BlockPos remotePos : wireConnections.connected()) {
                BlockEntity remote = level.getBlockEntity(remotePos);
                if (!(remote instanceof HbmLegacyWireNode remoteWire)) {
                    continue;
                }
                List<Vec3> remoteMounts = remoteWire.getWireMountPoints();
                if (remoteMounts.isEmpty()) {
                    continue;
                }
                int lineCount = Math.min(selfMounts.size(), remoteMounts.size());
                int selfLegacyMetadata = HbmLegacyWireRenderMath.legacyMetadata(getBlockState());
                int remoteLegacyMetadata = HbmLegacyWireRenderMath.legacyMetadata(remote.getBlockState());
                for (int line = 0; line < lineCount; line++) {
                    Vec3 start = selfMounts.get(line % selfMounts.size());
                    int remoteIndex = HbmLegacyWireRenderMath.pylonSecondMountIndex(line, remoteMounts.size(), lineCount,
                            selfLegacyMetadata, remoteLegacyMetadata);
                    Vec3 end = start.add(remoteMounts.get(remoteIndex).subtract(start).scale(0.5D));
                    double hang = HbmLegacyWireRenderMath.pylonHang(start.distanceTo(end));
                    box.include(start);
                    box.include(end);
                    box.include(new Vec3((start.x + end.x) * 0.5D, Math.min(start.y, end.y) - hang,
                            (start.z + end.z) * 0.5D));
                }
            }
        }
        return box.build().inflate(WIRE_RENDER_PADDING);
    }

    private static final class RenderBoxBuilder {
        private double minX;
        private double minY;
        private double minZ;
        private double maxX;
        private double maxY;
        private double maxZ;

        private RenderBoxBuilder(AABB initial) {
            this.minX = initial.minX;
            this.minY = initial.minY;
            this.minZ = initial.minZ;
            this.maxX = initial.maxX;
            this.maxY = initial.maxY;
            this.maxZ = initial.maxZ;
        }

        private void include(Vec3 point) {
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            minZ = Math.min(minZ, point.z);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
            maxZ = Math.max(maxZ, point.z);
        }

        private AABB build() {
            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}
