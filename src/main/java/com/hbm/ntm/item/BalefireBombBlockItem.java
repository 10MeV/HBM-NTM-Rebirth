package com.hbm.ntm.item;

import com.hbm.ntm.client.renderer.BalefireBombItemRenderer;
import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class BalefireBombBlockItem extends BlockItem {
    public BalefireBombBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> BalefireBombItemRenderer.INSTANCE);
    }
}
