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
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class RadioTorchRenderer implements BlockEntityRenderer<RadioTorchBlockEntity> {
    private static final LegacyWavefrontModel MODEL = ObjBlockModels.RTTY.asVBO();

    private static final TextureAtlasSprite SENDER_OFF = sprite("rtty_sender_off");
    private static final TextureAtlasSprite SENDER_ON = sprite("rtty_sender_on");
    private static final TextureAtlasSprite RECEIVER_OFF = sprite("rtty_rec_off");
    private static final TextureAtlasSprite RECEIVER_ON = sprite("rtty_rec_on");
    private static final TextureAtlasSprite LOGIC_OFF = sprite("rtty_logic_off");
    private static final TextureAtlasSprite LOGIC_ON = sprite("rtty_logic_on");
    private static final TextureAtlasSprite READER = sprite("rtty_reader");
    private static final TextureAtlasSprite COUNTER = sprite("rtty_counter");
    private static final TextureAtlasSprite CONTROLLER = sprite("rtty_controller");

    public RadioTorchRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RadioTorchBlockEntity torch, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = torch.getBlockState();
        Direction facing = state.hasProperty(RadioTorchBlock.FACING)
                ? state.getValue(RadioTorchBlock.FACING)
                : Direction.UP;
        int light = LegacyRenderLighting.resolveMultiblockLight(torch, packedLight);
        TextureAtlasSprite sprite = textureFor(torch);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        Rotation rotation = legacyRotation(facing);
        MODEL.renderWithSprite(sprite, poseStack, buffer, light, packedOverlay, rotation.yaw(), rotation.pitch(),
                0.0F, false);
        poseStack.popPose();
    }

    private static TextureAtlasSprite textureFor(RadioTorchBlockEntity torch) {
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

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + name));
    }

    private record Rotation(float yaw, float pitch) {
    }
}
