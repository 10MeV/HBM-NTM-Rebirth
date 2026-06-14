package com.hbm.ntm.armor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.BulletConfigSyncRegistry;
import com.hbm.ntm.bullet.BulletKinematicsUtil;
import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = HbmNtm.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ArmorModEvents {
    private static final List<Attribute> MANAGED_ATTRIBUTES = List.of(
            Attributes.MAX_HEALTH,
            Attributes.MOVEMENT_SPEED,
            Attributes.ATTACK_DAMAGE,
            Attributes.KNOCKBACK_RESISTANCE);

    private static final List<UUID> MANAGED_UUIDS = List.of(
            ArmorModHandler.UUIDs[0],
            ArmorModHandler.UUIDs[1],
            ArmorModHandler.UUIDs[2],
            ArmorModHandler.UUIDs[3],
            ArmorModItems.BottledCloud.SPEED_UUID);
    private static final List<GauntletAmmo> GAUNTLET_AMMO = List.of(
            new GauntletAmmo(ModItems.AMMO_STANDARD_G12_BP, LegacySednaRuntimeBulletConfigs.G12_BP),
            new GauntletAmmo(ModItems.AMMO_STANDARD_G12_BP_MAGNUM, LegacySednaRuntimeBulletConfigs.G12_BP_MAGNUM),
            new GauntletAmmo(ModItems.AMMO_STANDARD_G12_BP_SLUG, LegacySednaRuntimeBulletConfigs.G12_BP_SLUG),
            new GauntletAmmo(ModItems.AMMO_STANDARD_G12, LegacySednaRuntimeBulletConfigs.G12),
            new GauntletAmmo(ModItems.AMMO_STANDARD_G12_SLUG, LegacySednaRuntimeBulletConfigs.G12_SLUG),
            new GauntletAmmo(ModItems.AMMO_STANDARD_G12_FLECHETTE, LegacySednaRuntimeBulletConfigs.G12_FLECHETTE),
            new GauntletAmmo(ModItems.AMMO_STANDARD_G12_MAGNUM, LegacySednaRuntimeBulletConfigs.G12_MAGNUM),
            new GauntletAmmo(ModItems.AMMO_STANDARD_G12_EXPLOSIVE, LegacySednaRuntimeBulletConfigs.G12_EXPLOSIVE),
            new GauntletAmmo(ModItems.AMMO_STANDARD_G12_PHOSPHORUS, LegacySednaRuntimeBulletConfigs.G12_PHOSPHORUS));

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide || entity.isDeadOrDying()) {
            return;
        }
        tickArmorMods(entity);
        tickDirectWearablePlateMod(entity);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!ArmorModHandler.hasMods(armor)) {
                continue;
            }
            for (ItemStack mod : ArmorModHandler.pryMods(armor)) {
                if (mod.getItem() instanceof ArmorModItem armorMod) {
                    armorMod.onArmorModHurt(event, armor, mod);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!ArmorModHandler.hasMods(chestplate) || hasHeldAttackDamage(player.getMainHandItem())) {
            return;
        }
        ItemStack servo = ArmorModHandler.pryMod(chestplate, ArmorModHandler.servos);
        if (!(servo.getItem() instanceof ArmorModItems.BallisticGauntlet)) {
            return;
        }
        GauntletAmmo ammo = consumeGauntletAmmo(player);
        if (ammo == null) {
            return;
        }
        int bullets = BulletLaunchUtil.rollProjectileCount(ammo.config(), player.getRandom());
        for (int i = 0; i < bullets; i++) {
            BulletLaunchUtil.LaunchPlan plan = gauntletLaunchPlan(player, ammo.config());
            BulletProjectileEntity bullet = BulletProjectileEntity.fromLaunchPlan(level, plan, player);
            bullet.overrideDamage = 15.0F * ammo.config().damageMin();
            level.addFreshEntity(bullet);
        }
        LegacySoundPlayer.playLegacyShotgunShoot(player, 1.0F, 1.0F);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!ArmorModHandler.hasMods(armor)) {
                continue;
            }
            ItemStack mod = ArmorModHandler.pryMod(armor, ArmorModHandler.extra);
            if (mod.getItem() instanceof ArmorModItems.Revive revive) {
                revive.handleDeath(event, armor, mod);
                return;
            }
            if (mod.getItem() instanceof ArmorModItems.Shackles shackles) {
                shackles.handleDeath(event, armor, mod);
                if (event.isCanceled()) {
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (!(stack.getItem() instanceof ArmorItem) || !ArmorModHandler.hasMods(stack)) {
            return;
        }
        ItemStack cladding = ArmorModHandler.pryMod(stack, ArmorModHandler.cladding);
        if (cladding.getItem() instanceof ArmorModItems.ObsidianCladding) {
            event.getEntity().setInvulnerable(true);
        }
    }

    private static boolean hasHeldAttackDamage(ItemStack stack) {
        return !stack.isEmpty()
                && stack.getAttributeModifiers(EquipmentSlot.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE);
    }

    private static GauntletAmmo consumeGauntletAmmo(Player player) {
        for (GauntletAmmo ammo : GAUNTLET_AMMO) {
            Item item = ammo.item().get();
            for (ItemStack stack : player.getInventory().items) {
                if (stack.is(item)) {
                    stack.shrink(1);
                    return ammo;
                }
            }
        }
        return null;
    }

    private static BulletLaunchUtil.LaunchPlan gauntletLaunchPlan(Player player, BulletConfig config) {
        Vec3 offset = new Vec3(-0.1875D, -0.0625D, 0.5D)
                .xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                .yRot(-player.getYRot() * Mth.DEG_TO_RAD);
        Vec3 position = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ()).add(offset);
        Vec3 motion = BulletKinematicsUtil.directionFromRotation(player.getYRot(), player.getXRot());
        BulletLaunchUtil.Rotation rotation = BulletLaunchUtil.rotationFromMotion(motion);
        return new BulletLaunchUtil.LaunchPlan(config, BulletConfigSyncRegistry.syncedState(config), position, motion,
                rotation.yaw(), rotation.pitch(), BulletKinematicsUtil.ENTITY_SIZE,
                BulletKinematicsUtil.RENDER_DISTANCE_WEIGHT, true);
    }

    private record GauntletAmmo(RegistryObject<Item> item, BulletConfig config) {
    }

    private static void tickArmorMods(LivingEntity entity) {
        reconcileAttributeModifiers(entity);
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!ArmorModHandler.hasMods(armor)) {
                continue;
            }
            for (ItemStack mod : ArmorModHandler.pryMods(armor)) {
                if (mod.getItem() instanceof ArmorModItem armorMod) {
                    armorMod.onArmorModTick(entity, armor, mod);
                }
            }
        }
    }

    private static void tickDirectWearablePlateMod(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ArmorModItems.Jetpack jetpack) {
            jetpack.tickJetpack(player, chest);
        } else if (chest.getItem() instanceof ArmorModItems.Wings wings) {
            wings.tickWings(player);
        }
    }

    private static void reconcileAttributeModifiers(LivingEntity entity) {
        removeManagedModifiers(entity);
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        for (ItemStack armor : entity.getArmorSlots()) {
            if (!ArmorModHandler.hasMods(armor)) {
                continue;
            }
            for (ItemStack mod : ArmorModHandler.pryMods(armor)) {
                if (mod.getItem() instanceof ArmorModItem armorMod) {
                    armorMod.addArmorModAttributeModifiers(armor, mod, modifiers);
                }
            }
        }
        for (var entry : modifiers.entries()) {
            AttributeInstance instance = entity.getAttribute(entry.getKey());
            if (instance != null && instance.getModifier(entry.getValue().getId()) == null) {
                instance.addTransientModifier(entry.getValue());
            }
        }
    }

    private static void removeManagedModifiers(LivingEntity entity) {
        for (Attribute attribute : MANAGED_ATTRIBUTES) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            for (UUID uuid : MANAGED_UUIDS) {
                if (instance.getModifier(uuid) != null) {
                    instance.removeModifier(uuid);
                }
            }
        }
    }

    private ArmorModEvents() {
    }
}
