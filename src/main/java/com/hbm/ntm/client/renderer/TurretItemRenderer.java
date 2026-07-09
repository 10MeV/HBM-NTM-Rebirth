package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.SingleTurretBlock;
import com.hbm.ntm.block.TurretBaseBlock;
import com.hbm.ntm.client.obj.ObjTurretModels;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TurretItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float LEGACY_GUI_MODEL_SCALE = 1.0F / 16.0F;
    private static final float WORLD_TARGET_SIZE = 0.86F;

    public static final TurretItemRenderer INSTANCE = new TurretItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private TurretItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }
        Block block = blockItem.getBlock();
        if (!(block instanceof TurretBaseBlock) && !(block instanceof SingleTurretBlock)) {
            return;
        }

        TurretBlockEntityRenderer.StaticTurretModel model = TurretBlockEntityRenderer.staticModelForBlock(block);
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyGuiDisplayTransform(poseStack);
            applyLegacyInventoryTransform(model, poseStack);
        } else {
            AABB bounds = legacyCommonBounds(model);
            Vec3 center = bounds.getCenter();
            double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
            applyNonGuiDisplayTransform(displayContext, poseStack, center, maxSize);
        }
        TurretBlockEntityRenderer.renderLegacyItemModel(model, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyLegacyGuiDisplayTransform(PoseStack poseStack) {
        poseStack.translate(0.5D, 0.625D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.scale(LEGACY_GUI_MODEL_SCALE, LEGACY_GUI_MODEL_SCALE, LEGACY_GUI_MODEL_SCALE);
    }

    private static void applyNonGuiDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack,
            Vec3 center, double maxSize) {
        float fitScale = (float) Math.max(0.035D, Math.min(0.32D,
                WORLD_TARGET_SIZE / Math.max(1.0D, maxSize)));

        poseStack.translate(0.5D, 0.5D, 0.5D);
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

    private static AABB legacyCommonBounds(TurretBlockEntityRenderer.StaticTurretModel model) {
        return switch (model) {
            case CHEKHOV, FRIENDLY -> transformedTranslate(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base", "Carriage", "Body", "Barrels"),
                    -0.75D, 0.0D, 0.0D);
            case JEREMY -> transformedTranslate(union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base", "Carriage"),
                    ObjTurretModels.JEREMY.boundsOnly("Gun")),
                    -0.5D, 0.0D, 0.0D);
            case RICHARD -> union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base", "Carriage"),
                    ObjTurretModels.RICHARD.boundsOnly("Launcher"));
            case TAUON -> union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base", "Carriage"),
                    ObjTurretModels.TAUON.boundsOnly("Cannon", "Rotor"));
            case HOWARD -> transformedTranslate(union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base"),
                    ObjTurretModels.HOWARD.boundsOnly("Carriage", "Body", "BarrelsTop", "BarrelsBottom")),
                    -0.75D, 0.0D, 0.0D);
            case HOWARD_DAMAGED -> transformedTranslate(union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base"),
                    ObjTurretModels.HOWARD.boundsOnly("Carriage"),
                    ObjTurretModels.HOWARD_DAMAGED.boundsOnly("Body", "BarrelsTop", "BarrelsBottom")),
                    -0.75D, 0.0D, 0.0D);
            case SENTRY, SENTRY_DAMAGED -> transformedRotateY(
                    ObjTurretModels.SENTRY.boundsOnly("Base", "Pivot", "Body", "Drum", "BarrelL", "BarrelR"),
                    90.0F, 1.0D);
            case MAXWELL -> union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base"),
                    ObjTurretModels.HOWARD.boundsOnly("Carriage"),
                    ObjTurretModels.MAXWELL.boundsOnly("Microwave"));
            case ARTY -> union(
                    transformedRotateY(ObjTurretModels.ARTY.boundsOnly("Base", "Carriage"), -90.0F, 0.5D),
                    transformedArtyBarrelBounds(ObjTurretModels.ARTY.boundsOnly("Cannon", "Barrel")));
            case HIMARS -> transformedRotateY(
                    union(ObjTurretModels.ARTY.boundsOnly("Base"),
                            ObjTurretModels.HIMARS.boundsOnly("Carriage", "Launcher", "Crane", "TubeStandard")),
                    -90.0F, 0.5D);
            case FRITZ -> union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base", "Carriage"),
                    ObjTurretModels.FRITZ.boundsOnly("Gun"));
        };
    }

    private static void applyLegacyInventoryTransform(TurretBlockEntityRenderer.StaticTurretModel model,
            PoseStack poseStack) {
        poseStack.translate(legacyInventoryTranslationX(model), legacyInventoryTranslationY(model), 0.0D);
        float scale = legacyInventoryScale(model);
        poseStack.scale(scale, scale, scale);
    }

    private static double legacyInventoryTranslationX(TurretBlockEntityRenderer.StaticTurretModel model) {
        return switch (model) {
            case MAXWELL -> -1.0D;
            case ARTY -> -3.0D;
            default -> 0.0D;
        };
    }

    private static double legacyInventoryTranslationY(TurretBlockEntityRenderer.StaticTurretModel model) {
        return switch (model) {
            case CHEKHOV, FRIENDLY, MAXWELL -> -3.0D;
            case JEREMY, RICHARD, TAUON, FRITZ, HIMARS -> -2.0D;
            case HOWARD, HOWARD_DAMAGED -> -4.5D;
            case SENTRY, SENTRY_DAMAGED, ARTY -> -4.0D;
        };
    }

    private static float legacyInventoryScale(TurretBlockEntityRenderer.StaticTurretModel model) {
        return switch (model) {
            case JEREMY -> 2.5F;
            case RICHARD, TAUON -> 5.0F;
            case SENTRY, SENTRY_DAMAGED -> 7.0F;
            case ARTY, HIMARS -> 3.5F;
            default -> 4.0F;
        };
    }

    private static AABB union(AABB first, AABB second, AABB... rest) {
        AABB result = first.minmax(second);
        for (AABB bounds : rest) {
            result = result.minmax(bounds);
        }
        return result;
    }

    private static AABB transformedTranslate(AABB bounds, double xOffset, double yOffset, double zOffset) {
        return LegacyTransformedBounds.transform(bounds,
                (x, y, z, accumulator) -> accumulator.include(x + xOffset, y + yOffset, z + zOffset));
    }

    private static AABB transformedRotateY(AABB bounds, float degrees, double scale) {
        double sin = LegacyTransformedBounds.sinDeg(degrees);
        double cos = LegacyTransformedBounds.cosDeg(degrees);
        return LegacyTransformedBounds.transform(bounds, (x, y, z, accumulator) -> {
            double scaledX = x * scale;
            double scaledY = y * scale;
            double scaledZ = z * scale;
            LegacyTransformedBounds.includeRotatedY(accumulator, scaledX, scaledY, scaledZ, sin, cos);
        });
    }

    private static AABB transformedArtyBarrelBounds(AABB bounds) {
        double sinX = LegacyTransformedBounds.sinDeg(45.0F);
        double cosX = LegacyTransformedBounds.cosDeg(45.0F);
        double sinY = LegacyTransformedBounds.sinDeg(-90.0F);
        double cosY = LegacyTransformedBounds.cosDeg(-90.0F);
        return LegacyTransformedBounds.transform(bounds, (x, y, z, accumulator) -> {
            double pivotY = y - 3.0D;
            double rotatedY = pivotY * cosX - z * sinX + 3.0D;
            double rotatedZ = pivotY * sinX + z * cosX;
            double scaledX = x * 0.5D;
            double scaledY = rotatedY * 0.5D;
            double scaledZ = rotatedZ * 0.5D;
            LegacyTransformedBounds.includeRotatedY(accumulator, scaledX, scaledY, scaledZ, sinY, cosY);
        });
    }
}
