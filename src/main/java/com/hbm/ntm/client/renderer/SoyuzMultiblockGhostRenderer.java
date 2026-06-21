package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.SoyuzStructBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.ObjRenderContext;
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
    private static final ResourceLocation STRUCT_LAUNCHER = blockTexture("struct_launcher");
    private static final ResourceLocation CONCRETE = blockTexture("concrete");
    private static final ResourceLocation STRUCT_SCAFFOLD = blockTexture("struct_scaffold");

    public SoyuzMultiblockGhostRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(SoyuzStructBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(SoyuzStructBlockEntity blockEntity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, blockEntity.getBlockState(),
                LegacyRenderLighting.resolveMultiblockLight(blockEntity, packedLight), OverlayTexture.NO_OVERLAY)
                .withTranslucencyDepthWrite()
                .withColor(0xFFFFFF, Math.round(PLAN.color().alpha() * 255.0F));
        poseStack.pushPose();
        poseStack.translate(1.0D, 1.0D, 0.0D);
        for (LegacyTileRenderPlans.SoyuzGhostRangePlan range : PLAN.ranges()) {
            renderRange(range, context);
        }
        poseStack.popPose();
    }

    private static void renderRange(LegacyTileRenderPlans.SoyuzGhostRangePlan range, ObjRenderContext context) {
        TextureAtlasSprite sprite = textureFor(range.textureRole());
        for (int x = range.minX(); x <= range.maxX(); x++) {
            for (int y = range.minY(); y <= range.maxY(); y++) {
                for (int z = range.minZ(); z <= range.maxZ(); z++) {
                    LegacyAtlasCuboidRenderer.smallBlock(sprite, sprite, sprite, sprite, sprite, sprite,
                            context, x, y, z);
                }
            }
        }
    }

    private static TextureAtlasSprite textureFor(String role) {
        return LegacyTexturedQuadRenderer.blockSprite(switch (role) {
            case "concrete_smooth" -> CONCRETE;
            case "struct_scaffold" -> STRUCT_SCAFFOLD;
            default -> STRUCT_LAUNCHER;
        });
    }

    private static ResourceLocation blockTexture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + name);
    }
}
