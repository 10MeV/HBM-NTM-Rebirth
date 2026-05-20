package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyDirectionalShapeBlock;
import com.hbm.ntm.blockentity.LegacyLightBlockEntity;
import com.hbm.ntm.client.obj.LegacyObjTransforms;
import com.hbm.ntm.client.obj.ObjModelLibrary;
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
            case SPOTLIGHT_FLUORO -> ObjModelLibrary.FLUORESCENT_LAMP_SINGLE;
            case SPOTLIGHT_HALOGEN -> ObjModelLibrary.FLOOD_LAMP;
            default -> ObjModelLibrary.CAGE_LAMP;
        };

        poseStack.pushPose();
        poseStack.translate(
                0.5D - face.getStepX() * 0.5D,
                0.5D - face.getStepY() * 0.5D,
                0.5D - face.getStepZ() * 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(LegacyObjTransforms.yawDegrees(face)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-LegacyObjTransforms.pitchDegrees(face)));
        model.render(new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay));
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
        ObjModelLibrary.FLOODLIGHT_BASE.render(context);

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
        ObjModelLibrary.FLOODLIGHT_LIGHTS.render(angledContext);
        ObjModelLibrary.FLOODLIGHT_LAMPS.render(blockEntity.isOn()
                ? angledContext.fullBright()
                : angledContext.withColor(0x404040));
        poseStack.popPose();

        poseStack.popPose();
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
