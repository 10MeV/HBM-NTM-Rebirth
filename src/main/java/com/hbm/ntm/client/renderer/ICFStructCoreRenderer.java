package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.ICFStructCoreBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class ICFStructCoreRenderer implements BlockEntityRenderer<ICFStructCoreBlockEntity> {
    private static final TextureAtlasSprite SCAFFOLD = sprite("legacy_blocks/icf_component");
    private static final TextureAtlasSprite VESSEL_WELDED =
            sprite("legacy_blocks/icf_component.vessel_welded");
    private static final TextureAtlasSprite STRUCTURE_BOLTED =
            sprite("legacy_blocks/icf_component.structure_bolted");

    public ICFStructCoreRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ICFStructCoreBlockEntity blockEntity) {
        return false;
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(ICFStructCoreBlockEntity blockEntity, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(blockEntity, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance());
    }

    @Override
    public void render(ICFStructCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(blockEntity, getViewDistance())) {
            return;
        }
        try (var cullingScope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(blockEntity)) {
            int alpha = LegacyBlockEntityRenderCulling.fadedStaticAlpha(
                    LegacyAtlasCuboidRenderer.SMALL_BLOCK_GHOST_ALPHA);
            if (alpha <= 0) {
                return;
            }
            BlockState state = blockEntity.getBlockState();
            Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                    ? state.getValue(HorizontalMachineBlock.FACING)
                    : Direction.NORTH;
            Direction rot = facing.getClockWise();
            int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

            LegacyMachineEffectPresenter.enqueueAtlasSpriteQuadGroup(PresentStage.AFTER_BLOCK_ENTITIES,
                    poseStack, buffer, LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE,
                    quads -> renderPreview(facing, rot, quads, light, alpha));
        }
    }

    private static void renderPreview(Direction facing, Direction rot,
            LegacyTexturedQuadRenderer.SpritePixelQuadSink quads, int light, int alpha) {
        for (int y = 0; y < ICFStructCoreBlockEntity.PREVIEW_HEIGHT; y++) {
            for (int width = ICFStructCoreBlockEntity.PREVIEW_WIDTH_MIN;
                    width <= ICFStructCoreBlockEntity.PREVIEW_WIDTH_MAX; width++) {
                for (int length = ICFStructCoreBlockEntity.PREVIEW_LENGTH_MIN;
                        length <= ICFStructCoreBlockEntity.PREVIEW_LENGTH_MAX; length++) {
                    int component = ICFStructCoreBlockEntity.legacyPreviewComponent(width, y, length);
                    if (component < 0) {
                        continue;
                    }
                    double x = facing.getStepX() * width + rot.getStepX() * length;
                    double z = facing.getStepZ() * width + rot.getStepZ() * length;
                    TextureAtlasSprite sprite = textureFor(component);
                    LegacyAtlasCuboidRenderer.smallBlock(sprite, sprite, sprite, sprite, sprite, sprite,
                            quads, light, OverlayTexture.NO_OVERLAY, 0xFFFFFF, alpha, x, y, z);
                }
            }
        }
    }

    private static TextureAtlasSprite textureFor(int component) {
        return switch (component) {
            case ICFStructCoreBlockEntity.PREVIEW_META_VESSEL_WELDED -> VESSEL_WELDED;
            case ICFStructCoreBlockEntity.PREVIEW_META_STRUCTURE_BOLTED -> STRUCTURE_BOLTED;
            default -> SCAFFOLD;
        };
    }

    private static TextureAtlasSprite sprite(String name) {
        return LegacyTexturedQuadRenderer.blockSprite(HbmNtm.MOD_ID, "block/" + name);
    }
}
