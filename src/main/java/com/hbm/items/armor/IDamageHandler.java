package com.hbm.items.armor;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

/**
 * Legacy package bridge for armor pieces that handle post-attack hurt events.
 */
@Deprecated(forRemoval = false)
public interface IDamageHandler {
    void handleDamage(LivingHurtEvent event, ItemStack stack);
}
