package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.FluidBarrelBlock;
import com.hbm.ntm.blockentity.FluidBarrelBlockEntity;
import com.hbm.ntm.client.obj.LegacyBarrelObjRenderer;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmFluids;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public class FluidBarrelRenderer implements BlockEntityRenderer<FluidBarrelBlockEntity> {
    private static final ResourceLocation BARREL_PLASTIC = ObjBlockModels.texture("barrel_plastic");
    private static final ResourceLocation BARREL_CORRODED = ObjBlockModels.texture("barrel_corroded");
    private static final ResourceLocation BARREL_STEEL = ObjBlockModels.texture("barrel_steel");
    private static final ResourceLocation BARREL_TCALLOY = ObjBlockModels.texture("barrel_tcalloy");
    private static final ResourceLocation BARREL_ANTIMATTER = ObjBlockModels.texture("barrel_antimatter");

    public FluidBarrelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public void render(FluidBarrelBlockEntity barrel, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        FluidType type = barrel.getTank().getTankType();
        if (type == null || type == HbmFluids.NONE || barrel.getTank().getFill() <= 0) {
            return;
        }
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(barrel, getViewDistance())) {
            return;
        }
        LegacyBlockEntityRenderCulling.recordMachineSubmission(barrel);

        int modelLight = LegacyRenderLighting.resolveMultiblockLight(barrel, packedLight);
        ResourceLocation barrelTexture = barrelTexture(barrel.getVariant());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        renderConnectorIfConnected(barrel, Direction.EAST, 0.0F, barrelTexture, poseStack, buffer, modelLight,
                packedOverlay);
        renderConnectorIfConnected(barrel, Direction.WEST, 180.0F, barrelTexture, poseStack, buffer, modelLight,
                packedOverlay);
        renderConnectorIfConnected(barrel, Direction.NORTH, 90.0F, barrelTexture, poseStack, buffer, modelLight,
                packedOverlay);
        renderConnectorIfConnected(barrel, Direction.SOUTH, -90.0F, barrelTexture, poseStack, buffer, modelLight,
                packedOverlay);
        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                queuedPose -> LegacyFluidTankRenderHelper.renderDangerDiamonds(
                        LegacyTileRenderPlans.fluidBarrelDangerDiamondPlan(true), type,
                        queuedPose, buffer, modelLight, packedOverlay));
        poseStack.popPose();
    }

    private static void renderConnectorIfConnected(FluidBarrelBlockEntity barrel, Direction direction, float yawDegrees,
            ResourceLocation barrelTexture, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        if (barrel.getLevel() == null) {
            return;
        }
        HbmFluidUtil.PortSnapshot snapshot = HbmFluidUtil.inspectPort(barrel.getLevel(), barrel.getBlockPos(),
                HbmFluidPortLayouts.adjacent(direction), barrel.getTank().getTankType());
        if (!snapshot.connectable()) {
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(yawDegrees));
        poseStack.translate(0.0D, -0.5D, 0.0D);
        LegacyBarrelObjRenderer.renderConnector(barrelTexture, poseStack, buffer, packedLight, packedOverlay,
                0.0F, 0.0F, 0.0F);
        poseStack.popPose();
    }

    private static ResourceLocation barrelTexture(FluidBarrelBlock.Variant variant) {
        return switch (variant) {
            case PLASTIC -> BARREL_PLASTIC;
            case CORRODED -> BARREL_CORRODED;
            case STEEL -> BARREL_STEEL;
            case TCALLOY -> BARREL_TCALLOY;
            case ANTIMATTER -> BARREL_ANTIMATTER;
        };
    }
}
