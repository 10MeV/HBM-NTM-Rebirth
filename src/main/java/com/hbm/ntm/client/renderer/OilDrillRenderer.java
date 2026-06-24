package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.blockentity.OilDrillBlockEntity;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class OilDrillRenderer implements BlockEntityRenderer<OilDrillBlockEntity> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();
    private static final String[] FRACKING_PIPE_PARTS = { "pX", "nX", "pZ", "nZ" };
    private static final LegacyWavefrontModel.SelectionHandle FRACKING_PIPE_HANDLE =
            ObjBlockModels.PIPE_NEO.prepareRenderOnlyInCallOrder(FRACKING_PIPE_PARTS);
    private static final LegacyWavefrontModel.SelectionHandle PUMPJACK_BASE =
            ObjMachineModels.PUMPJACK_LEGACY.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle PUMPJACK_ROTOR =
            ObjMachineModels.PUMPJACK_LEGACY.prepareRenderOnlyInCallOrder("Rotor");
    private static final LegacyWavefrontModel.SelectionHandle PUMPJACK_HEAD =
            ObjMachineModels.PUMPJACK_LEGACY.prepareRenderOnlyInCallOrder("Head");
    private static final LegacyWavefrontModel.SelectionHandle PUMPJACK_CARRIAGE =
            ObjMachineModels.PUMPJACK_LEGACY.prepareRenderOnlyInCallOrder("Carriage");

    public OilDrillRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(OilDrillBlockEntity blockEntity) {
        return false;
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
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        if (drill.getKind() == OilDrillBlockEntity.Kind.PUMPJACK) {
            renderPumpjack(drill, partialTick, poseStack, buffer, modelLight, packedOverlay, definition, model);
        } else {
            if (definition.renderAll()) {
                model.renderAll(definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
            } else {
                ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);
                for (String part : definition.renderParts()) {
                    renderModelPart(model, part, definition.textureLocation(), context);
                }
            }
            if (drill.getKind() == OilDrillBlockEntity.Kind.FRACKING_TOWER) {
                renderFrackingPipes(state, poseStack, buffer, modelLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    private static void renderFrackingPipes(BlockState state, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        ObjBlockModels.PIPE_NEO.renderOnlyInCallOrder(ObjBlockModels.PIPE_SILVER_TEXTURE, context,
                FRACKING_PIPE_HANDLE);
        poseStack.popPose();
    }

    private static void renderPumpjack(OilDrillBlockEntity drill, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, LegacyMachineDefinition definition,
            LegacyWavefrontModel model) {
        float rotation = Mth.lerp(partialTick, drill.getPreviousPumpjackRotation(), drill.getPumpjackRotation());
        LegacyTileRenderPlans.PumpjackPlan plan = LegacyTileRenderPlans.pumpjackPlan(rotation);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, drill.getBlockState(), packedLight, packedOverlay);
        renderModelPart(model, "Base", definition.textureLocation(), context);

        renderRotatingPart(model, plan.rotor(), definition.textureLocation(), poseStack, context);
        renderRotatingPart(model, plan.head(), definition.textureLocation(), poseStack, context);
        renderTranslatedPart(model, plan.carriage(), definition.textureLocation(), poseStack, context);
        renderPumpjackRods(plan, poseStack, buffer);
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, ObjRenderContext context) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        if (part.axisX() != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) (part.angleDegrees() * part.axisX())));
        }
        if (part.axisY() != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (part.angleDegrees() * part.axisY())));
        }
        if (part.axisZ() != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (part.angleDegrees() * part.axisZ())));
        }
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        renderModelPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    private static void renderTranslatedPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.TranslatedModelPartPlan part, ResourceLocation texture,
            PoseStack poseStack, ObjRenderContext context) {
        if (!part.active()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(part.translateX(), part.translateY(), part.translateZ());
        renderModelPart(model, part.partName(), texture, context);
        poseStack.popPose();
    }

    public static void renderModelPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = pumpjackHandle(model, partName);
        if (handle != null) {
            ObjMachineModels.PUMPJACK_LEGACY.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        model.renderPart(partName, texture, context);
    }

    private static LegacyWavefrontModel.SelectionHandle pumpjackHandle(LegacyWavefrontModel model, String partName) {
        if (!isPumpjackModel(model)) {
            return null;
        }
        return switch (partName) {
            case "Base" -> PUMPJACK_BASE;
            case "Rotor" -> PUMPJACK_ROTOR;
            case "Head" -> PUMPJACK_HEAD;
            case "Carriage" -> PUMPJACK_CARRIAGE;
            default -> null;
        };
    }

    private static boolean isPumpjackModel(LegacyWavefrontModel model) {
        return model == ObjMachineModels.PUMPJACK_LEGACY
                || model.modelLocation().equals(ObjMachineModels.PUMPJACK_LEGACY.modelLocation());
    }

    private static void renderPumpjackRods(LegacyTileRenderPlans.PumpjackPlan plan, PoseStack poseStack,
            MultiBufferSource buffer) {
        VertexConsumer consumer = LegacyUntexturedQuadRenderer.solid(buffer);
        PoseStack.Pose pose = poseStack.last();
        for (LegacyTileRenderPlans.UntexturedQuadPlan quad : plan.rods()) {
            for (LegacyTileRenderPlans.UntexturedVertexPlan vertex : quad.vertices()) {
                LegacyTileRenderPlans.RgbaPlan color = vertex.color();
                LegacyUntexturedQuadRenderer.vertexRgbaF(consumer, pose,
                        vertex.x(), vertex.y(), vertex.z(),
                        color.red(), color.green(), color.blue(), color.alpha());
            }
        }
    }
}
