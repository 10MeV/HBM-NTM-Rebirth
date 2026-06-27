package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyDangerDiamondRenderer;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.fluid.FluidSymbol;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidTank;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.fluid.trait.CorrosiveFluidTrait;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
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
        LegacyTileRenderPlans.BigAssTankFluidPlan plan = LegacyTileRenderPlans.bigAssTankFluidPlan(
                height, u.min(), u.max(), LegacyUvAnimation.bigAssTankFluidV(height));
        ResourceLocation texture = type == null ? HbmFluids.NONE.getTexture() : type.getTexture();
        for (LegacyTileRenderPlans.TexturedQuadPlan quad : plan.quads()) {
            renderTexturedQuad(texture, poseStack, buffer, packedLight, packedOverlay,
                    plan.blend().modernRenderMode(), quad);
        }
    }

    public static void renderBat9000Fluid(HbmFluidTank tank, BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tank.isEmpty() || tank.getMaxFill() <= 0) {
            return;
        }
        LegacyTileRenderPlans.Bat9000FluidPlan plan = LegacyTileRenderPlans.bat9000FluidPlan(tank.getFill(),
                tank.getMaxFill(), fluidColor(tank.getTankType()));
        for (LegacyTileRenderPlans.UntexturedQuadPlan quad : plan.quads()) {
            renderUntexturedQuad(poseStack, buffer, LegacyTexturedRenderMode.CUTOUT_NO_CULL, quad);
        }
    }

    public static void renderSmallTankDiamonds(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        renderDangerDiamondPlan(LegacyTileRenderPlans.smallTankDangerDiamondPlan(true), type,
                poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderBigAssTankDiamonds(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        renderDangerDiamondPlan(LegacyTileRenderPlans.bigAssTankDangerDiamondPlan(true), type,
                poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderBat9000Diamonds(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        renderDangerDiamondPlan(LegacyTileRenderPlans.bat9000DangerDiamondPlan(true), type,
                poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderDangerDiamonds(LegacyTileRenderPlans.TankDangerDiamondPlan plan, FluidType type,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        renderDangerDiamondPlan(plan, type, poseStack, buffer, packedLight, packedOverlay);
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

    private static void renderDangerDiamondPlan(LegacyTileRenderPlans.TankDangerDiamondPlan plan, FluidType type,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!plan.hasFluid()) {
            return;
        }
        for (LegacyTileRenderPlans.DiamondTransformPlan transform : plan.transforms()) {
            poseStack.pushPose();
            if (transform.role().startsWith("radial_")) {
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(transform.yawDegrees()));
                poseStack.translate(transform.translateX(), transform.translateY(), transform.translateZ());
            } else {
                poseStack.translate(transform.translateX(), transform.translateY(), transform.translateZ());
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(transform.yawDegrees()));
            }
            poseStack.scale(transform.scaleX(), transform.scaleY(), transform.scaleZ());
            renderDangerDiamond(type, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static void renderTexturedQuad(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, LegacyTexturedRenderMode renderMode,
            LegacyTileRenderPlans.TexturedQuadPlan quad) {
        if (quad.vertices().size() != 4) {
            return;
        }
        float normalX = quad.role().startsWith("pos_x") ? -1.0F : 1.0F;
        LegacyTileRenderPlans.QuadVertexPlan v0 = quad.vertices().get(0);
        LegacyTileRenderPlans.QuadVertexPlan v1 = quad.vertices().get(1);
        LegacyTileRenderPlans.QuadVertexPlan v2 = quad.vertices().get(2);
        LegacyTileRenderPlans.QuadVertexPlan v3 = quad.vertices().get(3);
        LegacyTexturedQuadRenderer.quad(texture, poseStack, buffer, packedLight, packedOverlay, renderMode,
                normalX, 0.0F, 0.0F,
                vertex(v0), vertex(v1), vertex(v2), vertex(v3));
    }

    private static LegacyTexturedQuadRenderer.Vertex vertex(LegacyTileRenderPlans.QuadVertexPlan vertex) {
        return LegacyTexturedQuadRenderer.vertex(vertex.x(), vertex.y(), vertex.z(), vertex.u(), vertex.v(),
                vertex.color(), vertex.alpha());
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
