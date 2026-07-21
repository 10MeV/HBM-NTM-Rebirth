package com.hbm.ntm.blockentity;

import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.item.RebarPlacerItem;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.uninos.networkproviders.RebarNetwork;
import com.hbm.ntm.uninos.networkproviders.RebarNode;
import com.hbm.ntm.uninos.networkproviders.RebarNodespace;
import com.hbm.ntm.util.HbmRegistryUtil;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RebarBlockEntity extends HbmFluidNetworkBlockEntity implements HbmFluidReceiver {
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_HAS_CONNECTION = "hasConnection";
    private static final String TAG_TARGET = "target";
    private static final String TAG_LEGACY_BLOCK = "block";
    private static final String TAG_LEGACY_META = "meta";
    private static final int MAX_PROGRESS = 1_000;
    private static final int MAX_TRANSFER_PER_REBAR = 50;

    private static final List<FluidPort> FLUID_PORTS = HbmFluidPortLayouts.allAdjacent();

    private int progress;
    private int prevProgress;
    private boolean hasConnection;
    @Nullable
    private ResourceLocation targetBlockId;
    @Nullable
    private RebarNode node;

    public RebarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REBAR.get(), pos, state, List.of(new HbmFluidTank(HbmFluids.CONCRETE, MAX_PROGRESS)));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RebarBlockEntity rebar) {
        rebar.syncConcreteTank();
        if (rebar.progress >= MAX_PROGRESS) {
            rebar.completeConcreteFill();
            return;
        }

        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, rebar);
        rebar.ensureRebarNode();
        if (level.getGameTime() % 60L == 0L) {
            rebar.refreshHasConnection();
        }
        if (rebar.prevProgress != rebar.progress) {
            rebar.prevProgress = rebar.progress;
            rebar.markProgressChanged();
        }
    }

    public void setup(BlockState targetState) {
        if (targetState != null && RebarPlacerItem.isValidConcrete(targetState)) {
            targetBlockId = HbmRegistryUtil.blockKey(targetState.getBlock());
            setChanged();
            sendBlockUpdate();
        }
    }

    public int getProgress() {
        return progress;
    }

    public boolean hasConnection() {
        return hasConnection;
    }

    public void refreshHasConnection() {
        if (level == null || level.isClientSide) {
            return;
        }
        boolean connected = false;
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(worldPosition.relative(direction));
            if (neighbor instanceof FluidPipeBlockEntity
                    || neighbor instanceof FluidDuctExhaustBlockEntity
                    || neighbor instanceof FluidDuctPaintableExhaustBlockEntity) {
                connected = true;
                break;
            }
        }
        if (hasConnection != connected) {
            hasConnection = connected;
            setChanged();
            sendBlockUpdate();
        }
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        if (type != HbmFluids.CONCRETE || amount <= 0L || level == null) {
            return amount;
        }
        RebarNode localNode = ensureRebarNode();
        if (localNode == null || localNode.isExpired() || !localNode.hasValidNet()) {
            return amount;
        }
        RebarNetwork network = localNode.getRebarNet();
        if (network == null || !network.isValid()) {
            return amount;
        }

        List<RebarBlockEntity> lowestLinks = new ArrayList<>();
        int lowestY = Integer.MAX_VALUE;
        int totalProgress = 0;
        int capacity = 0;
        for (RebarNode link : network.getLinks()) {
            BlockPos linkPos = link.getPos();
            int y = linkPos.getY();
            if (y < lowestY) {
                lowestY = y;
                totalProgress = 0;
                capacity = 0;
                lowestLinks.clear();
            }
            if (y == lowestY && level.getBlockEntity(linkPos) instanceof RebarBlockEntity rebar) {
                totalProgress += rebar.progress;
                capacity += MAX_PROGRESS;
                lowestLinks.add(rebar);
            }
        }

        if (capacity <= 0 || lowestLinks.isEmpty()) {
            return amount;
        }
        int maxAccept = (int) Math.min(Math.min(capacity - totalProgress, amount),
                (long) MAX_TRANSFER_PER_REBAR * lowestLinks.size());
        int target = Math.min((totalProgress + maxAccept) / lowestLinks.size(), MAX_PROGRESS);
        for (RebarBlockEntity rebar : lowestLinks) {
            if (rebar.progress >= target) {
                continue;
            }
            int delta = target - rebar.progress;
            if (delta > amount) {
                continue;
            }
            rebar.setProgress(rebar.progress + delta);
            amount -= delta;
        }
        return amount;
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        return type == HbmFluids.CONCRETE ? 10_000L : 0L;
    }

    @Override
    public boolean canConnectFluid(FluidType type, Direction side) {
        return side != null && type == HbmFluids.CONCRETE;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == HbmFluids.CONCRETE;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.NONE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        syncConcreteTank();
        super.saveAdditional(tag);
        tag.putInt(TAG_PROGRESS, progress);
        tag.putBoolean(TAG_HAS_CONNECTION, hasConnection);
        if (targetBlockId != null) {
            tag.putString(TAG_TARGET, targetBlockId.toString());
            tag.putString(TAG_LEGACY_BLOCK, targetBlockId.toString());
            tag.putInt(TAG_LEGACY_META, 0);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        progress = clampProgress(tag.getInt(TAG_PROGRESS));
        prevProgress = progress;
        hasConnection = tag.getBoolean(TAG_HAS_CONNECTION);
        targetBlockId = readTargetBlockId(tag);
        syncConcreteTank();
    }

    @Override
    public void setRemoved() {
        removeRebarNode();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    }

    private void setProgress(int progress) {
        int clamped = clampProgress(progress);
        if (this.progress == clamped) {
            return;
        }
        this.progress = clamped;
        syncConcreteTank();
        markProgressChanged();
    }

    private static int clampProgress(int progress) {
        return Math.max(0, Math.min(MAX_PROGRESS, progress));
    }

    private void syncConcreteTank() {
        HbmFluidTank tank = getAllTanks().get(0);
        if (tank.getTankType() != HbmFluids.CONCRETE) {
            tank.setTankType(HbmFluids.CONCRETE);
        }
        if (tank.getFill() != progress) {
            tank.setFill(progress);
        }
    }

    private void markProgressChanged() {
        syncConcreteTank();
        setChanged();
        sendBlockUpdate();
    }

    private void sendBlockUpdate() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    private RebarNode ensureRebarNode() {
        if (level == null || level.isClientSide) {
            return node;
        }
        if (node != null && !node.isExpired()) {
            return node;
        }
        RebarNode existing = RebarNodespace.getNode(level, worldPosition);
        if (existing != null && !existing.isExpired()) {
            node = existing;
            return node;
        }
        node = RebarNodespace.createNode(level, new RebarNode(worldPosition, EnumSet.allOf(Direction.class)));
        return node;
    }

    private void removeRebarNode() {
        if (level != null && !level.isClientSide && node != null) {
            RebarNodespace.destroyNode(level, worldPosition);
        }
        node = null;
    }

    private void completeConcreteFill() {
        if (level == null || level.isClientSide) {
            return;
        }
        BlockState target = targetConcreteState();
        removeRebarNode();
        level.setBlock(worldPosition, target, Block.UPDATE_ALL);
    }

    private BlockState targetConcreteState() {
        Block block = targetBlockId == null ? null : HbmRegistryUtil.block(targetBlockId).orElse(null);
        if (block != null && block != Blocks.AIR && RebarPlacerItem.isValidConcrete(block.defaultBlockState())) {
            return block.defaultBlockState();
        }
        return ModBlocks.legacyBlock("concrete_rebar").get().defaultBlockState();
    }

    @Nullable
    private static ResourceLocation readTargetBlockId(CompoundTag tag) {
        if (tag.contains(TAG_TARGET, Tag.TAG_STRING)) {
            return ResourceLocation.tryParse(tag.getString(TAG_TARGET));
        }
        if (tag.contains(TAG_LEGACY_BLOCK, Tag.TAG_STRING)) {
            return ResourceLocation.tryParse(tag.getString(TAG_LEGACY_BLOCK));
        }
        return null;
    }
}
