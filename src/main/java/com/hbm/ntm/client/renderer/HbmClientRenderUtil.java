package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.ObjRenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public final class HbmClientRenderUtil {
    private HbmClientRenderUtil() {
    }

    public static void renderSingleBlock(BlockRenderDispatcher dispatcher, BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        dispatcher.renderSingleBlock(state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY, null);
    }

    public static void renderBlockModel(BlockRenderDispatcher dispatcher, BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        BakedModel model = dispatcher.getBlockModel(state);
        ObjRenderUtils.renderBlockModel(model, state, dispatcher.getModelRenderer(), poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, null, 1.0F);
    }

    @SuppressWarnings("deprecation")
    public static void bindParticleAtlas() {
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
    }
}
