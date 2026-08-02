package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.LegacySignWeaponItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/** Direct modern equivalent of ItemRenderShim's shared warning-sign path. */
public final class LegacySignWeaponItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final RenderSpec SPEC = renderSpec(ObjWeaponModels.STOPSIGN.boundsAll());

    public static final LegacySignWeaponItemRenderer INSTANCE = new LegacySignWeaponItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private LegacySignWeaponItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof LegacySignWeaponItem item)) {
            return;
        }

        poseStack.pushPose();
        applyDisplay(displayContext, poseStack);
        applyLegacySignTransform(displayContext, poseStack);
        ObjWeaponModels.STOPSIGN.renderAll(texture(item.variant()), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack) {
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.scale(SPEC.guiScale(), SPEC.guiScale(), SPEC.guiScale());
        } else if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.translate(0.5D, 0.25D, 0.5D);
            poseStack.scale(0.45F, 0.45F, 0.45F);
        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.scale(0.75F, 0.75F, 0.75F);
        }
        poseStack.translate(-SPEC.centerX(), -SPEC.centerY(), -SPEC.centerZ());
    }

    private static void applyLegacySignTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
        if (displayContext.firstPerson()) {
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            LegacyPoseRotations.rotateZDegrees(poseStack, -90.0F);
            poseStack.translate(-1.0D, -1.5D, 0.0D);
        }
        LegacyPoseRotations.rotateZDegrees(poseStack, 45.0F);
        poseStack.scale(0.35F, 0.35F, 0.35F);
        poseStack.translate(2.0D, -2.0D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
    }

    private static ResourceLocation texture(LegacySignWeaponItem.Variant variant) {
        return switch (variant) {
            case STOP -> ObjWeaponModels.STOPSIGN_TEXTURE;
            case SOP -> ObjWeaponModels.SOPSIGN_TEXTURE;
            case CHERNOBYL -> ObjWeaponModels.CHERNOBYLSIGN_TEXTURE;
        };
    }

    private static RenderSpec renderSpec(AABB bounds) {
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        return new RenderSpec(
                (bounds.minX + bounds.maxX) * 0.5D,
                (bounds.minY + bounds.maxY) * 0.5D,
                (bounds.minZ + bounds.maxZ) * 0.5D,
                (float) Math.max(0.45D, Math.min(0.85D, 0.72D / Math.max(1.0D, maxSize))));
    }

    private record RenderSpec(double centerX, double centerY, double centerZ, float guiScale) {
    }
}
