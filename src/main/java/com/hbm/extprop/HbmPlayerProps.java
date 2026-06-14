package com.hbm.extprop;

import com.hbm.handler.HbmKeybinds;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.ntm.network.HbmKeybind;
import com.hbm.ntm.player.HbmPlayerProperties;
import com.hbm.ntm.sound.LegacySoundPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Legacy package facade for source migrations. Real state is owned by
 * {@link HbmPlayerProperties}; this class must not grow a parallel store.
 */
public final class HbmPlayerProps {
    public static final String key = HbmPlayerProperties.key;
    public static final int dashCooldownLength = HbmPlayerProperties.dashCooldownLength;
    public static final int plinkCooldownLength = HbmPlayerProperties.plinkCooldownLength;
    public static final float shieldCap = HbmPlayerProperties.shieldCap;

    private final Player player;

    private HbmPlayerProps(Player player) {
        this.player = player;
    }

    public static HbmPlayerProps registerData(Player player) {
        return getData(player);
    }

    public static HbmPlayerProps getData(Player player) {
        return new HbmPlayerProps(player);
    }

    public Player player() {
        return player;
    }

    public static boolean hasReceivedBook(Player player) {
        return HbmPlayerProperties.hasReceivedBook(player);
    }

    public static void setHasReceivedBook(Player player, boolean received) {
        HbmPlayerProperties.setHasReceivedBook(player, received);
    }

    public static boolean isHudEnabled(Player player) {
        return HbmPlayerProperties.isHudEnabled(player);
    }

    public static void setHudEnabled(Player player, boolean enabled) {
        HbmPlayerProperties.setHudEnabled(player, enabled);
    }

    public static boolean isBackpackEnabled(Player player) {
        return HbmPlayerProperties.isBackpackEnabled(player);
    }

    public static void setBackpackEnabled(Player player, boolean enabled) {
        HbmPlayerProperties.setBackpackEnabled(player, enabled);
    }

    public static boolean isMagnetActive(Player player) {
        return HbmPlayerProperties.isMagnetActive(player);
    }

    public static boolean isMagnetEnabled(Player player) {
        return HbmPlayerProperties.isMagnetEnabled(player);
    }

    public static void setMagnetEnabled(Player player, boolean enabled) {
        HbmPlayerProperties.setMagnetEnabled(player, enabled);
    }

    public static boolean getKeyPressed(Player player, HbmKeybind key) {
        return HbmPlayerProperties.getKeyPressed(player, key);
    }

    public static boolean getKeyPressed(Player player, EnumKeybind key) {
        return getKeyPressed(player, HbmKeybinds.toModern(key));
    }

    public static boolean isToolAltPressed(Player player) {
        return HbmPlayerProperties.isToolAltPressed(player);
    }

    public static boolean isToolCtrlPressed(Player player) {
        return HbmPlayerProperties.isToolCtrlPressed(player);
    }

    public static boolean isAbilityCyclePressed(Player player) {
        return HbmPlayerProperties.isAbilityCyclePressed(player);
    }

    public static boolean isAbilityAltPressed(Player player) {
        return HbmPlayerProperties.isAbilityAltPressed(player);
    }

    public static boolean isGunPrimaryPressed(Player player) {
        return HbmPlayerProperties.isGunPrimaryPressed(player);
    }

    public static boolean isGunSecondaryPressed(Player player) {
        return HbmPlayerProperties.isGunSecondaryPressed(player);
    }

    public static boolean isGunTertiaryPressed(Player player) {
        return HbmPlayerProperties.isGunTertiaryPressed(player);
    }

    public static boolean isReloadPressed(Player player) {
        return HbmPlayerProperties.isReloadPressed(player);
    }

    public static boolean isCraneUpPressed(Player player) {
        return HbmPlayerProperties.isCraneUpPressed(player);
    }

    public static boolean isCraneDownPressed(Player player) {
        return HbmPlayerProperties.isCraneDownPressed(player);
    }

    public static boolean isCraneLeftPressed(Player player) {
        return HbmPlayerProperties.isCraneLeftPressed(player);
    }

    public static boolean isCraneRightPressed(Player player) {
        return HbmPlayerProperties.isCraneRightPressed(player);
    }

