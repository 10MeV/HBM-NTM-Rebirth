package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.FluidPipeAnchorBlock;
import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.energy.HbmNetworkNode;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.Set;

public class FluidPipeAnchorBlockEntity extends FluidPipeBlockEntity {
    private static final String TAG_CONNECTION_COUNT = "conCount";
    private static final String TAG_CONNECTION_PREFIX = "con";
    private static final String TAG_CONNECTIONS = "remoteConnections";
    private static final String TAG_CONNECTION_POS = "pos";
    private static final double MAX_PIPE_LENGTH = 10.0D;
    private static final double MAX_PIPE_LENGTH_SQ = MAX_PIPE_LENGTH * MAX_PIPE_LENGTH;

    private final Set<BlockPos> remoteConnections = new LinkedHashSet<>();

    public FluidPipeAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PIPE_ANCHOR.get(), pos, state);
    }

    @Override
    protected Set<Direction> getConnections() {
        if (level == null) {
            return Set.of();
        }
        Direction attachedSide = FluidPipeAnchorBlock.attachedSide(getBlockState());
        return HbmFluidConnectionUtil.canConnect(level, worldPosition, getFluidType(), this, attachedSide)
                ? Set.of(attachedSide)
                : Set.of();
    }

    @Override
    protected HbmFluidNode createNode() {
        Set<BlockPos> positions = new LinkedHashSet<>();
        positions.add(worldPosition.immutable());

        Set<HbmNetworkNode.NodeConnection> connections = new LinkedHashSet<>();
        Direction attachedSide = FluidPipeAnchorBlock.attachedSide(getBlockState());
        if (level != null && HbmFluidConnectionUtil.canConnect(level, worldPosition, getFluidType(), this, attachedSide)) {
            connections.add(new HbmNetworkNode.NodeConnection(worldPosition.relative(attachedSide), attachedSide));
        }
        for (BlockPos remote : remoteConnections) {
            connections.add(HbmNetworkNode.NodeConnection.direct(remote, worldPosition));
        }
        return HbmFluidNode.withConnectionPoints(positions, getFluidType(), connections);
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return super.canConnectFluid(type, side) && side == FluidPipeAnchorBlock.attachedSide(getBlockState());
    }

    public Set<BlockPos> getRemoteConnections() {
        return Set.copyOf(remoteConnections);
    }

    public boolean hasRemoteConnection(BlockPos pos) {
        return pos != null && remoteConnections.contains(pos.immutable());
    }

    public LinkResult linkTo(FluidPipeAnchorBlockEntity other) {
        return link(this, other);
    }

    public boolean unlinkRemote(BlockPos remotePos) {
        if (remotePos == null || level == null || level.isClientSide) {
            return false;
        }
        BlockPos immutable = remotePos.immutable();
        boolean removed = remoteConnections.remove(immutable);
        if (!removed) {
            return false;
        }
        HbmFluidNodespace.destroyNode(level, worldPosition, getFluidType());
        node = null;
        refreshAfterConnectionChange();
        return true;
    }

    public boolean clearRemoteConnections() {
        if (remoteConnections.isEmpty()) {
            return false;
        }
        Set<BlockPos> copy = new LinkedHashSet<>(remoteConnections);
        boolean changed = false;
        for (BlockPos remote : copy) {
            changed |= unlinkPair(remote);
        }
        if (!changed) {
            remoteConnections.clear();
            refreshAfterConnectionChange();
            changed = true;
        }
        return changed;
    }

    public static LinkResult link(FluidPipeAnchorBlockEntity first, FluidPipeAnchorBlockEntity second) {
        if (first == null || second == null || first.level == null || first.level.isClientSide || first.level != second.level) {
            return LinkResult.INCOMPATIBLE;
        }
        LinkResult result = canLink(first, second);
        if (result != LinkResult.CONNECTED) {
            return result;
        }

        first.addRemoteConnection(second.worldPosition);
        second.addRemoteConnection(first.worldPosition);
        return LinkResult.CONNECTED;
    }

    public static LinkResult canLink(FluidPipeAnchorBlockEntity first, FluidPipeAnchorBlockEntity second) {
        if (first == null || second == null) {
            return LinkResult.INCOMPATIBLE;
        }
        if (first == second || first.worldPosition.equals(second.worldPosition)) {
            return LinkResult.SAME_BLOCK;
        }

        if (first.getFluidType() == HbmFluids.NONE && second.getFluidType() != HbmFluids.NONE) {
            first.setFluidType(second.getFluidType());
        }
        if (second.getFluidType() == HbmFluids.NONE && first.getFluidType() != HbmFluids.NONE) {
            second.setFluidType(first.getFluidType());
        }
        if (first.getFluidType() != second.getFluidType()) {
            return LinkResult.FLUID_MISMATCH;
        }
        if (first.getConnectionPoint().distSqr(second.getConnectionPoint()) > MAX_PIPE_LENGTH_SQ) {
            return LinkResult.TOO_FAR;
        }
        return LinkResult.CONNECTED;
    }

    @Override
    public void setFluidType(FluidType type) {
        FluidType previous = getFluidType();
        super.setFluidType(type);
        if (previous != getFluidType()) {
            refreshRemotePartners();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_CONNECTION_COUNT, remoteConnections.size());
        int index = 0;
        ListTag connections = new ListTag();
        for (BlockPos pos : remoteConnections) {
            tag.putIntArray(TAG_CONNECTION_PREFIX + index, new int[]{pos.getX(), pos.getY(), pos.getZ()});
            CompoundTag entry = new CompoundTag();
            entry.putLong(TAG_CONNECTION_POS, pos.asLong());
            connections.add(entry);
            index++;
        }
        tag.put(TAG_CONNECTIONS, connections);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        remoteConnections.clear();
        int legacyCount = tag.getInt(TAG_CONNECTION_COUNT);
        for (int i = 0; i < legacyCount; i++) {
            int[] pos = tag.getIntArray(TAG_CONNECTION_PREFIX + i);
            if (pos.length >= 3) {
                remoteConnections.add(new BlockPos(pos[0], pos[1], pos[2]));
            }
        }

        ListTag connections = tag.getList(TAG_CONNECTIONS, Tag.TAG_COMPOUND);
        for (Tag entryTag : connections) {
            CompoundTag entry = (CompoundTag) entryTag;
            if (entry.contains(TAG_CONNECTION_POS, Tag.TAG_LONG)) {
                remoteConnections.add(BlockPos.of(entry.getLong(TAG_CONNECTION_POS)));
            }
        }
        remoteConnections.remove(worldPosition);
    }

    private void addRemoteConnection(BlockPos remotePos) {
        if (remotePos == null) {
            return;
        }
        remoteConnections.add(remotePos.immutable());
        refreshAfterConnectionChange();
    }

    private boolean unlinkPair(BlockPos remotePos) {
        if (remotePos == null || level == null || level.isClientSide) {
            return false;
        }
        boolean changed = unlinkRemote(remotePos);
        BlockEntity remoteEntity = level.getBlockEntity(remotePos);
        if (remoteEntity instanceof FluidPipeAnchorBlockEntity remote) {
            changed |= remote.unlinkRemote(worldPosition);
        }
        return changed;
    }

    public void disconnectAllRemotePartners() {
        if (level == null || level.isClientSide) {
            return;
        }
        Set<BlockPos> copy = new LinkedHashSet<>(remoteConnections);
        remoteConnections.clear();
        for (BlockPos remotePos : copy) {
            BlockEntity remoteEntity = level.getBlockEntity(remotePos);
            if (remoteEntity instanceof FluidPipeAnchorBlockEntity remote) {
                remote.remoteConnections.remove(worldPosition);
                remote.refreshAfterConnectionChange();
            }
        }
    }

    private void refreshRemotePartners() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (BlockPos remotePos : remoteConnections) {
            BlockEntity remoteEntity = level.getBlockEntity(remotePos);
            if (remoteEntity instanceof FluidPipeAnchorBlockEntity remote) {
                remote.refreshAfterConnectionChange();
            }
        }
    }

    private void refreshAfterConnectionChange() {
        setChanged();
        if (level == null || level.isClientSide) {
            return;
        }
        refreshFluidNode();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        if (getBlockState().getBlock() instanceof HbmFluidNodeBlock nodeBlock) {
            nodeBlock.updateFluidConnectionGraph(level, worldPosition);
        }
    }

    private Vec3iPoint getConnectionPoint() {
        return new Vec3iPoint(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D);
    }

    public enum LinkResult {
        CONNECTED,
        INCOMPATIBLE,
        SAME_BLOCK,
        TOO_FAR,
        FLUID_MISMATCH
    }

    private record Vec3iPoint(double x, double y, double z) {
        private double distSqr(Vec3iPoint other) {
            double dx = x - other.x;
            double dy = y - other.y;
            double dz = z - other.z;
            return dx * dx + dy * dy + dz * dz;
        }
    }
}
