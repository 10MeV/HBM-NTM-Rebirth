package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidThermalExchange;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait.HeatingType;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.ParticleTypes;
import org.jetbrains.annotations.Nullable;

public class HephaestusBlockEntity extends HbmFluidBlockEntity implements HbmStandardFluidTransceiver {
    private static final int TANK_CAPACITY = 24_000;
    private static final int HEAT_LAYER_COUNT = 10;
    private static final int HEAT_SCAN_RANGE = 7;
    private static final String TAG_BUFFERED_HEAT = "bufferedHeat";
    private static final String TAG_FISSURE_SCAN_TIME = "fissureScanTime";
    private static final String TAG_HEAT = "Heat";
    private static final String TAG_ROT = "rot";
    private static final String TAG_PREV_ROT = "prevRot";
    private static final List<FluidPort> FLUID_PORTS = List.copyOf(new ArrayList<>() {{
        addAll(HbmFluidPortLayouts.cardinal(2, 0));
        addAll(HbmFluidPortLayouts.cardinal(2, 11));
    }});

    private final HbmFluidTank inputTank;
    private final HbmFluidTank outputTank;
    private final int[] heat = new int[HEAT_LAYER_COUNT];
    private long fissureScanTime = Long.MIN_VALUE;
    private int bufferedHeat;
    private float rot;
    private float prevRot;
    private Object audioLoop;

