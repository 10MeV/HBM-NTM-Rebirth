package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.item.missile.MissilePartItem;
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

        ObjMissilePartModels.LegacyMissilePart part = ObjMissilePartModels.part(item.legacyModelKey());
        if (part == null) {
            return;
        }

        poseStack.pushPose();
        applyDisplay(displayContext, poseStack, part);
        part.render(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack,
            ObjMissilePartModels.LegacyMissilePart part) {
        AABB bounds = part.model().boundsAll();
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(part.guiHeight() > 0.0D ? part.guiHeight() : 4.0D,
                Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize())));
        float targetSize = displayContext == ItemDisplayContext.GUI ? GUI_TARGET_SIZE : WORLD_TARGET_SIZE;
        float fitScale = (float) Math.max(0.035D, Math.min(0.5D, targetSize / Math.max(1.0D, maxSize)));

        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(135.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(145.0F));
            if (part.kind() == ObjMissilePartModels.PartKind.WARHEAD) {
                poseStack.translate(0.0D, 0.08D, 0.0D);
            } else if (part.kind() == ObjMissilePartModels.PartKind.FUSELAGE) {
                poseStack.translate(0.0D, 0.14D, 0.0D);
            }
            poseStack.mulPose(Axis.YN.rotationDegrees((System.currentTimeMillis() / 25L) % 360L));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
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
        poseStack.translate(-center.x, -center.y, -center.z);
    }
}
