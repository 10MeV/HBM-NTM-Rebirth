package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.redstoneoverradio.RORDispatcher;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ChungusBlockEntity extends LegacySteamTurbineBlockEntity implements RORValueProvider {
    private static final int TANK_CAPACITY = 1_000_000_000;
    private static final double EFFICIENCY = 0.85D;
    private static final double CONSUMPTION_PERCENT = 1.0D;
    private static final long MAX_TRANSIENT_POWER = Long.MAX_VALUE;
    private static final int TURN_TIMER_ACTIVE = 25;
    private static final String[] SPIN_BLOCKS = new String[] {"", "|", "/", "\\"};

    private int turnTimer;
    private float rotor;
    private float lastRotor;
    private float fanAcceleration;
    private Object audioLoop;
    private final float audioDesync;
    private final RORDispatcher ror;

    public ChungusBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHUNGUS.get(), pos, state, MAX_TRANSIENT_POWER, TANK_CAPACITY, TANK_CAPACITY);
        this.audioDesync = (Math.floorMod(pos.asLong(), 1000L) / 1000.0F) * 0.05F;
        this.ror = RORDispatcher.builder()
                .value("output", () -> Long.toString(getLastPowerProduced()))
                .build();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ChungusBlockEntity chungus) {
        tickTurbine(level, pos, state, chungus);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ChungusBlockEntity chungus) {
        chungus.lastRotor = chungus.rotor;
        chungus.rotor += chungus.fanAcceleration;
        if (chungus.rotor >= 360.0F) {
            chungus.rotor -= 360.0F;
            chungus.lastRotor -= 360.0F;
        }

        if (chungus.turnTimer > 0) {
            chungus.fanAcceleration = Math.max(0.0F,
                    Math.min(25.0F, chungus.fanAcceleration + 0.075F + chungus.audioDesync));
            chungus.spawnClouds(level, pos, state);
        } else {
            chungus.fanAcceleration = Math.max(0.0F, Math.min(25.0F, chungus.fanAcceleration - 0.1F));
        }
        chungus.updateAudioLoop();
    }

    @Override
    protected double getEfficiency() {
        return EFFICIENCY;
    }

    @Override
    protected double getConsumptionPercent() {
        return CONSUMPTION_PERCENT;
    }

    @Override
    protected void beforeTurbineTick() {
        energy.setPower(0L);
    }

    @Override
    protected void afterTurbineTick() {
        turnTimer--;
        if (isOperational()) {
            turnTimer = TURN_TIMER_ACTIVE;
        }
    }

    @Override
    protected void normalizeConfigState() {
        normalizeTankCapacity(TANK_CAPACITY, TANK_CAPACITY);
        energy.setMaxPower(MAX_TRANSIENT_POWER);
        energy.setTransferRates(0L, MAX_TRANSIENT_POWER);
    }

    public void onLeverPull() {
        FluidType type = inputTank.getTankType();
        if (type == HbmFluids.STEAM) {
            inputTank.setTankType(HbmFluids.HOTSTEAM);
            outputTank.setTankType(HbmFluids.STEAM);
        } else if (type == HbmFluids.HOTSTEAM) {
            inputTank.setTankType(HbmFluids.SUPERHOTSTEAM);
            outputTank.setTankType(HbmFluids.HOTSTEAM);
        } else if (type == HbmFluids.SUPERHOTSTEAM) {
            inputTank.setTankType(HbmFluids.ULTRAHOTSTEAM);
            outputTank.setTankType(HbmFluids.SUPERHOTSTEAM);
        } else {
            inputTank.setTankType(HbmFluids.STEAM);
            outputTank.setTankType(HbmFluids.SPENTSTEAM);
        }
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public float getRotor(float partialTick) {
        return lastRotor + (rotor - lastRotor) * partialTick;
    }

    public float getFanAcceleration() {
        return fanAcceleration;
    }

    public float getLeverAngle() {
        return 15.0F - (legacySteamOrdinal(inputTank.getTankType()) - 2) * 10.0F;
    }

    @Override
    public String[] getFunctionInfo() {
        return ror.getFunctionInfo();
    }

    @Override
    public String provideRORValue(String name) {
        return ror.provideValue(name);
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        int spinner = isOperational() ? (int) ((level.getGameTime() / 4L) % 4L) : 0;
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.tank(true, inputTank),
                LegacyLookOverlayLines.tank(false, outputTank),
                LegacyLookOverlayLines.energyOut(getLastPowerProduced(),
                        net.minecraft.network.chat.Component.literal(SPIN_BLOCKS[spinner]))));
    }

    @Override
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = rotatedFacing();
        return List.of(
                fluidPort(facing, facing.getStepX() * 5, 2, facing.getStepZ() * 5),
                fluidPort(rot, rot.getStepX() * 3, 0, rot.getStepZ() * 3),
                fluidPort(rot.getOpposite(), -rot.getStepX() * 3, 0, -rot.getStepZ() * 3));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        return List.of(energyPort(facing.getOpposite(), -facing.getStepX() * 11, 0, -facing.getStepZ() * 11));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("turnTimer", turnTimer);
        tag.putFloat("rotor", rotor);
        tag.putFloat("lastRotor", lastRotor);
        tag.putFloat("fanAcceleration", fanAcceleration);
        inputTank.writeToNbt(tag, "water");
        outputTank.writeToNbt(tag, "steam");
        tag.putLong("power", energy.getPower());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("water")) {
            inputTank.readFromNbt(tag, "water");
        }
        if (tag.contains("steam")) {
            outputTank.readFromNbt(tag, "steam");
        }
        if (tag.contains("power")) {
            energy.setPower(tag.getLong("power"));
        }
        turnTimer = tag.getInt("turnTimer");
        rotor = tag.getFloat("rotor");
        lastRotor = tag.getFloat("lastRotor");
        fanAcceleration = Math.max(0.0F, tag.getFloat("fanAcceleration"));
        normalizeConfigState();
    }

    private void spawnClouds(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.hasProperty(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                ? state.getValue(com.hbm.ntm.block.HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        Direction side = facing.getClockWise();
        for (int i = 0; i < 10; i++) {
            level.addParticle(ParticleTypes.CLOUD,
                    pos.getX() + 0.5D + facing.getStepX() * (level.random.nextDouble() + 1.25D)
                            + level.random.nextGaussian() * side.getStepX() * 0.65D,
                    pos.getY() + 2.5D + level.random.nextGaussian() * 0.65D,
                    pos.getZ() + 0.5D + facing.getStepZ() * (level.random.nextDouble() + 1.25D)
                            + level.random.nextGaussian() * side.getStepZ() * 0.65D,
                    -facing.getStepX() * 0.2D, 0.0D, -facing.getStepZ() * 0.2D);
        }
    }

    private void updateAudioLoop() {
        float turbineSpeed = fanAcceleration / 25.0F;
        boolean active = turnTimer > 0 || fanAcceleration > 0.0F;
        audioLoop = LegacyMachineAudioBridge.updateLoop(audioLoop, this, "hbm:block.chungusTurbineRunning",
                active, 20.0D, 20.0F, 0.5F * turbineSpeed, 0.25F + 0.75F * turbineSpeed);
    }

    private static int legacySteamOrdinal(FluidType type) {
        if (type == HbmFluids.STEAM) {
            return 2;
        }
        if (type == HbmFluids.HOTSTEAM) {
            return 3;
        }
        if (type == HbmFluids.SUPERHOTSTEAM) {
            return 4;
        }
        if (type == HbmFluids.ULTRAHOTSTEAM) {
            return 5;
        }
        return 2;
    }
}
