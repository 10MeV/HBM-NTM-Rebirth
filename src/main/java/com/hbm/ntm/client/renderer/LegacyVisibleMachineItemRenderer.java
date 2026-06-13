package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.block.CargoElevatorBlock;
import com.hbm.ntm.block.ElectricPressBlock;
import com.hbm.ntm.block.LegacyConnectorBlock;
import com.hbm.ntm.block.LegacyLargePylonBlock;
import com.hbm.ntm.block.LegacyMachineDefinition;
import com.hbm.ntm.block.LegacyMachinePartRenderProperties;
import com.hbm.ntm.block.LegacyMachineRenderProfile;
import com.hbm.ntm.block.LegacyMediumPylonBlock;
import com.hbm.ntm.block.LegacySmallPylonBlock;
import com.hbm.ntm.block.LegacySubstationBlock;
import com.hbm.ntm.block.LegacyVisibleMultiblockMachineBlock;
import com.hbm.ntm.block.RadioAutocalBlock;
import com.hbm.ntm.block.RadioTelexBlock;
import com.hbm.ntm.block.AssemblyMachineBlock;
import com.hbm.ntm.block.MachineBatterySocketBlock;
import com.hbm.ntm.block.MachineLpw2Block;
import com.hbm.ntm.block.VendingMachineBlock;
import com.hbm.ntm.item.LegacyStateBlockItem;
import com.hbm.ntm.item.LegacyStateMultiblockBlockItem;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.client.obj.ObjMachineModels;
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
                || blockItem.getBlock() instanceof MachineLpw2Block
                || blockItem.getBlock() instanceof CargoElevatorBlock
                || blockItem.getBlock() instanceof ElectricPressBlock
                || blockItem.getBlock() instanceof VendingMachineBlock
                || blockItem.getBlock() instanceof LegacyConnectorBlock
                || blockItem.getBlock() instanceof LegacySmallPylonBlock
                || blockItem.getBlock() instanceof LegacyMediumPylonBlock
                || blockItem.getBlock() instanceof LegacyLargePylonBlock
                || blockItem.getBlock() instanceof LegacySubstationBlock
                || blockItem.getBlock() instanceof RadioAutocalBlock
                || blockItem.getBlock() instanceof RadioTelexBlock)) {
            return;
        }

        if (blockItem.getBlock() instanceof LegacyVisibleMultiblockMachineBlock block) {
            renderVisibleMachineItem(block, displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof AssemblyMachineBlock) {
            renderAssemblyMachineItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof MachineBatterySocketBlock) {
            renderBatterySocketItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof MachineLpw2Block) {
            renderLpw2Item(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof CargoElevatorBlock) {
            renderCargoElevatorItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof ElectricPressBlock) {
            renderElectricPressItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
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
        } else if (blockItem.getBlock() instanceof RadioAutocalBlock) {
            renderAutocalItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
        } else if (blockItem.getBlock() instanceof RadioTelexBlock) {
            renderTelexItem(displayContext, poseStack, buffer, packedLight, packedOverlay);
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

    private static void renderElectricPressItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bodyBounds = ObjMachineModels.EPRESS_BODY.boundsAll();
        AABB headBounds = transformBounds(ObjMachineModels.EPRESS_HEAD.boundsAll(), point -> point.add(0.0D, 1.5D, 0.0D));
        AABB bounds = union(bodyBounds, headBounds);

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 4.5F);
        ObjMachineModels.EPRESS_BODY.renderAll(ObjMachineModels.EPRESS_BODY_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, 1.5D, 0.0D);
        ObjMachineModels.EPRESS_HEAD.renderAll(ObjMachineModels.EPRESS_HEAD_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderLpw2Item(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = com.hbm.ntm.client.obj.ObjReactorModels.LPW2.boundsAll();

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 0.0F);
        com.hbm.ntm.client.obj.ObjReactorModels.LPW2.renderAll(com.hbm.ntm.client.obj.ObjReactorModels.LPW2_TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderCargoElevatorItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB rawBounds = ObjMachineModels.ELEVATOR_LEGACY.boundsOnly("Base", "Piston", "Guides", "Platform");
        AABB piston2 = transformBounds(ObjMachineModels.ELEVATOR_LEGACY.boundsOnly("Piston", "Guides", "Platform"),
                point -> point.add(0.0D, 1.0D, 0.0D));
        AABB guides3 = transformBounds(ObjMachineModels.ELEVATOR_LEGACY.boundsOnly("Guides"),
                point -> point.add(0.0D, 2.0D, 0.0D));
        AABB bounds = union(union(rawBounds, piston2), guides3);

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 3.25F);
        ObjMachineModels.ELEVATOR_LEGACY.renderPart("Base", CargoElevatorRenderer.TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        ObjMachineModels.ELEVATOR_LEGACY.renderPart("Piston", CargoElevatorRenderer.TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        ObjMachineModels.ELEVATOR_LEGACY.renderPart("Guides", CargoElevatorRenderer.TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, 1.0D, 0.0D);
        ObjMachineModels.ELEVATOR_LEGACY.renderPart("Piston", CargoElevatorRenderer.TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        ObjMachineModels.ELEVATOR_LEGACY.renderPart("Guides", CargoElevatorRenderer.TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        ObjMachineModels.ELEVATOR_LEGACY.renderPart("Platform", CargoElevatorRenderer.TEXTURE,
                poseStack, buffer, packedLight, packedOverlay);
        poseStack.translate(0.0D, 1.0D, 0.0D);
        ObjMachineModels.ELEVATOR_LEGACY.renderPart("Guides", CargoElevatorRenderer.TEXTURE,
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

    private static void renderAutocalItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, RadioAutocalRenderer.MODEL.boundsAll(), 0.58F, 6.25F);
        RadioAutocalRenderer.MODEL.renderAll(RadioAutocalRenderer.TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderTelexItem(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        AABB bounds = transformBounds(RadioTelexRenderer.MODEL.boundsAll(), point -> point.add(0.0D, 0.0D, -0.5D));

        poseStack.pushPose();
        applyDisplayTransform(displayContext, poseStack, bounds, 0.58F, 6.0F);
        poseStack.translate(0.0D, 0.0D, -0.5D);
        RadioTelexRenderer.MODEL.renderAll(RadioTelexRenderer.TEXTURE, poseStack, buffer, packedLight, packedOverlay);
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

    private static AABB union(AABB first, AABB second) {
        return new AABB(
                Math.min(first.minX, second.minX),
                Math.min(first.minY, second.minY),
                Math.min(first.minZ, second.minZ),
                Math.max(first.maxX, second.maxX),
                Math.max(first.maxY, second.maxY),
                Math.max(first.maxZ, second.maxZ));
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
            ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
            if (!renderMachineProfile(definition, model, context, poseStack)) {
                renderMachineParts(definition, model, context);
            }
        }

        poseStack.popPose();
    }

    private static boolean renderMachineProfile(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context, PoseStack poseStack) {
        long currentMillis = System.currentTimeMillis();
        if (definition.renderProfile() == LegacyMachineRenderProfile.ANNIHILATOR_UV_SCROLL) {
            model.renderPart("Annihilator", definition.textureLocation(), context);
            renderRotatingPart(model, LegacyTileRenderPlans.annihilatorRollerPlan(currentMillis), context, poseStack);
            LegacyTileRenderPlans.TextureMatrixPartPlan belt = LegacyTileRenderPlans.annihilatorBeltPlan(currentMillis);
            model.renderPart(belt.partName(), definition.itemPartTextures().getOrDefault(belt.partName(),
                    definition.partTextures().getOrDefault(belt.partName(), definition.textureLocation())),
                    context.withTextureMatrixPlan(belt.textureMatrix()));
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.STEAM_ENGINE_ITEM_PREVIEW) {
            SteamEngineRenderer.renderPlan(model,
                    LegacyTileRenderPlans.steamEngineItemPlan(true, currentMillis), context, poseStack);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.INDUSTRIAL_TURBINE_ITEM_PREVIEW) {
            IndustrialSteamTurbineRenderer.renderPlan(model,
                    LegacyTileRenderPlans.industrialTurbineItemPlan(currentMillis), context, poseStack);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.ARC_FURNACE_STATIC_PREVIEW) {
            LegacyArcFurnaceRenderHelper.renderPlan(model,
                    LegacyTileRenderPlans.arcFurnaceStaticPreviewPlan(), context, poseStack);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.RADGEN_STATIC_SPECIAL) {
            renderRadgenStatic(definition, model, context);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.BATTERY_REDD_STATIC_SPECIAL) {
            renderBatteryReddStatic(definition, model, context);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.CRYSTALLIZER_STATIC_SPECIAL) {
            renderCrystallizerStatic(definition, model, context);
            return true;
        }
        if (definition.renderProfile() == LegacyMachineRenderProfile.TURBOFAN_ITEM_PREVIEW) {
            renderTurbofanItem(definition, model, context);
            return true;
        }
        return false;
    }

    private static void renderRadgenStatic(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        model.renderPart("Base", definition.textureLocation(), context);
        model.renderPart("Rotor", definition.textureLocation(), context);
        renderTintedPart(model, definition.textureLocation(), LegacyTileRenderPlans.radgenLightPlan(false), context);
        renderTintedPart(model, definition.textureLocation(), LegacyTileRenderPlans.radgenGlassPlan(), context);
        model.renderPart("Glass", definition.textureLocation(), context);
    }

    private static void renderBatteryReddStatic(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        model.renderPart("Base", definition.textureLocation(), context);
        model.renderPart("Wheel", definition.textureLocation(), context);
        model.renderPart("Lights", definition.textureLocation(), context.fullBright());
    }

    private static void renderCrystallizerStatic(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        model.renderPart("Body", definition.textureLocation(), context);
        model.renderPart("Spinner", definition.textureLocation(), context);
        renderTintedPart(model, definition.textureLocation(), LegacyTileRenderPlans.crystallizerFluidPlan(false),
                context);
    }

    private static void renderTurbofanItem(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        model.renderPart("Body", definition.textureLocation(), context);
        model.renderPart("Blades", definition.textureLocation(), context);
        model.renderPart("Afterburner", definition.itemPartTextures().getOrDefault("Afterburner",
                definition.partTextures().getOrDefault("Afterburner", definition.textureLocation())), context);
    }

    private static void renderTintedPart(LegacyWavefrontModel model, net.minecraft.resources.ResourceLocation texture,
            LegacyTileRenderPlans.ModelPartTintPlan plan, ObjRenderContext context) {
        if (!plan.active()) {
            return;
        }
        ObjRenderContext resolved = applyTintPlan(context, plan);
        if (plan.textured()) {
            model.renderPart(plan.partName(), texture, resolved);
        } else {
            model.renderPartUntextured(plan.partName(), resolved);
        }
    }

    private static ObjRenderContext applyTintPlan(ObjRenderContext context,
            LegacyTileRenderPlans.ModelPartTintPlan plan) {
        ObjRenderContext resolved = context;
        if (plan.blend() != null) {
            resolved = resolved.withRenderMode(plan.blend().modernRenderMode());
        }
        if (plan.color() != null) {
            resolved = resolved.withRgba(plan.color().redByte(), plan.color().greenByte(),
                    plan.color().blueByte(), plan.color().alphaByte());
        }
        if (plan.fullbright() != null) {
            resolved = resolved.withLegacyLightmap(plan.fullbright().lightmapX(), plan.fullbright().lightmapY());
        }
        return resolved;
    }

    private static void renderMachineParts(LegacyMachineDefinition definition, LegacyWavefrontModel model,
            ObjRenderContext context) {
        for (boolean translucentPass : new boolean[] { false, true }) {
            for (String part : definition.itemRenderParts()) {
                LegacyMachinePartRenderProperties properties = definition.itemPartRenderProperties().get(part);
                if (LegacyMachinePartRenderContexts.translucent(properties) != translucentPass) {
                    continue;
                }
                model.renderPart(part, definition.itemPartTextures().getOrDefault(part,
                                definition.partTextures().getOrDefault(part, definition.textureLocation())),
                        LegacyMachinePartRenderContexts.apply(context, properties));
            }
        }
    }

    private static void renderRotatingPart(LegacyWavefrontModel model,
            LegacyTileRenderPlans.RotatingModelPartPlan part, ObjRenderContext context, PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(part.pivotX(), part.pivotY(), part.pivotZ());
        rotate(poseStack, part.axisX(), part.axisY(), part.axisZ(), part.angleDegrees());
        poseStack.translate(-part.pivotX(), -part.pivotY(), -part.pivotZ());
        model.renderPart(part.partName(), context);
        poseStack.popPose();
    }

    private static void rotate(PoseStack poseStack, float axisX, float axisY, float axisZ, double degrees) {
        if (axisX != 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees((float) (degrees * axisX)));
        }
        if (axisY != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) (degrees * axisY)));
        }
        if (axisZ != 0.0F) {
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (degrees * axisZ)));
        }
    }

    private interface PointTransform {
        Vec3 apply(Vec3 point);
    }
}