    public static boolean isCraneLoadPressed(Player player) {
        return HbmPlayerProperties.isCraneLoadPressed(player);
    }

    public static void setKeyPressed(Player player, HbmKeybind key, boolean pressed) {
        if (player instanceof ServerPlayer serverPlayer) {
            HbmPlayerProperties.setKeyPressed(serverPlayer, key, pressed);
        }
    }

    public static void setKeyPressed(Player player, EnumKeybind key, boolean pressed) {
        setKeyPressed(player, HbmKeybinds.toModern(key), pressed);
    }

    public static boolean isJetpackActive(Player player) {
        return HbmPlayerProperties.isJetpackActive(player);
    }

    public static boolean isDashActivated(Player player) {
        return HbmPlayerProperties.isDashActivated(player);
    }

    public static void setDashActivated(Player player, boolean activated) {
        HbmPlayerProperties.setDashActivated(player, activated);
    }

    public static int getDashCooldown(Player player) {
        return HbmPlayerProperties.getDashCooldown(player);
    }

    public static void setDashCooldown(Player player, int cooldown) {
        HbmPlayerProperties.setDashCooldown(player, cooldown);
    }

    public static int getStamina(Player player) {
        return HbmPlayerProperties.getStamina(player);
    }

    public static void setStamina(Player player, int stamina) {
        HbmPlayerProperties.setStamina(player, stamina);
    }

    public static int getDashCount(Player player) {
        return HbmPlayerProperties.getDashCount(player);
    }

    public static void setDashCount(Player player, int count) {
        HbmPlayerProperties.setDashCount(player, count);
    }

    public static int getPlinkCooldown(Player player) {
        return HbmPlayerProperties.getPlinkCooldown(player);
    }

    public static void setPlinkCooldown(Player player, int cooldown) {
        HbmPlayerProperties.setPlinkCooldown(player, cooldown);
    }

    public static void plink(Player player, String sound, float volume, float pitch) {
        if (player == null || HbmPlayerProperties.getPlinkCooldown(player) > 0) {
            return;
        }
        LegacySoundPlayer.playSoundAtPlayer(player, sound, volume, pitch);
        HbmPlayerProperties.setPlinkCooldown(player, HbmPlayerProperties.PLINK_COOLDOWN_LENGTH);
    }

    public static float getShield(Player player) {
        return HbmPlayerProperties.getShield(player);
    }

    public static void setShield(Player player, float shield) {
        HbmPlayerProperties.setShield(player, shield);
    }

    public static boolean canApplyShieldInfusion(Player player) {
        return HbmPlayerProperties.canApplyShieldInfusion(player);
    }

    public static boolean applyShieldInfusion(Player player, float infusion) {
        return HbmPlayerProperties.applyShieldInfusion(player, infusion);
    }

    public static float addShield(Player player, float amount) {
        return HbmPlayerProperties.addShield(player, amount);
    }

    public static float ensureShield(Player player, float minimum) {
        return HbmPlayerProperties.ensureShield(player, minimum);
    }

    public static float clampShieldToEffectiveMax(Player player) {
        return HbmPlayerProperties.clampShieldToEffectiveMax(player);
    }

    public static float fillShield(Player player) {
        return HbmPlayerProperties.fillShield(player);
    }

    public static float addMaxShield(Player player, float amount) {
        return HbmPlayerProperties.addMaxShield(player, amount);
    }

    public static float addMaxShieldCapped(Player player, float amount) {
        return HbmPlayerProperties.addMaxShieldCapped(player, amount);
    }

    public static float getMaxShield(Player player) {
        return HbmPlayerProperties.getMaxShield(player);
    }

    public static void setMaxShield(Player player, float maxShield) {
        HbmPlayerProperties.setMaxShield(player, maxShield);
    }

    public static float getEffectiveMaxShield(Player player) {
        return HbmPlayerProperties.getEffectiveMaxShield(player);
    }

    public static int getLastDamage(Player player) {
        return HbmPlayerProperties.getLastDamage(player);
    }

    public static void setLastDamage(Player player, int lastDamage) {
        HbmPlayerProperties.setLastDamage(player, lastDamage);
    }

    public static int getReputation(Player player) {
        return HbmPlayerProperties.getReputation(player);
    }

