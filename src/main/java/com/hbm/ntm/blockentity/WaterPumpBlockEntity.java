package com.hbm.ntm.blockentity;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmStandardFluidSender;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WaterPumpBlockEntity extends HbmFluidNetworkBlockEntity
        implements HbmStandardFluidReceiver, HbmStandardFluidSender, HbmEnergyReceiver {
    private static final String TAG_ACTIVE = "isOn";
    private static final String TAG_ON_GROUND = "onGround";
    private static final String TAG_ROTOR = "rotor";
    private static final String TAG_LAST_ROTOR = "lastRotor";
    private static final String TAG_GROUND_CHECK_DELAY = "groundCheckDelay";
    private static final String TAG_POWER = "power";
    private static final String TAG_ENERGY = "Energy";

    private static final int GROUND_HEIGHT = 70;
    private static final int GROUND_DEPTH = 4;
    private static final int STEAM_SPEED = 1_000;
    private static final int ELECTRIC_SPEED = 10_000;
    private static final long ELECTRIC_MAX_POWER = 10_000L;
    private static final long ELECTRIC_CONSUMPTION = 1_000L;

    private static final List<FluidPort> FLUID_PORTS = List.of(
            FluidPort.of(2, 0, 0, Direction.EAST),
            FluidPort.of(-2, 0, 0, Direction.WEST),
            FluidPort.of(0, 0, 2, Direction.SOUTH),
            FluidPort.of(0, 0, -2, Direction.NORTH));
    private static final List<EnergyPort> ENERGY_PORTS = List.of(
            EnergyPort.of(2, 0, 0, Direction.EAST),
            EnergyPort.of(-2, 0, 0, Direction.WEST),
            EnergyPort.of(0, 0, 2, Direction.SOUTH),
            EnergyPort.of(0, 0, -2, Direction.NORTH));

    private final HbmFluidTank water;
    @Nullable
    private final HbmFluidTank steam;
    @Nullable
    private final HbmFluidTank spentSteam;
    private final HbmEnergyStorage energy = new HbmEnergyStorage(ELECTRIC_MAX_POWER, ELECTRIC_MAX_POWER, 0L);
    private final LazyOptional<net.minecraftforge.energy.IEnergyStorage> energyHandler =
            LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));

    private boolean active;
    private boolean onGround;
    private int groundCheckDelay;
    private float rotor;
    private float lastRotor;

    public WaterPumpBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, createTanks(isElectricState(state)));
    }

    private WaterPumpBlockEntity(BlockPos pos, BlockState state, PumpTanks tanks) {
        super(ModBlockEntities.WATER_PUMP.get(), pos, state, tanks.all());
        this.water = tanks.water();
        this.steam = tanks.steam();
        this.spentSteam = tanks.spentSteam();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, WaterPumpBlockEntity pump) {
        if (level.isClientSide) {
            return;
        }
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, pump);

        boolean oldActive = pump.active;
        boolean oldGround = pump.onGround;
        int oldWater = pump.water.getFill();
        int oldSteam = pump.steam == null ? 0 : pump.steam.getFill();
        int oldSpentSteam = pump.spentSteam == null ? 0 : pump.spentSteam.getFill();
        long oldPower = pump.energy.getPower();

        pump.normalizeTankTypes();
        pump.provideOutputs();
        pump.subscribeInputs(level);

        if (pump.groundCheckDelay > 0) {
            pump.groundCheckDelay--;
        } else {
            pump.onGround = pump.checkGround(level, pos);
        }

        pump.active = false;
        if (pump.canOperate(level, pos)) {
            pump.active = true;
            pump.operate();
        }

        boolean changed = oldActive != pump.active
                || oldGround != pump.onGround
                || oldWater != pump.water.getFill()
                || oldSteam != (pump.steam == null ? 0 : pump.steam.getFill())
                || oldSpentSteam != (pump.spentSteam == null ? 0 : pump.spentSteam.getFill())
                || oldPower != pump.energy.getPower();
        if (changed || level.getGameTime() % 20L == 0L) {
            pump.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        pump.networkPackNT(150);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, WaterPumpBlockEntity pump) {
        if (!level.isClientSide) {
            return;
        }
        pump.lastRotor = pump.rotor;
        if (pump.active) {
            pump.rotor += 10.0F;
        }
        if (pump.rotor >= 360.0F) {
            pump.rotor -= 360.0F;
            pump.lastRotor -= 360.0F;
            LegacySoundPlayer.playSoundEffect(level, pos.getX(), pos.getY(), pos.getZ(),
                    "hbm:block.steamEngineOperate", SoundSource.BLOCKS, 0.5F, 0.75F);
            level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.PLAYER_SPLASH,
                    SoundSource.BLOCKS, 1.0F, 0.5F, false);
        }
    }

    private void normalizeTankTypes() {
        water.setTankType(HbmFluids.WATER);
        if (steam != null) {
            steam.setTankType(HbmFluids.STEAM);
        }
        if (spentSteam != null) {
            spentSteam.setTankType(HbmFluids.SPENTSTEAM);
        }
    }

    private void provideOutputs() {
        if (water.getFill() > 0) {
            tryProvideFluidToPorts(water.getTankType(), water.getPressure(), this);
        }
        if (spentSteam != null && spentSteam.getFill() > 0) {
            tryProvideFluidToPorts(spentSteam.getTankType(), spentSteam.getPressure(), this);
        }
    }

    private void subscribeInputs(Level level) {
        if (isElectric()) {
            if (level.getGameTime() % 20L == 0L) {
                HbmEnergyUtil.subscribeReceiverToPorts(level, worldPosition, ENERGY_PORTS, this);
            }
        } else if (steam != null) {
            subscribeFluidReceiverToPorts(steam.getTankType(), this);
        }
    }

    private boolean canOperate(Level level, BlockPos pos) {
        return pos.getY() <= GROUND_HEIGHT && onGround && (isElectric() ? canOperateElectric() : canOperateSteam());
    }

    private boolean canOperateSteam() {
        return steam != null
                && spentSteam != null
                && steam.getFill() >= 100
                && spentSteam.getSpace() > 0
                && water.getFill() < water.getMaxFill();
    }

    private boolean canOperateElectric() {
        return energy.getPower() >= ELECTRIC_CONSUMPTION && water.getFill() < water.getMaxFill();
    }

    private void operate() {
        if (isElectric()) {
            energy.setPower(energy.getPower() - ELECTRIC_CONSUMPTION);
            water.setFill(Math.min(water.getMaxFill(), water.getFill() + ELECTRIC_SPEED));
            return;
        }
        if (steam != null && spentSteam != null) {
            steam.setFill(steam.getFill() - 100);
            spentSteam.setFill(spentSteam.getFill() + 1);
            water.setFill(Math.min(water.getMaxFill(), water.getFill() + STEAM_SPEED));
        }
    }

    private boolean checkGround(Level level, BlockPos pos) {
        if (!level.dimensionType().hasSkyLight()) {
            return false;
        }
        int valid = 0;
        int invalid = 0;
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y >= -GROUND_DEPTH; y--) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState state = level.getBlockState(checkPos);
                    if (y == -1 && !isFullSolidBlock(level, checkPos, state)) {
                        return false;
                    }
                    if (isValidGroundBlock(state.getBlock())) {
                        valid++;
                    } else {
                        invalid++;
                    }
                }
            }
        }
        return valid >= invalid;
    }

    private static boolean isFullSolidBlock(Level level, BlockPos pos, BlockState state) {
        return Block.isShapeFullBlock(state.getCollisionShape(level, pos));
    }

    private static boolean isValidGroundBlock(Block block) {
        if (block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.SAND || block == Blocks.MYCELIUM
                || block == ModBlocks.WASTE_EARTH.get()) {
            return true;
        }
        return block == registryBlock("dirt_dead")
                || block == registryBlock("dirt_oily")
                || block == registryBlock("sand_dirty")
                || block == registryBlock("sand_dirty_red");
    }

    @Nullable
    private static Block registryBlock(String name) {
        return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(HbmNtm.MOD_ID, name));
    }

    public boolean isElectric() {
        return isElectricState(getBlockState());
    }

    private static boolean isElectricState(BlockState state) {
        return state.getBlock() == ModBlocks.PUMP_ELECTRIC.get();
    }

    public HbmFluidTank getWaterTank() {
        return water;
    }

    @Nullable
    public HbmFluidTank getSteamTank() {
        return steam;
    }

    @Nullable
    public HbmFluidTank getSpentSteamTank() {
        return spentSteam;
    }

    public long getPower() {
        return energy.getPower();
    }

    @Override
    public void setPower(long power) {
        energy.setPower(power);
    }

    @Override
    public long getMaxPower() {
        return isElectric() ? ELECTRIC_MAX_POWER : 0L;
    }

    @Override
    public long getReceiverSpeed() {
        return isElectric() ? ELECTRIC_MAX_POWER : 0L;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public float getRotor(float partialTick) {
        return lastRotor + (rotor - lastRotor) * partialTick;
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return steam == null ? List.of() : List.of(steam);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return spentSteam == null ? List.of(water) : List.of(water, spentSteam);
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
        return steam != null && type == steam.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == water.getTankType() && water.getFill() > 0
                || spentSteam != null && type == spentSteam.getTankType() && spentSteam.getFill() > 0;
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
    protected List<HbmFluidTank> getInputTanks(@Nullable Direction side) {
        return getReceivingTanks();
    }

    @Override
    protected List<HbmFluidTank> getOutputTanks(@Nullable Direction side) {
        return getSendingTanks();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<Component> lines = new ArrayList<>();
        if (isElectric()) {
            lines.add(Component.literal("-> " + energy.getPower() + " / " + ELECTRIC_MAX_POWER + "HE"));
            lines.add(LegacyLookOverlayLines.tank(false, water));
        } else if (steam != null && spentSteam != null) {
            lines.add(LegacyLookOverlayLines.tank(true, steam));
            lines.add(LegacyLookOverlayLines.tank(false, spentSteam));
            lines.add(LegacyLookOverlayLines.tank(false, water));
        }
        if (worldPosition.getY() > GROUND_HEIGHT) {
            lines.add(LegacyLookOverlayLines.blinkingWarning("ALTITUDE"));
        }
        if (!onGround) {
            lines.add(LegacyLookOverlayLines.blinkingWarning("NO VALID GROUND"));
        }
        return LegacyLookOverlay.forBlock(this, lines);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        water.writeToNbt(tag, "0");
        if (steam != null) {
            steam.writeToNbt(tag, "1");
        }
        if (spentSteam != null) {
            spentSteam.writeToNbt(tag, "2");
        }
        tag.putBoolean(TAG_ACTIVE, active);
        tag.putBoolean(TAG_ON_GROUND, onGround);
        tag.putInt(TAG_GROUND_CHECK_DELAY, groundCheckDelay);
        tag.putFloat(TAG_ROTOR, rotor);
        tag.putFloat(TAG_LAST_ROTOR, lastRotor);
        tag.putLong(TAG_POWER, energy.getPower());
        tag.put(TAG_ENERGY, energy.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("0_type") || tag.contains("0_type_id") || tag.contains("0")) {
            water.readFromNbt(tag, "0");
        }
        if (steam != null && (tag.contains("1_type") || tag.contains("1_type_id") || tag.contains("1"))) {
            steam.readFromNbt(tag, "1");
        }
        if (spentSteam != null && (tag.contains("2_type") || tag.contains("2_type_id") || tag.contains("2"))) {
            spentSteam.readFromNbt(tag, "2");
        }
        if (tag.contains(TAG_ENERGY)) {
            energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
        } else {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        active = tag.getBoolean(TAG_ACTIVE);
        onGround = tag.getBoolean(TAG_ON_GROUND);
        groundCheckDelay = Math.max(0, tag.getInt(TAG_GROUND_CHECK_DELAY));
        rotor = tag.getFloat(TAG_ROTOR);
        lastRotor = tag.getFloat(TAG_LAST_ROTOR);
        normalizeTankTypes();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ENERGY && isElectric()) {
            return energyHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private static PumpTanks createTanks(boolean electric) {
        HbmFluidTank water = new HbmFluidTank(HbmFluids.WATER,
                electric ? ELECTRIC_SPEED * 100 : STEAM_SPEED * 100);
        if (electric) {
            return new PumpTanks(water, null, null, List.of(water));
        }
        HbmFluidTank steam = new HbmFluidTank(HbmFluids.STEAM, 1_000);
        HbmFluidTank spentSteam = new HbmFluidTank(HbmFluids.SPENTSTEAM, 10);
        return new PumpTanks(water, steam, spentSteam, List.of(water, steam, spentSteam));
    }

    private record PumpTanks(HbmFluidTank water, @Nullable HbmFluidTank steam,
            @Nullable HbmFluidTank spentSteam, List<HbmFluidTank> all) {
        private PumpTanks {
            Objects.requireNonNull(water, "water");
            all = List.copyOf(all);
        }
    }
}
