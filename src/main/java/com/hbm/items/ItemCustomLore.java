package com.hbm.items;

import com.hbm.ntm.item.LegacyLoreItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

/**
 * Legacy 1.7.10 package bridge for simple lore-bearing items.
 */
@Deprecated(forRemoval = false)
public class ItemCustomLore extends LegacyLoreItem {
    private Rarity rarity;
    private boolean hasEffect;

    public ItemCustomLore() {
        this(new Item.Properties());
    }

    public ItemCustomLore(Item.Properties properties) {
        super(properties);
    }

    public ItemCustomLore setRarity(Rarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public ItemCustomLore setEffect() {
        this.hasEffect = true;
        return this;
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return rarity != null ? rarity : super.getRarity(stack);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasEffect || super.isFoil(stack);
    }
}
