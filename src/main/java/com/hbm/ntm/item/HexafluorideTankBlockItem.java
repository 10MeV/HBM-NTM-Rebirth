package com.hbm.ntm.item;

import com.hbm.ntm.client.renderer.HexafluorideTankItemRenderer;
import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import java.util.function.Consumer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class HexafluorideTankBlockItem extends BlockItem {
    public HexafluorideTankBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> HexafluorideTankItemRenderer.INSTANCE);
    }
}
