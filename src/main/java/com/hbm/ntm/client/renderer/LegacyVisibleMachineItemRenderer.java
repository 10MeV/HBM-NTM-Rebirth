package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.block.AssemblyMachineBlock;
import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.Map;

public class LegacyVisibleMachineItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float LEGACY_GUI_SLOT_PIXELS = 16.0F;
    private static final float LEGACY_GUI_MAX_OCCUPANCY = 0.86F;
    private static final Map<LegacyMachineDefinition, LegacyWavefrontModel> MODELS = new IdentityHashMap<>();

    public static final LegacyVisibleMachineItemRenderer INSTANCE = new LegacyVisibleMachineItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private LegacyVisibleMachineItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof LegacyVisibleMultiblockMachineBlock
                || blockItem.getBlock() instanceof AssemblyMachineBlock
                || blockItem.getBlock() instanceof MachineBatterySocketBlock)) {
            return;
        }

        if (blockItem.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block) {
            renderVisibleMachineItem(block, displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof AssemblyMachineBlock) {
            renderAssemblyMachineItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof MachineBatterySocketBlock) {
            renderBatterySocketItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderVisibleMachineItem(LegacyVisibleMultiblockMachineBlock block, ItemDisplayContext displayContext,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyMachineDefinition definition = block.definition();
        BlockState state = itemState(block);
        LegacyWavefrontModel model = MODELS.computeIfAbsent(definition,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()));
        AABB rawBounds = definition.itemRenderAll()
                ? model.boundsAll()
                : model.boundsOnly(definition.itemRenderParts().toArray(String[]::new));
        AABB bounds = transformVisibleBounds(rawBounds, definition, state);

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, definition.itemFitSize(), definition.legacyItemScale());
        renderMachine(definition, state, model, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderAssemblyMachineItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(AssemblyMachineRenderer.MODEL.boundsAll(),
                point -> rotateY(point.scale(0.75D), 90.0F).add(0.5D, 0.0D, 0.5D));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 4.5F);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(0.75F, 0.75F, 0.75F);
        AssemblyMachineRenderer.MODEL.renderAll(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderBatterySocketItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = MachineBatterySocketRenderer.MODEL.boundsOnly("Socket");

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 5.0F);
        MachineBatterySocketRenderer.MODEL.renderPart("Socket", MachineBatterySocketRenderer.SOCKET_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static BlockState itemState(LegacyVisibleMultiblockMachineBlock block) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(HorizontalMachineBlock.FACING)) {
            state = state.setValue(HorizontalMachineBlock.FACING, Direction.SOUTH);
        }
        return state;
    }

    private static void applyDisplayTransform(ItemDisplayContext displayContext, PoseStack poseStack, AABB bounds,
            float targetSize, float legacyItemScale) {
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        float resolvedTargetSize = targetSize;
        if (legacyItemScale > 0.0F) {
            resolvedTargetSize = (float) Math.min(LEGACY_GUI_MAX_OCCUPANCY,
                    maxSize * legacyItemScale / LEGACY_GUI_SLOT_PIXELS);
        }
        float fitScale = (float) Math.max(0.035D,
                Math.min(0.32D, resolvedTargetSize / Math.max(1.0D, maxSize)));

        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
            poseStack.scale(fitScale, fitScale, fitScale);
            poseStack.translate(-center.x, -center.y, -center.z);
            return;
        }

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

    private static AABB transformVisibleBounds(AABB bounds, LegacyMachineDefinition definition, BlockState state) {
        Vec3 translation = definition.modelTranslation(state);
        float yRotation = definition.yRotation(state);
        return transformBounds(bounds, point -> rotateY(point.add(translation), yRotation).add(0.5D, 0.0D, 0.5D));
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

    private static void renderMachine(LegacyMachineDefinition definition, BlockState state, LegacyWavefrontModel model,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.yRotation(state)));
        Vec3 translation = definition.modelTranslation(state);
        poseStack.translate(translation.x, translation.y, translation.z);

        if (definition.itemRenderAll()) {
            model.renderAll(definition.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        } else {
            for (String part : definition.itemRenderParts()) {
                model.renderPart(part, definition.itemPartTextures().getOrDefault(part,
                                definition.partTextures().getOrDefault(part, definition.textureLocation())),
                        poseStack, buffer, packedLight, packedOverlay);
            }
        }

        poseStack.popPose();
    }

    private interface PointTransform {
        Vec3 apply(Vec3 point);
    }
}
