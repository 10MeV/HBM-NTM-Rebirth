package com.hbm.ntm.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Client renderer bridge for the legacy ItemRenderSatelliteReceiver transforms. */
public final class PoleSatelliteReceiverBlockItem extends BlockItem {
    public PoleSatelliteReceiverBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptPoleSatelliteReceiver", consumer);
    }
}
