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
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ShrapnelRenderer extends EntityRenderer<ShrapnelEntity> {
    public static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/shrapnel.png");
    private static final Vector3f LEGACY_ROTATION_AXIS = new Vector3f(1.0F, 1.0F, 1.0F).normalize();
    private static final ThreadLocal<Quaternionf> ROTATION_SCRATCH = ThreadLocal.withInitial(Quaternionf::new);
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
                .addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F),
                PartPose.offset(1.0F, -0.5F, -0.5F));
        return LayerDefinition.create(mesh, 16, 8);
    }

    @Override
    public void render(ShrapnelEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
        float rotation = (entity.tickCount % 360) * 10.0F + partialTick;
        poseStack.mulPose(legacyRotation(rotation));
        float scale = entity.isLargeRenderMode() ? 3.0F : 1.0F;
        poseStack.scale(scale, scale, scale);
        cube.render(poseStack, buffer.getBuffer(RenderType.entityCutout(TEXTURE)), packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ShrapnelEntity entity) {
        return TEXTURE;
    }

    private static Quaternionf legacyRotation(float degrees) {
        return ROTATION_SCRATCH.get().rotationAxis(degrees * Mth.DEG_TO_RAD, LEGACY_ROTATION_AXIS);
    }
}
