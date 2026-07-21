package com.hbm.ntm.client.renderer;

import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/** Client-only bridge for the old power-armor controller item renderer. */
public final class PowerArmorWeaponItemRendererBridge {
    public static void accept(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> PowerArmorWeaponItemRenderer.INSTANCE);
    }

    private PowerArmorWeaponItemRendererBridge() {
    }
}
