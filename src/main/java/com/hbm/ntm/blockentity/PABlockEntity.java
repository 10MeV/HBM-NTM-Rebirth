package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyReceiver;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.ForgeRecipeFluidHandlerAdapter;
import com.hbm.ntm.fluid.HbmFluidPortMachine;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluidUtil.FluidPort;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.HbmStandardFluidTransceiver;
import com.hbm.ntm.menu.ParticleAcceleratorMenu;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PABlockEntity extends BlockEntity implements MenuProvider, HbmLegacyLoadedTile,
        HbmEnergyReceiver, HbmStandardFluidTransceiver {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_TANK_COLD = "t0";
    private static final String TAG_TANK_HOT = "t1";
    private static final String TAG_TEMPERATURE = "temperature";
    private static final String TAG_POWER = "power";
    private static final float KELVIN = 273.0F;
    public static final float TARGET_TEMPERATURE = KELVIN - 150.0F;
    private static final float PASSIVE_HEATING = 2.5F;
    private static final float CHANGE_PER_MB = 0.5F;
    private static final float MAX_COOLING_WINDOW = 5.0F + PASSIVE_HEATING;

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    protected final ParticleAcceleratorBlock.Variant variant;
    protected final HbmEnergyStorage energy;
    protected final HbmFluidTank coldCoolant = new HbmFluidTank(HbmFluids.PERFLUOROMETHYL_COLD, 4_000).withPressure(0);
    protected final HbmFluidTank hotCoolant = new HbmFluidTank(HbmFluids.PERFLUOROMETHYL, 4_000).withPressure(0);
    protected final ItemStackHandler items;
    private final LazyOptional<IItemHandler> itemHandler;
    private final LazyOptional<IEnergyStorage> energyHandler;
    private final LazyOptional<IFluidHandler> fluidHandler;
    protected float temperature = KELVIN + 20.0F;

    protected PABlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
            ParticleAcceleratorBlock.Variant variant, int slotCount, long maxPower) {
        super(type, pos, state);
        this.variant = variant;
        this.energy = new HbmEnergyStorage(maxPower, maxPower, 0L);
        this.items = new ItemStackHandler(slotCount) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }

            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack) {
                return PABlockEntity.this.isItemValid(slot, stack);
            }

            @Override
            public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
                return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
            }
        };
        this.itemHandler = LazyOptional.of(() -> items);
        this.energyHandler = LazyOptional.of(() -> new ForgeEnergyAdapter(energy, true, false));
        this.fluidHandler = LazyOptional.of(() -> ForgeRecipeFluidHandlerAdapter.create(
                List.of(coldCoolant), List.of(hotCoolant), 0, this::onFluidChanged));
    }

    @Nullable
    public static PABlockEntity create(ParticleAcceleratorBlock.Variant variant, BlockPos pos, BlockState state) {
        return switch (variant) {
            case SOURCE -> new PASourceBlockEntity(pos, state);
            case BEAMLINE -> new PABeamlineBlockEntity(pos, state);
            case RFC -> new PARfcBlockEntity(pos, state);
            case QUADRUPOLE -> new PAQuadrupoleBlockEntity(pos, state);
            case DIPOLE -> new PADipoleBlockEntity(pos, state);
            case DETECTOR -> new PADetectorBlockEntity(pos, state);
        };
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public void serverTick() {
        if (level == null || level.isClientSide) {
            return;
        }
        HbmEnergyUtil.chargeStorageFromItem(items.getStackInSlot(0), this, getReceiverSpeed());
        if (level.getGameTime() % 20L == 0L) {
            HbmEnergyUtil.subscribeReceiverToPorts(level, worldPosition, energyPorts(), this);
            HbmFluidPortMachine.refreshTransceiverPorts(level, worldPosition, fluidPorts(),
                    getReceivingTanks(), getSendingTanks(), this);
        }
        coolMachine();
        setChanged();
        if (level.getGameTime() % 20L == 0L) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public void clientTick() {
    }

    protected void coolMachine() {
        temperature = Math.min(KELVIN + 20.0F, temperature + PASSIVE_HEATING);
        if (temperature <= TARGET_TEMPERATURE) {
            return;
        }
        int cyclesTemp = (int) Math.ceil(Math.min(temperature - TARGET_TEMPERATURE, MAX_COOLING_WINDOW) / CHANGE_PER_MB);
        int cycles = Math.min(cyclesTemp, Math.min(coldCoolant.getFill(), hotCoolant.getSpace()));
        if (cycles > 0) {
            coldCoolant.setFill(coldCoolant.getFill() - cycles);
            hotCoolant.setFill(hotCoolant.getFill() + cycles);
            temperature -= CHANGE_PER_MB * cycles;
        }
    }

    public boolean isCool() {
        return temperature <= TARGET_TEMPERATURE;
    }

    protected boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0 && HbmInventoryMenuHelper.isBatteryLike(stack);
    }

    public abstract List<EnergyPort> energyPorts();

    public abstract List<FluidPort> fluidPorts();

    protected Direction facing() {
        return getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
    }

    protected static Direction beamSide(Direction facing) {
        return facing.getCounterClockWise();
    }

    protected static BlockPos rel(Direction direction, int distance) {
        return new BlockPos(direction.getStepX() * distance, direction.getStepY() * distance,
                direction.getStepZ() * distance);
    }

    protected static EnergyPort energyPort(BlockPos offset, Direction direction) {
        return new EnergyPort(offset, direction);
    }

    protected static FluidPort fluidPort(BlockPos offset, Direction direction) {
        return new FluidPort(offset, direction);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public HbmFluidTank getColdCoolant() {
        return coldCoolant;
    }

    public HbmFluidTank getHotCoolant() {
        return hotCoolant;
    }

    public float getTemperature() {
        return temperature;
    }

    public long getUsage() {
        return 0L;
    }

    public ParticleAcceleratorBlock.Variant getVariant() {
        return variant;
    }

    @Override
    public long getPower() {
        return energy.getPower();
    }

    @Override
    public void setPower(long power) {
        energy.setPower(power);
    }

    @Override
    public long getMaxPower() {
        return energy.getMaxPower();
    }

    @Override
    public long getReceiverSpeed() {
        return energy.getReceiverSpeed();
    }

    @Override
    public long transferPower(long power) {
        return energy.transferPower(power);
    }

    @Override
    public List<HbmFluidTank> getReceivingTanks() {
        return List.of(coldCoolant);
    }

    @Override
    public List<HbmFluidTank> getSendingTanks() {
        return List.of(hotCoolant);
    }

    public List<HbmFluidTank> getAllTanks() {
        return List.of(coldCoolant, hotCoolant);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        coldCoolant.readFromNbt(tag, TAG_TANK_COLD);
        hotCoolant.readFromNbt(tag, TAG_TANK_HOT);
        temperature = tag.contains(TAG_TEMPERATURE) ? tag.getFloat(TAG_TEMPERATURE) : KELVIN + 20.0F;
        energy.setPower(tag.getLong(TAG_POWER));
        loadPa(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        coldCoolant.writeToNbt(tag, TAG_TANK_COLD);
        hotCoolant.writeToNbt(tag, TAG_TANK_HOT);
        tag.putFloat(TAG_TEMPERATURE, temperature);
        tag.putLong(TAG_POWER, energy.getPower());
        savePa(tag);
    }

    protected void loadPa(CompoundTag tag) {
    }

    protected void savePa(CompoundTag tag) {
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return getClientSyncTag();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.hbm_ntm_rebirth.pa_" + variant.name().toLowerCase());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ParticleAcceleratorMenu(containerId, inventory, this);
    }

    @Override
    public boolean canReceiveClientControl(ServerPlayer player, CompoundTag tag) {
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    protected void onFluidChanged() {
        setChanged();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        itemHandler.invalidate();
        energyHandler.invalidate();
        fluidHandler.invalidate();
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(6.0D, 3.0D, 6.0D);
    }

    protected boolean isLoaded(BlockPos pos) {
        return level != null && level.hasChunkAt(pos);
    }

    protected BlockEntity resolveCore(BlockPos pos) {
        if (level == null) {
            return null;
        }
        return com.hbm.ntm.multiblock.MultiblockHelper.resolveCoreBlockEntity(level, pos);
    }
}
