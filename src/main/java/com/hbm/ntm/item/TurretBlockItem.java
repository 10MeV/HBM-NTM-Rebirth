package com.hbm.ntm.item;

import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import com.hbm.ntm.client.renderer.TurretItemRenderer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class TurretBlockItem extends MultiblockBlockItem {
    public TurretBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> TurretItemRenderer.INSTANCE);
    }
}
