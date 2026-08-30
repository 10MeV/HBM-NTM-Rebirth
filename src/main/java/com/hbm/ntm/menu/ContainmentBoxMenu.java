package com.hbm.ntm.menu;

import com.hbm.ntm.block.CrateBlock;
import com.hbm.ntm.item.ContainmentBoxItem;
import com.hbm.ntm.item.MassStorageBlockItem;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ContainmentBoxMenu extends LegacyItemBagMenu {
    public ContainmentBoxMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        super(ModMenuTypes.CONTAINMENT_BOX.get(), containerId, playerInventory, data,
                ContainmentBoxItem.SLOT_COUNT, ContainmentBoxItem.SLOT_STACK_LIMIT,
                ContainmentBoxMenu::canStoreItem);
        addSlots();
    }

    public ContainmentBoxMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenuTypes.CONTAINMENT_BOX.get(), containerId, playerInventory, hand,
                ContainmentBoxItem.SLOT_COUNT, ContainmentBoxItem.SLOT_STACK_LIMIT,
                ContainmentBoxMenu::canStoreItem);
        addSlots();
    }

    @Override
    protected Supplier<? extends Item> bagItem() {
        return ModItems.CONTAINMENT_BOX;
    }

    private void addSlots() {
        addBagSlots(43, 18, 4, 5);
        addLegacyPlayerInventory(104, 162);
    }

    private static boolean canStoreItem(ItemStack stack) {
        return !(stack.getItem() instanceof MassStorageBlockItem)
                && !(stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof CrateBlock);
    }
}