    public HephaestusBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.OIL, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.HOTOIL, TANK_CAPACITY));
    }

    private HephaestusBlockEntity(BlockPos pos, BlockState state, HbmFluidTank inputTank, HbmFluidTank outputTank) {
        super(ModBlockEntities.HEPHAESTUS.get(), pos, state, List.of(inputTank, outputTank));
        this.inputTank = inputTank;
        this.outputTank = outputTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HephaestusBlockEntity hephaestus) {
        if (level.isClientSide) {
            return;
        }

        int oldBufferedHeat = hephaestus.bufferedHeat;
        int oldInputFill = hephaestus.inputTank.getFill();
        int oldOutputFill = hephaestus.outputTank.getFill();
        FluidType oldInputType = hephaestus.inputTank.getTankType();
        FluidType oldOutputType = hephaestus.outputTank.getTankType();

        boolean changed = hephaestus.setupTanks();
        if (level.getGameTime() % 20L == 0L) {
            hephaestus.refreshTrackedTransceiverFluidPortsReport(
                    hephaestus.getReceivingTanks(), hephaestus.getSendingTanks(), hephaestus);
        }

        hephaestus.scanHeatLayer(level);
        HbmFluidThermalExchange.ThermalResult result = HbmFluidThermalExchange.heat(
                hephaestus.inputTank, hephaestus.outputTank, HeatingType.HEATEXCHANGER,
                hephaestus.getTotalHeat(), false);
        if (result.converted()) {
            changed = true;
            hephaestus.onFluidContentsChanged();
        }

        if (hephaestus.outputTank.getFill() > 0) {
            hephaestus.tryProvideFluidToPorts(hephaestus.outputTank.getTankType(),
                    hephaestus.outputTank.getPressure(), hephaestus);
        }

        hephaestus.bufferedHeat = hephaestus.getTotalHeat();
        changed |= oldBufferedHeat != hephaestus.bufferedHeat
                || oldInputFill != hephaestus.inputTank.getFill()
                || oldOutputFill != hephaestus.outputTank.getFill()
                || oldInputType != hephaestus.inputTank.getTankType()
                || oldOutputType != hephaestus.outputTank.getTankType();
        if (changed) {
            hephaestus.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        hephaestus.networkPackNT(150);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, HephaestusBlockEntity hephaestus) {
        hephaestus.prevRot = hephaestus.rot;
        boolean active = hephaestus.bufferedHeat > 0;
        if (active) {
            hephaestus.rot += 0.5F;
            if (level.random.nextInt(7) == 0) {
                double x = level.random.nextGaussian() * 2.0D;
                double y = level.random.nextGaussian() * 3.0D;
                double z = level.random.nextGaussian() * 2.0D;
                level.addParticle(ParticleTypes.CLOUD,
                        pos.getX() + 0.5D + x, pos.getY() + 6.0D + y, pos.getZ() + 0.5D + z,
                        0.0D, 0.0D, 0.0D);
            }
        }
        if (hephaestus.rot >= 360.0F) {
            hephaestus.prevRot -= 360.0F;
            hephaestus.rot -= 360.0F;
        }
        hephaestus.audioLoop = LegacyMachineAudioBridge.updateLoop(hephaestus.audioLoop, hephaestus,
                "hbm:block.hephaestusRunning", active, 100.0D, 10.0F, 0.75F, 1.0F);
    }

    private boolean setupTanks() {
        FluidType inputType = inputTank.getTankType();
        HeatableFluidTrait trait = inputType.getTrait(HeatableFluidTrait.class);
        FluidType targetInput = inputType;
        FluidType targetOutput = HbmFluids.NONE;
        if (trait != null && trait.getEfficiency(HeatingType.HEATEXCHANGER) > 0.0D
                && trait.getFirstStep() != null) {
            targetOutput = trait.getFirstStep().producedType();
        } else {
            targetInput = HbmFluids.NONE;
        }
        boolean changed = inputTank.getTankType() != targetInput || outputTank.getTankType() != targetOutput;
        inputTank.setTankType(targetInput);
        outputTank.setTankType(targetOutput);
        if (changed) {
            onFluidContentsChanged();
        }
        return changed;
    }

    private void scanHeatLayer(Level level) {
        int height = (int) (level.getGameTime() % HEAT_LAYER_COUNT);
        int y = worldPosition.getY() - 1 - height;
        heat[height] = 0;
        if (y < level.getMinBuildHeight()) {
            return;
        }
        for (int x = -HEAT_SCAN_RANGE; x <= HEAT_SCAN_RANGE; x++) {
            for (int z = -HEAT_SCAN_RANGE; z <= HEAT_SCAN_RANGE; z++) {
                heat[height] += heatFromBlock(level, worldPosition.offset(x, -1 - height, z));
            }
        }
    }

    private int heatFromBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.LAVA) || state.getFluidState().is(FluidTags.LAVA)) {
            return 5;
        }
        if (state.is(ModBlocks.VOLCANIC_LAVA_BLOCK.get())) {
            return 150;
        }
        return 0;
    }

    public int getTotalHeat() {
        long time = level == null ? 0L : level.getGameTime();
        boolean fissure = time - fissureScanTime < 20L;
        int total = 0;
        for (int layerHeat : heat) {
            total += layerHeat;
        }
        return fissure ? total * 3 : total;
    }

    public HbmFluidTank getInputTank() {
        return inputTank;
    }

    public HbmFluidTank getOutputTank() {
        return outputTank;
    }

    public int getBufferedHeat() {
        return bufferedHeat;
    }

    public float getRotor(float partialTick) {
        return prevRot + (rot - prevRot) * partialTick;
    }

    public boolean isActive() {
        return bufferedHeat > 0;
    }

    public void onIdentifierFluidChanged() {
        setupTanks();
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
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
        long leftover = HbmStandardFluidTransceiver.super.transferFluid(type, pressure, amount);
        if (leftover != amount) {
            onFluidContentsChanged();
        }
        return leftover;
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidTransceiver.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return canConnectFluidSide(side) ? List.of(inputTank) : List.of();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return canConnectFluidSide(side) ? List.of(outputTank) : List.of();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return canConnectFluidSide(side) ? HbmFluidSideMode.BOTH : HbmFluidSideMode.NONE;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        return FLUID_PORTS;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.heatTu(bufferedHeat),
                LegacyLookOverlayLines.tank(true, inputTank),
                LegacyLookOverlayLines.tank(false, outputTank)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        inputTank.writeToNbt(tag, "0");
        outputTank.writeToNbt(tag, "1");
        writeHephaestusState(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("0_type") || tag.contains("0_type_id") || tag.contains("0")) {
            inputTank.readFromNbt(tag, "0");
        }
        if (tag.contains("1_type") || tag.contains("1_type_id") || tag.contains("1")) {
            outputTank.readFromNbt(tag, "1");
        }
        readHephaestusState(tag);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        writeHephaestusState(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        readHephaestusState(tag);
    }

    private void writeHephaestusState(CompoundTag tag) {
        tag.putInt(TAG_BUFFERED_HEAT, bufferedHeat);
        tag.putLong(TAG_FISSURE_SCAN_TIME, fissureScanTime);
        tag.putIntArray(TAG_HEAT, heat);
        tag.putFloat(TAG_ROT, rot);
        tag.putFloat(TAG_PREV_ROT, prevRot);
    }

    private void readHephaestusState(CompoundTag tag) {
        bufferedHeat = Math.max(0, tag.getInt(TAG_BUFFERED_HEAT));
        if (tag.contains(TAG_FISSURE_SCAN_TIME)) {
            fissureScanTime = tag.getLong(TAG_FISSURE_SCAN_TIME);
        }
        if (tag.contains(TAG_HEAT)) {
            int[] savedHeat = tag.getIntArray(TAG_HEAT);
            for (int i = 0; i < heat.length && i < savedHeat.length; i++) {
                heat[i] = Math.max(0, savedHeat[i]);
            }
        }
        rot = tag.getFloat(TAG_ROT);
        prevRot = tag.getFloat(TAG_PREV_ROT);
    }

    private static boolean canConnectFluidSide(@Nullable Direction side) {
        return side == null || side.getAxis().isHorizontal();
    }
}
