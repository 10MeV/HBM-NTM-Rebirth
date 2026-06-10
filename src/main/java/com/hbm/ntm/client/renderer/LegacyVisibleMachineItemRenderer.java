package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.LegacyConnectorBlock;
import com.hbm.ntm.block.LegacyLargePylonBlock;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMediumPylonBlock;
import com.hbm.ntm.block.LegacySmallPylonBlock;
import com.hbm.ntm.block.LegacySubstationBlock;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.block.AssemblyMachineBlock;
import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.block.VendingMachineBlock;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.LegacyStateMultiblockBlockItem;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjNetworkModels;
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
                || blockItem.getBlock() instanceof MachineBatterySocketBlock
                || blockItem.getBlock() instanceof VendingMachineBlock
                || blockItem.getBlock() instanceof LegacyConnectorBlock
                || blockItem.getBlock() instanceof LegacySmallPylonBlock
                || blockItem.getBlock() instanceof LegacyMediumPylonBlock
                || blockItem.getBlock() instanceof LegacyLargePylonBlock
                || blockItem.getBlock() instanceof LegacySubstationBlock)) {
            return;
        }

        if (blockItem.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block) {
            renderVisibleMachineItem(block, displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof AssemblyMachineBlock) {
            renderAssemblyMachineItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof MachineBatterySocketBlock) {
            renderBatterySocketItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof VendingMachineBlock block) {
            renderVendingMachineItem(block, stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacyConnectorBlock connector) {
            renderConnectorItem(connector.kind(), displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacySmallPylonBlock) {
            renderSmallPylonItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacyMediumPylonBlock pylon) {
            renderMediumPylonItem(pylon.kind(), displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacyLargePylonBlock) {
            renderLargePylonItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof LegacySubstationBlock) {
            renderSubstationItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
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

    private static void renderVendingMachineItem(VendingMachineBlock block, ItemStack stack,
            ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        int variant = 0;
        if (stack.getItem() instanceof LegacyStateBlockItem item) {
            variant = item.getVariant(stack);
        } else if (stack.getItem() instanceof LegacyStateMultiblockBlockItem item) {
            variant = item.getVariant(stack);
        }
        BlockState state = block.defaultBlockState()
                .setValue(HorizontalMachineBlock.FACING, Direction.SOUTH)
                .setValue(VendingMachineBlock.VARIANT, variant);
        String part = variant == 0 ? "Soda" : "Obamna";
        AABB bounds = VendingMachineRenderer.MODEL.boundsOnly(part);

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 6.25F);
        VendingMachineRenderer.render(state, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderMediumPylonItem(LegacyMediumPylonBlock.Kind kind, ItemDisplayContext displayContext,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB rawBounds = kind.transformer()
                ? ObjNetworkModels.PYLON_MEDIUM_LEGACY.boundsOnly("Pylon", "Transformer")
                : ObjNetworkModels.PYLON_MEDIUM_LEGACY.boundsOnly("Pylon");
        AABB bounds = transformBounds(rawBounds,
                point -> rotateY(point.scale(0.5D).add(0.75D, 0.0D, 0.0D), 90.0F).add(0.5D, 0.0D, 0.5D));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 4.5F);
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.translate(0.75D, 0.0D, 0.0D);
        ObjNetworkModels.PYLON_MEDIUM_LEGACY.renderPart("Pylon",
                kind.steel() ? LegacyPylonRenderer.PYLON_MEDIUM_STEEL_TEXTURE : LegacyPylonRenderer.PYLON_MEDIUM_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        if (kind.transformer()) {
            ObjNetworkModels.PYLON_MEDIUM_LEGACY.renderPart("Transformer",
                    kind.steel() ? LegacyPylonRenderer.PYLON_MEDIUM_STEEL_TEXTURE : LegacyPylonRenderer.PYLON_MEDIUM_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderSmallPylonItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, LegacySmallPylonModel.LEGACY_RENDER_BOUNDS, 0.58F, 4.5F);
        LegacySmallPylonModel.render(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderConnectorItem(LegacyConnectorBlock.Kind kind, ItemDisplayContext displayContext,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB rawBounds = (kind == LegacyConnectorBlock.Kind.SUPER
                ? ObjNetworkModels.CONNECTOR_SUPER_LEGACY
                : ObjNetworkModels.CONNECTOR_LEGACY).boundsAll();
        AABB bounds = transformBounds(rawBounds, point -> point.scale(2.0D));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 7.0F);
        poseStack.scale(2.0F, 2.0F, 2.0F);
        if (kind == LegacyConnectorBlock.Kind.SUPER) {
            ObjNetworkModels.CONNECTOR_SUPER_LEGACY.renderAll(LegacyPylonRenderer.CONNECTOR_SUPER_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
        } else {
            ObjNetworkModels.CONNECTOR_LEGACY.renderAll(LegacyPylonRenderer.CONNECTOR_TEXTURE,
                    poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderLargePylonItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(ObjNetworkModels.PYLON_LARGE_LEGACY.boundsAll(),
                point -> point.scale(0.5D));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 2.25F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjNetworkModels.PYLON_LARGE_LEGACY.renderAll(LegacyPylonRenderer.PYLON_LARGE_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderSubstationItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(ObjNetworkModels.SUBSTATION_LEGACY.boundsAll(), point -> point.scale(0.5D));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 4.5F);
        poseStack.scale(0.5F, 0.5F, 0.5F);
        ObjNetworkModels.SUBSTATION_LEGACY.renderAll(LegacyPylonRenderer.SUBSTATION_TEXTURE,
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
        float postModelYRotation = definition.postModelYRotation(state);
        return transformBounds(bounds, point -> rotateY(
                rotateY(point, postModelYRotation).add(translation), yRotation).add(0.5D, 0.0D, 0.5D));
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
        poseStack.mulPose(Axis.YP.rotationDegrees(definition.postModelYRotation(state)));

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
