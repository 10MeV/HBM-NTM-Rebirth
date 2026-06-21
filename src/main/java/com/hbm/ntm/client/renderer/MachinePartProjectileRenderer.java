package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.entity.projectile.MachinePartProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public abstract class MachinePartProjectileRenderer<T extends MachinePartProjectileEntity>
        extends EntityRenderer<T> {
    protected MachinePartProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.4F;
    }

    @Override
    public void render(T entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(entity.getOrientation())));
        poseStack.translate(0.0D, 0.0D, -1.0D);
        if (entity.getOrientation() < 6) {
            poseStack.mulPose(Axis.ZN.rotationDegrees(spinDegrees()));
        }
        poseStack.translate(0.0D, -1.375D, 0.0D);
        model().renderPart(partName(), texture(entity), poseStack, buffer, packedLight, 0);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static float legacyYaw(int orientation) {
        return switch (Math.floorMod(orientation, 6)) {
            case 5 -> 90.0F;
            case 2 -> 180.0F;
            case 4 -> 270.0F;
            default -> 0.0F;
        };
    }

    protected abstract LegacyWavefrontModel model();

    protected abstract String partName();

    protected abstract ResourceLocation texture(T entity);

    protected abstract float spinDegrees();

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return texture(entity);
    }
}
