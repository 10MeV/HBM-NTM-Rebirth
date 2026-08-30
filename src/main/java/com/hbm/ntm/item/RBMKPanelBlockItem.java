package com.hbm.ntm.item;

import java.util.function.Consumer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Keeps the seven legacy RBMK mini-panel inventory renderers on their own client bridge. */
public class RBMKPanelBlockItem extends net.minecraft.world.item.BlockItem {
    public RBMKPanelBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptRbmkPanel", consumer);
    }
}
