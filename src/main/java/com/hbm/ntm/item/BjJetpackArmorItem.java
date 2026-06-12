package com.hbm.ntm.item;

import com.hbm.ntm.armor.FsbPoweredArmor;
import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.registry.ModSounds;
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

    @Override
    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        super.tickEquippedArmor(stack, level, player);
        if (!FsbPoweredArmor.hasFullPoweredSet(player)) {
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
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.WEAPON_FLAMETHROWER_SHOOT.get(), player.getSoundSource(), 0.125F, 1.5F);
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
        tooltip.add(Component.literal("  + Electric jetpack").withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("  + Glider").withStyle(ChatFormatting.GRAY));
    }
}
