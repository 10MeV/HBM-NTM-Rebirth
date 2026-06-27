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
        return LegacyBlockEntityRenderDistances.LEGACY_65536_SQUARED;
    }

    @Override
    public void render(ICFStructCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        Direction rot = facing.getClockWise();
        int light = LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight);

        LegacyMachineEffectPresenter.enqueue(PresentStage.AFTER_BLOCK_ENTITIES, poseStack,
                queuedPose -> renderPreview(facing, rot, queuedPose, buffer, light));
    }

    private static void renderPreview(Direction facing, Direction rot, PoseStack poseStack,
            MultiBufferSource buffer, int light) {
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
                            poseStack, buffer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFF,
                            LegacyAtlasCuboidRenderer.SMALL_BLOCK_GHOST_ALPHA,
                            LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE, x, y, z);
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
        return LegacyTexturedQuadRenderer.blockSprite(new ResourceLocation(HbmNtm.MOD_ID, "block/" + name));
    }
}
