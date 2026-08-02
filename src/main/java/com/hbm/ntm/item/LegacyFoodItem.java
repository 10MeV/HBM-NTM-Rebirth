package com.hbm.ntm.item;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class LegacyFoodItem extends Item {
    private final Kind kind;

    public LegacyFoodItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return kind == Kind.MUCHO_MANGO ? 200 : super.getUseDuration(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return kind == Kind.MUCHO_MANGO ? UseAnim.DRINK : UseAnim.EAT;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof Player player) {
            applyFoodEffects(player);
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        switch (kind) {
            case MED_IPECAC, MED_PTSD -> {
                tooltip.add(Component.translatable(getDescriptionId() + ".desc.0"));
                tooltip.add(Component.translatable(getDescriptionId() + ".desc.1"));
            }
            case LEMON, MUCHO_MANGO, QUESADILLA -> tooltip.add(Component.translatable(getDescriptionId() + ".desc"));
            case PUDDING -> {
                tooltip.add(Component.translatable(getDescriptionId() + ".desc.0"));
                tooltip.add(Component.translatable(getDescriptionId() + ".desc.1"));
                tooltip.add(Component.translatable(getDescriptionId() + ".desc.2"));
            }
        }
    }

    private void applyFoodEffects(Player player) {
        switch (kind) {
            case MED_IPECAC, MED_PTSD -> {
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 50, 49));
                ParticleUtil.spawnVomit(player, ParticleUtil.VOMIT_NORMAL, 0);
                LegacySoundPlayer.playSoundAtEntity(player, "hbm:player.vomit", SoundSource.HOSTILE, 1.0F, 1.0F);
            }
            case MUCHO_MANGO -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
            case QUESADILLA, PUDDING -> {
            }
        }
    }

    public enum Kind {
        LEMON,
        MED_IPECAC,
        MED_PTSD,
        MUCHO_MANGO,
        QUESADILLA,
        PUDDING
    }
}
