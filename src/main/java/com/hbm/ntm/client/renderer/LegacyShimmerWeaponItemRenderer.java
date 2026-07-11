package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.LegacyShimmerWeaponItem;
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

public class LegacyShimmerWeaponItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final RenderSpec SLEDGE_SPEC = renderSpec(ObjWeaponModels.SHIMMER_SLEDGE.boundsAll());
    private static final RenderSpec AXE_SPEC = renderSpec(ObjWeaponModels.SHIMMER_AXE.boundsAll());

    public static final LegacyShimmerWeaponItemRenderer INSTANCE = new LegacyShimmerWeaponItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private LegacyShimmerWeaponItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof LegacyShimmerWeaponItem item)) {
            return;
        }

        LegacyWavefrontModel model = model(item.kind());
        ResourceLocation texture = texture(item.kind());
        RenderSpec spec = spec(item.kind());

        poseStack.pushPose();
        applyDisplay(displayContext, poseStack, spec);
        applyLegacyTransform(poseStack);
        model.renderAll(texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack, RenderSpec spec) {
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.scale(spec.guiScale(), spec.guiScale(), spec.guiScale());
            poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());
            return;
        }

        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.translate(0.5D, 0.25D, 0.5D);
            poseStack.scale(0.45F, 0.45F, 0.45F);
            poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale(0.75F, 0.75F, 0.75F);
        poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());
    }

    private static void applyLegacyTransform(PoseStack poseStack) {
        LegacyPoseRotations.rotateZDegrees(poseStack, -135.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 180.0F);
        poseStack.scale(1.5F, 1.5F, 1.5F);
        poseStack.translate(0.45D, -0.3D, 0.0D);
    }

    private static LegacyWavefrontModel model(LegacyShimmerWeaponItem.Kind kind) {
        return kind == LegacyShimmerWeaponItem.Kind.SLEDGE
                ? ObjWeaponModels.SHIMMER_SLEDGE
                : ObjWeaponModels.SHIMMER_AXE;
    }

    private static ResourceLocation texture(LegacyShimmerWeaponItem.Kind kind) {
        return kind == LegacyShimmerWeaponItem.Kind.SLEDGE
                ? ObjWeaponModels.SHIMMER_SLEDGE_TEXTURE
                : ObjWeaponModels.SHIMMER_AXE_TEXTURE;
    }

    private static RenderSpec spec(LegacyShimmerWeaponItem.Kind kind) {
        return kind == LegacyShimmerWeaponItem.Kind.SLEDGE ? SLEDGE_SPEC : AXE_SPEC;
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
