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
import net.minecraft.world.phys.Vec3;

public class SoyuzMultiblockGhostRenderer implements BlockEntityRenderer<SoyuzStructBlockEntity> {
    private static final int GHOST_ALPHA = Math.round(LegacyTileRenderPlans.SOYUZ_GHOST_ALPHA * 255.0F);
    private static final GhostRange[] RANGES = {
            new GhostRange("struct_launcher", -6, 6, 3, 4, -6, 6),
            new GhostRange("struct_launcher", -1, 1, 3, 4, -8, -7),
            new GhostRange("struct_launcher", -2, 2, 3, 4, 7, 9),
            new GhostRange("struct_launcher", -2, 2, 51, 51, 5, 9),
            new GhostRange("struct_launcher", -1, 1, 38, 38, -8, -6),
            new GhostRange("concrete_smooth", 3, 6, 0, 2, 3, 6),
            new GhostRange("concrete_smooth", -6, -3, 0, 2, 3, 6),
            new GhostRange("concrete_smooth", -6, -3, 0, 2, -6, -3),
            new GhostRange("concrete_smooth", 3, 6, 0, 2, -6, -3),
            new GhostRange("concrete_smooth", -1, 1, 0, 2, -8, -6),
            new GhostRange("concrete_smooth", -2, 2, 0, 2, 5, 9),
            new GhostRange("struct_scaffold", -1, 1, 5, 50, 6, 8),
            new GhostRange("struct_scaffold", 0, 0, 5, 37, -7, -7)
    };
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
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(SoyuzStructBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(SoyuzStructBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            int alpha = LegacyBlockEntityRenderCulling.fadedStaticAlpha(GHOST_ALPHA);
            if (alpha <= 0) {
                return;
            }
            int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);
            poseStack.pushPose();
            poseStack.translate(1.0D, 1.0D, 0.0D);
            LegacyMachineEffectPresenter.enqueueAtlasSpriteQuadGroup(PresentStage.AFTER_BLOCK_ENTITIES,
                    poseStack, buffer, LegacyTexturedRenderMode.TRANSLUCENT_DEPTH_WRITE, quads -> {
                for (GhostRange range : RANGES) {
                    renderRange(range, quads, light, alpha);
                }
            });
            poseStack.popPose();
        }
    }

    private static void renderRange(GhostRange range, LegacyTexturedQuadRenderer.SpritePixelQuadSink quads,
            int packedLight, int alpha) {
        TextureAtlasSprite sprite = textureFor(range.textureRole());
        for (int x = range.minX(); x <= range.maxX(); x++) {
            for (int y = range.minY(); y <= range.maxY(); y++) {
                for (int z = range.minZ(); z <= range.maxZ(); z++) {
                    LegacyAtlasCuboidRenderer.smallBlock(sprite, sprite, sprite, sprite, sprite, sprite,
                            quads, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFF, alpha, x, y, z);
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
        return LegacyTexturedQuadRenderer.blockSprite(HbmNtm.MOD_ID, "block/" + name);
    }

    private record GhostRange(String textureRole, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
    }
}
