package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjVehicleModels;
import com.hbm.ntm.entity.cart.NtmMinecartEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class NtmMinecartRenderer extends EntityRenderer<NtmMinecartEntity> {
    public NtmMinecartRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.7F;
    }

    @Override
    public void render(NtmMinecartEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
        poseStack.pushPose();

        long rand = (long) entity.getId() * 493286711L;
        rand = rand * rand * 4392167121L + rand * 98761L;
        float randX = (((float) (rand >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float randY = (((float) (rand >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        float randZ = (((float) (rand >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
        poseStack.translate(randX, randY, randZ);

        double interpX = Mth.lerp((double) partialTick, entity.xOld, entity.getX());
        double interpY = Mth.lerp((double) partialTick, entity.yOld, entity.getY());
        double interpZ = Mth.lerp((double) partialTick, entity.zOld, entity.getZ());
        Vec3 railPos = entity.getPos(interpX, interpY, interpZ);
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        if (railPos != null) {
            Vec3 front = entity.getPosOffs(interpX, interpY, interpZ, 0.3D);
            Vec3 back = entity.getPosOffs(interpX, interpY, interpZ, -0.3D);
            if (front == null) {
                front = railPos;
            }
            if (back == null) {
                back = railPos;
            }

            poseStack.translate(railPos.x - interpX, (front.y + back.y) / 2.0D - interpY, railPos.z - interpZ);
            Vec3 tangent = back.add(-front.x, -front.y, -front.z);
            if (tangent.length() != 0.0D) {
                tangent = tangent.normalize();
                yaw = (float) (Math.atan2(tangent.z, tangent.x) * 180.0D / Math.PI);
                pitch = (float) (Math.atan(tangent.y) * 73.0D);
            }
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-pitch));
        poseStack.translate(0.0D, -0.4375D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));

        ResourceLocation texture = getTextureLocation(entity);
        ObjVehicleModels.CART.renderPart("Carriage", texture, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);

        float hurtTime = entity.getHurtTime() - partialTick;
        float damage = entity.getDamage() - partialTick;
        if (damage < 0.0F) {
            damage = 0.0F;
        }
        if (hurtTime > 0.0F) {
            poseStack.translate(0.0D, 0.75D, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(hurtTime) * hurtTime * damage / 10.0F
                    * entity.getHurtDir()));
            poseStack.translate(0.0D, -0.75D, 0.0D);
        }

        ObjVehicleModels.CART.renderPart("Bucket", texture, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        renderSpecialContent(entity, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static void renderSpecialContent(NtmMinecartEntity entity, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight) {
        switch (entity.cartType()) {
            case EMPTY -> {
            }
            case CRATE -> {
            }
            case POWDER -> ObjVehicleModels.CART_POWDER.renderPart("Powder",
                    ObjVehicleModels.CART_POWDER_TEXTURE, poseStack, buffer, packedLight,
                    OverlayTexture.NO_OVERLAY);
            case SEMTEX -> {
                ObjVehicleModels.CART_POWDER.renderPart("SemtexTop",
                        ObjVehicleModels.CART_SEMTEX_TOP_TEXTURE, poseStack, buffer, packedLight,
                        OverlayTexture.NO_OVERLAY);
                ObjVehicleModels.CART_POWDER.renderPart("SemtexSide",
                        ObjVehicleModels.CART_SEMTEX_SIDE_TEXTURE, poseStack, buffer, packedLight,
                        OverlayTexture.NO_OVERLAY);
            }
            case DESTROYER -> ObjVehicleModels.CART_DESTROYER.renderAll(
                    ObjVehicleModels.CART_DESTROYER_TEXTURE, poseStack, buffer, packedLight,
                    OverlayTexture.NO_OVERLAY);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(NtmMinecartEntity entity) {
        return switch (entity.getBase()) {
            case PAINTED -> ObjVehicleModels.CART_METAL_TEXTURE;
            case WOOD -> ObjVehicleModels.CART_WOOD_TEXTURE;
            case VANILLA, STEEL -> ObjVehicleModels.CART_BLANK_TEXTURE;
        };
    }
}
