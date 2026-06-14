package com.hbm.ntm.menu;

import com.hbm.ntm.item.CasingBagItem;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class CasingBagMenu extends LegacyItemBagMenu {
    public CasingBagMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        super(ModMenuTypes.CASING_BAG.get(), containerId, playerInventory, data, CasingBagItem.SLOT_COUNT,
                CasingBagMenu::canStoreCasing);
        addSlots();
    }

    public CasingBagMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.CASING_BAG.get(), containerId, playerInventory, hand, CasingBagItem.SLOT_COUNT,
                CasingBagMenu::canStoreCasing);
        addSlots();
    }

    @Override
    protected Supplier<? extends Item> bagItem() {
        return ModItems.CASING_BAG;
    }

    private void addSlots() {
        addBagSlots(44, 18, 3, 5);
        addLegacyPlayerInventory(100, 158);
    }

    private static boolean canStoreCasing(ItemStack stack) {
        return false;
    }
}
