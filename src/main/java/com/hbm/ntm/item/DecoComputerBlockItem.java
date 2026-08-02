package com.hbm.ntm.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import java.util.function.Consumer;

/** Keeps BlockDecoModel's IBM_300PL inventory rendering on the legacy OBJ path. */
public final class DecoComputerBlockItem extends BlockItem {
    public DecoComputerBlockItem(Block block, Properties properties) { super(block, properties); }
    @Override public void initializeClient(Consumer<IClientItemExtensions> consumer) { ClientItemRendererBridge.accept("acceptDecoComputer", consumer); }
}
