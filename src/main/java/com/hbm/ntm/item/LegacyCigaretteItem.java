package com.hbm.ntm.item;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.AchievementHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

public class LegacyCigaretteItem extends Item {
    private static final ChatFormatting[] GREAT_COLORS = {
            ChatFormatting.RED,
            ChatFormatting.GOLD,
            ChatFormatting.YELLOW,
            ChatFormatting.GREEN,
            ChatFormatting.AQUA,
            ChatFormatting.BLUE,
            ChatFormatting.DARK_PURPLE,
            ChatFormatting.LIGHT_PURPLE
    };

    private final Kind kind;

    public LegacyCigaretteItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 30;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            stack.shrink(1);
            if (kind == Kind.CIGARETTE) {
                HbmLivingProperties.incrementBlackLung(player, 2000);
                HbmLivingProperties.incrementAsbestos(player, 2000);
                HbmLivingProperties.incrementRadiation(player, 100.0F);
                if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.NO9.get())) {
                    AchievementHandler.award(player, AchievementHandler.NO9);
                }
            } else {
                HbmLivingProperties.incrementBlackLung(player, 500);
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                player.heal(10.0F);
            }
            LegacySoundPlayer.playLegacyPlayerCough(player);
            ParticleUtil.spawnVomit(player, ParticleUtil.VOMIT_SMOKE, 30);
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        if (kind == Kind.CIGARETTE) {
            tooltip.add(Component.literal("\u2713 Asbestos filter").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("\u2713 High in tar").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("\u2713 Tobacco contains 100% Polonium-210").withStyle(ChatFormatting.RED));
            tooltip.add(Component.literal("\u2713 Yum").withStyle(ChatFormatting.RED));
            return;
        }

        int color = (int) (Util.getMillis() % 2000L * GREAT_COLORS.length / 2000L);
        tooltip.add(Component.literal("This can't be good for me, but I feel ")
                .append(Component.literal("GREAT").withStyle(GREAT_COLORS[color])));
    }

    public enum Kind {
        CIGARETTE,
        CRACKPIPE
    }
}
