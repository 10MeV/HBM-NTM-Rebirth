package com.hbm.ntm.player;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.armor.ArmorModItems;
import com.hbm.ntm.api.item.ArmorDashProvider;
import com.hbm.ntm.item.FsbArmorItem;
import com.hbm.ntm.network.ModMessages;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
    @Deprecated(forRemoval = false)
    public static final String key = "NTM_EXT_PLAYER";
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
    @Deprecated(forRemoval = false)
    public static final int dashCooldownLength = DASH_COOLDOWN_LENGTH;
    @Deprecated(forRemoval = false)
    public static final int plinkCooldownLength = PLINK_COOLDOWN_LENGTH;
    @Deprecated(forRemoval = false)
    public static final float shieldCap = SHIELD_CAP;

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

    public static float addShield(Player player, float amount) {
        float shield = Math.max(0.0F, getShield(player) + amount);
        setShield(player, shield);
        return shield;
    }

    public static float ensureShield(Player player, float minimum) {
        float target = Math.max(0.0F, minimum);
        if (getShield(player) < target) {
            setShield(player, target);
        }
        return getShield(player);
    }

    public static float clampShieldToEffectiveMax(Player player) {
        float effectiveMax = getEffectiveMaxShield(player);
        if (getShield(player) > effectiveMax) {
            setShield(player, effectiveMax);
        }
        return getShield(player);
    }

    public static float fillShield(Player player) {
        float effectiveMax = getEffectiveMaxShield(player);
        setShield(player, effectiveMax);
        return effectiveMax;
    }

    public static float getMaxShield(Player player) {
        return getTag(player).getFloat(KEY_MAX_SHIELD);
    }

    public static void setMaxShield(Player player, float maxShield) {
        setFloat(player, KEY_MAX_SHIELD, Math.max(0.0F, maxShield));
    }

    public static float addMaxShield(Player player, float amount) {
        float maxShield = Math.max(0.0F, getMaxShield(player) + amount);
        setMaxShield(player, maxShield);
        return maxShield;
    }

    public static float addMaxShieldCapped(Player player, float amount) {
        float maxShield = Mth.clamp(getMaxShield(player) + amount, 0.0F, SHIELD_CAP);
        setMaxShield(player, maxShield);
        return maxShield;
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

    public static void setBackpackEnabled(Player player, boolean enabled) {
        setBoolean(player, KEY_ENABLE_BACKPACK, enabled);
    }

    public static boolean isMagnetEnabled(Player player) {
        return getDefaultTrueBoolean(player, KEY_ENABLE_MAGNET);
    }

    public static boolean isMagnetActive(Player player) {
        return isMagnetEnabled(player);
    }

    public static void setMagnetEnabled(Player player, boolean enabled) {
        setBoolean(player, KEY_ENABLE_MAGNET, enabled);
    }

    public static boolean isHudEnabled(Player player) {
        return getDefaultTrueBoolean(player, KEY_ENABLE_HUD);
    }

    public static void setHudEnabled(Player player, boolean enabled) {
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

    public static int incrementReputation(Player player) {
        return addReputation(player, 1);
    }

    public static int decrementReputation(Player player) {
        return addReputation(player, -1);
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

    public static void markOnLadder(Player player) {
        setOnLadder(player, true);
    }

    public static boolean getKeyPressed(Player player, HbmKeybind keybind) {
        if (player == null || keybind == null) {
            return false;
        }
        EnumSet<HbmKeybind> keys = PRESSED_KEYS.get(player.getUUID());
        return keys != null && keys.contains(keybind);
    }

    public static boolean isToolAltPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.TOOL_ALT);
    }

    public static boolean isToolCtrlPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.TOOL_CTRL);
    }

    public static boolean isAbilityCyclePressed(Player player) {
        return getKeyPressed(player, HbmKeybind.ABILITY_CYCLE);
    }

    public static boolean isAbilityAltPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.ABILITY_ALT);
    }

    public static boolean isGunPrimaryPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.GUN_PRIMARY);
    }

    public static boolean isGunSecondaryPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.GUN_SECONDARY);
    }

    public static boolean isGunTertiaryPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.GUN_TERTIARY);
    }

    public static boolean isReloadPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.RELOAD);
    }

    public static boolean isCraneUpPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.CRANE_UP);
    }

    public static boolean isCraneDownPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.CRANE_DOWN);
    }

    public static boolean isCraneLeftPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.CRANE_LEFT);
    }

    public static boolean isCraneRightPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.CRANE_RIGHT);
    }

    public static boolean isCraneLoadPressed(Player player) {
        return getKeyPressed(player, HbmKeybind.CRANE_LOAD);
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

    public static void clearKeyState(Player player, HbmKeybind keybind) {
        if (player == null || keybind == null) {
            return;
        }
        EnumSet<HbmKeybind> keys = PRESSED_KEYS.get(player.getUUID());
        if (keys == null) {
            return;
        }
        keys.remove(keybind);
        if (keys.isEmpty()) {
            PRESSED_KEYS.remove(player.getUUID());
        }
    }

    public static void clearKeyState(ServerPlayer player, HbmKeybind keybind) {
        setKeyPressed(player, keybind, false);
    }

    public static void clearMovementKeyStates(ServerPlayer player) {
        clearKeyState(player, HbmKeybind.JETPACK);
        clearKeyState(player, HbmKeybind.DASH);
    }

    public static void clearMovementKeyStates(Player player) {
        clearKeyState(player, HbmKeybind.JETPACK);
        clearKeyState(player, HbmKeybind.DASH);
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
                LegacySoundPlayer.playLegacyTechBoop(player, 1.0F,
                        1.0F + (1.0F / 12.0F) * (stamina / perDash));
                stamina++;
            }
        }

        data.stamina = stamina;
    }

    private static int getArmorModDashCount(Player player) {
        int dashCount = 0;
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof FsbArmorItem fsb && fsb.hasFullSet(player)) {
            dashCount += fsb.getDashes();
        }
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
        LegacySoundPlayer.playLegacyRocketFlame(player, 1.0F, 1.0F);
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
        ModMessages.syncPlayerProperties(player);
    }

    public static CompoundTag writeSyncedData(Player player) {
        return writePlayerSyncData(player).toTag();
    }

    public static CompoundTag writeSyncedDataTag(Player player) {
        return writeSyncedData(player);
    }

    public static SyncData writePlayerSyncData(Player player) {
        return new SyncData(
                hasReceivedBook(player),
                getShield(player),
                getMaxShield(player),
                isBackpackEnabled(player),
                isMagnetEnabled(player),
                isHudEnabled(player),
                getReputation(player),
                isOnLadder(player),
                getDashCount(player),
                getStamina(player),
                getDashCooldown(player));
    }

    public static SyncData readSyncedData(CompoundTag data) {
        CompoundTag safeData = data == null ? new CompoundTag() : data;
        return new SyncData(
                safeData.getBoolean(KEY_HAS_RECEIVED_BOOK),
                safeData.getFloat(KEY_SHIELD),
                safeData.getFloat(KEY_MAX_SHIELD),
                getDefaultTrueBoolean(safeData, KEY_ENABLE_BACKPACK),
                getDefaultTrueBoolean(safeData, KEY_ENABLE_MAGNET),
                getDefaultTrueBoolean(safeData, KEY_ENABLE_HUD),
                safeData.getInt(KEY_REPUTATION),
                safeData.getBoolean(KEY_IS_ON_LADDER),
                safeData.getInt(KEY_DASH_COUNT),
                safeData.getInt(KEY_STAMINA),
                safeData.getInt(KEY_DASH_COOLDOWN));
    }

    public static SyncData emptySyncedData() {
        return readSyncedData(new CompoundTag());
    }

    public static void encodeSyncedData(SyncData data, FriendlyByteBuf buffer) {
        SyncData safeData = data == null ? emptySyncedData() : data;
        encodeLegacySyncedData(safeData, buffer);
        buffer.writeVarInt(safeData.dashCount());
        buffer.writeVarInt(safeData.stamina());
        buffer.writeVarInt(safeData.dashCooldown());
    }

    public static SyncData decodeSyncedData(FriendlyByteBuf buffer) {
        if (buffer == null || buffer.readableBytes() <= 0) {
            return emptySyncedData();
        }
        boolean hasReceivedBook = buffer.readBoolean();
        float shield = buffer.readFloat();
        float maxShield = buffer.readFloat();
        boolean backpackEnabled = buffer.readBoolean();
        boolean hudEnabled = buffer.readBoolean();
        int reputation = buffer.readInt();
        boolean onLadder = buffer.readBoolean();
        boolean magnetEnabled = buffer.readBoolean();
        int dashCount = buffer.readableBytes() > 0 ? buffer.readVarInt() : 0;
        int stamina = buffer.readableBytes() > 0 ? buffer.readVarInt() : 0;
        int dashCooldown = buffer.readableBytes() > 0 ? buffer.readVarInt() : 0;
        return new SyncData(hasReceivedBook, shield, maxShield, backpackEnabled, magnetEnabled,
                hudEnabled, reputation, onLadder, dashCount, stamina, dashCooldown);
    }

    public static void encodeLegacySyncedData(SyncData data, FriendlyByteBuf buffer) {
        SyncData safeData = data == null ? emptySyncedData() : data;
        buffer.writeBoolean(safeData.hasReceivedBook());
        buffer.writeFloat(safeData.shield());
        buffer.writeFloat(safeData.maxShield());
        buffer.writeBoolean(safeData.backpackEnabled());
        buffer.writeBoolean(safeData.hudEnabled());
        buffer.writeInt(safeData.reputation());
        buffer.writeBoolean(safeData.onLadder());
        buffer.writeBoolean(safeData.magnetEnabled());
    }

    public static SyncData decodeLegacySyncedData(FriendlyByteBuf buffer) {
        if (buffer == null || buffer.readableBytes() <= 0) {
            return emptySyncedData();
        }
        boolean hasReceivedBook = buffer.readBoolean();
        float shield = buffer.readFloat();
        float maxShield = buffer.readFloat();
        boolean backpackEnabled = buffer.readBoolean();
        boolean hudEnabled = buffer.readBoolean();
        int reputation = buffer.readInt();
        boolean onLadder = buffer.readBoolean();
        boolean magnetEnabled = buffer.readBoolean();
        return new SyncData(hasReceivedBook, shield, maxShield, backpackEnabled, magnetEnabled,
                hudEnabled, reputation, onLadder, 0, 0, 0);
    }

    public static void serializeSyncedData(Player player, FriendlyByteBuf buffer) {
        encodeSyncedData(writePlayerSyncData(player), buffer);
    }

    public static void deserializeSyncedData(Player player, FriendlyByteBuf buffer) {
        applySyncedData(player, decodeSyncedData(buffer));
    }

    public static void deserializeSyncedData(Player player, CompoundTag data) {
        applySyncedData(player, readSyncedData(data));
    }

    public static void serializeLegacySyncedData(Player player, FriendlyByteBuf buffer) {
        encodeLegacySyncedData(writePlayerSyncData(player), buffer);
    }

    public static void deserializeLegacySyncedData(Player player, FriendlyByteBuf buffer) {
        applyLegacySyncedData(player, decodeLegacySyncedData(buffer));
    }

    public static void applySyncedData(Player player, SyncData data) {
        if (player == null) {
            return;
        }
        SyncData safeData = data == null ? emptySyncedData() : data;
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_HAS_RECEIVED_BOOK, safeData.hasReceivedBook());
        tag.putFloat(KEY_SHIELD, safeData.shield());
        tag.putFloat(KEY_MAX_SHIELD, safeData.maxShield());
        tag.putBoolean(KEY_ENABLE_BACKPACK, safeData.backpackEnabled());
        tag.putBoolean(KEY_ENABLE_MAGNET, safeData.magnetEnabled());
        tag.putBoolean(KEY_ENABLE_HUD, safeData.hudEnabled());
        tag.putInt(KEY_REPUTATION, safeData.reputation());
        tag.putBoolean(KEY_IS_ON_LADDER, safeData.onLadder());
        readPersistentData(player, tag);

        RuntimeData runtime = runtime(player);
        runtime.totalDashCount = Math.max(0, safeData.dashCount());
        runtime.stamina = Math.max(0, safeData.stamina());
        runtime.dashCooldown = Math.max(0, safeData.dashCooldown());
    }

    public static void applyLegacySyncedData(Player player, SyncData data) {
        if (player == null) {
            return;
        }
        SyncData safeData = data == null ? emptySyncedData() : data;
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_HAS_RECEIVED_BOOK, safeData.hasReceivedBook());
        tag.putFloat(KEY_SHIELD, safeData.shield());
        tag.putFloat(KEY_MAX_SHIELD, safeData.maxShield());
        tag.putBoolean(KEY_ENABLE_BACKPACK, safeData.backpackEnabled());
        tag.putBoolean(KEY_ENABLE_MAGNET, safeData.magnetEnabled());
        tag.putBoolean(KEY_ENABLE_HUD, safeData.hudEnabled());
        tag.putInt(KEY_REPUTATION, safeData.reputation());
        tag.putBoolean(KEY_IS_ON_LADDER, safeData.onLadder());
        readPersistentData(player, tag);
    }

    public static CompoundTag writePersistentData(Player player) {
        return getTag(player).copy();
    }

    public static void readPersistentData(Player player, CompoundTag data) {
        if (player == null) {
            return;
        }
        CompoundTag tag = data == null ? new CompoundTag() : data.copy();
        applyDefaults(tag);
        player.getPersistentData().put(TAG_ROOT, tag);
        player.getPersistentData().remove(TAG_PREVIOUS_ROOT);
    }

    public static void saveNBTData(Player player, CompoundTag nbt) {
        if (nbt != null) {
            nbt.put(TAG_ROOT, writePersistentData(player));
        }
    }

    public static void loadNBTData(Player player, CompoundTag nbt) {
        if (nbt == null) {
            return;
        }
        if (nbt.contains(TAG_ROOT, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            readPersistentData(player, nbt.getCompound(TAG_ROOT));
        } else if (nbt.contains(TAG_PREVIOUS_ROOT, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            readPersistentData(player, nbt.getCompound(TAG_PREVIOUS_ROOT));
        }
    }

    private static boolean getDefaultTrueBoolean(CompoundTag data, String key) {
        return !data.contains(key) || data.getBoolean(key);
    }

    public record SyncData(boolean hasReceivedBook, float shield, float maxShield, boolean backpackEnabled,
            boolean magnetEnabled, boolean hudEnabled, int reputation, boolean onLadder,
            int dashCount, int stamina, int dashCooldown) {
        public CompoundTag toTag() {
            CompoundTag data = new CompoundTag();
            data.putBoolean(KEY_HAS_RECEIVED_BOOK, hasReceivedBook);
            data.putFloat(KEY_SHIELD, shield);
            data.putFloat(KEY_MAX_SHIELD, maxShield);
            data.putBoolean(KEY_ENABLE_BACKPACK, backpackEnabled);
            data.putBoolean(KEY_ENABLE_MAGNET, magnetEnabled);
            data.putBoolean(KEY_ENABLE_HUD, hudEnabled);
            data.putInt(KEY_REPUTATION, reputation);
            data.putBoolean(KEY_IS_ON_LADDER, onLadder);
            data.putInt(KEY_DASH_COUNT, dashCount);
            data.putInt(KEY_STAMINA, stamina);
            data.putInt(KEY_DASH_COOLDOWN, dashCooldown);
            return data;
        }
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
