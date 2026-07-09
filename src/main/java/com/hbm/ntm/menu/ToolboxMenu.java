package com.hbm.ntm.menu;

import com.hbm.ntm.item.ToolboxItem;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ToolboxMenu extends LegacyItemBagMenu {
    public ToolboxMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        super(ModMenuTypes.TOOLBOX.get(), containerId, playerInventory, data,
                ToolboxItem.SLOT_COUNT, ToolboxMenu::canStoreItem);
        addSlots();
    }

    public ToolboxMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.TOOLBOX.get(), containerId, playerInventory, hand,
                ToolboxItem.SLOT_COUNT, ToolboxMenu::canStoreItem);
        addSlots();
    }

    @Override
    protected Supplier<? extends Item> bagItem() {
        return ModItems.TOOLBOX;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        ToolboxItem.closeInventory(getBagStack(), player);
    }

    private void addSlots() {
        addBagSlots(17, 49, 3, 8);
        addLegacyPlayerInventory(129, 187);
    }

    private static boolean canStoreItem(ItemStack stack) {
        return !stack.is(ModItems.TOOLBOX.get());
    }
}
