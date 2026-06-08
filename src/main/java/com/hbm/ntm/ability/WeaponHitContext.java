package com.hbm.ntm.ability;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record WeaponHitContext(Level level, Player player, Entity victim, ItemStack weaponStack) {
}
