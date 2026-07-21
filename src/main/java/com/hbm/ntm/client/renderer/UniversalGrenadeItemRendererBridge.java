package com.hbm.ntm.client.renderer;

import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class UniversalGrenadeItemRendererBridge {
    public static void accept(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> UniversalGrenadeItemRenderer.INSTANCE);
    }

    private UniversalGrenadeItemRendererBridge() {
    }
}
