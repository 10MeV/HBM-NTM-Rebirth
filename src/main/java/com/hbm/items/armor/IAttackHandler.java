package com.hbm.items.armor;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

/**
 * Legacy package bridge for armor pieces that handle incoming attack events.
 */
@Deprecated(forRemoval = false)
public interface IAttackHandler {
    void handleAttack(LivingAttackEvent event, ItemStack armor);
}
