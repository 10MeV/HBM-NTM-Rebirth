package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.RadioAutocalBlockEntity;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class RadioAutocalRenderer implements BlockEntityRenderer<RadioAutocalBlockEntity> {
    static final ResourceLocation TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/machines/autocal.png");
    static final LegacyWavefrontModel MODEL = new LegacyWavefrontModel(
            new ResourceLocation(HbmNtm.MOD_ID, "models/block/machines/autocal.obj"), TEXTURE).noSmooth().asVBO();

    public RadioAutocalRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(RadioAutocalBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(RadioAutocalBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.SOUTH;
        int light = LegacyRenderLighting.resolveBlockEntityLight(blockEntity, packedLight);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(legacyYaw(facing)));
        MODEL.renderAll(TEXTURE, poseStack, buffer, light, packedOverlay);
        poseStack.popPose();
    }

    private static float legacyYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 90.0F;
            case SOUTH -> 270.0F;
            case WEST -> 180.0F;
            default -> 0.0F;
        };
    }
}
