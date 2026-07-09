package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.BalefireBombBlock;
import com.hbm.ntm.client.obj.ObjBombModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BalefireBombItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float LEGACY_GUI_SLOT_PIXELS = 16.0F;
    private static final float LEGACY_GUI_MAX_OCCUPANCY = 0.86F;
    private static final float LEGACY_INVENTORY_SCALE = 2.25F;

    public static final BalefireBombItemRenderer INSTANCE = new BalefireBombItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private BalefireBombItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof BalefireBombBlock)) {
            return;
        }

        boolean gui = displayContext == ItemDisplayContext.GUI;
        AABB bounds = gui ? transformedInventoryBounds(ObjBombModels.FSTBMB.boundsAll())
                : transformedCommonBounds(ObjBombModels.FSTBMB.boundsAll());
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));

        poseStack.pushPose();
        applyBaseDisplay(displayContext, poseStack, center, maxSize, gui);
        if (gui) {
            poseStack.scale(LEGACY_INVENTORY_SCALE, LEGACY_INVENTORY_SCALE, LEGACY_INVENTORY_SCALE);
        }
        BalefireBombRenderer.applyLegacyItemCommon(poseStack);
        BalefireBombRenderer.renderModel(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyBaseDisplay(ItemDisplayContext displayContext, PoseStack poseStack, Vec3 center,
            double maxSize, boolean gui) {
        float fitScale = (float) Math.max(0.035D, Math.min(0.32D,
                targetDisplaySize(gui, maxSize) / Math.max(1.0D, maxSize)));

        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(fitScale, fitScale, fitScale);
            poseStack.translate(-center.x, -center.y, -center.z);
            return;
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float worldScale = fitScale * 0.82F;
        poseStack.scale(worldScale, worldScale, worldScale);
        poseStack.translate(-center.x, -center.y, -center.z);

        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.translate(0.0D, -0.25D, 0.0D);
            poseStack.scale(0.8F, 0.8F, 0.8F);
        } else if (displayContext.firstPerson()) {
            poseStack.translate(0.0D, 0.1D, 0.0D);
            poseStack.scale(0.85F, 0.85F, 0.85F);
        }
    }

    private static double targetDisplaySize(boolean gui, double maxSize) {
        if (gui) {
            return Math.min(LEGACY_GUI_MAX_OCCUPANCY, maxSize / LEGACY_GUI_SLOT_PIXELS);
        }
        return LEGACY_GUI_MAX_OCCUPANCY;
    }

    private static AABB transformedInventoryBounds(AABB bounds) {
        double sin = LegacyTransformedBounds.sinDeg(90.0F);
        double cos = LegacyTransformedBounds.cosDeg(90.0F);
        return LegacyTransformedBounds.transform(bounds, (x, y, z, accumulator) -> {
            double rotatedX = LegacyTransformedBounds.rotateYX(x + 1.0D, z, sin, cos);
            double rotatedZ = LegacyTransformedBounds.rotateYZ(x + 1.0D, z, sin, cos);
            accumulator.include(rotatedX * LEGACY_INVENTORY_SCALE,
                    y * LEGACY_INVENTORY_SCALE,
                    rotatedZ * LEGACY_INVENTORY_SCALE);
        });
    }

    private static AABB transformedCommonBounds(AABB bounds) {
        double sin = LegacyTransformedBounds.sinDeg(90.0F);
        double cos = LegacyTransformedBounds.cosDeg(90.0F);
        return LegacyTransformedBounds.transform(bounds,
                (x, y, z, accumulator) -> LegacyTransformedBounds.includeRotatedY(accumulator,
                        x + 1.0D, y, z, sin, cos));
    }
}
