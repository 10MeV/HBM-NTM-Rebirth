package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachinePartRenderProperties;
import com.hbm.ntm.block.LegacyMachineRenderProfile;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.Map;

public class LegacyVisibleMachineRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();

    public LegacyVisibleMachineRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block)) {
            return;
        }

        LegacyMachineDefinition definition = block.definition();
        int modelLight = LegacyRenderLighting.resolveMachineLight(blockEntity, state, definition, packedLight);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()));

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

        if (definition.renderAll()) {
            model.renderAll(definition.textureLocation(), poseStack, buffer, modelLight, packedOverlay);
        } else {
            ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);
            if (!renderProfile(definition, model, context, poseStack)) {
                renderParts(definition, model, context);
            }
        }

        poseStack.popPose();
    }

    private static void renderParts(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        for (boolean translucentPass : new boolean[] { false, true }) {
            for (String part : definition.renderParts()) {
                LegacyMachinePartRenderProperties properties = definition.partRenderProperties().get(part);
                if (LegacyMachinePartRenderContexts.translucent(properties) != translucentPass) {
                    continue;
                }
                model.renderPart(part, definition.partTextures().getOrDefault(part, definition.textureLocation()),
                        LegacyMachinePartRenderContexts.apply(context, properties));
            }
        }
    }

    private static boolean renderProfile(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack) {
        long currentMillis;
        switch (definition.renderProfile()) {
            case ANNIHILATOR_UV_SCROLL -> {
                currentMillis = System.currentTimeMillis();
                model.renderPart("Annihilator", definition.textureLocation(), context);
                renderRotatingPart(model, LegacyTileRenderPlans.annihilatorRollerPlan(currentMillis), context, poseStack);
                LegacyTileRenderPlans.TextureMatrixPartPlan belt = LegacyTileRenderPlans.annihilatorBeltPlan(currentMillis);
                model.renderPart(belt.partName(), definition.partTextures().getOrDefault(belt.partName(),
                        definition.textureLocation()), context.withTextureMatrixPlan(belt.textureMatrix()));
                return true;
            }
            case RADGEN_STATIC_SPECIAL -> {
                renderRadgenStatic(definition, model, context);
                return true;
            }
            case BATTERY_REDD_STATIC_SPECIAL -> {
                renderBatteryReddStatic(definition, model, context);
                return true;
            }
            case CRYSTALLIZER_STATIC_SPECIAL -> {
                renderCrystallizerStatic(definition, model, context);
                return true;
            }
            case ARC_FURNACE_STATIC_PREVIEW -> {
                LegacyArcFurnaceRenderHelper.renderPlan(model,
                        LegacyTileRenderPlans.arcFurnaceStaticPreviewPlan(), context, poseStack);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static void renderRadgenStatic(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        model.renderPart("Base", definition.textureLocation(), context);
        model.renderPart("Rotor", definition.textureLocation(), context);
        renderTintedPart(model, definition.textureLocation(), LegacyTileRenderPlans.radgenLightPlan(false), context);
        renderTintedPart(model, definition.textureLocation(), LegacyTileRenderPlans.radgenGlassPlan(), context);
        model.renderPart("Glass", definition.textureLocation(), context);
    }

    private static void renderBatteryReddStatic(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        model.renderPart("Base", definition.textureLocation(), context);
        model.renderPart("Wheel", definition.textureLocation(), context);
        model.renderPart("Lights", definition.textureLocation(), context.fullBright());
    }

    private static void renderCrystallizerStatic(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        model.renderPart("Body", definition.textureLocation(), context);
        model.renderPart("Spinner", definition.textureLocation(), context);
        renderTintedPart(model, definition.textureLocation(), LegacyTileRenderPlans.crystallizerFluidPlan(false),
                context);
    }

    private static void renderTintedPart(LegacyWavefrontModel model, net.minecraft.resources.ResourceLocation texture,
            LegacyTileRenderPlans.ModelPartTintPlan plan, ObjRenderContext context) {
        if (!plan.active()) {
            return;
        }
        ObjRenderContext resolved = applyTintPlan(context, plan);
        if (plan.textured()) {
            model.renderPart(plan.partName(), texture, resolved);
        } else {
            model.renderPartUntextured(plan.partName(), resolved);
        }
    }

    private static ObjRenderContext applyTintPlan(ObjRenderContext context,
            LegacyTileRenderPlans.ModelPartTintPlan plan) {
        ObjRenderContext resolved = context;
        if (plan.blend() != null) {
            resolved = resolved.withRenderMode(plan.blend().modernRenderMode());
        }
        if (plan.color() != null) {
            resolved = resolved.withRgba(plan.color().redByte(), plan.color().greenByte(),
                    plan.color().blueByte(), plan.color().alphaByte());
        }
        if (plan.fullbright() != null) {
            resolved = resolved.withLegacyLightmap(plan.fullbright().lightmapX(), plan.fullbright().lightmapY());
        }
        return resolved;
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        model.renderPart(part.partName(), context);
        poseStack.popPose();
    }

    private static void rotate(PoseStack poseStack, float axisX, float axisY, float axisZ, double degrees) {
        if (axisX != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) (degrees * axisX)));
        }
        if (axisY != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (degrees * axisY)));
        }
        if (axisZ != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (degrees * axisZ)));
        }
    }
}
