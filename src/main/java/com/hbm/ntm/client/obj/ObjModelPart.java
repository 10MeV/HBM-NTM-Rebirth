package com.hbm.ntm.client.obj;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public final class ObjModelPart {
    private final ResourceLocation modelLocation;
    private final RenderType renderType;
    private final ObjPartTransform originTransform;
    private final float lightMultiplier;
    private final boolean directRender;

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
        this.modelLocation = modelLocation;
        this.renderType = renderType;
        this.originTransform = originTransform;
        this.lightMultiplier = lightMultiplier;
        this.directRender = directRender;
        ObjModelLibrary.register(modelLocation);
    }

    public ResourceLocation modelLocation() {
        return modelLocation;
    }

    public void render(ObjRenderContext context) {
        context.poseStack().pushPose();
        originTransform.apply(context.poseStack());
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        if (directRender) {
            ObjRenderUtils.renderModel(
                    model,
                    context.poseStack(),
                    context.buffer(),
                    context.packedLight(),
                    context.packedOverlay(),
                    renderType,
                    lightMultiplier,
                    context.color(),
                    context.hasColor(),
                    context.legacyShadow());
        } else {
            ObjRenderUtils.renderBlockModel(
                    model,
                    context.state(),
                    context.modelRenderer(),
                    context.poseStack(),
                    context.buffer(),
                    context.packedLight(),
                    context.packedOverlay(),
                    renderType,
                    lightMultiplier,
                    context.color(),
                    context.hasColor(),
                    context.legacyShadow());
        }
        context.poseStack().popPose();
    }
}
