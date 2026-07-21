package com.hbm.ntm.client;

import com.hbm.ntm.bullet.SednaWeaponModEvaluator;
import com.hbm.ntm.item.SednaGunItem;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Direct client-side equivalent of ItemGunBaseNT.prevAimingProgress/aimingProgress. */
public final class LegacySednaAimProgress {
    private static float previous;
    private static float current;

    private LegacySednaAimProgress() {
    }

    /** Mirrors the held-gun client branch in ItemGunBaseNT#onUpdate. */
    public static void tick(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SednaGunItem gun)) {
            return;
        }
        previous = current;
        current += gun.legacyIsAiming(stack) ? 0.25F : -0.25F;
        current = Mth.clamp(current, 0.0F, 1.0F);
    }

    public static float previous() {
        return previous;
    }

    public static float current() {
        return current;
    }

    /** Exact ItemRenderWeaponBase interpolation expression. */
    public static float interpolated(float partialTick) {
        return previous + (current - previous) * partialTick;
    }

    public static boolean fullyAimed() {
        return current >= 1.0F;
    }

    /** Exact legacy full-aim renderer predicate: previous and current tick values are both one. */
    public static boolean settledFullyAimed() {
        return previous >= 1.0F && current >= 1.0F;
    }

    /**
     * Exact legacy ItemRenderWeaponBase#getViewFOV dispatch, as called by
     * ModEventHandlerClient#setupFOV. That event did not consult ClientConfig.GUN_MODEL_FOV:
     * the setting only selected the separately-rendered weapon model's projection. The caller
     * supplies the local player's main-hand stack only.
     */
    public static double applyLegacyViewFov(ItemStack stack, double fov, float partialTick) {
        if (!(stack.getItem() instanceof SednaGunItem gun)) {
            return fov;
        }
        return fov * (1.0D - interpolated(partialTick) * legacyFovReduction(stack, gun.gunConfig().legacyName()));
    }

    private static float legacyFovReduction(ItemStack stack, String legacyName) {
        return switch (legacyName) {
            case "gun_stinger" -> 0.5F;
            case "gun_stg77" -> 0.66F;
            case "gun_amat", "gun_amat_subtlety", "gun_amat_penance" -> 0.8F;
            case "gun_lasrifle" -> SednaWeaponModEvaluator.hasUpgrade(stack, 0,
                    SednaWeaponModEvaluator.ID_LAS_AUTO) ? 0.66F : 0.75F;
            case "gun_carbine", "gun_mas36", "gun_charge_thrower" ->
                    scoped(stack) ? 0.66F : 0.33F;
            case "gun_g3" -> scoped(stack) ? 0.66F : 0.33F;
            case "gun_g3_zebra", "gun_heavy_revolver_lilmac" -> 0.66F;
            case "gun_heavy_revolver", "gun_heavy_revolver_protege" ->
                    scoped(stack) ? 0.66F : 0.33F;
            case "gun_congolake", "gun_greasegun",
                    "gun_laser_pistol", "gun_laser_pistol_pew_pew", "gun_laser_pistol_morning_glory",
                    "gun_folly", "gun_flamer", "gun_flamer_topaz", "gun_flamer_daybreaker", "gun_flaregun",
                    "gun_light_revolver", "gun_light_revolver_atlas", "gun_maresleg", "gun_maresleg_broken",
                    "gun_coilgun", "gun_lag", "gun_m2", "gun_bolter", "gun_hangman", "gun_aberrator_eott",
                    "gun_fatman", "gun_henry", "gun_henry_lincoln", "gun_liberator", "gun_double_barrel",
                    "gun_double_barrel_sacred_dragon", "gun_am180", "gun_minigun", "gun_minigun_lacunae",
                    "gun_minigun_dual", "gun_pepperbox", "gun_n_i_4_n_i", "gun_mk108", "gun_panzerschreck",
                    "gun_aberrator", "gun_autoshotgun_sexy", "gun_autoshotgun_heretic", "gun_star_f",
                    "gun_star_f_akimbo", "gun_autoshotgun", "gun_autoshotgun_shredder", "gun_spas12",
                    "gun_tesla_cannon", "gun_uzi", "gun_uzi_akimbo" -> 0.33F;
            default -> 0.0F;
        };
    }

    private static boolean scoped(ItemStack stack) {
        return SednaWeaponModEvaluator.hasUpgrade(stack, 0, SednaWeaponModEvaluator.ID_SCOPE);
    }
}
