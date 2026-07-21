package com.hbm.ntm.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Item-side bridge for the legacy ItemRenderLibrary boxcar OBJ renderer. */
public final class BoxcarBlockItem extends BlockItem {
    public BoxcarBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptBoxcar", consumer);
    }
}
