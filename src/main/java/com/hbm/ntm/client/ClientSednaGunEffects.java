package com.hbm.ntm.client;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bullet.SednaGunConfig;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.item.SednaGunItem;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-local counterpart of the old {@code ItemGunBaseNT.lastShot}, {@code shotRand}, and smoke-node state.
 * Rendering is intentionally supplied by the existing transient quad facades rather than a separate effect backend.
 */
public final class ClientSednaGunEffects {
    private static final long MUZZLE_FLASH_DURATION_MILLIS = 75L;
    private static final SmokeProfile NINE_MILLIMETER_SMOKE = new SmokeProfile(2_000L, 0.05D, 1.1D);
    private static final SmokeProfile TWENTY_TWO_SMOKE = new SmokeProfile(3_000L, 0.05D, 1.1D);
    private static final SmokeProfile STANDARD_SMOKE = new SmokeProfile(2_000L, 0.025D, 1.15D);
    private static final SmokeProfile RIFLE_SMOKE = new SmokeProfile(1_500L, 0.075D, 1.1D);
    private static final SmokeProfile FORTY_MILLIMETER_SMOKE = new SmokeProfile(1_500L, 0.025D, 1.05D);
    private static final ResourceLocation LILMAC_PLUME_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/lilmac_plume.png");
    private static final ResourceLocation LASER_FLASH_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID,
            "textures/models/weapons/laser_flash.png");
    private static final Map<SednaGunItem, EffectState[]> EFFECTS = new IdentityHashMap<>();

    /** Equivalent to the local CYCLE branch of the old HbmAnimationPacket Sedna handler. */
    public static void markCycle(SednaGunItem gun, int configIndex) {
        if (gun == null || configIndex < 0) {
            return;
        }
        EffectState state = state(gun, configIndex);
        state.lastShotMillis = System.currentTimeMillis();
        Player player = Minecraft.getInstance().player;
        state.shotRand = player == null ? 0.0D : player.getRandom().nextDouble();
    }

    /** Mirrors the old held-gun smoke callback for each source-profiled receiver state. */
    public static void tick(Minecraft minecraft) {
        Player player = minecraft.player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SednaGunItem gun)) {
            return;
        }
        SmokeProfile profile = smokeProfile(gun);
        if (profile == null) {
            return;
        }
        EffectState[] states = EFFECTS.get(gun);
        if (states == null) {
            return;
        }
        for (int configIndex = 0; configIndex < states.length; configIndex++) {
            EffectState state = states[configIndex];
            if (state != null) {
                tickStandardSmoke(player, stack, configIndex, state, profile);
            }
        }
    }

    public static void clearAll() {
        EFFECTS.clear();
    }

    /** Source {@code renderSmokeNodes(nodes, nodeWidthScale)} using the existing transparent quad facade. */
    public static void renderSmoke(SednaGunItem gun, int configIndex, double nodeWidthScale, PoseStack poseStack,
            MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, configIndex);
        if (state == null || state.smokeNodes.size() <= 1) {
            return;
        }
        LegacyUntexturedQuadRenderer.QuadBatch batch = LegacyUntexturedQuadRenderer.quadBatch(poseStack, buffer,
                LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE);
        for (int i = 0; i < state.smokeNodes.size() - 1; i++) {
            SmokeNode node = state.smokeNodes.get(i);
            SmokeNode past = state.smokeNodes.get(i + 1);
            int nodeAlpha = LegacyUntexturedQuadRenderer.alpha((float) node.alpha);
            int pastAlpha = LegacyUntexturedQuadRenderer.alpha((float) past.alpha);
            double nodeWidth = node.width * nodeWidthScale;
            double pastWidth = past.width * nodeWidthScale;
            LegacyUntexturedQuadRenderer.quad(batch,
                    node.forward, node.lift, node.side,
                    node.forward, node.lift, node.side + nodeWidth,
                    past.forward, past.lift, past.side + pastWidth,
                    past.forward, past.lift, past.side,
                    0xFFFFFF, nodeAlpha, 0, 0, pastAlpha);
            LegacyUntexturedQuadRenderer.quad(batch,
                    node.forward, node.lift, node.side,
                    node.forward, node.lift, node.side - nodeWidth,
                    past.forward, past.lift, past.side - pastWidth,
                    past.forward, past.lift, past.side,
                    0xFFFFFF, nodeAlpha, 0, 0, pastAlpha);
        }
    }

    /** Source {@code renderMuzzleFlash(lastShot, 75, length)} geometry and timing. */
    public static void renderMuzzleFlash(SednaGunItem gun, int configIndex, double length, PoseStack poseStack,
            MultiBufferSource buffer) {
        renderMuzzleFlash(gun, configIndex, MUZZLE_FLASH_DURATION_MILLIS, length, poseStack, buffer);
    }

    /** Source {@code renderMuzzleFlash(lastShot, duration, length)} geometry and timing. */
    public static void renderMuzzleFlash(SednaGunItem gun, int configIndex, long durationMillis, double length,
            PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, configIndex);
        if (state == null) {
            return;
        }
        renderMuzzleFlash(state.lastShotMillis, durationMillis, length, poseStack, buffer);
    }

    /**
     * Source {@code ItemRenderWeaponBase.flashMap} consumer for a remote holder.
     * The packet timestamp is deliberately separate from the local receiver state: old remote renderers had no
     * receiver-local shot random and therefore used the unrotated flash geometry.
     */
    public static void renderMuzzleFlash(long shotMillis, long durationMillis, double length, PoseStack poseStack,
            MultiBufferSource buffer) {
        long age = System.currentTimeMillis() - shotMillis;
        if (age < 0L || age >= durationMillis) {
            return;
        }
        double fire = age / (double) durationMillis;
        double width = 6.0D * fire;
        double flashLength = length * fire;
        double inset = 2.0D;
        LegacyTexturedQuadRenderer.TexturedQuadBatch batch = LegacyTexturedQuadRenderer.texturedQuadBatch(
                LILMAC_PLUME_TEXTURE, poseStack, buffer, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 255);
        int light = LightTexture.FULL_BRIGHT;
        renderFlashQuad(batch, light,
                0.0D, -width, -inset, 1.0D, 1.0D,
                0.0D, width, -inset, 0.0D, 1.0D,
                0.1D, width, flashLength - inset, 0.0D, 0.0D,
                0.1D, -width, flashLength - inset, 1.0D, 0.0D);
        renderFlashQuad(batch, light,
                0.0D, width, inset, 0.0D, 1.0D,
                0.0D, -width, inset, 1.0D, 1.0D,
                0.1D, -width, -flashLength + inset, 1.0D, 0.0D,
                0.1D, width, -flashLength + inset, 0.0D, 0.0D);
        renderFlashQuad(batch, light,
                0.0D, -inset, width, 0.0D, 1.0D,
                0.0D, -inset, -width, 1.0D, 1.0D,
                0.1D, flashLength - inset, -width, 1.0D, 0.0D,
                0.1D, flashLength - inset, width, 0.0D, 0.0D);
        renderFlashQuad(batch, light,
                0.0D, inset, -width, 1.0D, 1.0D,
                0.0D, inset, width, 0.0D, 1.0D,
                0.1D, -flashLength + inset, width, 0.0D, 0.0D,
                0.1D, -flashLength + inset, -width, 1.0D, 0.0D);
    }

    public static void renderLagEffects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-10.25D, 1.0D, 0.0D);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderSmoke(gun, 0, 0.5D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(-10.25D, 1.0D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        renderMuzzleFlash(gun, 0, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    public static void renderUziEffects(SednaGunItem gun, int configIndex, boolean silenced, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (silenced || stateOrNull(gun, configIndex) == null) {
            return;
        }
        EffectState state = stateOrNull(gun, configIndex);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.75D, 8.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderSmoke(gun, configIndex, 0.75D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.75D, 8.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        renderMuzzleFlash(gun, configIndex, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    public static void renderGreasegunEffects(SednaGunItem gun, double turnZ, PoseStack poseStack,
            MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-0.25D, 0.0D, 1.5D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) -turnZ);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.25F, 0.25F, 0.25F);
        renderSmoke(gun, 0, 1.0D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 8.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) -turnZ);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.25F, 0.25F, 0.25F);
        renderSmoke(gun, 0, 1.0D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 8.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderMuzzleFlash(gun, 0, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    public static void renderCarbineEffects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 8.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderSmoke(gun, 0, 0.25D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 8.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderMuzzleFlash(gun, 0, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    public static void renderG3Effects(SednaGunItem gun, boolean silenced, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (silenced) {
            return;
        }
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 13.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        renderSmoke(gun, 0, 0.5D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 12.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (-25.0D + state.shotRand * 10.0D));
        poseStack.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(gun, 0, 10.0D, poseStack, buffer);
        poseStack.popPose();
    }

    public static void renderStg77Effects(SednaGunItem gun, double equipX, double liftX, double recoilZ,
            PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, -1.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) equipX);
        poseStack.translate(0.0D, 1.0D, 4.0D);
        poseStack.translate(0.0D, 0.0D, -4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) liftX);
        poseStack.translate(0.0D, 0.0D, 4.0D);
        poseStack.translate(0.0D, 0.0D, recoilZ);
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 8.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        renderSmoke(gun, 0, 0.5D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 7.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.25F, 0.25F, 0.25F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (-5.0D + state.shotRand * 10.0D));
        renderGapFlash(gun, 0, poseStack, buffer);
        poseStack.popPose();
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderMinigun conventional-gun smoke and 50 ms flash placement. */
    public static void renderMinigunEffects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-2.0D, 1.25D, -3.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderSmoke(gun, 0, 0.5D, poseStack, buffer);
        poseStack.popPose();
        renderMinigunDualFlash(gun, 0, poseStack, buffer);
    }

    /** Exact ItemRenderMinigunDual flash placement for each independently cycled receiver. */
    public static void renderMinigunDualFlash(SednaGunItem gun, int configIndex, PoseStack poseStack,
            MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, configIndex);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 12.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.translate(0.0D, 0.5D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(1.5F, 1.5F, 1.5F);
        renderMuzzleFlash(gun, configIndex, 50L, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact ItemRenderMinigun Lacunae twin colored laser flashes; source does not draw ordinary smoke. */
    public static void renderMinigunLacunaeEffects(SednaGunItem gun, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (stateOrNull(gun, 0) == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 12.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderLaserFlash(gun, 0, 50L, 1.0D, 0xFF00FF, poseStack, buffer);
        poseStack.translate(0.0D, 0.0D, -0.25D);
        renderLaserFlash(gun, 0, 50L, 0.5D, 0xFF0080, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact ItemRenderLaserPistol paired flash placement and Morning Glory colors. */
    public static void renderLaserPistolEffects(SednaGunItem gun, boolean emerald, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (stateOrNull(gun, 0) == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 2.0D, 4.75D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderLaserFlash(gun, 0, 150L, 1.5D, emerald ? 0x008000 : 0xFF0000, poseStack, buffer);
        poseStack.translate(0.0D, 0.0D, -0.25D);
        renderLaserFlash(gun, 0, 150L, 0.75D, emerald ? 0x80FF00 : 0xFF8000, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact ItemRenderLasrifle paired red/orange flash placement. */
    public static void renderLasrifleEffects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        if (stateOrNull(gun, 0) == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, 12.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderLaserFlash(gun, 0, 150L, 1.5D, 0xFF0000, poseStack, buffer);
        poseStack.translate(0.0D, 0.0D, -0.25D);
        renderLaserFlash(gun, 0, 150L, 0.75D, 0xFF8000, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact ItemRenderNI4NI white laser flash placement. */
    public static void renderNi4NiLaserFlash(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.75D, 4.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(0.125F, 0.125F, 0.125F);
        renderLaserFlash(gun, 0, 75L, 7.5D, 0xFFFFFF, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact ItemRenderAberrator/ItemRenderEOTT smoke, plume, and 150 ms fireball placement. */
    public static void renderAberratorEffects(SednaGunItem gun, int configIndex, int side, double recoilX,
            double rollZ, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, configIndex);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 2.0D, 4.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) -recoilX);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (-rollZ * side));
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderSmoke(gun, configIndex, 0.5D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 2.0D, 4.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(gun, configIndex, 75L, 7.5D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 2.0D, -1.5D);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderFireball(gun, configIndex, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderDoubleBarrel standard-flash placement. */
    public static void renderDoubleBarrelEffects(SednaGunItem gun, PoseStack poseStack,
            MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 8.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(2.0F, 2.0F, 2.0F);
        renderMuzzleFlash(gun, 0, 5.0D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderHenry smoke and standard-flash placement. */
    public static void renderHenryEffects(SednaGunItem gun, double turnZ, PoseStack poseStack,
            MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) return;
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 8.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) -turnZ);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderSmoke(gun, 0, 0.25D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 8.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        renderMuzzleFlash(gun, 0, 5.0D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact ItemRenderMaresleg and ItemRenderMareslegAkimbo receiver-local effects. */
    public static void renderMareslegEffects(SednaGunItem gun, int configIndex, boolean shortened, double turnZ,
            double flipX, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, configIndex);
        if (state == null) return;
        double muzzleZ = shortened ? 3.75D : 8.0D;
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, muzzleZ);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) -turnZ);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) flipX);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderSmoke(gun, configIndex, 0.25D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, muzzleZ);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        renderMuzzleFlash(gun, configIndex, 5.0D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Source ItemRenderAtlas/DANI smoke placement, called before receiver reload transforms. */
    public static void renderRevolverSmoke(SednaGunItem gun, int configIndex, double recoilZ, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (stateOrNull(gun, configIndex) == null) return;
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, 9.25D);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (-recoilZ * 10.0D));
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderSmoke(gun, configIndex, 0.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Source ItemRenderAtlas/DANI standard-flash placement after the receiver transforms. */
    public static void renderRevolverFlash(SednaGunItem gun, int configIndex, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (stateOrNull(gun, configIndex) == null) return;
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, 9.25D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderMuzzleFlash(gun, configIndex, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderMAS36 smoke and standard-flash placement. */
    public static void renderMas36Effects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.125D, 8.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.25F, 0.25F, 0.25F);
        renderSmoke(gun, 0, 1.0D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 8.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderMuzzleFlash(gun, 0, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderAm180 silencer branch, smoke and flash placement. */
    public static void renderAm180Effects(SednaGunItem gun, boolean silenced, double turnZ, PoseStack poseStack,
            MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.875D, silenced ? 17.0D : 13.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) -turnZ);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderSmoke(gun, 0, 0.25D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.875D, silenced ? 16.75D : 12.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        float scale = silenced ? 0.5F : 0.75F;
        poseStack.scale(scale, scale, scale);
        renderMuzzleFlash(gun, 0, silenced ? 75L : 50L, silenced ? 5.0D : 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderSPAS12 smoke and standard-flash placement. */
    public static void renderSpas12Effects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, -11.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        poseStack.scale(0.25F, 0.25F, 0.25F);
        renderSmoke(gun, 0, 0.75D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.5D, -11.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        renderMuzzleFlash(gun, 0, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderStarF and ItemRenderStarFAkimbo non-silenced effect placement. */
    public static void renderStarFEffects(SednaGunItem gun, int configIndex, boolean silenced, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (silenced) {
            return;
        }
        EffectState state = stateOrNull(gun, configIndex);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 3.0D, 6.125D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderSmoke(gun, configIndex, 0.75D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 3.0D, 6.125D);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        renderMuzzleFlash(gun, configIndex, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderPepperbox standard-smoke and paired flash placement. */
    public static void renderPepperboxEffects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 7.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        renderSmoke(gun, 0, 0.5D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 7.0D);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        renderMuzzleFlash(gun, 0, 7.5D, poseStack, buffer);
        LegacyPoseRotations.rotateXDegrees(poseStack, 45.0F);
        renderMuzzleFlash(gun, 0, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderHangman smoke and flash placement. */
    public static void renderHangmanEffects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 29.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(1.5F, 1.5F, 1.5F);
        renderSmoke(gun, 0, 0.5D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 29.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(2.0F, 2.0F, 2.0F);
        renderMuzzleFlash(gun, 0, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderShredder smoke and flash placement. */
    public static void renderShredderEffects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 7.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        renderSmoke(gun, 0, 0.5D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 7.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(gun, 0, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderSexy 150 ms flash; its source renderer does not draw smoke nodes. */
    public static void renderSexyEffects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 8.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        renderMuzzleFlash(gun, 0, 150L, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderM2 smoke and flash placement. */
    public static void renderM2Effects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.625D, 5.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderSmoke(gun, 0, 0.375D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.625D, 5.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderMuzzleFlash(gun, 0, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderLiberator four-ribbon smoke and flash placement. */
    public static void renderLiberatorEffects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        final double smokeScale = 0.375D;
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.25D, 7.25D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale((float) smokeScale, (float) smokeScale, (float) smokeScale);
        poseStack.translate(0.0D, 0.0D, 0.25D / smokeScale);
        renderSmoke(gun, 0, 1.0D, poseStack, buffer);
        poseStack.translate(0.0D, 0.0D, -0.5D / smokeScale);
        renderSmoke(gun, 0, 1.0D, poseStack, buffer);
        poseStack.translate(0.0D, 0.5D / smokeScale, 0.0D);
        renderSmoke(gun, 0, 1.0D, poseStack, buffer);
        poseStack.translate(0.0D, 0.0D, 0.5D / smokeScale);
        renderSmoke(gun, 0, 1.0D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 8.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(1.5F, 1.5F, 1.5F);
        renderMuzzleFlash(gun, 0, 5.0D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderCongoLake placement, including its authored Gun-rotation cancellation. */
    public static void renderCongolakeEffects(SednaGunItem gun, double[] gunTransform, PoseStack poseStack,
            MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.75D, 4.25D);
        LegacyPoseRotations.rotateZDegrees(poseStack, -((float) gunTransform[5]));
        LegacyPoseRotations.rotateYDegrees(poseStack, -((float) gunTransform[4]));
        LegacyPoseRotations.rotateXDegrees(poseStack, -((float) gunTransform[3]));
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.25F, 0.25F, 0.25F);
        renderSmoke(gun, 0, 1.0D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.75D, 4.25D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderMuzzleFlash(gun, 0, 150L, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderMK108 50 ms flash placement; its source renderer does not draw smoke nodes. */
    public static void renderMk108Effects(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 8.125D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        renderMuzzleFlash(gun, 0, 50L, 5.0D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact ItemRenderHeavyRevolver smoke placement before the legacy reload pose is applied. */
    public static void renderHeavyRevolverSmoke(SednaGunItem gun, double recoilZ, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (stateOrNull(gun, 0) == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-9.0D, 2.5D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (recoilZ * -10.0D));
        renderSmoke(gun, 0, 0.5D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact ItemRenderHeavyRevolver gap-flash placement after the legacy reload pose is applied. */
    public static void renderHeavyRevolverFlash(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        if (stateOrNull(gun, 0) == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.125D, 2.5D, 0.0D);
        renderGapFlash(gun, 0, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact unsilenced ItemRenderAmat smoke and gap-flash placement. */
    public static void renderAmatEffects(SednaGunItem gun, boolean silenced, PoseStack poseStack,
            MultiBufferSource buffer) {
        if (silenced || stateOrNull(gun, 0) == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.625D, 12.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderSmoke(gun, 0, 1.0D, poseStack, buffer);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 11.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        renderGapFlash(gun, 0, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact ItemRenderFlaregun pair of smoke ribbons; its source renderer has no flash. */
    public static void renderFlaregunSmoke(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        if (stateOrNull(gun, 0) == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 4.0D, 9.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        renderSmoke(gun, 0, 2.5D, poseStack, buffer);
        poseStack.translate(0.0D, 0.0D, 0.1D);
        renderSmoke(gun, 0, 2.0D, poseStack, buffer);
        poseStack.popPose();
    }

    /** Exact first-person ItemRenderMissileLauncher flash. */
    public static void renderMissileLauncherFlash(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        renderRocketLauncherFlash(gun, 0.0D, 1.0D, 6.75D, 75L, poseStack, buffer);
    }

    /** Exact first-person ItemRenderPanzerschreck flash. */
    public static void renderPanzerschreckFlash(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        renderRocketLauncherFlash(gun, 0.0D, 0.0D, 6.5D, 150L, poseStack, buffer);
    }

    /** Exact first-person ItemRenderQuadro flash. */
    public static void renderQuadroFlash(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        renderRocketLauncherFlash(gun, -1.0D, 0.75D, 6.5D, 150L, poseStack, buffer);
    }

    /** Exact first-person ItemRenderStinger flash. */
    public static void renderStingerFlash(SednaGunItem gun, PoseStack poseStack, MultiBufferSource buffer) {
        renderRocketLauncherFlash(gun, 0.0D, 0.0D, 6.5D, 150L, poseStack, buffer);
    }

    private static void renderRocketLauncherFlash(SednaGunItem gun, double x, double y, double z,
            long durationMillis, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, 0);
        if (state == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (90.0D * state.shotRand));
        poseStack.scale(0.75F, 0.75F, 0.75F);
        renderMuzzleFlash(gun, 0, durationMillis, 7.5D, poseStack, buffer);
        poseStack.popPose();
    }

    private static void renderFlashQuad(LegacyTexturedQuadRenderer.TexturedQuadBatch batch, int light,
            double x0, double y0, double z0, double u0, double v0,
            double x1, double y1, double z1, double u1, double v1,
            double x2, double y2, double z2, double u2, double v2,
            double x3, double y3, double z3, double u3, double v3) {
        LegacyTexturedQuadRenderer.quadDirect(batch, light, 0, 0.0F, 1.0F, 0.0F,
                x0, y0, z0, u0, v0, x1, y1, z1, u1, v1, x2, y2, z2, u2, v2, x3, y3, z3, u3, v3,
                0xFFFFFF, 255);
    }

    /** Source {@code ItemRenderWeaponBase.renderLaserFlash} geometry using the existing textured-quad facade. */
    private static void renderLaserFlash(SednaGunItem gun, int configIndex, long durationMillis, double scale,
            int color, PoseStack poseStack, MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, configIndex);
        if (state == null) {
            return;
        }
        renderLaserFlash(state.lastShotMillis, durationMillis, scale, color, poseStack, buffer);
    }

    /** Source {@code flashMap} variant of {@code ItemRenderWeaponBase.renderLaserFlash}. */
    public static void renderLaserFlash(long shotMillis, long durationMillis, double scale, int color,
            PoseStack poseStack, MultiBufferSource buffer) {
        long age = System.currentTimeMillis() - shotMillis;
        if (age < 0L || age >= durationMillis) {
            return;
        }
        double size = 4.0D * (age / (double) durationMillis) * scale;
        LegacyTexturedQuadRenderer.TexturedQuadBatch batch = LegacyTexturedQuadRenderer.texturedQuadBatch(
                LASER_FLASH_TEXTURE, poseStack, buffer, LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 255);
        LegacyTexturedQuadRenderer.quadDirect(batch, LightTexture.FULL_BRIGHT, 0, 0.0F, 1.0F, 0.0F,
                0.0D, -size, -size, 1.0D, 1.0D,
                0.0D, size, -size, 0.0D, 1.0D,
                0.0D, size, size, 0.0D, 0.0D,
                0.0D, -size, size, 1.0D, 0.0D,
                color, 255);
    }

    /** Exact shared geometry of {@code ItemRenderAberrator/EOTT.renderFireball(lastShot)}. */
    private static void renderFireball(SednaGunItem gun, int configIndex, PoseStack poseStack,
            MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, configIndex);
        if (state == null) {
            return;
        }
        renderFireball(state.lastShotMillis, poseStack, buffer);
    }

    /** Source {@code flashMap} variant of the Aberrator/EOTT 150 ms fireball. */
    public static void renderFireball(long shotMillis, PoseStack poseStack, MultiBufferSource buffer) {
        long age = System.currentTimeMillis() - shotMillis;
        if (age < 0L || age >= 150L) {
            return;
        }
        double fire = age / 150.0D;
        double height = 5.0D * fire;
        double length = 10.0D * fire;
        double offset = fire;
        double lengthOffset = -1.125D;
        LegacyTexturedQuadRenderer.TexturedQuadBatch batch = LegacyTexturedQuadRenderer.texturedQuadBatch(
                LILMAC_PLUME_TEXTURE, poseStack, buffer, LegacyTexturedRenderMode.ADDITIVE_DEPTH_WRITE, 255);
        int light = LightTexture.FULL_BRIGHT;
        LegacyTexturedQuadRenderer.quadDirect(batch, light, 0, 0.0F, 1.0F, 0.0F,
                height, -offset, 0.0D, 0.0D, 1.0D,
                -height, -offset, 0.0D, 1.0D, 1.0D,
                -height, -offset + length, -lengthOffset, 1.0D, 0.0D,
                height, -offset + length, -lengthOffset, 0.0D, 0.0D,
                0xFFFFFF, 255);
        LegacyTexturedQuadRenderer.quadDirect(batch, light, 0, 0.0F, 1.0F, 0.0F,
                height, -offset, 0.0D, 0.0D, 1.0D,
                -height, -offset, 0.0D, 1.0D, 1.0D,
                -height, -offset + length, lengthOffset, 1.0D, 0.0D,
                height, -offset + length, lengthOffset, 0.0D, 0.0D,
                0xFFFFFF, 255);
    }

    /** Exact source geometry of {@code ItemRenderWeaponBase.renderGapFlash(lastShot)}. */
    public static void renderGapFlash(SednaGunItem gun, int configIndex, PoseStack poseStack,
            MultiBufferSource buffer) {
        EffectState state = stateOrNull(gun, configIndex);
        if (state == null) {
            return;
        }
        renderGapFlash(state.lastShotMillis, poseStack, buffer);
    }

    /** Source {@code flashMap} variant of {@code ItemRenderWeaponBase.renderGapFlash}. */
    public static void renderGapFlash(long shotMillis, PoseStack poseStack, MultiBufferSource buffer) {
        long age = System.currentTimeMillis() - shotMillis;
        if (age < 0L || age >= MUZZLE_FLASH_DURATION_MILLIS) {
            return;
        }
        double fire = age / (double) MUZZLE_FLASH_DURATION_MILLIS;
        double height = 4.0D * fire;
        double length = 15.0D * fire;
        double lift = 3.0D * fire;
        double offset = fire;
        LegacyTexturedQuadRenderer.TexturedQuadBatch batch = LegacyTexturedQuadRenderer.texturedQuadBatch(
                LILMAC_PLUME_TEXTURE, poseStack, buffer, LegacyTexturedRenderMode.ADDITIVE_DEPTH_WRITE, 255);
        int light = LightTexture.FULL_BRIGHT;
        renderFlashQuad(batch, light,
                0.0D, -height, -offset, 1.0D, 1.0D,
                0.0D, height, -offset, 0.0D, 1.0D,
                0.0D, height + lift, length - offset, 0.0D, 0.0D,
                0.0D, -height + lift, length - offset, 1.0D, 0.0D);
        renderFlashQuad(batch, light,
                0.0D, height, offset, 0.0D, 1.0D,
                0.0D, -height, offset, 1.0D, 1.0D,
                0.0D, -height + lift, -length + offset, 1.0D, 0.0D,
                0.0D, height + lift, -length + offset, 0.0D, 0.0D);
        renderFlashQuad(batch, light,
                0.0D, -height, -offset, 1.0D, 1.0D,
                0.0D, height, -offset, 0.0D, 1.0D,
                0.125D, height, length - offset, 0.0D, 0.0D,
                0.125D, -height, length - offset, 1.0D, 0.0D);
        renderFlashQuad(batch, light,
                0.0D, height, offset, 0.0D, 1.0D,
                0.0D, -height, offset, 1.0D, 1.0D,
                0.125D, -height, -length + offset, 1.0D, 0.0D,
                0.125D, height, -length + offset, 0.0D, 0.0D);
    }

    private static void tickStandardSmoke(Player player, ItemStack stack, int configIndex, EffectState state,
            SmokeProfile profile) {
        long now = System.currentTimeMillis();
        boolean smoking = state.lastShotMillis + profile.durationMillis() > now;
        if (!smoking && !state.smokeNodes.isEmpty()) {
            state.smokeNodes.clear();
        }
        if (!smoking) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        double x = -motion.x;
        double y = -motion.y;
        double z = -motion.z;
        double yawRadians = Math.toRadians(player.getYRot());
        double cosine = Math.cos(yawRadians);
        double sine = Math.sin(yawRadians);
        double rotatedX = x * cosine + z * sine;
        double rotatedZ = z * cosine - x * sine;
        double side = (player.getYRot() - player.yHeadRotO) * 0.1D;
        for (SmokeNode node : state.smokeNodes) {
            node.forward += -rotatedZ * 15.0D + player.getRandom().nextGaussian() * 0.025D;
            node.lift += y + 1.5D;
            node.side += rotatedX * 15.0D + player.getRandom().nextGaussian() * 0.025D + side;
            if (node.alpha > 0.0D) {
                node.alpha -= profile.alphaDecay();
            }
            node.width *= profile.widthGrowth();
        }
        double alpha = (1.0D - (now - state.lastShotMillis) / (double) profile.durationMillis()) * 0.5D;
        if (isReloading(stack, configIndex) || state.smokeNodes.isEmpty()) {
            alpha = 0.0D;
        }
        state.smokeNodes.add(new SmokeNode(alpha));
    }

    private static boolean isReloading(ItemStack stack, int configIndex) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getInt("state_" + configIndex) == SednaGunConfig.GunState.RELOADING.ordinal();
    }

    private static SmokeProfile smokeProfile(SednaGunItem gun) {
        return switch (gun.gunConfig().legacyName()) {
            case "gun_greasegun", "gun_lag", "gun_uzi", "gun_uzi_akimbo", "gun_bolter" ->
                    NINE_MILLIMETER_SMOKE;
            case "gun_am180", "gun_star_f", "gun_star_f_akimbo" -> TWENTY_TWO_SMOKE;
            case "gun_light_revolver", "gun_light_revolver_atlas", "gun_light_revolver_dani", "gun_henry",
                    "gun_henry_lincoln", "gun_spas12", "gun_double_barrel",
                    "gun_double_barrel_sacred_dragon", "gun_maresleg", "gun_maresleg_akimbo",
                    "gun_maresleg_broken", "gun_pepperbox", "gun_hangman", "gun_liberator",
                    "gun_autoshotgun", "gun_autoshotgun_shredder", "gun_autoshotgun_sexy",
                    "gun_autoshotgun_heretic", "gun_heavy_revolver", "gun_heavy_revolver_lilmac",
                    "gun_heavy_revolver_protege", "gun_debug", "gun_aberrator", "gun_aberrator_eott" -> STANDARD_SMOKE;
            case "gun_carbine", "gun_g3", "gun_g3_zebra", "gun_stg77", "gun_minigun", "gun_minigun_dual" -> RIFLE_SMOKE;
            case "gun_m2" -> NINE_MILLIMETER_SMOKE;
            case "gun_congolake", "gun_mk108", "gun_flaregun" -> FORTY_MILLIMETER_SMOKE;
            case "gun_amat", "gun_amat_subtlety", "gun_amat_penance" -> NINE_MILLIMETER_SMOKE;
            case "gun_minigun_lacunae" -> null;
            default -> null;
        };
    }

    private static EffectState state(SednaGunItem gun, int configIndex) {
        EffectState[] states = EFFECTS.computeIfAbsent(gun, ignored -> new EffectState[Math.max(1,
                gun.gunConfig().configs().size())]);
        if (configIndex >= states.length) {
            throw new IllegalArgumentException("Unknown Sedna gun config index " + configIndex);
        }
        if (states[configIndex] == null) {
            states[configIndex] = new EffectState();
        }
        return states[configIndex];
    }

    private static EffectState stateOrNull(SednaGunItem gun, int configIndex) {
        EffectState[] states = EFFECTS.get(gun);
        return states == null || configIndex < 0 || configIndex >= states.length ? null : states[configIndex];
    }

    private static final class EffectState {
        private long lastShotMillis;
        private double shotRand;
        private final List<SmokeNode> smokeNodes = new ArrayList<>();
    }

    private static final class SmokeNode {
        private double forward;
        private double side;
        private double lift;
        private double alpha;
        private double width = 1.0D;

        private SmokeNode(double alpha) {
            this.alpha = alpha;
        }
    }

    private record SmokeProfile(long durationMillis, double alphaDecay, double widthGrowth) {
    }

    private ClientSednaGunEffects() {
    }
}
