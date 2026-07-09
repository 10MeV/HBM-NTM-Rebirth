package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyDangerDiamondRenderer;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.hbm.ntm.fluid.FluidSymbol;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.CorrosiveFluidTrait;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LegacyFluidTankRenderHelper {
    private static final ResourceLocation TANK_FRAME_TEXTURE = ObjMachineModels.LEGACY_FLUIDTANK_FRAME_TEXTURE;
    private static final ResourceLocation TANK_INNER_TEXTURE = ObjMachineModels.LEGACY_FLUIDTANK_INNER_TEXTURE;
    private static final Map<String, ResourceLocation> TANK_TEXTURES_BY_NAME = new ConcurrentHashMap<>();
    private static final ResourceLocation TANK_NONE_TEXTURE = tankTextureCached("NONE");
    private static final ResourceLocation TANK_DANGER_TEXTURE = tankTextureCached("DANGER");
    private static final LegacyWavefrontModel NORMAL_MODEL = ObjMachineModels.FLUIDTANK;
    private static final LegacyWavefrontModel EXPLODED_MODEL = ObjMachineModels.FLUIDTANK_EXPLODED;
    private static final LegacyWavefrontModel.SelectionHandle NORMAL_FRAME =
            NORMAL_MODEL.prepareRenderOnlyInCallOrder("Frame");
    private static final LegacyWavefrontModel.SelectionHandle NORMAL_TANK =
            NORMAL_MODEL.prepareRenderOnlyInCallOrder("Tank");
    private static final LegacyWavefrontModel.SelectionHandle EXPLODED_FRAME =
            EXPLODED_MODEL.prepareRenderOnlyInCallOrder("Frame");
    private static final LegacyWavefrontModel.SelectionHandle EXPLODED_TANK_INNER =
            EXPLODED_MODEL.prepareRenderOnlyInCallOrder("TankInner");
    private static final LegacyWavefrontModel.SelectionHandle EXPLODED_TANK =
            EXPLODED_MODEL.prepareRenderOnlyInCallOrder("Tank");

    private LegacyFluidTankRenderHelper() {
    }

    public static void renderSmallTank(LegacyWavefrontModel normalModel, LegacyWavefrontModel explodedModel,
            HbmFluidTank tank, boolean exploded, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        renderSmallTankBody(normalModel, explodedModel, tank, exploded, poseStack, buffer, packedLight, packedOverlay);
        renderSmallTankDiamonds(tank.getTankType(), poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderSmallTankBody(LegacyWavefrontModel normalModel, LegacyWavefrontModel explodedModel,
            HbmFluidTank tank, boolean exploded, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        FluidType type = tank.getTankType();
        if (exploded) {
            renderKnownTankPart(explodedModel, EXPLODED_FRAME, "Frame", TANK_FRAME_TEXTURE, poseStack, buffer,
                    packedLight, packedOverlay, 0xFFFFFF);
            renderKnownTankPart(explodedModel, EXPLODED_TANK_INNER, "TankInner", TANK_INNER_TEXTURE, poseStack, buffer,
                    packedLight, packedOverlay, 0xFFFFFF);
            renderTankPart(explodedModel, "Tank", type, poseStack, buffer, packedLight, packedOverlay);
        } else {
            renderKnownTankPart(normalModel, NORMAL_FRAME, "Frame", TANK_FRAME_TEXTURE, poseStack, buffer,
                    packedLight, packedOverlay, 0xFFFFFF);
            renderTankPart(normalModel, "Tank", type, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    public static void renderBigAssTankFluid(HbmFluidTank tank, BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, float animation) {
        if (tank.isEmpty() || tank.getMaxFill() <= 0) {
            return;
        }
        FluidType type = tank.getTankType();
        double height = (double) tank.getFill() * LegacyTileRenderPlans.BIG_ASS_TANK_FLUID_HEIGHT
                / (double) tank.getMaxFill();
        LegacyUvAnimation.Range u = LegacyUvAnimation.bigAssTankFluidU(animation);
        double fluidV = LegacyUvAnimation.bigAssTankFluidV(height);
        ResourceLocation texture = type == null ? HbmFluids.NONE.getTexture() : type.getTexture();
        double off = LegacyTileRenderPlans.BIG_ASS_TANK_FLUID_SIDE_OFFSET;
        double base = LegacyTileRenderPlans.BIG_ASS_TANK_FLUID_BASE_Y;
        double half = LegacyTileRenderPlans.BIG_ASS_TANK_FLUID_HALF_WIDTH;
        int alpha = 192;
        VertexConsumer consumer = LegacyTexturedQuadRenderer.vertexAlphaConsumer(texture, buffer,
                LegacyTexturedRenderMode.TRANSLUCENT_DEPTH_WRITE);
        PoseStack.Pose pose = poseStack.last();
        LegacyTexturedQuadRenderer.quadWithVertexAlpha(consumer, pose, packedLight, packedOverlay,
                1.0F, 0.0F, 0.0F,
                -off, base, -half, u.min(), 0.0D, alpha,
                -off, base + height, -half, u.min(), fluidV, alpha,
                -off, base + height, half, u.max(), fluidV, alpha,
                -off, base, half, u.max(), 0.0D, alpha,
                0xFFFFFF);
        LegacyTexturedQuadRenderer.quadWithVertexAlpha(consumer, pose, packedLight, packedOverlay,
                -1.0F, 0.0F, 0.0F,
                off, base, -half, u.max(), 0.0D, alpha,
                off, base + height, -half, u.max(), fluidV, alpha,
                off, base + height, half, u.min(), fluidV, alpha,
                off, base, half, u.min(), 0.0D, alpha,
                0xFFFFFF);
    }

    public static void renderBat9000Fluid(HbmFluidTank tank, BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tank.isEmpty() || tank.getMaxFill() <= 0) {
            return;
        }
        double height = (double) Math.max(0, tank.getFill()) * LegacyTileRenderPlans.BAT9000_FLUID_HEIGHT
                / (double) tank.getMaxFill();
        double off = LegacyTileRenderPlans.BAT9000_FLUID_OFFSET;
        double base = LegacyTileRenderPlans.BAT9000_FLUID_BASE_Y;
        double top = base + height;
        double half = LegacyTileRenderPlans.BAT9000_FLUID_HALF_WIDTH;
        int color = fluidColor(tank.getTankType());
        LegacyUntexturedQuadRenderer.QuadBatch batch = LegacyUntexturedQuadRenderer.quadBatch(poseStack, buffer,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        LegacyUntexturedQuadRenderer.quad(batch,
                -off, base, -half,
                -off, top, -half,
                -off, top, half,
                -off, base, half,
                color, 255, 255, 255, 255);
        LegacyUntexturedQuadRenderer.quad(batch,
                off, base, -half,
                off, top, -half,
                off, top, half,
                off, base, half,
                color, 255, 255, 255, 255);
        LegacyUntexturedQuadRenderer.quad(batch,
                -half, base, -off,
                -half, top, -off,
                half, top, -off,
                half, base, -off,
                color, 255, 255, 255, 255);
        LegacyUntexturedQuadRenderer.quad(batch,
                -half, base, off,
                -half, top, off,
                half, top, off,
                half, base, off,
                color, 255, 255, 255, 255);
    }

    public static void renderSmallTankDiamonds(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        renderDangerDiamondTransform(type, poseStack, buffer, packedLight, packedOverlay,
                -0.25D, 0.5D, -1.501D, 90.0F, 1.0F, 0.375F, 0.375F);
        renderDangerDiamondTransform(type, poseStack, buffer, packedLight, packedOverlay,
                0.25D, 0.5D, 1.501D, -90.0F, 1.0F, 0.375F, 0.375F);
    }

    public static void enqueueSmallTankDiamonds(PresentStage stage, FluidType type, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        enqueueDangerDiamondTransform(stage, type, poseStack, buffer, packedLight, packedOverlay,
                -0.25D, 0.5D, -1.501D, 90.0F, 1.0F, 0.375F, 0.375F);
        enqueueDangerDiamondTransform(stage, type, poseStack, buffer, packedLight, packedOverlay,
                0.25D, 0.5D, 1.501D, -90.0F, 1.0F, 0.375F, 0.375F);
    }

    public static void renderBigAssTankDiamonds(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        renderRadialDangerDiamonds(type, poseStack, buffer, packedLight, packedOverlay,
                2, 22.5F, 180.0F, 5.5D, 2.0D, 0.0D,
                1.0F, 1.0F, 1.0F);
    }

    public static void enqueueBigAssTankDiamonds(PresentStage stage, FluidType type, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        enqueueRadialDangerDiamonds(stage, type, poseStack, buffer, packedLight, packedOverlay,
                2, 22.5F, 180.0F, 5.5D, 2.0D, 0.0D,
                1.0F, 1.0F, 1.0F);
    }

    public static void renderBat9000Diamonds(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        renderRadialDangerDiamonds(type, poseStack, buffer, packedLight, packedOverlay,
                4, 45.0F, 90.0F, 2.5D, 2.25D, 0.0D,
                1.0F, 0.75F, 0.75F);
    }

    public static void enqueueBat9000Diamonds(PresentStage stage, FluidType type, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        enqueueRadialDangerDiamonds(stage, type, poseStack, buffer, packedLight, packedOverlay,
                4, 45.0F, 90.0F, 2.5D, 2.25D, 0.0D,
                1.0F, 0.75F, 0.75F);
    }

    public static void renderFluidBarrelDiamonds(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        renderRadialDangerDiamonds(type, poseStack, buffer, packedLight, packedOverlay,
                4, 0.0F, 90.0F, 0.4D, 0.3D, -0.24D,
                1.0F, 0.25F, 0.25F);
    }

    public static void enqueueFluidBarrelDiamonds(PresentStage stage, FluidType type, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        enqueueRadialDangerDiamonds(stage, type, poseStack, buffer, packedLight, packedOverlay,
                4, 0.0F, 90.0F, 0.4D, 0.3D, -0.24D,
                1.0F, 0.25F, 0.25F);
    }

    public static void renderDangerDiamonds(LegacyTileRenderPlans.TankDangerDiamondPlan plan, FluidType type,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        renderDangerDiamondPlan(plan, type, poseStack, buffer, packedLight, packedOverlay);
    }

    public static void enqueueDangerDiamonds(PresentStage stage, LegacyTileRenderPlans.TankDangerDiamondPlan plan,
            FluidType type, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        enqueueDangerDiamondPlan(stage, plan, type, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderRadialDangerDiamonds(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, int count, float startYaw, float yawStep,
            double translateX, double translateY, double translateZ,
            float scaleX, float scaleY, float scaleZ) {
        for (int i = 0; i < count; i++) {
            poseStack.pushPose();
            rotateYDegrees(poseStack, startYaw + yawStep * i);
            poseStack.translate(translateX, translateY, translateZ);
            poseStack.scale(scaleX, scaleY, scaleZ);
            renderDangerDiamond(type, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void enqueueRadialDangerDiamonds(PresentStage stage, FluidType type, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay, int count, float startYaw, float yawStep,
            double translateX, double translateY, double translateZ,
            float scaleX, float scaleY, float scaleZ) {
        for (int i = 0; i < count; i++) {
            poseStack.pushPose();
            rotateYDegrees(poseStack, startYaw + yawStep * i);
            poseStack.translate(translateX, translateY, translateZ);
            poseStack.scale(scaleX, scaleY, scaleZ);
            enqueueDangerDiamond(stage, type, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void renderDangerDiamondTransform(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, double translateX, double translateY, double translateZ,
            float yawDegrees, float scaleX, float scaleY, float scaleZ) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        rotateYDegrees(poseStack, yawDegrees);
        poseStack.scale(scaleX, scaleY, scaleZ);
        renderDangerDiamond(type, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void enqueueDangerDiamondTransform(PresentStage stage, FluidType type, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay,
            double translateX, double translateY, double translateZ,
            float yawDegrees, float scaleX, float scaleY, float scaleZ) {
        poseStack.pushPose();
        poseStack.translate(translateX, translateY, translateZ);
        rotateYDegrees(poseStack, yawDegrees);
        poseStack.scale(scaleX, scaleY, scaleZ);
        enqueueDangerDiamond(stage, type, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderTankPart(LegacyWavefrontModel model, String part, FluidType type,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int color = tankModelTint(type);
        ResourceLocation texture = tankTextureFor(type);
        LegacyWavefrontModel.SelectionHandle handle = model == NORMAL_MODEL && "Tank".equals(part)
                ? NORMAL_TANK
                : model == EXPLODED_MODEL && "Tank".equals(part) ? EXPLODED_TANK : null;
        renderKnownTankPart(model, handle, part, texture, poseStack, buffer, packedLight, packedOverlay, color);
    }

    private static void renderKnownTankPart(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle handle,
            String part, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, int color) {
        if (handle != null) {
            model.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, packedOverlay,
                    color >> 16 & 255, color >> 8 & 255, color & 255, 255, false, handle);
            return;
        }
        model.renderPart(part, texture, poseStack, buffer, packedLight, packedOverlay,
                color >> 16 & 255, color >> 8 & 255, color & 255, 255);
    }

    private static void renderDangerDiamond(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        LegacyDangerDiamondRenderer.render(poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE, type.getPoison(),
                type.getFlammability(), type.getReactivity(), dangerSymbol(type.getSymbol()));
    }

    private static void enqueueDangerDiamond(PresentStage stage, FluidType type, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyMachineEffectPresenter.enqueueDangerDiamond(stage, poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE, type.getPoison(),
                type.getFlammability(), type.getReactivity(), dangerSymbol(type.getSymbol()));
    }

    private static void renderDangerDiamondPlan(LegacyTileRenderPlans.TankDangerDiamondPlan plan, FluidType type,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!plan.hasFluid()) {
            return;
        }
        for (LegacyTileRenderPlans.DiamondTransformPlan transform : plan.transforms()) {
            poseStack.pushPose();
            if (transform.rotateBeforeTranslate()) {
                rotateYDegrees(poseStack, transform.yawDegrees());
                poseStack.translate(transform.translateX(), transform.translateY(), transform.translateZ());
            } else {
                poseStack.translate(transform.translateX(), transform.translateY(), transform.translateZ());
                rotateYDegrees(poseStack, transform.yawDegrees());
            }
            poseStack.scale(transform.scaleX(), transform.scaleY(), transform.scaleZ());
            renderDangerDiamond(type, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void enqueueDangerDiamondPlan(PresentStage stage,
            LegacyTileRenderPlans.TankDangerDiamondPlan plan, FluidType type,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!plan.hasFluid()) {
            return;
        }
        for (LegacyTileRenderPlans.DiamondTransformPlan transform : plan.transforms()) {
            poseStack.pushPose();
            if (transform.rotateBeforeTranslate()) {
                rotateYDegrees(poseStack, transform.yawDegrees());
                poseStack.translate(transform.translateX(), transform.translateY(), transform.translateZ());
            } else {
                poseStack.translate(transform.translateX(), transform.translateY(), transform.translateZ());
                rotateYDegrees(poseStack, transform.yawDegrees());
            }
            poseStack.scale(transform.scaleX(), transform.scaleY(), transform.scaleZ());
            enqueueDangerDiamond(stage, type, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void renderUntexturedQuad(PoseStack poseStack, MultiBufferSource buffer,
            LegacyTexturedRenderMode renderMode,
            LegacyTileRenderPlans.UntexturedQuadPlan quad) {
        if (quad.vertices().size() != 4) {
            return;
        }
        LegacyTileRenderPlans.UntexturedVertexPlan v0 = quad.vertices().get(0);
        LegacyTileRenderPlans.UntexturedVertexPlan v1 = quad.vertices().get(1);
        LegacyTileRenderPlans.UntexturedVertexPlan v2 = quad.vertices().get(2);
        LegacyTileRenderPlans.UntexturedVertexPlan v3 = quad.vertices().get(3);
        LegacyTileRenderPlans.RgbaPlan color = v0.color();
        int rgb = color.redByte() << 16 | color.greenByte() << 8 | color.blueByte();
        LegacyUntexturedQuadRenderer.quad(poseStack, buffer, renderMode,
                v0.x(), v0.y(), v0.z(),
                v1.x(), v1.y(), v1.z(),
                v2.x(), v2.y(), v2.z(),
                v3.x(), v3.y(), v3.z(),
                rgb, color.alphaByte(), color.alphaByte(), color.alphaByte(), color.alphaByte());
    }

    private static ResourceLocation tankTextureFor(FluidType type) {
        if (type == null || type == HbmFluids.NONE) {
            return TANK_NONE_TEXTURE;
        }
        if (type.shouldRenderTankWithTint()) {
            return TANK_NONE_TEXTURE;
        }
        if (type.isAntimatter()) {
            return TANK_DANGER_TEXTURE;
        }
        CorrosiveFluidTrait corrosive = type.getTrait(CorrosiveFluidTrait.class);
        if (corrosive != null && corrosive.isHighlyCorrosive()) {
            return TANK_DANGER_TEXTURE;
        }
        return tankTextureCached(type.getName());
    }

    private static int tankModelTint(FluidType type) {
        return type != null && type.shouldRenderTankWithTint() ? type.getGuiTint() : 0xFFFFFF;
    }

    private static int fluidColor(FluidType type) {
        return type == null ? 0xFFFFFF : type.getColor();
    }

    private static ResourceLocation tankTextureCached(String name) {
        return TANK_TEXTURES_BY_NAME.computeIfAbsent(name.toLowerCase(Locale.US),
                LegacyFluidTankRenderHelper::createTankTexture);
    }

    private static ResourceLocation createTankTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID,
                "textures/models/tank/tank_" + name + ".png");
    }

    private static void rotateYDegrees(PoseStack poseStack, float yawDegrees) {
        float radians = yawDegrees * Mth.DEG_TO_RAD;
        PoseStack.Pose pose = poseStack.last();
        pose.pose().rotateY(radians);
        pose.normal().rotateY(radians);
    }

    private static LegacyDangerDiamondRenderer.Symbol dangerSymbol(FluidSymbol symbol) {
        return switch (symbol) {
            case RADIATION -> LegacyDangerDiamondRenderer.Symbol.RADIATION;
            case NOWATER -> LegacyDangerDiamondRenderer.Symbol.NOWATER;
            case ACID -> LegacyDangerDiamondRenderer.Symbol.ACID;
            case ASPHYXIANT -> LegacyDangerDiamondRenderer.Symbol.ASPHYXIANT;
            case CRYOGENIC -> LegacyDangerDiamondRenderer.Symbol.CRYOGENIC;
            case ANTIMATTER -> LegacyDangerDiamondRenderer.Symbol.ANTIMATTER;
            case OXIDIZER -> LegacyDangerDiamondRenderer.Symbol.OXIDIZER;
            default -> LegacyDangerDiamondRenderer.Symbol.NONE;
        };
    }
}
