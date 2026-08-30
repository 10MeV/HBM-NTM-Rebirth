package com.hbm.ntm.menu;

import com.hbm.ntm.recipe.LemegetonRecipeRuntime;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** One-input portable processor matching the old ContainerLemegeton contract. */
public class LemegetonMenu extends AbstractContainerMenu {
    private static final int RESULT_SLOT = 0;
    private static final int INPUT_SLOT = 1;
    private static final int PLAYER_SLOT_START = 2;

    private final Player player;
    private final SimpleContainer input = new SimpleContainer(1);
    private final SimpleContainer result = new SimpleContainer(1);

    public LemegetonMenu(int containerId, Inventory inventory, FriendlyByteBuf data) {
        this(containerId, inventory);
    }

    public LemegetonMenu(int containerId, Inventory inventory) {
        super(ModMenuTypes.LEMEGETON.get(), containerId);
        this.player = inventory.player;
        input.addListener(container -> refreshResult());
        addSlot(new Slot(result, 0, 107, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(input, 0, 49, 35));
        addPlayerInventory(inventory);
        refreshResult();
    }

    public ItemStack inputStack() {
        return input.getItem(0);
    }

    public ItemStack resultStack() {
        return result.getItem(0);
    }

    @Override
    public boolean stillValid(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(ModItems.BOOK_LEMEGETON.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == RESULT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
            consumeInput();
        } else if (index == INPUT_SLOT) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (!player.level().isClientSide) {
            ItemStack left = input.removeItemNoUpdate(0);
            if (!left.isEmpty()) {
                player.drop(left, false);
            }
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == input) {
            refreshResult();
        }
    }

    @Override
    public void clicked(int slotId, int button, net.minecraft.world.inventory.ClickType clickType, Player player) {
        boolean takingResult = slotId == RESULT_SLOT && slots.get(RESULT_SLOT).hasItem();
        super.clicked(slotId, button, clickType, player);
        if (takingResult && !slots.get(RESULT_SLOT).hasItem()) {
            consumeInput();
        }
    }

    private void consumeInput() {
        input.removeItem(0, 1);
        refreshResult();
    }

    private void refreshResult() {
        result.setItem(0, LemegetonRecipeRuntime.result(player.level(), input.getItem(0)));
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }
}
