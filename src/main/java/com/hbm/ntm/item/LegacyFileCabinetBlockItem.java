package com.hbm.ntm.item;

import com.hbm.ntm.block.LegacyFileCabinetBlock;
import com.hbm.ntm.client.renderer.LegacyFileCabinetItemRenderer;
import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class LegacyFileCabinetBlockItem extends LegacyStateBlockItem {
    public LegacyFileCabinetBlockItem(Block block, Properties properties) {
        super(block, properties, LegacyFileCabinetBlock.VARIANT, 2, LegacyFileCabinetBlockItem::variantName);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyFileCabinetItemRenderer.INSTANCE);
    }

    private static Component variantName(int variant) {
        return Component.translatable("block.hbm_ntm_rebirth.filing_cabinet." + (variant == 1 ? "steel" : "green"));
    }
}
