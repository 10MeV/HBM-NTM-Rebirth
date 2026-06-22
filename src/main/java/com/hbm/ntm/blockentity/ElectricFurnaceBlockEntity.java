package com.hbm.ntm.blockentity;

import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.tile.LegacyUpgradeInfoProvider;
import com.hbm.ntm.energy.IBatteryItem;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.menu.ElectricFurnaceMenu;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElectricFurnaceBlockEntity extends HbmEnergyBlockEntity
        implements MenuProvider, LegacyLookOverlayProvider, LegacyUpgradeInfoProvider {
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_POWER = "power";
    private static final String TAG_PROGRESS = "progress";
    private static final String TAG_ACTIVE = "active";

    public static final long MAX_POWER = 100_000L;
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_INPUT = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_UPGRADE = 3;
    public static final int SLOT_COUNT = 4;
    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(
            UpgradeType.SPEED, 3,
            UpgradeType.POWER, 3);

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> isLegacyBattery(stack);
                case SLOT_INPUT -> getSmeltingResult(stack).isPresent();
                case SLOT_UPGRADE -> stack.getItem() instanceof ItemMachineUpgrade upgrade
                        && (upgrade.getUpgradeType() == UpgradeType.SPEED
                        || upgrade.getUpgradeType() == UpgradeType.POWER);
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };

    private final LazyOptional<IItemHandler> automationItems = LazyOptional.of(AccessibleItemHandler::new);
    private int progress;
    private int maxProgress = 100;
    private int consumption = 50;
    private int cooldown;
    private boolean active;

    public ElectricFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_FURNACE.get(), pos, state,
                new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity furnace) {
        if (level.isClientSide) {
            return;
        }
        long oldPower = furnace.energy.getPower();
        int oldProgress = furnace.progress;
        boolean oldActive = furnace.active;

        if (furnace.cooldown > 0) {
            furnace.cooldown--;
        }
        furnace.subscribeEnergyReceiverToAllSides();
        HbmEnergyUtil.chargeStorageFromItem(furnace.items.getStackInSlot(SLOT_BATTERY), furnace.energy,
                furnace.energy.getReceiverSpeed());
        furnace.recalculateUpgrades();

        if (!furnace.hasPower()) {
            furnace.cooldown = 20;
        }

        if (furnace.hasPower() && furnace.canProcess()) {
            furnace.progress++;
            furnace.energy.setPower(Math.max(0L, furnace.energy.getPower() - furnace.consumption));
            furnace.active = true;
            if (level.getGameTime() % 20L == 0L) {
                PollutionHandler.incrementPollution(level, pos, PollutionHandler.PollutionType.SOOT,
                        PollutionHandler.SOOT_PER_SECOND);
            }
            if (furnace.progress >= furnace.maxProgress) {
                furnace.progress = 0;
                furnace.processItem();
            }
        } else {
            furnace.progress = 0;
            furnace.active = false;
        }

        furnace.networkPackNT(50);
        if (oldPower != furnace.energy.getPower() || oldProgress != furnace.progress || oldActive != furnace.active
                || level.getGameTime() % 10L == 0L) {
            furnace.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ElectricFurnaceBlockEntity furnace) {
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getProgress() {
        return progress;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public int getConsumption() {
        return consumption;
    }

    public boolean isActive() {
        return active;
    }

    public boolean hasPower() {
        return energy.getPower() >= consumption;
    }

    public int getPowerBarHeight(int maxHeight) {
        return energy.getMaxPower() <= 0L ? 0 : (int) (energy.getPower() * maxHeight / energy.getMaxPower());
    }

    public int getProgressWidth(int maxWidth) {
        return maxProgress <= 0 ? 0 : progress * maxWidth / maxProgress;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    protected HbmEnergySideMode getEnergySideMode(@Nullable Direction side) {
        return HbmEnergySideMode.INPUT;
    }

    @Override
    public LegacyLookOverlay getLookOverlay(Level level, BlockPos viewedPos) {
        return LegacyLookOverlay.forBlock(this, List.of(
                LegacyLookOverlayLines.energyStored(energy.getPower(), energy.getMaxPower())));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatableWithFallback("container.electricFurnace", "Electric Furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ElectricFurnaceMenu(containerId, inventory, this);
    }

    @Override
    public Map<UpgradeType, Integer> getValidUpgrades() {
        return VALID_UPGRADES;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<Component> info, boolean extendedInfo) {
        if (type == UpgradeType.SPEED) {
            info.add(Component.translatableWithFallback(KEY_DELAY, "Delay %s", "-" + (level * 25) + "%"));
            info.add(Component.translatableWithFallback(KEY_CONSUMPTION, "Consumption %s",
                    "+" + (level * 100) + "%"));
        } else if (type == UpgradeType.POWER) {
            info.add(Component.translatableWithFallback(KEY_CONSUMPTION, "Consumption %s",
                    "-" + (level * 30) + "%"));
            info.add(Component.translatableWithFallback(KEY_DELAY, "Delay %s", "+" + (level * 10) + "%"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putInt(TAG_PROGRESS, progress);
        tag.putBoolean(TAG_ACTIVE, active);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        progress = tag.getInt(TAG_PROGRESS);
        active = tag.getBoolean(TAG_ACTIVE);
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
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        automationItems.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return automationItems.cast();
        }
        return super.getCapability(capability, side);
    }

    private void recalculateUpgrades() {
        consumption = 50;
        maxProgress = 100;
        LegacyMachineUpgradeManager.Levels upgrades = LegacyMachineUpgradeManager.checkSlots(items,
                SLOT_UPGRADE, SLOT_UPGRADE, VALID_UPGRADES);
        int speedLevel = upgrades.getLevel(UpgradeType.SPEED);
        int powerLevel = upgrades.getLevel(UpgradeType.POWER);
        maxProgress -= speedLevel * 25;
        consumption += speedLevel * 50;
        maxProgress += powerLevel * 10;
        consumption -= powerLevel * 15;
        maxProgress = Math.max(1, maxProgress);
        consumption = Math.max(1, consumption);
    }

    private boolean canProcess() {
        if (items.getStackInSlot(SLOT_INPUT).isEmpty() || cooldown > 0) {
            return false;
        }
        ItemStack result = smeltingResult();
        return !result.isEmpty()
                && HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(items, SLOT_OUTPUT, SLOT_OUTPUT, result);
    }

    private void processItem() {
        if (!canProcess()) {
            return;
        }
        ItemStack result = smeltingResult();
        HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, SLOT_OUTPUT, SLOT_OUTPUT, result);
        items.extractItem(SLOT_INPUT, 1, false);
    }

    private ItemStack smeltingResult() {
        return getSmeltingResult(items.getStackInSlot(SLOT_INPUT)).map(recipe ->
                recipe.getResultItem(level.registryAccess()).copy()).orElse(ItemStack.EMPTY);
    }

    private java.util.Optional<SmeltingRecipe> getSmeltingResult(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return java.util.Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(stack), level);
    }

    public static boolean isLegacyBattery(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IBatteryItem;
    }

    private final class AccessibleItemHandler implements IItemHandler {
        private final int[] slots = { SLOT_BATTERY, SLOT_INPUT, SLOT_OUTPUT };

        @Override
        public int getSlots() {
            return slots.length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return valid(slot) ? items.getStackInSlot(slots[slot]) : ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!valid(slot) || slots[slot] == SLOT_OUTPUT) {
                return stack;
            }
            return items.insertItem(slots[slot], stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!valid(slot)) {
                return ItemStack.EMPTY;
            }
            int target = slots[slot];
            if (target == SLOT_OUTPUT || target == SLOT_BATTERY) {
                return items.extractItem(target, amount, simulate);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return valid(slot) ? items.getSlotLimit(slots[slot]) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return valid(slot) && items.isItemValid(slots[slot], stack);
        }

        private boolean valid(int slot) {
            return slot >= 0 && slot < slots.length;
        }
    }
}
