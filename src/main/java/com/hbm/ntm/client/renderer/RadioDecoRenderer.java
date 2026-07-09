package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.LegacyMachineRenderShapes;
import com.hbm.ntm.block.RadioboxBlock;
import com.hbm.ntm.block.RadioReceiverBlock;
import com.hbm.ntm.blockentity.RadioboxBlockEntity;
import com.hbm.ntm.blockentity.RadioReceiverBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer.TexturedQuadBatch;
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
import net.minecraft.world.phys.Vec3;

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
    public boolean shouldRender(T blockEntity, Vec3 cameraPos) {
        boolean renderReceiverFallback = blockEntity instanceof RadioReceiverBlockEntity
                && LegacyMachineRenderShapes.renderChunkBakedStaticsInBer();
        return (blockEntity instanceof RadioboxBlockEntity || renderReceiverFallback)
                && BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        if (blockEntity instanceof RadioboxBlockEntity box) {
            try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
                renderRadiobox(state, box.isInfinite() || active(state),
                        LegacyMachineRenderShapes.renderChunkBakedStaticsInBer(),
                        poseStack, buffer, packedLight, packedOverlay);
            }
        } else if (blockEntity instanceof RadioReceiverBlockEntity
                && LegacyMachineRenderShapes.renderChunkBakedStaticsInBer()) {
            try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
                renderRadioReceiver(state, poseStack, buffer, packedLight, packedOverlay);
            }
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
        TexturedQuadBatch batch = LegacyTexturedQuadRenderer.texturedQuadBatch(RADIOREC_TEXTURE, poseStack, buffer,
                LegacyTexturedRenderMode.CUTOUT_CULL, 255);
        cube(batch, 64.0F, 32.0F, packedLight, packedOverlay,
                0, 0, -7, 14, -4, 14, 10, 8);
        cube(batch, 64.0F, 32.0F, packedLight, packedOverlay,
                4, 21, -5, 11, -1, 2, 3, 2);
        cube(batch, 64.0F, 32.0F, packedLight, packedOverlay,
                0, 18, -4.5F, 0, -0.5F, 1, 11, 1);
        cube(batch, 64.0F, 32.0F, packedLight, packedOverlay,
                4, 18, 2, 12, -0.5F, 3, 2, 1);
        poseStack.popPose();
    }

    private static void renderRadiobox(BlockState state, boolean active, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        renderRadiobox(state, active, true, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void renderRadiobox(BlockState state, boolean active, boolean renderStaticBody,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Direction facing = state.hasProperty(RadioboxBlock.FACING)
                ? state.getValue(RadioboxBlock.FACING)
                : Direction.SOUTH;

        poseStack.pushPose();
        applyLegacyRoot(poseStack, radioboxYaw(facing));
        poseStack.translate(0.0D, 0.0D, 16.0D);
        if (renderStaticBody) {
            TexturedQuadBatch staticBatch = LegacyTexturedQuadRenderer.texturedQuadBatch(RADIOBOX_TEXTURE,
                    poseStack, buffer, LegacyTexturedRenderMode.CUTOUT_CULL, 255);
            cube(staticBatch, 32.0F, 32.0F, packedLight, packedOverlay,
                    0, 0, -4, 9, -12, 8, 14, 4);
            cube(staticBatch, 32.0F, 32.0F, packedLight, packedOverlay,
                    0, 18, -3.5F, 9.5F, -12.5F, 7, 13, 1);
        }
        poseStack.pushPose();
        poseStack.translate(4.0D, 16.0D, -10.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees(active ? -160.0F : -20.0F));
        TexturedQuadBatch leverBatch = LegacyTexturedQuadRenderer.texturedQuadBatch(RADIOBOX_TEXTURE, poseStack,
                buffer, LegacyTexturedRenderMode.CUTOUT_CULL, 255);
        cube(leverBatch, 32.0F, 32.0F, packedLight, packedOverlay,
                16, 18, 0, -1, -1, 2, 8, 2);
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

    private static void cube(TexturedQuadBatch batch, float texW, float texH,
            int light, int overlay,
            int u, int v, float x, float y, float z, float width, float height, float depth) {
        float x2 = x + width;
        float y2 = y + height;
        float z2 = z + depth;
        float invTexW = 1.0F / texW;
        float invTexH = 1.0F / texH;

        face(batch, light, overlay, x, y, z2, x2, y2, z2, 0, 0, 1, u + depth + width, v + depth,
                u + depth + width + width, v + depth + height, invTexW, invTexH);
        face(batch, light, overlay, x2, y, z, x, y2, z, 0, 0, -1, u + depth + width + depth, v + depth,
                u + depth + width + depth + width, v + depth + height, invTexW, invTexH);
        face(batch, light, overlay, x, y, z, x, y2, z2, -1, 0, 0, u, v + depth,
                u + depth, v + depth + height, invTexW, invTexH);
        face(batch, light, overlay, x2, y, z2, x2, y2, z, 1, 0, 0, u + depth + width, v + depth,
                u + depth + width + depth, v + depth + height, invTexW, invTexH);
        face(batch, light, overlay, x, y, z, x2, y, z2, 0, -1, 0, u + depth, v,
                u + depth + width, v + depth, invTexW, invTexH);
        face(batch, light, overlay, x, y2, z2, x2, y2, z, 0, 1, 0, u + depth + width, v,
                u + depth + width + width, v + depth, invTexW, invTexH);
    }

    private static void face(TexturedQuadBatch batch, int light, int overlay,
            float x1, float y1, float z1, float x2, float y2, float z2, float nx, float ny, float nz,
            float u1, float v1, float u2, float v2, float invTexW, float invTexH) {
        LegacyTexturedQuadRenderer.quadDirect(batch, light, overlay, nx, ny, nz,
                x1, y1, z1, u1 * invTexW, v1 * invTexH,
                x2, y1, z1, u2 * invTexW, v1 * invTexH,
                x2, y2, z2, u2 * invTexW, v2 * invTexH,
                x1, y2, z2, u1 * invTexW, v2 * invTexH,
                0xFFFFFF, 255);
    }
}
