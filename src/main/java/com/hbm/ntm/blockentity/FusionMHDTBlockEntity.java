package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.config.HbmCommonConfig;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fusion.FusionPowerReceiver;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.uninos.networkproviders.PlasmaNetwork;
import com.hbm.ntm.uninos.networkproviders.PlasmaNode;
import com.hbm.ntm.uninos.networkproviders.PlasmaNodespace;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class FusionMHDTBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements HbmStandardFluidTransceiver, FusionPowerReceiver, LegacyLookOverlayProvider {
    public static final int TANK_CAPACITY = 4_000;
    public static final int COOLANT_USE = 50;
    public static final long MINIMUM_PLASMA = 5_000_000L;
    public static final double PLASMA_EFFICIENCY = 1.35D;
    public static final float ROTOR_ACCELERATION = 0.125F;
    private static final String TAG_PLASMA_ENERGY_SYNC = "plasmaEnergySync";

    private final HbmFluidTank coldTank;
    private final HbmFluidTank hotTank;
    private PlasmaNode plasmaNode;
    private long plasmaEnergy;
    private long plasmaEnergySync;
    private float rotor;
    private float prevRotor;
    private float rotorSpeed;
    private Object audio;

    public FusionMHDTBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.PERFLUOROMETHYL_COLD, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.PERFLUOROMETHYL, TANK_CAPACITY));
    }

    private FusionMHDTBlockEntity(BlockPos pos, BlockState state, HbmFluidTank coldTank, HbmFluidTank hotTank) {
        super(ModBlockEntities.FUSION_MHDT.get(), pos, state,
                new HbmEnergyStorage(Long.MAX_VALUE / 4L, 0L, Long.MAX_VALUE / 4L),
                List.of(coldTank, hotTank));
        this.coldTank = coldTank;
        this.hotTank = hotTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionMHDTBlockEntity mhdt) {
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, mhdt);
        boolean changed = mhdt.tickServer(level);
        mhdt.networkPackNT(150);
        if (changed || level.getGameTime() % 20L == 0L) {
            mhdt.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FusionMHDTBlockEntity mhdt) {
        boolean spinning = mhdt.plasmaEnergySync > 0L && mhdt.isCool();
        mhdt.rotorSpeed += spinning ? ROTOR_ACCELERATION : -ROTOR_ACCELERATION;
        mhdt.rotorSpeed = Math.max(0.0F,
                Math.min(mhdt.hasMinimumPlasma() ? 15.0F : 10.0F, mhdt.rotorSpeed));
        mhdt.prevRotor = mhdt.rotor;
        mhdt.rotor += mhdt.rotorSpeed;
        if (mhdt.rotor >= 360.0F) {
            mhdt.rotor -= 360.0F;
            mhdt.prevRotor -= 360.0F;
        }
        float speed = mhdt.rotorSpeed / 15.0F;
        mhdt.audio = LegacyMachineAudioBridge.updateLoop(mhdt.audio, mhdt, "TURBINE_LARGE_LOOP",
                mhdt.rotorSpeed > 0.0F, 30.0D, 20.0F, mhdt.getVolume(speed), speed);
    }

    public HbmFluidTank getColdTank() {
        return coldTank;
    }

    public HbmFluidTank getHotTank() {
        return hotTank;
    }

    public long getPlasmaEnergySync() {
        return plasmaEnergySync;
    }

    public float getRotor(float partialTick) {
        return prevRotor + (rotor - prevRotor) * partialTick;
    }

    public boolean hasMinimumPlasma() {
        return plasmaEnergySync >= HbmCommonConfig.fusionMhdtMinimumPlasma();
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        boolean hasPlasma = hasMinimumPlasma();
        boolean cool = isCool();
        long power = displayedPower();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("-> ")
                .withStyle(hasPlasma ? net.minecraft.ChatFormatting.GREEN : net.minecraft.ChatFormatting.GOLD)
                .append(Component.literal(LegacyLookOverlayLines.shortNumber(plasmaEnergySync)
                        + "TU/t / " + LegacyLookOverlayLines.shortNumber(HbmCommonConfig.fusionMhdtMinimumPlasma())
                        + "TU/t")));
        lines.add(Component.literal("<- ").withStyle(net.minecraft.ChatFormatting.RED)
                .append(Component.literal(LegacyLookOverlayLines.shortNumber(cool ? power : 0L) + "HE/t")));
        lines.add(LegacyLookOverlayLines.tank(true, coldTank));
        lines.add(LegacyLookOverlayLines.tank(false, hotTank));
        if (plasmaEnergySync > 0L && !hasPlasma) {
            lines.add(LegacyLookOverlayLines.blinkingWarning("LOW POWER"));
        }
        if (!cool) {
            lines.add(LegacyLookOverlayLines.blinkingWarning("INSUFFICIENT COOLING"));
        }
        return LegacyLookOverlay.forBlock(this, lines);
    }

    @Override
    public boolean receivesFusionPower() {
        return true;
    }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) {
        plasmaEnergy = Math.max(0L, fusionPower);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of(coldTank, hotTank);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(coldTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(hotTank);
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
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = facing.getClockWise();
        return List.of(
                FluidPort.of(facing.getStepX() * 4 + rot.getStepX() * 4, 0,
                        facing.getStepZ() * 4 + rot.getStepZ() * 4, rot),
                FluidPort.of(facing.getStepX() * 4 - rot.getStepX() * 4, 0,
                        facing.getStepZ() * 4 - rot.getStepZ() * 4, rot.getOpposite()),
                FluidPort.of(facing.getStepX() * 8, 1, facing.getStepZ() * 8, facing));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        Direction rot = facing.getClockWise();
        return List.of(
                EnergyPort.of(facing.getStepX() * 4 + rot.getStepX() * 4, 0,
                        facing.getStepZ() * 4 + rot.getStepZ() * 4, rot),
                EnergyPort.of(facing.getStepX() * 4 - rot.getStepX() * 4, 0,
                        facing.getStepZ() * 4 - rot.getStepZ() * 4, rot.getOpposite()),
                EnergyPort.of(facing.getStepX() * 8, 1, facing.getStepZ() * 8, facing));
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return true;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type == coldTank.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == hotTank.getTankType() && hotTank.getFill() > 0;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-7, 0, -7), worldPosition.offset(8, 5, 8));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.remove("Energy");
        coldTank.writeToNbt(tag, "t0");
        hotTank.writeToNbt(tag, "t1");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        setPower(0L);
        if (hasTankTag(tag, "t0")) {
            coldTank.readFromNbt(tag, "t0");
        }
        if (hasTankTag(tag, "t1")) {
            hotTank.readFromNbt(tag, "t1");
        }
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putLong(TAG_PLASMA_ENERGY_SYNC, plasmaEnergySync);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_PLASMA_ENERGY_SYNC)) {
            plasmaEnergySync = tag.getLong(TAG_PLASMA_ENERGY_SYNC);
            plasmaEnergy = plasmaEnergySync;
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeLong(plasmaEnergySync);
        writeTank(data, coldTank);
        writeTank(data, hotTank);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        plasmaEnergySync = data.readLong();
        plasmaEnergy = plasmaEnergySync;
        readTank(data, coldTank);
        readTank(data, hotTank);
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide && plasmaNode != null) {
            PlasmaNodespace.destroyNode(level, plasmaNode.getPos());
        }
        super.setRemoved();
    }

    private boolean tickServer(Level level) {
        long previousPower = energy.getPower();
        int previousCold = coldTank.getFill();
        int previousHot = hotTank.getFill();
        plasmaEnergySync = plasmaEnergy;
        if (isCool()) {
            energy.setPower(displayedPower());
            coldTank.drain(COOLANT_USE, false);
            hotTank.fill(HbmFluids.PERFLUOROMETHYL, COOLANT_USE, hotTank.getPressure(), false);
        } else {
            energy.setPower(0L);
        }
        tryProvideEnergyToPorts();
        if (coldTank.getTankType() != HbmFluids.NONE) {
            refreshTrackedReceiverFluidPortsReport(List.of(coldTank), this);
        }
        if (hotTank.getFill() > 0) {
            tryProvideFluidToPorts(hotTank.getTankType(), hotTank.getPressure(), this);
        }
        ensureNode(level);
        plasmaEnergy = 0L;
        return previousPower != energy.getPower()
                || previousCold != coldTank.getFill()
                || previousHot != hotTank.getFill();
    }

    private boolean isCool() {
        return coldTank.getFill() >= COOLANT_USE && hotTank.getSpaceFor(HbmFluids.PERFLUOROMETHYL) >= COOLANT_USE;
    }

    private long displayedPower() {
        long generated = (long) Math.floor(plasmaEnergySync * PLASMA_EFFICIENCY);
        if (!hasMinimumPlasma()) {
            generated /= 2L;
        }
        return generated;
    }

    private void ensureNode(Level level) {
        Direction direction = facing().getOpposite();
        BlockPos nodePos = worldPosition.relative(direction, 6).above(2);
        if (plasmaNode == null || plasmaNode.isExpired()) {
            PlasmaNode existing = PlasmaNodespace.getNode(level, nodePos);
            plasmaNode = existing == null
                    ? PlasmaNodespace.createNode(level, new PlasmaNode(nodePos, Set.of(direction)))
                    : existing;
        }
        PlasmaNetwork network = plasmaNode.getPlasmaNet();
        if (network != null) {
            network.addReceiver(this);
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }

    private static void writeTank(FriendlyByteBuf data, HbmFluidTank tank) {
        data.writeInt(tank.getFill());
        data.writeInt(tank.getMaxFill());
        data.writeInt(tank.getTankType().getId());
        data.writeShort((short) tank.getPressure());
    }

    private static void readTank(FriendlyByteBuf data, HbmFluidTank tank) {
        int fill = data.readInt();
        int maxFill = data.readInt();
        FluidType type = HbmFluids.fromId(data.readInt());
        int pressure = data.readShort();
        tank.changeTankSize(maxFill);
        tank.withPressure(pressure);
        tank.setTankType(type);
        tank.setFill(fill);
    }
}
