package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.entity.item.DeliveryDroneEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Direct OBJ migration of RenderDeliveryDrone: Drone plus Crate/Barrel payload parts. */
public class DeliveryDroneRenderer extends EntityRenderer<DeliveryDroneEntity> {
    private static final ResourceLocation NORMAL = new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/drone.png");
    private static final ResourceLocation EXPRESS = new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/drone_express.png");
    /** Shared reload-aware cache for the legacy drone OBJ; do not allocate a renderer-local VBO. */
    private static final LegacyWavefrontModel MODEL = ObjMachineModels.DELIVERY_DRONE;

    public DeliveryDroneRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override public void render(DeliveryDroneEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ResourceLocation texture = entity.express() ? EXPRESS : NORMAL;
        poseStack.pushPose();
        RenderSystem.disableCull();
        MODEL.renderPart("Drone", texture, poseStack, buffer, packedLight, 0);
        if (entity.appearance() == 1) MODEL.renderPart("Crate", texture, poseStack, buffer, packedLight, 0);
        if (entity.appearance() == 2) MODEL.renderPart("Barrel", texture, poseStack, buffer, packedLight, 0);
        RenderSystem.enableCull();
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override public ResourceLocation getTextureLocation(DeliveryDroneEntity entity) { return entity.express() ? EXPRESS : NORMAL; }
}
