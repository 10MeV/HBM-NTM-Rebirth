package com.hbm.ntm.item;

import com.hbm.ntm.config.PotionConfig;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.radiation.ModDamageSources;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.InventoryUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class LegacySyringeItem extends Item {
    private final Kind kind;

    public LegacySyringeItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (kind == Kind.MKUNICORN) {
            return InteractionResultHolder.pass(stack);
        }
        if (!applyTo(level, stack, player, player)) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (kind == Kind.MED_BAG) {
            return false;
        }
        applyTo(target.level(), stack, target, attacker);
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        int lines = kind.tooltipLines;
        for (int i = 0; i < lines; i++) {
            tooltip.add(Component.translatable(getDescriptionId() + ".desc." + i));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return kind.foil || super.isFoil(stack);
    }

    private boolean applyTo(Level level, ItemStack stack, LivingEntity target, LivingEntity source) {
        if (level.isClientSide) {
            return true;
        }
        if (kind.blocksPotionSickness && PotionConfig.hasPotionSickness(target)) {
            return false;
        }

        switch (kind) {
            case STIMPAK -> {
                target.heal(5.0F);
                PotionConfig.applyPotionSickness(target, 5);
            }
            case MEDX -> {
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 4 * 60 * 20, 2));
                PotionConfig.applyPotionSickness(target, 5);
            }
            case PSYCHO -> {
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2 * 60 * 20, 0));
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2 * 60 * 20, 0));
                PotionConfig.applyPotionSickness(target, 5);
            }
            case SUPER -> {
                target.heal(25.0F);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10 * 20, 0));
                PotionConfig.applyPotionSickness(target, 15);
            }
            case MED_BAG -> {
                target.setHealth(target.getMaxHealth());
                target.removeEffect(MobEffects.BLINDNESS);
                target.removeEffect(MobEffects.CONFUSION);
                target.removeEffect(MobEffects.DIG_SLOWDOWN);
                target.removeEffect(MobEffects.HUNGER);
                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                target.removeEffect(MobEffects.POISON);
                target.removeEffect(MobEffects.WEAKNESS);
                target.removeEffect(MobEffects.WITHER);
                target.removeEffect(ModEffects.RADIATION.get());
                PotionConfig.applyPotionSickness(target, 15);
            }
            case TAINT -> {
                target.addEffect(new MobEffectInstance(ModEffects.TAINT.get(), 60 * 20, 0));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 5 * 20, 0));
            }
            case MKUNICORN -> HbmLivingProperties.applyMkuContagion(target);
            case ANTIDOTE -> {
                target.removeAllEffects();
                PotionConfig.applyPotionSickness(target, 5);
            }
            case POISON -> {
                if (target == source) {
                    target.hurt(ModDamageSources.source(level, target.getRandom().nextBoolean()
                            ? ModDamageSources.EUTHANIZED_SELF
                            : ModDamageSources.EUTHANIZED_SELF2), 30.0F);
                } else {
                    target.hurt(ModDamageSources.euthanized(level, source, source), 30.0F);
                }
            }
            case AWESOME -> {
                target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50 * 20, 9));
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 50 * 20, 9));
                target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 50 * 20, 0));
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50 * 20, 24));
                target.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 50 * 20, 9));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50 * 20, 6));
                target.addEffect(new MobEffectInstance(MobEffects.JUMP, 50 * 20, 9));
                target.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 50 * 20, 9));
                target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 50 * 20, 4));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 5 * 20, 4));
                target.addEffect(new MobEffectInstance(ModEffects.RADX.get(), 50 * 20, 9));
                PotionConfig.applyPotionSickness(target, 5);
            }
        }

        stack.shrink(1);
        if (kind != Kind.MED_BAG) {
            playSyringeSound(level, target);
        }
        if (kind.returnsContainer) {
            giveContainer(source, new ItemStack(kind.ordinaryContainer
                    ? ModItems.SYRINGE_EMPTY.get()
                    : ModItems.SYRINGE_METAL_EMPTY.get()));
        }
        if (kind == Kind.TAINT) {
            giveContainer(source, new ItemStack(ModItems.BOTTLE2_EMPTY.get()));
        }
        return true;
    }

    private static void playSyringeSound(Level level, LivingEntity target) {
        LegacySoundPlayer.playLegacySyringe(target);
    }

    private static void giveContainer(LivingEntity source, ItemStack stack) {
        if (source instanceof Player player) {
            InventoryUtil.giveOrDrop(player, stack);
        }
    }

    public enum Kind {
        STIMPAK(true, 1),
        MEDX(true, 1),
        PSYCHO(true, 2),
        SUPER(true, 2),
        MED_BAG(true, 2, false, false, false),
        TAINT(false, 3),
        MKUNICORN(false, 1, false, false, false),
        ANTIDOTE(true, 1, true, false),
        POISON(false, 1, true, false),
        AWESOME(true, 1, true, true);

        private final boolean blocksPotionSickness;
        private final int tooltipLines;
        private final boolean ordinaryContainer;
        private final boolean foil;
        private final boolean returnsContainer;

        Kind(boolean blocksPotionSickness, int tooltipLines) {
            this(blocksPotionSickness, tooltipLines, false, false, true);
        }

        Kind(boolean blocksPotionSickness, int tooltipLines, boolean ordinaryContainer, boolean foil) {
            this(blocksPotionSickness, tooltipLines, ordinaryContainer, foil, true);
        }

        Kind(boolean blocksPotionSickness, int tooltipLines, boolean ordinaryContainer, boolean foil,
                boolean returnsContainer) {
            this.blocksPotionSickness = blocksPotionSickness;
            this.tooltipLines = tooltipLines;
            this.ordinaryContainer = ordinaryContainer;
            this.foil = foil;
            this.returnsContainer = returnsContainer;
        }
    }
}
