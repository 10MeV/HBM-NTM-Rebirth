package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.item.FireworksEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** The legacy EntityFireworks renderer was RenderShrapnel with its normal-size path. */
public final class FireworksRenderer extends EntityRenderer<FireworksEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/shrapnel.png");
    private final ModelPart cube;

    public FireworksRenderer(EntityRendererProvider.Context context) {
        super(context);
        cube = createLayer().bakeRoot().getChild("cube");
        shadowRadius = 0.1F;
    }

    private static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 8);
    }

    @Override
    public void render(FireworksEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
        float rotation = (entity.tickCount + partialTick) * 10.0F;
        LegacyPoseRotations.rotateXDegrees(poseStack, rotation);
        LegacyPoseRotations.rotateYDegrees(poseStack, rotation);
        LegacyPoseRotations.rotateZDegrees(poseStack, rotation);
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        cube.render(poseStack, buffer.getBuffer(RenderType.entityCutout(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FireworksEntity entity) {
        return TEXTURE;
    }
}
