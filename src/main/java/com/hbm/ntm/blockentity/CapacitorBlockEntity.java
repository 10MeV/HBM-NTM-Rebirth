package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.HbmPersistentBlockState;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.redstoneoverradio.RORDispatcher;
import com.hbm.ntm.api.redstoneoverradio.RORValueProvider;
import com.hbm.ntm.block.CapacitorBlock;
import com.hbm.ntm.block.CapacitorBusBlock;
import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

public class CapacitorBlockEntity extends HbmEnergyBlockEntity
        implements HbmPersistentBlockState, LegacyLookOverlayProvider, RORValueProvider {
    private static final String TAG_POWER = "power";
    private static final String TAG_MAX_POWER = "maxPower";

    private final RORDispatcher ror;
    private final CapacitorEnergyStorage capacitorEnergy;
    private long powerReceived;
    private long powerSent;
    private EnergyPort lastOutputPort;

    public CapacitorBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.CAPACITOR.get(), pos, state, new CapacitorEnergyStorage(maxPowerFor(state)));
    }

    private CapacitorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            CapacitorEnergyStorage energy) {
        super(type, pos, state, energy);
        this.capacitorEnergy = energy;
        this.capacitorEnergy.bindOwner(this);
        this.ror = RORDispatcher.builder()
                .value("fill", () -> Long.toString(getPower()))
                .value("fillpercent", () -> Long.toString(getPower() * 100L / Math.max(getMaxPower(), 1L)))
                .build();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CapacitorBlockEntity capacitor) {
        if (level.isClientSide) {
            return;
        }
        long previousPower = capacitor.getPower();
        capacitor.powerReceived = 0L;
        capacitor.powerSent = 0L;
        capacitor.subscribeInput();
        capacitor.tryProvideThroughBus();
        if (previousPower != capacitor.getPower()) {
            capacitor.setChanged();
        }
        capacitor.networkPackNT(15);
    }

    public static long maxPowerFor(BlockState state) {
        return state.getBlock() instanceof CapacitorBlock capacitor ? capacitor.maxPower() : 0L;
    }

    public String legacyTextureName() {
        return getBlockState().getBlock() instanceof CapacitorBlock capacitor
                ? capacitor.legacyTextureName()
                : "copper";
    }

    public long getPowerReceived() {
        return powerReceived;
    }

    public long getPowerSent() {
        return powerSent;
    }

    public Direction inputDirection() {
        BlockState state = getBlockState();
        return state.hasProperty(CapacitorBlock.FACING) ? state.getValue(CapacitorBlock.FACING) : Direction.UP;
    }

    public Direction outputDirection() {
        return inputDirection().getOpposite();
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return side == inputDirection() ? HbmEnergySideMode.INPUT : HbmEnergySideMode.NONE;
    }

    @Override
    public HbmEnergyUtil.PortSetSnapshot inspectEnergyPorts() {
        EnergyPort port = resolveOutputPort();
        return level == null || port == null
                ? new HbmEnergyUtil.PortSetSnapshot(0, 0, 0, 0, 0, 0, 0L, 0L)
                : HbmEnergyUtil.inspectPortSet(level, worldPosition, port);
    }

    private void subscribeInput() {
        subscribeEnergyReceiverToSide(inputDirection());
    }

    private void tryProvideThroughBus() {
        if (level == null || level.isClientSide) {
            return;
        }
        EnergyPort outputPort = resolveOutputPort();
        if (outputPort == null) {
            clearOutputSubscription();
            return;
        }
        if (!outputPort.equals(lastOutputPort)) {
            clearOutputSubscription();
            lastOutputPort = outputPort;
        }
        HbmEnergyUtil.tryProvideToPort(level, worldPosition, outputPort, energy);
    }

    @Nullable
    private EnergyPort resolveOutputPort() {
        if (level == null) {
            return null;
        }
        Direction output = outputDirection();
        BlockPos cursor = worldPosition.relative(output);
        boolean didStep = false;
        Direction last = null;
        while (level.getBlockState(cursor).is(ModBlocks.CAPACITOR_BUS.get())) {
            Direction current = level.getBlockState(cursor).hasProperty(CapacitorBusBlock.FACING)
                    ? level.getBlockState(cursor).getValue(CapacitorBusBlock.FACING)
                    : Direction.UP;
            if (!didStep) {
                last = current;
            }
            didStep = true;
            if (last != current) {
                return null;
            }
            cursor = cursor.relative(current);
        }
        return didStep && last != null ? new EnergyPort(cursor.subtract(worldPosition), last) : null;
    }

    public void clearOutputSubscription() {
        if (level != null && !level.isClientSide && lastOutputPort != null) {
            HbmEnergyUtil.unsubscribeProviderFromPort(level, worldPosition, lastOutputPort, energy);
        }
        lastOutputPort = null;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        List<Component> lines = new ArrayList<>();
        lines.add(LegacyLookOverlayLines.energyStored(getPower(), getMaxPower()));
        lines.add(LegacyLookOverlayLines.legacyUnclampedChargePercent(getPower(), getMaxPower()));
        lines.add(Component.literal("-> ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal("+" + LegacyLookOverlayLines.shortNumber(powerReceived) + "HE/t")
                        .withStyle(ChatFormatting.RESET)));
        lines.add(Component.literal("<- ").withStyle(ChatFormatting.RED)
                .append(Component.literal("-" + LegacyLookOverlayLines.shortNumber(powerSent) + "HE/t")
                        .withStyle(ChatFormatting.RESET)));
        return LegacyLookOverlay.forBlock(this, lines);
    }

    public ItemStack createPersistentBlockDrop(Item item) {
        ItemStack stack = new ItemStack(item);
        writePersistentStateToStack(stack);
        return stack;
    }

    @Override
    public void writePersistentState(CompoundTag persistent) {
        persistent.putLong(TAG_POWER, getPower());
        persistent.putLong(TAG_MAX_POWER, getMaxPower());
    }

    @Override
    public void readPersistentState(CompoundTag persistent) {
        if (persistent.contains(TAG_MAX_POWER)) {
            energy.setMaxPower(persistent.getLong(TAG_MAX_POWER));
            energy.setTransferRates(Math.max(0L, persistent.getLong(TAG_MAX_POWER) / 100L),
                    Math.max(0L, persistent.getLong(TAG_MAX_POWER) / 300L));
        }
        energy.setPower(persistent.getLong(TAG_POWER));
        setChanged();
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
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putLong(CompatEnergyControl.L_DIFF_HE, powerReceived - powerSent);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong(TAG_POWER, getPower());
        tag.putLong(TAG_MAX_POWER, getMaxPower());
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putLong(TAG_POWER, getPower());
        tag.putLong(TAG_MAX_POWER, getMaxPower());
        tag.putLong("powerReceived", powerReceived);
        tag.putLong("powerSent", powerSent);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_MAX_POWER)) {
            energy.setMaxPower(tag.getLong(TAG_MAX_POWER));
            energy.setTransferRates(Math.max(0L, tag.getLong(TAG_MAX_POWER) / 100L),
                    Math.max(0L, tag.getLong(TAG_MAX_POWER) / 300L));
        }
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        powerReceived = tag.getLong("powerReceived");
        powerSent = tag.getLong("powerSent");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        long max = tag.contains(TAG_MAX_POWER) ? tag.getLong(TAG_MAX_POWER) : maxPowerFor(getBlockState());
        energy.setMaxPower(max);
        energy.setTransferRates(max / 100L, max / 300L);
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
    }

    @Override
    public void setRemoved() {
        clearOutputSubscription();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        clearOutputSubscription();
        super.onChunkUnloaded();
    }

    private static final class CapacitorEnergyStorage extends HbmEnergyStorage {
        private CapacitorBlockEntity owner;

        private CapacitorEnergyStorage(long maxPower) {
            super(maxPower, maxPower / 100L, maxPower / 300L);
        }

        private void bindOwner(CapacitorBlockEntity owner) {
            this.owner = owner;
        }

        @Override
        public HbmEnergyReceiver.ConnectionPriority getPriority() {
            return HbmEnergyReceiver.ConnectionPriority.LOW;
        }

        @Override
        public long transferPower(long power) {
            long before = getPower();
            long remainder = super.transferPower(power);
            if (owner != null) {
                owner.powerReceived += Math.max(0L, getPower() - before);
            }
            return remainder;
        }

        @Override
        public long usePower(long power) {
            long used = super.usePower(power);
            if (owner != null) {
                owner.powerSent += used;
            }
            return used;
        }
    }
}
