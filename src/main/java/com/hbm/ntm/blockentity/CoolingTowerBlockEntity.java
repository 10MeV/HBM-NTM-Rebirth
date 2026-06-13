package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.config.HbmClientConfig;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.particle.ParticleUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class CoolingTowerBlockEntity extends HbmFluidNetworkBlockEntity
        implements HbmStandardFluidReceiver, HbmStandardFluidSender {
    protected final HbmFluidTank inputTank;
    protected final HbmFluidTank outputTank;
    private int age;
    private int waterTimer;
    private int throughput;

    protected CoolingTowerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            int inputCapacity, int outputCapacity) {
        this(type, pos, state, new HbmFluidTank(HbmFluids.SPENTSTEAM, inputCapacity),
                new HbmFluidTank(HbmFluids.WATER, outputCapacity));
    }

    private CoolingTowerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            HbmFluidTank inputTank, HbmFluidTank outputTank) {
        super(type, pos, state, List.of(inputTank, outputTank));
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    protected static void tickTower(Level level, BlockPos pos, BlockState state, CoolingTowerBlockEntity tower) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, tower);
        tower.age = (tower.age + 1) % 2;
        if (tower.waterTimer > 0) {
            tower.waterTimer--;
        }
        tower.inputTank.setTankType(HbmFluids.SPENTSTEAM);
        tower.outputTank.setTankType(HbmFluids.WATER);
        tower.normalizeConfigCapacity();

        int convert = Math.min(tower.inputTank.getFill(), tower.outputTank.getSpace());
        tower.throughput = convert;
        if (convert > 0) {
            tower.inputTank.drain(convert, false);
            tower.outputTank.fill(HbmFluids.WATER, convert, 0, false);
            tower.waterTimer = 20;
            tower.onFluidContentsChanged();
        }
        if (tower.outputTank.getFill() > 0) {
            tower.tryProvideFluidToPorts(tower.outputTank.getTankType(), tower.outputTank.getPressure(), tower);
        }

        if (convert > 0 || level.getGameTime() % 20L == 0L) {
            tower.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        tower.networkPackNT(150);
    }

    protected static void tickSmallTowerClient(Level level, BlockPos pos, CoolingTowerBlockEntity tower) {
        if (!shouldSpawnCoolingTowerParticles(level, tower, 2L)) {
            return;
        }
        ParticleUtil.spawnSmallCoolingTowerSteam(level, pos);
    }

    protected static void tickLargeTowerClient(Level level, BlockPos pos, CoolingTowerBlockEntity tower) {
        if (!shouldSpawnCoolingTowerParticles(level, tower, 4L)) {
            return;
        }
        ParticleUtil.spawnLargeCoolingTowerSteam(level, pos);
    }

    private static boolean shouldSpawnCoolingTowerParticles(Level level, CoolingTowerBlockEntity tower, long interval) {
        return level.isClientSide
                && HbmClientConfig.coolingTowerParticles()
                && tower.waterTimer > 0
                && level.getGameTime() % interval == 0L;
    }

    public HbmFluidTank getInputTank() {
        return inputTank;
    }

    public HbmFluidTank getOutputTank() {
        return outputTank;
    }

    public int getAge() {
        return age;
    }

    public int getWaterTimer() {
        return waterTimer;
    }

    public int getThroughput() {
        return throughput;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, LegacyLookOverlayLines.allCompactFluidUserTanks(this));
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
        return ports();
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
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("age", age);
        tag.putInt("waterTimer", waterTimer);
        tag.putInt("throughput", throughput);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        normalizeConfigCapacity();
        age = Math.floorMod(tag.getInt("age"), 2);
        waterTimer = Math.max(0, tag.getInt("waterTimer"));
        throughput = Math.max(0, tag.getInt("throughput"));
    }

    protected void normalizeConfigCapacity() {
        inputTank.changeTankSize(configuredInputCapacity());
        outputTank.changeTankSize(configuredOutputCapacity());
    }

    protected abstract int configuredInputCapacity();

    protected abstract int configuredOutputCapacity();

    protected abstract Iterable<FluidPort> ports();

    protected static List<FluidPort> cardinalPorts(int radius) {
        return List.of(
                FluidPort.of(radius, 0, 0, Direction.EAST),
                FluidPort.of(-radius, 0, 0, Direction.WEST),
                FluidPort.of(0, 0, radius, Direction.SOUTH),
                FluidPort.of(0, 0, -radius, Direction.NORTH));
    }

    protected static List<FluidPort> largeTowerPorts() {
        List<FluidPort> ports = new ArrayList<>();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            Direction rot = direction.getClockWise();
            ports.add(FluidPort.of(direction.getStepX() * 5, 0,
                    direction.getStepZ() * 5, direction));
            ports.add(FluidPort.of(direction.getStepX() * 5 + rot.getStepX() * 3, 0,
                    direction.getStepZ() * 5 + rot.getStepZ() * 3, direction));
            ports.add(FluidPort.of(direction.getStepX() * 5 - rot.getStepX() * 3, 0,
                    direction.getStepZ() * 5 - rot.getStepZ() * 3, direction));
        }
        return List.copyOf(ports);
    }
}