    public static void setReputation(Player player, int reputation) {
        HbmPlayerProperties.setReputation(player, reputation);
    }

    public static int addReputation(Player player, int amount) {
        return HbmPlayerProperties.addReputation(player, amount);
    }

    public static int incrementReputation(Player player) {
        return HbmPlayerProperties.incrementReputation(player);
    }

    public static int decrementReputation(Player player) {
        return HbmPlayerProperties.decrementReputation(player);
    }

    public static boolean decrementReputationAbove(Player player, int floor) {
        return HbmPlayerProperties.decrementReputationAbove(player, floor);
    }

    public static boolean hasReputationAtLeast(Player player, int threshold) {
        return HbmPlayerProperties.hasReputationAtLeast(player, threshold);
    }

    public static boolean hasReputationAtMost(Player player, int threshold) {
        return HbmPlayerProperties.hasReputationAtMost(player, threshold);
    }

    public static boolean isOnLadder(Player player) {
        return HbmPlayerProperties.isOnLadder(player);
    }

    public static void setOnLadder(Player player, boolean onLadder) {
        HbmPlayerProperties.setOnLadder(player, onLadder);
    }

    public static void markOnLadder(Player player) {
        HbmPlayerProperties.markOnLadder(player);
    }

    public static int getGrenadeDeployment(Player player) {
        return HbmPlayerProperties.getGrenadeDeployment(player);
    }

    public static void setGrenadeDeployment(Player player, int deployment) {
        HbmPlayerProperties.setGrenadeDeployment(player, deployment);
    }

    public static void resetGrenadeDeployment(Player player) {
        HbmPlayerProperties.resetGrenadeDeployment(player);
    }

    public static int incrementGrenadeDeployment(Player player) {
        return HbmPlayerProperties.incrementGrenadeDeployment(player);
    }

    public static boolean isGrenadeDeploymentReady(Player player, int drawDuration) {
        return HbmPlayerProperties.isGrenadeDeploymentReady(player, drawDuration);
    }

    public static void clearKeyStates(Player player) {
        HbmPlayerProperties.clearKeyStates(player);
    }

    public static void clearKeyState(Player player, HbmKeybind key) {
        HbmPlayerProperties.clearKeyState(player, key);
    }

    public static void clearKeyState(Player player, EnumKeybind key) {
        clearKeyState(player, HbmKeybinds.toModern(key));
    }

    public static void clearMovementKeyStates(ServerPlayer player) {
        HbmPlayerProperties.clearMovementKeyStates(player);
    }

    public static void clearMovementKeyStates(Player player) {
        HbmPlayerProperties.clearMovementKeyStates(player);
    }

    public static void clearRuntime(Player player) {
        HbmPlayerProperties.clearRuntime(player);
    }

    public static void copyForRespawn(Player original, Player replacement) {
        HbmPlayerProperties.copyForRespawn(original, replacement);
    }

    public static CompoundTag writePersistentData(Player player) {
        return HbmPlayerProperties.writePersistentData(player);
    }

    public static void readPersistentData(Player player, CompoundTag data) {
        HbmPlayerProperties.readPersistentData(player, data);
    }

    public static void serialize(Player player, FriendlyByteBuf buffer) {
        HbmPlayerProperties.serializeLegacySyncedData(player, buffer);
    }

    public static void deserialize(Player player, FriendlyByteBuf buffer) {
        HbmPlayerProperties.deserializeLegacySyncedData(player, buffer);
    }

    public static void saveNBTData(Player player, CompoundTag nbt) {
        HbmPlayerProperties.saveNBTData(player, nbt);
    }

    public static void loadNBTData(Player player, CompoundTag nbt) {
        HbmPlayerProperties.loadNBTData(player, nbt);
    }

    public boolean hasReceivedBook() {
        return HbmPlayerProperties.hasReceivedBook(player);
    }

    public void setHasReceivedBook(boolean received) {
        HbmPlayerProperties.setHasReceivedBook(player, received);
    }

    public boolean isHudEnabled() {
        return HbmPlayerProperties.isHudEnabled(player);
    }

    public void setHudEnabled(boolean enabled) {
        HbmPlayerProperties.setHudEnabled(player, enabled);
    }

