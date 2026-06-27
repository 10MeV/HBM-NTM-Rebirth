package com.hbm.ntm.blockentity;

import com.hbm.ntm.compat.CompatEnergyControl;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.menu.RtgMenu;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.RtgPelletRuntime;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RtgBlockEntity extends HbmEnergyBlockEntity implements MenuProvider {
    public static final int SLOT_COUNT = 15;
    public static final long POWER_MAX = 100_000L;
    private static final String TAG_ITEMS = "items";
    private static final String TAG_POWER = "power";
    private static final String TAG_HEAT = "heat";
    private static final String TAG_NAME = "name";
    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isRtgPellet(stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new AccessibleItemHandler(items));
    private int heat;
    private String customName;

    public RtgBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RTG.get(), pos, state, new HbmEnergyStorage(POWER_MAX, 0L, POWER_MAX));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RtgBlockEntity rtg) {
        if (level.isClientSide) {
            return;
        }
        long oldPower = rtg.getPower();
        int oldHeat = rtg.heat;

        if (rtg.getPower() > 0L) {
            HbmEnergyUtil.tryProvideToAllNeighbors(level, pos, rtg.energy);
        }
        rtg.heat = Math.min(rtg.calculateHeat(), RtgPelletRuntime.heatMax());
        rtg.setPower(rtg.getPower() + rtg.heat * 5L);

        if (oldPower != rtg.getPower() || oldHeat != rtg.heat) {
            rtg.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        rtg.networkPackNT(50);
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getHeat() {
        return heat;
    }

    public int getHeatBarHeight(int maxHeight) {
        return heat <= 0 ? 0 : heat * maxHeight / RtgPelletRuntime.heatMax();
    }

    public int getPowerBarHeight(int maxHeight) {
        return getPower() <= 0L ? 0 : (int) (getPower() * maxHeight / POWER_MAX);
    }

    public long getProduction() {
        return heat * 5L;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public Component getDisplayName() {
        return customName != null && !customName.isBlank()
                ? Component.literal(customName)
                : Component.translatableWithFallback("container.rtg", "RT Generator");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new RtgMenu(containerId, inventory, this);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.OUTPUT;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
            @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putLong(TAG_POWER, getPower());
        tag.putInt(TAG_HEAT, heat);
        if (customName != null && !customName.isBlank()) {
            tag.putString(TAG_NAME, customName);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        if (tag.contains(TAG_POWER)) {
            setPower(tag.getLong(TAG_POWER));
        }
        heat = Math.max(0, tag.getInt(TAG_HEAT));
        customName = tag.contains(TAG_NAME) ? tag.getString(TAG_NAME) : null;
    }

    @Override
    public CompoundTag getClientSyncTag() {
        CompoundTag tag = super.getClientSyncTag();
        tag.putLong(TAG_POWER, getPower());
        tag.putInt(TAG_HEAT, heat);
        return tag;
    }

    @Override
    public void handleClientSyncTag(CompoundTag tag) {
        super.handleClientSyncTag(tag);
        if (tag.contains(TAG_POWER)) {
            setPower(tag.getLong(TAG_POWER));
        }
        if (tag.contains(TAG_HEAT)) {
            heat = Math.max(0, tag.getInt(TAG_HEAT));
        }
    }

    @Override
    public void provideExtraInfo(CompoundTag data) {
        super.provideExtraInfo(data);
        data.putBoolean(CompatEnergyControl.B_ACTIVE, heat > 0);
        data.putDouble(CompatEnergyControl.D_OUTPUT_HE, getProduction());
    }

    private int calculateHeat() {
        return RtgPelletRuntime.updateHeat(items, 0, SLOT_COUNT - 1);
    }

    public static boolean isRtgPellet(ItemStack stack) {
        return RtgPelletRuntime.isPellet(stack);
    }

    public static int rtgHeat(ItemStack stack) {
        return RtgPelletRuntime.heat(stack);
    }

    public static List<Component> acceptedPelletTooltip() {
        return RtgPelletRuntime.acceptedPelletTooltip();
    }

    public static List<FuelSpec> acceptedFuelSpecs() {
        List<FuelSpec> specs = new ArrayList<>();
        RtgPelletRuntime.acceptedFuelSpecs()
                .forEach(spec -> specs.add(new FuelSpec(spec.input(), spec.heat(), spec.powerPerTick())));
        return specs;
    }

    public record FuelSpec(ItemStack input, int heat, int powerPerTick) {
    }

    private static final class AccessibleItemHandler implements IItemHandler {
        private final IItemHandlerModifiable items;

        private AccessibleItemHandler(IItemHandlerModifiable items) {
            this.items = items;
        }

        @Override
        public int getSlots() {
            return SLOT_COUNT;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return isSlot(slot) ? items.getStackInSlot(slot) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isSlot(slot) && isRtgPellet(stack) ? items.insertItem(slot, stack, simulate) : stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return isSlot(slot) ? items.getSlotLimit(slot) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isSlot(slot) && isRtgPellet(stack);
        }

        private boolean isSlot(int slot) {
            return slot >= 0 && slot < SLOT_COUNT;
        }
    }
}
