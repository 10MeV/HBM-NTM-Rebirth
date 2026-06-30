package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.block.FluidPipeAnchorBlock;
import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.client.ClientGeometryInvalidationBridge;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluidJsonUtil;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluidNodeHost;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.ArrayDeque;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public class FluidPipeBlockEntity extends BlockEntity implements HbmFluidConnector, HbmFluidNodeHost,
        HbmFluidNodeBlock.FluidTypedBlockEntity, HbmFluidCopiable, LegacyLookOverlayProvider {
    private static final String TAG_TYPE = "type";

    protected HbmFluidNode node;
    private FluidType type = HbmFluids.NONE;

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.FLUID_PIPE.get(), pos, state);
    }

    protected FluidPipeBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> blockEntityType,
            BlockPos pos, BlockState state) {
        super(blockEntityType, pos, state);
    }

    @Override
    public HbmFluidNode getFluidNode() {
        return node;
    }

    public HbmFluidNet getFluidNet() {
        return node == null ? null : node.getFluidNet();
    }

    @Override
    public FluidType getFluidType() {
        return type;
    }

    public void setFluidType(FluidType type) {
        FluidType next = type == null ? HbmFluids.NONE : type;
        if (this.type == next) {
            return;
        }
        FluidType previous = this.type;
        this.type = next;
        setChanged();

        if (level != null && !level.isClientSide) {
            HbmFluidNodespace.destroyNode(level, worldPosition, previous);
            node = null;
            refreshFluidNode();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            if (getBlockState().getBlock() instanceof HbmFluidNodeBlock nodeBlock) {
                nodeBlock.updateFluidConnectionGraph(level, worldPosition);
            }
        }
    }

    @Override
    public int[] getFluidIdsToCopy() {
        return new int[] {type.getId()};
    }

    @Override
    public CompoundTag getFluidSettings() {
        return HbmFluidCopiable.super.getFluidSettings();
    }

    @Override
    public List<Component> fluidSettingsDisplayInfo() {
        return HbmFluidCopiable.super.fluidSettingsDisplayInfo();
    }

    @Override
    public boolean pasteFluidSettings(CompoundTag tag, int index, @Nullable Player player, boolean recursive) {
        java.util.OptionalInt id = HbmFluidCopiable.copiedPipeFluidIdAt(tag, index);
        if (id.isEmpty()) {
            return false;
        }
        FluidType target = HbmFluids.fromId(id.getAsInt());
        if (recursive && level != null && !level.isClientSide) {
            return changeConnectedPipeTypes(level, worldPosition, getFluidType(), target, 64) > 0;
        }
        setFluidType(target);
        return true;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(net.minecraft.world.level.Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(LegacyLookOverlayLines.fluidName(type)));
    }

    @Override
    public void refreshFluidNode() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (type == HbmFluids.NONE) {
            removeFluidNode();
            return;
        }
        if (node != null) {
            HbmFluidNodespace.destroyNode(level, worldPosition, type);
            node = null;
        }
        node = HbmFluidNodespace.createNode(level, createNode());
    }

    protected HbmFluidNode createNode() {
        return new HbmFluidNode(worldPosition, type, getConnections());
    }

    protected Set<Direction> getConnections() {
        return level == null
                ? Set.of()
                : HbmFluidConnectionUtil.collectNodeConnections(level, worldPosition, type, this);
    }

    @Override
    public void removeFluidNode() {
        if (level != null && !level.isClientSide && type != HbmFluids.NONE) {
            HbmFluidNodespace.destroyNode(level, worldPosition, type);
        }
        node = null;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != null && type != null && type == this.type;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(TAG_TYPE, type.getName());
        tag.putInt(TAG_TYPE + "_id", type.getId());
    }

    @Override
    public void load(CompoundTag tag) {
        FluidType previous = type;
        super.load(tag);
        type = HbmFluidJsonUtil.readFluidReference(tag.getString(TAG_TYPE));
        if (type == HbmFluids.NONE && tag.contains(TAG_TYPE + "_id")) {
            type = HbmFluids.fromId(tag.getInt(TAG_TYPE + "_id"));
        }
        if (level != null && level.isClientSide && previous != type) {
            ClientGeometryInvalidationBridge.schedule(worldPosition);
        }
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
    public void onLoad() {
        super.onLoad();
        refreshFluidNode();
        if (level != null && level.isClientSide) {
            ClientGeometryInvalidationBridge.scheduleWithNeighbors(worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        removeFluidNode();
        super.setRemoved();
    }

    public static int changeConnectedPipeTypes(net.minecraft.world.level.Level level, BlockPos start,
            FluidType previous, FluidType target, int maxDistance) {
        if (previous == target) {
            return 0;
        }

        Queue<PipeVisit> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(new PipeVisit(start.immutable(), 0));
        int changed = 0;

        while (!queue.isEmpty()) {
            PipeVisit visit = queue.remove();
            if (!visited.add(visit.pos())) {
                continue;
            }
            if (!(level.getBlockEntity(visit.pos()) instanceof FluidPipeBlockEntity pipe)
                    || pipe.getFluidType() != previous) {
                continue;
            }

            pipe.setFluidType(target);
            changed++;

            if (visit.distance() >= maxDistance) {
                continue;
            }
            if (pipe instanceof FluidPipeAnchorBlockEntity anchor) {
                Direction attachedSide = FluidPipeAnchorBlock.attachedSide(pipe.getBlockState());
                queue.add(new PipeVisit(visit.pos().relative(attachedSide), visit.distance() + 1));
                for (BlockPos remote : anchor.getRemoteConnections()) {
                    queue.add(new PipeVisit(remote, visit.distance() + 1));
                }
            } else {
                for (Direction direction : Direction.values()) {
                    queue.add(new PipeVisit(visit.pos().relative(direction), visit.distance() + 1));
                }
            }
        }
        return changed;
    }

    private record PipeVisit(BlockPos pos, int distance) {
    }
}
