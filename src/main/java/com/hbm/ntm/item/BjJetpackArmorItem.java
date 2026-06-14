package com.hbm.ntm.item;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.ArmorUtil;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BjJetpackArmorItem extends FsbPoweredArmorItem {
    public BjJetpackArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain);
    }

    public BjJetpackArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain,
            FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain, fullSetTraits);
    }

    @Override
    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        super.tickEquippedArmor(stack, level, player);
        if (!hasFullSet(player)) {
            return;
        }

        ArmorUtil.resetFlightTime(player);
        if (HbmPlayerProperties.isJetpackActive(player)) {
            Vec3 movement = player.getDeltaMovement();
            if (movement.y < 0.4D) {
                player.setDeltaMovement(movement.x, movement.y + 0.1D, movement.z);
                player.hasImpulse = true;
            }
            player.fallDistance = 0.0F;
            LegacySoundPlayer.playLegacyImmolatorShoot(level, player.getX(), player.getY(), player.getZ(),
                    player.getSoundSource(), 0.125F, 1.5F);
            if (!level.isClientSide) {
                ParticleUtil.spawnJetpackBj(level, player);
            }
        } else if (player.isShiftKeyDown()) {
            Vec3 movement = player.getDeltaMovement();
            if (movement.y < -0.08D) {
                double lift = movement.y * -0.4D;
                Vec3 look = player.getLookAngle().scale(lift);
                player.setDeltaMovement(movement.x + look.x, movement.y + lift + look.y, movement.z + look.z);
                player.hasImpulse = true;
                player.fallDistance = 0.0F;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("  + ")
                .append(Component.translatable("armor.electricJetpack"))
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("  + ")
                .append(Component.translatable("armor.glider"))
                .withStyle(ChatFormatting.GRAY));
    }
}
