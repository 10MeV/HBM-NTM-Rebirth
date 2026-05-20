package com.hbm.ntm.radiation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RbmkHotModifier implements HazardModifier {
    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0.0F;
        }
        double heat = tag.contains("hull") ? tag.getDouble("hull") : tag.getDouble("hullHeat");
        return (float) Math.max(0.0D, Math.min(Math.ceil((heat - 100.0D) / 10.0D), 60.0D));
    }
}
