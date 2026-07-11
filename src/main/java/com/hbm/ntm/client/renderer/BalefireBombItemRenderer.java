package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.BalefireBombBlock;
import com.hbm.ntm.client.obj.ObjBombModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class BalefireBombItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float LEGACY_GUI_SLOT_PIXELS = 16.0F;
    private static final float LEGACY_GUI_MAX_OCCUPANCY = 0.86F;
    private static final float LEGACY_INVENTORY_SCALE = 2.25F;
    private static final RenderSpec INVENTORY_SPEC =
            renderSpec(transformedInventoryBounds(ObjBombModels.FSTBMB.boundsAll()), true);
    private static final RenderSpec COMMON_SPEC =
            renderSpec(transformedCommonBounds(ObjBombModels.FSTBMB.boundsAll()), false);

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
        RenderSpec spec = gui ? INVENTORY_SPEC : COMMON_SPEC;

        poseStack.pushPose();
        applyBaseDisplay(displayContext, poseStack, spec, gui);
        if (gui) {
            poseStack.scale(LEGACY_INVENTORY_SCALE, LEGACY_INVENTORY_SCALE, LEGACY_INVENTORY_SCALE);
        }
        BalefireBombRenderer.applyLegacyItemCommon(poseStack);
        BalefireBombRenderer.renderModel(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyBaseDisplay(ItemDisplayContext displayContext, PoseStack poseStack, RenderSpec spec,
            boolean gui) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.scale(spec.fitScale(), spec.fitScale(), spec.fitScale());
            poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());
            return;
        }

        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        float worldScale = spec.fitScale() * 0.82F;
        poseStack.scale(worldScale, worldScale, worldScale);
        poseStack.translate(-spec.centerX(), -spec.centerY(), -spec.centerZ());

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

    private static RenderSpec renderSpec(AABB bounds, boolean gui) {
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        return new RenderSpec(
                (bounds.minX + bounds.maxX) * 0.5D,
                (bounds.minY + bounds.maxY) * 0.5D,
                (bounds.minZ + bounds.maxZ) * 0.5D,
                (float) Math.max(0.035D, Math.min(0.32D,
                        targetDisplaySize(gui, maxSize) / Math.max(1.0D, maxSize))));
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

    private record RenderSpec(double centerX, double centerY, double centerZ, float fitScale) {
    }
}
