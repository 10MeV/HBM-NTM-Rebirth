package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.fluid.ForgeFluidHandlerAdapter;
import com.hbm.ntm.fluid.ForgeRecipeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidContainerRegistry;
import com.hbm.ntm.fluid.HbmFluidCopiable;
import com.hbm.ntm.fluid.HbmFluidGuiHelper;
import com.hbm.ntm.fluid.HbmFluidItemTransfer;
import com.hbm.ntm.fluid.HbmFluidItemTransfer.FluidIdentifierSlotReport;
import com.hbm.ntm.fluid.HbmFluidItemTransfer.TankSlotTransfer;
import com.hbm.ntm.fluid.HbmFluidItemTransfer.TankSlotTransferResult;
import com.hbm.ntm.fluid.HbmFluidItemTransfer.TransferBatchReport;
import com.hbm.ntm.fluid.HbmFluidNode;
import com.hbm.ntm.fluid.HbmFluidNodeHost;
import com.hbm.ntm.fluid.HbmFluidNodespace;
import com.hbm.ntm.fluid.HbmFluidPortMachine;
import com.hbm.ntm.fluid.HbmFluidPortSubscriptionTracker;
import com.hbm.ntm.fluid.HbmFluidRecipeIO;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidStack;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidTankSet;
import com.hbm.ntm.fluid.HbmFluidUser;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.util.HbmMachinePerformanceCounters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class HbmFluidBlockEntity extends BlockEntity implements HbmFluidNodeHost, HbmFluidUser,
        HbmFluidCopiable,
        LegacyLookOverlayProvider, HbmLegacyLoadedTile {
    private static final String TAG_FLUIDS = "hbm_fluids";
    private static final String TAG_TANK_PREFIX = "tank_";

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final List<HbmFluidTank> tanks;
    private final Map<Direction, LazyOptional<IFluidHandler>> sidedFluidHandlers = new EnumMap<>(Direction.class);
    private LazyOptional<IFluidHandler> nullSideFluidHandler;
    private final Map<com.hbm.ntm.fluid.FluidType, HbmFluidNode> fluidNodes = new HashMap<>();
    private final HbmFluidPortSubscriptionTracker fluidPortSubscriptions = new HbmFluidPortSubscriptionTracker();

    protected HbmFluidBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, List<HbmFluidTank> tanks) {
        super(type, pos, state);
        this.tanks = List.copyOf(tanks);
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
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

    public boolean supportsFluidSettingsCopy() {
        return true;
    }

    @Override
    public int[] getFluidIdsToCopy() {
        return supportsFluidSettingsCopy() ? HbmFluidCopiable.super.getFluidIdsToCopy() : new int[0];
    }

    @Nullable
    @Override
    public HbmFluidTank getTankToPasteFluidSettings() {
        return supportsFluidSettingsCopy() ? HbmFluidCopiable.super.getTankToPasteFluidSettings() : null;
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

    protected HbmFluidUtil.PortSubscribeDetailReport subscribeFluidProviderToPortsDetailedReport(FluidType type,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortSubscribeDetailReport.empty()
                : HbmFluidUtil.subscribeProviderToPortsDetailedReport(
                        level, worldPosition, getFluidPorts(), type, provider);
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

    protected HbmFluidUtil.PortSubscribeDetailReport subscribeFluidReceiverToPortsDetailedReport(FluidType type,
            com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortSubscribeDetailReport.empty()
                : HbmFluidUtil.subscribeReceiverToPortsDetailedReport(
                        level, worldPosition, getFluidPorts(), type, receiver);
    }

    protected int tryProvideFluidToPorts(com.hbm.ntm.fluid.FluidType type, int pressure, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return tryProvideFluidToPortsReport(type, pressure, provider).touchedPorts();
    }

    protected HbmFluidUtil.PortTransferReport tryProvideFluidToPortsReport(FluidType type, int pressure,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        if (provider == null || type == null || type == HbmFluids.NONE
                || provider.getFluidAvailable(type, pressure) <= 0L) {
            return HbmFluidUtil.PortTransferReport.empty();
        }
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortTransferReport.empty()
                : HbmFluidUtil.tryProvideToPortsReport(level, worldPosition, getFluidPorts(), type, pressure, provider);
    }

    protected HbmFluidUtil.PortTransferDetailReport tryProvideFluidToPortsDetailedReport(FluidType type, int pressure,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortTransferDetailReport.empty()
                : HbmFluidUtil.tryProvideToPortsDetailedReport(
                        level, worldPosition, getFluidPorts(), type, pressure, provider);
    }

    protected HbmFluidUtil.ForgeFluidTransferReport previewProvideFluidToForgeHandler(
            BlockEntity target, @Nullable Direction targetSide, FluidType type, int pressure,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return HbmFluidUtil.previewProvideToForgeHandler(target, targetSide, type, pressure, provider);
    }

    protected HbmFluidUtil.ForgeFluidTransferReport tryProvideFluidToForgeHandlerReport(
            BlockEntity target, @Nullable Direction targetSide, FluidType type, int pressure,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return HbmFluidUtil.tryProvideToForgeHandlerReport(target, targetSide, type, pressure, provider);
    }

    protected HbmFluidUtil.HbmFluidTransferReport tryProvideFluidToReceiverReport(
            FluidType type, int pressure, com.hbm.ntm.fluid.HbmFluidProvider provider,
            com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return HbmFluidUtil.tryProvideToReceiverReport(type, pressure, provider, receiver);
    }

    protected HbmFluidPortMachine.PortMachineRefreshReport refreshReceiverFluidPortsReport(
            Iterable<HbmFluidTank> receivingTanks, com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineRefreshReport.empty()
                : HbmFluidPortMachine.refreshReceiverPortsReport(
                        level, worldPosition, getFluidPorts(), receivingTanks, receiver);
    }

    protected HbmFluidPortMachine.PortMachineRefreshDetailReport refreshReceiverFluidPortsDetailedReport(
            Iterable<HbmFluidTank> receivingTanks, com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineRefreshDetailReport.empty()
                : HbmFluidPortMachine.refreshReceiverPortsDetailedReport(
                        level, worldPosition, getFluidPorts(), receivingTanks, receiver);
    }

    protected HbmFluidPortMachine.PortMachineRefreshReport refreshProviderFluidPortsReport(
            Iterable<HbmFluidTank> sendingTanks, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineRefreshReport.empty()
                : HbmFluidPortMachine.refreshProviderPortsReport(
                        level, worldPosition, getFluidPorts(), sendingTanks, provider);
    }

    protected HbmFluidPortMachine.PortMachineRefreshDetailReport refreshProviderFluidPortsDetailedReport(
            Iterable<HbmFluidTank> sendingTanks, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineRefreshDetailReport.empty()
                : HbmFluidPortMachine.refreshProviderPortsDetailedReport(
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

    protected HbmFluidPortMachine.PortMachineRefreshDetailReport refreshTransceiverFluidPortsDetailedReport(
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            com.hbm.ntm.fluid.HbmStandardFluidTransceiver transceiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineRefreshDetailReport.empty()
                : HbmFluidPortMachine.refreshTransceiverPortsDetailedReport(
                        level, worldPosition, getFluidPorts(), receivingTanks, sendingTanks, transceiver);
    }

    protected HbmFluidPortSubscriptionTracker.TrackedPortRefreshReport refreshTrackedReceiverFluidPortsReport(
            Iterable<HbmFluidTank> receivingTanks, com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortSubscriptionTracker.TrackedPortRefreshReport.empty()
                : fluidPortSubscriptions.refreshReceiver(level, worldPosition, getFluidPorts(), receivingTanks, receiver);
    }

    protected HbmFluidPortSubscriptionTracker.TrackedPortRefreshDetailReport refreshTrackedReceiverFluidPortsDetailedReport(
            Iterable<HbmFluidTank> receivingTanks, com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortSubscriptionTracker.TrackedPortRefreshDetailReport.empty()
                : fluidPortSubscriptions.refreshReceiverDetailed(
                        level, worldPosition, getFluidPorts(), receivingTanks, receiver);
    }

    protected HbmFluidPortSubscriptionTracker.TrackedPortRefreshReport refreshTrackedProviderFluidPortsReport(
            Iterable<HbmFluidTank> sendingTanks, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidPortSubscriptionTracker.TrackedPortRefreshReport.empty()
                : fluidPortSubscriptions.refreshProvider(level, worldPosition, getFluidPorts(), sendingTanks, provider);
    }

    protected HbmFluidPortSubscriptionTracker.TrackedPortRefreshDetailReport refreshTrackedProviderFluidPortsDetailedReport(
            Iterable<HbmFluidTank> sendingTanks, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidPortSubscriptionTracker.TrackedPortRefreshDetailReport.empty()
                : fluidPortSubscriptions.refreshProviderDetailed(
                        level, worldPosition, getFluidPorts(), sendingTanks, provider);
    }

    protected HbmFluidPortSubscriptionTracker.TrackedPortRefreshReport refreshTrackedTransceiverFluidPortsReport(
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            com.hbm.ntm.fluid.HbmStandardFluidTransceiver transceiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortSubscriptionTracker.TrackedPortRefreshReport.empty()
                : fluidPortSubscriptions.refreshTransceiver(
                        level, worldPosition, getFluidPorts(), receivingTanks, sendingTanks, transceiver);
    }

    protected HbmFluidPortSubscriptionTracker.TrackedPortRefreshDetailReport refreshTrackedTransceiverFluidPortsDetailedReport(
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            com.hbm.ntm.fluid.HbmStandardFluidTransceiver transceiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortSubscriptionTracker.TrackedPortRefreshDetailReport.empty()
                : fluidPortSubscriptions.refreshTransceiverDetailed(
                        level, worldPosition, getFluidPorts(), receivingTanks, sendingTanks, transceiver);
    }

    protected HbmFluidPortMachine.PortMachineDetachReport detachReceiverFluidPortsReport(
            Iterable<HbmFluidTank> receivingTanks, com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineDetachReport.empty()
                : HbmFluidPortMachine.detachReceiverPortsReport(
                        level, worldPosition, getFluidPorts(), receivingTanks, receiver);
    }

    protected HbmFluidPortMachine.PortMachineDetachDetailReport detachReceiverFluidPortsDetailedReport(
            Iterable<HbmFluidTank> receivingTanks, com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineDetachDetailReport.empty()
                : HbmFluidPortMachine.detachReceiverPortsDetailedReport(
                        level, worldPosition, getFluidPorts(), receivingTanks, receiver);
    }

    protected HbmFluidPortMachine.PortMachineDetachReport detachProviderFluidPortsReport(
            Iterable<HbmFluidTank> sendingTanks, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineDetachReport.empty()
                : HbmFluidPortMachine.detachProviderPortsReport(
                        level, worldPosition, getFluidPorts(), sendingTanks, provider);
    }

    protected HbmFluidPortMachine.PortMachineDetachDetailReport detachProviderFluidPortsDetailedReport(
            Iterable<HbmFluidTank> sendingTanks, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineDetachDetailReport.empty()
                : HbmFluidPortMachine.detachProviderPortsDetailedReport(
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

    protected HbmFluidPortMachine.PortMachineDetachDetailReport detachTransceiverFluidPortsDetailedReport(
            Iterable<HbmFluidTank> receivingTanks, Iterable<HbmFluidTank> sendingTanks,
            com.hbm.ntm.fluid.HbmStandardFluidTransceiver transceiver) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineDetachDetailReport.empty()
                : HbmFluidPortMachine.detachTransceiverPortsDetailedReport(
                        level, worldPosition, getFluidPorts(), receivingTanks, sendingTanks, transceiver);
    }

    protected HbmFluidPortMachine.PortMachineDetachReport detachTrackedFluidPortsReport(
            com.hbm.ntm.fluid.HbmFluidReceiver receiver, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineDetachReport.empty()
                : fluidPortSubscriptions.detachAll(level, worldPosition, getFluidPorts(), receiver, provider);
    }

    protected HbmFluidPortMachine.PortMachineDetachDetailReport detachTrackedFluidPortsDetailedReport(
            com.hbm.ntm.fluid.HbmFluidReceiver receiver, com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidPortMachine.PortMachineDetachDetailReport.empty()
                : fluidPortSubscriptions.detachAllDetailed(level, worldPosition, getFluidPorts(), receiver, provider);
    }

    protected HbmFluidPortMachine.PortMachineDetachReport detachTrackedTransceiverFluidPortsReport(
            com.hbm.ntm.fluid.HbmStandardFluidTransceiver transceiver) {
        return detachTrackedFluidPortsReport(transceiver, transceiver);
    }

    protected HbmFluidPortMachine.PortMachineDetachDetailReport detachTrackedTransceiverFluidPortsDetailedReport(
            com.hbm.ntm.fluid.HbmStandardFluidTransceiver transceiver) {
        return detachTrackedFluidPortsDetailedReport(transceiver, transceiver);
    }

    protected HbmFluidTankSet.TankSetInspection inspectReceivingFluidTanks(
            Iterable<HbmFluidTank> receivingTanks, FluidType type, int pressure) {
        return HbmFluidTankSet.inspectReceivingTanks(receivingTanks, type, pressure);
    }

    protected HbmFluidTankSet.TankSetInspection inspectSendingFluidTanks(
            Iterable<HbmFluidTank> sendingTanks, FluidType type, int pressure) {
        return HbmFluidTankSet.inspectSendingTanks(sendingTanks, type, pressure);
    }

    protected HbmFluidTankSet.TankTransferReport previewReceiveFluidIntoTanks(
            Iterable<HbmFluidTank> receivingTanks, FluidType type, int pressure, long amount) {
        return HbmFluidTankSet.previewReceive(receivingTanks, type, pressure, amount);
    }

    protected HbmFluidTankSet.TankTransferReport receiveFluidIntoTanksReport(
            Iterable<HbmFluidTank> receivingTanks, FluidType type, int pressure, long amount) {
        return HbmFluidTankSet.receive(receivingTanks, type, pressure, amount, false);
    }

    protected HbmFluidTankSet.TankTransferReport previewUseUpFluidFromTanks(
            Iterable<HbmFluidTank> sendingTanks, FluidType type, int pressure, long amount) {
        return HbmFluidTankSet.previewUseUp(sendingTanks, type, pressure, amount);
    }

    protected HbmFluidTankSet.TankTransferReport useUpFluidFromTanksReport(
            Iterable<HbmFluidTank> sendingTanks, FluidType type, int pressure, long amount) {
        return HbmFluidTankSet.useUp(sendingTanks, type, pressure, amount, false);
    }

    protected int unsubscribeFluidProviderFromPorts(com.hbm.ntm.fluid.FluidType type,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return unsubscribeFluidProviderFromPortsReport(type, provider).unsubscribedPorts();
    }

    protected HbmFluidUtil.PortDetachReport unsubscribeFluidProviderFromPortsReport(FluidType type,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortDetachReport.empty()
                : HbmFluidUtil.unsubscribeProviderFromPortsReport(
                        level, worldPosition, getFluidPorts(), type, provider);
    }

    protected HbmFluidUtil.PortDetachDetailReport unsubscribeFluidProviderFromPortsDetailedReport(FluidType type,
            com.hbm.ntm.fluid.HbmFluidProvider provider) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortDetachDetailReport.empty()
                : HbmFluidUtil.unsubscribeProviderFromPortsDetailedReport(
                        level, worldPosition, getFluidPorts(), type, provider);
    }

    protected int unsubscribeFluidReceiverFromPorts(com.hbm.ntm.fluid.FluidType type,
            com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return unsubscribeFluidReceiverFromPortsReport(type, receiver).unsubscribedPorts();
    }

    protected HbmFluidUtil.PortDetachReport unsubscribeFluidReceiverFromPortsReport(FluidType type,
            com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortDetachReport.empty()
                : HbmFluidUtil.unsubscribeReceiverFromPortsReport(
                        level, worldPosition, getFluidPorts(), type, receiver);
    }

    protected HbmFluidUtil.PortDetachDetailReport unsubscribeFluidReceiverFromPortsDetailedReport(FluidType type,
            com.hbm.ntm.fluid.HbmFluidReceiver receiver) {
        return level == null || level.isClientSide
                ? HbmFluidUtil.PortDetachDetailReport.empty()
                : HbmFluidUtil.unsubscribeReceiverFromPortsDetailedReport(
                        level, worldPosition, getFluidPorts(), type, receiver);
    }

    protected HbmFluidUtil.PortSetSnapshot inspectFluidPorts(FluidType type) {
        return level == null
                ? new HbmFluidUtil.PortSetSnapshot(0, 0, 0, 0, 0, 0)
                : HbmFluidUtil.inspectPorts(level, worldPosition, getFluidPorts(), type);
    }

    protected HbmFluidUtil.FluidEndpointSnapshot inspectFluidEndpoint(
            BlockPos pos, @Nullable Direction side, FluidType type) {
        return HbmFluidUtil.inspectEndpoint(level, pos, side, type);
    }

    protected HbmFluidNodespace.NetworkPressureBalanceSnapshot inspectFluidNetworkPressureBalance(FluidType type) {
        return level == null
                ? new HbmFluidNodespace.NetworkPressureBalanceSnapshot(
                        worldPosition,
                        (type == null ? HbmFluids.NONE : type).getName(),
                        false,
                        "none",
                        false,
                        false,
                        null)
                : HbmFluidNodespace.getNetworkPressureBalanceSnapshot(level, worldPosition, type);
    }

    public HbmFluidGuiHelper.TankSetSnapshot inspectFluidTankSnapshot() {
        return HbmFluidGuiHelper.snapshotTanks(getAllTanks());
    }

    public HbmFluidGuiHelper.TankSetDiff diffFluidTankSnapshot(HbmFluidGuiHelper.TankSetSnapshot previous) {
        return HbmFluidGuiHelper.diff(previous, inspectFluidTankSnapshot());
    }

    protected HbmFluidTank.TankState inspectFluidTankState(HbmFluidTank tank) {
        return tank == null ? new HbmFluidTank.TankState(HbmFluids.NONE, 0, 0, 0) : tank.snapshot();
    }

    protected HbmFluidTank.TankMutationReport fillFluidTankReport(
            HbmFluidTank tank, FluidType type, int amount, int pressure, boolean simulate) {
        HbmFluidTank.TankMutationReport report = tank == null
                ? new HbmFluidTank.TankMutationReport(
                        "fill", simulate, inspectFluidTankState(null), inspectFluidTankState(null),
                        amount, 0, 0, Math.max(0, amount), false)
                : tank.fillReport(type, amount, pressure, simulate);
        if (!simulate && report.changed()) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected HbmFluidTank.TankMutationReport drainFluidTankReport(
            HbmFluidTank tank, int amount, boolean simulate) {
        HbmFluidTank.TankMutationReport report = tank == null
                ? new HbmFluidTank.TankMutationReport(
                        "drain", simulate, inspectFluidTankState(null), inspectFluidTankState(null),
                        amount, 0, 0, Math.max(0, amount), false)
                : tank.drainReport(amount, simulate);
        if (!simulate && report.changed()) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected HbmFluidTank.TankMutationReport setFluidTankTypeReport(HbmFluidTank tank, FluidType type) {
        HbmFluidTank.TankMutationReport report = tank == null
                ? new HbmFluidTank.TankMutationReport(
                        "type", false, inspectFluidTankState(null), inspectFluidTankState(null),
                        0, 0, 0, 0, false)
                : tank.setTankTypeReport(type);
        if (report.changed()) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected HbmFluidTank.TankMutationReport setFluidTankPressureReport(HbmFluidTank tank, int pressure) {
        HbmFluidTank.TankMutationReport report = tank == null
                ? new HbmFluidTank.TankMutationReport(
                        "pressure", false, inspectFluidTankState(null), inspectFluidTankState(null),
                        pressure, 0, 0, 0, false)
                : tank.withPressureReport(pressure);
        if (report.changed()) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected HbmFluidTank.TankMutationReport changeFluidTankSizeReport(HbmFluidTank tank, int maxFill) {
        HbmFluidTank.TankMutationReport report = tank == null
                ? new HbmFluidTank.TankMutationReport(
                        "capacity", false, inspectFluidTankState(null), inspectFluidTankState(null),
                        maxFill, 0, 0, 0, false)
                : tank.changeTankSizeReport(maxFill);
        if (report.changed()) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected FluidTankNbtBatchWriteReport writeFluidTanksToNbtReport(CompoundTag tag) {
        CompoundTag fluids = new CompoundTag();
        List<HbmFluidTank.TankNbtWriteReport> reports = new ArrayList<>();
        for (int i = 0; i < tanks.size(); i++) {
            reports.add(tanks.get(i).writeToNbtReport(fluids, TAG_TANK_PREFIX + i));
        }
        tag.put(TAG_FLUIDS, fluids);
        return new FluidTankNbtBatchWriteReport(TAG_FLUIDS, reports, inspectFluidTankSnapshot());
    }

    protected FluidTankNbtBatchReadReport readFluidTanksFromNbtReport(CompoundTag tag) {
        HbmFluidGuiHelper.TankSetSnapshot before = inspectFluidTankSnapshot();
        CompoundTag fluids = tag.getCompound(TAG_FLUIDS);
        List<HbmFluidTank.TankNbtReadReport> reports = new ArrayList<>();
        for (int i = 0; i < tanks.size(); i++) {
            reports.add(tanks.get(i).readFromNbtReport(fluids, TAG_TANK_PREFIX + i));
        }
        HbmFluidGuiHelper.TankSetSnapshot after = inspectFluidTankSnapshot();
        return new FluidTankNbtBatchReadReport(
                TAG_FLUIDS, reports, before, after, HbmFluidGuiHelper.diff(before, after));
    }

    protected HbmFluidRecipeIO.RecipeTankSetupReport setupRecipeFluidTanksReport(
            List<HbmFluidStack> inputStacks, List<HbmFluidStack> outputStacks,
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, int defaultCapacity) {
        HbmFluidRecipeIO.RecipeTankSetupReport report = HbmFluidRecipeIO.setupRecipeTanks(
                inputStacks, outputStacks, inputTanks, outputTanks, defaultCapacity);
        if (report.changed()) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected HbmFluidRecipeIO.TankConformReport conformRecipeFluidTankReport(
            HbmFluidTank tank, @Nullable HbmFluidStack stack, int defaultCapacity) {
        HbmFluidRecipeIO.TankConformReport report =
                HbmFluidRecipeIO.conformTankReport(tank, stack, defaultCapacity);
        if (report.changed()) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected HbmFluidRecipeIO.FluidStackSetCheckReport inspectRecipeFluidInputs(
            List<HbmFluidStack> requiredStacks, List<HbmFluidTank> inputTanks) {
        return HbmFluidRecipeIO.inspectInputs(requiredStacks, inputTanks);
    }

    protected HbmFluidRecipeIO.FluidStackSetCheckReport inspectRecipeFluidOutputs(
            List<HbmFluidStack> outputStacks, List<HbmFluidTank> outputTanks) {
        return HbmFluidRecipeIO.inspectOutputs(outputStacks, outputTanks);
    }

    protected HbmFluidRecipeIO.FluidStackSetTransferReport consumeRecipeFluidInputsReport(
            List<HbmFluidStack> requiredStacks, List<HbmFluidTank> inputTanks, boolean simulate) {
        HbmFluidRecipeIO.FluidStackSetTransferReport report =
                HbmFluidRecipeIO.consumeInputsReport(requiredStacks, inputTanks, simulate);
        if (!simulate && report.movedMb() > 0) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected HbmFluidRecipeIO.FluidStackSetTransferReport produceRecipeFluidOutputsReport(
            List<HbmFluidStack> outputStacks, List<HbmFluidTank> outputTanks, boolean simulate) {
        HbmFluidRecipeIO.FluidStackSetTransferReport report =
                HbmFluidRecipeIO.produceOutputsReport(outputStacks, outputTanks, simulate);
        if (!simulate && report.movedMb() > 0) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected HbmFluidRecipeIO.RecipeFluidIoCheckReport inspectRecipeFluidIo(
            List<HbmFluidStack> inputStacks, List<HbmFluidStack> outputStacks,
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks) {
        return HbmFluidRecipeIO.inspectRecipeIo(inputStacks, outputStacks, inputTanks, outputTanks);
    }

    protected HbmFluidRecipeIO.RecipeFluidIoProcessReport previewRecipeFluidIo(
            List<HbmFluidStack> inputStacks, List<HbmFluidStack> outputStacks,
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks) {
        return processRecipeFluidIoReport(inputStacks, outputStacks, inputTanks, outputTanks, true);
    }

    protected HbmFluidRecipeIO.RecipeFluidIoProcessReport processRecipeFluidIoReport(
            List<HbmFluidStack> inputStacks, List<HbmFluidStack> outputStacks,
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, boolean simulate) {
        HbmFluidRecipeIO.RecipeFluidIoProcessReport report =
                HbmFluidRecipeIO.processRecipeIoReport(inputStacks, outputStacks, inputTanks, outputTanks, simulate);
        if (!simulate && report.movedMb() > 0) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected ForgeRecipeFluidHandlerAdapter createRecipeForgeFluidHandler(
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, int inputPressure) {
        return ForgeRecipeFluidHandlerAdapter.create(inputTanks, outputTanks, inputPressure,
                this::onFluidContentsChanged);
    }

    protected RecipeForgeFluidHandlerView inspectRecipeForgeFluidHandler(
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, int inputPressure) {
        List<HbmFluidTank> safeInputs = safeTankList(inputTanks);
        List<HbmFluidTank> safeOutputs = safeTankList(outputTanks);
        return new RecipeForgeFluidHandlerView(
                HbmFluidGuiHelper.snapshotTanks(safeInputs),
                HbmFluidGuiHelper.snapshotTanks(safeOutputs),
                HbmFluidGuiHelper.snapshotTanks(mergeVisibleTanks(safeInputs, safeOutputs)),
                HbmFluidTank.clampPressure(inputPressure),
                !safeInputs.isEmpty(),
                !safeOutputs.isEmpty());
    }

    protected ForgeRecipeFluidHandlerAdapter.RecipeForgeFillReport previewRecipeForgeFluidFill(
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, int inputPressure, FluidStack stack) {
        return processRecipeForgeFluidFillReport(inputTanks, outputTanks, inputPressure, stack, true);
    }

    protected ForgeRecipeFluidHandlerAdapter.RecipeForgeFillReport processRecipeForgeFluidFillReport(
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, int inputPressure,
            FluidStack stack, boolean simulate) {
        return createRecipeForgeFluidHandler(inputTanks, outputTanks, inputPressure)
                .fillRecipeReport(stack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    protected ForgeRecipeFluidHandlerAdapter.RecipeForgeDrainReport previewRecipeForgeFluidDrain(
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, int inputPressure, FluidStack stack) {
        return processRecipeForgeFluidDrainReport(inputTanks, outputTanks, inputPressure, stack, true);
    }

    protected ForgeRecipeFluidHandlerAdapter.RecipeForgeDrainReport processRecipeForgeFluidDrainReport(
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, int inputPressure,
            FluidStack stack, boolean simulate) {
        return createRecipeForgeFluidHandler(inputTanks, outputTanks, inputPressure)
                .drainRecipeReport(stack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    protected ForgeRecipeFluidHandlerAdapter.RecipeForgeDrainReport previewRecipeForgeFluidDrain(
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, int inputPressure, int maxDrain) {
        return processRecipeForgeFluidDrainReport(inputTanks, outputTanks, inputPressure, maxDrain, true);
    }

    protected ForgeRecipeFluidHandlerAdapter.RecipeForgeDrainReport processRecipeForgeFluidDrainReport(
            List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks, int inputPressure,
            int maxDrain, boolean simulate) {
        return createRecipeForgeFluidHandler(inputTanks, outputTanks, inputPressure)
                .drainRecipeReport(maxDrain, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    public FluidSideAccessReport inspectFluidSideAccess(@Nullable Direction side) {
        HbmFluidSideMode mode = normalizeFluidSideMode(getFluidSideMode(side));
        List<HbmFluidTank> inputTanks = safeTankList(getInputTanks(side));
        List<HbmFluidTank> outputTanks = safeTankList(getOutputTanks(side));
        List<HbmFluidTank> visibleTanks = switch (mode) {
            case NONE -> List.of();
            case INPUT -> inputTanks;
            case OUTPUT -> outputTanks;
            case BOTH -> mergeVisibleTanks(inputTanks, outputTanks);
        };
        boolean handlerPresent = mode != HbmFluidSideMode.NONE;
        return new FluidSideAccessReport(
                side,
                mode,
                HbmFluidTank.clampPressure(getInputPressure(side)),
                handlerPresent,
                handlerPresent && mode.canFill(),
                handlerPresent && mode.canDrain(),
                HbmFluidGuiHelper.snapshotTanks(getAllTanks()),
                HbmFluidGuiHelper.snapshotTanks(inputTanks),
                HbmFluidGuiHelper.snapshotTanks(outputTanks),
                HbmFluidGuiHelper.snapshotTanks(visibleTanks),
                createFluidSideTankRoles(visibleTanks, inputTanks, outputTanks));
    }

    public ForgeFluidCapabilityView inspectForgeFluidCapability(@Nullable Direction side) {
        FluidSideAccessReport access = inspectFluidSideAccess(side);
        ForgeFluidHandlerAdapter adapter = createFluidHandlerAdapter(side);
        return new ForgeFluidCapabilityView(
                side,
                access.sideMode(),
                adapter != null,
                adapter == null ? ForgeFluidHandlerAdapter.emptySnapshot() : adapter.createSnapshot(),
                access);
    }

    public ForgeFluidCapabilityPreview previewForgeFluidFill(@Nullable Direction side, FluidStack stack) {
        ForgeFluidHandlerAdapter adapter = createFluidHandlerAdapter(side);
        HbmFluidSideMode mode = normalizeFluidSideMode(getFluidSideMode(side));
        ForgeFluidHandlerAdapter.AdapterSnapshot snapshot = adapter == null
                ? ForgeFluidHandlerAdapter.emptySnapshot()
                : adapter.createSnapshot();
        int filled = adapter == null ? 0 : adapter.previewFill(stack);
        return new ForgeFluidCapabilityPreview(side, mode, adapter != null, snapshot, filled, FluidStack.EMPTY);
    }

    public ForgeFluidHandlerAdapter.ForgeFillReport previewForgeFluidFillReport(
            @Nullable Direction side, FluidStack stack) {
        return processForgeFluidFillReport(side, stack, true);
    }

    public ForgeFluidHandlerAdapter.ForgeFillReport processForgeFluidFillReport(
            @Nullable Direction side, FluidStack stack, boolean simulate) {
        ForgeFluidHandlerAdapter adapter = createFluidHandlerAdapter(side);
        if (adapter == null) {
            boolean stackPresent = stack != null && !stack.isEmpty();
            return new ForgeFluidHandlerAdapter.ForgeFillReport(
                    simulate, false, stackPresent, false, HbmFluids.NONE,
                    stackPresent ? stack.getAmount() : 0, 0, List.of());
        }
        return adapter.fillReport(stack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    public ForgeFluidCapabilityPreview previewForgeFluidDrain(@Nullable Direction side, FluidStack stack) {
        ForgeFluidHandlerAdapter adapter = createFluidHandlerAdapter(side);
        HbmFluidSideMode mode = normalizeFluidSideMode(getFluidSideMode(side));
        ForgeFluidHandlerAdapter.AdapterSnapshot snapshot = adapter == null
                ? ForgeFluidHandlerAdapter.emptySnapshot()
                : adapter.createSnapshot();
        FluidStack drained = adapter == null ? FluidStack.EMPTY : adapter.previewDrain(stack);
        return new ForgeFluidCapabilityPreview(side, mode, adapter != null, snapshot, 0, drained);
    }

    public ForgeFluidHandlerAdapter.ForgeDrainReport previewForgeFluidDrainReport(
            @Nullable Direction side, FluidStack stack) {
        return processForgeFluidDrainReport(side, stack, true);
    }

    public ForgeFluidHandlerAdapter.ForgeDrainReport processForgeFluidDrainReport(
            @Nullable Direction side, FluidStack stack, boolean simulate) {
        ForgeFluidHandlerAdapter adapter = createFluidHandlerAdapter(side);
        if (adapter == null) {
            boolean stackPresent = stack != null && !stack.isEmpty();
            return new ForgeFluidHandlerAdapter.ForgeDrainReport(
                    simulate, false, stackPresent, false, HbmFluids.NONE,
                    stackPresent ? stack.getAmount() : 0, 0, FluidStack.EMPTY, List.of());
        }
        return adapter.drainReport(stack, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    public ForgeFluidCapabilityPreview previewForgeFluidDrain(@Nullable Direction side, int maxDrain) {
        ForgeFluidHandlerAdapter adapter = createFluidHandlerAdapter(side);
        HbmFluidSideMode mode = normalizeFluidSideMode(getFluidSideMode(side));
        ForgeFluidHandlerAdapter.AdapterSnapshot snapshot = adapter == null
                ? ForgeFluidHandlerAdapter.emptySnapshot()
                : adapter.createSnapshot();
        FluidStack drained = adapter == null ? FluidStack.EMPTY : adapter.previewDrain(maxDrain);
        return new ForgeFluidCapabilityPreview(side, mode, adapter != null, snapshot, 0, drained);
    }

    public ForgeFluidHandlerAdapter.ForgeDrainReport previewForgeFluidDrainReport(
            @Nullable Direction side, int maxDrain) {
        return processForgeFluidDrainReport(side, maxDrain, true);
    }

    public ForgeFluidHandlerAdapter.ForgeDrainReport processForgeFluidDrainReport(
            @Nullable Direction side, int maxDrain, boolean simulate) {
        ForgeFluidHandlerAdapter adapter = createFluidHandlerAdapter(side);
        if (adapter == null) {
            return new ForgeFluidHandlerAdapter.ForgeDrainReport(
                    simulate, false, maxDrain > 0, false, HbmFluids.NONE,
                    Math.max(0, maxDrain), 0, FluidStack.EMPTY, List.of());
        }
        return adapter.drainReport(maxDrain, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    protected boolean processFluidItemTransfers(IItemHandlerModifiable items, Iterable<TankSlotTransfer> transfers) {
        return processFluidItemTransfersReport(items, transfers).moved();
    }

    protected TankSlotTransferResult loadFluidTankFromSlotReport(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank) {
        return loadFluidTankFromSlotReport(items, inputSlot, outputSlot, tank, Integer.MAX_VALUE, false);
    }

    protected TankSlotTransferResult loadFluidTankFromSlotReport(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank,
            int maxAmount, boolean simulate) {
        TankSlotTransferResult report =
                HbmFluidItemTransfer.loadTankFromSlotReport(items, inputSlot, outputSlot, tank, maxAmount, simulate);
        if (!simulate && report.moved()) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected TankSlotTransferResult previewLoadFluidTankFromSlot(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank) {
        return HbmFluidItemTransfer.previewLoadTankFromSlot(items, inputSlot, outputSlot, tank);
    }

    protected TankSlotTransferResult unloadFluidTankToSlotReport(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank) {
        return unloadFluidTankToSlotReport(items, inputSlot, outputSlot, tank, Integer.MAX_VALUE, false);
    }

    protected TankSlotTransferResult unloadFluidTankToSlotReport(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank,
            int maxAmount, boolean simulate) {
        TankSlotTransferResult report =
                HbmFluidItemTransfer.unloadTankToSlotReport(items, inputSlot, outputSlot, tank, maxAmount, simulate);
        if (!simulate && report.moved()) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected TankSlotTransferResult previewUnloadFluidTankToSlot(
            IItemHandlerModifiable items, int inputSlot, int outputSlot, HbmFluidTank tank) {
        return HbmFluidItemTransfer.previewUnloadTankToSlot(items, inputSlot, outputSlot, tank);
    }

    protected HbmFluidContainerRegistry.EmptyContainerLookupReport inspectFluidFullContainerFor(
            ItemStack emptyStack, FluidType type) {
        return HbmFluidContainerRegistry.inspectFullContainerFor(emptyStack, type);
    }

    protected HbmFluidContainerRegistry.FluidContentLookupReport inspectFluidContentContainer(
            ItemStack fullStack, FluidType type) {
        return HbmFluidContainerRegistry.inspectFluidContent(fullStack, type);
    }

    protected HbmFluidContainerRegistry.FluidTypeLookupReport inspectFluidContainerType(ItemStack fullStack) {
        return HbmFluidContainerRegistry.inspectFluidType(fullStack);
    }

    protected HbmFluidContainerRegistry.FullContainerLookupReport inspectFluidEmptyContainerFor(ItemStack fullStack) {
        return HbmFluidContainerRegistry.inspectEmptyContainerFor(fullStack);
    }

    protected TransferBatchReport processFluidItemTransfersReport(IItemHandlerModifiable items,
            Iterable<TankSlotTransfer> transfers) {
        return processFluidItemTransfersReport(items, transfers, false);
    }

    protected TransferBatchReport processFluidItemTransfersReport(IItemHandlerModifiable items,
            Iterable<TankSlotTransfer> transfers, boolean simulate) {
        TransferBatchReport report = HbmFluidItemTransfer.processTransferReport(items, transfers, simulate);
        if (!simulate && report.moved()) {
            onFluidContentsChanged();
        }
        return report;
    }

    protected boolean setFluidTankTypeFromIdentifierSlot(IItemHandlerModifiable items, int inputSlot,
            HbmFluidTank tank) {
        return setFluidTankTypeFromIdentifierSlotReport(items, inputSlot, inputSlot, tank, 0, false).changed();
    }

    protected boolean setFluidTankTypeFromIdentifierSlot(IItemHandlerModifiable items, int inputSlot, int outputSlot,
            HbmFluidTank tank) {
        return setFluidTankTypeFromIdentifierSlotReport(items, inputSlot, outputSlot, tank, 0, false).changed();
    }

    protected boolean setFluidTankTypeFromIdentifierSlot(IItemHandlerModifiable items, int inputSlot,
            HbmFluidTank tank, int pressure, boolean forcePressure) {
        return setFluidTankTypeFromIdentifierSlotReport(items, inputSlot, inputSlot, tank, pressure, forcePressure)
                .changed();
    }

    protected boolean setFluidTankTypeFromIdentifierSlot(IItemHandlerModifiable items, int inputSlot, int outputSlot,
            HbmFluidTank tank, int pressure, boolean forcePressure) {
        return setFluidTankTypeFromIdentifierSlotReport(items, inputSlot, outputSlot, tank, pressure, forcePressure)
                .changed();
    }

    protected FluidIdentifierSlotReport setFluidTankTypeFromIdentifierSlotReport(IItemHandlerModifiable items,
            int inputSlot, HbmFluidTank tank) {
        return setFluidTankTypeFromIdentifierSlotReport(items, inputSlot, inputSlot, tank, 0, false);
    }

    protected FluidIdentifierSlotReport setFluidTankTypeFromIdentifierSlotReport(IItemHandlerModifiable items,
            int inputSlot, int outputSlot, HbmFluidTank tank) {
        return setFluidTankTypeFromIdentifierSlotReport(items, inputSlot, outputSlot, tank, 0, false);
    }

    protected FluidIdentifierSlotReport setFluidTankTypeFromIdentifierSlotReport(IItemHandlerModifiable items,
            int inputSlot, HbmFluidTank tank, int pressure, boolean forcePressure) {
        return setFluidTankTypeFromIdentifierSlotReport(items, inputSlot, inputSlot, tank, pressure, forcePressure);
    }

    protected FluidIdentifierSlotReport setFluidTankTypeFromIdentifierSlotReport(IItemHandlerModifiable items,
            int inputSlot, int outputSlot, HbmFluidTank tank, int pressure, boolean forcePressure) {
        FluidIdentifierSlotReport report = HbmFluidItemTransfer.setTankTypeFromIdentifierSlotReport(
                items, inputSlot, outputSlot, tank, level, worldPosition, pressure, forcePressure);
        if (report.changed()) {
            onFluidContentsChanged();
        }
        return report;
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
    public void setRemoved() {
        detachTrackedFluidPortsOnLifecycle();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        detachTrackedFluidPortsOnLifecycle();
        super.onChunkUnloaded();
    }

    private void detachTrackedFluidPortsOnLifecycle() {
        com.hbm.ntm.fluid.HbmFluidReceiver receiver = this instanceof com.hbm.ntm.fluid.HbmFluidReceiver fluidReceiver
                ? fluidReceiver
                : null;
        com.hbm.ntm.fluid.HbmFluidProvider provider = this instanceof com.hbm.ntm.fluid.HbmFluidProvider fluidProvider
                ? fluidProvider
                : null;
        detachTrackedFluidPortsReport(receiver, provider);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        writeFluidTanksToNbtReport(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        readFluidTanksFromNbtReport(tag);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(getClientSyncTag());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            handleClientSyncTag(tag);
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
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
            handleClientSyncTag(tag);
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        handleClientSyncTag(tag);
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
            HbmMachinePerformanceCounters.blockUpdate();
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
        HbmFluidSideMode mode = normalizeFluidSideMode(getFluidSideMode(side));
        return mode == HbmFluidSideMode.NONE
                ? LazyOptional.empty()
                : LazyOptional.of(() -> createFluidHandlerAdapter(side, mode));
    }

    @Nullable
    private ForgeFluidHandlerAdapter createFluidHandlerAdapter(@Nullable Direction side) {
        return createFluidHandlerAdapter(side, normalizeFluidSideMode(getFluidSideMode(side)));
    }

    @Nullable
    private ForgeFluidHandlerAdapter createFluidHandlerAdapter(@Nullable Direction side, HbmFluidSideMode mode) {
        if (mode == null || mode == HbmFluidSideMode.NONE) {
            return null;
        }
        if (mode == HbmFluidSideMode.BOTH) {
            return new ForgeFluidHandlerAdapter(
                    getInputTanks(side),
                    getOutputTanks(side),
                    getInputPressure(side),
                    true,
                    true,
                    this::onFluidContentsChanged);
        }
        List<HbmFluidTank> inputTanks = mode == HbmFluidSideMode.INPUT ? getInputTanks(side) : List.of();
        List<HbmFluidTank> outputTanks = mode == HbmFluidSideMode.OUTPUT ? getOutputTanks(side) : List.of();
        return new ForgeFluidHandlerAdapter(
                inputTanks,
                outputTanks,
                getInputPressure(side),
                mode.canFill(),
                mode.canDrain(),
                this::onFluidContentsChanged);
    }

    protected int getInputPressure(@Nullable Direction side) {
        return 0;
    }

    private static HbmFluidSideMode normalizeFluidSideMode(HbmFluidSideMode mode) {
        return mode == null ? HbmFluidSideMode.NONE : mode;
    }

    private static List<HbmFluidTank> safeTankList(List<HbmFluidTank> tanks) {
        if (tanks == null || tanks.isEmpty()) {
            return List.of();
        }
        List<HbmFluidTank> result = new ArrayList<>();
        for (HbmFluidTank tank : tanks) {
            if (tank != null) {
                result.add(tank);
            }
        }
        return List.copyOf(result);
    }

    private static List<HbmFluidTank> mergeVisibleTanks(List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks) {
        List<HbmFluidTank> visible = new ArrayList<>();
        for (HbmFluidTank tank : inputTanks) {
            if (!visible.contains(tank)) {
                visible.add(tank);
            }
        }
        for (HbmFluidTank tank : outputTanks) {
            if (!visible.contains(tank)) {
                visible.add(tank);
            }
        }
        return List.copyOf(visible);
    }

    private static List<FluidSideTankRole> createFluidSideTankRoles(
            List<HbmFluidTank> visibleTanks, List<HbmFluidTank> inputTanks, List<HbmFluidTank> outputTanks) {
        List<FluidSideTankRole> roles = new ArrayList<>();
        for (int i = 0; i < visibleTanks.size(); i++) {
            HbmFluidTank tank = visibleTanks.get(i);
            int inputIndex = inputTanks.indexOf(tank);
            int outputIndex = outputTanks.indexOf(tank);
            roles.add(new FluidSideTankRole(
                    i,
                    inputIndex,
                    outputIndex,
                    inputIndex >= 0,
                    outputIndex >= 0,
                    HbmFluidGuiHelper.snapshot(i, tank)));
        }
        return List.copyOf(roles);
    }

    public record FluidTankNbtBatchWriteReport(
            String rootKey,
            List<HbmFluidTank.TankNbtWriteReport> tanks,
            HbmFluidGuiHelper.TankSetSnapshot writtenSnapshot) {
        public FluidTankNbtBatchWriteReport {
            rootKey = rootKey == null ? "" : rootKey;
            tanks = tanks == null ? List.of() : List.copyOf(tanks);
            writtenSnapshot = writtenSnapshot == null ? HbmFluidGuiHelper.snapshotTanks(List.of()) : writtenSnapshot;
        }

        public int tankCount() {
            return tanks.size();
        }
    }

    public record FluidTankNbtBatchReadReport(
            String rootKey,
            List<HbmFluidTank.TankNbtReadReport> tanks,
            HbmFluidGuiHelper.TankSetSnapshot before,
            HbmFluidGuiHelper.TankSetSnapshot after,
            HbmFluidGuiHelper.TankSetDiff diff) {
        public FluidTankNbtBatchReadReport {
            rootKey = rootKey == null ? "" : rootKey;
            tanks = tanks == null ? List.of() : List.copyOf(tanks);
            before = before == null ? HbmFluidGuiHelper.snapshotTanks(List.of()) : before;
            after = after == null ? HbmFluidGuiHelper.snapshotTanks(List.of()) : after;
            diff = diff == null ? HbmFluidGuiHelper.diff(before, after) : diff;
        }

        public int tankCount() {
            return tanks.size();
        }

        public boolean changed() {
            return diff.changed();
        }
    }

    public record RecipeForgeFluidHandlerView(
            HbmFluidGuiHelper.TankSetSnapshot inputTanks,
            HbmFluidGuiHelper.TankSetSnapshot outputTanks,
            HbmFluidGuiHelper.TankSetSnapshot visibleTanks,
            int inputPressure,
            boolean canFill,
            boolean canDrain) {
        public RecipeForgeFluidHandlerView {
            inputTanks = inputTanks == null ? HbmFluidGuiHelper.snapshotTanks(List.of()) : inputTanks;
            outputTanks = outputTanks == null ? HbmFluidGuiHelper.snapshotTanks(List.of()) : outputTanks;
            visibleTanks = visibleTanks == null ? HbmFluidGuiHelper.snapshotTanks(List.of()) : visibleTanks;
            inputPressure = HbmFluidTank.clampPressure(inputPressure);
        }
    }

    public record FluidSideAccessReport(
            @Nullable Direction side,
            HbmFluidSideMode sideMode,
            int inputPressure,
            boolean handlerPresent,
            boolean canFill,
            boolean canDrain,
            HbmFluidGuiHelper.TankSetSnapshot allTanks,
            HbmFluidGuiHelper.TankSetSnapshot inputTanks,
            HbmFluidGuiHelper.TankSetSnapshot outputTanks,
            HbmFluidGuiHelper.TankSetSnapshot visibleTanks,
            List<FluidSideTankRole> tankRoles) {
        public FluidSideAccessReport {
            sideMode = normalizeFluidSideMode(sideMode);
            inputPressure = HbmFluidTank.clampPressure(inputPressure);
            allTanks = allTanks == null ? HbmFluidGuiHelper.snapshotTanks(List.of()) : allTanks;
            inputTanks = inputTanks == null ? HbmFluidGuiHelper.snapshotTanks(List.of()) : inputTanks;
            outputTanks = outputTanks == null ? HbmFluidGuiHelper.snapshotTanks(List.of()) : outputTanks;
            visibleTanks = visibleTanks == null ? HbmFluidGuiHelper.snapshotTanks(List.of()) : visibleTanks;
            tankRoles = tankRoles == null ? List.of() : List.copyOf(tankRoles);
        }
    }

    public record FluidSideTankRole(
            int visibleTankIndex,
            int inputTankIndex,
            int outputTankIndex,
            boolean input,
            boolean output,
            HbmFluidGuiHelper.TankSnapshot tank) {
        public FluidSideTankRole {
            inputTankIndex = input ? inputTankIndex : -1;
            outputTankIndex = output ? outputTankIndex : -1;
            tank = tank == null ? HbmFluidGuiHelper.snapshot(visibleTankIndex, null) : tank;
        }
    }

    public record ForgeFluidCapabilityView(
            @Nullable Direction side,
            HbmFluidSideMode sideMode,
            boolean handlerPresent,
            ForgeFluidHandlerAdapter.AdapterSnapshot snapshot,
            FluidSideAccessReport sideAccess) {
        public ForgeFluidCapabilityView {
            sideMode = normalizeFluidSideMode(sideMode);
            snapshot = snapshot == null ? ForgeFluidHandlerAdapter.emptySnapshot() : snapshot;
            sideAccess = sideAccess == null
                    ? new FluidSideAccessReport(
                            side,
                            sideMode,
                            0,
                            handlerPresent,
                            handlerPresent && sideMode.canFill(),
                            handlerPresent && sideMode.canDrain(),
                            HbmFluidGuiHelper.snapshotTanks(List.of()),
                            HbmFluidGuiHelper.snapshotTanks(List.of()),
                            HbmFluidGuiHelper.snapshotTanks(List.of()),
                            HbmFluidGuiHelper.snapshotTanks(List.of()),
                            List.of())
                    : sideAccess;
        }

        public boolean canFill() {
            return handlerPresent && snapshot.canFill();
        }

        public boolean canDrain() {
            return handlerPresent && snapshot.canDrain();
        }
    }

    public record ForgeFluidCapabilityPreview(
            @Nullable Direction side,
            HbmFluidSideMode sideMode,
            boolean handlerPresent,
            ForgeFluidHandlerAdapter.AdapterSnapshot snapshot,
            int filledAmount,
            FluidStack drainedStack) {
        public int drainedAmount() {
            return drainedStack == null || drainedStack.isEmpty() ? 0 : drainedStack.getAmount();
        }

        public boolean moved() {
            return filledAmount > 0 || drainedAmount() > 0;
        }
    }
}
