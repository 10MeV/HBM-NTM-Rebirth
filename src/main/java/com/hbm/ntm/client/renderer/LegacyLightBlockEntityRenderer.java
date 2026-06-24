package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyDirectionalShapeBlock;
import com.hbm.ntm.block.LegacySpotlightBlock;
import com.hbm.ntm.blockentity.LegacyLightBlockEntity;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjLightModels;
import com.hbm.ntm.client.obj.ObjModelPart;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyLightBlockEntityRenderer implements BlockEntityRenderer<LegacyLightBlockEntity> {
    private static final LegacyWavefrontModel.SelectionHandle FLOODLIGHT_BASE =
            ObjLightModels.FLOODLIGHT_LEGACY.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle FLOODLIGHT_LIGHTS =
            ObjLightModels.FLOODLIGHT_LEGACY.prepareRenderOnlyInCallOrder("Lights");
    private static final LegacyWavefrontModel.SelectionHandle FLOODLIGHT_LAMPS =
            ObjLightModels.FLOODLIGHT_LEGACY.prepareRenderOnlyInCallOrder("Lamps");

    public LegacyLightBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LegacyLightBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyDirectionalShapeBlock block)) {
            return;
        }

        Direction face = state.getValue(LegacyDirectionalShapeBlock.FACE);
        if (block.kind() == LegacyDirectionalShapeBlock.Kind.FLOODLIGHT) {
            renderFloodlight(blockEntity, face, state, poseStack, buffer, packedLight, packedOverlay);
        } else {
            renderSpotlight(block.kind(), face, state, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void renderSpotlight(LegacyDirectionalShapeBlock.Kind kind, Direction face, BlockState state,
                                        PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjModelPart model = switch (kind) {
            case SPOTLIGHT_FLUORO -> ObjLightModels.FLUORESCENT_LAMP_SINGLE;
            case SPOTLIGHT_HALOGEN -> ObjLightModels.FLOOD_LAMP;
            default -> ObjLightModels.CAGE_LAMP;
        };

        poseStack.pushPose();
        poseStack.translate(
                0.5D - face.getStepX() * 0.5D,
                0.5D - face.getStepY() * 0.5D,
                0.5D - face.getStepZ() * 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(LegacyObjTransforms.yawDegrees(face)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-LegacyObjTransforms.pitchDegrees(face)));
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        if (state.getBlock() instanceof LegacySpotlightBlock spotlight && !spotlight.isActive()) {
            context = context.withColor(0x404040);
        }
        model.render(context);
        poseStack.popPose();
    }

    private static void renderFloodlight(LegacyLightBlockEntity blockEntity, Direction face, BlockState state,
                                         PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        applyFloodlightBaseRotation(poseStack, face);
        poseStack.translate(0.0D, -0.5D, 0.0D);
        boolean topBottomRotated = state.getValue(LegacyDirectionalShapeBlock.TOP_BOTTOM_ROTATED);
        if ((face != Direction.DOWN && face != Direction.UP) || topBottomRotated) {
            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        }

        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        renderFloodlightPart(FLOODLIGHT_BASE, context);

        float rotation = blockEntity.rotation();
        if (face == Direction.DOWN) {
            rotation -= 90.0F;
        }
        if (face == Direction.UP) {
            rotation += 90.0F;
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.5D, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
        poseStack.translate(0.0D, -0.5D, 0.0D);
        ObjRenderContext angledContext = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        renderFloodlightPart(FLOODLIGHT_LIGHTS, angledContext);
        renderFloodlightPart(FLOODLIGHT_LAMPS, blockEntity.isOn()
                ? angledContext.fullBright()
                : angledContext.withColor(0x404040));
        poseStack.popPose();

        poseStack.popPose();
    }

    private static void renderFloodlightPart(LegacyWavefrontModel.SelectionHandle handle, ObjRenderContext context) {
        ObjLightModels.FLOODLIGHT_LEGACY.renderOnlyInCallOrder(ObjLightModels.FLOODLIGHT_LEGACY.textureLocation(),
                context, handle);
    }

    private static void applyFloodlightBaseRotation(PoseStack poseStack, Direction face) {
        switch (face) {
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
}
