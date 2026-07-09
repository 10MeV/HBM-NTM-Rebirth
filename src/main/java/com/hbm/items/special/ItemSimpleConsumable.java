package com.hbm.items.special;

import com.hbm.config.VersatileConfig;
import com.hbm.items.ItemCustomLore;
import com.hbm.lib.ModDamageSource;
import com.hbm.ntm.registry.ModEffects;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.EnchantmentUtil;
import com.hbm.ntm.util.InventoryUtil;
import com.hbm.potion.HbmPotion;
import com.hbm.util.Tuple.Pair;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.BiConsumer;

/**
 * Legacy 1.7.10 package bridge for delegate-based simple consumables.
 *
 * <p>The old {@code init()} registration side effect is owned by modern
 * DeferredRegister, so this class carries only the source-shaped behavior
 * surface and static helper methods.</p>
 */
@Deprecated(forRemoval = false)
public class ItemSimpleConsumable extends ItemCustomLore {
    private static final int XP_BAG_AMOUNT = 100;

    private BiConsumer<ItemStack, Player> useAction;
    private BiConsumer<ItemStack, Player> useActionServer;
    private BiConsumer<ItemStack, Pair<LivingEntity, LivingEntity>> hitAction;
    private BiConsumer<ItemStack, Pair<LivingEntity, LivingEntity>> hitActionServer;

    public ItemSimpleConsumable() {
        this(new Item.Properties());
    }

