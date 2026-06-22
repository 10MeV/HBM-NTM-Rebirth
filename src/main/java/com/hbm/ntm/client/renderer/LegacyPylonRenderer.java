package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyConnectorBlock;
import com.hbm.ntm.block.LegacyLargePylonBlock;
import com.hbm.ntm.block.LegacyMediumPylonBlock;
import com.hbm.ntm.block.LegacySmallPylonBlock;
import com.hbm.ntm.block.LegacySubstationBlock;
import com.hbm.ntm.blockentity.HbmLegacyWireNodeBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedLineRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjNetworkModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.energy.HbmLegacyWireNode;
import com.hbm.ntm.energy.HbmLegacyWireRenderMath;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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

    public LegacyPylonRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(T blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(T pylon, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int modelLight = LegacyRenderLighting.resolveMultiblockLight(pylon, packedLight);
        renderPylonModel(pylon.getBlockState(), poseStack, buffer, modelLight, packedOverlay);
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
        poseStack.mulPose(Axis.YP.rotationDegrees(mediumRotation(facing)));
        ObjNetworkModels.PYLON_MEDIUM_LEGACY.renderPart("Pylon", texture, poseStack, buffer, packedLight, packedOverlay);
        if (kind.transformer()) {
            ObjNetworkModels.PYLON_MEDIUM_LEGACY.renderPart("Transformer", texture, poseStack, buffer, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    public static void renderLargePylon(Direction facing, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(largeRotation(facing)));
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
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
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
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, pylon.getBlockState(), packedLight, packedOverlay);

        for (BlockPos remotePos : pylon.getWireConnections().connected()) {
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
                Vec3 start = startAbs.subtract(selfPos.getX(), selfPos.getY(), selfPos.getZ());
                Vec3 remoteRelative = remoteAbs.subtract(selfPos.getX(), selfPos.getY(), selfPos.getZ());
                Vec3 end = start.add(remoteRelative.subtract(start).scale(0.5D));
                LegacyTexturedLineRenderer.pylonLine(texture, context,
                        start.x, start.y, start.z,
                        end.x, end.y, end.z,
                        true, wireColor);
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
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            }
            case SOUTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case WEST -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            }
            case EAST -> {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(270.0F));
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

    private static float largeRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90.0F;
            case WEST -> 135.0F;
            case EAST -> 45.0F;
            default -> 0.0F;
        };
    }

}