    public boolean isBackpackEnabled() {
        return HbmPlayerProperties.isBackpackEnabled(player);
    }

    public void setBackpackEnabled(boolean enabled) {
        HbmPlayerProperties.setBackpackEnabled(player, enabled);
    }

    public boolean isMagnetActive() {
        return HbmPlayerProperties.isMagnetActive(player);
    }

    public boolean isMagnetEnabled() {
        return HbmPlayerProperties.isMagnetEnabled(player);
    }

    public void setMagnetEnabled(boolean enabled) {
        HbmPlayerProperties.setMagnetEnabled(player, enabled);
    }

    public boolean getKeyPressed(HbmKeybind key) {
        return HbmPlayerProperties.getKeyPressed(player, key);
    }

    public boolean getKeyPressed(EnumKeybind key) {
        return getKeyPressed(HbmKeybinds.toModern(key));
    }

    public boolean isToolAltPressed() {
        return HbmPlayerProperties.isToolAltPressed(player);
    }

    public boolean isToolCtrlPressed() {
        return HbmPlayerProperties.isToolCtrlPressed(player);
    }

    public boolean isAbilityCyclePressed() {
        return HbmPlayerProperties.isAbilityCyclePressed(player);
    }

    public boolean isAbilityAltPressed() {
        return HbmPlayerProperties.isAbilityAltPressed(player);
    }

    public boolean isGunPrimaryPressed() {
        return HbmPlayerProperties.isGunPrimaryPressed(player);
    }

    public boolean isGunSecondaryPressed() {
        return HbmPlayerProperties.isGunSecondaryPressed(player);
    }

    public boolean isGunTertiaryPressed() {
        return HbmPlayerProperties.isGunTertiaryPressed(player);
    }

    public boolean isReloadPressed() {
        return HbmPlayerProperties.isReloadPressed(player);
    }

    public boolean isCraneUpPressed() {
        return HbmPlayerProperties.isCraneUpPressed(player);
    }

    public boolean isCraneDownPressed() {
        return HbmPlayerProperties.isCraneDownPressed(player);
    }

    public boolean isCraneLeftPressed() {
        return HbmPlayerProperties.isCraneLeftPressed(player);
    }

    public boolean isCraneRightPressed() {
        return HbmPlayerProperties.isCraneRightPressed(player);
    }

    public boolean isCraneLoadPressed() {
        return HbmPlayerProperties.isCraneLoadPressed(player);
    }

    public void setKeyPressed(HbmKeybind key, boolean pressed) {
        if (player instanceof ServerPlayer serverPlayer) {
            HbmPlayerProperties.setKeyPressed(serverPlayer, key, pressed);
        }
    }

    public void setKeyPressed(EnumKeybind key, boolean pressed) {
        setKeyPressed(HbmKeybinds.toModern(key), pressed);
    }

    public boolean isJetpackActive() {
        return HbmPlayerProperties.isJetpackActive(player);
    }

    public boolean isDashActivated() {
        return HbmPlayerProperties.isDashActivated(player);
    }

    public void setDashActivated(boolean activated) {
        HbmPlayerProperties.setDashActivated(player, activated);
    }

    public int getDashCooldown() {
        return HbmPlayerProperties.getDashCooldown(player);
    }

    public void setDashCooldown(int cooldown) {
        HbmPlayerProperties.setDashCooldown(player, cooldown);
    }

    public int getStamina() {
        return HbmPlayerProperties.getStamina(player);
    }

    public void setStamina(int stamina) {
        HbmPlayerProperties.setStamina(player, stamina);
    }

    public int getDashCount() {
        return HbmPlayerProperties.getDashCount(player);
    }

    public void setDashCount(int count) {
        HbmPlayerProperties.setDashCount(player, count);
    }

    public int getPlinkCooldown() {
        return HbmPlayerProperties.getPlinkCooldown(player);
    }

    public void setPlinkCooldown(int cooldown) {
        HbmPlayerProperties.setPlinkCooldown(player, cooldown);
    }

    public float getShield() {
        return HbmPlayerProperties.getShield(player);
    }

    public void setShield(float shield) {
        HbmPlayerProperties.setShield(player, shield);
    }

    public float addShield(float amount) {
        return HbmPlayerProperties.addShield(player, amount);
    }

