package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.MiningLaserBlockEntity;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjModelLibrary;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MiningLaserRenderer implements BlockEntityRenderer<MiningLaserBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjModelLibrary.MACHINE_MINING_LASER;
    private static final LegacyWavefrontModel.SelectionHandle BASE =
            MODEL.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle PIVOT =
            MODEL.prepareRenderOnlyInCallOrder("Pivot");
    private static final LegacyWavefrontModel.SelectionHandle LASER =
            MODEL.prepareRenderOnlyInCallOrder("Laser");

    public MiningLaserRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(MiningLaserBlockEntity blockEntity) {
        return false;
    }

    @Override
    public boolean shouldRender(MiningLaserBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(MiningLaserBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }
        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);

        double tx = Mth.lerp(partialTick, blockEntity.getLastTargetX(), blockEntity.getTargetX());
        double ty = Mth.lerp(partialTick, blockEntity.getLastTargetY(), blockEntity.getTargetY());
        double tz = Mth.lerp(partialTick, blockEntity.getLastTargetZ(), blockEntity.getTargetZ());
        double vx = tx - blockEntity.getBlockPos().getX();
        double vy = ty - blockEntity.getBlockPos().getY() + 3.0D;
        double vz = tz - blockEntity.getBlockPos().getZ();
        double targetLength = Math.sqrt(vx * vx + vy * vy + vz * vz);
        double normalX = 0.0D;
        double normalY = 0.0D;
        double normalZ = 0.0D;
        if (targetLength >= 1.0E-4D) {
            double normalScale = 1.5D / targetLength;
            normalX = vx * normalScale;
            normalY = vy * normalScale;
            normalZ = vz * normalScale;
        }
        double beamX = vx - normalX;
        double beamY = vy - normalY;
        double beamZ = vz - normalZ;
        double yaw = Math.toDegrees(Math.atan2(beamX, beamZ));
        double horizontal = Math.sqrt(beamX * beamX + beamZ * beamZ);
        double pitch = Math.toDegrees(Math.atan2(beamY, horizontal));
        double beamLength = Math.sqrt(beamX * beamX + beamY * beamY + beamZ * beamZ);

        poseStack.pushPose();
        poseStack.translate(0.5D, -1.0D, 0.5D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            if (LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
                renderModelPart("Base", ObjMachineModels.MINING_LASER_BASE_TEXTURE, poseStack, buffer, modelLight,
                        packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
            }

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees((float) yaw));
            renderModelPart("Pivot", ObjMachineModels.MINING_LASER_PIVOT_TEXTURE, poseStack, buffer, modelLight,
                    packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees((float) yaw));
            poseStack.translate(0.0D, -1.0D, 0.0D);
            poseStack.mulPose(Axis.XN.rotationDegrees((float) pitch + 90.0F));
            poseStack.translate(0.0D, 1.0D, 0.0D);
            renderModelPart("Laser", ObjMachineModels.MINING_LASER_LASER_TEXTURE, poseStack, buffer, modelLight,
                    packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
            poseStack.popPose();
        }

        if (blockEntity.hasBeam()) {
            poseStack.translate(normalX, normalY - 1.0D, normalZ);
            int range = (int) Math.ceil(beamLength * 0.5D);
            int start = blockEntity.getLevel() == null ? 0
                    : (int) (blockEntity.getLevel().getGameTime() * -25L % 360L);
            int segments = Math.max(1, range * 2);
            LegacyMachineEffectPresenter.enqueueSolidBeamGroup(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, buffer,
                    false, beams -> {
                for (int offset = 0; offset < 360; offset += 120) {
                    beams.add(beamX, beamY, beamZ,
                        LegacyBeamRenderer.WaveType.SPIRAL,
                        0xA00000, 0xA00000,
                        start + offset, segments, 0.075F, 3, 0.025F);
                }
            });
        }
        poseStack.popPose();
    }

    private static void renderModelPart(String partName, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle = handle(partName);
        if (handle != null) {
            MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle, renderMode);
            return;
        }
        MODEL.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay,
            LegacyTexturedRenderMode renderMode) {
        LegacyWavefrontModel.SelectionHandle handle = sameModel(model) ? handle(partName) : null;
        if (handle != null) {
            MODEL.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay, handle, renderMode);
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    private static boolean sameModel(LegacyWavefrontModel model) {
        return model == MODEL || model.modelLocation().equals(MODEL.modelLocation());
    }

    private static LegacyWavefrontModel.SelectionHandle handle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Base" -> BASE;
            case "Pivot" -> PIVOT;
            case "Laser" -> LASER;
            default -> null;
        };
    }
}
