package com.hbm.ntm.client.renderer;

import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class LegacyToolItemRendererBridge {
    public static void acceptBoltgun(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyToolItemRenderer.INSTANCE);
    }

    public static void acceptChainsaw(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyToolItemRenderer.INSTANCE);
    }

    public static void acceptCrucible(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> LegacyToolItemRenderer.INSTANCE);
    }

    private LegacyToolItemRendererBridge() {
    }
}
