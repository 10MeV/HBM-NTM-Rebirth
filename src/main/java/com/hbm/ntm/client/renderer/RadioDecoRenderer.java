package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.RadioboxBlock;
import com.hbm.ntm.block.RadioReceiverBlock;
import com.hbm.ntm.blockentity.RadioboxBlockEntity;
import com.hbm.ntm.blockentity.RadioReceiverBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RadioDecoRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    private static final ResourceLocation RADIOBOX_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/model_radio.png");
    private static final ResourceLocation RADIOREC_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/model_radio_receiver.png");

    public RadioDecoRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if (blockEntity instanceof RadioboxBlockEntity box) {
            renderRadiobox(state, box.isInfinite() || active(state), poseStack, buffer, packedLight, packedOverlay);
        } else if (blockEntity instanceof RadioReceiverBlockEntity) {
            renderRadioReceiver(state, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    public static void renderItem(BlockState state, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
        poseStack.scale(0.55F, 0.55F, 0.55F);
        poseStack.translate(-0.5D, -0.5D, -0.5D);
        if (state.getBlock() instanceof RadioboxBlock) {
            renderRadiobox(state.setValue(RadioboxBlock.FACING, Direction.SOUTH), false, poseStack, buffer,
                    packedLight, packedOverlay);
        } else {
            renderRadioReceiver(state.setValue(RadioReceiverBlock.FACING, Direction.SOUTH), poseStack, buffer,
                    packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderRadioReceiver(BlockState state, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        Direction facing = state.hasProperty(RadioReceiverBlock.FACING)
                ? state.getValue(RadioReceiverBlock.FACING)
                : Direction.SOUTH;

        poseStack.pushPose();
        applyLegacyRoot(poseStack, radioReceiverYaw(facing));
        cube(RADIOREC_TEXTURE, poseStack, buffer, packedLight, packedOverlay, 0, 0, 14, 14, -4, 14, 10, 8);
        cube(RADIOREC_TEXTURE, poseStack, buffer, packedLight, packedOverlay, 4, 21, 11, 11, -1, 2, 3, 2);
        cube(RADIOREC_TEXTURE, poseStack, buffer, packedLight, packedOverlay, 0, 18, -4.5F, 0, -0.5F, 1, 11, 1);
        cube(RADIOREC_TEXTURE, poseStack, buffer, packedLight, packedOverlay, 4, 18, 2, 12, -0.5F, 3, 2, 1);
        poseStack.popPose();
    }

    private static void renderRadiobox(BlockState state, boolean active, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        Direction facing = state.hasProperty(RadioboxBlock.FACING) ? state.getValue(RadioboxBlock.FACING) : Direction.SOUTH;

        poseStack.pushPose();
        applyLegacyRoot(poseStack, radioboxYaw(facing));
        poseStack.translate(0.0D, 0.0D, 16.0D);
        cube(RADIOBOX_TEXTURE, poseStack, buffer, packedLight, packedOverlay, 0, 0, -4, 9, -12, 8, 14, 4);
        cube(RADIOBOX_TEXTURE, poseStack, buffer, packedLight, packedOverlay, 0, 18, -3.5F, 9.5F, -12.5F, 7, 13, 1);
        poseStack.pushPose();
        poseStack.translate(4.0D, 16.0D, -10.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(active ? -160.0F : -20.0F));
        cube(RADIOBOX_TEXTURE, poseStack, buffer, packedLight, packedOverlay, 16, 18, 0, -1, -1, 2, 8, 2);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void applyLegacyRoot(PoseStack poseStack, float yaw) {
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.scale(1.0F / 16.0F, 1.0F / 16.0F, 1.0F / 16.0F);
    }

    private static float radioReceiverYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 90.0F;
            case EAST -> 270.0F;
            default -> 0.0F;
        };
    }

    private static float radioboxYaw(Direction facing) {
        return switch (facing) {
            case NORTH -> 180.0F;
            case WEST -> 270.0F;
            case EAST -> 90.0F;
            default -> 0.0F;
        };
    }

    private static boolean active(BlockState state) {
        return state.hasProperty(RadioboxBlock.ACTIVE) && state.getValue(RadioboxBlock.ACTIVE);
    }

    private static void cube(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int light, int overlay,
            int u, int v, float x, float y, float z, float width, float height, float depth) {
        float x2 = x + width;
        float y2 = y + height;
        float z2 = z + depth;
        float texW = 64.0F;
        float texH = 32.0F;

        face(texture, poseStack, buffer, light, overlay, x, y, z2, x2, y2, z2, 0, 0, 1, u + depth + width, v + depth,
                u + depth + width + width, v + depth + height, texW, texH);
        face(texture, poseStack, buffer, light, overlay, x2, y, z, x, y2, z, 0, 0, -1, u + depth + width + depth, v + depth,
                u + depth + width + depth + width, v + depth + height, texW, texH);
        face(texture, poseStack, buffer, light, overlay, x, y, z, x, y2, z2, -1, 0, 0, u, v + depth,
                u + depth, v + depth + height, texW, texH);
        face(texture, poseStack, buffer, light, overlay, x2, y, z2, x2, y2, z, 1, 0, 0, u + depth + width, v + depth,
                u + depth + width + depth, v + depth + height, texW, texH);
        face(texture, poseStack, buffer, light, overlay, x, y, z, x2, y, z2, 0, -1, 0, u + depth, v,
                u + depth + width, v + depth, texW, texH);
        face(texture, poseStack, buffer, light, overlay, x, y2, z2, x2, y2, z, 0, 1, 0, u + depth + width, v,
                u + depth + width + width, v + depth, texW, texH);
    }

    private static void face(ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer,
            int light, int overlay,
            float x1, float y1, float z1, float x2, float y2, float z2, float nx, float ny, float nz,
            float u1, float v1, float u2, float v2, float texW, float texH) {
        LegacyTexturedQuadRenderer.pixelQuad(texture, poseStack, buffer, light, overlay,
                LegacyTexturedRenderMode.CUTOUT_CULL, nx, ny, nz, texW, texH,
                x1, y1, z1, u1, v1,
                x2, y1, z1, u2, v1,
                x2, y2, z2, u2, v2,
                x1, y2, z2, u1, v2,
                0xFFFFFF, 255);
    }
}
