package com.hbm.ntm.api.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface GasMask {
    List<HazardClass> getBlacklist(ItemStack stack, LivingEntity entity);

    ItemStack getFilter(ItemStack stack, LivingEntity entity);

    boolean isFilterApplicable(ItemStack stack, LivingEntity entity, ItemStack filter);

    void installFilter(ItemStack stack, LivingEntity entity, ItemStack filter);

    void damageFilter(ItemStack stack, LivingEntity entity, int damage);
}
