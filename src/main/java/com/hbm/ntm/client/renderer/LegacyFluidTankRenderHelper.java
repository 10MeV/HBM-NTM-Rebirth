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
        double height = (double) tank.getFill() * 1.5D / (double) tank.getMaxFill();
        FluidType type = tank.getTankType();
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay)
                .withColor(0xFFFFFF, 192)
                .withRenderMode(LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE);

        double off = 5.9375D;
        LegacyUvAnimation.Range u = LegacyUvAnimation.bigAssTankFluidU(animation);
        double fluidV = LegacyUvAnimation.bigAssTankFluidV(height);
        ResourceLocation texture = type == null ? HbmFluids.NONE.getTexture() : type.getTexture();
        LegacyTexturedQuadRenderer.quad(texture, context, 1.0F, 0.0F, 0.0F,
                LegacyTexturedQuadRenderer.vertex(-off, 1.75D, -0.25D, u.min(), 0.0D, 0xFFFFFF, 192),
                LegacyTexturedQuadRenderer.vertex(-off, 1.75D + height, -0.25D, u.min(), fluidV, 0xFFFFFF, 192),
                LegacyTexturedQuadRenderer.vertex(-off, 1.75D + height, 0.25D, u.max(), fluidV, 0xFFFFFF, 192),
                LegacyTexturedQuadRenderer.vertex(-off, 1.75D, 0.25D, u.max(), 0.0D, 0xFFFFFF, 192));
        LegacyTexturedQuadRenderer.quad(texture, context, -1.0F, 0.0F, 0.0F,
                LegacyTexturedQuadRenderer.vertex(off, 1.75D, -0.25D, u.max(), 0.0D, 0xFFFFFF, 192),
                LegacyTexturedQuadRenderer.vertex(off, 1.75D + height, -0.25D, u.max(), fluidV, 0xFFFFFF, 192),
                LegacyTexturedQuadRenderer.vertex(off, 1.75D + height, 0.25D, u.min(), fluidV, 0xFFFFFF, 192),
                LegacyTexturedQuadRenderer.vertex(off, 1.75D, 0.25D, u.min(), 0.0D, 0xFFFFFF, 192));
    }

    public static void renderBat9000Fluid(HbmFluidTank tank, BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (tank.isEmpty() || tank.getMaxFill() <= 0) {
            return;
        }
        double height = (double) tank.getFill() * 1.5D / (double) tank.getMaxFill();
        int color = fluidColor(tank.getTankType());
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay)
                .withoutTranslucency();
        LegacyUntexturedQuadRenderer.verticalCrossPanels(context, 1.5D, height, 0.5D, 2.2D, color, 255);
    }

    public static void renderSmallTankDiamonds(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-0.25D, 0.5D, -1.501D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(1.0F, 0.375F, 0.375F);
        renderDangerDiamond(type, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.25D, 0.5D, 1.501D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-90.0F));
        poseStack.scale(1.0F, 0.375F, 0.375F);
        renderDangerDiamond(type, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    public static void renderBigAssTankDiamonds(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(22.5F));
        for (int i = 0; i < 2; i++) {
            poseStack.pushPose();
            poseStack.translate(5.5D, 2.0D, 0.0D);
            renderDangerDiamond(type, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
        }
        poseStack.popPose();
    }

    public static void renderBat9000Diamonds(FluidType type, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (type == null || type == HbmFluids.NONE) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(45.0F));
        for (int i = 0; i < 4; i++) {
            poseStack.pushPose();
            poseStack.translate(2.5D, 2.25D, 0.0D);
            poseStack.scale(1.0F, 0.75F, 0.75F);
            renderDangerDiamond(type, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90.0F));
        }
        poseStack.popPose();
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
