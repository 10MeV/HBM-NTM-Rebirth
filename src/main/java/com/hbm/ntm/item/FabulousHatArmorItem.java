package com.hbm.ntm.item;

import com.hbm.items.armor.IAttackHandler;
import com.hbm.items.armor.IDamageHandler;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class FabulousHatArmorItem extends ObjArmorItem implements IAttackHandler, IDamageHandler {
    public FabulousHatArmorItem(ArmorMaterial material, Properties properties) {
        super(material, Type.HELMET, properties,
                List.of(new TooltipLine("tooltip.hbm_ntm_rebirth.armor.dt_2", ChatFormatting.BLUE)));
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!entity.level().isClientSide) {
            entity.discard();
        }
        return true;
    }

    @Override
    public void handleAttack(LivingAttackEvent event, ItemStack armor) {
        if (!event.getSource().is(DamageTypeTags.BYPASSES_ARMOR) && event.getAmount() <= 2.0F) {
            event.getEntity().level().playSound(null, event.getEntity().getX(), event.getEntity().getY(),
                    event.getEntity().getZ(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 5.0F,
                    1.0F + event.getEntity().getRandom().nextFloat() * 0.5F);
            event.setCanceled(true);
        }
    }

    @Override
    public void handleDamage(LivingHurtEvent event, ItemStack armor) {
        if (!event.getSource().is(DamageTypeTags.BYPASSES_ARMOR)) {
            event.setAmount(Math.max(0.0F, event.getAmount() - 2.0F));
        }
    }
}
