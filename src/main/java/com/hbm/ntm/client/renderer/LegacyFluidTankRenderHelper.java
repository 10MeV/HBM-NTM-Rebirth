package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyDangerDiamondRenderer;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUvAnimation;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
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

public final class LegacyFluidTankRenderHelper {
    private static final ResourceLocation TANK_FRAME_TEXTURE = ObjMachineModels.LEGACY_FLUIDTANK_FRAME_TEXTURE;
    private static final ResourceLocation TANK_INNER_TEXTURE = ObjMachineModels.LEGACY_FLUIDTANK_INNER_TEXTURE;
    private static final ResourceLocation TANK_NONE_TEXTURE = tankTexture("NONE");
    private static final ResourceLocation TANK_DANGER_TEXTURE = tankTexture("DANGER");

    private LegacyFluidTankRenderHelper() {
    }

    public static void renderSmallTank(LegacyWavefrontModel normalModel, LegacyWavefrontModel explodedModel,
            HbmFluidTank tank, boolean exploded, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        FluidType type = tank.getTankType();
        if (exploded) {
            explodedModel.renderPart("Frame", TANK_FRAME_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
            explodedModel.renderPart("TankInner", TANK_INNER_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
            renderTankPart(explodedModel, "Tank", type, poseStack, buffer, packedLight, packedOverlay);
        } else {
            normalModel.renderPart("Frame", TANK_FRAME_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
            renderTankPart(normalModel, "Tank", type, poseStack, buffer, packedLight, packedOverlay);
        }
        renderSmallTankDiamonds(type, poseStack, buffer, packedLight, packedOverlay);
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
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay)
                .withRenderMode(plan.blend().modernRenderMode());
        ResourceLocation texture = type == null ? HbmFluids.NONE.getTexture() : type.getTexture();
        for (LegacyTileRenderPlans.TexturedQuadPlan quad : plan.quads()) {
            renderTexturedQuad(texture, context, quad);
        }
    }

    public static void renderBat9000Fluid(HbmFluidTank tank, BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tank.isEmpty() || tank.getMaxFill() <= 0) {
            return;
        }
        LegacyTileRenderPlans.Bat9000FluidPlan plan = LegacyTileRenderPlans.bat9000FluidPlan(tank.getFill(),
                tank.getMaxFill(), fluidColor(tank.getTankType()));
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay)
                .withoutTranslucency();
        for (LegacyTileRenderPlans.UntexturedQuadPlan quad : plan.quads()) {
            renderUntexturedQuad(context, quad);
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

    private static void renderTankPart(LegacyWavefrontModel model, String part, FluidType type,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int color = tankModelTint(type);
        ResourceLocation texture = tankTextureFor(type);
        model.renderPart(part, texture, poseStack, buffer, packedLight, packedOverlay,
                color >> 16 & 255, color >> 8 & 255, color & 255, 255);
    }

    private static void renderDangerDiamond(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                packedLight, packedOverlay).withRenderMode(LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE);
        LegacyDangerDiamondRenderer.render(context, type.getPoison(), type.getFlammability(),
                type.getReactivity(), dangerSymbol(type.getSymbol()));
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

    private static void renderTexturedQuad(ResourceLocation texture, ObjRenderContext context,
            LegacyTileRenderPlans.TexturedQuadPlan quad) {
        if (quad.vertices().size() != 4) {
            return;
        }
        float normalX = quad.role().startsWith("pos_x") ? -1.0F : 1.0F;
        LegacyTileRenderPlans.QuadVertexPlan v0 = quad.vertices().get(0);
        LegacyTileRenderPlans.QuadVertexPlan v1 = quad.vertices().get(1);
        LegacyTileRenderPlans.QuadVertexPlan v2 = quad.vertices().get(2);
        LegacyTileRenderPlans.QuadVertexPlan v3 = quad.vertices().get(3);
        LegacyTexturedQuadRenderer.quad(texture, context, normalX, 0.0F, 0.0F,
                vertex(v0), vertex(v1), vertex(v2), vertex(v3));
    }

    private static LegacyTexturedQuadRenderer.Vertex vertex(LegacyTileRenderPlans.QuadVertexPlan vertex) {
        return LegacyTexturedQuadRenderer.vertex(vertex.x(), vertex.y(), vertex.z(), vertex.u(), vertex.v(),
                vertex.color(), vertex.alpha());
    }

    private static void renderUntexturedQuad(ObjRenderContext context,
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
        LegacyUntexturedQuadRenderer.quad(context,
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
        return tankTexture(type.getName());
    }

    private static int tankModelTint(FluidType type) {
        return type != null && type.shouldRenderTankWithTint() ? type.getGuiTint() : 0xFFFFFF;
    }

    private static int fluidColor(FluidType type) {
        return type == null ? 0xFFFFFF : type.getColor();
    }

    private static ResourceLocation tankTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID,
                "textures/models/tank/tank_" + name.toLowerCase(Locale.US) + ".png");
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
