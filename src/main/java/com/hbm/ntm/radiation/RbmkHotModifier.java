package com.hbm.ntm.radiation;

import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.ntm.neutron.RBMKFuelRodState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RbmkHotModifier implements HazardModifier {
    @Override
    public float modify(ItemStack stack, LivingEntity holder, float level) {
        if (!(stack.getItem() instanceof ItemRBMKRod)) {
            return 0.0F;
        }
        CompoundTag tag = stack.getTag();
        double heat = tag == null ? RBMKFuelRodState.DEFAULT_HEAT : tag.getDouble(RBMKFuelRodState.TAG_HULL_HEAT);
        return (float) Math.min(Math.ceil((heat - 100.0D) / 10.0D), 60.0D);
    }
}
