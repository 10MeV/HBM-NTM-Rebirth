package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.OilDrillBlockEntity;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class OilDrillRenderer implements BlockEntityRenderer<OilDrillBlockEntity> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();

    public OilDrillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(OilDrillBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(OilDrillBlockEntity drill, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = drill.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(drill, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()));

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        if (drill.getKind() == OilDrillBlockEntity.Kind.PUMPJACK) {
            renderPumpjack(drill, partialTick, poseStack, buffer, modelLight, packedOverlay, definition, model);
        } else if (definition.renderAll()) {
            model.renderAll(definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        } else {
            for (String part : definition.renderParts()) {
                model.renderPart(part, definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    private static void renderPumpjack(OilDrillBlockEntity drill, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyMachineDefinition definition,
            LegacyWavefrontModel model) {
        float rotation = Mth.lerp(partialTick, drill.getPreviousPumpjackRotation(), drill.getPumpjackRotation());
        model.renderPart("Base", definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay);

        poseStack.pushPose();
        LegacyObjTransforms.rotateAroundX(poseStack, 0.0D, 1.5D, -5.5D, rotation - 90.0F);
        model.renderPart("Rotor", definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        LegacyObjTransforms.rotateAroundX(poseStack, 0.0D, 3.5D, -3.5D,
                (float) Math.toDegrees(Math.sin(Math.toRadians(rotation))) * 0.25F);
        model.renderPart("Head", definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, -Math.sin(Math.toRadians(rotation)), 0.0D);
        model.renderPart("Carriage", definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        renderPumpjackRods(rotation, poseStack, buffer);
    }

    private static void renderPumpjackRods(float rotation, PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer consumer = LegacyUntexturedQuadRenderer.solid(buffer);
        PoseStack.Pose pose = poseStack.last();
        double sin = Math.sin(Math.toRadians(rotation));
        Vec3 backPos = rotateX(new Vec3(0.0D, 0.0D, -2.0D), -sin * 0.25D);
        Vec3 rot = rotateX(new Vec3(0.0D, 0.5D, 0.0D), -Math.toRadians(rotation - 90.0D));

        for (int i = -1; i <= 1; i += 2) {
            LegacyUntexturedQuadRenderer.quad(consumer, pose,
                    0.53125D * i, 1.5D + rot.y, -5.5D + rot.z - 0.0625D,
                    0.53125D * i, 1.5D + rot.y, -5.5D + rot.z + 0.0625D,
                    0.53125D * i, 3.5D + backPos.y, -3.5D + backPos.z + 0.0625D,
                    0.53125D * i, 3.5D + backPos.y, -3.5D + backPos.z - 0.0625D,
                    128, 128, 128, 255, 255, 255, 255);
        }

        double pd = 0.03125D;
        double width = 0.25D;
        double height = -sin;
        for (int i = -1; i <= 1; i += 2) {
            double pRot = -sin * 0.25D;
            Vec3 frontPos = rotateX(new Vec3(0.0D, 0.0D, 1.0D), pRot);
            double dist = 0.03125D;
            Vec3 frontRad = rotateX(new Vec3(0.0D, 0.0D, 2.5D + dist), pRot);
            double cutlet = 360.0D / 32.0D;
            frontRad = rotateX(frontRad, -Math.toRadians(cutlet * -3.0D));
            for (int j = 0; j < 4; j++) {
                Vec3 start = fixedFrontRodPoint(frontPos, frontRad, dist);
                Vec3 nextRad = rotateX(frontRad, -Math.toRadians(cutlet));
                Vec3 end = fixedFrontRodPoint(frontPos, nextRad, dist);
                LegacyUntexturedQuadRenderer.quad(consumer, pose,
                        (width - pd) * i, 3.5D + start.y, -3.5D + start.z,
                        (width + pd) * i, 3.5D + start.y, -3.5D + start.z,
                        (width + pd) * i, 3.5D + end.y, -3.5D + end.z,
                        (width - pd) * i, 3.5D + end.y, -3.5D + end.z,
                        51, 51, 51, 255, 255, 255, 255);
                frontRad = nextRad;
            }
            Vec3 tail = fixedFrontRodPoint(frontPos, frontRad, dist);
            LegacyUntexturedQuadRenderer.quad(consumer, pose,
                    (width + pd) * i, 3.5D + tail.y, -3.5D + tail.z,
                    (width - pd) * i, 3.5D + tail.y, -3.5D + tail.z,
                    (width - pd) * i, 2.0D + height, 0.0D,
                    (width + pd) * i, 2.0D + height, 0.0D,
                    51, 51, 51, 255, 255, 255, 255);
        }

        double p = 0.03125D;
        LegacyUntexturedQuadRenderer.quad(consumer, pose,
                p, height + 1.5D, p,
                -p, height + 1.5D, -p,
                -p, 0.75D, -p,
                p, 0.75D, p,
                51, 51, 51, 255, 255, 255, 255);
        LegacyUntexturedQuadRenderer.quad(consumer, pose,
                -p, height + 1.5D, p,
                p, height + 1.5D, -p,
                p, 0.75D, -p,
                -p, 0.75D, p,
                51, 51, 51, 255, 255, 255, 255);
    }

    private static Vec3 fixedFrontRodPoint(Vec3 frontPos, Vec3 frontRad, double dist) {
        double y = frontPos.y + frontRad.y;
        double z = frontPos.z + frontRad.z;
        if (frontRad.y < 0.0D) {
            z = 3.5D + dist * 0.5D;
        }
        return new Vec3(0.0D, y, z);
    }

    private static Vec3 rotateX(Vec3 vec, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(vec.x, vec.y * cos + vec.z * sin, vec.z * cos - vec.y * sin);
    }
}
