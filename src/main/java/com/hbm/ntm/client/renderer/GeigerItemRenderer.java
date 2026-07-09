package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjUtilityModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GeigerItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final GeigerItemRenderer INSTANCE = new GeigerItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private GeigerItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformedCommonBounds(ObjUtilityModels.GEIGER_COUNTER.boundsAll());
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyInventoryTransform(poseStack, bounds);
            poseStack.translate(0.2D, 0.0D, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.scale(10.0F, 10.0F, 10.0F);
        } else {
            applyWorldTransform(displayContext, poseStack, bounds);
            poseStack.translate(0.2D, 0.0D, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
        ObjUtilityModels.GEIGER_COUNTER.renderAll(ObjUtilityModels.GEIGER_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyInventoryTransform(PoseStack poseStack, AABB bounds) {
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        float fitScale = (float) Math.max(0.025D, Math.min(0.32D, 0.86D / Math.max(1.0D, maxSize)));
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.scale(fitScale, fitScale, fitScale);
        poseStack.translate(-center.x, -center.y, -center.z);
    }

    private static void applyWorldTransform(ItemDisplayContext displayContext, PoseStack poseStack, AABB bounds) {
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        float fitScale = (float) Math.max(0.035D, Math.min(0.32D, 0.58D / Math.max(1.0D, maxSize)));
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(fitScale, fitScale, fitScale);
        poseStack.translate(-center.x, -center.y, -center.z);
        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.translate(0.0D, -0.25D, 0.0D);
            poseStack.scale(0.8F, 0.8F, 0.8F);
        }
    }

    private static AABB transformedCommonBounds(AABB bounds) {
        double sin = LegacyTransformedBounds.sinDeg(90.0F);
        double cos = LegacyTransformedBounds.cosDeg(90.0F);
        return LegacyTransformedBounds.transform(bounds,
                (x, y, z, accumulator) -> LegacyTransformedBounds.includeRotatedY(accumulator,
                        x + 0.2D, y, z, sin, cos));
    }
}
