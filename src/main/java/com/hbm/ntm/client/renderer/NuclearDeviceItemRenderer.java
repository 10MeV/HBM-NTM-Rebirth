package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.NuclearDeviceBlock;
import com.hbm.ntm.block.CustomNukeBlock;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
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

public class NuclearDeviceItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float LEGACY_GUI_SLOT_PIXELS = 16.0F;
    private static final float LEGACY_GUI_MAX_OCCUPANCY = 0.86F;

    public static final NuclearDeviceItemRenderer INSTANCE = new NuclearDeviceItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private NuclearDeviceItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        if (blockItem.getBlock() instanceof CustomNukeBlock) {
            renderCustomNuke(displayContext, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        if (!(blockItem.getBlock() instanceof NuclearDeviceBlock block)) {
            return;
        }

        NuclearDeviceBlock.Kind kind = block.kind();
        LegacyWavefrontModel model = NuclearDeviceRenderer.model(kind);
        boolean gui = displayContext == ItemDisplayContext.GUI;
        AABB bounds = gui ? transformedInventoryBounds(kind, model.boundsAll()) : transformedCommonBounds(kind, model.boundsAll());
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));

        poseStack.pushPose();
        applyBaseDisplay(displayContext, poseStack, center, maxSize, gui);
        if (gui) {
            applyLegacyInventory(kind, poseStack);
        }
        applyLegacyCommon(kind, poseStack);
        NuclearDeviceRenderer.renderKind(kind, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderCustomNuke(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel model = ObjNukeModels.BOY;
        boolean gui = displayContext == ItemDisplayContext.GUI;
        AABB bounds = gui
                ? transformBounds(model.boundsAll(), point -> point.add(-1.0D, 0.0D, 0.0D).scale(5.0D))
                : transformBounds(model.boundsAll(), point -> point.add(-1.0D, 0.0D, 0.0D));
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));

        poseStack.pushPose();
        applyBaseDisplay(displayContext, poseStack, center, maxSize, gui);
        if (gui) {
            poseStack.scale(5.0F, 5.0F, 5.0F);
        }
        NuclearDeviceRenderer.applyCustomNukeLegacyItemCommon(poseStack);
        NuclearDeviceRenderer.renderCustomNuke(poseStack, buffer, packedLight, packedOverlay);
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

    private static void applyLegacyCommon(NuclearDeviceBlock.Kind kind, PoseStack poseStack) {
        switch (kind) {
            case GADGET -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case BOY -> poseStack.translate(-1.0D, 0.0D, 0.0D);
            case MAN -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.translate(-0.75D, 0.0D, 0.0D);
            }
            case TSAR -> poseStack.translate(1.5D, 0.0D, 0.0D);
            case PROTOTYPE -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.translate(0.0D, 0.125D, 0.0D);
            }
            case FLEIJA -> {
                poseStack.translate(0.125D, 0.0D, 0.0D);
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case SOLINIUM -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.translate(0.0D, -0.125D, 0.0D);
            }
            case MIKE, N2 -> {
            }
        }
    }

    private static Vec3 applyLegacyCommonTransform(NuclearDeviceBlock.Kind kind, Vec3 point) {
        return switch (kind) {
            case GADGET -> rotateY(point, -90.0F);
            case BOY -> point.add(-1.0D, 0.0D, 0.0D);
            case MAN -> rotateY(point.add(-0.75D, 0.0D, 0.0D), 180.0F);
            case TSAR -> point.add(1.5D, 0.0D, 0.0D);
            case PROTOTYPE -> rotateY(point.add(0.0D, 0.125D, 0.0D), 90.0F);
            case FLEIJA -> rotateY(point, 90.0F).add(0.125D, 0.0D, 0.0D);
            case SOLINIUM -> rotateY(point.add(0.0D, -0.125D, 0.0D), 90.0F);
            case MIKE, N2 -> point;
        };
    }

    private static void applyLegacyInventory(NuclearDeviceBlock.Kind kind, PoseStack poseStack) {
        Vec3 translation = legacyInventoryTranslation(kind);
        poseStack.translate(translation.x, translation.y, translation.z);
        float scale = legacyInventoryScale(kind);
        poseStack.scale(scale, scale, scale);
    }

    private static AABB transformedInventoryBounds(NuclearDeviceBlock.Kind kind, AABB bounds) {
        return transformBounds(bounds, point -> {
            Vec3 result = applyLegacyCommonTransform(kind, point);
            float scale = legacyInventoryScale(kind);
            result = new Vec3(result.x * scale, result.y * scale, result.z * scale);
            return result.add(legacyInventoryTranslation(kind));
        });
    }

    private static AABB transformedCommonBounds(NuclearDeviceBlock.Kind kind, AABB bounds) {
        return transformBounds(bounds, point -> applyLegacyCommonTransform(kind, point));
    }

    private static Vec3 legacyInventoryTranslation(NuclearDeviceBlock.Kind kind) {
        return switch (kind) {
            case GADGET -> new Vec3(0.0D, -3.0D, 0.0D);
            case MAN -> new Vec3(0.0D, -2.0D, 0.0D);
            case MIKE, N2 -> new Vec3(0.0D, -5.0D, 0.0D);
            case PROTOTYPE -> new Vec3(0.0D, 0.125D, 0.0D);
            case SOLINIUM -> new Vec3(0.0D, -0.125D, 0.0D);
            default -> Vec3.ZERO;
        };
    }

    private static float legacyInventoryScale(NuclearDeviceBlock.Kind kind) {
        return switch (kind) {
            case GADGET, MAN, SOLINIUM -> 5.0F;
            case PROTOTYPE -> 3.0F;
            case MIKE, TSAR, N2 -> 2.25F;
            case FLEIJA -> 6.8F;
            case BOY -> 5.0F;
        };
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
