package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidConnectionUtil;
import com.hbm.ntm.fluid.HbmFluidConnector;
import com.hbm.ntm.fluid.HbmFluidNet;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodeHost;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FluidDuctPaintableExhaustBlockEntity extends BlockEntity
        implements HbmFluidConnector, HbmFluidNodeHost, PaintableDuctBlockEntity {
    private static final String TAG_PAINT_BLOCK = "block";
    private static final String TAG_PAINT_META = "meta";
    private static final String TAG_PAINT_BLOCK_NAME = "paint_block";
    private static final FluidType[] SMOKES = {
            HbmFluids.SMOKE,
            HbmFluids.SMOKE_LEADED,
            HbmFluids.SMOKE_POISON
    };

    private final HbmFluidNode[] nodes = new HbmFluidNode[SMOKES.length];
    @Nullable
    private BlockState paintedState;
    private int paintedMeta;

    public FluidDuctPaintableExhaustBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_DUCT_PAINTABLE_EXHAUST.get(), pos, state);
    }

    @Nullable
    @Override
    public BlockState getPaintedState() {
        return paintedState;
    }

    @Override
    public int getPaintedMeta() {
        return paintedMeta;
    }

    @Override
    public void setPaintedState(@Nullable BlockState state, int legacyMeta) {
        if (state != null && state.isAir()) {
            state = null;
        }
        paintedState = state;
        paintedMeta = legacyMeta & 15;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public HbmFluidNode getFluidNode() {
        return nodes[0];
    }

    public HbmFluidNet getFluidNet(FluidType type) {
        for (int i = 0; i < SMOKES.length; i++) {
            if (SMOKES[i] == type) {
                return nodes[i] == null ? null : nodes[i].getFluidNet();
            }
        }
        return null;
    }

    @Override
    public void refreshFluidNode() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int i = 0; i < SMOKES.length; i++) {
            FluidType type = SMOKES[i];
            if (nodes[i] != null) {
                HbmFluidNodespace.destroyNode(level, worldPosition, type);
                nodes[i] = null;
            }
            Set<Direction> connections = HbmFluidConnectionUtil.collectNodeConnections(level, worldPosition, type, this);
            nodes[i] = HbmFluidNodespace.createNode(level, new HbmFluidNode(worldPosition, type, connections));
        }
    }

    @Override
    public void removeFluidNode() {
        if (level != null && !level.isClientSide) {
            for (FluidType type : SMOKES) {
                HbmFluidNodespace.destroyNode(level, worldPosition, type);
            }
        }
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = null;
        }
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != null && (type == HbmFluids.SMOKE
                || type == HbmFluids.SMOKE_LEADED
                || type == HbmFluids.SMOKE_POISON);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (paintedState == null) {
            tag.remove(TAG_PAINT_BLOCK);
            tag.remove(TAG_PAINT_BLOCK_NAME);
        } else {
            ResourceLocation key = ForgeRegistries.BLOCKS.getKey(paintedState.getBlock());
            if (key != null) {
                tag.putString(TAG_PAINT_BLOCK_NAME, key.toString());
            }
            int legacyId = Block.getId(paintedState);
            if (legacyId != 0) {
                tag.putInt(TAG_PAINT_BLOCK, legacyId);
            }
        }
        tag.putInt(TAG_PAINT_META, paintedMeta & 15);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        BlockState state = null;
        if (tag.contains(TAG_PAINT_BLOCK_NAME)) {
            ResourceLocation key = ResourceLocation.tryParse(tag.getString(TAG_PAINT_BLOCK_NAME));
            Block block = key == null ? null : ForgeRegistries.BLOCKS.getValue(key);
            if (block != null && block != Blocks.AIR) {
                state = block.defaultBlockState();
            }
        }
        if (state == null && tag.contains(TAG_PAINT_BLOCK)) {
            BlockState legacyState = Block.stateById(tag.getInt(TAG_PAINT_BLOCK));
            if (!legacyState.isAir()) {
                state = legacyState;
            }
        }
        paintedState = state;
        paintedMeta = tag.getInt(TAG_PAINT_META) & 15;
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
