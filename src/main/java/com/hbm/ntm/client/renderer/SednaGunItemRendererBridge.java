package com.hbm.ntm.client.renderer;

import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class SednaGunItemRendererBridge {
    public static void accept(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> SednaGunItemRenderer.INSTANCE);
    }

    private SednaGunItemRendererBridge() {
    }
}
