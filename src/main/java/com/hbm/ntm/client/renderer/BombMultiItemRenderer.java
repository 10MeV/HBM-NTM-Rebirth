package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.BombMultiBlock;
import com.hbm.ntm.client.obj.ObjNukeModels;
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

public class BombMultiItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float LEGACY_GUI_SLOT_PIXELS = 16.0F;
    private static final float LEGACY_GUI_MAX_OCCUPANCY = 0.86F;
    private static final float LEGACY_INVENTORY_SCALE = 4.0F;

    public static final BombMultiItemRenderer INSTANCE = new BombMultiItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private BombMultiItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof BombMultiBlock)) {
            return;
        }

        boolean gui = displayContext == ItemDisplayContext.GUI;
        AABB bounds = gui ? transformedInventoryBounds(ObjNukeModels.BOMB_MULTI_LEGACY.boundsAll())
                : transformedCommonBounds(ObjNukeModels.BOMB_MULTI_LEGACY.boundsAll());
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));

        poseStack.pushPose();
        applyBaseDisplay(displayContext, poseStack, center, maxSize, gui);
        if (gui) {
            poseStack.translate(0.0D, -1.0D, 0.0D);
            poseStack.scale(LEGACY_INVENTORY_SCALE, LEGACY_INVENTORY_SCALE, LEGACY_INVENTORY_SCALE);
        }
        BombMultiRenderer.applyLegacyItemCommon(poseStack);
        BombMultiRenderer.renderModel(poseStack, buffer, packedLight, packedOverlay);
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
        double sinY = LegacyTransformedBounds.sinDeg(90.0F);
        double cosY = LegacyTransformedBounds.cosDeg(90.0F);
        double sinX = LegacyTransformedBounds.sinDeg(180.0F);
        double cosX = LegacyTransformedBounds.cosDeg(180.0F);
        return LegacyTransformedBounds.transform(bounds, (x, y, z, accumulator) -> {
            double rotatedYx = LegacyTransformedBounds.rotateYX(x, z, sinY, cosY);
            double rotatedYz = LegacyTransformedBounds.rotateYZ(x, z, sinY, cosY);
            double rotatedXy = y * cosX - rotatedYz * sinX;
            double rotatedXz = y * sinX + rotatedYz * cosX;
            double commonX = rotatedYx * 3.0D + 0.75D;
            double commonY = (rotatedXy + 0.5D) * 3.0D;
            double commonZ = rotatedXz * 3.0D;
            accumulator.include(commonX * LEGACY_INVENTORY_SCALE,
                    commonY * LEGACY_INVENTORY_SCALE - 1.0D,
                    commonZ * LEGACY_INVENTORY_SCALE);
        });
    }

    private static AABB transformedCommonBounds(AABB bounds) {
        double sinY = LegacyTransformedBounds.sinDeg(90.0F);
        double cosY = LegacyTransformedBounds.cosDeg(90.0F);
        double sinX = LegacyTransformedBounds.sinDeg(180.0F);
        double cosX = LegacyTransformedBounds.cosDeg(180.0F);
        return LegacyTransformedBounds.transform(bounds, (x, y, z, accumulator) -> {
            double rotatedYx = LegacyTransformedBounds.rotateYX(x, z, sinY, cosY);
            double rotatedYz = LegacyTransformedBounds.rotateYZ(x, z, sinY, cosY);
            double rotatedXy = y * cosX - rotatedYz * sinX;
            double rotatedXz = y * sinX + rotatedYz * cosX;
            accumulator.include(rotatedYx * 3.0D + 0.75D,
                    (rotatedXy + 0.5D) * 3.0D,
                    rotatedXz * 3.0D);
        });
    }
}
