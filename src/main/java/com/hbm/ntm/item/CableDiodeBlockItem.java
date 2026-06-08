package com.hbm.ntm.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class CableDiodeBlockItem extends BlockItem {
    public CableDiodeBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        com.hbm.ntm.client.renderer.LegacyItemRendererBridge.accept(consumer,
                () -> com.hbm.ntm.client.renderer.CableDiodeItemRenderer.INSTANCE);
    }
}
