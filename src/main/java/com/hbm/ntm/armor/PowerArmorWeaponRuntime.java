package com.hbm.ntm.armor;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.item.AmmoBagItem;
import com.hbm.ntm.item.CasingBagItem;
import com.hbm.ntm.item.PowerArmorWeaponItem;
import com.hbm.ntm.particle.LegacyConfettiUtil;
import com.hbm.ntm.registry.ModItems;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.hbm.ntm.util.HbmItemStackUtil;
import com.hbm.ntm.util.RayTraceUtil;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side equivalent of the old XFactoryPA / IPAWeaponsProvider path.
 * State lives only on the two old gun ItemStacks; armor remains the authority
 * for selecting a component.
 */
public final class PowerArmorWeaponRuntime {
    private static final String KEY_PHASE = "pa_weapon_phase";
    private static final String KEY_AGE = "pa_weapon_age";
    private static final int PHASE_IDLE = 0;
    private static final int PHASE_PRIMARY = 1;
    private static final int PHASE_SECONDARY = 2;
    private static final List<BulletConfig> NCRPA_STEER_ROCKETS = List.of(
            LegacySednaRuntimeBulletConfigs.ROCKET_NCRPA_STEER_HE,
            LegacySednaRuntimeBulletConfigs.ROCKET_NCRPA_STEER_HEAT,
            LegacySednaRuntimeBulletConfigs.ROCKET_NCRPA_STEER_DEMO,
            LegacySednaRuntimeBulletConfigs.ROCKET_NCRPA_STEER_INC,
            LegacySednaRuntimeBulletConfigs.ROCKET_NCRPA_STEER_PHOSPHORUS);
    private static final List<BulletConfig> NCRPA_ROCKETS = List.of(
            LegacySednaRuntimeBulletConfigs.ROCKET_NCRPA_HE,
            LegacySednaRuntimeBulletConfigs.ROCKET_NCRPA_HEAT,
            LegacySednaRuntimeBulletConfigs.ROCKET_NCRPA_DEMO,
            LegacySednaRuntimeBulletConfigs.ROCKET_NCRPA_INC,
            LegacySednaRuntimeBulletConfigs.ROCKET_NCRPA_PHOSPHORUS);

    public static boolean canUse(Player player, PowerArmorWeaponItem.Kind kind) {
        if (player == null || !FsbPoweredArmor.hasFullPoweredSetIgnoreCharge(player)) {
            return false;
        }
        ItemStack chest = player.getInventory().armor.get(2);
        return switch (kind) {
            case MELEE -> chest.is(ModItems.RPA_PLATE.get()) || chest.is(ModItems.NCRPA_PLATE.get());
            case RANGED -> chest.is(ModItems.NCRPA_PLATE.get());
        };
    }

    public static void activate(ServerPlayer player, ItemStack stack, PowerArmorWeaponItem.Kind kind,
            boolean primary) {
        if (!canUse(player, kind)) {
            return;
        }
        if (kind == PowerArmorWeaponItem.Kind.RANGED) {
            fireRocket(player, stack, primary);
            return;
        }
        if (phase(stack) != PHASE_IDLE) {
            return;
        }
        setPhase(stack, primary ? PHASE_PRIMARY : PHASE_SECONDARY);
        setAge(stack, 0);
    }

    public static void tick(ServerPlayer player, ItemStack stack, PowerArmorWeaponItem.Kind kind) {
        if (phase(stack) == PHASE_IDLE) {
            return;
        }
        if (kind == PowerArmorWeaponItem.Kind.RANGED) {
            if (age(stack) + 1 >= 10) {
                reset(stack);
            } else {
                setAge(stack, age(stack) + 1);
            }
            return;
        }
        if (!canUse(player, kind)) {
            reset(stack);
            return;
        }
        int age = age(stack) + 1;
        setAge(stack, age);
        boolean ncrpa = player.getInventory().armor.get(2).is(ModItems.NCRPA_PLATE.get());
        int currentPhase = phase(stack);
        if (isHitFrame(ncrpa, currentPhase, age)) {
            strike(player, ncrpa, currentPhase == PHASE_SECONDARY);
        }
        int duration = meleeDuration(ncrpa, currentPhase);
        if (age < duration) {
            return;
        }
        if (!ncrpa && currentPhase == PHASE_PRIMARY
                && com.hbm.ntm.network.HbmServerKeybinds.isPressed(player,
                com.hbm.ntm.network.HbmKeybind.GUN_PRIMARY)) {
            setAge(stack, 0);
            return;
        }
        reset(stack);
    }

