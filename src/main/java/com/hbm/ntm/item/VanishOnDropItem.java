package com.hbm.ntm.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Exact modern carrier for legacy {@code ItemDrop} items that disappear when dropped. */
public final class VanishOnDropItem extends Item {
    public VanishOnDropItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        entity.discard();
        return true;
    }
}
