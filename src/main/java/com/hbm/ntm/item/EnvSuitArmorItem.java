package com.hbm.ntm.item;

import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.armor.ArmorModItems;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EnvSuitArmorItem extends FsbPoweredArmorItem {
    private static final UUID SPRINT_SPEED_UUID = UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");

    public EnvSuitArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain);
    }

    @Override
    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        super.tickEquippedArmor(stack, level, player);
        if (getType() != Type.CHESTPLATE) {
            return;
        }

        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.removeModifier(SPRINT_SPEED_UUID);
        }
        if (!hasFullSet(player)) {
            return;
        }

        if (player.isSprinting() && speed != null) {
            speed.addTransientModifier(new AttributeModifier(SPRINT_SPEED_UUID, "SQUIRREL SPEED",
                    0.1D, AttributeModifier.Operation.ADDITION));
        }

        if (player.isInWater()) {
            if (!level.isClientSide) {
                player.setAirSupply(300);
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 15 * 20, 0, true, true));
            }
            double thrust = 0.1D * player.zza;
            Vec3 push = player.getLookAngle().scale(thrust);
            player.setDeltaMovement(player.getDeltaMovement().add(push));
            player.hasImpulse = true;
        } else if (!level.isClientSide && canRemoveEnvNightVision(player)) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }

    private static boolean canRemoveEnvNightVision(Player player) {
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack helmetMod = ArmorModHandler.pryMod(helmet, ArmorModHandler.helmet_only);
        return !(helmetMod.getItem() instanceof ArmorModItems.NightVision);
    }
}
