package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.HorizontalMachineBlock;
import com.hbm.ntm.blockentity.ICFStructCoreBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjRenderContext;
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
    private static final ResourceLocation SCAFFOLD = blockTexture("legacy_blocks/icf_component");
    private static final ResourceLocation VESSEL_WELDED =
            blockTexture("legacy_blocks/icf_component.vessel_welded");
    private static final ResourceLocation STRUCTURE_BOLTED =
            blockTexture("legacy_blocks/icf_component.structure_bolted");

    public ICFStructCoreRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(ICFStructCoreBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(ICFStructCoreBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(HorizontalMachineBlock.FACING)
                ? state.getValue(HorizontalMachineBlock.FACING)
                : Direction.NORTH;
        Direction rot = facing.getClockWise();
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, state,
                LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight), OverlayTexture.NO_OVERLAY)
                .withTranslucencyNoDepthWrite()
                .withColor(0xFFFFFF, LegacyAtlasCuboidRenderer.SMALL_BLOCK_GHOST_ALPHA);

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
                            context, x, y, z);
                }
            }
        }
    }

    private static TextureAtlasSprite textureFor(int component) {
        return LegacyTexturedQuadRenderer.blockSprite(switch (component) {
            case ICFStructCoreBlockEntity.PREVIEW_META_VESSEL_WELDED -> VESSEL_WELDED;
            case ICFStructCoreBlockEntity.PREVIEW_META_STRUCTURE_BOLTED -> STRUCTURE_BOLTED;
            default -> SCAFFOLD;
        });
    }

    private static ResourceLocation blockTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + name);
    }
}
