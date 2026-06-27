package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LegacyChargeBlock;
import com.hbm.ntm.blockentity.LegacyChargeBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class LegacyChargeBlockEntityRenderer implements BlockEntityRenderer<LegacyChargeBlockEntity> {
    private static final ResourceLocation DYNAMITE_TEXTURE = ObjBlockModels.texture("charge_dynamite");
    private static final ResourceLocation MINER_TEXTURE = ObjBlockModels.texture("charge_miner");
    private static final ResourceLocation C4_TEXTURE = ObjBlockModels.texture("charge_c4");
    private static final ResourceLocation SEMTEX_TEXTURE = ObjBlockModels.texture("charge_semtex");

    public LegacyChargeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(LegacyChargeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof LegacyChargeBlock block)) {
            return;
        }
        int modelLight = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        applyLegacyRotation(poseStack, state.getValue(LegacyChargeBlock.FACING));
        model(block.kind()).renderAll(texture(block.kind()), poseStack, buffer, modelLight, packedOverlay);
        renderTimer(blockEntity, poseStack, buffer, modelLight);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(LegacyChargeBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    private static void applyLegacyRotation(PoseStack poseStack, Direction facing) {
        switch (facing) {
            case DOWN -> poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            case UP -> {
            }
            case NORTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            }
            case SOUTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            }
            case EAST -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        }
    }

    private static void renderTimer(LegacyChargeBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        Font font = Minecraft.getInstance().font;
        String text = blockEntity.timerDisplay();

        poseStack.pushPose();
        poseStack.translate(-0.05F, -0.185F, 0.15F);
        poseStack.scale(0.0125F, -0.0125F, 0.0125F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        font.drawInBatch(text, 0.0F, 0.0F, 0x00FF00, false, poseStack.last().pose(), buffer,
                Font.DisplayMode.NORMAL, 0, packedLight);
        poseStack.popPose();
    }

    private static LegacyWavefrontModel model(LegacyChargeBlock.Kind kind) {
        return switch (kind) {
            case DYNAMITE, MINER -> ObjBlockModels.CHARGE_DYNAMITE;
            case C4, SEMTEX -> ObjBlockModels.CHARGE_C4;
        };
    }

    private static ResourceLocation texture(LegacyChargeBlock.Kind kind) {
        return switch (kind) {
            case DYNAMITE -> DYNAMITE_TEXTURE;
            case MINER -> MINER_TEXTURE;
            case C4 -> C4_TEXTURE;
            case SEMTEX -> SEMTEX_TEXTURE;
        };
    }
}
