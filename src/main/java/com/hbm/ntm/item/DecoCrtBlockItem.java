package com.hbm.ntm.item;

import com.hbm.ntm.block.DecoCrtBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/** Keeps BlockDecoCRT's four old metadata variants as explicit item data. */
public final class DecoCrtBlockItem extends LegacyStateBlockItem {
    public DecoCrtBlockItem(Block block, Properties properties) {
        super(block, properties, DecoCrtBlock.VARIANT, 4,
                variant -> Component.translatable("block.hbm_ntm_rebirth.deco_crt"));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptDecoCrt", consumer);
    }
}
