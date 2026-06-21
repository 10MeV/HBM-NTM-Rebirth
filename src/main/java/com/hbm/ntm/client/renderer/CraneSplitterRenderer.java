package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.CraneSplitterBlock;
import com.hbm.ntm.blockentity.CraneSplitterBlockEntity;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

public class CraneSplitterRenderer implements BlockEntityRenderer<CraneSplitterBlockEntity> {
    private static final ResourceLocation TOP_LEFT = ObjBlockModels.texture("crane_splitter_top_left");
    private static final ResourceLocation TOP_RIGHT = ObjBlockModels.texture("crane_splitter_top_right");
    private static final ResourceLocation FRONT_LEFT = ObjBlockModels.texture("crane_splitter_front_left");
    private static final ResourceLocation FRONT_RIGHT = ObjBlockModels.texture("crane_splitter_front_right");
    private static final ResourceLocation BACK_LEFT = ObjBlockModels.texture("crane_splitter_back_left");
    private static final ResourceLocation BACK_RIGHT = ObjBlockModels.texture("crane_splitter_back_right");
    private static final ResourceLocation LEFT = ObjBlockModels.texture("crane_splitter_left");
    private static final ResourceLocation RIGHT = ObjBlockModels.texture("crane_splitter_right");
    private static final ResourceLocation BELT = ObjBlockModels.texture("crane_splitter_belt");
    private static final ResourceLocation INNER = ObjBlockModels.texture("crane_splitter_inner");
    private static final ResourceLocation INNER_SIDE = ObjBlockModels.texture("crane_splitter_inner_side");

    public CraneSplitterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(CraneSplitterBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        renderHalf(state, true, 0.0D, 0.0D, poseStack, buffer, modelLight, packedOverlay);
        Direction side = CraneSplitterBlock.sideOffset(state);
        renderHalf(state, false, side.getStepX(), side.getStepZ(), poseStack, buffer, modelLight, packedOverlay);
    }

    public static void renderItem(ItemDisplayContext displayContext, BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.scale(0.625F, 0.625F, 0.625F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.translate(0.0D, -0.5D, 0.5D);
            renderInventoryHalf(state, true, poseStack, buffer, packedLight, packedOverlay);
            poseStack.translate(0.0D, 0.0D, -1.0D);
            renderInventoryHalf(state, false, poseStack, buffer, packedLight, packedOverlay);
        } else {
            poseStack.translate(0.5D, 0.35D, 0.5D);
            poseStack.scale(0.35F, 0.35F, 0.35F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.translate(0.0D, -0.5D, 0.5D);
            renderInventoryHalf(state, true, poseStack, buffer, packedLight, packedOverlay);
            poseStack.translate(0.0D, 0.0D, -1.0D);
            renderInventoryHalf(state, false, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderHalf(BlockState state, boolean left, double offsetX, double offsetZ,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(offsetX + 0.5D, 0.0D, offsetZ + 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(CraneSplitterBlock.legacyRenderRotationDegrees(state)));
        drawSplitter(state, left, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderInventoryHalf(BlockState state, boolean left, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        drawSplitter(state, left, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void drawSplitter(BlockState state, boolean left, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        ObjBlockModels.SPLITTER.renderPart("Top", left ? TOP_LEFT : TOP_RIGHT, context);
        ObjBlockModels.SPLITTER.renderPart("Bottom", left ? TOP_RIGHT : TOP_LEFT, context);
        if (left) {
            ObjBlockModels.SPLITTER.renderPart("Left", LEFT, context);
        } else {
            ObjBlockModels.SPLITTER.renderPart("Right", RIGHT, context);
        }
        ObjBlockModels.SPLITTER.renderPart("Back", left ? BACK_LEFT : BACK_RIGHT, context);
        ObjBlockModels.SPLITTER.renderPart("Front", left ? FRONT_LEFT : FRONT_RIGHT, context);
        ObjBlockModels.SPLITTER.renderPart("Inner", INNER, context);
        ObjBlockModels.SPLITTER.renderPart("InnerLeft", INNER_SIDE, context);
        ObjBlockModels.SPLITTER.renderPart("InnerRight", INNER_SIDE, context);
        ObjBlockModels.SPLITTER.renderPart("InnerTop", INNER_SIDE, context);
        ObjBlockModels.SPLITTER.renderPart("InnerBottom", BELT, context);
    }
}
