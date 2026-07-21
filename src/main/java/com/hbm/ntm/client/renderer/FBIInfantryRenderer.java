package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.entity.mob.EntityFBI;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Source-backed vanilla-biped renderer for {@code RenderFBI}. */
public final class FBIInfantryRenderer extends MobRenderer<EntityFBI, HumanoidModel<EntityFBI>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/entity/fbi.png");

    public FBIInfantryRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), 0.5F);
    }

    @Override
    public void render(EntityFBI entity, float entityYaw, float partialTick,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffer, int packedLight) {
        getModel().rightArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
        getModel().leftArmPose = HumanoidModel.ArmPose.BOW_AND_ARROW;
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityFBI entity) {
        return TEXTURE;
    }
}
