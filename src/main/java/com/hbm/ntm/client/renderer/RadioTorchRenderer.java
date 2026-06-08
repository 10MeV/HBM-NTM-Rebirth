package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.RadioTorchBlock;
import com.hbm.ntm.blockentity.RadioTorchBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchControllerBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchCounterBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchDeviceBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchLogicBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchReaderBlockEntity;
import com.hbm.ntm.blockentity.RadioTorchReceiverBlockEntity;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class RadioTorchRenderer implements BlockEntityRenderer<RadioTorchBlockEntity> {
    private static final LegacyWavefrontModel MODEL = new LegacyWavefrontModel(
            new ResourceLocation(HbmNtm.MOD_ID, "models/block/network/rtty.obj"),
            textureFile("rtty_sender_off")).noSmooth().asVBO();

    private static final ResourceLocation SENDER_OFF = blockSprite("rtty_sender_off");
    private static final ResourceLocation SENDER_ON = blockSprite("rtty_sender_on");
    private static final ResourceLocation RECEIVER_OFF = blockSprite("rtty_rec_off");
    private static final ResourceLocation RECEIVER_ON = blockSprite("rtty_rec_on");
    private static final ResourceLocation LOGIC_OFF = blockSprite("rtty_logic_off");
    private static final ResourceLocation LOGIC_ON = blockSprite("rtty_logic_on");
    private static final ResourceLocation READER = blockSprite("rtty_reader");
    private static final ResourceLocation COUNTER = blockSprite("rtty_counter");
    private static final ResourceLocation CONTROLLER = blockSprite("rtty_controller");

    public RadioTorchRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RadioTorchBlockEntity torch, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = torch.getBlockState();
        Direction facing = state.hasProperty(RadioTorchBlock.FACING)
                ? state.getValue(RadioTorchBlock.FACING)
                : Direction.UP;
        int light = LegacyRenderLighting.resolveBlockEntityLight(torch, packedLight);
        TextureAtlasSprite sprite = LegacyTexturedQuadRenderer.blockSprite(textureFor(torch));

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state, light, packedOverlay);
        Rotation rotation = legacyRotation(facing);
        MODEL.renderWithSprite(sprite, context, rotation.yaw(), rotation.pitch(), 0.0F);
        poseStack.popPose();
    }

    private static ResourceLocation textureFor(RadioTorchBlockEntity torch) {
        if (torch instanceof RadioTorchDeviceBlockEntity device) {
            boolean active = device.radioState().lastState() > 0;
            return torch instanceof RadioTorchReceiverBlockEntity
                    ? active ? RECEIVER_ON : RECEIVER_OFF
                    : active ? SENDER_ON : SENDER_OFF;
        }
        if (torch instanceof RadioTorchLogicBlockEntity logic) {
            return logic.redstoneOutput() > 0 ? LOGIC_ON : LOGIC_OFF;
        }
        if (torch instanceof RadioTorchReaderBlockEntity) {
            return READER;
        }
        if (torch instanceof RadioTorchCounterBlockEntity) {
            return COUNTER;
        }
        if (torch instanceof RadioTorchControllerBlockEntity) {
            return CONTROLLER;
        }
        return SENDER_OFF;
    }

    private static Rotation legacyRotation(Direction facing) {
        float flip = 0.0F;
        float rotation = 0.0F;
        switch (facing) {
            case DOWN -> flip = (float) Math.PI;
            case NORTH -> rotation = (float) Math.PI * 0.5F;
            case SOUTH -> rotation = (float) Math.PI * 1.5F;
            case WEST -> rotation = (float) Math.PI;
            default -> {
            }
        }
        if (rotation != 0.0F || facing == Direction.EAST) {
            flip = (float) Math.PI * 0.5F;
        }
        return new Rotation(rotation, flip);
    }

    private static ResourceLocation blockSprite(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + name);
    }

    private static ResourceLocation textureFile(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/" + name + ".png");
    }

    private record Rotation(float yaw, float pitch) {
    }
}
