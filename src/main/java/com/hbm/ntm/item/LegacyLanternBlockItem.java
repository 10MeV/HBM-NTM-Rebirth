package com.hbm.ntm.item;

import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import com.hbm.ntm.client.renderer.LegacyLanternItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class LegacyLanternBlockItem extends BlockItem {
    public LegacyLanternBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyLanternItemRenderer.INSTANCE);
    }
}
