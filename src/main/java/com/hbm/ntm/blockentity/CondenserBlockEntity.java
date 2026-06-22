package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.tile.IInfoProviderEC;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.config.CondenserConfig;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.registry.ModBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CondenserBlockEntity extends HbmFluidNetworkBlockEntity
        implements HbmStandardFluidReceiver, HbmStandardFluidSender, IInfoProviderEC {
    private static final List<FluidPort> FLUID_PORTS = HbmFluidPortLayouts.allAdjacent();

    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank;
    private int age;
    private int waterTimer;
    private int throughput;

    public CondenserBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.SPENTSTEAM, CondenserConfig.inputTankSize()),
                new HbmFluidTank(HbmFluids.WATER, CondenserConfig.outputTankSize()));
    }

    private CondenserBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank, HbmFluidTank outputTank) {
        super(ModBlockEntities.CONDENSER.get(), pos, state, List.of(inputTank, outputTank));
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CondenserBlockEntity condenser) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, condenser);

        int oldInput = condenser.inputTank.getFill();
        int oldOutput = condenser.outputTank.getFill();
        int oldWaterTimer = condenser.waterTimer;

        condenser.age = (condenser.age + 1) % 2;
        if (condenser.waterTimer > 0) {
            condenser.waterTimer--;
        }
        condenser.inputTank.setTankType(HbmFluids.SPENTSTEAM);
        condenser.outputTank.setTankType(HbmFluids.WATER);
        condenser.normalizeConfiguredLimits();

        int convert = Math.min(condenser.inputTank.getFill(), condenser.outputTank.getSpace());
        condenser.throughput = convert;
        if (convert > 0) {
            condenser.inputTank.drain(convert, false);
            condenser.outputTank.fill(HbmFluids.WATER, convert, 0, false);
            condenser.waterTimer = 20;
            condenser.onFluidContentsChanged();
        }
        if (condenser.outputTank.getFill() > 0) {
            condenser.tryProvideFluidToPorts(condenser.outputTank.getTankType(), condenser.outputTank.getPressure(),
                    condenser);
        }

        boolean changed = oldInput != condenser.inputTank.getFill()
                || oldOutput != condenser.outputTank.getFill()
                || oldWaterTimer != condenser.waterTimer;
        if (changed || level.getGameTime() % 20L == 0L) {
            condenser.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        condenser.networkPackNT(150);
    }

    public int getWaterTimer() {
        return waterTimer;
    }

    public int getThroughput() {
        return throughput;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(inputTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(outputTank);
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long leftover = HbmStandardFluidReceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidSender.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == inputTank.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == outputTank.getTankType() && outputTank.getFill() > 0;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of(inputTank);
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(outputTank);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<net.minecraft.network.chat.Component> lines = new ArrayList<>(
                LegacyLookOverlayLines.allCompactFluidUserTanks(this));
        return LegacyLookOverlay.forBlock(this, lines);
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        data.putDouble(CompatEnergyControl.D_CONSUMPTION_MB, throughput);
        data.putDouble(CompatEnergyControl.D_OUTPUT_MB, throughput);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        inputTank.writeToNbt(tag, "water");
        outputTank.writeToNbt(tag, "steam");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inputTank.setTankType(HbmFluids.SPENTSTEAM);
        outputTank.setTankType(HbmFluids.WATER);
        normalizeConfiguredLimits();
        if (tag.contains("water")) {
            inputTank.readFromNbt(tag, "water");
        }
        if (tag.contains("steam")) {
            outputTank.readFromNbt(tag, "steam");
        }
        readRuntimeSync(tag);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putInt("age", age);
        tag.putInt("waterTimer", waterTimer);
        tag.putInt("throughput", throughput);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        readRuntimeSync(tag);
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

    private void readRuntimeSync(CompoundTag tag) {
        if (tag.contains("age")) {
            age = Math.floorMod(tag.getInt("age"), 2);
        }
        if (tag.contains("waterTimer")) {
            waterTimer = Math.max(0, tag.getInt("waterTimer"));
        }
        if (tag.contains("throughput")) {
            throughput = Math.max(0, tag.getInt("throughput"));
        }
    }

    private void normalizeConfiguredLimits() {
        inputTank.changeTankSize(CondenserConfig.inputTankSize());
        outputTank.changeTankSize(CondenserConfig.outputTankSize());
    }
}
