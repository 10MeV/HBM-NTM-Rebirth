package com.hbm.ntm.player;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.armor.ArmorModItems;
import com.hbm.ntm.api.item.ArmorDashProvider;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class HbmPlayerProperties {
    public static final ResourceLocation DATA_TYPE = new ResourceLocation(HbmNtm.MOD_ID, "player_props");
    public static final int NOTICE_ID_JETPACK = 5;
    public static final int NOTICE_ID_MAGNET = 6;
    public static final int NOTICE_ID_HUD = 7;
    public static final String TAG_ROOT = "HbmPlayerProps";
    public static final String TAG_PREVIOUS_ROOT = "hbm_player_props";
    public static final String KEY_HAS_RECEIVED_BOOK = "hasReceivedBook";
    public static final String KEY_SHIELD = "shield";
    public static final String KEY_MAX_SHIELD = "maxShield";
    public static final String KEY_ENABLE_BACKPACK = "enableBackpack";
    public static final String KEY_ENABLE_MAGNET = "enableMagnet";
    public static final String KEY_ENABLE_HUD = "enableHUD";
    public static final String KEY_REPUTATION = "reputation";
    public static final String KEY_IS_ON_LADDER = "isOnLadder";
    public static final String KEY_DASH_COUNT = "dashCount";
    public static final String KEY_STAMINA = "stamina";
    public static final String KEY_DASH_COOLDOWN = "dashCooldown";
    public static final int DASH_COOLDOWN_LENGTH = 5;
    public static final int PLINK_COOLDOWN_LENGTH = 10;
    public static final float SHIELD_CAP = 100.0F;

    private static final int NOTICE_MILLIS = 1_000;
    private static final Map<UUID, EnumSet<HbmKeybind>> PRESSED_KEYS = new ConcurrentHashMap<>();
    private static final Map<UUID, RuntimeData> RUNTIME_DATA = new ConcurrentHashMap<>();

    public static boolean hasReceivedBook(Player player) {
        return getTag(player).getBoolean(KEY_HAS_RECEIVED_BOOK);
    }

    public static void setHasReceivedBook(Player player, boolean received) {
        setBoolean(player, KEY_HAS_RECEIVED_BOOK, received);
    }

    public static float getShield(Player player) {
        return getTag(player).getFloat(KEY_SHIELD);
    }

    public static void setShield(Player player, float shield) {
        setFloat(player, KEY_SHIELD, Math.max(0.0F, shield));
    }

    public static float getMaxShield(Player player) {
        return getTag(player).getFloat(KEY_MAX_SHIELD);
    }

    public static void setMaxShield(Player player, float maxShield) {
        setFloat(player, KEY_MAX_SHIELD, Math.max(0.0F, maxShield));
    }

    public static boolean canApplyShieldInfusion(Player player) {
        return getMaxShield(player) < SHIELD_CAP;
    }

    public static boolean applyShieldInfusion(Player player, float infusion) {
        if (player == null || infusion <= 0.0F || !canApplyShieldInfusion(player)) {
            return false;
        }
        setMaxShield(player, Math.min(SHIELD_CAP, getMaxShield(player) + infusion));
        setShield(player, Math.min(getShield(player) + infusion, getEffectiveMaxShield(player)));
        return true;
    }

    public static float getEffectiveMaxShield(Player player) {
        return Math.min(getMaxShield(player), SHIELD_CAP) + getArmorShieldBonus(player);
    }

    public static float absorbShieldDamage(Player player, float amount) {
        if (player == null || amount <= 0.0F) {
            return Math.max(0.0F, amount);
        }
        float shield = getShield(player);
        if (shield <= 0.0F) {
            return amount;
        }
        float reduced = Math.min(shield, amount);
        setShield(player, shield - reduced);
        return amount - reduced;
    }

    public static void markDamaged(Player player) {
        if (player != null) {
            setLastDamage(player, player.tickCount);
        }
    }

    public static boolean isBackpackEnabled(Player player) {
        return getDefaultTrueBoolean(player, KEY_ENABLE_BACKPACK);
    }

    public static void setBackpackEnabled(ServerPlayer player, boolean enabled) {
        setBoolean(player, KEY_ENABLE_BACKPACK, enabled);
    }

    public static boolean isMagnetEnabled(Player player) {
        return getDefaultTrueBoolean(player, KEY_ENABLE_MAGNET);
    }

    public static boolean isMagnetActive(Player player) {
        return isMagnetEnabled(player);
    }

    public static void setMagnetEnabled(ServerPlayer player, boolean enabled) {
        setBoolean(player, KEY_ENABLE_MAGNET, enabled);
    }

    public static boolean isHudEnabled(Player player) {
        return getDefaultTrueBoolean(player, KEY_ENABLE_HUD);
    }

    public static void setHudEnabled(ServerPlayer player, boolean enabled) {
        setBoolean(player, KEY_ENABLE_HUD, enabled);
    }

    public static int getReputation(Player player) {
        return getTag(player).getInt(KEY_REPUTATION);
    }

    public static void setReputation(Player player, int reputation) {
        setInt(player, KEY_REPUTATION, reputation);
    }

    public static int addReputation(Player player, int amount) {
        int reputation = getReputation(player) + amount;
        setReputation(player, reputation);
        return reputation;
    }

    public static boolean decrementReputationAbove(Player player, int floor) {
        int reputation = getReputation(player);
        if (reputation <= floor) {
            return false;
        }
        setReputation(player, reputation - 1);
        return true;
    }

    public static boolean hasReputationAtLeast(Player player, int threshold) {
        return getReputation(player) >= threshold;
    }

    public static boolean hasReputationAtMost(Player player, int threshold) {
        return getReputation(player) <= threshold;
    }

    public static boolean isOnLadder(Player player) {
        return getTag(player).getBoolean(KEY_IS_ON_LADDER);
    }

    public static void setOnLadder(Player player, boolean onLadder) {
        setBoolean(player, KEY_IS_ON_LADDER, onLadder);
    }

    public static boolean getKeyPressed(Player player, HbmKeybind keybind) {
        if (player == null || keybind == null) {
            return false;
        }
        EnumSet<HbmKeybind> keys = PRESSED_KEYS.get(player.getUUID());
        return keys != null && keys.contains(keybind);
    }

    public static void setKeyPressed(ServerPlayer player, HbmKeybind keybind, boolean pressed) {
        if (player == null || keybind == null) {
            return;
        }
        boolean wasPressed = getKeyPressed(player, keybind);
        EnumSet<HbmKeybind> keys = PRESSED_KEYS.computeIfAbsent(player.getUUID(), ignored -> EnumSet.noneOf(HbmKeybind.class));
        if (pressed) {
            keys.add(keybind);
        } else {
            keys.remove(keybind);
            if (keys.isEmpty()) {
                PRESSED_KEYS.remove(player.getUUID());
            }
        }

        if (!wasPressed && pressed) {
            handleRisingEdge(player, keybind);
        }
    }

    public static boolean isJetpackActive(Player player) {
        return isBackpackEnabled(player) && getKeyPressed(player, HbmKeybind.JETPACK);
    }

    public static void clearKeyStates(Player player) {
        if (player != null) {
            PRESSED_KEYS.remove(player.getUUID());
        }
    }

    public static boolean isDashActivated(Player player) {
        return runtime(player).dashActivated;
    }

    public static void setDashActivated(Player player, boolean activated) {
        runtime(player).dashActivated = activated;
    }

    public static int getDashCooldown(Player player) {
        return runtime(player).dashCooldown;
    }

    public static void setDashCooldown(Player player, int cooldown) {
        runtime(player).dashCooldown = Math.max(0, cooldown);
    }

    public static int getDashCount(Player player) {
        return runtime(player).totalDashCount;
    }

    public static void setDashCount(Player player, int count) {
        runtime(player).totalDashCount = Math.max(0, count);
    }

    public static int getStamina(Player player) {
        return runtime(player).stamina;
    }

    public static void setStamina(Player player, int stamina) {
        runtime(player).stamina = Math.max(0, stamina);
    }

    public static int getPlinkCooldown(Player player) {
        return runtime(player).plinkCooldown;
    }

    public static void setPlinkCooldown(Player player, int cooldown) {
        runtime(player).plinkCooldown = Math.max(0, cooldown);
    }

    public static int getLastDamage(Player player) {
        return runtime(player).lastDamage;
    }

    public static void setLastDamage(Player player, int lastDamage) {
        runtime(player).lastDamage = Math.max(0, lastDamage);
    }

    public static int getGrenadeDeployment(Player player) {
        return runtime(player).grenadeDeployment;
    }

    public static void setGrenadeDeployment(Player player, int deployment) {
        runtime(player).grenadeDeployment = Math.max(0, deployment);
    }

    public static void resetGrenadeDeployment(Player player) {
        setGrenadeDeployment(player, 0);
    }

    public static int incrementGrenadeDeployment(Player player) {
        RuntimeData data = runtime(player);
        data.grenadeDeployment = Math.max(0, data.grenadeDeployment) + 1;
        return data.grenadeDeployment;
    }

    public static boolean isGrenadeDeploymentReady(Player player, int drawDuration) {
        return getGrenadeDeployment(player) >= Math.max(0, drawDuration);
    }

    public static void clearRuntime(Player player) {
        if (player != null) {
            RUNTIME_DATA.remove(player.getUUID());
        }
    }

    public static void tickRuntime(Player player) {
        if (player == null) {
            return;
        }
        RuntimeData data = runtime(player);
        if (data.plinkCooldown > 0) {
            data.plinkCooldown--;
        }
        rechargeShield(player);
        handleDashing(player);
        handleFauxLadder(player);
        syncRuntimeIfChanged(player, data);
    }

    public static void plink(Player player, SoundEvent sound, float volume, float pitch) {
        if (player == null || sound == null) {
            return;
        }
        RuntimeData data = runtime(player);
        if (data.plinkCooldown > 0) {
            return;
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
        data.plinkCooldown = PLINK_COOLDOWN_LENGTH;
    }

    public static void plinkItemBreak(Player player, float volume, float pitch) {
        plink(player, SoundEvents.ITEM_BREAK, volume, pitch);
    }

    private static void rechargeShield(Player player) {
        float effectiveMax = getEffectiveMaxShield(player);
        float shield = getShield(player);
        if (shield > effectiveMax) {
            setShield(player, effectiveMax);
            return;
        }
        if (shield >= effectiveMax || player.tickCount <= getLastDamage(player) + 60) {
            return;
        }

        int ticksSinceDelay = player.tickCount - (getLastDamage(player) + 60);
        setShield(player, shield + Math.min(effectiveMax - shield, 0.005F * ticksSinceDelay));
    }

    private static float getArmorShieldBonus(Player player) {
        if (player == null) {
            return 0.0F;
        }
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack insert = ArmorModHandler.pryMod(chestplate, ArmorModHandler.kevlar);
        if (insert.getItem() instanceof ArmorModItems.Shield shieldMod) {
            return shieldMod.shield();
        }
        return 0.0F;
    }

    private static void handleDashing(Player player) {
        RuntimeData data = runtime(player);
        int dashCount = getArmorModDashCount(player);
        boolean dashActivated = getKeyPressed(player, HbmKeybind.DASH);

        data.totalDashCount = 0;
        if (dashCount * 30 < data.stamina) {
            data.stamina = dashCount * 30;
        }
        if (dashCount <= 0) {
            return;
        }

        int perDash = 30;
        int stamina = data.stamina;
        data.totalDashCount = dashCount;

        if (data.dashCooldown <= 0) {
            if (dashActivated && stamina >= perDash) {
                dash(player);
                data.dashCooldown = DASH_COOLDOWN_LENGTH;
                stamina -= perDash;
            }
        } else {
            data.dashCooldown--;
            if (player instanceof ServerPlayer serverPlayer) {
                setKeyPressed(serverPlayer, HbmKeybind.DASH, false);
            }
        }

        if (stamina < data.totalDashCount * perDash) {
            stamina++;
            if (stamina % perDash == perDash - 1) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.TOOL_TECH_BOOP.get(), SoundSource.PLAYERS,
                        1.0F, 1.0F + (1.0F / 12.0F) * (stamina / perDash));
                stamina++;
            }
        }

        data.stamina = stamina;
    }

    private static int getArmorModDashCount(Player player) {
        int dashCount = 0;
        for (ItemStack armor : player.getArmorSlots()) {
            if (!(armor.getItem() instanceof ArmorItem) || !ArmorModHandler.hasMods(armor)) {
                continue;
            }
            for (ItemStack mod : ArmorModHandler.pryMods(armor)) {
                if (mod.getItem() instanceof ArmorDashProvider dashProvider) {
                    dashCount += dashProvider.getDashes();
                }
            }
        }
        return dashCount;
    }

    private static void dash(Player player) {
        Vec3 lookingIn = player.getLookAngle();
        Vec3 strafeVec = lookingIn.yRot((float) Math.PI * 0.5F);
        int forward = (int) Math.signum(player.zza);
        int strafe = (int) Math.signum(player.xxa);

        if (forward == 0 && strafe == 0) {
            forward = 1;
        }

        player.push(lookingIn.x * forward + strafeVec.x * strafe, 0.0D,
                lookingIn.z * forward + strafeVec.z * strafe);
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, 0.0D, motion.z);
        player.fallDistance = 0.0F;
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.WEAPON_ROCKET_FLAME.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void handleFauxLadder(Player player) {
        if (!isOnLadder(player)) {
            return;
        }

        double climbSpeed = 0.15D;
        Vec3 motion = player.getDeltaMovement();
        double x = Mth.clamp(motion.x, -climbSpeed, climbSpeed);
        double y = motion.y;
        double z = Mth.clamp(motion.z, -climbSpeed, climbSpeed);

        if (y < -climbSpeed) {
            y = -climbSpeed;
        }
        if (player.isShiftKeyDown() && y < 0.0D) {
            y = 0.0D;
        }
        if (player.horizontalCollision) {
            y = 0.2D;
        }

        player.setDeltaMovement(x, y, z);
        player.fallDistance = 0.0F;
        setOnLadder(player, false);
    }

    public static void toggleHud(ServerPlayer player) {
        setHudEnabledWithNotice(player, !isHudEnabled(player));
    }

    public static void toggleBackpack(ServerPlayer player) {
        setBackpackEnabledWithNotice(player, !isBackpackEnabled(player));
    }

    public static void toggleMagnet(ServerPlayer player) {
        setMagnetEnabledWithNotice(player, !isMagnetEnabled(player));
    }

    public static void sync(ServerPlayer player) {
        ModMessages.syncPlayerProperties(player, DATA_TYPE, writeSyncedData(player));
    }

    public static CompoundTag writeSyncedData(Player player) {
        CompoundTag data = new CompoundTag();
        data.putBoolean(KEY_HAS_RECEIVED_BOOK, hasReceivedBook(player));
        data.putFloat(KEY_SHIELD, getShield(player));
        data.putFloat(KEY_MAX_SHIELD, getMaxShield(player));
        data.putBoolean(KEY_ENABLE_BACKPACK, isBackpackEnabled(player));
        data.putBoolean(KEY_ENABLE_MAGNET, isMagnetEnabled(player));
        data.putBoolean(KEY_ENABLE_HUD, isHudEnabled(player));
        data.putInt(KEY_REPUTATION, getReputation(player));
        data.putBoolean(KEY_IS_ON_LADDER, isOnLadder(player));
        data.putInt(KEY_DASH_COUNT, getDashCount(player));
        data.putInt(KEY_STAMINA, getStamina(player));
        data.putInt(KEY_DASH_COOLDOWN, getDashCooldown(player));
        return data;
    }

    public static void copyForRespawn(Player original, Player replacement) {
        CompoundTag copy = new CompoundTag();
        copy.putBoolean(KEY_HAS_RECEIVED_BOOK, hasReceivedBook(original));
        copy.putFloat(KEY_SHIELD, getShield(original));
        copy.putFloat(KEY_MAX_SHIELD, getMaxShield(original));
        copy.putBoolean(KEY_ENABLE_BACKPACK, isBackpackEnabled(original));
        copy.putBoolean(KEY_ENABLE_HUD, isHudEnabled(original));
        copy.putInt(KEY_REPUTATION, getReputation(original));
        copy.putBoolean(KEY_IS_ON_LADDER, isOnLadder(original));
        copy.putBoolean(KEY_ENABLE_MAGNET, isMagnetEnabled(original));
        replacement.getPersistentData().put(TAG_ROOT, copy);
    }

    private static void handleRisingEdge(ServerPlayer player, HbmKeybind keybind) {
        if (keybind == HbmKeybind.TOGGLE_JETPACK) {
            toggleBackpack(player);
        } else if (keybind == HbmKeybind.TOGGLE_MAGNET) {
            toggleMagnet(player);
        } else if (keybind == HbmKeybind.TOGGLE_HEAD) {
            toggleHud(player);
        }
    }

    private static void setBackpackEnabledWithNotice(ServerPlayer player, boolean enabled) {
        setBackpackEnabled(player, enabled);
        ModMessages.informPlayer(player, toggleMessage("Jetpack", enabled), NOTICE_ID_JETPACK, NOTICE_MILLIS);
    }

    private static void setMagnetEnabledWithNotice(ServerPlayer player, boolean enabled) {
        setMagnetEnabled(player, enabled);
        ModMessages.informPlayer(player, toggleMessage("Magnet", enabled), NOTICE_ID_MAGNET, NOTICE_MILLIS);
    }

    private static void setHudEnabledWithNotice(ServerPlayer player, boolean enabled) {
        setHudEnabled(player, enabled);
        ModMessages.informPlayer(player, toggleMessage("HUD", enabled), NOTICE_ID_HUD, NOTICE_MILLIS);
    }

    private static Component toggleMessage(String label, boolean enabled) {
        return Component.literal(label + (enabled ? " ON" : " OFF"))
                .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED);
    }

    private static boolean getDefaultTrueBoolean(Player player, String key) {
        if (player == null) {
            return true;
        }
        CompoundTag root = getTag(player);
        return !root.contains(key) || root.getBoolean(key);
    }

    private static void setBoolean(Player player, String key, boolean value) {
        CompoundTag root = getTag(player);
        if (root.contains(key) && root.getBoolean(key) == value) {
            return;
        }
        root.putBoolean(key, value);
        syncIfServer(player);
    }

    private static void setFloat(Player player, String key, float value) {
        CompoundTag root = getTag(player);
        if (root.contains(key) && Float.compare(root.getFloat(key), value) == 0) {
            return;
        }
        root.putFloat(key, value);
        syncIfServer(player);
    }

    private static void setInt(Player player, String key, int value) {
        CompoundTag root = getTag(player);
        if (root.contains(key) && root.getInt(key) == value) {
            return;
        }
        root.putInt(key, value);
        syncIfServer(player);
    }

    private static void syncIfServer(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            sync(serverPlayer);
        }
    }

    private static void syncRuntimeIfChanged(Player player, RuntimeData data) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (data.lastSyncedDashCount == data.totalDashCount
                && data.lastSyncedStamina == data.stamina
                && data.lastSyncedDashCooldown == data.dashCooldown) {
            return;
        }
        data.lastSyncedDashCount = data.totalDashCount;
        data.lastSyncedStamina = data.stamina;
        data.lastSyncedDashCooldown = data.dashCooldown;
        sync(serverPlayer);
    }

    private static CompoundTag getTag(Player player) {
        if (player == null) {
            return new CompoundTag();
        }
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(TAG_ROOT, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            CompoundTag tag = persistentData.contains(TAG_PREVIOUS_ROOT, net.minecraft.nbt.Tag.TAG_COMPOUND)
                    ? persistentData.getCompound(TAG_PREVIOUS_ROOT).copy()
                    : new CompoundTag();
            persistentData.put(TAG_ROOT, tag);
            persistentData.remove(TAG_PREVIOUS_ROOT);
        }
        CompoundTag tag = persistentData.getCompound(TAG_ROOT);
        applyDefaults(tag);
        return tag;
    }

    private static void applyDefaults(CompoundTag tag) {
        if (!tag.contains(KEY_ENABLE_BACKPACK)) {
            tag.putBoolean(KEY_ENABLE_BACKPACK, true);
        }
        if (!tag.contains(KEY_ENABLE_MAGNET)) {
            tag.putBoolean(KEY_ENABLE_MAGNET, true);
        }
        if (!tag.contains(KEY_ENABLE_HUD)) {
            tag.putBoolean(KEY_ENABLE_HUD, true);
        }
    }

    private static RuntimeData runtime(Player player) {
        if (player == null) {
            return new RuntimeData();
        }
        return RUNTIME_DATA.computeIfAbsent(player.getUUID(), ignored -> new RuntimeData());
    }

    private static final class RuntimeData {
        private boolean dashActivated = true;
        private int dashCooldown;
        private int totalDashCount;
        private int stamina;
        private int plinkCooldown;
        private int lastDamage;
        private int grenadeDeployment;
        private int lastSyncedDashCount = -1;
        private int lastSyncedStamina = -1;
        private int lastSyncedDashCooldown = -1;
    }

    private HbmPlayerProperties() {
    }
}
