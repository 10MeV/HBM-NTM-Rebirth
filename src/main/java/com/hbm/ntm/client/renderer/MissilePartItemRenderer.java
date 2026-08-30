package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.missile.MissilePartItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MissilePartItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float GUI_TARGET_SIZE = 0.82F;
    private static final float WORLD_TARGET_SIZE = 0.58F;

    public static final MissilePartItemRenderer INSTANCE = new MissilePartItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private MissilePartItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof MissilePartItem item) || !item.usesObjItemRenderer()) {
            return;
        }

        MissilePartRenderCache.PartRenderSpec spec = MissilePartRenderCache.spec(item.legacyModelKey());
        if (spec == null) {
            return;
        }

        poseStack.pushPose();
        applyDisplay(displayContext, poseStack, spec);
        spec.part().render(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack,
            MissilePartRenderCache.PartRenderSpec spec) {
        float targetSize = displayContext == ItemDisplayContext.GUI ? GUI_TARGET_SIZE : WORLD_TARGET_SIZE;
        float fitScale = MissilePartRenderCache.fitScale(targetSize, spec.maxSize(), 0.035D, 0.5D);

        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            // GUI inherited coordinates mirror the apparent Z direction: this is the requested clockwise 90° from 135°.
            LegacyPoseRotations.rotateZDegrees(poseStack, 45.0F);
            LegacyPoseRotations.rotateXDegrees(poseStack, 145.0F);
            if (spec.part().kind() == ObjMissilePartModels.PartKind.WARHEAD) {
                poseStack.translate(0.0D, 0.08D, 0.0D);
            } else if (spec.part().kind() == ObjMissilePartModels.PartKind.FUSELAGE) {
                poseStack.translate(0.0D, 0.14D, 0.0D);
            }
            LegacyPoseRotations.rotateYDegrees(poseStack, -((System.currentTimeMillis() / 25L) % 360L));
        } else {
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            if (displayContext == ItemDisplayContext.GROUND) {
                poseStack.scale(0.8F, 0.8F, 0.8F);
            } else if (displayContext.firstPerson()) {
                poseStack.translate(0.0D, 0.1D, 0.0D);
                poseStack.scale(0.85F, 0.85F, 0.85F);
            }
        }
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.scale(-fitScale, -fitScale, -fitScale);
        } else {
            poseStack.scale(fitScale, fitScale, fitScale);
        }
        poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());
    }
}
