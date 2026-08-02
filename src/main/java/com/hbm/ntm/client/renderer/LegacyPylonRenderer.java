package com.hbm.ntm.client.renderer;

import com.hbm.config.ClientConfig;
import com.hbm.ntm.block.LegacyConnectorBlock;
import com.hbm.ntm.block.LegacyLargePylonBlock;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.LegacyMediumPylonBlock;
import com.hbm.ntm.block.LegacySmallPylonBlock;
import com.hbm.ntm.block.LegacySubstationBlock;
import com.hbm.ntm.blockentity.HbmLegacyWireNodeBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedLineRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjNetworkModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.energy.HbmLegacyWireNode;
import com.hbm.ntm.energy.HbmLegacyWireRenderMath;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

import java.util.List;

public class LegacyPylonRenderer<T extends HbmLegacyWireNodeBlockEntity> implements BlockEntityRenderer<T> {
    public static final ResourceLocation PYLON_MEDIUM_TEXTURE =
            ObjNetworkModels.PYLON_MEDIUM_TEXTURE;
    public static final ResourceLocation PYLON_MEDIUM_STEEL_TEXTURE =
            ObjNetworkModels.PYLON_MEDIUM_STEEL_TEXTURE;
    public static final ResourceLocation PYLON_LARGE_TEXTURE =
            ObjNetworkModels.PYLON_LARGE_TEXTURE;
    public static final ResourceLocation CONNECTOR_TEXTURE =
            ObjNetworkModels.CONNECTOR_TEXTURE;
    public static final ResourceLocation CONNECTOR_SUPER_TEXTURE =
            ObjNetworkModels.CONNECTOR_SUPER_TEXTURE;
    public static final ResourceLocation SUBSTATION_TEXTURE =
            ObjNetworkModels.SUBSTATION_TEXTURE;
    public static final ResourceLocation WIRE_TEXTURE =
            ObjNetworkModels.texture("wire");
    public static final ResourceLocation WIRE_GREYSCALE_TEXTURE =
            ObjNetworkModels.texture("wire_greyscale");
    private static final LegacyWavefrontModel.SelectionHandle MEDIUM_PYLON =
            ObjNetworkModels.PYLON_MEDIUM_LEGACY.prepareRenderOnlyInCallOrder("Pylon");
    private static final LegacyWavefrontModel.SelectionHandle MEDIUM_TRANSFORMER =
            ObjNetworkModels.PYLON_MEDIUM_LEGACY.prepareRenderOnlyInCallOrder("Transformer");

    public LegacyPylonRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(T pylon, Vec3 cameraPos) {
        return hasBerVisuals(pylon)
                && BlockEntityRenderer.super.shouldRender(pylon, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(pylon, getViewDistance());
    }

    @Override
    public void render(T pylon, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(pylon, getViewDistance())) {
            return;
        }
        BlockState state = pylon.getBlockState();
        boolean renderStaticBody = !usesChunkBakedPylonBody(state)
                || LegacyMachineRenderShapes.renderChunkBakedStaticsInBer();
        if (renderStaticBody) {
            int modelLight = LegacyRenderLighting.resolveMultiblockLight(pylon, packedLight);
            try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(pylon)) {
                renderPylonModel(state, poseStack, buffer, modelLight, packedOverlay);
            }
        }
        renderWires(pylon, poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderPylonModel(BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (state.getBlock() instanceof LegacyMediumPylonBlock block) {
            renderMediumPylon(block.kind(), getHorizontalFacing(state), poseStack, buffer, packedLight, packedOverlay);
        } else if (state.getBlock() instanceof LegacyLargePylonBlock) {
            renderLargePylon(getHorizontalFacing(state), poseStack, buffer, packedLight, packedOverlay);
        } else if (state.getBlock() instanceof LegacySmallPylonBlock) {
            renderSmallPylon(poseStack, buffer, packedLight, packedOverlay);
        } else if (state.getBlock() instanceof LegacyConnectorBlock block) {
            renderConnector(block.kind(), getFacing(state), poseStack, buffer, packedLight, packedOverlay);
        } else if (state.getBlock() instanceof LegacySubstationBlock) {
            renderSubstation(getHorizontalFacing(state), poseStack, buffer, packedLight, packedOverlay);
        }
    }

    public static void renderSmallPylon(PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacySmallPylonModel.render(poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderMediumPylon(LegacyMediumPylonBlock.Kind kind, Direction facing, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ResourceLocation texture = kind.steel() ? PYLON_MEDIUM_STEEL_TEXTURE : PYLON_MEDIUM_TEXTURE;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, mediumRotation(facing));
        ObjNetworkModels.PYLON_MEDIUM_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer,
                packedLight, packedOverlay, MEDIUM_PYLON);
        if (kind.transformer()) {
            ObjNetworkModels.PYLON_MEDIUM_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer,
                    packedLight, packedOverlay, MEDIUM_TRANSFORMER);
        }
        poseStack.popPose();
    }

