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
    private static final float LEGACY_GUI_SLOT_PIXELS = 16.0F;
    private static final float LEGACY_GUI_MAX_OCCUPANCY = 0.86F;

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
        AABB bounds = transformedBounds(model, displayContext == ItemDisplayContext.GUI);
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, center, maxSize);
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyInventoryTransform(model, poseStack);
        }
        TurretBlockEntityRenderer.renderLegacyItemModel(model, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack,
            Vec3 center, double maxSize) {
        float fitScale = (float) Math.max(0.035D, Math.min(0.32D,
                targetDisplaySize(displayContext == ItemDisplayContext.GUI, maxSize) / Math.max(1.0D, maxSize)));

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

    private static AABB transformedBounds(TurretBlockEntityRenderer.StaticTurretModel model, boolean inventory) {
        AABB bounds = legacyCommonBounds(model);
        if (!inventory) {
            return bounds;
        }
        Vec3 translation = legacyInventoryTranslation(model);
        float scale = legacyInventoryScale(model);
        return transformBounds(bounds, point -> point.scale(scale).add(translation));
    }

    private static AABB legacyCommonBounds(TurretBlockEntityRenderer.StaticTurretModel model) {
        return switch (model) {
            case CHEKHOV, FRIENDLY -> transformBounds(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base", "Carriage", "Body", "Barrels"),
                    point -> point.add(-0.75D, 0.0D, 0.0D));
            case JEREMY -> transformBounds(union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base", "Carriage"),
                    ObjTurretModels.JEREMY.boundsOnly("Gun")),
                    point -> point.add(-0.5D, 0.0D, 0.0D));
            case RICHARD -> union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base", "Carriage"),
                    ObjTurretModels.RICHARD.boundsOnly("Launcher"));
            case TAUON -> union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base", "Carriage"),
                    ObjTurretModels.TAUON.boundsOnly("Cannon", "Rotor"));
            case HOWARD, HOWARD_DAMAGED -> transformBounds(union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base"),
                    (model == TurretBlockEntityRenderer.StaticTurretModel.HOWARD
                            ? ObjTurretModels.HOWARD
                            : ObjTurretModels.HOWARD_DAMAGED).boundsOnly("Carriage", "Body", "BarrelsTop", "BarrelsBottom")),
                    point -> point.add(-0.75D, 0.0D, 0.0D));
            case SENTRY, SENTRY_DAMAGED -> transformBounds(
                    ObjTurretModels.SENTRY.boundsOnly("Base", "Pivot", "Body", "Drum", "BarrelL", "BarrelR"),
                    point -> rotateY(point, 90.0F));
            case MAXWELL -> union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base"),
                    ObjTurretModels.HOWARD.boundsOnly("Carriage"),
                    ObjTurretModels.MAXWELL.boundsOnly("Microwave"));
            case ARTY -> union(
                    transformBounds(ObjTurretModels.ARTY.boundsOnly("Base", "Carriage"),
                            point -> rotateY(point.scale(0.5D), -90.0F)),
                    transformBounds(ObjTurretModels.ARTY.boundsOnly("Cannon", "Barrel"),
                            point -> rotateY(rotateX(point, 45.0F, 3.0D, 0.0D).scale(0.5D), -90.0F)));
            case HIMARS -> transformBounds(
                    union(ObjTurretModels.ARTY.boundsOnly("Base"),
                            ObjTurretModels.HIMARS.boundsOnly("Carriage", "Launcher", "Crane", "TubeStandard")),
                    point -> rotateY(point.scale(0.5D), -90.0F));
            case FRITZ -> union(
                    ObjTurretModels.CHEKHOV.boundsOnly("Base", "Carriage"),
                    ObjTurretModels.FRITZ.boundsOnly("Gun"));
        };
    }

    private static void applyLegacyInventoryTransform(TurretBlockEntityRenderer.StaticTurretModel model,
            PoseStack poseStack) {
        Vec3 translation = legacyInventoryTranslation(model);
        poseStack.translate(translation.x, translation.y, translation.z);
        float scale = legacyInventoryScale(model);
        poseStack.scale(scale, scale, scale);
    }

    private static Vec3 legacyInventoryTranslation(TurretBlockEntityRenderer.StaticTurretModel model) {
        return switch (model) {
            case CHEKHOV, FRIENDLY -> new Vec3(0.0D, -3.0D, 0.0D);
            case JEREMY, RICHARD, TAUON, FRITZ, HIMARS -> new Vec3(0.0D, -2.0D, 0.0D);
            case HOWARD, HOWARD_DAMAGED -> new Vec3(0.0D, -4.5D, 0.0D);
            case SENTRY, SENTRY_DAMAGED -> new Vec3(0.0D, -4.0D, 0.0D);
            case MAXWELL -> new Vec3(-1.0D, -3.0D, 0.0D);
            case ARTY -> new Vec3(-3.0D, -4.0D, 0.0D);
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

    private static AABB transformBounds(AABB bounds, PointTransform transform) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        for (double x : new double[] { bounds.minX, bounds.maxX }) {
            for (double y : new double[] { bounds.minY, bounds.maxY }) {
                for (double z : new double[] { bounds.minZ, bounds.maxZ }) {
                    Vec3 point = transform.apply(new Vec3(x, y, z));
                    minX = Math.min(minX, point.x);
                    minY = Math.min(minY, point.y);
                    minZ = Math.min(minZ, point.z);
                    maxX = Math.max(maxX, point.x);
                    maxY = Math.max(maxY, point.y);
                    maxZ = Math.max(maxZ, point.z);
                }
            }
        }

        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static Vec3 rotateX(Vec3 point, float degrees, double pivotY, double pivotZ) {
        double radians = Math.toRadians(degrees);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double y = point.y - pivotY;
        double z = point.z - pivotZ;
        return new Vec3(point.x, y * cos - z * sin + pivotY, z * cos + y * sin + pivotZ);
    }

    private static Vec3 rotateY(Vec3 point, float degrees) {
        double radians = Math.toRadians(degrees);
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        return new Vec3(point.x * cos + point.z * sin, point.y, point.z * cos - point.x * sin);
    }

    private interface PointTransform {
        Vec3 apply(Vec3 point);
    }
}
