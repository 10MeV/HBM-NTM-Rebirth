package com.hbm.ntm.menu;

import com.mojang.datafixers.util.Pair;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.armor.ArmorModItem;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class ArmorTableMenu extends AbstractContainerMenu {
    public static final int UPGRADE_SLOT_COUNT = ArmorModHandler.MOD_SLOTS;
    public static final int ARMOR_SLOT = UPGRADE_SLOT_COUNT;
    public static final int PLAYER_ARMOR_START = ARMOR_SLOT + 1;
    public static final int PLAYER_ARMOR_END = PLAYER_ARMOR_START + 4;
    public static final int PLAYER_INVENTORY_START = PLAYER_ARMOR_END;
    public static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    public static final int HOTBAR_START = PLAYER_INVENTORY_END;
    public static final int HOTBAR_END = HOTBAR_START + 9;

    private final SimpleContainer upgrades = new SimpleContainer(UPGRADE_SLOT_COUNT);
    private final SimpleContainer armor = new SimpleContainer(1);

    public ArmorTableMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readBlockPos());
    }

    public ArmorTableMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.ARMOR_TABLE.get(), containerId);

        addSlot(new UpgradeSlot(upgrades, ArmorModHandler.helmet_only, 48, 27));
        addSlot(new UpgradeSlot(upgrades, ArmorModHandler.plate_only, 84, 27));
        addSlot(new UpgradeSlot(upgrades, ArmorModHandler.legs_only, 120, 27));
        addSlot(new UpgradeSlot(upgrades, ArmorModHandler.boots_only, 156, 45));
        addSlot(new UpgradeSlot(upgrades, ArmorModHandler.servos, 156, 81));
        addSlot(new UpgradeSlot(upgrades, ArmorModHandler.cladding, 120, 99));
        addSlot(new UpgradeSlot(upgrades, ArmorModHandler.kevlar, 84, 99));
        addSlot(new UpgradeSlot(upgrades, ArmorModHandler.extra, 48, 99));
        addSlot(new UpgradeSlot(upgrades, ArmorModHandler.battery, 30, 63));
        addSlot(new ArmorSlot(armor, 0, 66, 63));

        addPlayerArmorSlots(playerInventory);
        addPlayerInventory(playerInventory);
        addHotbar(playerInventory);
    }

    public ItemStack getUpgradeStack(int slot) {
        if (slot < 0 || slot >= UPGRADE_SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        return upgrades.getItem(slot);
    }

    public ItemStack getArmorStack() {
        return armor.getItem(0);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
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

        if (index <= ARMOR_SLOT) {
            if (index == ARMOR_SLOT && moveItemStackTo(stack, PLAYER_ARMOR_START, PLAYER_ARMOR_END, false)) {
                slot.onTake(player, stack);
            } else if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            } else {
                slot.onTake(player, stack);
            }
        } else if (stack.getItem() instanceof ArmorItem) {
            if (!moveItemStackTo(stack, ARMOR_SLOT, ARMOR_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (stack.getItem() instanceof ArmorModItem armorMod && slots.get(ARMOR_SLOT).hasItem()) {
            int targetSlot = armorMod.slot().legacyIndex();
            if (!slots.get(targetSlot).mayPlace(stack)
                    || !moveItemStackTo(stack, targetSlot, targetSlot + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
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
        ItemStack armorStack = armor.getItem(0);
        for (int i = 0; i < upgrades.getContainerSize(); i++) {
            ItemStack stack = upgrades.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                player.drop(stack, false);
                ArmorModHandler.removeMod(armorStack, i);
            }
        }
        armorStack = armor.removeItemNoUpdate(0);
        if (!armorStack.isEmpty()) {
            player.drop(armorStack, false);
        }
    }

    private void addPlayerArmorSlots(Inventory inventory) {
        EquipmentSlot[] equipmentSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };
        for (int i = 0; i < 4; i++) {
            EquipmentSlot equipmentSlot = equipmentSlots[i];
            int inventorySlot = 39 - i;
            addSlot(new Slot(inventory, inventorySlot, 5, 36 + i * 18) {
                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.canEquip(equipmentSlot, inventory.player);
                }

                @Override
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, emptyArmorSlotIcon(equipmentSlot));
                }
            });
        }
    }

    private static ResourceLocation emptyArmorSlotIcon(EquipmentSlot equipmentSlot) {
        return switch (equipmentSlot) {
            case HEAD -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
            case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
            case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
            case FEET -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
            default -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
        };
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 30 + column * 18, 140 + row * 18));
            }
        }
    }

    private void addHotbar(Inventory inventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 30 + column * 18, 198));
        }
    }

    private void clearDisplayedApplicableMods(ItemStack armorStack) {
        for (int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
            ItemStack mod = upgrades.getItem(i);
            if (ArmorModHandler.isApplicable(armorStack, mod)) {
                upgrades.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    private final class ArmorSlot extends Slot {
        private ArmorSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getItem() instanceof ArmorItem;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void set(ItemStack stack) {
            upgrades.clearContent();
            if (!stack.isEmpty()) {
                ItemStack[] mods = ArmorModHandler.pryMods(stack);
                for (int i = 0; i < ArmorModHandler.MOD_SLOTS; i++) {
                    upgrades.setItem(i, mods[i]);
                }
            }
            super.set(stack);
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            clearDisplayedApplicableMods(stack);
        }
    }

    private final class UpgradeSlot extends Slot {
        private final int legacySlot;

        private UpgradeSlot(Container container, int legacySlot, int x, int y) {
            super(container, legacySlot, x, y);
            this.legacySlot = legacySlot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            ItemStack armorStack = armor.getItem(0);
            return stack.getItem() instanceof ArmorModItem armorMod
                    && armorMod.slot().legacyIndex() == legacySlot
                    && ArmorModHandler.isApplicable(armorStack, stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public void set(ItemStack stack) {
            super.set(stack);
            if (!stack.isEmpty()) {
                ArmorModHandler.applyMod(armor.getItem(0), stack);
            }
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            ArmorModHandler.removeMod(armor.getItem(0), legacySlot);
        }
    }
}
