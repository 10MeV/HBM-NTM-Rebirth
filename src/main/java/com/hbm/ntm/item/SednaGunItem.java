package com.hbm.ntm.item;

import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.BulletConfigSyncRegistry;
import com.hbm.ntm.bullet.BulletKinematicsUtil;
import com.hbm.ntm.bullet.BulletLaunchUtil;
import com.hbm.ntm.bullet.LegacySednaRuntimeBulletConfigs;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.bullet.SednaMagazineConfig;
import com.hbm.ntm.bullet.SednaReceiverConfig;
import com.hbm.ntm.bullet.SednaWeaponModEvaluator;
import com.hbm.ntm.client.renderer.LegacyItemRendererBridge;
import com.hbm.ntm.client.renderer.SednaGunItemRenderer;
import com.hbm.ntm.config.WeaponConfig;
import com.hbm.ntm.damage.EntityDamageUtil;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.network.HbmKeybindReceiver;
import com.hbm.ntm.network.HbmServerKeybinds;
import com.hbm.ntm.player.HbmLivingProperties;
import com.hbm.ntm.registry.ModSounds;
import com.hbm.ntm.sound.LegacySoundIds;
import com.hbm.ntm.util.RayTraceUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SednaGunItem extends Item implements HbmKeybindReceiver {
    private static final String KEY_AIMING = "aiming";
    private static final String KEY_PRIMARY = "mouse1_";
    private static final String KEY_SECONDARY = "mouse2_";
    private static final String KEY_TERTIARY = "mouse3_";
    private static final String KEY_RELOAD = "reload_";
    private static final String KEY_WEAR = "wear_";
    private static final String KEY_MODE = "mode_";
    private static final String KEY_TIMER = "timer_";
    private static final String KEY_STATE = "state_";
    private static final String KEY_BAYONET_STRIKE = "bayonet_strike_";
    private static final String KEY_CANCEL_RELOAD = "cancel";
    private static final String KEY_EQUIPPED = "eqipped";
    private static final double BAYONET_REACH = 3.0D;
    private static final float BAYONET_DAMAGE = 15.0F;
    private static final int BAYONET_DAMAGE_TIMER = 15;

    private final SednaGunConfig gunConfig;

    public SednaGunItem(Properties properties, SednaGunConfig gunConfig) {
        super(properties.stacksTo(1));
        this.gunConfig = gunConfig;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide || !selected || !(entity instanceof ServerPlayer player)) {
            if (!level.isClientSide && entity instanceof ServerPlayer) {
                resetUnequippedState(stack);
            }
            return;
        }
        Optional<GunParts> parts = primaryParts(stack);
        if (parts.isEmpty()) {
            return;
        }
        handleEquipped(stack);
        if (!WeaponConfig.gunsEnabled()) {
            for (GunParts gun : allModeParts(stack)) {
                clearGunKeyStates(player, stack, gun.mode().configIndex());
            }
            return;
        }
        for (GunParts gun : allModeParts(stack)) {
            clearReleasedKeyStates(player, stack, gun.mode().configIndex());
            tickBayonetStrike(player, stack, gun);
            tickStandardStateMachine(player, stack, gun);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        primaryParts(stack).ifPresent(parts -> {
            SednaMagazineConfig magazine = parts.magazine();
            if (magazine.kind() == SednaMagazineConfig.Kind.BELT) {
                tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.sedna_gun.belt_ammo")
                        .withStyle(ChatFormatting.GRAY));
            } else if (magazine.kind() == SednaMagazineConfig.Kind.INFINITE) {
                tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.sedna_gun.infinite_ammo")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                int count = magazineCount(stack, magazine);
                tooltip.add(Component.translatable("tooltip.hbm_ntm_rebirth.sedna_gun.ammo",
                        count, magazine.capacity()).withStyle(ChatFormatting.GRAY));
            }
            gunConfig.defaultAmmo().ifPresent(defaultAmmo -> tooltip.add(Component.translatable(
                    "tooltip.hbm_ntm_rebirth.sedna_gun.default_ammo", defaultAmmo.ammoName(),
                    defaultAmmo.amount()).withStyle(ChatFormatting.DARK_GRAY)));
        });
    }

    public SednaGunConfig gunConfig() {
        return gunConfig;
    }

    public com.hbm.ntm.api.item.Crosshair currentCrosshair(ItemStack stack) {
        return primaryParts(stack)
                .map(parts -> toApiCrosshair(parts.mode().crosshair()))
                .orElse(com.hbm.ntm.api.item.Crosshair.NONE);
    }

    public boolean shouldHideCrosshair(ItemStack stack) {
        return primaryParts(stack)
                .map(parts -> parts.mode().hideCrosshair() && isAiming(stack))
                .orElse(false);
    }

    @Override
    public boolean canHandleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind) {
        return WeaponConfig.gunsEnabled()
                && (keybind == HbmKeybind.GUN_PRIMARY
                || keybind == HbmKeybind.GUN_SECONDARY
                || keybind == HbmKeybind.GUN_TERTIARY
                || keybind == HbmKeybind.RELOAD);
    }

    @Override
    public void handleKeybind(ServerPlayer player, ItemStack stack, HbmKeybind keybind, boolean pressed) {
        if (!WeaponConfig.gunsEnabled()) {
            return;
        }
        for (GunParts gun : allModeParts(stack)) {
            int configIndex = gun.mode().configIndex();
            switch (keybind) {
                case GUN_PRIMARY -> handlePrimaryKey(player, stack, gun, configIndex, pressed);
                case GUN_SECONDARY -> handleSecondaryKey(player, stack, gun, configIndex, pressed);
                case GUN_TERTIARY -> handleTertiaryKey(stack, gun, configIndex, pressed);
                case RELOAD -> handleReloadKey(player, stack, gun, configIndex, pressed);
                default -> {
                }
            }
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        LegacyItemRendererBridge.accept(consumer, () -> SednaGunItemRenderer.INSTANCE);
    }

    private static com.hbm.ntm.api.item.Crosshair toApiCrosshair(SednaGunConfig.Crosshair crosshair) {
        if (crosshair == null) {
            return com.hbm.ntm.api.item.Crosshair.NONE;
        }
        try {
            return com.hbm.ntm.api.item.Crosshair.valueOf(crosshair.name());
        } catch (IllegalArgumentException ignored) {
            return com.hbm.ntm.api.item.Crosshair.NONE;
        }
    }

    private void handlePrimaryKey(ServerPlayer player, ItemStack stack, GunParts gun, int configIndex, boolean pressed) {
        if (!handleEdgeKey(stack, KEY_PRIMARY, configIndex, pressed) || !pressed) {
            return;
        }
        if ("Lego.LAMBDA_STANDARD_CLICK_PRIMARY".equals(gun.mode().pressPrimaryHandlerName())) {
            clickPrimary(player, stack, gun);
        }
    }

    private void handleSecondaryKey(ServerPlayer player, ItemStack stack, GunParts gun, int configIndex,
            boolean pressed) {
        if (!handleEdgeKey(stack, KEY_SECONDARY, configIndex, pressed) || !pressed) {
            return;
        }
        String handler = gun.mode().pressSecondaryHandlerName();
        if (hasBayonetUpgrade(stack, gun) || "XFactory44.SMACK_A_FUCKER".equals(handler)) {
            beginBayonetInspect(player, stack, gun);
        } else if ("lambda:Lego.clickReceiver(receiver=1)".equals(handler)) {
            partsForReceiver(stack, configIndex, 1).ifPresent(receiverGun -> clickReceiver(player, stack, receiverGun));
        } else if ("Lego.LAMBDA_STANDARD_CLICK_PRIMARY".equals(handler)) {
            clickReceiver(player, stack, gun);
        } else if ("Lego.LAMBDA_STANDARD_CLICK_SECONDARY".equals(handler)) {
            clickModeToggle(player, stack, configIndex);
        } else if ("XFactory10ga.LAMBDA_DOUBLE_SECONDARY".equals(handler)) {
            clickDoubleSecondary(player, stack, gun);
        } else if ("XFactory12ga.LAMBDA_SPAS_SECONDARY".equals(handler)) {
            clickSpasSecondary(player, stack, gun);
        }
    }

    private void handleTertiaryKey(ItemStack stack, GunParts gun, int configIndex, boolean pressed) {
        if (!handleEdgeKey(stack, KEY_TERTIARY, configIndex, pressed) || !pressed) {
            return;
        }
        if ("Lego.LAMBDA_TOGGLE_AIM".equals(gun.mode().pressTertiaryHandlerName())) {
            setAiming(stack, !isAiming(stack));
        }
    }

    private void handleReloadKey(ServerPlayer player, ItemStack stack, GunParts gun, int configIndex, boolean pressed) {
        if (!handleEdgeKey(stack, KEY_RELOAD, configIndex, pressed) || !pressed) {
            return;
        }
        if ("Lego.LAMBDA_STANDARD_RELOAD".equals(gun.mode().pressReloadHandlerName())) {
            beginReloadOrInspect(player, stack, gun);
        }
    }

    private void clearGunKeyStates(ServerPlayer player, ItemStack stack, int configIndex) {
        handleEdgeKey(stack, KEY_PRIMARY, configIndex, false);
        handleEdgeKey(stack, KEY_SECONDARY, configIndex, false);
        handleEdgeKey(stack, KEY_TERTIARY, configIndex, false);
        handleEdgeKey(stack, KEY_RELOAD, configIndex, false);
        setReloadCancel(stack, false);
        setBayonetStrikePending(stack, configIndex, false);
        if (HbmServerKeybinds.isPressed(player, HbmKeybind.GUN_PRIMARY)
                || HbmServerKeybinds.isPressed(player, HbmKeybind.GUN_SECONDARY)
                || HbmServerKeybinds.isPressed(player, HbmKeybind.GUN_TERTIARY)
                || HbmServerKeybinds.isPressed(player, HbmKeybind.RELOAD)) {
            setAiming(stack, false);
        }
    }

    protected void clickPrimary(ServerPlayer player, ItemStack stack, GunParts gun) {
        clickReceiver(player, stack, gun);
    }

    protected void clickReceiver(ServerPlayer player, ItemStack stack, GunParts gun) {
        SednaGunConfig.GunState state = gunState(stack, gun.mode().configIndex());
        if (state == SednaGunConfig.GunState.IDLE) {
            LoadedRound round = getLoadedRound(player, stack, gun.magazine()).orElse(null);
            if (round != null) {
                fire(player.level(), player, stack, gun, round);
                setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.COOLDOWN);
                setTimer(stack, gun.mode().configIndex(), gun.receiver().delayAfterFire());
            } else if (gun.receiver().doesDryFire()) {
                setGunState(stack, gun.mode().configIndex(), gun.receiver().refireAfterDry()
                        ? SednaGunConfig.GunState.COOLDOWN
                        : SednaGunConfig.GunState.DRAWING);
                setTimer(stack, gun.mode().configIndex(), gun.receiver().delayAfterDryFire());
            }
        }
        if (state == SednaGunConfig.GunState.RELOADING) {
            setReloadCancel(stack, true);
        }
    }

    private void clickDoubleSecondary(ServerPlayer player, ItemStack stack, GunParts gun) {
        SednaGunConfig.GunState state = gunState(stack, gun.mode().configIndex());
        if (state == SednaGunConfig.GunState.IDLE) {
            LoadedRound round = getLoadedRound(player, stack, gun.magazine()).orElse(null);
            if (round != null) {
                if (fireLimited(player.level(), player, stack, gun, round, 1) > 0) {
                    playFireSound(player.level(), player, gun.receiver());
                }
                setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.COOLDOWN);
                setTimer(stack, gun.mode().configIndex(), gun.receiver().delayAfterFire());
            } else if (gun.receiver().doesDryFire()) {
                setGunState(stack, gun.mode().configIndex(), gun.receiver().refireAfterDry()
                        ? SednaGunConfig.GunState.COOLDOWN
                        : SednaGunConfig.GunState.DRAWING);
                setTimer(stack, gun.mode().configIndex(), gun.receiver().delayAfterDryFire());
            }
        }
        if (state == SednaGunConfig.GunState.RELOADING) {
            setReloadCancel(stack, true);
        }
    }

    private void clickSpasSecondary(ServerPlayer player, ItemStack stack, GunParts gun) {
        SednaGunConfig.GunState state = gunState(stack, gun.mode().configIndex());
        if (state == SednaGunConfig.GunState.IDLE) {
            LoadedRound round = getLoadedRound(player, stack, gun.magazine()).orElse(null);
            if (round != null) {
                int shotsFired = fireLimited(player.level(), player, stack, gun, round,
                        1 + gun.receiver().roundsPerCycle());
                if (shotsFired > 0) {
                    playFireSound(player.level(), player, gun.receiver(), shotsFired > 1 ? 0.9F : 1.0F);
                }
                setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.COOLDOWN);
                setTimer(stack, gun.mode().configIndex(), 20);
            } else if (gun.receiver().doesDryFire()) {
                setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.DRAWING);
                setTimer(stack, gun.mode().configIndex(), gun.receiver().delayAfterDryFire());
            }
        }
        if (state == SednaGunConfig.GunState.RELOADING) {
            setReloadCancel(stack, true);
        }
    }

    private void clickModeToggle(ServerPlayer player, ItemStack stack, int configIndex) {
        if (gunState(stack, configIndex) != SednaGunConfig.GunState.IDLE) {
            return;
        }
        int mode = gunMode(stack, 0);
        setGunMode(stack, configIndex, 1 - mode);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                mode == 0 ? ModSounds.WEAPON_SWITCHMODE1.get() : ModSounds.WEAPON_SWITCHMODE2.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private void beginReloadOrInspect(ServerPlayer player, ItemStack stack, GunParts gun) {
        int configIndex = gun.mode().configIndex();
        if (gunState(stack, configIndex) != SednaGunConfig.GunState.IDLE) {
            return;
        }
        setAiming(stack, false);
        if (canReload(player, stack, gun.magazine())) {
            int loaded = magazineCount(stack, gun.magazine());
            setAmountBeforeReload(stack, gun.magazine(), loaded);
            setGunState(stack, configIndex, SednaGunConfig.GunState.RELOADING);
            setTimer(stack, configIndex, gun.receiver().reloadBeginDuration()
                    + (loaded <= 0 ? gun.receiver().reloadCockOnEmptyPre() : 0));
            return;
        }
        if (!gun.mode().inspectCancel()) {
            setGunState(stack, configIndex, SednaGunConfig.GunState.DRAWING);
            setTimer(stack, configIndex, gun.mode().inspectDuration());
            if (hasBayonetUpgrade(stack, gun)) {
                setBayonetStrikePending(stack, configIndex, true);
            }
        }
    }

    private void beginBayonetInspect(ServerPlayer player, ItemStack stack, GunParts gun) {
        int configIndex = gun.mode().configIndex();
        SednaGunConfig.GunState state = gunState(stack, configIndex);
        if (state != SednaGunConfig.GunState.IDLE && state != SednaGunConfig.GunState.COOLDOWN) {
            return;
        }
        setAiming(stack, false);
        setGunState(stack, configIndex, SednaGunConfig.GunState.DRAWING);
        setTimer(stack, configIndex, gun.mode().inspectDuration());
        setBayonetStrikePending(stack, configIndex, true);
    }

    private void tickBayonetStrike(ServerPlayer player, ItemStack stack, GunParts gun) {
        int configIndex = gun.mode().configIndex();
        if (!bayonetStrikePending(stack, configIndex)
                || gunState(stack, configIndex) != SednaGunConfig.GunState.DRAWING
                || timer(stack, configIndex) != BAYONET_DAMAGE_TIMER) {
            return;
        }
        setBayonetStrikePending(stack, configIndex, false);
        applyBayonetStrike(player);
    }

    private void applyBayonetStrike(ServerPlayer player) {
        HitResult hit = RayTraceUtil.getMouseOver(player, BAYONET_REACH);
        if (hit instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            if (target == player) {
                return;
            }
            target.hurt(player.damageSources().playerAttack(player), BAYONET_DAMAGE);
            Vec3 motion = target.getDeltaMovement();
            target.setDeltaMovement(motion.x * 2.0D, motion.y, motion.z * 2.0D);
            player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    ModSounds.WEAPON_FIRE_STAB.get(), SoundSource.PLAYERS, 1.0F,
                    0.9F + player.getRandom().nextFloat() * 0.2F);
            return;
        }
        if (hit instanceof BlockHitResult blockHit && blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = player.level().getBlockState(pos);
            SoundEvent sound = state.getSoundType(player.level(), pos, player).getStepSound();
            Vec3 location = blockHit.getLocation();
            player.level().playSound(null, location.x, location.y, location.z, sound, SoundSource.PLAYERS,
                    2.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
        }
    }

    protected void fire(Level level, Player player, ItemStack stack, GunParts gun, LoadedRound round) {
        int shotsFired = fireLimited(level, player, stack, gun, round, Math.max(1, gun.receiver().roundsPerCycle()));
        if (shotsFired <= 0) {
            return;
        }
        playFireSound(level, player, gun.receiver());
    }

    private int fireLimited(Level level, Player player, ItemStack stack, GunParts gun, LoadedRound round,
            int maxShots) {
        SednaReceiverConfig receiver = gun.receiver();
        int rounds = Math.max(1, maxShots);
        int shots = player.getAbilities().instabuild ? rounds : Math.min(rounds, round.count());
        if (shots <= 0) {
            return 0;
        }

        int shotsFired = 0;
        for (int shot = 0; shot < shots; shot++) {
            int projectiles = BulletLaunchUtil.rollProjectileCount(round.config(), player.getRandom());
            boolean firedShot = false;
            for (int i = 0; i < projectiles; i++) {
                BulletLaunchUtil.LaunchPlan plan = launchPlan(player, stack, round.config(), receiver);
                if (!plan.valid()) {
                    continue;
                }
                BulletProjectileEntity bullet = createBullet(level, player, stack, gun, round.config(), receiver,
                        receiver.baseDamage() * round.config().damageMin());
                if (bullet == null) {
                    continue;
                }
                level.addFreshEntity(bullet);
                firedShot = true;
            }
            if (firedShot) {
                consumeRound(player, stack, gun.magazine(), round.config());
                if (shouldApplyWear(gun)) {
                    addWearClamped(stack, gun.mode().configIndex(), round.config().wear(), gun.mode().durability());
                }
                shotsFired++;
            }
        }

        if (shotsFired > 0) {
            applyPanzerschreckNoShieldFire(level, player, stack, gun);
        }
        return shotsFired;
    }

    protected boolean shouldApplyWear(GunParts gun) {
        String handler = gun.receiver().fireHandlerName();
        return !"Lego.LAMBDA_NOWEAR_FIRE".equals(handler)
                && !"XFactoryFolly.LAMBDA_FIRE".equals(handler);
    }

    private void applyPanzerschreckNoShieldFire(Level level, Player player, ItemStack stack, GunParts gun) {
        if (!gunConfig.legacyName().equals("gun_panzerschreck")
                || !SednaWeaponModEvaluator.hasUpgrade(stack, gun.mode().configIndex(),
                SednaWeaponModEvaluator.ID_NO_SHIELD)) {
            return;
        }
        HbmLivingProperties.addFire(player, 100);
        EntityDamageUtil.attackEntityFromNt(player, level.damageSources().onFire(), 4.0F,
                true, false, 0.0D, 0.0F, 0.0F);
    }

    private void tickStandardStateMachine(ServerPlayer player, ItemStack stack, GunParts gun) {
        int configIndex = gun.mode().configIndex();
        int timer = timer(stack, configIndex);
        if (timer > 0) {
            setTimer(stack, configIndex, timer - 1);
        }
        if (timer <= 1) {
            decideStandardState(player, stack, gun, gunState(stack, configIndex));
        }
    }

    private void decideStandardState(ServerPlayer player, ItemStack stack, GunParts gun,
            SednaGunConfig.GunState lastState) {
        int configIndex = gun.mode().configIndex();
        if (lastState == SednaGunConfig.GunState.DRAWING) {
            setGunState(stack, configIndex, SednaGunConfig.GunState.IDLE);
            setTimer(stack, configIndex, 0);
            return;
        }
        if (lastState == SednaGunConfig.GunState.JAMMED) {
            setGunState(stack, configIndex, SednaGunConfig.GunState.IDLE);
            setTimer(stack, configIndex, 0);
            return;
        }
        if (lastState == SednaGunConfig.GunState.RELOADING) {
            finishReloadCycle(player, stack, gun);
            return;
        }
        if (lastState == SednaGunConfig.GunState.COOLDOWN) {
            finishCooldown(player, stack, gun);
        }
    }

    private void finishReloadCycle(ServerPlayer player, ItemStack stack, GunParts gun) {
        SednaMagazineConfig magazine = gun.magazine();
        tryReload(stack, player, magazine);
        boolean cancel = reloadCancel(stack);
        if (!cancel && canReload(player, stack, magazine)) {
            setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.RELOADING);
            setTimer(stack, gun.mode().configIndex(), gun.receiver().reloadCycleDuration());
        } else {
            if (jamChance(stack, gun) > player.getRandom().nextFloat()) {
                setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.JAMMED);
                setTimer(stack, gun.mode().configIndex(), gun.receiver().jamDuration());
            } else {
                setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.DRAWING);
                setTimer(stack, gun.mode().configIndex(), gun.receiver().reloadEndDuration()
                        + (amountBeforeReload(stack, magazine) <= 0
                        ? gun.receiver().reloadCockOnEmptyPost()
                        : 0));
            }
            setReloadCancel(stack, false);
        }
        setAmountAfterReload(stack, magazine, magazineCount(stack, magazine));
    }

    private void finishCooldown(ServerPlayer player, ItemStack stack, GunParts gun) {
        boolean refireHeld = shouldAutoRefire(player, stack, gun);
        if (gun.receiver().refireOnHold() && refireHeld) {
            LoadedRound round = getLoadedRound(player, stack, gun.magazine()).orElse(null);
            if (round != null) {
                fire(player.level(), player, stack, gun, round);
                setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.COOLDOWN);
                setTimer(stack, gun.mode().configIndex(), gun.receiver().delayAfterFire());
            } else if (gun.receiver().doesDryFireAfterAuto()) {
                setGunState(stack, gun.mode().configIndex(), gun.receiver().refireAfterDry()
                        ? SednaGunConfig.GunState.COOLDOWN
                        : SednaGunConfig.GunState.DRAWING);
                setTimer(stack, gun.mode().configIndex(), gun.receiver().delayAfterDryFire());
            } else {
                setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.IDLE);
                setTimer(stack, gun.mode().configIndex(), 0);
            }
        } else if (gun.receiver().reloadOnEmpty() && magazineCount(stack, gun.magazine()) <= 0) {
            setAiming(stack, false);
            if (canReload(player, stack, gun.magazine())) {
                int loaded = magazineCount(stack, gun.magazine());
                setAmountBeforeReload(stack, gun.magazine(), loaded);
                setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.RELOADING);
                setTimer(stack, gun.mode().configIndex(), gun.receiver().reloadBeginDuration()
                        + (loaded <= 0 ? gun.receiver().reloadCockOnEmptyPre() : 0));
            } else {
                setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.IDLE);
                setTimer(stack, gun.mode().configIndex(), 0);
            }
        } else {
            setGunState(stack, gun.mode().configIndex(), SednaGunConfig.GunState.IDLE);
            setTimer(stack, gun.mode().configIndex(), 0);
        }
    }

    private boolean shouldAutoRefire(ServerPlayer player, ItemStack stack, GunParts gun) {
        String decider = gun.mode().deciderName();
        boolean modeAllowsAuto = gunMode(stack, gun.mode().configIndex()) == 0;
        if ("XFactory556mm.LAMBDA_STG77_DECIDER".equals(decider)) {
            return HbmServerKeybinds.isPressed(player, HbmKeybind.GUN_SECONDARY);
        }
        if ("XFactory9mm.LAMBDA_SECOND_UZI".equals(decider)
                || "XFactory762mm.LAMBDA_SECOND_MINIGUN".equals(decider)) {
            return modeAllowsAuto && HbmServerKeybinds.isPressed(player, HbmKeybind.GUN_SECONDARY);
        }
        return modeAllowsAuto && HbmServerKeybinds.isPressed(player, HbmKeybind.GUN_PRIMARY);
    }

    @Nullable
    protected BulletProjectileEntity createBullet(Level level, Player player, ItemStack stack, GunParts gun,
            BulletConfig config, SednaReceiverConfig receiver, float overrideDamage) {
        BulletLaunchUtil.LaunchPlan plan = launchPlan(player, stack, config, receiver);
        if (!plan.valid()) {
            return null;
        }
        BulletProjectileEntity bullet = BulletProjectileEntity.fromLaunchPlan(level, plan, player);
        bullet.overrideDamage = overrideDamage;
        return bullet;
    }

    protected BulletLaunchUtil.LaunchPlan launchPlan(Player player, ItemStack stack, BulletConfig config,
            SednaReceiverConfig receiver) {
        SednaReceiverConfig.Offset offset = isAiming(stack)
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

    protected float spread(Player player, BulletConfig config, SednaReceiverConfig receiver) {
        float spread = config.spread() * receiver.spreadAmmoMultiplier() + receiver.spreadInnate();
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SednaGunItem) || !isAiming(stack)) {
            spread += receiver.spreadHipfire();
        }
        return Math.max(0.0F, spread);
    }

    protected void playFireSound(Level level, Player player, SednaReceiverConfig receiver) {
        playFireSound(level, player, receiver, 1.0F);
    }

    protected void playFireSound(Level level, Player player, SednaReceiverConfig receiver, float pitchMultiplier) {
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
                receiver.fireVolume(), receiver.firePitch() * pitchMultiplier);
    }

    private boolean tryReload(ItemStack gunStack, Player player, SednaMagazineConfig magazine) {
        Optional<RuntimeAmmo> ammo = findReloadAmmo(player, magazine);
        if (ammo.isEmpty()) {
            return false;
        }

        int capacity = Math.max(1, magazine.capacity());
        int before = magazineCount(gunStack, magazine);
        if (before >= capacity) {
            return false;
        }
        int loadLimit = switch (magazine.kind()) {
            case SINGLE_RELOAD -> 1;
            case FULL_RELOAD -> capacity;
            default -> 1;
        };
        int loaded = Math.min(capacity - before, loadLimit);
        if (!player.getAbilities().instabuild) {
            loaded = Math.min(loaded, ammo.get().stack().getCount());
            ammo.get().stack().shrink(loaded);
        }
        if (loaded <= 0) {
            return false;
        }

        int after = before + loaded;
        CompoundTag tag = gunStack.getOrCreateTag();
        tag.putString(magazine.nbtTypeKey(), ammo.get().config().legacyName());
        tag.putInt(magazine.nbtCountKey(), after);
        tag.putInt(magazine.nbtBeforeReloadKey(), before);
        tag.putInt(magazine.nbtAfterReloadKey(), after);
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

    protected Optional<LoadedRound> getLoadedRound(Player player, ItemStack stack, SednaMagazineConfig magazine) {
        if (magazine.kind() == SednaMagazineConfig.Kind.INFINITE) {
            List<BulletConfig> accepted = acceptedRuntimeConfigs(magazine);
            return accepted.isEmpty()
                    ? Optional.empty()
                    : Optional.of(new LoadedRound(accepted.get(0), Integer.MAX_VALUE));
        }
        if (magazine.kind() == SednaMagazineConfig.Kind.BELT) {
            Optional<RuntimeAmmo> ammo = findBeltAmmo(player, magazine);
            ammo.ifPresent(runtimeAmmo -> stack.getOrCreateTag().putString(magazine.nbtTypeKey(),
                    runtimeAmmo.config().legacyName()));
            return ammo.map(runtimeAmmo -> new LoadedRound(runtimeAmmo.config(),
                    beltAmmoCount(player, runtimeAmmo.config())));
        }
        int count = magazineCount(stack, magazine);
        if (count <= 0) {
            return Optional.empty();
        }
        CompoundTag tag = stack.getTag();
        String legacyName = tag == null ? "" : tag.getString(magazine.nbtTypeKey());
        Optional<BulletConfig> stored = LegacySednaRuntimeBulletConfigs.byName(legacyName);
        List<BulletConfig> accepted = acceptedRuntimeConfigs(magazine);
        if (stored.isPresent() && accepted.contains(stored.get())) {
            return Optional.of(new LoadedRound(stored.get(), count));
        }
        return accepted.isEmpty() ? Optional.empty() : Optional.of(new LoadedRound(accepted.get(0), count));
    }

    protected int magazineCount(ItemStack stack, SednaMagazineConfig magazine) {
        CompoundTag tag = stack.getTag();
        return tag == null || magazine.nbtCountKey().isEmpty() ? 0 : tag.getInt(magazine.nbtCountKey());
    }

    protected void setMagazineCount(ItemStack stack, SednaMagazineConfig magazine, int count) {
        stack.getOrCreateTag().putInt(magazine.nbtCountKey(), Math.max(0, count));
    }

    private void consumeRound(Player player, ItemStack gunStack, SednaMagazineConfig magazine, BulletConfig config) {
        if (player.getAbilities().instabuild) {
            return;
        }
        if (magazine.kind() == SednaMagazineConfig.Kind.INFINITE) {
            return;
        }
        if (magazine.kind() == SednaMagazineConfig.Kind.BELT) {
            consumeBeltAmmo(player, config);
        } else {
            setMagazineCount(gunStack, magazine, Math.max(0, magazineCount(gunStack, magazine) - 1));
        }
    }

    protected Optional<RuntimeAmmo> findBeltAmmo(Player player, SednaMagazineConfig magazine) {
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

    private int beltAmmoCount(Player player, BulletConfig config) {
        if (player.getAbilities().instabuild) {
            return Integer.MAX_VALUE;
        }
        Item item = ForgeRegistries.ITEMS.getValue(config.ammo().itemId());
        if (item == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void consumeBeltAmmo(Player player, BulletConfig config) {
        Item item = ForgeRegistries.ITEMS.getValue(config.ammo().itemId());
        if (item == null) {
            return;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.is(item)) {
                stack.shrink(1);
                return;
            }
        }
    }

    protected void addWear(ItemStack stack, int configIndex, int wear) {
        if (wear <= 0) {
            return;
        }
        String key = KEY_WEAR + configIndex;
        CompoundTag tag = stack.getOrCreateTag();
        tag.putFloat(key, tag.getFloat(key) + wear);
    }

    protected void addWearClamped(ItemStack stack, int configIndex, int wear, float durability) {
        if (wear <= 0) {
            return;
        }
        String key = KEY_WEAR + configIndex;
        CompoundTag tag = stack.getOrCreateTag();
        float next = tag.getFloat(key) + wear;
        tag.putFloat(key, durability > 0.0F ? Math.min(next, durability) : next);
    }

    protected float wear(ItemStack stack, int configIndex) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0.0F : tag.getFloat(KEY_WEAR + configIndex);
    }

    private int gunMode(ItemStack stack, int configIndex) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(KEY_MODE + configIndex);
    }

    private void setGunMode(ItemStack stack, int configIndex, int mode) {
        stack.getOrCreateTag().putInt(KEY_MODE + configIndex, mode);
    }

    private float jamChance(ItemStack stack, GunParts gun) {
        if (gun.mode().durability() <= 0.0F) {
            return 0.0F;
        }
        float percent = wear(stack, gun.mode().configIndex()) / gun.mode().durability();
        if (percent < 0.66F) {
            return 0.0F;
        }
        return Math.min((percent - 0.66F) * 4.0F, 1.0F);
    }

    private boolean canReload(Player player, ItemStack stack, SednaMagazineConfig magazine) {
        if (magazine.kind() == SednaMagazineConfig.Kind.BELT
                || magazine.kind() == SednaMagazineConfig.Kind.INFINITE) {
            return false;
        }
        return magazineCount(stack, magazine) < Math.max(1, magazine.capacity())
                && findReloadAmmo(player, magazine).isPresent();
    }

    private int amountBeforeReload(ItemStack stack, SednaMagazineConfig magazine) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(magazine.nbtBeforeReloadKey());
    }

    private void setAmountBeforeReload(ItemStack stack, SednaMagazineConfig magazine, int count) {
        stack.getOrCreateTag().putInt(magazine.nbtBeforeReloadKey(), Math.max(0, count));
    }

    private void setAmountAfterReload(ItemStack stack, SednaMagazineConfig magazine, int count) {
        stack.getOrCreateTag().putInt(magazine.nbtAfterReloadKey(), Math.max(0, count));
    }

    protected int timer(ItemStack stack, int configIndex) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(KEY_TIMER + configIndex);
    }

    protected void setTimer(ItemStack stack, int configIndex, int timer) {
        stack.getOrCreateTag().putInt(KEY_TIMER + configIndex, Math.max(0, timer));
    }

    protected SednaGunConfig.GunState gunState(ItemStack stack, int configIndex) {
        CompoundTag tag = stack.getTag();
        int ordinal = tag == null ? 0 : tag.getByte(KEY_STATE + configIndex);
        SednaGunConfig.GunState[] values = SednaGunConfig.GunState.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SednaGunConfig.GunState.DRAWING;
    }

    protected void setGunState(ItemStack stack, int configIndex, SednaGunConfig.GunState state) {
        stack.getOrCreateTag().putByte(KEY_STATE + configIndex, (byte) state.ordinal());
    }

    private boolean reloadCancel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(KEY_CANCEL_RELOAD);
    }

    private void setReloadCancel(ItemStack stack, boolean cancel) {
        stack.getOrCreateTag().putBoolean(KEY_CANCEL_RELOAD, cancel);
    }

    private boolean equipped(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(KEY_EQUIPPED);
    }

    private void setEquipped(ItemStack stack, boolean equipped) {
        stack.getOrCreateTag().putBoolean(KEY_EQUIPPED, equipped);
    }

    private void handleEquipped(ItemStack stack) {
        if (!equipped(stack)) {
            for (GunParts gun : allModeParts(stack)) {
                int configIndex = gun.mode().configIndex();
                handleEdgeKey(stack, KEY_PRIMARY, configIndex, false);
                handleEdgeKey(stack, KEY_SECONDARY, configIndex, false);
                handleEdgeKey(stack, KEY_TERTIARY, configIndex, false);
                handleEdgeKey(stack, KEY_RELOAD, configIndex, false);
                if (gunState(stack, configIndex) == SednaGunConfig.GunState.DRAWING
                        && timer(stack, configIndex) == 0) {
                    setTimer(stack, configIndex, gun.mode().drawDuration());
                }
            }
        }
        setEquipped(stack, true);
    }

    private void resetUnequippedState(ItemStack stack) {
        for (GunParts gun : allModeParts(stack)) {
            int configIndex = gun.mode().configIndex();
            if (gunState(stack, configIndex) != SednaGunConfig.GunState.JAMMED) {
                setGunState(stack, configIndex, SednaGunConfig.GunState.DRAWING);
                setTimer(stack, configIndex, gun.mode().drawDuration());
            }
            setAiming(stack, false);
            setReloadCancel(stack, false);
            setEquipped(stack, false);
            setBayonetStrikePending(stack, configIndex, false);
        }
    }

    protected boolean isAiming(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(KEY_AIMING);
    }

    protected void setAiming(ItemStack stack, boolean aiming) {
        stack.getOrCreateTag().putBoolean(KEY_AIMING, aiming);
    }

    protected boolean handleEdgeKey(ItemStack stack, String key, int index, boolean pressed) {
        boolean previous = keyState(stack, key, index);
        if (previous == pressed) {
            return false;
        }
        stack.getOrCreateTag().putBoolean(key + index, pressed);
        return true;
    }

    private boolean keyState(ItemStack stack, String key, int index) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(key + index);
    }

    private boolean bayonetStrikePending(ItemStack stack, int configIndex) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(KEY_BAYONET_STRIKE + configIndex);
    }

    private void setBayonetStrikePending(ItemStack stack, int configIndex, boolean pending) {
        stack.getOrCreateTag().putBoolean(KEY_BAYONET_STRIKE + configIndex, pending);
    }

    private boolean hasBayonetUpgrade(ItemStack stack, GunParts gun) {
        int configIndex = gun.mode().configIndex();
        return (gunConfig.legacyName().equals("gun_mas36")
                && SednaWeaponModEvaluator.hasUpgrade(stack, configIndex, SednaWeaponModEvaluator.ID_MAS_BAYONET))
                || (gunConfig.legacyName().equals("gun_carbine")
                && SednaWeaponModEvaluator.hasUpgrade(stack, configIndex,
                SednaWeaponModEvaluator.ID_CARBINE_BAYONET));
    }

    private void clearReleasedKeyStates(ServerPlayer player, ItemStack stack, int configIndex) {
        clearReleasedKeyState(player, stack, configIndex, HbmKeybind.GUN_PRIMARY, KEY_PRIMARY);
        clearReleasedKeyState(player, stack, configIndex, HbmKeybind.GUN_SECONDARY, KEY_SECONDARY);
        clearReleasedKeyState(player, stack, configIndex, HbmKeybind.GUN_TERTIARY, KEY_TERTIARY);
        clearReleasedKeyState(player, stack, configIndex, HbmKeybind.RELOAD, KEY_RELOAD);
    }

    private void clearReleasedKeyState(ServerPlayer player, ItemStack stack, int configIndex, HbmKeybind keybind,
            String key) {
        if (!HbmServerKeybinds.isPressed(player, keybind) && keyState(stack, key, configIndex)) {
            stack.getOrCreateTag().putBoolean(key + configIndex, false);
        }
    }

    protected List<BulletConfig> acceptedRuntimeConfigs(SednaMagazineConfig magazine) {
        return magazine.acceptedBulletConfigNames().stream()
                .map(LegacySednaRuntimeBulletConfigs::byName)
                .flatMap(Optional::stream)
                .toList();
    }

    protected Optional<GunParts> primaryParts() {
        return partsForMode(ItemStack.EMPTY, 0);
    }

    protected Optional<GunParts> primaryParts(ItemStack stack) {
        return partsForMode(stack, 0);
    }

    private List<GunParts> allModeParts(ItemStack stack) {
        return gunConfig.configs().stream()
                .map(SednaGunConfig.GunModeConfig::configIndex)
                .map(index -> partsForMode(stack, index))
                .flatMap(Optional::stream)
                .toList();
    }

    protected Optional<GunParts> partsForMode(ItemStack stack, int configIndex) {
        return partsForReceiver(stack, configIndex, 0);
    }

    protected Optional<GunParts> partsForReceiver(ItemStack stack, int configIndex, int receiverIndex) {
        if (gunConfig.configs().isEmpty()) {
            return Optional.empty();
        }
        Optional<SednaGunConfig.GunModeConfig> modeOptional = gunConfig.configs().stream()
                .filter(candidate -> candidate.configIndex() == configIndex)
                .findFirst();
        if (modeOptional.isEmpty()) {
            return Optional.empty();
        }
        SednaGunConfig.GunModeConfig mode = modeOptional.get();
        SednaGunConfig.GunModeConfig effectiveMode = stack.isEmpty()
                ? mode
                : SednaWeaponModEvaluator.effectiveMode(stack, gunConfig.legacyName(), mode);
        Optional<SednaReceiverConfig> receiverOptional = mode.receivers().stream()
                .filter(candidate -> candidate.receiverIndex() == receiverIndex)
                .findFirst();
        if (receiverOptional.isEmpty()) {
            return Optional.empty();
        }
        SednaReceiverConfig receiver = receiverOptional.get();
        return receiver.magazine().map(magazine -> {
            SednaMagazineConfig effectiveMagazine = stack.isEmpty()
                    ? magazine
                    : SednaWeaponModEvaluator.effectiveMagazine(stack, gunConfig.legacyName(),
                            mode.configIndex(), magazine);
            SednaReceiverConfig effectiveReceiver = stack.isEmpty()
                    ? receiver
                    : SednaWeaponModEvaluator.effectiveReceiver(stack, gunConfig.legacyName(),
                            mode.configIndex(), receiver);
            return new GunParts(effectiveMode, effectiveReceiver, effectiveMagazine);
        });
    }

    protected record GunParts(
            SednaGunConfig.GunModeConfig mode,
            SednaReceiverConfig receiver,
            SednaMagazineConfig magazine) {
    }

    protected record RuntimeAmmo(BulletConfig config, ItemStack stack) {
    }

    protected record LoadedRound(BulletConfig config, int count) {
    }
}
