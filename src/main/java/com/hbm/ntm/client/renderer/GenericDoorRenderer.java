package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.GenericDoorBlock;
import com.hbm.ntm.blockentity.GenericDoorBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjDoorModels;
import com.hbm.ntm.client.obj.ObjPheoDoorModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.client.sound.LegacyMachineAudio;
import com.hbm.ntm.registry.ModSounds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Client transcription of 1.7.10 {@code RenderDoorGeneric} and its twelve
 * source-backed {@code IRenderDoors} implementations.  The server owns the
 * authoritative state; this renderer intentionally derives all animation
 * positions from the synchronised state/openTicks/skin snapshot.
 */
public final class GenericDoorRenderer implements BlockEntityRenderer<GenericDoorBlockEntity> {
    private static final double AUDIO_DISTANCE = 160.0D;
    private final Map<GenericDoorBlockEntity, AudioState> audio = new WeakHashMap<>();

    public GenericDoorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(GenericDoorBlockEntity door) {
        return false;
    }

    @Override
    public boolean shouldRender(GenericDoorBlockEntity door, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(door, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(door, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(GenericDoorBlockEntity door, float partialTick, PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(door, getViewDistance())) {
            return;
        }
        String id = door.definition().id();
        if (!hasLegacyAssets(id)) {
            // transition_seal deliberately remains on its temporary carrier until its legacy
            // DAE/AnimatedModel animation format receives a separate migration.
            return;
        }
        updateAudio(door, id);
        int light = LegacyRenderLighting.resolveMultiblockLight(door, packedLight);
        double ticks = interpolatedTicks(door, partialTick);
        double progress = clamp(ticks / door.definition().timeToOpen(), 0.0D, 1.0D);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, legacyRotation(door.getBlockState()));
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(door);
                var animatedScope = LegacyBlockEntityRenderCulling.animatedModelFadeScope(door)) {
            switch (id) {
                case "vault_door" -> renderVault(door, progress, poseStack, buffer, light, packedLight, packedOverlay);
                case "fire_door" -> renderFire(door, progress, poseStack, buffer, light, packedLight, packedOverlay);
                case "sliding_blast_door" -> renderSlidingBlast(door, ticks, poseStack, buffer,
                        light, packedLight, packedOverlay);
                case "sliding_seal_door" -> renderSeal(progress, poseStack, buffer, light, packedLight, packedOverlay);
                case "secure_access_door" -> renderSecure(door, progress, poseStack, buffer,
                        light, packedLight, packedOverlay);
                case "round_airlock_door" -> renderAirlock(door, progress, poseStack, buffer,
                        light, packedLight, packedOverlay);
                case "qe_sliding_door" -> renderQeSliding(progress, poseStack, buffer,
                        light, packedLight, packedOverlay);
                case "qe_containment" -> renderContainment(door, progress, poseStack, buffer,
                        light, packedLight, packedOverlay);
                case "water_door" -> renderWater(door, ticks, poseStack, buffer, light, packedLight, packedOverlay);
                case "silo_hatch" -> renderSilo(door, false, ticks, poseStack, buffer,
                        light, packedLight, packedOverlay);
                case "silo_hatch_large" -> renderSilo(door, true, ticks, poseStack, buffer,
                        light, packedLight, packedOverlay);
                case "large_vehicle_door" -> renderVehicle(progress, poseStack, buffer,
                        light, packedLight, packedOverlay);
                case "cargo_door" -> renderCargo(door, ticks, poseStack, buffer, light, packedLight, packedOverlay);
                default -> { }
            }
        }
        poseStack.popPose();
    }

    private static boolean hasLegacyAssets(String id) {
        return switch (id) {
            case "vault_door", "fire_door", "sliding_blast_door", "sliding_seal_door",
                    "secure_access_door", "round_airlock_door", "qe_sliding_door", "qe_containment",
                    "water_door", "silo_hatch", "silo_hatch_large", "large_vehicle_door", "cargo_door" -> true;
            default -> false;
        };
    }

