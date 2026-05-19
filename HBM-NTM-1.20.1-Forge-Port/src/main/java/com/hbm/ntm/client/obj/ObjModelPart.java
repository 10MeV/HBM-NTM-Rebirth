package com.hbm.ntm.client.obj;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

public final class ObjModelPart {
    private final ResourceLocation modelLocation;
    private final RenderType renderType;
    private final ObjPartTransform originTransform;

    public ObjModelPart(ResourceLocation modelLocation, RenderType renderType) {
        this(modelLocation, renderType, ObjPartTransform.IDENTITY);
    }

    public ObjModelPart(ResourceLocation modelLocation, RenderType renderType, ObjPartTransform originTransform) {
        this.modelLocation = modelLocation;
        this.renderType = renderType;
        this.originTransform = originTransform;
        ObjModelLibrary.register(modelLocation);
    }

    public ResourceLocation modelLocation() {
        return modelLocation;
    }

    public void render(ObjRenderContext context) {
        context.poseStack().pushPose();
        originTransform.apply(context.poseStack());
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
        ObjRenderUtils.renderBlockModel(
                model,
                context.state(),
                context.modelRenderer(),
                context.poseStack(),
                context.buffer(),
                context.packedLight(),
                context.packedOverlay(),
                renderType);
        context.poseStack().popPose();
    }
}
