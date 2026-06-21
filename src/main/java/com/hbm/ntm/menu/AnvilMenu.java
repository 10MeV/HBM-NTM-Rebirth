package com.hbm.ntm.menu;

import com.hbm.ntm.network.HbmNetworkActions;
import com.hbm.ntm.network.HbmTypedMenuActionReceiver;
import com.hbm.ntm.recipe.AnvilConstructionRecipe;
import com.hbm.ntm.recipe.AnvilConstructionRecipeRuntime;
import com.hbm.ntm.recipe.AnvilSmithingRecipeRuntime;
import com.hbm.ntm.registry.ModMenuTypes;
import com.hbm.ntm.util.HbmInventoryMenuHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AnvilMenu extends AbstractContainerMenu implements HbmTypedMenuActionReceiver {
    private static final int SMITHING_SLOT_COUNT = 3;
    private static final int INPUT_LEFT = 0;
    private static final int INPUT_RIGHT = 1;
    private static final int OUTPUT = 2;
    private static final int PLAYER_INVENTORY_START = SMITHING_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int HOTBAR_END = PLAYER_INVENTORY_END + 9;

    private final Inventory playerInventory;
    private final int tier;
    private final SimpleContainer input = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            updateSmithingOutput();
        }
    };
    private final SimpleContainer output = new SimpleContainer(1);

    public AnvilMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, data.readVarInt());
    }

    public AnvilMenu(int containerId, Inventory playerInventory, int tier) {
        super(ModMenuTypes.ANVIL.get(), containerId);
        this.playerInventory = playerInventory;
        this.tier = tier;

        addSlot(new SmithingInputSlot(input, 0, 17, 27));
        addSlot(new SmithingInputSlot(input, 1, 53, 27));
        addSlot(new Slot(output, 0, 89, 27) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                if (AnvilSmithingRecipeRuntime.consume(input, tier)) {
                    updateSmithingOutput();
                }
            }
        });
        HbmInventoryMenuHelper.addPlayerInventoryAndHotbar(this::addSlot, playerInventory, 8, 140, 198);
        updateSmithingOutput();
    }

    public int tier() {
        return tier;
    }

    public Inventory playerInventory() {
        return playerInventory;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == playerInventory.player;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == OUTPUT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, original);
            slot.onTake(player, original);
        } else if (index <= INPUT_RIGHT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, INPUT_LEFT, INPUT_RIGHT + 1, false)) {
            return ItemStack.EMPTY;
        }
        HbmInventoryMenuHelper.finishQuickMove(slot, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        clearContainer(player, input);
    }

    @Override
    public boolean canReceiveTypedMenuAction(ServerPlayer player, ResourceLocation actionType, int value,
            CompoundTag data) {
        if (!HbmNetworkActions.ANVIL_CRAFT.equals(actionType) || player != playerInventory.player) {
            return false;
        }
        int recipeIndex = data.contains("recipeIndex") ? data.getInt("recipeIndex") : value;
        return AnvilConstructionRecipeRuntime.recipeByIndex(player.level(), tier, recipeIndex)
                .filter(recipe -> AnvilConstructionRecipeRuntime.canCraft(player, recipe, tier))
                .isPresent();
    }

    @Override
    public void handleTypedMenuAction(ServerPlayer player, ResourceLocation actionType, int value, CompoundTag data) {
        if (!HbmNetworkActions.ANVIL_CRAFT.equals(actionType)) {
            return;
        }
        int recipeIndex = data.contains("recipeIndex") ? data.getInt("recipeIndex") : value;
        boolean batch = data.getInt("mode") == 1;
        AnvilConstructionRecipeRuntime.recipeByIndex(player.level(), tier, recipeIndex)
                .ifPresent(recipe -> craftConstruction(player, recipe, batch));
    }

    private void craftConstruction(ServerPlayer player, AnvilConstructionRecipe recipe, boolean batch) {
        AnvilConstructionRecipeRuntime.craft(player, recipe, tier, batch);
        player.inventoryMenu.broadcastChanges();
    }

    private void updateSmithingOutput() {
        output.setItem(0, AnvilSmithingRecipeRuntime.result(input, tier));
    }

    private class SmithingInputSlot extends Slot {
        private SmithingInputSlot(SimpleContainer container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            updateSmithingOutput();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            super.onTake(player, stack);
            updateSmithingOutput();
        }
    }
}