    public static int phase(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? PHASE_IDLE : tag.getInt(KEY_PHASE);
    }

    public static int age(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(KEY_AGE);
    }

    private static void strike(ServerPlayer player, boolean ncrpa, boolean secondary) {
        HitResult hit = RayTraceUtil.getMouseOver(player, 3.0D, 0.5D, 1.0F);
        if (hit instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            if (target == player || (ncrpa && !target.isAlive())) {
                return;
            }
            float damage = secondary ? 35.0F : 15.0F;
            double knockback = secondary ? 1.5D : 0.0D;
            float dt = secondary ? 15.0F : 5.0F;
            float pierce = secondary ? 0.25F : 0.1F;
            if (target instanceof LivingEntity living) {
                if (living.getMaxHealth() >= 100.0F) {
                    damage *= 2.5F;
                }
                EntityDamageUtil.attackEntityFromNt(living, player.damageSources().playerAttack(player), damage,
                        true, false, knockback, dt, pierce);
                if (!living.isAlive() && (ncrpa || living.getRandom().nextInt(secondary ? 3 : 10) == 0)) {
                    LegacyConfettiUtil.gib(living);
                }
            } else {
                target.hurt(player.damageSources().playerAttack(player), damage);
            }
            LegacySoundPlayer.playSoundEffectRandomPitch(player.level(), target.getX(), target.getY(), target.getZ(),
                    ncrpa ? "hbm:weapon.fire.stab" : "hbm:weapon.fire.smack", SoundSource.PLAYERS,
                    1.0F, 0.9F, 0.2F);
            return;
        }
        if (hit instanceof BlockHitResult blockHit) {
            var state = player.level().getBlockState(blockHit.getBlockPos());
            var location = blockHit.getLocation();
            player.level().playSound(null, location.x, location.y, location.z,
                    state.getSoundType(player.level(), blockHit.getBlockPos(), player).getStepSound(),
                    SoundSource.PLAYERS, 2.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
        }
    }

    private static void fireRocket(ServerPlayer player, ItemStack stack, boolean steer) {
        if (phase(stack) != PHASE_IDLE) {
            return;
        }
        List<BulletConfig> accepted = steer ? NCRPA_STEER_ROCKETS : NCRPA_ROCKETS;
        AmmoSource ammo = findAmmo(player, accepted);
        if (ammo != null) {
            float yawRadians = player.getYRot() * net.minecraft.util.Mth.DEG_TO_RAD;
            double side = 0.25D * (player.getRandom().nextBoolean() ? -1.0D : 1.0D);
            // EntityBulletBaseMK4 rotates its (sideOffset, 0, 0) muzzle vector around
            // -yaw: (cos(yaw) * sideOffset, 0, -sin(yaw) * sideOffset).
            Vec3 position = new Vec3(player.getX() + Math.cos(yawRadians) * side,
                    player.getY() + player.getEyeHeight(), player.getZ() - Math.sin(yawRadians) * side);
            Vec3 heading = com.hbm.ntm.bullet.BulletKinematicsUtil.directionFromRotation(player.getYRot(),
                    player.getXRot());
            BulletLaunchUtil.LaunchPlan plan = BulletLaunchUtil.directedMk4LaunchPlan(ammo.config(), position,
                    heading, 1.0F, 0.0F, player.getRandom());
            if (plan.valid()) {
                BulletProjectileEntity bullet = BulletProjectileEntity.fromLaunchPlan(player.level(), plan, player);
                // EntityBulletBaseMK4 multiplies ArmorNCRPARanged's baseDamage=25 by config.damageMult.
                bullet.overrideDamage = 25.0F * ammo.config().damageMin();
                player.level().addFreshEntity(bullet);
                if (consumeAmmo(player, ammo)) {
                    handleCasingBag(player, ammo.config());
                }
                LegacySoundPlayer.playSoundAtEntity(player, "hbm:weapon.rpgShoot", SoundSource.PLAYERS,
                        0.5F, 0.9F + player.getRandom().nextFloat() * 0.2F);
            }
        } else {
            LegacySoundPlayer.playSoundAtEntity(player, "hbm:weapon.reload.dryFireClick", SoundSource.PLAYERS,
                    1.0F, 1.0F);
        }
        setPhase(stack, PHASE_PRIMARY);
        setAge(stack, 0);
    }

    private static AmmoSource findAmmo(Player player, List<BulletConfig> accepted) {
        for (ItemStack candidate : player.getInventory().items) {
            BulletConfig config = matching(candidate, accepted);
            if (config != null) {
                return new AmmoSource(config, candidate, ItemStack.EMPTY, null, -1, false);
            }
            boolean infinite = candidate.is(ModItems.AMMO_BAG_INFINITE.get());
            if (!infinite && !candidate.is(ModItems.AMMO_BAG.get())) {
                continue;
            }
            NonNullList<ItemStack> slots = HbmItemStackUtil.readStacksFromNbt(candidate, AmmoBagItem.SLOT_COUNT);
            for (int slot = 0; slot < slots.size(); slot++) {
                config = matching(slots.get(slot), accepted);
                if (config != null) {
                    return new AmmoSource(config, slots.get(slot), candidate, slots, slot, infinite);
                }
            }
        }
        return null;
    }

    private static BulletConfig matching(ItemStack stack, List<BulletConfig> accepted) {
        if (stack.isEmpty()) {
            return null;
        }
        for (BulletConfig config : accepted) {
            Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(config.ammo().itemId());
            if (item != null && stack.is(item)) {
                return config;
            }
        }
        return null;
    }

    private static boolean consumeAmmo(Player player, AmmoSource source) {
        if (!com.hbm.ntm.item.TrenchmasterArmorItem.shouldUseUpTrenchmasterAmmo(player)) {
            return false;
        }
        if (source.infinite()) {
            return true;
        }
        source.stack().shrink(1);
        if (!source.bag().isEmpty()) {
            source.slots().set(source.slot(), source.stack().isEmpty() ? ItemStack.EMPTY : source.stack());
            HbmItemStackUtil.setStacksToNbt(source.bag(), source.slots(), false);
        }
        return true;
    }

    private static void handleCasingBag(Player player, BulletConfig config) {
        if (config.casingItemName().isBlank() || config.casingItemAmount() <= 0) {
            return;
        }
        Item casingItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                com.hbm.ntm.bullet.BulletAmmo.legacyItem(config.casingItemName()).itemId());
        if (casingItem == null) {
            return;
        }
        ItemStack casing = new ItemStack(casingItem, Math.max(1, config.casingItemStackSize()));
        float amount = 1.0F / config.casingItemAmount() * 0.5F;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.CASING_BAG.get()) && CasingBagItem.pushCasing(stack, casing, amount)) {
                return;
            }
        }
    }

    private static boolean isHitFrame(boolean ncrpa, int phase, int age) {
        return phase == PHASE_PRIMARY
                ? (ncrpa ? age == 5 || age == 15 : age == 3 || age == 9)
                : (ncrpa ? age == 5 : age == 8);
    }

    private static int meleeDuration(boolean ncrpa, int phase) {
        return phase == PHASE_PRIMARY ? (ncrpa ? 25 : 14) : (ncrpa ? 30 : 20);
    }

    private static void setPhase(ItemStack stack, int phase) {
        stack.getOrCreateTag().putInt(KEY_PHASE, phase);
    }

    private static void setAge(ItemStack stack, int age) {
        stack.getOrCreateTag().putInt(KEY_AGE, Math.max(0, age));
    }

    private static void reset(ItemStack stack) {
        setPhase(stack, PHASE_IDLE);
        setAge(stack, 0);
    }

    private record AmmoSource(BulletConfig config, ItemStack stack, ItemStack bag, NonNullList<ItemStack> slots,
                              int slot, boolean infinite) {
    }

    private PowerArmorWeaponRuntime() {
    }
}
