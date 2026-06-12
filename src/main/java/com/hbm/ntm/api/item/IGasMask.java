package com.hbm.ntm.api.item;

import com.hbm.ntm.radiation.ArmorUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Legacy-name bridge for gas mask items and installed filters.
 */
@Deprecated(forRemoval = false)
public interface IGasMask extends GasMask {
    @Override
    default List<HazardClass> getBlacklist(ItemStack stack, LivingEntity entity) {
        return List.of();
    }

    @Override
    default ItemStack getFilter(ItemStack stack, LivingEntity entity) {
        return ArmorUtil.getGasMaskFilter(stack);
    }

    @Override
    default boolean isFilterApplicable(ItemStack stack, LivingEntity entity, ItemStack filter) {
        return true;
    }

    @Override
    default void installFilter(ItemStack stack, LivingEntity entity, ItemStack filter) {
        ArmorUtil.installGasMaskFilter(stack, filter);
    }

    @Override
    default void damageFilter(ItemStack stack, LivingEntity entity, int damage) {
        ArmorUtil.damageGasMaskFilter(stack, damage);
    }
}