    /* RenderDoorGeneric receives the legacy player-facing metadata.  The modern FACING value is
       the opposite placement direction, hence this exact four-way inverse mapping. */
    private static float legacyRotation(BlockState state) {
        Direction facing = state.hasProperty(GenericDoorBlock.FACING)
                ? state.getValue(GenericDoorBlock.FACING) : Direction.NORTH;
        return switch (facing) {
            case NORTH -> 270.0F;
            case EAST -> 180.0F;
            case SOUTH -> 90.0F;
            case WEST -> 0.0F;
            default -> 0.0F;
        };
    }

    private static double interpolatedTicks(GenericDoorBlockEntity door, float partialTick) {
        return switch (door.state()) {
            case GenericDoorBlockEntity.STATE_OPENING -> clamp(door.openTicks() + partialTick,
                    0.0D, door.definition().timeToOpen());
            case GenericDoorBlockEntity.STATE_CLOSING -> clamp(door.openTicks() - partialTick,
                    0.0D, door.definition().timeToOpen());
            case GenericDoorBlockEntity.STATE_OPEN -> door.definition().timeToOpen();
            default -> 0.0D;
        };
    }

    private static void renderVault(GenericDoorBlockEntity door, double progress, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int activityLight, int overlay) {
        ResourceLocation doorTexture = switch (door.skinIndex()) {
            case 3, 4 -> ObjPheoDoorModels.VAULT_DOOR_4_TEXTURE;
            case 5, 6 -> ObjPheoDoorModels.VAULT_DOOR_S_TEXTURE;
            default -> ObjPheoDoorModels.VAULT_DOOR_3_TEXTURE;
        };
        ResourceLocation labelTexture = switch (door.skinIndex()) {
            case 1 -> ObjPheoDoorModels.LABEL_87_TEXTURE;
            case 2 -> ObjPheoDoorModels.LABEL_106_TEXTURE;
            case 3 -> ObjPheoDoorModels.LABEL_81_TEXTURE;
            case 4 -> ObjPheoDoorModels.LABEL_111_TEXTURE;
            case 5 -> ObjPheoDoorModels.LABEL_2_TEXTURE;
            case 6 -> ObjPheoDoorModels.LABEL_99_TEXTURE;
            default -> ObjPheoDoorModels.LABEL_101_TEXTURE;
        };
        // DoorDecl's PULL bus is the first 2s; SLIDE starts after it and lasts 4s.
        double pull = clamp(progress * 3.0D, 0.0D, 1.0D);
        double slide = clamp((progress * 6.0D - 2.0D) / 4.0D, 0.0D, 1.0D) * 5.0D;
        double roll = 360.0D * slide / (4.25D * Math.PI);
        renderPart(ObjPheoDoorModels.VAULT_DOOR, "Frame", doorTexture, stack, buffer, light, overlay);
        stack.pushPose();
        stack.translate(-pull, 0.0D, slide);
        stack.translate(0.0D, 2.5D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(stack, (float) roll);
        stack.translate(0.0D, -2.5D, 0.0D);
        renderPart(ObjPheoDoorModels.VAULT_DOOR, "Door", doorTexture, stack, buffer, activityLight, overlay);
        renderPart(ObjPheoDoorModels.VAULT_DOOR, "Label", labelTexture, stack, buffer, activityLight, overlay);
        stack.popPose();
    }

    private static void renderFire(GenericDoorBlockEntity door, double progress, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int activityLight, int overlay) {
        ResourceLocation texture = switch (door.skinIndex()) {
            case 1 -> ObjPheoDoorModels.FIRE_DOOR_BLACK_TEXTURE;
            case 2 -> ObjPheoDoorModels.FIRE_DOOR_ORANGE_TEXTURE;
            case 3 -> ObjPheoDoorModels.FIRE_DOOR_YELLOW_TEXTURE;
            case 4 -> ObjPheoDoorModels.FIRE_DOOR_TREFOIL_TEXTURE;
            default -> ObjPheoDoorModels.FIRE_DOOR_TEXTURE;
        };
        stack.pushPose();
        LegacyPoseRotations.rotateYDegrees(stack, 90.0F);
        stack.translate(-0.5D, 0.0D, 0.0D);
        renderPart(ObjPheoDoorModels.FIRE_DOOR, "Frame", texture, stack, buffer, light, overlay);
        stack.translate(0.0D, 2.75D * progress, 0.0D);
        renderPart(ObjPheoDoorModels.FIRE_DOOR, "Door", texture, stack, buffer, activityLight, overlay);
        stack.popPose();
    }

    private static void renderSlidingBlast(GenericDoorBlockEntity door, double ticks, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int activityLight, int overlay) {
        double opening = clamp((ticks - 7.0D) / 13.0D, 0.0D, 1.0D) * 2.125D;
        double lock = door.state() == GenericDoorBlockEntity.STATE_OPEN ? 90.0D
                : door.state() == GenericDoorBlockEntity.STATE_CLOSED ? 0.0D
                : (door.state() == GenericDoorBlockEntity.STATE_OPENING
                        ? clamp(ticks / 4.0D, 0.0D, 1.0D) * 90.0D
                        : clamp((ticks - 4.0D) / 4.0D, 0.0D, 1.0D) * 90.0D);
        ResourceLocation texture = ObjPheoDoorModels.BLAST_DOOR_TEXTURE;
        renderPart(ObjPheoDoorModels.BLAST_DOOR, "Frame", texture, stack, buffer, light, overlay);
        stack.pushPose();
        stack.translate(0.0D, 0.0D, opening);
        renderClipped(ObjPheoDoorModels.BLAST_DOOR, "LeftDoor", texture, stack, buffer, activityLight, overlay,
                0.0D, 0.0D, 1.0D, 2.5D);
        rotateLockAndRender(ObjPheoDoorModels.BLAST_DOOR, "RightLock", texture, lock, stack, buffer,
                activityLight, overlay);
        stack.popPose();
        stack.pushPose();
        stack.translate(0.0D, 0.0D, -opening);
        renderClipped(ObjPheoDoorModels.BLAST_DOOR, "RightDoor", texture, stack, buffer, activityLight, overlay,
                0.0D, 0.0D, -1.0D, 2.5D);
        rotateLockAndRender(ObjPheoDoorModels.BLAST_DOOR, "LeftLock", texture, lock, stack, buffer,
                activityLight, overlay);
        stack.popPose();
    }

    private static void rotateLockAndRender(LegacyWavefrontModel model, String part, ResourceLocation texture,
            double lock, PoseStack stack, net.minecraft.client.renderer.MultiBufferSource buffer, int light, int overlay) {
        stack.pushPose();
        stack.translate(0.0D, 1.8125D, 0.0D);
        LegacyPoseRotations.rotateXDegrees(stack, (float) (90.0D + lock));
        stack.translate(0.0D, -1.8125D, 0.0D);
        renderPart(model, part, texture, stack, buffer, light, overlay);
        stack.popPose();
    }

    private static void renderSeal(double progress, PoseStack stack, net.minecraft.client.renderer.MultiBufferSource buffer,
            int light, int activityLight, int overlay) {
        stack.pushPose();
        stack.translate(0.5D, 0.0D, 0.0D);
        renderPart(ObjPheoDoorModels.SEAL_DOOR, "Frame", ObjPheoDoorModels.SEAL_DOOR_TEXTURE, stack, buffer, light, overlay);
        stack.translate(0.0D, 0.0D, smoothstep(progress) * 0.9D);
        renderClipped(ObjPheoDoorModels.SEAL_DOOR, "Door", ObjPheoDoorModels.SEAL_DOOR_TEXTURE, stack, buffer,
                activityLight, overlay, 0.0D, 0.0D, -1.0D, 0.5001D);
        stack.popPose();
    }

    private static void renderSecure(GenericDoorBlockEntity door, double progress, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int activityLight, int overlay) {
        ResourceLocation texture = switch (door.skinIndex()) {
            case 1 -> ObjPheoDoorModels.SECURE_DOOR_GREY_TEXTURE;
            case 2 -> ObjPheoDoorModels.SECURE_DOOR_BLACK_TEXTURE;
            case 3 -> ObjPheoDoorModels.SECURE_DOOR_YELLOW_TEXTURE;
            default -> ObjPheoDoorModels.SECURE_DOOR_TEXTURE;
        };
        stack.pushPose();
        stack.translate(0.0D, 1.0D, 0.0D);
        renderPart(ObjPheoDoorModels.SECURE_DOOR, "Frame", texture, stack, buffer, light, overlay);
        stack.translate(0.0D, 3.5D * progress, 0.0D);
        renderPart(ObjPheoDoorModels.SECURE_DOOR, "Door", texture, stack, buffer, activityLight, overlay);
        stack.popPose();
    }

    private static void renderAirlock(GenericDoorBlockEntity door, double progress, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int activityLight, int overlay) {
        ResourceLocation texture = switch (door.skinIndex()) {
            case 1 -> ObjPheoDoorModels.AIRLOCK_DOOR_CLEAN_TEXTURE;
            case 2 -> ObjPheoDoorModels.AIRLOCK_DOOR_GREEN_TEXTURE;
            default -> ObjPheoDoorModels.AIRLOCK_DOOR_TEXTURE;
        };
        double opening = 1.5D * progress;
        stack.pushPose();
        stack.translate(0.0D, 0.0D, 0.5D);
        renderPart(ObjPheoDoorModels.AIRLOCK_DOOR, "Frame", texture, stack, buffer, light, overlay);
        stack.pushPose();
        stack.translate(0.0D, 0.0D, opening);
        renderClipped(ObjPheoDoorModels.AIRLOCK_DOOR, "Left", texture, stack, buffer, activityLight, overlay,
                0.0D, 0.0D, 1.0D, 1.999D);
        stack.popPose();
        stack.pushPose();
        stack.translate(0.0D, 0.0D, -opening);
        renderClipped(ObjPheoDoorModels.AIRLOCK_DOOR, "Right", texture, stack, buffer, activityLight, overlay,
                0.0D, 0.0D, -1.0D, 1.999D);
        stack.popPose();
        stack.popPose();
    }

    private static void renderQeSliding(double progress, PoseStack stack, net.minecraft.client.renderer.MultiBufferSource buffer,
            int light, int activityLight, int overlay) {
        double opening = 0.95D * progress;
        stack.pushPose();
        stack.translate(0.53125D, 0.001D, 0.5D);
        renderPart(ObjPheoDoorModels.SLIDING_DOOR, "Frame", ObjPheoDoorModels.SLIDING_DOOR_TEXTURE, stack, buffer, light, overlay);
        stack.pushPose();
        stack.translate(0.0D, 0.0D, opening);
        renderPart(ObjPheoDoorModels.SLIDING_DOOR, "Left", ObjPheoDoorModels.SLIDING_DOOR_TEXTURE, stack, buffer,
                activityLight, overlay);
        stack.popPose();
        stack.pushPose();
        stack.translate(0.0D, 0.0D, -opening);
        renderPart(ObjPheoDoorModels.SLIDING_DOOR, "Right", ObjPheoDoorModels.SLIDING_DOOR_TEXTURE, stack, buffer,
                activityLight, overlay);
        stack.popPose();
        stack.popPose();
    }

    private static void renderContainment(GenericDoorBlockEntity door, double progress, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int activityLight, int overlay) {
        ResourceLocation texture = switch (door.skinIndex()) {
            case 1 -> ObjPheoDoorModels.CONTAINMENT_DOOR_TREFOIL_TEXTURE;
            case 2 -> ObjPheoDoorModels.CONTAINMENT_DOOR_TREFOIL_YELLOW_TEXTURE;
            default -> ObjPheoDoorModels.CONTAINMENT_DOOR_TEXTURE;
        };
        stack.pushPose();
        stack.translate(0.25D, 0.0D, 0.0D);
        renderPart(ObjPheoDoorModels.CONTAINMENT_DOOR, "Frame", texture, stack, buffer, light, overlay);
        stack.translate(0.0D, 2.25D * progress, 0.0D);
        renderClipped(ObjPheoDoorModels.CONTAINMENT_DOOR, "Door", texture, stack, buffer, activityLight, overlay,
                0.0D, -1.0D, 0.0D, 3.0D);
        stack.popPose();
    }

    private static void renderWater(GenericDoorBlockEntity door, double ticks, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int activityLight, int overlay) {
        double progress = clamp(ticks / 60.0D, 0.0D, 1.0D);
        double doorProgress;
        double bolt;
        if (door.state() == GenericDoorBlockEntity.STATE_OPENING) {
            doorProgress = smoothSin(clamp((ticks - 30.0D) / 30.0D, 0.0D, 1.0D));
            bolt = smoothSin(clamp(ticks / 30.0D, 0.0D, 1.0D));
        } else if (door.state() == GenericDoorBlockEntity.STATE_CLOSING) {
            doorProgress = smoothSin(progress);
            // The close bus holds BOLT at one for 1,200 ms (24 ticks), then
            // returns it over 1,500 ms.  With openTicks counting down this is
            // the exact 60..36 hold followed by the 36..0 return window.
            bolt = ticks >= 36.0D ? 1.0D : smoothSin(clamp(ticks / 30.0D, 0.0D, 1.0D));
        } else {
            doorProgress = progress;
            bolt = progress;
        }
        ResourceLocation texture = door.skinIndex() == 1
                ? ObjPheoDoorModels.WATER_DOOR_CLEAN_TEXTURE : ObjPheoDoorModels.WATER_DOOR_TEXTURE;
        stack.pushPose();
        stack.translate(0.375D, 0.0D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(stack, 90.0F);
        renderPart(ObjPheoDoorModels.WATER_DOOR, "Frame", texture, stack, buffer, light, overlay);
        stack.translate(-1.1875D, 0.0D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(stack, (float) (-120.0D * doorProgress));
        stack.translate(1.1875D, 0.0D, 0.0D);
        renderPart(ObjPheoDoorModels.WATER_DOOR, "Door_Cube.003", texture, stack, buffer, activityLight, overlay);
        stack.pushPose();
        stack.translate(-0.4D * bolt, 0.0D, 0.0D);
        renderPart(ObjPheoDoorModels.WATER_DOOR, "Bolts", texture, stack, buffer, activityLight, overlay);
        stack.popPose();
        renderWaterWheel("Top", 2.28125D, bolt, texture, stack, buffer, activityLight, overlay);
        renderWaterWheel("Bottom", 0.71875D, bolt, texture, stack, buffer, activityLight, overlay);
        stack.popPose();
    }

    private static void renderWaterWheel(String part, double y, double bolt, ResourceLocation texture, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int overlay) {
        stack.pushPose();
        stack.translate(0.40625D, y, 0.0D);
        LegacyPoseRotations.rotateZDegrees(stack, (float) (bolt * 360.0D));
        stack.translate(-0.40625D, -y, 0.0D);
        renderPart(ObjPheoDoorModels.WATER_DOOR, part, texture, stack, buffer, light, overlay);
        stack.popPose();
    }

    private static void renderSilo(GenericDoorBlockEntity door, boolean large, double ticks, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int activityLight, int overlay) {
        LegacyWavefrontModel model = large ? ObjDoorModels.SILO_HATCH_LARGE_LEGACY : ObjDoorModels.SILO_HATCH_LEGACY;
        ResourceLocation texture = large ? ObjDoorModels.SILO_HATCH_LARGE_TEXTURE : ObjDoorModels.SILO_HATCH_TEXTURE;
        double lift = 0.25D * smoothstep(clamp(ticks / 10.0D, 0.0D, 1.0D));
        double rotation = -240.0D * smoothstep(clamp((ticks - 20.0D) / 80.0D, 0.0D, 1.0D));
        double originZ = large ? -2.875D : -1.875D;
        renderPart(model, "Frame", texture, stack, buffer, light, overlay);
        stack.pushPose();
        stack.translate(0.0D, 0.875D, originZ);
        LegacyPoseRotations.rotateXDegrees(stack, (float) rotation);
        stack.translate(0.0D, -0.875D + lift, -originZ);
        renderPart(model, "Hatch", texture, stack, buffer, activityLight, overlay);
        stack.popPose();
    }

    private static void renderVehicle(double progress, PoseStack stack, net.minecraft.client.renderer.MultiBufferSource buffer,
            int light, int activityLight, int overlay) {
        double opening = 3.0D * progress;
        stack.pushPose();
        LegacyPoseRotations.rotateYDegrees(stack, 90.0F);
        renderPart(ObjPheoDoorModels.VEHICLE_DOOR, "Frame", ObjPheoDoorModels.VEHICLE_DOOR_TEXTURE, stack, buffer, light, overlay);
        stack.pushPose();
        stack.translate(-opening, 0.0D, 0.0D);
        renderClipped(ObjPheoDoorModels.VEHICLE_DOOR, "Left", ObjPheoDoorModels.VEHICLE_DOOR_TEXTURE, stack, buffer,
                activityLight, overlay, 1.0D, 0.0D, 0.0D, 3.4375D);
        stack.popPose();
        stack.pushPose();
        stack.translate(opening, 0.0D, 0.0D);
        renderClipped(ObjPheoDoorModels.VEHICLE_DOOR, "Right", ObjPheoDoorModels.VEHICLE_DOOR_TEXTURE, stack, buffer,
                activityLight, overlay, -1.0D, 0.0D, 0.0D, 3.4375D);
        stack.popPose();
        stack.popPose();
    }

    /**
     * Exact {@code RenderCargoDoor} part sequence.  The old BOT bus spans the
     * whole 60-tick transition (two blocks), while TOP waits for the first 30
     * ticks and then moves one block over the remaining 30 ticks.  On closing,
     * the same sequence runs backwards: TOP stays raised until {@code ticks}
     * reaches 30 and then descends.
     */
    private static void renderCargo(GenericDoorBlockEntity door, double ticks, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int activityLight, int overlay) {
        double botProgress = clamp(ticks / 60.0D, 0.0D, 1.0D);
        double topProgress = switch (door.state()) {
            case GenericDoorBlockEntity.STATE_OPENING -> clamp((ticks - 30.0D) / 30.0D, 0.0D, 1.0D);
            case GenericDoorBlockEntity.STATE_CLOSING -> clamp(ticks / 30.0D, 0.0D, 1.0D);
            case GenericDoorBlockEntity.STATE_OPEN -> 1.0D;
            default -> 0.0D;
        };
        ResourceLocation texture = ObjPheoDoorModels.CARGO_DOOR_TEXTURE;
        renderPart(ObjPheoDoorModels.CARGO_DOOR, "Frame", texture, stack, buffer, light, overlay);
        stack.pushPose();
        stack.translate(0.0D, topProgress, 0.0D);
        renderPart(ObjPheoDoorModels.CARGO_DOOR, "DoorTop", texture, stack, buffer, activityLight, overlay);
        stack.popPose();
        stack.pushPose();
        stack.translate(0.0D, botProgress * 2.0D, 0.0D);
        renderPart(ObjPheoDoorModels.CARGO_DOOR, "DoorBot", texture, stack, buffer, activityLight, overlay);
        stack.popPose();
    }

    private static void renderPart(LegacyWavefrontModel model, String part, ResourceLocation texture, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int overlay) {
        model.renderPart(part, texture, stack, buffer, light, overlay, 255, 255, 255, 255, false,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, LegacyWavefrontModel.UvTransform.DEFAULT);
    }

    private static void renderClipped(LegacyWavefrontModel model, String part, ResourceLocation texture, PoseStack stack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int light, int overlay,
            double x, double y, double z, double d) {
        model.renderPartClipped(part, texture, stack, buffer, light, overlay, 255, 255, 255, 255, false,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, LegacyWavefrontModel.UvTransform.DEFAULT, x, y, z, d);
    }

    private void updateAudio(GenericDoorBlockEntity door, String id) {
        AudioState state = audio.computeIfAbsent(door, ignored -> new AudioState());
        byte current = door.state();
        boolean sameTransition = state.lastState == current
                && (current == GenericDoorBlockEntity.STATE_OPENING || current == GenericDoorBlockEntity.STATE_CLOSING);
        if (state.lastState != current) {
            if (state.lastState == GenericDoorBlockEntity.STATE_OPENING && current == GenericDoorBlockEntity.STATE_OPEN) {
                playEnd(door, id, true);
            } else if (state.lastState == GenericDoorBlockEntity.STATE_CLOSING && current == GenericDoorBlockEntity.STATE_CLOSED) {
                playEnd(door, id, false);
            } else if (current == GenericDoorBlockEntity.STATE_OPENING) {
                playStart(door, id, true);
            } else if (current == GenericDoorBlockEntity.STATE_CLOSING) {
                playStart(door, id, false);
            }
            state.lastState = current;
        }
        if ("vault_door".equals(id) && sameTransition) {
            playVaultMilestones(door, state.lastOpenTicks, door.openTicks());
        }
        state.lastOpenTicks = door.openTicks();
        boolean transitioning = current == GenericDoorBlockEntity.STATE_OPENING
                || current == GenericDoorBlockEntity.STATE_CLOSING;
        ResourceLocation primary = transitioning ? loopSound(id, current == GenericDoorBlockEntity.STATE_OPENING) : null;
        // TileEntityDoorGeneric owns two independent AudioWrappers.  Fire uses
        // alarm6 as its second loop; the old sliding-blast declaration also
        // deliberately supplied the same movement loop twice, so preserve
        // that source behaviour instead of collapsing it into one instance.
        ResourceLocation secondary = transitioning ? switch (id) {
            case "fire_door" -> ModSounds.DOOR_ALARM6.get().getLocation();
            case "sliding_blast_door" -> ModSounds.DOOR_SLIDING_DOOR_OPENING.get().getLocation();
            default -> null;
        } : null;
        state.primary = LegacyMachineAudio.updateLoop(state.primary, door, primary, transitioning,
                AUDIO_DISTANCE, 10.0F, volume(id), 1.0F);
        state.secondary = LegacyMachineAudio.updateLoop(state.secondary, door, secondary, transitioning,
                AUDIO_DISTANCE, 10.0F, volume(id), 1.0F);
    }

    private static ResourceLocation loopSound(String id, boolean opening) {
        return switch (id) {
            case "fire_door", "qe_containment" -> ModSounds.DOOR_WGH_START.get().getLocation();
            case "sliding_blast_door" -> ModSounds.DOOR_SLIDING_DOOR_OPENING.get().getLocation();
            case "secure_access_door", "round_airlock_door", "large_vehicle_door", "cargo_door" -> ModSounds.DOOR_GARAGE_MOVE.get().getLocation();
            case "qe_sliding_door" -> ModSounds.DOOR_QE_SLIDING_OPENING.get().getLocation();
            case "water_door", "silo_hatch", "silo_hatch_large" -> ModSounds.DOOR_WGH_BIG_START.get().getLocation();
            default -> null;
        };
    }

    private static void playStart(GenericDoorBlockEntity door, String id, boolean opening) {
        if (opening && "sliding_seal_door".equals(id)) {
            play(door, ModSounds.DOOR_SLIDING_SEAL_OPEN.get().getLocation(), 2.0F);
        } else if (opening && "water_door".equals(id)) {
            play(door, ModSounds.DOOR_LEVER.get().getLocation(), 2.0F);
        }
    }

    private static void playEnd(GenericDoorBlockEntity door, String id, boolean opening) {
        ResourceLocation sound = switch (id) {
            case "fire_door", "qe_containment" -> ModSounds.DOOR_WGH_STOP.get().getLocation();
            case "sliding_blast_door" -> opening ? ModSounds.DOOR_SLIDING_DOOR_OPENED.get().getLocation()
                    : ModSounds.DOOR_SLIDING_DOOR_SHUT.get().getLocation();
            case "sliding_seal_door" -> ModSounds.DOOR_SLIDING_SEAL_STOP.get().getLocation();
            case "secure_access_door", "round_airlock_door", "large_vehicle_door", "cargo_door" -> ModSounds.DOOR_GARAGE_STOP.get().getLocation();
            case "qe_sliding_door" -> opening ? ModSounds.DOOR_QE_SLIDING_OPENED.get().getLocation()
                    : ModSounds.DOOR_QE_SLIDING_SHUT.get().getLocation();
            case "water_door" -> ModSounds.DOOR_LEVER.get().getLocation();
            case "silo_hatch", "silo_hatch_large" -> ModSounds.DOOR_WGH_BIG_STOP.get().getLocation();
            default -> null;
        };
        play(door, sound, volume(id));
    }

    private static void play(GenericDoorBlockEntity door, ResourceLocation sound, float volume) {
        LegacyMachineAudio.playLocal(door, sound, volume, 1.0F, AUDIO_DISTANCE);
    }

    /** Exact {@code DoorDecl.VAULT_DOOR#onDoorUpdate} milestone sounds. */
    private static void playVaultMilestones(GenericDoorBlockEntity door, int previousTicks, int currentTicks) {
        if (door.state() == GenericDoorBlockEntity.STATE_OPENING && previousTicks <= 0 && currentTicks > 0) {
            play(door, ModSounds.BLOCK_VAULT_SCRAPE_NEW.get().getLocation(), 1.0F);
        }
        if (door.state() == GenericDoorBlockEntity.STATE_CLOSING && previousTicks > 30 && currentTicks <= 30) {
            play(door, ModSounds.BLOCK_VAULT_SCRAPE_NEW.get().getLocation(), 1.0F);
        }
        for (int tick = 45; tick <= 115; tick += 10) {
            boolean crossed = door.state() == GenericDoorBlockEntity.STATE_OPENING
                    ? previousTicks < tick && currentTicks >= tick
                    : previousTicks > tick && currentTicks <= tick;
            if (crossed) {
                play(door, ModSounds.BLOCK_VAULT_THUD_NEW.get().getLocation(), 1.0F);
            }
        }
    }

    private static float volume(String id) {
        return "vault_door".equals(id) ? 1.0F : 2.0F;
    }

    private static double smoothstep(double value) {
        double t = clamp(value, 0.0D, 1.0D);
        return t * t * (3.0D - 2.0D * t);
    }

    private static double smoothSin(double value) {
        return Math.sin(clamp(value, 0.0D, 1.0D) * Math.PI * 0.5D);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class AudioState {
        private byte lastState = GenericDoorBlockEntity.STATE_CLOSED;
        private int lastOpenTicks;
        private Object primary;
        private Object secondary;
    }
}
