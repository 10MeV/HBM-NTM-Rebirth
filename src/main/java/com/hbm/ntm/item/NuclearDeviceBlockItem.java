package com.hbm.ntm.item;

import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import com.hbm.ntm.client.renderer.NuclearDeviceItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class NuclearDeviceBlockItem extends BlockItem {
    public NuclearDeviceBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> NuclearDeviceItemRenderer.INSTANCE);
    }
}
