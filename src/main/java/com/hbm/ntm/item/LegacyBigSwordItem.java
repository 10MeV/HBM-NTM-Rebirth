package com.hbm.ntm.item;

import java.util.function.Consumer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** 1.7.10 {@code BigSword}: an emerald/diamond-tier sword with no active ability. */
public class LegacyBigSwordItem extends SwordItem {
    public LegacyBigSwordItem(Item.Properties properties) {
        // The old ItemSword(ToolMaterial.EMERALD) has the vanilla diamond sword combat contract.
        super(Tiers.DIAMOND, 3, -2.4F, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptBigSword", consumer);
    }
}
