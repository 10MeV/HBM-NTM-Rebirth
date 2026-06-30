package com.hbm.ntm.client.renderer;

import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public final class MissileItemRendererBridge {
    public static void acceptMissile(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> MissileItemRenderer.INSTANCE);
    }

    public static void acceptMissilePart(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> MissilePartItemRenderer.INSTANCE);
    }

    public static void acceptCustomMissile(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> CustomMissileItemRenderer.INSTANCE);
    }

    private MissileItemRendererBridge() {
    }
}
