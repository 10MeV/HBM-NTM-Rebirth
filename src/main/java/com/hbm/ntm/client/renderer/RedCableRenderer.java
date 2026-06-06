package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HbmEnergyNodeBlock;
import com.hbm.ntm.blockentity.RedCableBlockEntity;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class RedCableRenderer implements BlockEntityRenderer<RedCableBlockEntity> {
    public static final ResourceLocation CABLE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/legacy_blocks/cable_neo.png");

    public RedCableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(RedCableBlockEntity cable, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = cable.getBlockState();
        int light = LegacyRenderLighting.resolveBlockEntityLight(cable, packedLight);

        boolean posX = state.getValue(HbmEnergyNodeBlock.EAST);
        boolean negX = state.getValue(HbmEnergyNodeBlock.WEST);
        boolean posY = state.getValue(HbmEnergyNodeBlock.UP);
        boolean negY = state.getValue(HbmEnergyNodeBlock.DOWN);
        boolean posZ = state.getValue(HbmEnergyNodeBlock.SOUTH);
        boolean negZ = state.getValue(HbmEnergyNodeBlock.NORTH);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        if (posX && negX && !posY && !negY && !posZ && !negZ) {
            ObjBlockModels.CABLE_NEO.renderPart("CX", CABLE_TEXTURE, poseStack, buffer, light, packedOverlay);
        } else if (!posX && !negX && posY && negY && !posZ && !negZ) {
            ObjBlockModels.CABLE_NEO.renderPart("CY", CABLE_TEXTURE, poseStack, buffer, light, packedOverlay);
        } else if (!posX && !negX && !posY && !negY && posZ && negZ) {
            ObjBlockModels.CABLE_NEO.renderPart("CZ", CABLE_TEXTURE, poseStack, buffer, light, packedOverlay);
        } else {
            ObjBlockModels.CABLE_NEO.renderPart("Core", CABLE_TEXTURE, poseStack, buffer, light, packedOverlay);
            if (posX) {
                ObjBlockModels.CABLE_NEO.renderPart("posX", CABLE_TEXTURE, poseStack, buffer, light, packedOverlay);
            }
            if (negX) {
                ObjBlockModels.CABLE_NEO.renderPart("negX", CABLE_TEXTURE, poseStack, buffer, light, packedOverlay);
            }
            if (posY) {
                ObjBlockModels.CABLE_NEO.renderPart("posY", CABLE_TEXTURE, poseStack, buffer, light, packedOverlay);
            }
            if (negY) {
                ObjBlockModels.CABLE_NEO.renderPart("negY", CABLE_TEXTURE, poseStack, buffer, light, packedOverlay);
            }
            if (negZ) {
                ObjBlockModels.CABLE_NEO.renderPart("posZ", CABLE_TEXTURE, poseStack, buffer, light, packedOverlay);
            }
            if (posZ) {
                ObjBlockModels.CABLE_NEO.renderPart("negZ", CABLE_TEXTURE, poseStack, buffer, light, packedOverlay);
            }
        }

        poseStack.popPose();
    }
}
