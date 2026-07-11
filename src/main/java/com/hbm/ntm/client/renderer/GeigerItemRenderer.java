package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjUtilityModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class GeigerItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final RenderSpec GEIGER_SPEC =
            renderSpec(transformedCommonBounds(ObjUtilityModels.GEIGER_COUNTER.boundsAll()));

    public static final GeigerItemRenderer INSTANCE = new GeigerItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private GeigerItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyInventoryTransform(poseStack, GEIGER_SPEC);
            poseStack.translate(0.2D, 0.0D, 0.0D);
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
            poseStack.scale(10.0F, 10.0F, 10.0F);
        } else {
            applyWorldTransform(displayContext, poseStack, GEIGER_SPEC);
            poseStack.translate(0.2D, 0.0D, 0.0D);
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        }
        ObjUtilityModels.GEIGER_COUNTER.renderAll(ObjUtilityModels.GEIGER_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyInventoryTransform(PoseStack poseStack, RenderSpec spec) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.scale(spec.guiFitScale(), spec.guiFitScale(), spec.guiFitScale());
        poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());
    }

    private static void applyWorldTransform(ItemDisplayContext displayContext, PoseStack poseStack, RenderSpec spec) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.scale(spec.worldFitScale(), spec.worldFitScale(), spec.worldFitScale());
        poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());
        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.translate(0.0D, -0.25D, 0.0D);
            poseStack.scale(0.8F, 0.8F, 0.8F);
        }
    }

    private static RenderSpec renderSpec(AABB bounds) {
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        return new RenderSpec(
                (bounds.minX + bounds.maxX) * 0.5D,
                (bounds.minY + bounds.maxY) * 0.5D,
                (bounds.minZ + bounds.maxZ) * 0.5D,
                (float) Math.max(0.025D, Math.min(0.32D, 0.86D / Math.max(1.0D, maxSize))),
                (float) Math.max(0.035D, Math.min(0.32D, 0.58D / Math.max(1.0D, maxSize))));
    }

    private static AABB transformedCommonBounds(AABB bounds) {
        double sin = LegacyTransformedBounds.sinDeg(90.0F);
        double cos = LegacyTransformedBounds.cosDeg(90.0F);
        return LegacyTransformedBounds.transform(bounds,
                (x, y, z, accumulator) -> LegacyTransformedBounds.includeRotatedY(accumulator,
                        x + 0.2D, y, z, sin, cos));
    }

    private record RenderSpec(double centerX, double centerY, double centerZ, float guiFitScale, float worldFitScale) {
    }
}
