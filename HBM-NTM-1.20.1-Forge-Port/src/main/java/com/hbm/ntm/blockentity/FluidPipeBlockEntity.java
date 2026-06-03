package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.block.HbmFluidNodeBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluidNodeHost;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class FluidPipeBlockEntity extends BlockEntity implements HbmFluidConnector, HbmFluidNodeHost,
        HbmFluidNodeBlock.FluidTypedBlockEntity, LegacyLookOverlayProvider {
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
        return side != null && type != null && type != HbmFluids.NONE && type == this.type;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(TAG_TYPE, type.getName());
        tag.putInt(TAG_TYPE + "_id", type.getId());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        type = HbmFluids.fromName(tag.getString(TAG_TYPE));
        if (type == HbmFluids.NONE && tag.contains(TAG_TYPE + "_id")) {
            type = HbmFluids.fromId(tag.getInt(TAG_TYPE + "_id"));
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
    }

    @Override
    public void setRemoved() {
        removeFluidNode();
        super.setRemoved();
    }
}
