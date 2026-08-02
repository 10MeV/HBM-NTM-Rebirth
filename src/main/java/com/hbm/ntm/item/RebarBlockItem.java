package com.hbm.ntm.item;

import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Client bridge for the legacy inventory-only twelve-bar rebar rendering path. */
public final class RebarBlockItem extends BlockItem {
    public RebarBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptRebar", consumer);
    }
}
