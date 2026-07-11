package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.projectile.ShrapnelEntity;
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

public class ShrapnelRenderer extends EntityRenderer<ShrapnelEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/shrapnel.png");
    private final ModelPart cube;

    public ShrapnelRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.cube = createLayer().bakeRoot().getChild("cube");
        this.shadowRadius = 0.1F;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cube", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 8);
    }

    @Override
    public void render(ShrapnelEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
        LegacyPoseRotations.rotateXDegrees(poseStack, (entity.tickCount + partialTick) * 10.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, (entity.tickCount + partialTick) * 10.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, (entity.tickCount + partialTick) * 10.0F);
        float scale = entity.isLargeRenderMode() ? 0.1875F : 0.0625F;
        poseStack.scale(scale, scale, scale);
        cube.render(poseStack, buffer.getBuffer(RenderType.entityCutout(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ShrapnelEntity entity) {
        return TEXTURE;
    }
}
