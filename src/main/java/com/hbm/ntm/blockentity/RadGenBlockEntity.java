package com.hbm.ntm.blockentity;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.energy.ForgeEnergyAdapter;
import com.hbm.ntm.energy.HbmEnergyProvider;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.energy.HbmEnergyUtil.EnergyPort;
import com.hbm.ntm.menu.RadGenMenu;
import com.hbm.ntm.network.HbmLegacyLoadedTile;
import com.hbm.ntm.network.HbmLegacyLoadedTileState;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.recipe.RadGenRecipeRuntime;
import com.hbm.ntm.recipe.RadGenRecipeRuntime.FuelSpec;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RadGenBlockEntity extends BlockEntity implements MenuProvider, HbmEnergyProvider, HbmLegacyLoadedTile {
    private static final String TAG_INVENTORY = "items";
    private static final String TAG_ENERGY = "Energy";
    private static final String TAG_LEGACY_POWER = "power";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_MAX_PROGRESS = "maxProgress";
    private static final String TAG_PRODUCTION = "production";
    private static final String TAG_IS_ON = "isOn";
    private static final String TAG_PROCESSING = "progressing";
    private static final String TAG_SLOT = "slot";
    private static final int LANES = 12;
    private static final long MAX_POWER = 1_000_000L;

    private final HbmLegacyLoadedTileState legacyLoadedTile = new HbmLegacyLoadedTileState();
    private final HbmEnergyStorage energy = new HbmEnergyStorage(MAX_POWER, 0L, MAX_POWER);
    private final int[] progress = new int[LANES];
    private final int[] maxProgress = new int[LANES];
    private final int[] production = new int[LANES];
    private final ItemStack[] processing = new ItemStack[LANES];
    private final ItemStackHandler items = new ItemStackHandler(LANES * 2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return canInsertInput(slot, stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler());
    private final LazyOptional<IEnergyStorage> energyHandler =
            LazyOptional.of(() -> new ForgeEnergyAdapter(energy, false, true));
    private boolean isOn;
    private int output;

    public RadGenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RADGEN.get(), pos, state);
        java.util.Arrays.fill(processing, ItemStack.EMPTY);
        energy.setLoadedCheck(() -> level != null && !isRemoved());
    }

    @Override
    public HbmLegacyLoadedTileState getLegacyLoadedTileState() {
        return legacyLoadedTile;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RadGenBlockEntity radGen) {
        long oldPower = radGen.energy.getPower();
        boolean oldOn = radGen.isOn;
        int oldOutput = radGen.output;

        radGen.output = 0;
        if (level.getGameTime() % 20L == 0L) {
            HbmEnergyUtil.subscribeProviderToPorts(level, pos, radGen.energyPorts(state), radGen);
        }
        HbmEnergyUtil.tryProvideToPorts(level, pos, radGen.energyPorts(state), radGen);

        boolean changed = radGen.loadWaitingLanes();
        changed |= radGen.tickProcessing();
        energyClamp(radGen.energy);

        changed |= oldPower != radGen.energy.getPower() || oldOn != radGen.isOn || oldOutput != radGen.output;
        radGen.networkPackNT(50);
        if (changed || level.getGameTime() % 20L == 0L) {
            radGen.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private static void energyClamp(HbmEnergyStorage energy) {
        if (energy.getPower() > MAX_POWER) {
            energy.setPower(MAX_POWER);
        }
    }

    private boolean loadWaitingLanes() {
        boolean changed = false;
        for (int i = 0; i < LANES; i++) {
            if (!processing[i].isEmpty()) {
                continue;
            }
            ItemStack input = items.getStackInSlot(i);
            FuelSpec fuel = fuelFor(input);
            if (fuel == null || !canFitOutput(i, fuel.output())) {
                continue;
            }
            progress[i] = 0;
            maxProgress[i] = fuel.duration();
            production[i] = fuel.powerPerTick();
            processing[i] = input.copyWithCount(1);
            items.extractItem(i, 1, false);
            changed = true;
        }
        return changed;
    }

    private boolean canInsertInput(int slot, ItemStack stack) {
        if (slot < 0 || slot >= LANES || fuelFor(stack) == null) {
            return false;
        }
        ItemStack existing = items.getStackInSlot(slot);
        if (existing.isEmpty()) {
            return true;
        }
        int size = existing.getCount();
        for (int i = 0; i < LANES; i++) {
            ItemStack candidate = items.getStackInSlot(i);
            if (candidate.isEmpty()) {
                return false;
            }
            if (ItemStack.isSameItemSameTags(candidate, stack) && candidate.getCount() < size) {
                return false;
            }
        }
        return true;
    }

    private boolean tickProcessing() {
        boolean changed = false;
        isOn = false;
        for (int i = 0; i < LANES; i++) {
            ItemStack active = processing[i];
            if (active == null || active.isEmpty()) {
                continue;
            }
            isOn = true;
            energy.setPower(energy.getPower() + production[i]);
            output += production[i];
            progress[i]++;
            changed = true;
            if (progress[i] >= maxProgress[i]) {
                progress[i] = 0;
                FuelSpec fuel = fuelFor(active);
                if (fuel != null) {
                    mergeOutput(i, fuel.output());
                }
                processing[i] = ItemStack.EMPTY;
            }
        }
        return changed;
    }

    private boolean canFitOutput(int lane, ItemStack output) {
        if (output.isEmpty()) {
            return true;
        }
        ItemStack existing = items.getStackInSlot(lane + LANES);
        return existing.isEmpty()
                || (ItemStack.isSameItemSameTags(existing, output)
                && existing.getCount() + output.getCount() <= existing.getMaxStackSize());
    }

    private void mergeOutput(int lane, ItemStack output) {
        if (output.isEmpty()) {
            return;
        }
        int slot = lane + LANES;
        ItemStack existing = items.getStackInSlot(slot);
        if (existing.isEmpty()) {
            items.setStackInSlot(slot, output.copy());
        } else if (ItemStack.isSameItemSameTags(existing, output)) {
            existing.grow(output.getCount());
            items.setStackInSlot(slot, existing);
        }
    }

    @Nullable
    private static FuelSpec fuelFor(ItemStack stack) {
        return RadGenRecipeRuntime.fuelFor(stack);
    }

    private List<EnergyPort> energyPorts(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        return List.of(EnergyPort.of(-facing.getStepX() * 4, 0, -facing.getStepZ() * 4, facing.getOpposite()));
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress(int lane) {
        return lane >= 0 && lane < LANES ? progress[lane] : 0;
    }

    public int getMaxProgress(int lane) {
        return lane >= 0 && lane < LANES ? maxProgress[lane] : 0;
    }

    public int getProduction(int lane) {
        return lane >= 0 && lane < LANES ? production[lane] : 0;
    }

    public boolean isProcessing(int lane) {
        return lane >= 0 && lane < LANES && !processing[lane].isEmpty();
    }

    public boolean isOn() {
        return isOn;
    }

    public int getOutput() {
        return output;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_INVENTORY, items);
        tag.put(TAG_ENERGY, energy.serializeNBT());
        tag.putLong(TAG_LEGACY_POWER, energy.getPower());
        tag.putIntArray(TAG_PROGRESS, progress);
        tag.putIntArray(TAG_MAX_PROGRESS, maxProgress);
        tag.putIntArray(TAG_PRODUCTION, production);
        tag.putBoolean(TAG_IS_ON, isOn);
        ListTag list = new ListTag();
        for (int i = 0; i < processing.length; i++) {
            if (!processing[i].isEmpty()) {
                CompoundTag entry = processing[i].save(new CompoundTag());
                entry.putByte(TAG_SLOT, (byte) i);
                list.add(entry);
            }
        }
        tag.put(TAG_PROCESSING, list);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readLegacyLoadedTileNbt(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_INVENTORY, items);
        if (tag.contains(TAG_ENERGY)) {
            energy.deserializeNBT(tag.getCompound(TAG_ENERGY));
        } else if (tag.contains(TAG_LEGACY_POWER)) {
            energy.setPower(tag.getLong(TAG_LEGACY_POWER));
        }
        copyArray(tag.getIntArray(TAG_PROGRESS), progress);
        copyArray(tag.getIntArray(TAG_MAX_PROGRESS), maxProgress);
        copyArray(tag.getIntArray(TAG_PRODUCTION), production);
        isOn = tag.getBoolean(TAG_IS_ON);
        java.util.Arrays.fill(processing, ItemStack.EMPTY);
        if (tag.contains(TAG_PROCESSING, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_PROCESSING, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                int slot = entry.getByte(TAG_SLOT);
                if (slot >= 0 && slot < processing.length) {
                    processing[slot] = ItemStack.of(entry);
                }
            }
        }
    }

    private static void copyArray(int[] source, int[] target) {
        java.util.Arrays.fill(target, 0);
        System.arraycopy(source, 0, target, 0, Math.min(source.length, target.length));
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public CompoundTag getClientSyncTag() {
        return saveWithoutMetadata();
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        load(tag);
    }

    @Override
    public void serializeLegacyBufPacket(FriendlyByteBuf data) {
        data.writeNbt(saveWithoutMetadata());
    }

    @Override
    public void deserializeLegacyBufPacket(FriendlyByteBuf data) {
        CompoundTag tag = data.readNbt();
        if (tag != null) {
            load(tag);
        }
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public AABB getRenderBoundingBox() {
        Direction facing = getBlockState().hasProperty(HorizontalMachineBlock.FACING)
                ? getBlockState().getValue(HorizontalMachineBlock.FACING)
                : Direction.EAST;
        return switch (facing) {
            case NORTH, SOUTH -> new AABB(worldPosition.offset(-2, 0, -4), worldPosition.offset(3, 4, 4));
            default -> new AABB(worldPosition.offset(-4, 0, -2), worldPosition.offset(4, 4, 3));
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.radGen", "Radiation-Powered Engine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RadGenMenu(containerId, inventory, this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
        energyHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        if (capability == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(capability, side);
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
    public long getProviderSpeed() {
        return energy.getProviderSpeed();
    }

    private class AccessibleItemHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return LANES * 2;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < getSlots() ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return slot >= 0 && slot < LANES ? items.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot >= LANES && slot < LANES * 2 ? items.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < getSlots() ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return slot >= 0 && slot < LANES && items.isItemValid(slot, stack);
        }
    }

}
