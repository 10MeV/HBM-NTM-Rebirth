package com.hbm.ntm.item;

import com.hbm.ntm.client.renderer.LegacyFloodlightItemRenderer;
import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class LegacyFloodlightBlockItem extends BlockItem {
    public LegacyFloodlightBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyFloodlightItemRenderer.INSTANCE);
    }
}
