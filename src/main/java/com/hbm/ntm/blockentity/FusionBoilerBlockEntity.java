package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.trait.HeatableFluidTrait;
import com.hbm.ntm.fusion.FusionPowerReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.uninos.networkproviders.PlasmaNetwork;
import com.hbm.ntm.uninos.networkproviders.PlasmaNode;
import com.hbm.ntm.uninos.networkproviders.PlasmaNodespace;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class FusionBoilerBlockEntity extends HbmFluidNetworkBlockEntity
        implements HbmStandardFluidTransceiver, FusionPowerReceiver, LegacyLookOverlayProvider {
    public static final int TANK_CAPACITY = 32_000;
    private static final String TAG_PLASMA_ENERGY_SYNC = "plasmaEnergySync";

    private final HbmFluidTank waterTank;
    private final HbmFluidTank steamTank;
    private PlasmaNode plasmaNode;
    private long plasmaEnergy;
    private long plasmaEnergySync;

    public FusionBoilerBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.WATER, TANK_CAPACITY),
                new HbmFluidTank(HbmFluids.SUPERHOTSTEAM, TANK_CAPACITY));
    }

    private FusionBoilerBlockEntity(BlockPos pos, BlockState state, HbmFluidTank waterTank, HbmFluidTank steamTank) {
        super(ModBlockEntities.FUSION_BOILER.get(), pos, state, List.of(waterTank, steamTank));
        this.waterTank = waterTank;
        this.steamTank = steamTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionBoilerBlockEntity boiler) {
        boiler.plasmaEnergySync = boiler.plasmaEnergy;
        boiler.plasmaEnergy = 0L;
        HbmFluidNetworkBlockEntity.serverTick(level, pos, state, boiler);
        boiler.ensureNode(level);
        if (boiler.waterTank.getTankType() != HbmFluids.NONE) {
            boiler.refreshTrackedReceiverFluidPortsReport(List.of(boiler.waterTank), boiler);
        }
        if (boiler.steamTank.getFill() > 0) {
            boiler.tryProvideFluidToPorts(boiler.steamTank.getTankType(), boiler.steamTank.getPressure(), boiler);
        }
        boiler.networkPackNT(50);
        if (level.getGameTime() % 20L == 0L) {
            boiler.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public HbmFluidTank getWaterTank() { return waterTank; }
    public HbmFluidTank getSteamTank() { return steamTank; }
    public long getPlasmaEnergySync() { return displayedPlasmaEnergy(); }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                Component.literal("-> ").withStyle(ChatFormatting.GREEN)
                        .append(LegacyLookOverlayLines.rate(displayedPlasmaEnergy(), "TU")
                                .copy().withStyle(ChatFormatting.RESET)),
                LegacyLookOverlayLines.tank(true, waterTank),
                LegacyLookOverlayLines.tank(false, steamTank)));
    }

    @Override
    public boolean receivesFusionPower() {
        return true;
    }

    @Override
    public void receiveFusionPower(long fusionPower, double neutronPower, float r, float g, float b) {
        plasmaEnergy = fusionPower;
        HeatableFluidTrait.HeatingStep step = waterTank.getTankType()
                .getTrait(HeatableFluidTrait.class)
                .getFirstStep();
        int waterCycles = Math.min(waterTank.getFill(), steamTank.getMaxFill() - steamTank.getFill());
        int steamCycles = (int) Math.min(fusionPower / step.heatRequired(), waterCycles);
        if (steamCycles > 0) {
            waterTank.setFill(waterTank.getFill() - steamCycles);
            steamTank.setFill(steamTank.getFill() + steamCycles);
            if (level != null && level.random.nextInt(200) == 0) {
                level.playSound(null, worldPosition.above(2), ModSounds.BLOCK_BOILER_GROAN.get(), SoundSource.BLOCKS,
                        2.5F, 1.0F);
            }
            setChanged();
        }
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(waterTank);
    }

    @Override
    public List<HbmFluidTank> getAllTanks() {
        return List.of(waterTank, steamTank);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(steamTank);
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
                FluidPort.of(-facing.getStepX() + rot.getStepX() * 2, 0,
                        -facing.getStepZ() + rot.getStepZ() * 2, rot),
                FluidPort.of(-facing.getStepX() - rot.getStepX() * 2, 0,
                        -facing.getStepZ() - rot.getStepZ() * 2, rot.getOpposite()),
                FluidPort.of(facing.getStepX() * 2 + rot.getStepX() * 2, 0,
                        facing.getStepZ() * 2 + rot.getStepZ() * 2, rot),
                FluidPort.of(facing.getStepX() * 2 - rot.getStepX() * 2, 0,
                        facing.getStepZ() * 2 - rot.getStepZ() * 2, rot.getOpposite()));
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return true;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == waterTank.getTankType();
    }

    @Override
    protected boolean shouldSubscribeAsFluidProvider(FluidType type) {
        return type == steamTank.getTankType() && steamTank.getFill() > 0;
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(Direction side) {
        return HbmFluidSideMode.BOTH;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-4, 0, -4), worldPosition.offset(5, 4, 5));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        waterTank.writeToNbt(tag, "t0");
        steamTank.writeToNbt(tag, "t1");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (hasTankTag(tag, "t0")) {
            waterTank.readFromNbt(tag, "t0");
        }
        if (hasTankTag(tag, "t1")) {
            steamTank.readFromNbt(tag, "t1");
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
            plasmaEnergy = tag.getLong(TAG_PLASMA_ENERGY_SYNC);
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeLong(plasmaEnergySync);
        writeTank(data, waterTank);
        writeTank(data, steamTank);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        plasmaEnergy = data.readLong();
        readTank(data, waterTank);
        readTank(data, steamTank);
    }

    @Override
    public void setRemoved() {
        destroyNode();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        destroyNode();
        super.onChunkUnloaded();
    }

    private void destroyNode() {
        if (level != null && !level.isClientSide && plasmaNode != null) {
            PlasmaNodespace.destroyNode(level, plasmaNode.getPos());
        }
        plasmaNode = null;
    }

    private void ensureNode(Level level) {
        Direction direction = facing().getOpposite();
        BlockPos nodePos = worldPosition.relative(direction, 4).above(2);
        if (plasmaNode == null || plasmaNode.isExpired()) {
            PlasmaNode existing = PlasmaNodespace.getNode(level, nodePos);
            plasmaNode = existing == null
                    ? PlasmaNodespace.createNode(level, new PlasmaNode(nodePos, Set.of(direction)))
                    : existing;
        }
        PlasmaNetwork net = plasmaNode.getPlasmaNet();
        if (net != null) {
            net.addReceiver(this);
        }
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
    }

    private long displayedPlasmaEnergy() {
        return level != null && level.isClientSide ? plasmaEnergy : plasmaEnergySync;
    }

    private static boolean hasTankTag(CompoundTag tag, String key) {
        return tag.contains(key) || tag.contains(key + "_type") || tag.contains(key + "_type_id");
    }

    private static void writeTank(FriendlyByteBuf data, HbmFluidTank tank) {
        com.hbm.ntm.fluid.LegacyFluidTankPacket.write(data, tank);
    }

    private static void readTank(FriendlyByteBuf data, HbmFluidTank tank) {
        com.hbm.ntm.fluid.LegacyFluidTankPacket.read(data, tank);
    }
}
