package com.hbm.ntm.item;

import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LegacyBottleOpenerItem extends HbmAbilitySwordItem {
    private static final Tier OPENER_TIER = new Tier() {
        @Override
        public int getUses() {
            return 250;
        }

        @Override
        public float getSpeed() {
            return 1.5F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 0.5F;
        }

        @Override
        public int getLevel() {
            return 1;
        }

        @Override
        public int getEnchantmentValue() {
            return 200;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(ModItems.STEEL_PLATE.get());
        }
    };
    private final RandomSource legacyRandom = RandomSource.create();

    public LegacyBottleOpenerItem(Properties properties) {
        super(OPENER_TIER, 4.5F, 0.0D, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level level = target.level();
        if (!level.isClientSide) {
            switch (legacyRandom.nextInt(7)) {
                case 0 -> target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 60 * 20, 0));
                case 1 -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 60 * 20, 2));
                case 2 -> target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 5 * 60 * 20, 2));
                case 3 -> target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60 * 20, 0));
                default -> {
                }
            }
            LegacySoundPlayer.playSoundAtEntity(target, "VANILLA_ANVIL", 3.0F, 1.0F);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".desc.0"));
        tooltip.add(Component.translatable(getDescriptionId() + ".desc.1"));
    }
}
