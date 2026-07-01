package com.hbm.ntm.item;

import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class RedCableBoxBlockItem extends LegacyStateBlockItem {
    public RedCableBoxBlockItem(Block block, Properties properties, IntegerProperty property, int variants,
            IntFunction<Component> nameFactory) {
        super(block, properties, property, variants, nameFactory);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptRedCableBox", consumer);
    }

    public static ItemStack createStack(RedCableBoxBlockItem item, int variant) {
        return LegacyStateBlockItem.createStack(item, variant);
    }
}
