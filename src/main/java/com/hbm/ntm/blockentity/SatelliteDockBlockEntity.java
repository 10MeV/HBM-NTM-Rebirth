package com.hbm.ntm.blockentity;

import com.hbm.ntm.entity.missile.MinerRocketEntity;
import com.hbm.ntm.explosion.ExplosionNukeSmall;
import com.hbm.ntm.itempool.HbmItemPoolRegistry;
import com.hbm.ntm.menu.SatelliteDockMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.satellite.ISatelliteChip;
import com.hbm.ntm.satellite.Satellite;
import com.hbm.ntm.satellite.SatelliteSavedData;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SatelliteDockBlockEntity extends BlockEntity implements MenuProvider {
    public static final int OUTPUT_SLOT_COUNT = 15;
    public static final int SLOT_CHIP = 15;
    public static final int SLOT_COUNT = 16;
    private static final int[] OUTPUT_SLOTS = new int[] {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14
    };
    private static final String TAG_CUSTOM_NAME = "name";
    private static final long CARGO_DELAY_MILLIS = 10L * 60L * 1000L;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // TileEntityMachineSatDock#isItemValidForSlot only checks the
            // slot number.  ContainerSatDock supplies the separate manual
            // ItemSatChip class boundary; the inventory itself stays generic.
            return slot == SLOT_CHIP;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private final LazyOptional<IItemHandler> itemHandler =
            LazyOptional.of(() -> new ExtractOnlySidedItemHandler(items, OUTPUT_SLOTS));
    private String customName;

    public SatelliteDockBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SAT_DOCK.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SatelliteDockBlockEntity dock) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        SatelliteSavedData data = SatelliteSavedData.get(serverLevel);
        ItemStack chip = dock.items.getStackInSlot(SLOT_CHIP);
        // TileEntityMachineSatDock gates this path on a non-null slot, then
        // calls ISatChip#getFreqS. That helper returns zero for an arbitrary
        // non-chip stack, so direct inventory/NBT insertion can intentionally
        // address a miner registered at frequency zero.
        if (!chip.isEmpty()) {
            int frequency = ISatelliteChip.getFrequencyFromStack(chip);
            Satellite satellite = data.getCargoSatellite(frequency);
            if (satellite != null
                    && satellite.lastOperationMillis() + CARGO_DELAY_MILLIS < System.currentTimeMillis()) {
                MinerRocketEntity rocket = new MinerRocketEntity(serverLevel);
                rocket.setPos(pos.getX() + 0.5D, 300.0D, pos.getZ() + 0.5D);
                rocket.setSatelliteFrequency(frequency);
                serverLevel.addFreshEntity(rocket);
                satellite.setLastOperationMillis(System.currentTimeMillis());
                data.markDirty();
            }
        }

        AABB landingBox = new AABB(
                pos.getX() + 0.25D, pos.getY() + 0.75D, pos.getZ() + 0.25D,
                pos.getX() + 0.75D, pos.getY() + 2.0D, pos.getZ() + 0.75D);
        for (MinerRocketEntity rocket : serverLevel.getEntitiesOfClass(MinerRocketEntity.class, landingBox)) {
            // ISatChip#getFreqS returns zero for a null slot as well as a
            // non-chip stack.  The source only guards the mismatched-frequency
            // explosion with a non-null chip; its unloading branch always
            // reads that same helper, so a frequency-zero miner can unload
            // after the chip was removed.
            int frequency = ISatelliteChip.getFrequencyFromStack(chip);
            if (!chip.isEmpty()) {
                if (frequency != rocket.satelliteFrequency()) {
                    rocket.discard();
                    ExplosionNukeSmall.explode(serverLevel, pos.getX() + 0.5D, pos.getY() + 0.5D,
                            pos.getZ() + 0.5D, ExplosionNukeSmall.PARAMS_TOTS);
                    break;
                }
            }
            if (rocket.mode() == MinerRocketEntity.MODE_UNLOADING && rocket.timer() == 50) {
                // TileEntityMachineSatDock only unloads a mining satellite.
                // Non-miner entries at the matching frequency leave the rocket
                // untouched and produce no cargo; they must not be cast.
                Satellite satellite = data.getSatFromFreq(frequency);
                if (satellite instanceof com.hbm.saveddata.satellites.SatelliteMiner miner) {
                    dock.unloadCargo(serverLevel, miner.getCargo());
                }
            }
        }

        dock.ejectInto(pos.offset(2, 0, 0));
        dock.ejectInto(pos.offset(-2, 0, 0));
        dock.ejectInto(pos.offset(0, 0, 2));
        dock.ejectInto(pos.offset(0, 0, -2));
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return LegacyMachineRenderBounds.visibleMultiblockOr(this, super.getRenderBoundingBox());
    }

    private void unloadCargo(ServerLevel level, String pool) {
        int itemAmount = level.random.nextInt(6) + 10;
        Vec3 origin = Vec3.atCenterOf(worldPosition);
        for (int i = 0; i < itemAmount; i++) {
            addToOutput(HbmItemPoolRegistry.getStack(level, pool, origin));
        }
    }

    private void addToOutput(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        // TileEntityMachineSatDock#addToInv compares only item and metadata.
        // In particular it neither compares nor preserves an incoming NBT payload:
        // matching output keeps its existing NBT, while a new output is one bare item.
        for (int slot = 0; slot < OUTPUT_SLOT_COUNT; slot++) {
            ItemStack current = items.getStackInSlot(slot);
            if (current.is(stack.getItem()) && current.getDamageValue() == stack.getDamageValue()
                    && current.getCount() < current.getMaxStackSize()) {
                int transferred = Math.min(current.getMaxStackSize() - current.getCount(), stack.getCount());
                current.grow(transferred);
                items.setStackInSlot(slot, current);
                stack.shrink(transferred);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }

        for (int slot = 0; slot < OUTPUT_SLOT_COUNT; slot++) {
            if (items.getStackInSlot(slot).isEmpty()) {
                ItemStack bareOutput = new ItemStack(stack.getItem(), 1);
                bareOutput.setDamageValue(stack.getDamageValue());
                items.setStackInSlot(slot, bareOutput);
                return;
            }
        }
    }

    private void ejectInto(BlockPos targetPos) {
        if (level == null) {
            return;
        }
        BlockEntity target = level.getBlockEntity(targetPos);
        if (target == null) {
            return;
        }
        // TileEntityMachineSatDock only accepted IInventory.  Container is its
        // modern equivalent here: retain the old slot order, merge semantics
        // and canPlaceItem boundary, but do not add a new item-handler-only
        // output path that the legacy dock could not use.
        if (target instanceof Container container) {
            HbmInventoryUtil.moveSingleItemFromHandlerToContainer(items, 0, OUTPUT_SLOT_COUNT - 1, container);
        }
    }

    @Override
    public Component getDisplayName() {
        if (customName != null && !customName.isEmpty()) {
            return Component.literal(customName);
        }
        return Component.translatable("container.satDock");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new SatelliteDockMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsToTag(tag, items);
        // TileEntityMachineSatDock writes every non-null customName, while its
        // hasCustomInventoryName equivalent still hides an empty display name.
        if (customName != null) {
            tag.putString(TAG_CUSTOM_NAME, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItems(tag, items);
        customName = tag.getString(TAG_CUSTOM_NAME);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    private static final class ExtractOnlySidedItemHandler implements IItemHandler {
        private final ItemStackHandler items;
        private final int[] slots;

        private ExtractOnlySidedItemHandler(ItemStackHandler items, int[] slots) {
            this.items = items;
            this.slots = slots;
        }

        @Override
        public int getSlots() {
            return slots.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return items.getStackInSlot(mapSlot(slot));
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return items.extractItem(mapSlot(slot), amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return items.getSlotLimit(mapSlot(slot));
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        private int mapSlot(int slot) {
            if (slot < 0 || slot >= slots.length) {
                throw new IndexOutOfBoundsException(slot);
            }
            return slots[slot];
        }
    }
}
