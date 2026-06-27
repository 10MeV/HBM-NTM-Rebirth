package com.hbm.ntm.client.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ObjModelPart {
    private final ResourceLocation modelLocation;
    private final RenderType renderType;
    private final ObjPartTransform originTransform;
    private final float lightMultiplier;
    private final boolean directRender;
    private final boolean translucent;

    public ObjModelPart(ResourceLocation modelLocation, RenderType renderType) {
        this(modelLocation, renderType, ObjPartTransform.IDENTITY);
    }

    public ObjModelPart(ResourceLocation modelLocation, RenderType renderType, ObjPartTransform originTransform) {
        this(modelLocation, renderType, originTransform, 1.0F);
    }

    public ObjModelPart(ResourceLocation modelLocation, RenderType renderType, ObjPartTransform originTransform, float lightMultiplier) {
        this(modelLocation, renderType, originTransform, lightMultiplier, false);
    }

    public ObjModelPart(ResourceLocation modelLocation, RenderType renderType, ObjPartTransform originTransform, float lightMultiplier, boolean directRender) {
        this(modelLocation, renderType, originTransform, lightMultiplier, directRender, renderType == RenderType.translucent());
    }

    public ObjModelPart(ResourceLocation modelLocation, RenderType renderType, ObjPartTransform originTransform, float lightMultiplier,
            boolean directRender, boolean translucent) {
        this.modelLocation = modelLocation;
        this.renderType = renderType;
        this.originTransform = originTransform;
        this.lightMultiplier = lightMultiplier;
        this.directRender = directRender;
        this.translucent = translucent;
        ObjModelLibrary.register(modelLocation);
    }

    public ResourceLocation modelLocation() {
        return modelLocation;
    }

    public boolean translucent() {
        return translucent;
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        render(poseStack, buffer, Blocks.AIR.defaultBlockState(), packedLight, packedOverlay, 0xFFFFFF, false, false);
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay, int color) {
        render(poseStack, buffer, Blocks.AIR.defaultBlockState(), packedLight, packedOverlay, color & 0xFFFFFF, true, false);
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, BlockState state, int packedLight, int packedOverlay) {
        render(poseStack, buffer, state, packedLight, packedOverlay, 0xFFFFFF, false, false);
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, BlockState state, int packedLight, int packedOverlay,
            int color) {
        render(poseStack, buffer, state, packedLight, packedOverlay, color & 0xFFFFFF, true, false);
    }

    void render(PoseStack poseStack, MultiBufferSource buffer, BlockState state, int packedLight, int packedOverlay,
            int color, boolean hasColor, boolean legacyShadow) {
        color &= 0xFFFFFF;
        poseStack.pushPose();
        originTransform.apply(poseStack);
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        if (directRender) {
            ObjRenderUtils.renderModel(
                    model,
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay,
                    renderType,
                    lightMultiplier,
                    color,
                    hasColor,
                    legacyShadow);
        } else {
            ObjRenderUtils.renderBlockModel(
                    model,
                    state,
                    Minecraft.getInstance().getBlockRenderer().getModelRenderer(),
                    poseStack,
                    buffer,
                    packedLight,
                    packedOverlay,
                    renderType,
                    lightMultiplier,
                    color,
                    hasColor,
                    legacyShadow);
        }
        poseStack.popPose();
    }
}
