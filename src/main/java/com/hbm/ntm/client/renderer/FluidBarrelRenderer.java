package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.FluidBarrelBlock;
import com.hbm.ntm.blockentity.FluidBarrelBlockEntity;
import com.hbm.ntm.client.obj.LegacyBarrelObjRenderer;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.fluid.FluidType;
import com.hbm.ntm.fluid.HbmFluidPortLayouts;
import com.hbm.ntm.fluid.HbmFluidUtil;
import com.hbm.ntm.fluid.HbmFluids;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class FluidBarrelRenderer implements BlockEntityRenderer<FluidBarrelBlockEntity> {
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

        BlockState state = barrel.getBlockState();
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(barrel, packedLight);
        ResourceLocation barrelTexture = barrelTexture(barrel.getVariant());
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, modelLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        renderConnectorIfConnected(barrel, Direction.EAST, 0.0F, barrelTexture, context);
        renderConnectorIfConnected(barrel, Direction.WEST, 180.0F, barrelTexture, context);
        renderConnectorIfConnected(barrel, Direction.NORTH, 90.0F, barrelTexture, context);
        renderConnectorIfConnected(barrel, Direction.SOUTH, -90.0F, barrelTexture, context);
        LegacyFluidTankRenderHelper.renderDangerDiamonds(
                LegacyTileRenderPlans.fluidBarrelDangerDiamondPlan(true), type,
                poseStack, buffer, modelLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderConnectorIfConnected(FluidBarrelBlockEntity barrel, Direction direction, float yawDegrees,
            ResourceLocation barrelTexture, ObjRenderContext context) {
        if (barrel.getLevel() == null) {
            return;
        }
        HbmFluidUtil.PortSnapshot snapshot = HbmFluidUtil.inspectPort(barrel.getLevel(), barrel.getBlockPos(),
                HbmFluidPortLayouts.adjacent(direction), barrel.getTank().getTankType());
        if (!snapshot.connectable()) {
            return;
        }

        context.poseStack().pushPose();
        context.poseStack().mulPose(Axis.YP.rotationDegrees(yawDegrees));
        context.poseStack().translate(0.0D, -0.5D, 0.0D);
        LegacyBarrelObjRenderer.renderConnector(barrelTexture, context);
        context.poseStack().popPose();
    }

    private static ResourceLocation barrelTexture(FluidBarrelBlock.Variant variant) {
        return switch (variant) {
            case PLASTIC -> ObjBlockModels.texture("barrel_plastic");
            case CORRODED -> ObjBlockModels.texture("barrel_corroded");
            case STEEL -> ObjBlockModels.texture("barrel_steel");
            case TCALLOY -> ObjBlockModels.texture("barrel_tcalloy");
            case ANTIMATTER -> ObjBlockModels.texture("barrel_antimatter");
        };
    }
}
