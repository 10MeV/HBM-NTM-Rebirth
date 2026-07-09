package com.hbm.ntm.item;

import com.hbm.ntm.config.PotionConfig;
import com.hbm.ntm.explosion.ExplosionLarge;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.radiation.HazardType;
import com.hbm.ntm.radiation.RadiationUtil;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.util.AchievementHandler;
import com.hbm.ntm.util.InventoryUtil;
import java.util.List;
import java.util.function.Supplier;
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
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

public class LegacyEnergyDrinkItem extends Item {
    private static final int LEGACY_USE_DURATION = 32;
    private final Kind kind;
    @Nullable
    private final Supplier<? extends Item> container;
    @Nullable
    private final Supplier<? extends Item> cap;
    private final boolean requiresOpener;

    public LegacyEnergyDrinkItem(Properties properties, Kind kind) {
        this(properties, kind, null, null, false);
    }

    public LegacyEnergyDrinkItem(Properties properties, Kind kind, Supplier<? extends Item> container,
            Supplier<? extends Item> cap) {
        this(properties, kind, container, cap, true);
    }

    public LegacyEnergyDrinkItem(Properties properties, Kind kind, Supplier<? extends Item> container,
            Supplier<? extends Item> cap, boolean requiresOpener) {
        super(properties);
        this.kind = kind;
        this.container = container;
        this.cap = cap;
        this.requiresOpener = requiresOpener;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return LEGACY_USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (PotionConfig.hasPotionSickness(player) || (requiresOpener && !hasBottleOpener(player))) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return stack;
        }

        boolean creative = player.getAbilities().instabuild;
        if (!creative) {
            stack.shrink(1);
        }

        if (player instanceof FakePlayer) {
            level.explode(player, player.getX(), player.getY(), player.getZ(), 5.0F, true,
                    Level.ExplosionInteraction.BLOCK);
            return stack;
        }

        PotionConfig.applyPotionSickness(player, 5);
        applyDrinkEffects(player);

        if (!creative) {
            if (cap != null) {
                InventoryUtil.giveOrDrop(player, new ItemStack(cap.get()));
            }
            if (container != null) {
                if (stack.isEmpty()) {
                    return new ItemStack(container.get());
                }
                InventoryUtil.giveOrDrop(player, new ItemStack(container.get()));
            }
        }

        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        for (int i = 0; i < tooltipLineCount(); i++) {
            tooltip.add(Component.translatable(getDescriptionId() + ".desc." + i));
        }
        if (requiresOpener) {
            tooltip.add(Component.translatable("item.hbm_ntm_rebirth.bottle.requires_opener"));
        }
    }

    private void applyDrinkEffects(Player player) {
        switch (kind) {
            case SMART -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * 20, 0));
            }
            case CREATURE -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30 * 20, 1));
            }
            case REDBOMB -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 0));
                player.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 30 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 30 * 20, 1));
            }
            case MRSUGAR -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 30 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 30 * 20, 2));
            }
            case OVERCHARGE -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * 20, 0));
            }
            case LUNA -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30 * 20, 2));
            }
            case BEPIS -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 3));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * 20, 3));
            }
            case BREEN -> player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 30 * 20, 0));
            case MUG -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3 * 60 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60 * 20, 2));
            }
            case COFFEE -> applyCoffeeEffects(player);
            case COFFEE_RADIUM -> {
                applyCoffeeEffects(player);
                HbmLivingProperties.incrementRadiation(player, 500.0F);
                AchievementHandler.award(player, AchievementHandler.RADIUM);
            }
            case CHOCOLATE_MILK -> ExplosionLarge.explode(player.level(), player.getX(), player.getY(), player.getZ(),
                    50.0F, true, false, false);
            case NUKA -> {
                player.heal(4.0F);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 30 * 20, 1));
                contaminate(player, 5.0F);
            }
            case CHERRY -> {
                player.heal(6.0F);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 0));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 30 * 20, 2));
                contaminate(player, 5.0F);
            }
            case QUANTUM -> {
                player.heal(10.0F);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * 20, 1));
                contaminate(player, 15.0F);
            }
            case KORL -> {
                player.heal(6.0F);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 30 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 30 * 20, 2));
            }
            case FRITZ -> {
                player.heal(6.0F);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 30 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 30 * 20, 2));
            }
            case SPARKLE -> {
                player.heal(10.0F);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 120 * 20, 1));
                contaminate(player, 5.0F);
            }
            case RAD -> {
                player.heal(10.0F);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120 * 20, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 120 * 20, 2));
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 120 * 20, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120 * 20, 4));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 120 * 20, 1));
                contaminate(player, 15.0F);
            }
        }
    }

    private static void contaminate(Player player, float amount) {
        RadiationUtil.contaminate(player, HazardType.RADIATION, RadiationUtil.ContaminationType.RAD_BYPASS, amount);
    }

    private static void applyCoffeeEffects(Player player) {
        player.heal(10.0F);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60 * 20, 2));
    }

    private int tooltipLineCount() {
        return switch (kind) {
            case BREEN -> 2;
            case CHOCOLATE_MILK -> 2;
            case MUG, COFFEE, COFFEE_RADIUM -> 0;
            default -> 1;
        };
    }

    private static boolean hasBottleOpener(Player player) {
        return player.getInventory().items.stream().anyMatch(stack -> stack.is(ModItems.BOTTLE_OPENER.get()));
    }

    public enum Kind {
        SMART,
        CREATURE,
        REDBOMB,
        MRSUGAR,
        OVERCHARGE,
        LUNA,
        BEPIS,
        BREEN,
        MUG,
        COFFEE,
        COFFEE_RADIUM,
        CHOCOLATE_MILK,
        NUKA,
        CHERRY,
        QUANTUM,
        KORL,
        FRITZ,
        SPARKLE,
        RAD
    }
}
