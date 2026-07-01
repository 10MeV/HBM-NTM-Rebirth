package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
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
import java.util.ArrayList;
import java.util.List;
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
        Vec3 normal = new Vec3(vx, vy, vz).normalize().scale(1.5D);
        Vec3 beam = new Vec3(vx - normal.x, vy - normal.y, vz - normal.z);
        double yaw = Math.toDegrees(Math.atan2(beam.x, beam.z));
        double horizontal = Math.sqrt(beam.x * beam.x + beam.z * beam.z);
        double pitch = Math.toDegrees(Math.atan2(beam.y, horizontal));

        poseStack.pushPose();
        poseStack.translate(0.5D, -1.0D, 0.5D);
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            renderModelPart("Base", ObjMachineModels.MINING_LASER_BASE_TEXTURE, poseStack, buffer, modelLight,
                    packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);

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
            poseStack.translate(normal.x, normal.y - 1.0D, normal.z);
            int range = (int) Math.ceil(beam.length() * 0.5D);
            int ticks = blockEntity.getLevel() == null ? 0 : (int) blockEntity.getLevel().getGameTime();
            List<LegacyBeamRenderer.BeamPlan> beams = new ArrayList<>(3);
            for (int offset = 0; offset < 360; offset += 120) {
                beams.add(LegacyBeamRenderer.beamPlan(beam.x, beam.y, beam.z,
                        LegacyBeamRenderer.WaveType.SPIRAL,
                        LegacyBeamRenderer.BeamType.SOLID,
                        0xA00000, 0xA00000,
                        ticks * -25 + offset, Math.max(1, range * 2), 0.075F, 3, 0.025F));
            }
            LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, queuedPose -> {
                for (LegacyBeamRenderer.BeamPlan beamPlan : beams) {
                    LegacyBeamRenderer.beam(queuedPose, buffer, beamPlan);
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
