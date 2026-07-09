package com.hbm.ntm.menu;

import com.hbm.ntm.item.PlasticBagItem;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class PlasticBagMenu extends LegacyItemBagMenu {
    public PlasticBagMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        super(ModMenuTypes.PLASTIC_BAG.get(), containerId, playerInventory, data,
                PlasticBagItem.SLOT_COUNT, PlasticBagItem.SLOT_STACK_LIMIT, PlasticBagMenu::canStoreItem);
        addSlots();
    }

    public PlasticBagMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.PLASTIC_BAG.get(), containerId, playerInventory, hand,
                PlasticBagItem.SLOT_COUNT, PlasticBagItem.SLOT_STACK_LIMIT, PlasticBagMenu::canStoreItem);
        addSlots();
    }

    @Override
    protected Supplier<? extends Item> bagItem() {
        return ModItems.PLASTIC_BAG;
    }

    private void addSlots() {
        addBagSlots(80, 65, 1, 1);
        addLegacyPlayerInventory(134, 192);
    }

    private static boolean canStoreItem(ItemStack stack) {
        return true;
    }
}
