package com.hbm.ntm.item;

import com.hbm.ntm.explosion.vnt.ExplosionVnt;
import com.hbm.ntm.explosion.vnt.standard.EntityProcessorCrossSmooth;
import com.hbm.ntm.explosion.vnt.standard.PlayerProcessorStandard;
import com.hbm.ntm.particle.LegacyParticleCreators;
import com.hbm.ntm.registry.ModSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Source-backed 1.7.10 Market Gardener special sword. */
public class LegacyMemeSpoonItem extends HbmAbilitySwordItem {
    private static final float LEGACY_ATTACK_DAMAGE = 6.0F;
    private static final float CRITICAL_DAMAGE = 50.0F;
    private static final float EXPLOSION_DAMAGE = 150.0F;

    public LegacyMemeSpoonItem(Properties properties) {
        super(HbmToolTiers.STEEL, LEGACY_ATTACK_DAMAGE, 0.0D, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level();
        if (!level.isClientSide && attacker instanceof Player player) {
            if (player.fallDistance >= 2.0F) {
                level.playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.WEAPON_BANG.get(),
                        SoundSource.PLAYERS, 3.0F, 0.75F);
                target.hurt(player.damageSources().playerAttack(player), CRITICAL_DAMAGE);
            }

            if (player.fallDistance >= 20.0F && !player.getAbilities().instabuild) {
                double x = target.getX();
                double y = target.getY() + target.getBbHeight() / 2.0D;
                double z = target.getZ();
                ExplosionVnt explosion = new ExplosionVnt(level, x, y, z, 15.0F, player)
                        .setEntityProcessor(new EntityProcessorCrossSmooth(1.0D, EXPLOSION_DAMAGE)
                                .setupPiercing(25.0F, 0.5F))
                        .setPlayerProcessor(new PlayerProcessorStandard());
                LegacyParticleCreators.composeEffect(level, x, y, z, 10, 2.0F, 0.5F, 25.0F,
                        5, 8, 20, 0.75F, 1.0F, -2.0F, 150.0F);
                explosion.explode();
            }
        }

        // WeaponSpecial#hitEntity returned false rather than delegating to ItemSword.
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("Level 10 Shovel").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Deals crits while the wielder is rocket jumping").withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("20% slower firing speed").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("No random critical hits").withStyle(ChatFormatting.RED));
    }
}
