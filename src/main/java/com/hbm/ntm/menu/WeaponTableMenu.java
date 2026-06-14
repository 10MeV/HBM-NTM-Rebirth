package com.hbm.ntm.menu;

import com.hbm.ntm.bullet.SednaWeaponModInstallManager;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.registry.ModBlocks;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WeaponTableMenu extends AbstractContainerMenu {
    public static final int MOD_SLOT_COUNT = 7;
    public static final int GUN_SLOT = MOD_SLOT_COUNT;
    public static final int PLAYER_INVENTORY_START = GUN_SLOT + 1;
    public static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    public static final int HOTBAR_START = PLAYER_INVENTORY_END;
    public static final int HOTBAR_END = HOTBAR_START + 9;

    private final SimpleContainer mods = new SimpleContainer(MOD_SLOT_COUNT);
    private final SimpleContainer gun = new SimpleContainer(1);
    private final BlockPos pos;
    private int configIndex;

    public WeaponTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos());
    }

    public WeaponTableMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.WEAPON_TABLE.get(), containerId);
        this.pos = pos;

        for (int i = 0; i < MOD_SLOT_COUNT; i++) {
            addSlot(new ModSlot(mods, i, 44 + 18 * i, 108));
        }
        addSlot(new GunSlot(gun, 0, 8, 108));

        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 158, 216);
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return configIndex;
            }

            @Override
            public void set(int value) {
                configIndex = value;
            }
        });
    }

    public ItemStack getGunStack() {
        return gun.getItem(0);
    }

    public ItemStack getModStack(int slot) {
        return slot >= 0 && slot < MOD_SLOT_COUNT ? mods.getItem(slot) : ItemStack.EMPTY;
    }

    public int getConfigIndex() {
        return configIndex;
    }

    public int getConfigCount() {
        ItemStack stack = getGunStack();
        if (stack.getItem() instanceof SednaGunItem gunItem) {
            return gunItem.gunConfig().configs().size();
        }
        return 0;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id < 0 || id >= getConfigCount()) {
            return false;
        }
        switchConfig(id);
        return true;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockState(pos).is(ModBlocks.MACHINE_WEAPON_TABLE.get())
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return result;
        }
        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index == GUN_SLOT) {
            installDisplayedMods(stack);
            clearInstalledDisplayedMods(stack);
            configIndex = 0;
        }

        if (index <= GUN_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        } else if (stack.getItem() instanceof SednaGunItem) {
            if (!moveItemStackTo(stack, GUN_SLOT, GUN_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!moveItemStackTo(stack, 0, MOD_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        }

        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide) {
            return;
        }
        for (int i = 0; i < mods.getContainerSize(); i++) {
            ItemStack stack = mods.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                player.drop(stack, false);
            }
        }
        ItemStack gunStack = gun.removeItemNoUpdate(0);
        if (!gunStack.isEmpty()) {
            SednaWeaponModInstallManager.uninstall(gunStack, configIndex);
            player.drop(gunStack, false);
        }
    }

    private void switchConfig(int nextConfigIndex) {
        ItemStack gunStack = getGunStack();
        if (gunStack.isEmpty()) {
            configIndex = 0;
            mods.clearContent();
            broadcastChanges();
            return;
        }
        installDisplayedMods(gunStack);
        clearInstalledDisplayedMods(gunStack);
        configIndex = nextConfigIndex;
        loadInstalledMods(gunStack);
        broadcastChanges();
    }

    private void loadInstalledMods(ItemStack gunStack) {
        mods.clearContent();
        ItemStack[] installed = SednaWeaponModInstallManager.getUpgradeItems(gunStack, configIndex);
        for (int i = 0; i < Math.min(installed.length, MOD_SLOT_COUNT); i++) {
            mods.setItem(i, installed[i]);
        }
    }

    private void installDisplayedMods(ItemStack gunStack) {
        ItemStack[] stacks = new ItemStack[MOD_SLOT_COUNT];
        for (int i = 0; i < MOD_SLOT_COUNT; i++) {
            stacks[i] = mods.getItem(i);
        }
        SednaWeaponModInstallManager.install(gunStack, configIndex, stacks);
    }

    private void refreshDisplayedInstall() {
        ItemStack gunStack = getGunStack();
        if (!gunStack.isEmpty()) {
            installDisplayedMods(gunStack);
        }
    }

    private void clearInstalledDisplayedMods(ItemStack gunStack) {
        for (int i = 0; i < MOD_SLOT_COUNT; i++) {
            ItemStack mod = mods.getItem(i);
            if (SednaWeaponModInstallManager.isApplicable(gunStack, mod, configIndex, false)) {
                mods.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private final class GunSlot extends Slot {
        private GunSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return getItem().isEmpty() && stack.getItem() instanceof SednaGunItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void set(ItemStack stack) {
            configIndex = 0;
            mods.clearContent();
            super.set(stack);
            if (!stack.isEmpty()) {
                loadInstalledMods(stack);
            }
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            installDisplayedMods(stack);
            clearInstalledDisplayedMods(stack);
            configIndex = 0;
        }
    }

    private final class ModSlot extends Slot {
        private ModSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return SednaWeaponModInstallManager.isApplicable(getGunStack(), stack, configIndex, true);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }

        @Override
        public void set(ItemStack stack) {
            super.set(stack);
            refreshDisplayedInstall();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            refreshDisplayedInstall();
        }
    }
}
