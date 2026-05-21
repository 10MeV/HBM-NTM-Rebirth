package com.hbm.ntm.client.renderer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class LegacyItemRendererBridge {
    public static void accept(Consumer<IClientItemExtensions> consumer, BlockEntityWithoutLevelRenderer renderer) {
        accept(consumer, () -> renderer);
    }

    public static void accept(Consumer<IClientItemExtensions> consumer, Supplier<? extends BlockEntityWithoutLevelRenderer> rendererSupplier) {
        consumer.accept(extensions(rendererSupplier));
    }

    public static IClientItemExtensions extensions(BlockEntityWithoutLevelRenderer renderer) {
        return extensions(() -> renderer);
    }

    public static IClientItemExtensions extensions(Supplier<? extends BlockEntityWithoutLevelRenderer> rendererSupplier) {
        return new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return rendererSupplier.get();
            }
        };
    }

    private LegacyItemRendererBridge() {
    }
}
