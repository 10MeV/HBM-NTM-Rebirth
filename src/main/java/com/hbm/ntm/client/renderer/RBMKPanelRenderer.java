package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.RBMKPanelBlock;
import com.hbm.ntm.blockentity.RBMKPanelBlockEntity;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.neutron.RBMKPanelPlanner;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class RBMKPanelRenderer implements BlockEntityRenderer<RBMKPanelBlockEntity> {
    public RBMKPanelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RBMKPanelBlockEntity panel, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = panel.getBlockState();
        Direction facing = state.hasProperty(RBMKPanelBlock.FACING)
                ? state.getValue(RBMKPanelBlock.FACING) : Direction.NORTH;
        int light = LegacyRenderLighting.resolveBlockEntityLight(panel, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(facing)));
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, light, packedOverlay);
        switch (panel.panelType()) {
            case GAUGE -> LegacyRbmkPanelRenderer.renderGauges(context, panel.gauges(), partialTick);
            case GRAPH -> LegacyRbmkPanelRenderer.renderGraphs(context, panel.graphs());
            case INDICATOR -> LegacyRbmkPanelRenderer.renderIndicators(context, panel.indicators());
            case KEYPAD -> LegacyRbmkPanelRenderer.renderKeys(context, panel.keys());
            case LEVER -> LegacyRbmkPanelRenderer.renderLevers(context, panel.levers(), partialTick);
            case NUMITRON -> LegacyRbmkPanelRenderer.renderNumitrons(context, panel.numitrons());
            case TERMINAL, DISPLAY -> {
            }
        }
        poseStack.popPose();
    }

    private static float legacyYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case WEST -> 180.0F;
            case SOUTH -> 270.0F;
            default -> 0.0F;
        };
    }
}
