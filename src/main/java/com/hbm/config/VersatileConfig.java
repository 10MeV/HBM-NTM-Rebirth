package com.hbm.config;

import com.hbm.ntm.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.RegistryObject;

/**
 * Legacy package facade for small cross-config helpers.
 *
 * <p>528/LBSM recipe-changing branches are intentionally not restored in the
 * modern port; methods that depended only on those global modes return their
 * standard 1.7.10 value.</p>
 */
@Deprecated(forRemoval = false)
public final class VersatileConfig {
    private static final int MINUTE = 60 * 20;
    private static final int HOUR = 60 * MINUTE;
    private static final int STANDARD_SCHRAB_ORE_CHANCE = 100;

    public static Item getTransmutatorItem() {
        return legacyItem("ingot_schraranium");
    }

    public static int getSchrabOreChance() {
        return STANDARD_SCHRAB_ORE_CHANCE;
    }

    public static void applyPotionSickness(LivingEntity entity, int duration) {
        com.hbm.ntm.config.PotionConfig.applyPotionSickness(entity, duration);
    }

    public static boolean hasPotionSickness(LivingEntity entity) {
        return com.hbm.ntm.config.PotionConfig.hasPotionSickness(entity);
    }

    public static boolean rtgDecay() {
        return com.hbm.ntm.config.RtgConfig.doRtgsDecay();
    }

    public static boolean scaleRTGPower() {
        return com.hbm.ntm.config.RtgConfig.scaleRtgPower();
    }

    public static int getLongDecayChance() {
        return 3 * HOUR;
    }

    public static int getShortDecayChance() {
        return 15 * MINUTE;
    }

    private static Item legacyItem(String name) {
        RegistryObject<Item> item = ModItems.legacyItem(name);
        return item == null ? Items.AIR : item.get();
    }

    private VersatileConfig() {
    }
}
