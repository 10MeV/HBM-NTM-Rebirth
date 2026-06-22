package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.ChargerBlock;
import com.hbm.ntm.blockentity.ChargerBlockEntity;
import com.hbm.ntm.client.obj.ObjMachineModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ChargerRenderer implements BlockEntityRenderer<ChargerBlockEntity> {
    public ChargerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ChargerBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(ChargerBlockEntity charger, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = charger.getBlockState();
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        orient(poseStack, state.hasProperty(ChargerBlock.FACING) ? state.getValue(ChargerBlock.FACING) : Direction.NORTH);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, packedLight, packedOverlay);
        ObjMachineModels.CHARGER.renderPart("Base", ObjMachineModels.CHARGER_TEXTURE, context);
        ObjMachineModels.CHARGER.renderPart("Left", ObjMachineModels.CHARGER_TEXTURE, context);
        ObjMachineModels.CHARGER.renderPart("Right", ObjMachineModels.CHARGER_TEXTURE, context);
        if (charger.getUsingTicks() > 0) {
            ObjMachineModels.CHARGER.renderPart("Light", ObjMachineModels.CHARGER_TEXTURE, context.fullBright());
        } else {
            ObjMachineModels.CHARGER.renderPart("Light", ObjMachineModels.CHARGER_TEXTURE, context);
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, -charger.getSlide(partialTick) * 0.25D, 0.0D);
        ObjMachineModels.CHARGER.renderPart("Slide", ObjMachineModels.CHARGER_TEXTURE, context);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void orient(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            case SOUTH -> poseStack.mulPose(Axis.XN.rotationDegrees(90.0F));
            case WEST -> {
                poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
            case EAST -> {
                poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            }
            default -> {
            }
        }
    }
}
