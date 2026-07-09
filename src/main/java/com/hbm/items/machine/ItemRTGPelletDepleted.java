package com.hbm.items.machine;

import com.hbm.ntm.recipe.LegacyMetaItemMappings;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Legacy 1.7.10 package bridge for the depleted RTG pellet metadata family.
 *
 * <p>The modern runtime splits the old metadata item into per-material items.
 * This bridge preserves the legacy enum order and {@code stackFromEnum(...)}
 * call shape without registering a second metadata item family.
 */
@Deprecated(forRemoval = false)
public class ItemRTGPelletDepleted extends Item {
    public ItemRTGPelletDepleted() {
        super(new Item.Properties());
    }

    public ItemStack stackFromEnum(int count, Enum<?> material) {
        if (!(material instanceof DepletedRTGMaterial depletedMaterial)) {
            return null;
        }
        return stack(depletedMaterial, count);
    }

    public ItemStack stackFromEnum(Enum<?> material) {
        return stackFromEnum(1, material);
    }

    public ItemStack stackFromEnum(int count, DepletedRTGMaterial material) {
        return stack(material, count);
    }

    public ItemStack stackFromEnum(DepletedRTGMaterial material) {
        return stack(material, 1);
    }

    public static DepletedRTGMaterial byMeta(int meta) {
        DepletedRTGMaterial[] values = DepletedRTGMaterial.values();
        return values[Math.abs(meta % values.length)];
    }

    public static ItemStack stack(DepletedRTGMaterial material, int count) {
        if (material == null) {
            return ItemStack.EMPTY;
        }
        return LegacyMetaItemMappings.stackPreservingCount(
                LegacyMetaItemMappings.PELLET_RTG_DEPLETED, material.ordinal(), count)
                .orElse(ItemStack.EMPTY);
    }

    public enum DepletedRTGMaterial {
        BISMUTH,
        MERCURY,
        NEPTUNIUM,
        LEAD,
        ZIRCONIUM,
        NICKEL
    }
}
