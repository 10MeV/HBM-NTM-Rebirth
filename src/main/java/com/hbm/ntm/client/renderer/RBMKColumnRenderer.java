package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.RBMKColumnBlock;
import com.hbm.ntm.blockentity.RBMKColumnBlockEntity;
import com.hbm.ntm.client.obj.ObjRbmkModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.neutron.RBMKControlRodPlanner;
import com.hbm.ntm.neutron.RBMKNeutronHandler;
import com.hbm.ntm.neutron.RBMKWorldRenderPlanner;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public class RBMKColumnRenderer implements BlockEntityRenderer<RBMKColumnBlockEntity> {
    private static final int DEFAULT_FUEL_COLOR = 0x304825;

    public RBMKColumnRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RBMKColumnBlockEntity column, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = column.getBlockState();
        RBMKColumnBlock.Kind kind = column.kind();
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(column, packedLight);

        if (kind.rod()) {
            renderFuelChannel(column, state, poseStack, buffer, modelLight, packedOverlay);
        }
        if (kind.control()) {
            renderControlRod(column, kind, state, partialTick, poseStack, buffer, modelLight, packedOverlay);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(RBMKColumnBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    private static void renderFuelChannel(RBMKColumnBlockEntity column, BlockState state,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        RBMKWorldRenderPlanner.FuelChannelRenderPlan plan = RBMKWorldRenderPlanner.fuelChannelRenderPlan(
                column.hasFuelRod(),
                (int) Math.round(column.lastFluxQuantity()),
                column.fuelRodRenderColor(),
                sameColumnAbove(),
                emptyMetadataAbove());

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        if (!plan.rodParts().isEmpty()) {
            int color = plan.rodRgb() == 0 ? DEFAULT_FUEL_COLOR : plan.rodRgb();
            int red = color >> 16 & 255;
            int green = color >> 8 & 255;
            int blue = color & 255;
            for (RBMKWorldRenderPlanner.RodStackPart rod : plan.rodParts()) {
                poseStack.pushPose();
                poseStack.translate(0.0D, rod.yOffset(), 0.0D);
                ObjRbmkModels.ELEMENT_RODS_VBO.renderPart(plan.part(), ObjRbmkModels.ELEMENT_FUEL_TEXTURE,
                        poseStack, buffer, packedLight, packedOverlay, red, green, blue, 255);
                poseStack.popPose();
            }
        }
        if (plan.cherenkov()) {
            ObjRbmkModels.renderFuelChannelCherenkov(
                    new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay),
                    plan.columnOffset());
        }
        poseStack.popPose();
    }

    private static void renderControlRod(RBMKColumnBlockEntity column, RBMKColumnBlock.Kind kind, BlockState state,
            float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        RBMKControlRodPlanner.RBMKColor color = column.color();
        RBMKWorldRenderPlanner.ControlRodRenderPlan plan = RBMKWorldRenderPlanner.controlRodRenderPlan(
                !kind.automatic(),
                color,
                column.controlState().lastLevel(),
                column.controlState().level(),
                partialTick,
                sameColumnAbove());
        ResourceLocation texture = controlTexture(kind.automatic(), color);

        poseStack.pushPose();
        poseStack.translate(0.5D, plan.lidWorldY(), 0.5D);
        ObjRbmkModels.RODS_VBO.renderPart(plan.part(), texture,
                new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay));
        poseStack.popPose();
    }

    private static ResourceLocation controlTexture(boolean automatic, RBMKControlRodPlanner.RBMKColor color) {
        if (automatic) {
            return ObjRbmkModels.CONTROL_AUTO_TEXTURE;
        }
        return color == null
                ? ObjRbmkModels.CONTROL_STANDARD_TEXTURE
                : ObjRbmkModels.manualControlTexture(color.ordinal());
    }

    private static boolean[] sameColumnAbove() {
        boolean[] same = new boolean[columnHeightAbove()];
        Arrays.fill(same, true);
        return same;
    }

    private static int[] emptyMetadataAbove() {
        return new int[columnHeightAbove()];
    }

    private static int columnHeightAbove() {
        return Math.max(0, RBMKNeutronHandler.settings().columnHeight() - 1);
    }
}
