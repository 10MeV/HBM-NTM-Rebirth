package com.hbm.ntm.item;

import com.hbm.ntm.particle.ParticleUtil;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.ArmorUtil;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.jetbrains.annotations.Nullable;

public class DnsArmorItem extends FsbPoweredArmorItem {
    private static final UUID SPRINT_SPEED_UUID = UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");
    private static final String SPRINT_SPEED_NAME = "DNT SPEED";

    public DnsArmorItem(HbmArmorMaterials material, Type type, Properties properties,
            List<FullSetEffect> fullSetEffects, long baseMaxCharge, long chargeRate, long consumption, long drain,
            FullSetTraits fullSetTraits) {
        super(material, type, properties, fullSetEffects, baseMaxCharge, chargeRate, consumption, drain, fullSetTraits);
    }

    @Override
    public void tickEquippedArmor(ItemStack stack, Level level, Player player) {
        super.tickEquippedArmor(stack, level, player);
        if (getType() != Type.CHESTPLATE || !hasFullSet(player)) {
            return;
        }

        ArmorUtil.resetFlightTime(player);
        boolean jetpackActive = HbmPlayerProperties.isJetpackActive(player);
        boolean passiveGlide = !player.onGround() && !player.isShiftKeyDown()
                && HbmPlayerProperties.isBackpackEnabled(player);

        if (jetpackActive) {
            Vec3 movement = player.getDeltaMovement();
            if (movement.y < 0.6D) {
                player.setDeltaMovement(movement.x, movement.y + 0.2D, movement.z);
                player.hasImpulse = true;
            }
            player.fallDistance = 0.0F;
            playJetSound(level, player);
            if (!level.isClientSide) {
                ParticleUtil.spawnJetpackDns(level, player);
            }
        } else if (passiveGlide) {
            Vec3 movement = player.getDeltaMovement();
            double x = movement.x * 1.05D;
            double y = movement.y;
            double z = movement.z * 1.05D;
            if (y < -1.0D) {
                y += 0.4D;
            } else if (y < -0.1D) {
                y += 0.2D;
            } else if (y < 0.0D) {
                y = 0.0D;
            }
            if (player.zza != 0.0F) {
                Vec3 look = player.getLookAngle();
                x += look.x * 0.25D * player.zza;
                z += look.z * 0.25D * player.zza;
            }
            player.setDeltaMovement(x, y, z);
            player.hasImpulse = true;
            player.fallDistance = 0.0F;
            playJetSound(level, player);
            if (!level.isClientSide) {
                ParticleUtil.spawnJetpackDns(level, player);
            }
        }

        if (player.isShiftKeyDown() && !player.onGround()) {
            Vec3 movement = player.getDeltaMovement();
            player.setDeltaMovement(movement.x, movement.y - 0.1D, movement.z);
            player.hasImpulse = true;
        }
    }

    public static void reconcileSprintBoost(Player player) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        boolean hasDnsChest = chest.getItem() instanceof DnsArmorItem dns && dns.getType() == Type.CHESTPLATE;
        AttributeModifier existing = speed.getModifier(SPRINT_SPEED_UUID);
        if (existing != null && (SPRINT_SPEED_NAME.equals(existing.getName()) || hasDnsChest)) {
            speed.removeModifier(SPRINT_SPEED_UUID);
        }
        if (hasDnsChest && player.isSprinting()) {
            speed.addTransientModifier(new AttributeModifier(SPRINT_SPEED_UUID, SPRINT_SPEED_NAME,
                    0.25D, AttributeModifier.Operation.ADDITION));
        }
    }

    public static boolean tryCancelIncomingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player) || !hasDnsFullSet(player)) {
            return false;
        }
        if (event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        HbmPlayerProperties.plink(player, SoundEvents.ITEM_BREAK, 0.5F,
                1.0F + player.level().random.nextFloat() * 0.5F);
        event.setCanceled(true);
        return true;
    }

    public static float applyLegacyPreResistanceHurt(Player player, DamageSource source, float amount) {
        if (!hasDnsFullSet(player)) {
            return amount;
        }
        if (source != null && source.is(DamageTypeTags.IS_EXPLOSION)) {
            return amount * 0.001F;
        }
        return 0.0F;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("  + ")
                .append(Component.translatable("armor.rocketBoots"))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("  + ")
                .append(Component.translatable("armor.fastFall"))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("  + ")
                .append(Component.translatable("armor.sprintBoost"))
                .withStyle(ChatFormatting.AQUA));
    }

    private static void playJetSound(Level level, Player player) {
        LegacySoundPlayer.playLegacyImmolatorShoot(level, player.getX(), player.getY(), player.getZ(),
                player.getSoundSource(), 0.125F, 1.5F);
    }

    private static boolean hasDnsFullSet(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return chest.getItem() instanceof DnsArmorItem dns && dns.getType() == Type.CHESTPLATE
                && dns.hasFullSet(player);
    }
}
