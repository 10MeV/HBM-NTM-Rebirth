package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.sound.LegacySoundPlayer;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Legacy package facade for the 1.7.10 injector knife armor module.
 */
@Deprecated(forRemoval = false)
public class ItemModKnife extends ItemArmorMod {
    public static final UUID trigamma_UUID = UUID.fromString("86d44ca9-44f1-4ca6-bdbb-d9d33bead251");

    public ItemModKnife() {
        super(ArmorModHandler.extra, false, true, false, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Pain.").withStyle(ChatFormatting.RED));
        tooltip.add(Component.empty());
        tooltip.add(Component.literal("Hurts, doesn't it?").withStyle(ChatFormatting.RED));
        tooltip.add(Component.empty());
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public void addDesc(List<Component> tooltip, ItemStack stack, ItemStack armor) {
        tooltip.add(Component.literal("  ").append(stack.getHoverName()).withStyle(ChatFormatting.RED));
    }

    @Override
    public void modUpdate(LivingEntity entity, ItemStack armor) {
        if (entity.level().isClientSide || entity.tickCount % 50 != 0 || entity.getMaxHealth() <= 2.0F) {
            return;
        }

        AttributeInstance maxHealth = entity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        LegacySoundPlayer.playLegacySlicer(entity);
        ParticleUtil.spawnVomit(entity, ParticleUtil.VOMIT_BLOOD, 25);

        double currentMax = entity.getMaxHealth();
        maxHealth.removeModifier(trigamma_UUID);
        double restoredMax = entity.getMaxHealth();
        double modifier = -(restoredMax - currentMax + 2.0D);
        maxHealth.addPermanentModifier(new AttributeModifier(trigamma_UUID, "digamma",
                modifier, AttributeModifier.Operation.ADDITION));
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth());
        }

        if (entity instanceof ServerPlayer player) {
            CompoundTag data = new CompoundTag();
            data.putString("type", ParticleUtil.TYPE_PROPER_JOLT);
            if (entity.getMaxHealth() > 2.0F) {
                data.putInt("time", 10_000 + entity.getRandom().nextInt(10_000));
                data.putInt("maxTime", 10_000);
            } else {
                data.putInt("time", 0);
                data.putInt("maxTime", 0);
            }
            ModMessages.sendAuxParticle(player, 0.0D, 0.0D, 0.0D, data);
        }
    }
}
