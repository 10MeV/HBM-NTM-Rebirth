package com.hbm.hazard.type;

import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.modifier.HazardModifier;
import com.hbm.ntm.radiation.HazardType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Legacy package facade for 1.7.10 hazard type imports.
 */
@Deprecated(forRemoval = false)
public class HazardTypeBase {
    private final HazardType modernType;

    protected HazardTypeBase(HazardType modernType) {
        this.modernType = modernType;
    }

    public HazardType modernType() {
        return modernType;
    }

    public void onUpdate(LivingEntity target, float level, ItemStack stack) {
        com.hbm.ntm.radiation.HazardExposureUtil.applyHazard(target, stack, modernType, level);
    }

    public void updateEntity(ItemEntity item, float level) {
        com.hbm.ntm.radiation.HazardExposureUtil.applyDroppedHazard(item, modernType, level);
    }

    public void addHazardInformation(Player player, List<Component> list, float level, ItemStack stack,
                                     List<HazardModifier> modifiers) {
        com.hbm.ntm.radiation.HazardTooltipUtil.addHazardInformation(stack, list);
    }

    public static HazardTypeBase fromModern(HazardType type) {
        return switch (type) {
            case RADIATION -> HazardRegistry.RADIATION;
            case DIGAMMA -> HazardRegistry.DIGAMMA;
            case HOT -> HazardRegistry.HOT;
            case BLINDING -> HazardRegistry.BLINDING;
            case ASBESTOS -> HazardRegistry.ASBESTOS;
            case COAL -> HazardRegistry.COAL;
            case HYDROACTIVE -> HazardRegistry.HYDROACTIVE;
            case EXPLOSIVE -> HazardRegistry.EXPLOSIVE;
        };
    }
}
