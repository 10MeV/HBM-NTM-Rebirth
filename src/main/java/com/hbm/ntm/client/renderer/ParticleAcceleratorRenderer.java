package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.ParticleAcceleratorBlock;
import com.hbm.ntm.blockentity.PABeamlineBlockEntity;
import com.hbm.ntm.blockentity.PABlockEntity;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjParticleAcceleratorModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ParticleAcceleratorRenderer implements BlockEntityRenderer<PABlockEntity> {
    public ParticleAcceleratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(PABlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(PABlockEntity blockEntity, Vec3 cameraPos) {
        return hasBerVisuals(blockEntity)
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(PABlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ParticleAcceleratorBlock.Variant variant = blockEntity.getVariant();
        if (!hasBerVisuals(blockEntity)) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        poseStack.pushPose();
        poseStack.translate(0.5D, yOffset(variant), 0.5D);
        if (variant != ParticleAcceleratorBlock.Variant.DIPOLE) {
            LegacyPoseRotations.rotateYDegrees(poseStack, yaw(state));
        }

        LegacyWavefrontModel model = model(variant);
        ResourceLocation texture = texture(variant);
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            if (blockEntity instanceof PABeamlineBlockEntity beamline) {
                renderBeamline(beamline, partialTick, texture, poseStack, buffer, modelLight, packedOverlay,
                        LegacyMachineRenderShapes.renderChunkBakedStaticsInBer());
            } else {
                model.renderAll(texture, poseStack, buffer, modelLight, packedOverlay,
                        LegacyTexturedRenderMode.CUTOUT_CULL);
            }
        }
        poseStack.popPose();
    }

    private static void renderBeamline(PABeamlineBlockEntity beamline, float partialTick, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            boolean renderStaticBody) {
        if (renderStaticBody) {
            ObjParticleAcceleratorModels.renderBeamlinePart(beamline.hasWindow() ? "BeamlineWindow" : "Beamline",
                    texture, poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_CULL);
        }
        if (!beamline.hasWindow()) {
            return;
        }
        float flash = Math.max(0.0F, beamline.getFlash(partialTick));
        if (flash > 0.0F) {
            int color = Math.min(255, (int) (230.0F * flash));
            LegacyMachineEffectPresenter.enqueueUntexturedObjPart(PresentStage.AFTER_BLOCK_ENTITIES,
                    poseStack, buffer, ObjParticleAcceleratorModels.BEAMLINE,
                    ObjParticleAcceleratorModels.beamlineGlassHandle(), color, color, 255, 180,
                    LegacyTexturedRenderMode.ADDITIVE_CULL_NO_DEPTH_WRITE);
        }
    }

    private static double yOffset(ParticleAcceleratorBlock.Variant variant) {
        return switch (variant) {
            case BEAMLINE -> 0.0D;
            case DETECTOR -> -2.0D;
            default -> -1.0D;
        };
    }

    private static float yaw(BlockState state) {
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        return switch (facing) {
            case NORTH -> 90.0F;
            case EAST -> 0.0F;
            case SOUTH -> 270.0F;
            case WEST -> 180.0F;
            default -> 0.0F;
        };
    }

    private static LegacyWavefrontModel model(ParticleAcceleratorBlock.Variant variant) {
        return switch (variant) {
            case SOURCE -> ObjParticleAcceleratorModels.SOURCE;
            case BEAMLINE -> ObjParticleAcceleratorModels.BEAMLINE;
            case RFC -> ObjParticleAcceleratorModels.RFC;
            case QUADRUPOLE -> ObjParticleAcceleratorModels.QUADRUPOLE;
            case DIPOLE -> ObjParticleAcceleratorModels.DIPOLE;
            case DETECTOR -> ObjParticleAcceleratorModels.DETECTOR;
        };
    }

    private static ResourceLocation texture(ParticleAcceleratorBlock.Variant variant) {
        return switch (variant) {
            case SOURCE -> ObjParticleAcceleratorModels.SOURCE_TEXTURE;
            case BEAMLINE -> ObjParticleAcceleratorModels.BEAMLINE_TEXTURE;
            case RFC -> ObjParticleAcceleratorModels.RFC_TEXTURE;
            case QUADRUPOLE -> ObjParticleAcceleratorModels.QUADRUPOLE_TEXTURE;
            case DIPOLE -> ObjParticleAcceleratorModels.DIPOLE_TEXTURE;
            case DETECTOR -> ObjParticleAcceleratorModels.DETECTOR_TEXTURE;
        };
    }

    private static boolean hasBerVisuals(PABlockEntity blockEntity) {
        if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
            return true;
        }
        return blockEntity instanceof PABeamlineBlockEntity beamline
                && beamline.hasWindow()
                && Math.max(beamline.getFlash(0.0F), beamline.getFlash(1.0F)) > 0.0F;
    }
}
