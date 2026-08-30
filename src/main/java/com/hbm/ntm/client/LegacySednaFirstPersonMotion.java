package com.hbm.ntm.client;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.SednaGunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * ItemRenderWeaponBase#setupTransformsAndRender on the 1.7.10 renderer owned the
 * complete first-person model-view matrix. Forge 1.20.1 supplies a vanilla
 * hurt/view/arm matrix before RenderHandEvent, so this bridge first removes that
 * matrix and then installs the old Sedna-only one.
 */
public final class LegacySednaFirstPersonMotion {
    private static LocalPlayer owner;
    private static float previousCameraPitch;
    private static float cameraPitch;

    private LegacySednaFirstPersonMotion() {
    }

    /** Exact EntityPlayer#onLivingUpdate cameraPitch update, after the local player tick. */
    public static void tick(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            owner = null;
            previousCameraPitch = 0.0F;
            cameraPitch = 0.0F;
            return;
        }
        if (owner != player) {
            owner = player;
            previousCameraPitch = 0.0F;
            cameraPitch = 0.0F;
        }
        previousCameraPitch = cameraPitch;
        float target = (float) (Math.atan(-player.getDeltaMovement().y * 0.20000000298023224D) * 15.0D);
        if (player.onGround() || player.getHealth() <= 0.0F) {
            target = 0.0F;
        }
        cameraPitch += (target - cameraPitch) * 0.8F;
    }

    /** Applies the legacy root pose for a Sedna gun from the RenderHandEvent pose. */
    public static void apply(Minecraft minecraft, ItemStack stack, PoseStack poseStack, float partialTick) {
        if (!(stack.getItem() instanceof SednaGunItem gun)) {
            return;
        }
        applyLegacyWeaponBaseRoot(minecraft, poseStack, partialTick,
                turnMagnitude(gun.gunConfig().legacyName(), gun.legacyIsAiming(stack)),
                gun.legacyIsAiming(stack) ? 0.1F : 0.5F, 0.75F);
    }

    /**
     * Shared {@code ItemRenderWeaponBase#setupTransformsAndRender} root for a
     * legacy held item whose renderer is not a Sedna gun.  PA melee is the sole
     * current caller: its source renderer retains the base turn value and only
     * overrides the two sway parameters.
     */
    public static void applyPowerArmorMelee(Minecraft minecraft, PoseStack poseStack, float partialTick) {
        applyLegacyWeaponBaseRoot(minecraft, poseStack, partialTick, 2.75F, 2.0F, 0.5F);
    }

    private static void applyLegacyWeaponBaseRoot(Minecraft minecraft, PoseStack poseStack, float partialTick,
            float turnMagnitude, float swayMagnitude, float swayPeriod) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        // ItemInHandRenderer#renderHandsWithItems has already installed these two default arm rotations.
        float armPitch = Mth.lerp(partialTick, player.xBobO, player.xBob);
        float armYaw = Mth.lerp(partialTick, player.yBobO, player.yBob);
        float vanillaPitch = (player.getViewXRot(partialTick) - armPitch) * 0.1F;
        float vanillaYaw = (player.getViewYRot(partialTick) - armYaw) * 0.1F;
        LegacyPoseRotations.rotateYDegrees(poseStack, -vanillaYaw);
        LegacyPoseRotations.rotateXDegrees(poseStack, -vanillaPitch);

        if (minecraft.getCameraEntity() == player) {
            undoVanillaViewBob(minecraft, player, poseStack, partialTick);
            undoVanillaHurtBob(minecraft, player, poseStack, partialTick);
        }

        LegacyPoseRotations.rotateXDegrees(poseStack, (player.getXRot() - armPitch) * 0.1F * turnMagnitude);
        LegacyPoseRotations.rotateYDegrees(poseStack, (player.getYRot() - armYaw) * 0.1F * turnMagnitude);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);

        float distanceDelta = player.walkDist - player.walkDistO;
        float distanceInterp = -(player.walkDist + distanceDelta * partialTick);
        float camYaw = Mth.lerp(partialTick, player.oBob, player.bob);
        float phase = distanceInterp * (float) Math.PI * swayPeriod;
        poseStack.translate(Math.sin(phase) * camYaw * 0.5F * swayMagnitude,
                -Math.abs(Math.cos(phase) * camYaw) * swayMagnitude, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (Math.sin(phase) * camYaw * 3.0F));
        LegacyPoseRotations.rotateXDegrees(poseStack,
                (float) (Math.abs(Math.cos(phase - 0.2F) * camYaw) * 5.0F));
        LegacyPoseRotations.rotateXDegrees(poseStack, Mth.lerp(partialTick, previousCameraPitch, cameraPitch));
    }

    private static void undoVanillaViewBob(Minecraft minecraft, LocalPlayer player, PoseStack poseStack,
            float partialTick) {
        if (!minecraft.options.bobView().get()) {
            return;
        }
        float distanceDelta = player.walkDist - player.walkDistO;
        float distanceInterp = -(player.walkDist + distanceDelta * partialTick);
        float camYaw = Mth.lerp(partialTick, player.oBob, player.bob);
        float phase = distanceInterp * (float) Math.PI;
        float roll = (float) (Math.sin(phase) * camYaw * 3.0F);
        float pitch = (float) (Math.abs(Math.cos(phase - 0.2F) * camYaw) * 5.0F);
        float x = (float) (Math.sin(phase) * camYaw * 0.5F);
        float y = (float) (-Math.abs(Math.cos(phase) * camYaw));
        LegacyPoseRotations.rotateXDegrees(poseStack, -pitch);
        LegacyPoseRotations.rotateZDegrees(poseStack, -roll);
        poseStack.translate(-x, -y, 0.0D);
    }

    private static void undoVanillaHurtBob(Minecraft minecraft, LocalPlayer player, PoseStack poseStack,
            float partialTick) {
        float hurt = player.hurtTime - partialTick;
        if (hurt >= 0.0F) {
            hurt /= player.hurtDuration;
            hurt = Mth.sin(hurt * hurt * hurt * hurt * (float) Math.PI);
            float hurtDirection = player.getHurtDir();
            float tilt = (float) ((double) (-hurt) * 14.0D * minecraft.options.damageTiltStrength().get());
            LegacyPoseRotations.rotateYDegrees(poseStack, -hurtDirection);
            LegacyPoseRotations.rotateZDegrees(poseStack, -tilt);
            LegacyPoseRotations.rotateYDegrees(poseStack, hurtDirection);
        }
        if (player.isDeadOrDying()) {
            float death = Math.min(player.deathTime + partialTick, 20.0F);
            LegacyPoseRotations.rotateZDegrees(poseStack, -(40.0F - 8000.0F / (death + 200.0F)));
        }
    }

    /** Complete per-renderer ItemRenderWeaponBase#getTurnMagnitude table. */
    private static float turnMagnitude(String legacyName, boolean aiming) {
        if ("gun_folly".equals(legacyName)) {
            return aiming ? 2.0F : 2.5F;
        }
        if (aiming) {
            return switch (legacyName) {
                case "gun_charge_thrower", "gun_drill" -> 0.0F;
                case "gun_stg77" -> 0.5F;
                default -> 2.5F;
            };
        }
        return switch (legacyName) {
            case "gun_mas36", "gun_hangman", "gun_laser_pistol", "gun_laser_pistol_pew_pew",
                    "gun_laser_pistol_morning_glory", "gun_greasegun", "gun_henry", "gun_henry_lincoln",
                    "gun_maresleg", "gun_maresleg_broken", "gun_maresleg_akimbo", "gun_m2",
                    "gun_pepperbox", "gun_minigun", "gun_minigun_lacunae", "gun_spas12",
                    "gun_missile_launcher", "gun_flamer", "gun_flamer_topaz", "gun_flamer_daybreaker",
                    "gun_tesla_cannon", "gun_autoshotgun", "gun_autoshotgun_shredder", "gun_fatman",
                    "gun_double_barrel", "gun_double_barrel_sacred_dragon", "gun_carbine", "gun_am180",
                    "gun_amat", "gun_amat_subtlety", "gun_amat_penance", "gun_drill", "gun_tau",
                    "gun_charge_thrower" -> -0.5F;
            case "gun_heavy_revolver", "gun_heavy_revolver_lilmac", "gun_heavy_revolver_protege", "gun_mk108",
                    "gun_lag", "gun_n_i_4_n_i", "gun_flaregun", "gun_panzerschreck", "gun_liberator",
                    "gun_autoshotgun_sexy", "gun_autoshotgun_heretic", "gun_quadro", "gun_uzi",
                    "gun_uzi_akimbo", "gun_lasrifle", "gun_stinger", "gun_star_f", "gun_star_f_akimbo",
                    "gun_chemthrower", "gun_g3", "gun_g3_zebra", "gun_aberrator", "gun_aberrator_eott",
                    "gun_minigun_dual", "gun_stg77", "gun_light_revolver_dani", "gun_light_revolver",
                    "gun_light_revolver_atlas", "gun_bolter", "gun_coilgun", "gun_congolake" -> -0.25F;
            default -> 2.75F;
        };
    }
}
