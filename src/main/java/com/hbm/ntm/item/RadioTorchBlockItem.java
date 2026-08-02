package com.hbm.ntm.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Uses the legacy RTTY OBJ for item-frame, hand, and inventory rendering.
 */
public class RadioTorchBlockItem extends BlockItem {
    public RadioTorchBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptVisibleMachine", consumer);
    }
}
