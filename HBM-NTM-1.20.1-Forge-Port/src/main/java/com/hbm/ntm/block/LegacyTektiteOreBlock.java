package com.hbm.ntm.block;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class LegacyTektiteOreBlock extends Block {
    public LegacyTektiteOreBlock(Properties properties) {
        super(properties);
    }

    public Item droppedItem() {
        return ModItems.legacyItem("powder_tektite").get();
    }
}
