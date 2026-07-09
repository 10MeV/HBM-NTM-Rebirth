package com.hbm.ntm.item;

import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LegacyRadiationFoodItem extends Item {
    private static final int FOREVER = Integer.MAX_VALUE;

    private final Kind kind;

    public LegacyRadiationFoodItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            applyLegacyEffects(player);
        }
        return result;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return kind.foil || super.isFoil(stack);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return kind.rarity;
    }

    private void applyLegacyEffects(Player player) {
        switch (kind) {
            case SCHRABIDIUM_NUGGET -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 4));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
            }
            case SCHRABIDIUM_INGOT -> applyGreaterSchrabidium(player, 1200);
            case SCHRABIDIUM_BLOCK -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, FOREVER, 4));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, FOREVER, 1));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FOREVER, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, FOREVER, 9));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, FOREVER, 4));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, FOREVER, 3));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, FOREVER, 4));
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, FOREVER, 24));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, FOREVER, 14));
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, FOREVER, 99));
            }
            case LEAD_NUGGET -> player.addEffect(new MobEffectInstance(ModEffects.LEAD.get(), 15 * 20, 2));
            case LEAD_INGOT -> player.addEffect(new MobEffectInstance(ModEffects.LEAD.get(), 60 * 20, 4));
            case LEAD_BLOCK -> EntityDamageUtil.attackEntityFromNt(player,
                    ModDamageSources.source(player.level(), ModDamageSources.LEAD), 500.0F);
            case EUPHEMIUM -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, FOREVER, 120));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, FOREVER, 0));
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, FOREVER, 120));
            }
            case COTTON_CANDY -> {
                player.addEffect(new MobEffectInstance(MobEffects.POISON, 15 * 20, 0));
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 5 * 20, 0));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 25 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * 20, 4));
            }
            case SCHNITZEL_VEGAN -> {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 10 * 20, 0));
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 30 * 20, 0));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 3 * 60 * 20, 4));
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 3 * 20, 0));
                player.setSecondsOnFire(5 * 20);
                Vec3 movement = player.getDeltaMovement();
                player.setDeltaMovement(movement.x, 2.0D, movement.z);
                player.hasImpulse = true;
            }
        }
    }

    private static void applyGreaterSchrabidium(Player player, int duration) {
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, 4));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 4));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 0));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 4));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, 2));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 2));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, duration, 4));
        player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, duration, 9));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, 4));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, duration, 9));
    }

    public enum Kind {
        SCHRABIDIUM_NUGGET(Rarity.UNCOMMON, false),
        SCHRABIDIUM_INGOT(Rarity.RARE, false),
        SCHRABIDIUM_BLOCK(Rarity.EPIC, true),
        LEAD_NUGGET(Rarity.UNCOMMON, false),
        LEAD_INGOT(Rarity.RARE, false),
        LEAD_BLOCK(Rarity.EPIC, false),
        EUPHEMIUM(Rarity.EPIC, true),
        COTTON_CANDY(Rarity.COMMON, false),
        SCHNITZEL_VEGAN(Rarity.COMMON, false);

        private final Rarity rarity;
        private final boolean foil;

        Kind(Rarity rarity, boolean foil) {
            this.rarity = rarity;
            this.foil = foil;
        }
    }
}
