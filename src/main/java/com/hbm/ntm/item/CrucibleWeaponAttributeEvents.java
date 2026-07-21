package com.hbm.ntm.item;

import com.hbm.ntm.HbmNtm;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Source ItemCrucible removes every main-hand modifier when its three charges are exhausted. */
@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CrucibleWeaponAttributeEvents {
    @SubscribeEvent
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        if (event.getSlotType() == EquipmentSlot.MAINHAND
                && event.getItemStack().getItem() instanceof CrucibleWeaponItem crucible
                && !crucible.canOperate(event.getItemStack())) {
            event.clearModifiers();
        }
    }

    private CrucibleWeaponAttributeEvents() {
    }
}
