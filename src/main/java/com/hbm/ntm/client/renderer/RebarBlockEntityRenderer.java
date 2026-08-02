package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.blockentity.RebarBlockEntity;
import com.hbm.ntm.client.obj.LegacyAtlasCuboidRenderer;
import com.hbm.ntm.client.obj.LegacyIsbrhBlockPlans;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.config.HbmClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;

/** Modern BER carrier for the source-backed 1.7.10 {@code BlockRebar} world and fill passes. */
public final class RebarBlockEntityRenderer implements BlockEntityRenderer<RebarBlockEntity> {
    private static final ResourceLocation REBAR = texture("rebar");
    private static final ResourceLocation CONCRETE = texture("concrete_liquid");

    public RebarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public int getViewDistance() {
        return LegacyBlockEntityRenderDistances.machine();
    }

    @Override
    public boolean shouldRender(RebarBlockEntity rebar, Vec3 cameraPos) {
        return BlockEntityRenderer.super.shouldRender(rebar, cameraPos)
                && LegacyBlockEntityRenderCulling.shouldRenderMachine(rebar, getViewDistance());
    }

    @Override
    public void render(RebarBlockEntity rebar, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        if (!LegacyBlockEntityRenderCulling.shouldRenderMachine(rebar, getViewDistance())) {
            return;
        }
        try (var scope = LegacyBlockEntityRenderCulling.recordMachineSubmissionScope(rebar)) {
            drawPlan(LegacyIsbrhBlockPlans.rebarWorldPlan(0, HbmClientConfig.renderRebarSimple()), REBAR,
                    poseStack, buffer, packedLight, packedOverlay);
            if (rebar.getProgress() > 0 && RebarFillRenderBudget.tryAcquire()) {
                drawPlan(LegacyIsbrhBlockPlans.rebarConcreteFillPlan(rebar.getProgress(), 0xFFFFFF), CONCRETE,
                        poseStack, buffer, packedLight, packedOverlay);
            }
        }
    }

    public static void renderInventory(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        drawPlan(LegacyIsbrhBlockPlans.rebarInventoryPlan(0), REBAR, poseStack, buffer, packedLight, packedOverlay);
    }

    private static void drawPlan(LegacyIsbrhBlockPlans.RebarRenderPlan plan, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(texture);
        for (LegacyIsbrhBlockPlans.CuboidUvPlan cuboid : plan.rebarCuboids()) {
            drawCuboid(sprite, cuboid.bounds(), poseStack, buffer, packedLight, packedOverlay);
        }
        for (LegacyIsbrhBlockPlans.CuboidUvPlan cuboid : plan.overlayCuboids()) {
            drawCuboid(sprite, cuboid.bounds(), poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private static void drawCuboid(TextureAtlasSprite sprite, LegacyAtlasCuboidRenderer.CuboidBounds bounds,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LegacyAtlasCuboidRenderer.cuboid(sprite, sprite, sprite, sprite, sprite, sprite, poseStack, buffer,
                packedLight, packedOverlay, 0xFFFFFF, 255, LegacyTexturedRenderMode.CUTOUT_NO_CULL, bounds);
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "block/" + name);
    }
}
