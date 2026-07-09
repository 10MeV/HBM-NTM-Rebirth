package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModSounds;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class SchrabidiumHammerItem extends HbmAbilitySwordItem {
    private static final float LEGACY_ATTACK_DAMAGE = 1_000_000_000.0F;
    private static final double LEGACY_MOVEMENT_MODIFIER = -0.5D;

    public SchrabidiumHammerItem(Properties properties) {
        super(HbmToolTiers.SCHRABIDIUM, LEGACY_ATTACK_DAMAGE, LEGACY_MOVEMENT_MODIFIER, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity attacker) {
        Level level = victim.level();
        if (!level.isClientSide) {
            victim.setHealth(0.0F);
            level.playSound(null, victim.getX(), victim.getY(), victim.getZ(), ModSounds.WEAPON_BONK.get(),
                    SoundSource.PLAYERS, 3.0F, 1.0F);
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Even though it says \"+1000000000"));
        tooltip.add(Component.literal("damage\", it's actually \"onehit anything\""));
    }
}
