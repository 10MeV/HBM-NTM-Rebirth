package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.SoyuzStructBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter;
import com.hbm.ntm.client.render.LegacyMachineEffectPresenter.PresentStage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public class SoyuzMultiblockGhostRenderer implements BlockEntityRenderer<SoyuzStructBlockEntity> {
    private static final LegacyTileRenderPlans.SoyuzMultiblockGhostPlan PLAN =
            LegacyTileRenderPlans.soyuzMultiblockGhostPlan();
    private static final TextureAtlasSprite STRUCT_LAUNCHER = sprite("struct_launcher");
    private static final TextureAtlasSprite CONCRETE = sprite("concrete");
    private static final TextureAtlasSprite STRUCT_SCAFFOLD = sprite("struct_scaffold");

    public SoyuzMultiblockGhostRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SoyuzStructBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.MACHINE;
    }

    @Override
    public void render(SoyuzStructBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
        int alpha = Math.round(PLAN.color().alpha() * 255.0F);
        poseStack.pushPose();
        poseStack.translate(1.0D, 1.0D, 0.0D);
        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack, queuedPose -> {
            for (LegacyTileRenderPlans.SoyuzGhostRangePlan range : PLAN.ranges()) {
                renderRange(range, queuedPose, buffer, light, alpha);
            }
        });
        poseStack.popPose();
    }

    private static void renderRange(LegacyTileRenderPlans.SoyuzGhostRangePlan range, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int alpha) {
        TextureAtlasSprite sprite = textureFor(range.textureRole());
        for (int x = range.minX(); x <= range.maxX(); x++) {
            for (int y = range.minY(); y <= range.maxY(); y++) {
                for (int z = range.minZ(); z <= range.maxZ(); z++) {
                    LegacyAtlasCuboidRenderer.smallBlock(sprite, sprite, sprite, sprite, sprite, sprite,
                            poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFF, alpha,
                            LegacyTexturedRenderMode.TRANSLUCENT_DEPTH_WRITE, x, y, z);
                }
            }
        }
    }

    private static TextureAtlasSprite textureFor(String role) {
        return switch (role) {
            case "concrete_smooth" -> CONCRETE;
            case "struct_scaffold" -> STRUCT_SCAFFOLD;
            default -> STRUCT_LAUNCHER;
        };
    }

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + name));
    }
}
