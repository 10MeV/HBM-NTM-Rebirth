package com.hbm.ntm.item;

import java.util.function.Consumer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * The three legacy warning-sign battle axes use the alloy sword contract and
 * differ only by their direct-render texture.
 */
public final class LegacySignWeaponItem extends HbmAbilitySwordItem {
    private static final float LEGACY_ATTACK_DAMAGE_MODIFIER = 8.0F;

    private final Variant variant;

    public LegacySignWeaponItem(Variant variant, Properties properties) {
        super(HbmToolTiers.ALLOY, LEGACY_ATTACK_DAMAGE_MODIFIER, 0.0D, properties);
        this.variant = variant;
    }

    public Variant variant() {
        return variant;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        ClientItemRendererBridge.accept("acceptSignWeapon", consumer);
    }

    public enum Variant {
        STOP,
        SOP,
        CHERNOBYL
    }
}
