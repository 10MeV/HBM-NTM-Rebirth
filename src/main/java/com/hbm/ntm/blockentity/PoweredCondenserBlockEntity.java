package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.config.PoweredCondenserConfig;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PoweredCondenserBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmStandardFluidReceiver, HbmStandardFluidSender {
    private static final int LEGACY_POWER_GATE_HE_PER_MB = 10;

    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank;
    private int age;
    private int waterTimer;
    private int throughput;
    private float spin;
    private float lastSpin;

    public PoweredCondenserBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.SPENTSTEAM, PoweredCondenserConfig.inputTankSize()),
                new HbmFluidTank(HbmFluids.WATER, PoweredCondenserConfig.outputTankSize()));
    }

    private PoweredCondenserBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank,
            HbmFluidTank outputTank) {
        super(ModBlockEntities.POWERED_CONDENSER.get(), pos, state,
                new HbmEnergyStorage(PoweredCondenserConfig.maxPower(), PoweredCondenserConfig.maxPower(), 0L),
                List.of(inputTank, outputTank));
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PoweredCondenserBlockEntity condenser) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, condenser);

        long oldPower = condenser.energy.getPower();
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
        if (convert > 0 && condenser.energy.getPower() >= (long) convert * LEGACY_POWER_GATE_HE_PER_MB) {
            condenser.inputTank.drain(convert, false);
            condenser.outputTank.fill(HbmFluids.WATER, convert, 0, false);
            condenser.energy.setPower(Math.max(0L,
                    condenser.energy.getPower() - (long) convert * PoweredCondenserConfig.powerConsumption()));
            condenser.waterTimer = 20;
            condenser.onFluidContentsChanged();
        }
        if (condenser.outputTank.getFill() > 0) {
            condenser.tryProvideFluidToPorts(condenser.outputTank.getTankType(), condenser.outputTank.getPressure(),
                    condenser);
        }

        boolean changed = oldPower != condenser.energy.getPower()
                || oldInput != condenser.inputTank.getFill()
                || oldOutput != condenser.outputTank.getFill()
                || oldWaterTimer != condenser.waterTimer;
        if (changed) {
            condenser.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        condenser.networkPackNT(150);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PoweredCondenserBlockEntity condenser) {
        if (!level.isClientSide) {
            return;
        }
        condenser.lastSpin = condenser.spin;
        if (condenser.waterTimer <= 0) {
            return;
        }
        condenser.spin += 30.0F;
        if (condenser.spin >= 360.0F) {
            condenser.spin -= 360.0F;
            condenser.lastSpin -= 360.0F;
        }
        if (level.getGameTime() % 4L == 0L) {
            Direction facing = condenser.facing();
            level.addParticle(ParticleTypes.CLOUD,
                    pos.getX() + 0.5D + facing.getStepX() * 1.5D,
                    pos.getY() + 1.5D,
                    pos.getZ() + 0.5D + facing.getStepZ() * 1.5D,
                    facing.getStepX() * 0.1D, 0.0D, facing.getStepZ() * 0.1D);
            level.addParticle(ParticleTypes.CLOUD,
                    pos.getX() + 0.5D - facing.getStepX() * 1.5D,
                    pos.getY() + 1.5D,
                    pos.getZ() + 0.5D - facing.getStepZ() * 1.5D,
                    facing.getStepX() * -0.1D, 0.0D, facing.getStepZ() * -0.1D);
        }
    }

    public int getWaterTimer() {
        return waterTimer;
    }

    public int getThroughput() {
        return throughput;
    }

    public float getFanSpin(float partialTick) {
        return lastSpin + (spin - lastSpin) * partialTick;
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
        return condenserPorts(facing());
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
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
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        List<EnergyPort> ports = new ArrayList<>();
        for (FluidPort port : condenserPorts(facing())) {
            ports.add(new EnergyPort(port.offset(), port.direction()));
        }
        return List.copyOf(ports);
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<net.minecraft.network.chat.Component> lines = new ArrayList<>();
        lines.add(net.minecraft.network.chat.Component.literal(
                LegacyLookOverlayLines.shortNumber(energy.getPower()) + "HE / "
                        + LegacyLookOverlayLines.shortNumber(energy.getMaxPower()) + "HE"));
        lines.add(LegacyLookOverlayLines.groupedCompactTank(true, inputTank));
        lines.add(LegacyLookOverlayLines.groupedCompactTank(false, outputTank));
        return LegacyLookOverlay.forBlock(this, lines);
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putDouble(CompatEnergyControl.D_CONSUMPTION_MB, throughput);
        data.putDouble(CompatEnergyControl.D_OUTPUT_MB, throughput);
        data.putLong(CompatEnergyControl.D_CONSUMPTION_HE,
                (long) throughput * PoweredCondenserConfig.powerConsumption());
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("power", energy.getPower());
        inputTank.writeToNbt(tag, "water");
        outputTank.writeToNbt(tag, "steam");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        inputTank.setTankType(HbmFluids.SPENTSTEAM);
        outputTank.setTankType(HbmFluids.WATER);
        normalizeConfiguredLimits();
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
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
        tag.putFloat("spin", spin);
        tag.putFloat("lastSpin", lastSpin);
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
        if (tag.contains("spin")) {
            spin = tag.getFloat("spin");
        }
        if (tag.contains("lastSpin")) {
            lastSpin = tag.getFloat("lastSpin");
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private void normalizeConfiguredLimits() {
        long maxPower = PoweredCondenserConfig.maxPower();
        energy.setMaxPower(maxPower);
        energy.setTransferRates(maxPower, 0L);
        inputTank.changeTankSize(PoweredCondenserConfig.inputTankSize());
        outputTank.changeTankSize(PoweredCondenserConfig.outputTankSize());
    }

    private static List<FluidPort> condenserPorts(Direction facing) {
        Direction rot = facing.getClockWise();
        return HbmFluidPortLayouts.legacy(facing, rot,
                HbmFluidPortLayouts.LegacyPort.of(0, 4, 1, rot),
                HbmFluidPortLayouts.LegacyPort.of(0, -4, 1, rot.getOpposite()),
                HbmFluidPortLayouts.LegacyPort.of(2, -1, 1, facing),
                HbmFluidPortLayouts.LegacyPort.of(2, 1, 1, facing),
                HbmFluidPortLayouts.LegacyPort.of(-2, -1, 1, facing.getOpposite()),
                HbmFluidPortLayouts.LegacyPort.of(-2, 1, 1, facing.getOpposite()));
    }
}
