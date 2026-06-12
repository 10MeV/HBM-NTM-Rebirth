package com.hbm.ntm.item;

import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class JsonModelMultiblockBlockItem extends MultiblockBlockItem {
    public JsonModelMultiblockBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
    }
}
