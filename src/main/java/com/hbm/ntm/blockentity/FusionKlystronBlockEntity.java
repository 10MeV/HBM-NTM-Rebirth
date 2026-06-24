package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.HbmBatteryTransfer;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidSideMode;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidReceiver;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fusion.FusionKlystronProvider;
import com.hbm.ntm.menu.FusionKlystronMenu;
import com.hbm.ntm.network.HbmLegacyControlReceiver;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacyMachineAudioBridge;
import com.hbm.ntm.uninos.networkproviders.KlystronNetwork;
import com.hbm.ntm.uninos.networkproviders.KlystronNode;
import com.hbm.ntm.uninos.networkproviders.KlystronNodespace;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FusionKlystronBlockEntity extends HbmEnergyAndFluidBlockEntity
        implements MenuProvider, HbmStandardFluidReceiver, FusionKlystronProvider, HbmLegacyControlReceiver {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_COUNT = 1;
    public static final long MAX_OUTPUT = 1_000_000L;
    public static final int AIR_CONSUMPTION = 2_500;
    public static final float FAN_ACCELERATION = 0.125F;
    private static final String TAG_POWER = "power";
    private static final String TAG_MAX_POWER = "maxPower";
    private static final String TAG_OUTPUT_TARGET = "outputTarget";
    private static final String TAG_OUTPUT = "output";

    private final HbmFluidTank airTank;
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot == SLOT_BATTERY;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> items);
    private KlystronNode klystronNode;
    private long outputTarget;
    private long output;
    private long maxPower = 1_000_000L;
    private float fan;
    private float prevFan;
    private float fanSpeed;
    private Object audio;

    public FusionKlystronBlockEntity(BlockPos pos, BlockState state) {
        this(pos, state, new HbmFluidTank(HbmFluids.AIR, AIR_CONSUMPTION * 60));
    }

    private FusionKlystronBlockEntity(BlockPos pos, BlockState state, HbmFluidTank airTank) {
        super(ModBlockEntities.FUSION_KLYSTRON.get(), pos, state, new HbmEnergyStorage(1_000_000L),
                List.of(airTank));
        this.airTank = airTank;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FusionKlystronBlockEntity klystron) {
        HbmEnergyAndFluidBlockEntity.serverTick(level, pos, state, klystron);
        boolean changed = klystron.tickServer(level);
        klystron.networkPackNT(100);
        if (changed || level.getGameTime() % 20L == 0L) {
            klystron.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FusionKlystronBlockEntity klystron) {
        double mult = FusionTorusBlockEntity.getSpeedScaled(klystron.outputTarget, klystron.output);
        klystron.prevFan = klystron.fan;
        klystron.fanSpeed += klystron.output > 0 ? FAN_ACCELERATION * (float) mult : -FAN_ACCELERATION;
        klystron.fanSpeed = Math.max(0.0F, Math.min(5.0F * (float) mult, klystron.fanSpeed));
        klystron.fan += klystron.fanSpeed;
        if (klystron.fan >= 360.0F) {
            klystron.fan -= 360.0F;
            klystron.prevFan -= 360.0F;
        }
        float speed = klystron.fanSpeed / 5.0F;
        klystron.audio = LegacyMachineAudioBridge.updateLoop(klystron.audio, klystron, "FEL_LOOP",
                klystron.fanSpeed > 0.0F, 30.0D, 15.0F, klystron.getVolume(speed), speed,
                0.5D, 2.5D, 0.5D);
    }

    public ItemStackHandler getItems() { return items; }
    public HbmFluidTank getAirTank() { return airTank; }
    public long getOutputTarget() { return outputTarget; }
    public void setOutputTarget(long outputTarget) { this.outputTarget = outputTarget; }
    public long getOutput() { return output; }
    @Override public long getMaxPower() { return maxPower; }
    public float getFan(float partialTick) { return prevFan + (fan - prevFan) * partialTick; }
    public List<ItemStack> getDrops() { return HbmInventoryMenuHelper.clearToDrops(items); }

    public static CompoundTag outputTargetControlTag(long target) {
        CompoundTag tag = new CompoundTag();
        tag.putLong("amount", target);
        return tag;
    }

    @Override
    public void receiveControl(ServerPlayer player, CompoundTag data) {
        if (data.contains("amount")) {
            setOutputTarget(Math.max(0L, Math.min(MAX_OUTPUT, data.getLong("amount"))));
            setChanged();
        }
    }

    @Override
    public boolean hasPermission(ServerPlayer player) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 2.5D,
                worldPosition.getZ() + 0.5D) < 20.0D * 20.0D;
    }

    @Override
    public long provideKlystronEnergy() {
        return output;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.fusionKlystron", "Klystron");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new FusionKlystronMenu(containerId, inventory, this);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(airTank);
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
    protected Iterable<FluidPort> getFluidPorts() {
        Direction facing = facing();
        Direction rot = facing.getClockWise();
        return List.of(
                FluidPort.of(facing.getStepX() * 4, 2, facing.getStepZ() * 4, facing),
                FluidPort.of(rot.getStepX() * 3, 0, rot.getStepZ() * 3, rot),
                FluidPort.of(-rot.getStepX() * 3, 0, -rot.getStepZ() * 3, rot.getOpposite()));
    }

    @Override
    protected Iterable<EnergyPort> getEnergyPorts() {
        Direction facing = facing();
        Direction rot = facing.getClockWise();
        return List.of(EnergyPort.of(rot.getStepX() * 3, 0, rot.getStepZ() * 3, rot),
                EnergyPort.of(-rot.getStepX() * 3, 0, -rot.getStepZ() * 3, rot.getOpposite()));
    }

    @Override
    protected boolean shouldUseRemotePortFluidNode(FluidType type) {
        return true;
    }

    @Override
    protected boolean shouldSubscribeAsFluidReceiver(FluidType type) {
        return type != HbmFluids.NONE && type == airTank.getTankType();
    }

    @Override
    protected HbmFluidSideMode getFluidSideMode(@Nullable Direction side) {
        return HbmFluidSideMode.INPUT;
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.offset(-4, 0, -4), worldPosition.offset(5, 5, 5));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, "items", items);
        tag.putLong("power", getPower());
        tag.putLong("maxPower", maxPower);
        tag.putLong("outputTarget", outputTarget);
        airTank.writeToNbt(tag, "t");
        airTank.writeToNbt(tag, "compair");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, "items", items);
        if (tag.contains("maxPower")) {
            maxPower = tag.getLong("maxPower");
        }
        energy.setMaxPower(maxPower);
        energy.setTransferRates(maxPower, 0L);
        if (tag.contains("power")) {
            setPower(tag.getLong("power"));
        }
        setOutputTarget(tag.getLong("outputTarget"));
        if (hasTankTag(tag, "t")) {
            airTank.readFromNbt(tag, "t");
        } else if (hasTankTag(tag, "compair")) {
            airTank.readFromNbt(tag, "compair");
        }
        energy.setMaxPower(maxPower);
        energy.setTransferRates(maxPower, 0L);
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putLong(TAG_POWER, getPower());
        tag.putLong(TAG_MAX_POWER, maxPower);
        tag.putLong(TAG_OUTPUT_TARGET, outputTarget);
        tag.putLong(TAG_OUTPUT, output);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_POWER)) {
            setPower(tag.getLong(TAG_POWER));
        }
        if (tag.contains(TAG_MAX_POWER)) {
            maxPower = tag.getLong(TAG_MAX_POWER);
            energy.setMaxPower(maxPower);
        }
        if (tag.contains(TAG_OUTPUT_TARGET)) {
            setOutputTarget(tag.getLong(TAG_OUTPUT_TARGET));
        }
        if (tag.contains(TAG_OUTPUT)) {
            output = tag.getLong(TAG_OUTPUT);
        }
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        writeLegacyLoadedTileBinary(data);
        data.writeLong(getPower());
        data.writeLong(maxPower);
        data.writeLong(outputTarget);
        data.writeLong(output);
        writeTank(data, airTank);
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        readLegacyLoadedTileBinary(data);
        setPower(data.readLong());
        maxPower = data.readLong();
        energy.setMaxPower(maxPower);
        energy.setTransferRates(maxPower, 0L);
        setOutputTarget(data.readLong());
        output = data.readLong();
        readTank(data, airTank);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
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
        if (level != null && !level.isClientSide && klystronNode != null) {
            KlystronNodespace.destroyNode(level, klystronNode.getPos());
        }
        klystronNode = null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private boolean tickServer(Level level) {
        long oldOutput = output;
        long oldMax = maxPower;
        maxPower = Math.max(1_000_000L, outputTarget * 100L);
        energy.setMaxPower(maxPower);
        energy.setTransferRates(maxPower, 0L);
        HbmBatteryTransfer.chargeStorageFromItem(items.getStackInSlot(SLOT_BATTERY), energy, maxPower);
        output = 0L;
        double powerFactor = FusionTorusBlockEntity.getSpeedScaled(maxPower, energy.getPower());
        double airFactor = FusionTorusBlockEntity.getSpeedScaled(airTank.getMaxFill(), airTank.getFill());
        double factor = Math.min(powerFactor, airFactor);
        long powerReq = (long) Math.ceil(outputTarget * factor);
        int airReq = (int) Math.ceil(AIR_CONSUMPTION * factor);
        if (outputTarget > 0L && energy.getPower() >= powerReq && airTank.getFill() >= airReq) {
            output = powerReq;
            energy.setPower(energy.getPower() - powerReq);
            airTank.setFill(airTank.getFill() - airReq);
        }
        if (output < outputTarget / 50L) {
            output = 0L;
        }
        ensureNode(level);
        return oldOutput != output || oldMax != maxPower;
    }

    private void ensureNode(Level level) {
        Direction direction = facing().getOpposite();
        BlockPos nodePos = worldPosition.relative(direction, 4).above(2);
        if (klystronNode == null || klystronNode.isExpired()) {
            KlystronNode existing = KlystronNodespace.getNode(level, nodePos);
            klystronNode = existing == null
                    ? KlystronNodespace.createNode(level, new KlystronNode(nodePos, Set.of(direction)))
                    : existing;
        }
        KlystronNetwork network = klystronNode.getKlystronNet();
        if (network != null) {
            network.addProvider(this);
        }
        provideKyU(network, output);
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

    static boolean provideKyU(KlystronNetwork network, long output) {
        if (network == null) {
            return false;
        }
        for (Object receiver : network.receiverEntries.keySet()) {
            if (receiver instanceof FusionTorusBlockEntity torus && torus.isLoaded() && !torus.isRemoved()) {
                torus.addKlystronEnergy(output);
                return true;
            }
        }
        return false;
    }

    private static void writeTank(FriendlyByteBuf data, HbmFluidTank tank) {
        com.hbm.ntm.fluid.LegacyFluidTankPacket.write(data, tank);
    }

    private static void readTank(FriendlyByteBuf data, HbmFluidTank tank) {
        com.hbm.ntm.fluid.LegacyFluidTankPacket.read(data, tank);
    }
}
