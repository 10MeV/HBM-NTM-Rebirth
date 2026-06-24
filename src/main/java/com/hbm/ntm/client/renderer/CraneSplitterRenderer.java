package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.CraneSplitterBlock;
import com.hbm.ntm.blockentity.CraneSplitterBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

public class CraneSplitterRenderer implements BlockEntityRenderer<CraneSplitterBlockEntity> {
    private static final TextureAtlasSprite TOP_LEFT = sprite("crane_splitter_top_left");
    private static final TextureAtlasSprite TOP_RIGHT = sprite("crane_splitter_top_right");
    private static final TextureAtlasSprite FRONT_LEFT = sprite("crane_splitter_front_left");
    private static final TextureAtlasSprite FRONT_RIGHT = sprite("crane_splitter_front_right");
    private static final TextureAtlasSprite BACK_LEFT = sprite("crane_splitter_back_left");
    private static final TextureAtlasSprite BACK_RIGHT = sprite("crane_splitter_back_right");
    private static final TextureAtlasSprite LEFT = sprite("crane_splitter_left");
    private static final TextureAtlasSprite RIGHT = sprite("crane_splitter_right");
    private static final TextureAtlasSprite BELT = sprite("crane_splitter_belt");
    private static final TextureAtlasSprite INNER = sprite("crane_splitter_inner");
    private static final TextureAtlasSprite INNER_SIDE = sprite("crane_splitter_inner_side");
    private static final LegacyWavefrontModel.SelectionHandle TOP =
            ObjBlockModels.SPLITTER.prepareRenderOnlyInCallOrder("Top");
    private static final LegacyWavefrontModel.SelectionHandle BOTTOM =
            ObjBlockModels.SPLITTER.prepareRenderOnlyInCallOrder("Bottom");
    private static final LegacyWavefrontModel.SelectionHandle LEFT_PART =
            ObjBlockModels.SPLITTER.prepareRenderOnlyInCallOrder("Left");
    private static final LegacyWavefrontModel.SelectionHandle RIGHT_PART =
            ObjBlockModels.SPLITTER.prepareRenderOnlyInCallOrder("Right");
    private static final LegacyWavefrontModel.SelectionHandle BACK =
            ObjBlockModels.SPLITTER.prepareRenderOnlyInCallOrder("Back");
    private static final LegacyWavefrontModel.SelectionHandle FRONT =
            ObjBlockModels.SPLITTER.prepareRenderOnlyInCallOrder("Front");
    private static final LegacyWavefrontModel.SelectionHandle INNER_PART =
            ObjBlockModels.SPLITTER.prepareRenderOnlyInCallOrder("Inner");
    private static final LegacyWavefrontModel.SelectionHandle INNER_LEFT =
            ObjBlockModels.SPLITTER.prepareRenderOnlyInCallOrder("InnerLeft");
    private static final LegacyWavefrontModel.SelectionHandle INNER_RIGHT =
            ObjBlockModels.SPLITTER.prepareRenderOnlyInCallOrder("InnerRight");
    private static final LegacyWavefrontModel.SelectionHandle INNER_TOP =
            ObjBlockModels.SPLITTER.prepareRenderOnlyInCallOrder("InnerTop");
    private static final LegacyWavefrontModel.SelectionHandle INNER_BOTTOM =
            ObjBlockModels.SPLITTER.prepareRenderOnlyInCallOrder("InnerBottom");

    public CraneSplitterRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(CraneSplitterBlockEntity blockEntity) {
        return false;
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
        renderPart(TOP, left ? TOP_LEFT : TOP_RIGHT, context);
        renderPart(BOTTOM, left ? TOP_RIGHT : TOP_LEFT, context);
        if (left) {
            renderPart(LEFT_PART, LEFT, context);
        } else {
            renderPart(RIGHT_PART, RIGHT, context);
        }
        renderPart(BACK, left ? BACK_LEFT : BACK_RIGHT, context);
        renderPart(FRONT, left ? FRONT_LEFT : FRONT_RIGHT, context);
        renderPart(INNER_PART, INNER, context);
        renderPart(INNER_LEFT, INNER_SIDE, context);
        renderPart(INNER_RIGHT, INNER_SIDE, context);
        renderPart(INNER_TOP, INNER_SIDE, context);
        renderPart(INNER_BOTTOM, BELT, context);
    }

    private static void renderPart(LegacyWavefrontModel.SelectionHandle handle, TextureAtlasSprite sprite,
            ObjRenderContext context) {
        ObjBlockModels.SPLITTER.renderOnlyInCallOrderWithSprite(sprite, context, handle);
    }

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(ObjBlockModels.texture(name));
    }
}
