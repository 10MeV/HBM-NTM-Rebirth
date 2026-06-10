package com.hbm.ntm.blockentity;

import com.hbm.ntm.energy.HbmEnergyNode;
import com.hbm.ntm.energy.HbmLegacyPowerNodeShapes;
import com.hbm.ntm.energy.HbmLegacyWireConnections;
import com.hbm.ntm.energy.HbmLegacyWireNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
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
    private static final AABB INFINITE_RENDER_BOX = new AABB(
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY,
            Double.POSITIVE_INFINITY);

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
        return INFINITE_RENDER_BOX;
    }
}