    public float ensureShield(float minimum) {
        return HbmPlayerProperties.ensureShield(player, minimum);
    }

    public float clampShieldToEffectiveMax() {
        return HbmPlayerProperties.clampShieldToEffectiveMax(player);
    }

    public float fillShield() {
        return HbmPlayerProperties.fillShield(player);
    }

    public float getMaxShield() {
        return HbmPlayerProperties.getMaxShield(player);
    }

    public void setMaxShield(float maxShield) {
        HbmPlayerProperties.setMaxShield(player, maxShield);
    }

    public float addMaxShield(float amount) {
        return HbmPlayerProperties.addMaxShield(player, amount);
    }

    public float addMaxShieldCapped(float amount) {
        return HbmPlayerProperties.addMaxShieldCapped(player, amount);
    }

    public float getEffectiveMaxShield() {
        return HbmPlayerProperties.getEffectiveMaxShield(player);
    }

    public int getLastDamage() {
        return HbmPlayerProperties.getLastDamage(player);
    }

    public void setLastDamage(int lastDamage) {
        HbmPlayerProperties.setLastDamage(player, lastDamage);
    }

    public int getReputation() {
        return HbmPlayerProperties.getReputation(player);
    }

    public void setReputation(int reputation) {
        HbmPlayerProperties.setReputation(player, reputation);
    }

    public int addReputation(int amount) {
        return HbmPlayerProperties.addReputation(player, amount);
    }

    public int incrementReputation() {
        return HbmPlayerProperties.incrementReputation(player);
    }

    public int decrementReputation() {
        return HbmPlayerProperties.decrementReputation(player);
    }

    public boolean decrementReputationAbove(int floor) {
        return HbmPlayerProperties.decrementReputationAbove(player, floor);
    }

    public boolean hasReputationAtLeast(int threshold) {
        return HbmPlayerProperties.hasReputationAtLeast(player, threshold);
    }

    public boolean hasReputationAtMost(int threshold) {
        return HbmPlayerProperties.hasReputationAtMost(player, threshold);
    }

    public boolean isOnLadder() {
        return HbmPlayerProperties.isOnLadder(player);
    }

    public void setOnLadder(boolean onLadder) {
        HbmPlayerProperties.setOnLadder(player, onLadder);
    }

    public void markOnLadder() {
        HbmPlayerProperties.markOnLadder(player);
    }

    public int getGrenadeDeployment() {
        return HbmPlayerProperties.getGrenadeDeployment(player);
    }

    public void setGrenadeDeployment(int deployment) {
        HbmPlayerProperties.setGrenadeDeployment(player, deployment);
    }

    public void resetGrenadeDeployment() {
        HbmPlayerProperties.resetGrenadeDeployment(player);
    }

    public int incrementGrenadeDeployment() {
        return HbmPlayerProperties.incrementGrenadeDeployment(player);
    }

    public boolean isGrenadeDeploymentReady(int drawDuration) {
        return HbmPlayerProperties.isGrenadeDeploymentReady(player, drawDuration);
    }

    public void clearKeyStates() {
        HbmPlayerProperties.clearKeyStates(player);
    }

    public void clearKeyState(HbmKeybind key) {
        HbmPlayerProperties.clearKeyState(player, key);
    }

    public void clearKeyState(EnumKeybind key) {
        clearKeyState(HbmKeybinds.toModern(key));
    }

    public void clearRuntime() {
        HbmPlayerProperties.clearRuntime(player);
    }

    public CompoundTag writePersistentData() {
        return HbmPlayerProperties.writePersistentData(player);
    }

    public void readPersistentData(CompoundTag data) {
        HbmPlayerProperties.readPersistentData(player, data);
    }

    public void serialize(FriendlyByteBuf buffer) {
        HbmPlayerProperties.serializeLegacySyncedData(player, buffer);
    }

    public void deserialize(FriendlyByteBuf buffer) {
        HbmPlayerProperties.deserializeLegacySyncedData(player, buffer);
    }

    public void saveNBTData(CompoundTag nbt) {
        HbmPlayerProperties.saveNBTData(player, nbt);
    }

    public void loadNBTData(CompoundTag nbt) {
        HbmPlayerProperties.loadNBTData(player, nbt);
    }
}