    public ItemSimpleConsumable(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (useAction != null) {
            useAction.accept(stack, player);
        }
        if (!level.isClientSide && useActionServer != null) {
            useActionServer.accept(stack, player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Pair<LivingEntity, LivingEntity> pair = new Pair<>(target, attacker);
        if (hitAction != null) {
            hitAction.accept(stack, pair);
        }
        if (!target.level().isClientSide && hitActionServer != null) {
            hitActionServer.accept(stack, pair);
        }
        return false;
    }

    public ItemSimpleConsumable setUseAction(BiConsumer<ItemStack, Player> delegate) {
        this.useAction = delegate;
        return this;
    }

    public ItemSimpleConsumable setUseActionServer(BiConsumer<ItemStack, Player> delegate) {
        this.useActionServer = delegate;
        return this;
    }

    public ItemSimpleConsumable setHitAction(BiConsumer<ItemStack, Pair<LivingEntity, LivingEntity>> delegate) {
        this.hitAction = delegate;
        return this;
    }

    public ItemSimpleConsumable setHitActionServer(BiConsumer<ItemStack, Pair<LivingEntity, LivingEntity>> delegate) {
        this.hitActionServer = delegate;
        return this;
    }

    public static void init() {
        // Modern registration happens in ModItems. Keep the old call shape as a no-op.
    }

    public static ItemSimpleConsumable syringeAntidote(Item.Properties properties) {
        return new ItemSimpleConsumable(properties)
                .setUseActionServer((stack, user) -> effectAntidote(stack, user, user))
                .setHitActionServer((stack, pair) -> effectAntidote(stack, pair.key, pair.value));
    }

    public static ItemSimpleConsumable syringePoison(Item.Properties properties) {
        return new ItemSimpleConsumable(properties)
                .setUseActionServer((stack, user) -> effectPoison(stack, user, user))
                .setHitActionServer((stack, pair) -> effectPoison(stack, pair.key, pair.value));
    }

    public static ItemSimpleConsumable syringeAwesome(Item.Properties properties) {
        ItemSimpleConsumable item = new ItemSimpleConsumable(properties)
                .setUseActionServer((stack, user) -> effectAwesome(stack, user, user))
                .setHitActionServer((stack, pair) -> effectAwesome(stack, pair.key, pair.value));
        item.setEffect();
        return item;
    }

    public static ItemSimpleConsumable ivEmpty(Item.Properties properties) {
        return new ItemSimpleConsumable(properties).setUseActionServer((stack, user) -> {
            giveSoundAndDecrement(stack, user, "hbm:item.syringe", new ItemStack(ModItems.IV_BLOOD.get()));
            float health = Math.max(user.getHealth() - 5.0F, 0.0F);
            user.setHealth(health);
            if (health <= 0.0F) {
                user.die(user.damageSources().magic());
            }
        });
    }

    public static ItemSimpleConsumable ivBlood(Item.Properties properties) {
        return new ItemSimpleConsumable(properties).setUseActionServer((stack, user) -> {
            giveSoundAndDecrement(stack, user, "hbm:item.radaway", new ItemStack(ModItems.IV_EMPTY.get()));
            user.heal(5.0F);
        });
    }

    public static ItemSimpleConsumable ivXpEmpty(Item.Properties properties) {
        return new ItemSimpleConsumable(properties).setUseActionServer((stack, user) -> {
            int totalXp = EnchantmentUtil.getTotalExperience(user);
            if (totalXp >= XP_BAG_AMOUNT) {
                giveSoundAndDecrement(stack, user, "hbm:item.syringe", new ItemStack(ModItems.IV_XP.get()));
                EnchantmentUtil.setExperience(user, totalXp - XP_BAG_AMOUNT);
            }
        });
    }

    public static ItemSimpleConsumable ivXp(Item.Properties properties) {
        return new ItemSimpleConsumable(properties).setUseActionServer((stack, user) -> {
            giveSoundAndDecrement(stack, user, "random.orb", new ItemStack(ModItems.IV_XP_EMPTY.get()));
            EnchantmentUtil.addExperience(user, XP_BAG_AMOUNT, false);
        });
    }

    public static ItemSimpleConsumable radaway(Item.Properties properties, int duration) {
        return new ItemSimpleConsumable(properties).setUseActionServer((stack, user) -> doRadaway(stack, user, duration));
    }

    public static void giveSoundAndDecrement(ItemStack stack, LivingEntity entity, String sound, ItemStack container) {
        stack.shrink(1);
        LegacySoundPlayer.playSoundAtEntity(entity, sound, 1.0F, 1.0F);
        tryAddItem(entity, container);
    }

    public static void addPotionEffect(LivingEntity entity, MobEffect effect, int duration, int level) {
        MobEffectInstance active = entity.getEffect(effect);
        if (active == null) {
            entity.addEffect(new MobEffectInstance(effect, duration, level));
            return;
        }
        entity.addEffect(new MobEffectInstance(effect, active.getDuration() + duration, level));
    }

    public static void tryAddItem(LivingEntity entity, ItemStack stack) {
        if (entity instanceof Player player) {
            InventoryUtil.giveOrDrop(player, stack);
        }
    }

    public static void doRadaway(ItemStack stack, Player user, int duration) {
        giveSoundAndDecrement(stack, user, "hbm:item.radaway", new ItemStack(ModItems.IV_EMPTY.get()));
        addPotionEffect(user, HbmPotion.get(HbmPotion.radaway), duration, 0);
    }

    public static void effectAntidote(ItemStack stack, LivingEntity affected, LivingEntity source) {
        if (VersatileConfig.hasPotionSickness(affected)) {
            return;
        }
        affected.removeAllEffects();
        giveSoundAndDecrement(stack, source, "hbm:item.syringe", new ItemStack(ModItems.SYRINGE_EMPTY.get()));
        VersatileConfig.applyPotionSickness(affected, 5);
    }

    public static void effectPoison(ItemStack stack, LivingEntity affected, LivingEntity source) {
        if (affected == source) {
            affected.hurt(ModDamageSource.source(affected.level(), affected.getRandom().nextBoolean()
                    ? ModDamageSource.euthanizedSelf
                    : ModDamageSource.euthanizedSelf2), 30.0F);
        } else {
            affected.hurt(ModDamageSource.euthanized(source, source), 30.0F);
        }
        giveSoundAndDecrement(stack, source, "hbm:item.syringe", new ItemStack(ModItems.SYRINGE_EMPTY.get()));
    }

    public static void effectAwesome(ItemStack stack, LivingEntity affected, LivingEntity source) {
        if (VersatileConfig.hasPotionSickness(affected)) {
            return;
        }
        giveSoundAndDecrement(stack, source, "hbm:item.syringe", new ItemStack(ModItems.SYRINGE_EMPTY.get()));
        affected.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 50 * 20, 9));
        affected.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 50 * 20, 9));
        affected.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 50 * 20, 0));
        affected.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 50 * 20, 24));
        affected.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 50 * 20, 9));
        affected.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 50 * 20, 6));
        affected.addEffect(new MobEffectInstance(MobEffects.JUMP, 50 * 20, 9));
        affected.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 50 * 20, 9));
        affected.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 50 * 20, 4));
        affected.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 5 * 20, 4));
        affected.addEffect(new MobEffectInstance(ModEffects.RADX.get(), 50 * 20, 9));
        VersatileConfig.applyPotionSickness(affected, 5);
    }
}