    static void renderMediumPylonPart(String partName, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel.SelectionHandle handle = mediumPylonHandle(partName);
        if (handle != null) {
            ObjNetworkModels.PYLON_MEDIUM_LEGACY.renderOnlyInCallOrder(texture, poseStack, buffer,
                    packedLight, packedOverlay, handle);
            return;
        }
        ObjNetworkModels.PYLON_MEDIUM_LEGACY.renderPart(partName, texture, poseStack, buffer, packedLight,
                packedOverlay);
    }

    public static void renderLargePylon(Direction facing, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, largeRotation(facing));
        ObjNetworkModels.PYLON_LARGE_LEGACY.renderAll(PYLON_LARGE_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    public static void renderConnector(LegacyConnectorBlock.Kind kind, Direction facing, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyWavefrontModel model = kind == LegacyConnectorBlock.Kind.SUPER
                ? ObjNetworkModels.CONNECTOR_SUPER_LEGACY
                : ObjNetworkModels.CONNECTOR_LEGACY;
        ResourceLocation texture = kind == LegacyConnectorBlock.Kind.SUPER ? CONNECTOR_SUPER_TEXTURE : CONNECTOR_TEXTURE;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        applyConnectorRotation(facing, poseStack);
        poseStack.translate(0.0D, -0.5D, 0.0D);
        model.renderAll(texture, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    public static void renderSubstation(Direction facing, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        if (facing.getAxis() == Direction.Axis.Z) {
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        }
        ObjNetworkModels.SUBSTATION_LEGACY.renderAll(SUBSTATION_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderWires(HbmLegacyWireNodeBlockEntity pylon, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = pylon.getLevel();
        if (level == null || pylon.getWireConnections().isEmpty()) {
            return;
        }
        BlockPos selfPos = pylon.getBlockPos();
        List<Vec3> selfMounts = pylon.getWireMountPoints();
        if (selfMounts.isEmpty()) {
            return;
        }

        int color = pylon.getWireConnections().color();
        ResourceLocation texture = color == 0 ? WIRE_TEXTURE : WIRE_GREYSCALE_TEXTURE;
        int wireColor = color == 0 ? 0xFFFFFF : color;
        double selfX = selfPos.getX();
        double selfY = selfPos.getY();
        double selfZ = selfPos.getZ();
        LegacyTexturedLineRenderer.PylonLineBatch wireBatch = null;
        for (BlockPos remotePos : pylon.getWireConnections().connectedView()) {
            BlockEntity remote = level.getBlockEntity(remotePos);
            if (!(remote instanceof HbmLegacyWireNode remoteWire)) {
                continue;
            }
            List<Vec3> remoteMounts = remoteWire.getWireMountPoints();
            if (remoteMounts.isEmpty()) {
                continue;
            }
            int lineCount = Math.min(selfMounts.size(), remoteMounts.size());
            for (int line = 0; line < lineCount; line++) {
                Vec3 startAbs = selfMounts.get(line % selfMounts.size());
                int remoteIndex = LegacyTexturedLineRenderer.pylonSecondMountIndex(
                        line,
                        remoteMounts.size(),
                        lineCount,
                        HbmLegacyWireRenderMath.legacyMetadata(pylon.getBlockState()),
                        HbmLegacyWireRenderMath.legacyMetadata(remote.getBlockState()));
                Vec3 remoteAbs = remoteMounts.get(remoteIndex);
                double startX = startAbs.x - selfX;
                double startY = startAbs.y - selfY;
                double startZ = startAbs.z - selfZ;
                double remoteX = remoteAbs.x - selfX;
                double remoteY = remoteAbs.y - selfY;
                double remoteZ = remoteAbs.z - selfZ;
                double endX = startX + (remoteX - startX) * 0.5D;
                double endY = startY + (remoteY - startY) * 0.5D;
                double endZ = startZ + (remoteZ - startZ) * 0.5D;
                if (wireBatch == null) {
                    wireBatch = LegacyTexturedLineRenderer.pylonLineBatch(texture, poseStack, buffer, packedLight,
                            packedOverlay);
                }
                LegacyTexturedLineRenderer.pylonLine(wireBatch,
                        startX, startY, startZ,
                        endX, endY, endZ,
                        ClientConfig.renderCableHang(), wireColor);
            }
        }
    }

    private static Direction getHorizontalFacing(BlockState state) {
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
    }

    private static Direction getFacing(BlockState state) {
        return state.hasProperty(BlockStateProperties.FACING)
                ? state.getValue(BlockStateProperties.FACING)
                : getHorizontalFacing(state);
    }

    private static void applyConnectorRotation(Direction facing, PoseStack poseStack) {
        switch (facing) {
            case DOWN -> LegacyPoseRotations.rotateXDegrees(poseStack, 180.0F);
            case NORTH -> {
                LegacyPoseRotations.rotateXDegrees(poseStack, 90.0F);
                LegacyPoseRotations.rotateZDegrees(poseStack, 180.0F);
            }
            case SOUTH -> LegacyPoseRotations.rotateXDegrees(poseStack, 90.0F);
            case WEST -> {
                LegacyPoseRotations.rotateXDegrees(poseStack, 90.0F);
                LegacyPoseRotations.rotateZDegrees(poseStack, 90.0F);
            }
            case EAST -> {
                LegacyPoseRotations.rotateXDegrees(poseStack, 90.0F);
                LegacyPoseRotations.rotateZDegrees(poseStack, 270.0F);
            }
            default -> {
            }
        }
    }

    private static float mediumRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
    }

    private static LegacyWavefrontModel.SelectionHandle mediumPylonHandle(String partName) {
        if (partName == null) {
            return null;
        }
        return switch (partName) {
            case "Pylon" -> MEDIUM_PYLON;
            case "Transformer" -> MEDIUM_TRANSFORMER;
            default -> null;
        };
    }

    private static float largeRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90.0F;
            case WEST -> 135.0F;
            case EAST -> 45.0F;
            default -> 0.0F;
        };
    }

    private static boolean hasBerVisuals(HbmLegacyWireNodeBlockEntity pylon) {
        BlockState state = pylon.getBlockState();
        return !usesChunkBakedPylonBody(state)
                || LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()
                || !pylon.getWireConnections().isEmpty();
    }

    private static boolean usesChunkBakedPylonBody(BlockState state) {
        return state.getBlock() instanceof LegacyMediumPylonBlock
                || state.getBlock() instanceof LegacyLargePylonBlock
                || state.getBlock() instanceof LegacySmallPylonBlock
                || state.getBlock() instanceof LegacyConnectorBlock
                || state.getBlock() instanceof LegacySubstationBlock;
    }

}
