package com.hbm.ntm.menu;

import com.hbm.ntm.item.AmmoBagItem;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class AmmoBagMenu extends LegacyItemBagMenu {
    public AmmoBagMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        super(ModMenuTypes.AMMO_BAG.get(), containerId, playerInventory, data, AmmoBagItem.SLOT_COUNT,
                AmmoBagMenu::canStoreAmmo);
        addSlots();
    }

    public AmmoBagMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.AMMO_BAG.get(), containerId, playerInventory, hand, AmmoBagItem.SLOT_COUNT,
                AmmoBagMenu::canStoreAmmo);
        addSlots();
    }

    @Override
    protected Supplier<? extends Item> bagItem() {
        return () -> getBagStack().is(ModItems.AMMO_BAG_INFINITE.get())
                ? ModItems.AMMO_BAG_INFINITE.get()
                : ModItems.AMMO_BAG.get();
    }

    private void addSlots() {
        addBagSlots(53, 18, 2, 4);
        addLegacyPlayerInventory(82, 140);
    }

    private static boolean canStoreAmmo(ItemStack stack) {
        return !stack.isEmpty() && !stack.hasTag();
    }
}
