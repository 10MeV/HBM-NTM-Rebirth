package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.fluid.ForgeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodeHost;
import com.hbm.ntm.fluid.HbmFluidPortMachine;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUser;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.FluidType;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class HbmFluidBlockEntity extends BlockEntity implements HbmFluidNodeHost, HbmFluidUser,
        HbmFluidCopiable,
        LegacyLookOverlayProvider {
    private static final String TAG_FLUIDS = "hbm_fluids";
    private static final String TAG_TANK_PREFIX = "tank_";

    private final List<HbmFluidTank> tanks;
    private final Map<Direction, LazyOptional<IFluidHandler>> sidedFluidHandlers = new EnumMap<>(Direction.class);
    private LazyOptional<IFluidHandler> nullSideFluidHandler;
    private final Map<com.hbm.ntm.fluid.FluidType, HbmFluidNode> fluidNodes = new HashMap<>();

    protected HbmFluidBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, List<HbmFluidTank> tanks) {
        super(type, pos, state);
        this.tanks = List.copyOf(tanks);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return LegacyMachineRenderBounds.visibleMultiblockOr(this, super.getRenderBoundingBox());
    }

    public List<HbmFluidTank> getAllTanks() {
        return tanks;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(net.minecraft.world.level.Level level, BlockPos viewedPos) {
        if (!showsLegacyFluidLookOverlay()) {
            return null;
        }
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.allFluidUserTanks(this));
    }

    protected boolean showsLegacyFluidLookOverlay() {
        return false;
    }

    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return tanks;
    }

    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return tanks;
    }

    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    protected Iterable<FluidPort> getFluidPorts() {
        return List.of();
    }

    protected int subscribeFluidProviderToPorts(com.hbm.ntm.fluid.FluidType type, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return subscribeFluidProviderToPortsReport(type, provider).subscribedPorts();
    }

    protected HbmFluidUtil.PortSubscribeReport subscribeFluidProviderToPortsReport(FluidType type,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortSubscribeReport.empty()
                : HbmFluidUtil.subscribeProviderToPortsReport(level, worldPosition, getFluidPorts(), type, provider);
    }

    protected int subscribeFluidReceiverToPorts(com.hbm.ntm.fluid.FluidType type, com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return subscribeFluidReceiverToPortsReport(type, receiver).subscribedPorts();
    }

    protected HbmFluidUtil.PortSubscribeReport subscribeFluidReceiverToPortsReport(FluidType type,
            com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortSubscribeReport.empty()
                : HbmFluidUtil.subscribeReceiverToPortsReport(level, worldPosition, getFluidPorts(), type, receiver);
    }

    protected int tryProvideFluidToPorts(com.hbm.ntm.fluid.FluidType type, int pressure, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return tryProvideFluidToPortsReport(type, pressure, provider).touchedPorts();
    }

    protected HbmFluidUtil.PortTransferReport tryProvideFluidToPortsReport(FluidType type, int pressure,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortTransferReport.empty()
                : HbmFluidUtil.tryProvideToPortsReport(level, worldPosition, getFluidPorts(), type, pressure, provider);
    }

    protected HbmFluidPortMachine.PortMachineRefreshReport refreshReceiverFluidPortsReport(
            Iterable<HbmFluidTank> receivingTanks, com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineRefreshReport.empty()
                : HbmFluidPortMachine.refreshReceiverPortsReport(
                        level, worldPosition, getFluidPorts(), receivingTanks, receiver);
    }

    protected HbmFluidPortMachine.PortMachineRefreshReport refreshProviderFluidPortsReport(
            Iterable<HbmFluidTank> sendingTanks, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineRefreshReport.empty()
                : HbmFluidPortMachine.refreshProviderPortsReport(
                        level, worldPosition, getFluidPorts(), sendingTanks, provider);
    }

    protected HbmFluidPortMachine.PortMachineRefreshReport refreshTransceiverFluidPortsReport(
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            com.hbm.ntm.fluid.HbmStandardFluidTransceiver transceiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineRefreshReport.empty()
                : HbmFluidPortMachine.refreshTransceiverPortsReport(
                        level, worldPosition, getFluidPorts(), receivingTanks, sendingTanks, transceiver);
    }

    protected HbmFluidPortMachine.PortMachineDetachReport detachReceiverFluidPortsReport(
            Iterable<HbmFluidTank> receivingTanks, com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineDetachReport.empty()
                : HbmFluidPortMachine.detachReceiverPortsReport(
                        level, worldPosition, getFluidPorts(), receivingTanks, receiver);
    }

    protected HbmFluidPortMachine.PortMachineDetachReport detachProviderFluidPortsReport(
            Iterable<HbmFluidTank> sendingTanks, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineDetachReport.empty()
                : HbmFluidPortMachine.detachProviderPortsReport(
                        level, worldPosition, getFluidPorts(), sendingTanks, provider);
    }

    protected HbmFluidPortMachine.PortMachineDetachReport detachTransceiverFluidPortsReport(
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            com.hbm.ntm.fluid.HbmStandardFluidTransceiver transceiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineDetachReport.empty()
                : HbmFluidPortMachine.detachTransceiverPortsReport(
                        level, worldPosition, getFluidPorts(), receivingTanks, sendingTanks, transceiver);
    }

    protected int unsubscribeFluidProviderFromPorts(com.hbm.ntm.fluid.FluidType type,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return unsubscribeFluidProviderFromPortsReport(type, provider).unsubscribedPorts();
    }

    protected HbmFluidUtil.PortDetachReport unsubscribeFluidProviderFromPortsReport(FluidType type,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortDetachReport.empty()
                : HbmFluidUtil.unsubscribeProviderFromPortsReport(level, worldPosition, getFluidPorts(), type, provider);
    }

    protected int unsubscribeFluidReceiverFromPorts(com.hbm.ntm.fluid.FluidType type,
            com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return unsubscribeFluidReceiverFromPortsReport(type, receiver).unsubscribedPorts();
    }

    protected HbmFluidUtil.PortDetachReport unsubscribeFluidReceiverFromPortsReport(FluidType type,
            com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortDetachReport.empty()
                : HbmFluidUtil.unsubscribeReceiverFromPortsReport(level, worldPosition, getFluidPorts(), type, receiver);
    }

    protected HbmFluidUtil.PortSetSnapshot inspectFluidPorts(FluidType type) {
        return level == null
                ? new HbmFluidUtil.PortSetSnapshot(0, 0, 0, 0, 0, 0)
                : HbmFluidUtil.inspectPorts(level, worldPosition, getFluidPorts(), type);
    }

    @Override
    public HbmFluidNode getFluidNode() {
        return fluidNodes.values().stream().findFirst().orElse(null);
    }

    protected void setFluidNode(HbmFluidNode node) {
        if (node != null) {
            fluidNodes.put(node.getFluidType(), node);
        }
    }

    protected HbmFluidNode getFluidNode(com.hbm.ntm.fluid.FluidType type) {
        return fluidNodes.get(type);
    }

    protected void removeFluidNode(com.hbm.ntm.fluid.FluidType type) {
        fluidNodes.remove(type);
    }

    protected Set<com.hbm.ntm.fluid.FluidType> getTrackedFluidNodeTypes() {
        return new HashSet<>(fluidNodes.keySet());
    }

    @Override
    public void refreshFluidNode() {
    }

    @Override
    public void removeFluidNode() {
        fluidNodes.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        CompoundTag fluids = new CompoundTag();
        for (int i = 0; i < tanks.size(); i++) {
            tanks.get(i).writeToNbt(fluids, TAG_TANK_PREFIX + i);
        }
        tag.put(TAG_FLUIDS, fluids);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        CompoundTag fluids = tag.getCompound(TAG_FLUIDS);
        for (int i = 0; i < tanks.size(); i++) {
            tanks.get(i).readFromNbt(fluids, TAG_TANK_PREFIX + i);
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
    public void invalidateCaps() {
        super.invalidateCaps();
        invalidateFluidHandlers();
    }

    protected void invalidateFluidHandlers() {
        for (LazyOptional<IFluidHandler> handler : sidedFluidHandlers.values()) {
            handler.invalidate();
        }
        sidedFluidHandlers.clear();
        if (nullSideFluidHandler != null) {
            nullSideFluidHandler.invalidate();
            nullSideFluidHandler = null;
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return getFluidHandler(side).cast();
        }
        return super.getCapability(capability, side);
    }

    protected void onFluidContentsChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onFluidSettingsPasted() {
        onFluidContentsChanged();
        invalidateFluidHandlers();
    }

    private LazyOptional<IFluidHandler> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            if (nullSideFluidHandler == null) {
                nullSideFluidHandler = createFluidHandler(null);
            }
            return nullSideFluidHandler;
        }
        return sidedFluidHandlers.computeIfAbsent(side, this::createFluidHandler);
    }

    private LazyOptional<IFluidHandler> createFluidHandler(@Nullable Direction side) {
        HbmFluidSideMode mode = getFluidSideMode(side);
        if (mode == HbmFluidSideMode.NONE) {
            return LazyOptional.empty();
        }
        if (mode == HbmFluidSideMode.BOTH) {
            return LazyOptional.of(() -> new ForgeFluidHandlerAdapter(
                    getInputTanks(side),
                    getOutputTanks(side),
                    getInputPressure(side),
                    true,
                    true,
                    this::onFluidContentsChanged));
        }
        List<HbmFluidTank> visibleTanks = mode == HbmFluidSideMode.INPUT ? getInputTanks(side) : getOutputTanks(side);
        return LazyOptional.of(() -> new ForgeFluidHandlerAdapter(
                visibleTanks,
                getInputPressure(side),
                mode.canFill(),
                mode.canDrain(),
                this::onFluidContentsChanged));
    }

    protected int getInputPressure(@Nullable Direction side) {
        return 0;
    }
}
