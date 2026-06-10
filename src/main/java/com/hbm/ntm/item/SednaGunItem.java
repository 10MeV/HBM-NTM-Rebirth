package com.hbm.ntm.item;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.BulletConfigSyncRegistry;
import com.hbm.ntm.bullet.BulletKinematicsUtil;
import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaMagazineConfig;
import com.hbm.ntm.bullet.SednaReceiverConfig;
import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import com.hbm.ntm.client.renderer.SednaGunItemRenderer;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.sound.LegacySoundIds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SednaGunItem extends Item {
    private static final String KEY_WEAR = "wear_";

    private final SednaGunConfig gunConfig;

    public SednaGunItem(Properties properties, SednaGunConfig gunConfig) {
        super(properties.stacksTo(1));
        this.gunConfig = gunConfig;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Optional<GunParts> parts = primaryParts();
        if (parts.isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }

        GunParts gun = parts.get();
        LoadedRound round = getLoadedRound(stack, gun.magazine()).orElse(null);
        if (round == null) {
            if (!level.isClientSide && tryReload(stack, player, gun.magazine())) {
                player.getCooldowns().addCooldown(this, reloadCooldown(gun.receiver()));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (!level.isClientSide) {
            fire(level, player, stack, gun, round);
            player.getCooldowns().addCooldown(this, Math.max(1, gun.receiver().delayAfterFire()));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        primaryParts().ifPresent(parts -> {
            SednaMagazineConfig magazine = parts.magazine();
            int count = magazineCount(stack, magazine);
            tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.sedna_gun.ammo",
                    count, magazine.capacity()).withStyle(ChatFormatting.GRAY));
            gunConfig.defaultAmmo().ifPresent(defaultAmmo -> tooltip.add(Component.translatable(
                    "tooltip.hbm_ntm_rebirth.sedna_gun.default_ammo", defaultAmmo.ammoName(),
                    defaultAmmo.amount()).withStyle(ChatFormatting.DARK_GRAY)));
        });
    }

    public SednaGunConfig gunConfig() {
        return gunConfig;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> SednaGunItemRenderer.INSTANCE);
    }

    private void fire(Level level, Player player, ItemStack stack, GunParts gun, LoadedRound round) {
        SednaReceiverConfig receiver = gun.receiver();
        int rounds = Math.max(1, receiver.roundsPerCycle());
        int loaded = magazineCount(stack, gun.magazine());
        int shots = player.getAbilities().instabuild ? rounds : Math.min(rounds, loaded);
        if (shots <= 0) {
            return;
        }

        for (int shot = 0; shot < shots; shot++) {
            int projectiles = BulletLaunchUtil.rollProjectileCount(round.config(), player.getRandom());
            for (int i = 0; i < projectiles; i++) {
                BulletLaunchUtil.LaunchPlan plan = launchPlan(player, round.config(), receiver);
                if (!plan.valid()) {
                    continue;
                }
                BulletProjectileEntity bullet = BulletProjectileEntity.fromLaunchPlan(level, plan, player);
                bullet.overrideDamage = receiver.baseDamage() * round.config().damageMin();
                level.addFreshEntity(bullet);
            }
            if (!player.getAbilities().instabuild) {
                setMagazineCount(stack, gun.magazine(), Math.max(0, magazineCount(stack, gun.magazine()) - 1));
            }
            addWear(stack, gun.mode().configIndex(), round.config().wear());
        }

        playFireSound(level, player, receiver);
    }

    private BulletLaunchUtil.LaunchPlan launchPlan(Player player, BulletConfig config, SednaReceiverConfig receiver) {
        SednaReceiverConfig.Offset offset = player.isShiftKeyDown()
                ? receiver.projectileOffsetScoped()
                : receiver.projectileOffset();
        Vec3 localOffset = new Vec3(offset.side(), offset.up(), offset.forward())
                .xRot(-player.getXRot() * Mth.DEG_TO_RAD)
                .yRot(-player.getYRot() * Mth.DEG_TO_RAD);
        Vec3 position = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ())
                .add(localOffset);
        Vec3 direction = BulletKinematicsUtil.directionFromRotation(player.getYRot(), player.getXRot());
        Vec3 motion = BulletKinematicsUtil.shootWithSpread(direction, BulletKinematicsUtil.DEFAULT_THROW_FORCE,
                spread(player, config, receiver), player.getRandom());
        return new BulletLaunchUtil.LaunchPlan(config, BulletConfigSyncRegistry.syncedState(config), position, motion,
                player.getYRot(), player.getXRot(), BulletKinematicsUtil.ENTITY_SIZE,
                BulletKinematicsUtil.RENDER_DISTANCE_WEIGHT, true);
    }

    private float spread(Player player, BulletConfig config, SednaReceiverConfig receiver) {
        float spread = config.spread() * receiver.spreadAmmoMultiplier() + receiver.spreadInnate();
        if (!player.isShiftKeyDown()) {
            spread += receiver.spreadHipfire();
        }
        return Math.max(0.0F, spread);
    }

    private void playFireSound(Level level, Player player, SednaReceiverConfig receiver) {
        SoundEvent sound = receiver.fireSoundLocation()
                .map(ForgeRegistries.SOUND_EVENTS::getValue)
                .orElse(null);
        if (sound == null) {
            sound = LegacySoundIds.resolveEvent(receiver.fireSoundName());
        }
        if (sound == null) {
            sound = ModSounds.WEAPON_SHOTGUN_SHOOT.get();
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS,
                receiver.fireVolume(), receiver.firePitch());
    }

    private boolean tryReload(ItemStack gunStack, Player player, SednaMagazineConfig magazine) {
        Optional<RuntimeAmmo> ammo = findReloadAmmo(player, magazine);
        if (ammo.isEmpty()) {
            return false;
        }

        int capacity = Math.max(1, magazine.capacity());
        int loadLimit = switch (magazine.kind()) {
            case SINGLE_RELOAD -> 1;
            case FULL_RELOAD -> capacity;
            default -> 1;
        };
        int target = Math.min(capacity, loadLimit);
        if (!player.getAbilities().instabuild) {
            target = Math.min(target, ammo.get().stack().getCount());
            ammo.get().stack().shrink(target);
        }
        if (target <= 0) {
            return false;
        }

        CompoundTag tag = gunStack.getOrCreateTag();
        tag.putString(magazine.nbtTypeKey(), ammo.get().config().legacyName());
        tag.putInt(magazine.nbtCountKey(), target);
        tag.putInt(magazine.nbtBeforeReloadKey(), 0);
        tag.putInt(magazine.nbtAfterReloadKey(), target);
        return true;
    }

    private Optional<RuntimeAmmo> findReloadAmmo(Player player, SednaMagazineConfig magazine) {
        List<BulletConfig> accepted = acceptedRuntimeConfigs(magazine);
        if (accepted.isEmpty()) {
            return Optional.empty();
        }
        if (player.getAbilities().instabuild) {
            return Optional.of(new RuntimeAmmo(accepted.get(0), ItemStack.EMPTY));
        }
        Inventory inventory = player.getInventory();
        for (BulletConfig config : accepted) {
            Item item = ForgeRegistries.ITEMS.getValue(config.ammo().itemId());
            if (item == null) {
                continue;
            }
            for (ItemStack stack : inventory.items) {
                if (!stack.isEmpty() && stack.is(item)) {
                    return Optional.of(new RuntimeAmmo(config, stack));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<LoadedRound> getLoadedRound(ItemStack stack, SednaMagazineConfig magazine) {
        int count = magazineCount(stack, magazine);
        if (count <= 0) {
            return Optional.empty();
        }
        CompoundTag tag = stack.getTag();
        String legacyName = tag == null ? "" : tag.getString(magazine.nbtTypeKey());
        Optional<BulletConfig> stored = LegacySednaRuntimeBulletConfigs.byName(legacyName);
        if (stored.isPresent()) {
            return Optional.of(new LoadedRound(stored.get(), count));
        }
        List<BulletConfig> accepted = acceptedRuntimeConfigs(magazine);
        return accepted.isEmpty() ? Optional.empty() : Optional.of(new LoadedRound(accepted.get(0), count));
    }

    private int magazineCount(ItemStack stack, SednaMagazineConfig magazine) {
        CompoundTag tag = stack.getTag();
        return tag == null || magazine.nbtCountKey().isEmpty() ? 0 : tag.getInt(magazine.nbtCountKey());
    }

    private void setMagazineCount(ItemStack stack, SednaMagazineConfig magazine, int count) {
        stack.getOrCreateTag().putInt(magazine.nbtCountKey(), Math.max(0, count));
    }

    private void addWear(ItemStack stack, int configIndex, int wear) {
        if (wear <= 0) {
            return;
        }
        String key = KEY_WEAR + configIndex;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putFloat(key, tag.getFloat(key) + wear);
    }

    private List<BulletConfig> acceptedRuntimeConfigs(SednaMagazineConfig magazine) {
        return magazine.acceptedBulletConfigNames().stream()
                .map(LegacySednaRuntimeBulletConfigs::byName)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<GunParts> primaryParts() {
        if (gunConfig.configs().isEmpty()) {
            return Optional.empty();
        }
        SednaGunConfig.GunModeConfig mode = gunConfig.configs().get(0);
        if (mode.receivers().isEmpty()) {
            return Optional.empty();
        }
        SednaReceiverConfig receiver = mode.receivers().get(0);
        return receiver.magazine().map(magazine -> new GunParts(mode, receiver, magazine));
    }

    private int reloadCooldown(SednaReceiverConfig receiver) {
        int reload = receiver.reloadBeginDuration() + receiver.reloadCycleDuration()
                + receiver.reloadEndDuration() + receiver.reloadCockOnEmptyPre()
                + receiver.reloadCockOnEmptyPost();
        return Math.max(1, reload);
    }

    private record GunParts(
            SednaGunConfig.GunModeConfig mode,
            SednaReceiverConfig receiver,
            SednaMagazineConfig magazine) {
    }

    private record RuntimeAmmo(BulletConfig config, ItemStack stack) {
    }

    private record LoadedRound(BulletConfig config, int count) {
    }
}
