package com.hbm.ntm.item;

import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class FluidPipeStyleBlockItem extends LegacyStateBlockItem {
    public FluidPipeStyleBlockItem(Block block, Properties properties, IntegerProperty property, int variants,
            IntFunction<Component> nameFactory) {
        super(block, properties, property, variants, nameFactory);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptFluidDuct", consumer);
    }
}
