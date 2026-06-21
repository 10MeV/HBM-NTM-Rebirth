package com.hbm.ntm.blockentity;

import com.hbm.ntm.api.block.LegacyLookOverlay;
import com.hbm.ntm.api.block.LegacyLookOverlayLines;
import com.hbm.ntm.api.block.LegacyLookOverlayProvider;
import com.hbm.ntm.api.tile.LegacyUpgradeInfoProvider;
import com.hbm.ntm.energy.HbmEnergySideMode;
import com.hbm.ntm.energy.HbmEnergyStorage;
import com.hbm.ntm.energy.HbmEnergyUtil;
import com.hbm.ntm.item.ItemMachineUpgrade;
import com.hbm.ntm.item.ItemMachineUpgrade.UpgradeType;
import com.hbm.ntm.item.ItemPressStamp;
import com.hbm.ntm.menu.ElectricPressMenu;
import com.hbm.ntm.recipe.LegacyMachineUpgradeManager;
import com.hbm.ntm.recipe.ModRecipes;
import com.hbm.ntm.recipe.PressRecipe;
import com.hbm.ntm.registry.ModBlockEntities;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import com.hbm.ntm.util.HbmInventoryUtil;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
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

public class ElectricPressBlockEntity extends HbmEnergyBlockEntity
        implements MenuProvider, LegacyLookOverlayProvider, LegacyUpgradeInfoProvider {
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_PRESS = "press";
    private static final String TAG_POWER = "power";
    private static final String TAG_RETRACTING = "ret";
    private static final String TAG_DELAY = "delay";

    public static final long MAX_POWER = 50_000L;
    public static final long CONSUMPTION = 100L;
    public static final int MAX_PRESS = 200;

    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_STAMP = 1;
    public static final int SLOT_INPUT = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int SLOT_UPGRADE = 4;
    public static final int SLOT_COUNT = 5;

    private static final Map<UpgradeType, Integer> VALID_UPGRADES = Map.of(UpgradeType.SPEED, 3);

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_BATTERY -> HbmInventoryMenuHelper.isBatteryLike(stack);
                case SLOT_STAMP -> stack.getItem() instanceof ItemPressStamp;
                case SLOT_INPUT -> !(stack.getItem() instanceof ItemPressStamp)
                        && !(stack.getItem() instanceof ItemMachineUpgrade)
                        && !HbmInventoryMenuHelper.isBatteryLike(stack);
                case SLOT_UPGRADE -> stack.getItem() instanceof ItemMachineUpgrade upgrade
                        && upgrade.getUpgradeType() == UpgradeType.SPEED;
                default -> false;
            };
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? super.insertItem(slot, stack, simulate) : stack;
        }
    };
    private final LazyOptional<IItemHandler> automationItems = LazyOptional.of(() -> new AccessibleItemHandler());

    private int press;
    private double renderPress;
    private double lastPress;
    private int syncPress;
    private int turnProgress;
    private boolean retracting;
    private int delay;
    private boolean clientRenderInitialized;

    public ElectricPressBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTRIC_PRESS.get(), pos, state,
                new HbmEnergyStorage(MAX_POWER, MAX_POWER, 0L));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricPressBlockEntity press) {
        if (level.isClientSide) {
            return;
        }

        long oldPower = press.energy.getPower();
        int oldPress = press.press;
        boolean oldRetracting = press.retracting;
        int oldDelay = press.delay;

        press.subscribeEnergyReceiverToAllSides();
        HbmEnergyUtil.chargeStorageFromItem(press.items.getStackInSlot(SLOT_BATTERY),
                press.energy, press.energy.getReceiverSpeed());

        PressRecipe recipe = press.findMatchingPressRecipe(level);
        boolean canProcess = recipe != null && press.energy.getPower() >= CONSUMPTION;

        if ((canProcess || press.retracting || press.delay > 0) && press.energy.getPower() >= CONSUMPTION) {
            press.energy.setPower(press.energy.getPower() - CONSUMPTION);

            if (press.delay > 0) {
                press.delay--;
            } else {
                int speed = 1 + press.upgradeLevels().getLevel(UpgradeType.SPEED);
                int stampSpeed = (int) ((press.retracting ? 20 : 45) * (1.0D + speed / 4.0D));
                if (press.retracting) {
                    press.press -= stampSpeed;
                    if (press.press <= 0) {
                        press.press = 0;
                        press.retracting = false;
                        press.delay = 6 - speed;
                    }
                } else if (canProcess) {
                    press.press += stampSpeed;
                    if (press.press >= MAX_PRESS) {
                        press.finishPressRecipe(recipe);
                        press.retracting = true;
                        press.delay = 6 - speed;
                    }
                } else if (press.press > 0) {
                    press.retracting = true;
                }
            }
        }

        press.networkPackNT(50);
        boolean changed = oldPower != press.energy.getPower()
                || oldPress != press.press
                || oldRetracting != press.retracting
                || oldDelay != press.delay;
        if (changed || level.getGameTime() % 10L == 0L) {
            press.setChanged();
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ElectricPressBlockEntity press) {
        press.lastPress = press.renderPress;
        if (press.turnProgress > 0) {
            press.renderPress += (press.syncPress - press.renderPress) / (double) press.turnProgress;
            press.turnProgress--;
        } else {
            press.renderPress = press.syncPress;
        }
    }

    public ItemStackHandler getItems() {
        return items;
    }

    public int getPress() {
        return press;
    }

    public void setPress(int press) {
        this.press = press;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public double getInterpolatedPress(float partialTick) {
        return Mth.lerp(partialTick, lastPress, renderPress);
    }

    public ItemStack getRenderInputStack() {
        ItemStack stack = items.getStackInSlot(SLOT_INPUT);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack display = stack.copy();
        display.setCount(1);
        return display;
    }

    public List<ItemStack> getDrops() {
        return HbmInventoryMenuHelper.clearToDrops(items);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return LegacyMachineRenderBounds.visibleMultiblockOr(this, super.getRenderBoundingBox());
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
        return Component.translatableWithFallback("container.epress", "Electric Press");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ElectricPressMenu(containerId, inventory, this);
    }

    @Override
    public Map<UpgradeType, Integer> getValidUpgrades() {
        return VALID_UPGRADES;
    }

    @Override
    public void provideInfo(UpgradeType type, int level, List<Component> info, boolean extendedInfo) {
        if (type == UpgradeType.SPEED) {
            info.add(Component.translatableWithFallback(KEY_DELAY, "Delay %s", "-" + (50 * level / 3) + "%"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        HbmInventoryMenuHelper.saveLegacyItemsCompoundToTag(tag, TAG_ITEMS, items);
        tag.putInt(TAG_PRESS, press);
        tag.putLong(TAG_POWER, energy.getPower());
        tag.putBoolean(TAG_RETRACTING, retracting);
        tag.putInt(TAG_DELAY, delay);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        HbmInventoryMenuHelper.loadLegacyOrForgeItemsCompound(tag, TAG_ITEMS, items);
        press = tag.getInt(TAG_PRESS);
        if (tag.contains(TAG_POWER)) {
            energy.setPower(tag.getLong(TAG_POWER));
        }
        retracting = tag.getBoolean(TAG_RETRACTING);
        delay = tag.getInt(TAG_DELAY);

        if (level != null && level.isClientSide) {
            boolean targetChanged = press != syncPress;
            syncPress = press;
            if (!clientRenderInitialized) {
                renderPress = press;
                lastPress = press;
                clientRenderInitialized = true;
            } else if (targetChanged) {
                turnProgress = 2;
            }
        }
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

    private PressRecipe findMatchingPressRecipe(Level level) {
        SimpleContainer container = new SimpleContainer(items.getStackInSlot(SLOT_INPUT),
                items.getStackInSlot(SLOT_STAMP));
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.PRESS.type().get()).stream()
                .filter(recipe -> recipe.matches(container, level))
                .filter(this::canFitOutput)
                .findFirst()
                .orElse(null);
    }

    private boolean canFitOutput(PressRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        return HbmInventoryUtil.doesHandlerHaveSpaceUnchecked(items, SLOT_OUTPUT, SLOT_OUTPUT, result);
    }

    private LegacyMachineUpgradeManager.Levels upgradeLevels() {
        return LegacyMachineUpgradeManager.checkSlots(items, SLOT_UPGRADE, SLOT_UPGRADE, VALID_UPGRADES);
    }

    private void finishPressRecipe(PressRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        HbmInventoryUtil.tryAddItemToHandlerUnchecked(items, SLOT_OUTPUT, SLOT_OUTPUT, result);
        LegacySoundPlayer.playLegacyPressOperate(level, worldPosition, 1.5F, 1.0F);

        items.extractItem(SLOT_INPUT, 1, false);
        ItemStack stamp = items.getStackInSlot(SLOT_STAMP);
        if (stamp.isDamageableItem()) {
            stamp.hurt(1, level.random, null);
            if (stamp.getDamageValue() >= stamp.getMaxDamage()) {
                items.setStackInSlot(SLOT_STAMP, ItemStack.EMPTY);
            }
        }
    }

    private final class AccessibleItemHandler implements IItemHandler {
        private final int[] slots = { SLOT_STAMP, SLOT_INPUT, SLOT_OUTPUT };

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
            return valid(slot) && slots[slot] == SLOT_OUTPUT
                    ? items.extractItem(slots[slot], amount, simulate)
                    : ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return valid(slot) ? items.getSlotLimit(slots[slot]) : 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return valid(slot) && slots[slot] != SLOT_OUTPUT && items.isItemValid(slots[slot], stack);
        }

        private boolean valid(int slot) {
            return slot >= 0 && slot < slots.length;
        }
    }
}
