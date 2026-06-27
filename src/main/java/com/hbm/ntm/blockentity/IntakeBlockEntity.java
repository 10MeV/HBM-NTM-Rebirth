package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.multiblock.LegacyMultiblockOffsets;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class IntakeBlockEntity extends HbmEnergyAndFluidBlockEntity implements HbmStandardFluidSender {
    private static final long MAX_POWER = 2_000L;
    private static final long POWER_PER_TICK = MAX_POWER / 20L;
    private static final int AIR_CAPACITY = 1_000;

    private final HbmFluidTank compressedAir;
    private float fan;
    private float previousFan;
    private Object audioLoop;

    public IntakeBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L),
                new HbmFluidTank(HbmFluids.AIR, AIR_CAPACITY));
    }

    private IntakeBlockEntity(BlockPos pos, BlockState state, HbmEnergyStorage energy, HbmFluidTank compressedAir) {
        super(ModBlockEntities.INTAKE.get(), pos, state, energy, List.of(compressedAir));
        this.compressedAir = compressedAir;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, IntakeBlockEntity intake) {
        if (level.isClientSide) {
            return;
        }
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, intake);
        intake.compressedAir.setTankType(HbmFluids.AIR);

        int previousFill = intake.compressedAir.getFill();
        long previousPower = intake.getPower();
        if (intake.getPower() >= POWER_PER_TICK) {
            intake.compressedAir.setFill(intake.compressedAir.getMaxFill());
            intake.setPower(intake.getPower() - POWER_PER_TICK);
        }
        if (intake.compressedAir.getFill() > 0) {
            intake.tryProvideFluidToPorts(intake.compressedAir.getTankType(), intake.compressedAir.getPressure(), intake);
        }

        if (previousFill != intake.compressedAir.getFill() || previousPower != intake.getPower()) {
            intake.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        intake.networkPackNT(50);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, IntakeBlockEntity intake) {
        if (!level.isClientSide) {
            return;
        }
        intake.previousFan = intake.fan;
        boolean active = intake.getPower() >= POWER_PER_TICK;
        if (active) {
            intake.fan += 45.0F;
            if (intake.fan >= 360.0F) {
                intake.fan -= 360.0F;
                intake.previousFan -= 360.0F;
            }
        }
        intake.audioLoop = LegacyMachineAudioBridge.updateLoop(intake.audioLoop, intake,
                "hbm:block.motor", active, 10.0D, 20.0F, 0.25F, 1.0F);
    }

    public HbmFluidTank getCompressedAirTank() {
        return compressedAir;
    }

    public float getFanSpin(float partialTick) {
        return previousFan + (fan - previousFan) * partialTick;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        ChatFormatting powerColor = getPower() < POWER_PER_TICK ? ChatFormatting.RED : ChatFormatting.GREEN;
        return LegacyLookOverlay.forBlock(this, List.of(
                Component.literal("Power: " + LegacyLookOverlayLines.shortNumber(getPower()) + "HE")
                        .withStyle(powerColor),
                LegacyLookOverlayLines.compactTank(false, compressedAir)));
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(compressedAir);
    }

    @Override
    public void useUpFluid(FluidType type, int pressure, long amount) {
        HbmStandardFluidSender.super.useUpFluid(type, pressure, amount);
        if (amount > 0L) {
            onFluidContentsChanged();
        }
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == HbmFluids.AIR && compressedAir.getFill() > 0;
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = LegacyMultiblockOffsets.legacyUpSide(facing);
        return List.of(
                FluidPort.of(facing.getStepX(), 0, facing.getStepZ(), facing),
                FluidPort.of(facing.getStepX() + rot.getStepX(), 0, facing.getStepZ() + rot.getStepZ(), facing),
                FluidPort.of(-facing.getStepX() * 2, 0, -facing.getStepZ() * 2, facing.getOpposite()),
                FluidPort.of(-facing.getStepX() * 2 + rot.getStepX(), 0,
                        -facing.getStepZ() * 2 + rot.getStepZ(), facing.getOpposite()),
                FluidPort.of(rot.getStepX() * 2, 0, rot.getStepZ() * 2, rot),
                FluidPort.of(rot.getStepX() * 2 - facing.getStepX(), 0,
                        rot.getStepZ() * 2 - facing.getStepZ(), rot),
                FluidPort.of(-rot.getStepX(), 0, -rot.getStepZ(), rot.getOpposite()),
                FluidPort.of(-rot.getStepX() - facing.getStepX(), 0,
                        -rot.getStepZ() - facing.getStepZ(), rot.getOpposite()));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        return List.of(
                EnergyPort.of(2, 0, 0, Direction.EAST),
                EnergyPort.of(-2, 0, 0, Direction.WEST),
                EnergyPort.of(0, 0, 2, Direction.SOUTH),
                EnergyPort.of(0, 0, -2, Direction.NORTH));
    }

    @Override
    protected boolean shouldCreateFluidNode() {
        return false;
    }

    @Override
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return List.of();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return List.of(compressedAir);
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return side == null || side.getAxis().isHorizontal() ? HbmFluidSideMode.OUTPUT : HbmFluidSideMode.NONE;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return side == null || side.getAxis().isHorizontal() ? HbmEnergySideMode.INPUT : HbmEnergySideMode.NONE;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("power", getPower());
        compressedAir.writeToNbt(tag, "compair");
        tag.putFloat("fan", fan);
        tag.putFloat("prevFan", previousFan);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("power")) {
            setPower(tag.getLong("power"));
        }
        if (tag.contains("compair") || tag.contains("compair_type") || tag.contains("compair_type_id")) {
            compressedAir.readFromNbt(tag, "compair");
        }
        compressedAir.setTankType(HbmFluids.AIR);
        fan = tag.getFloat("fan");
        previousFan = tag.getFloat("prevFan");
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(LegacyVisibleMultiblockMachineBlock.FACING)
                ? state.getValue(LegacyVisibleMultiblockMachineBlock.FACING)
                : Direction.SOUTH;
    }
}
