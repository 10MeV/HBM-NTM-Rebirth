package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.block.NTMAnvilBlock;
import com.hbm.ntm.client.obj.ObjBlockModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.NTMAnvilBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Replays RenderAnvil#renderInventoryBlock after the modern centered-GUI transform used by the burner press.
 */
public final class NTMAnvilItemRenderer extends BlockEntityWithoutLevelRenderer {
    // The source OBJ is already in block units. The requested inventory presentation is 80% larger.
    private static final float GUI_FIT_SCALE = 0.576F;
    // The standard centered item anchor is 0.375; move the preview upward by two GUI pixels (2 / 16).
    private static final double GUI_CENTER_Y = 0.25D;
    private static final AABB LEGACY_INVENTORY_BOUNDS = legacyInventoryBounds(ObjBlockModels.ANVIL.boundsAll());

    public static final NTMAnvilItemRenderer INSTANCE = new NTMAnvilItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private NTMAnvilItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof NTMAnvilBlockItem item)
                || !(item.getBlock() instanceof NTMAnvilBlock anvil)) {
            return;
        }

        poseStack.pushPose();
        applyDisplayTransform(context, poseStack);
        // Exact local matrix from 1.7.10 RenderAnvil#renderInventoryBlock.
        poseStack.translate(0.0D, -0.5D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        ObjBlockModels.ANVIL.renderAll(textureFor(anvil), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyDisplayTransform(ItemDisplayContext context, PoseStack poseStack) {
        Vec3 center = LEGACY_INVENTORY_BOUNDS.getCenter();
        if (context == ItemDisplayContext.GUI) {
            // Same centered 30°/45° slot anchor used by the burner press item renderer.
            poseStack.translate(0.5D, GUI_CENTER_Y, 0.0D);
            LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            poseStack.scale(GUI_FIT_SCALE, GUI_FIT_SCALE, GUI_FIT_SCALE);
            poseStack.translate(-center.x, -center.y, -center.z);
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.scale(GUI_FIT_SCALE * 0.82F, GUI_FIT_SCALE * 0.82F, GUI_FIT_SCALE * 0.82F);
        poseStack.translate(-center.x, -center.y, -center.z);
        if (context == ItemDisplayContext.GROUND) {
            poseStack.translate(0.0D, -0.25D, 0.0D);
            poseStack.scale(0.8F, 0.8F, 0.8F);
        } else if (context.firstPerson()) {
            poseStack.translate(0.0D, 0.1D, 0.0D);
            poseStack.scale(0.85F, 0.85F, 0.85F);
        }
    }

    private static AABB legacyInventoryBounds(AABB raw) {
        // rotateY(180°), then translate(0, -0.5, 0), matching the old renderer order.
        return new AABB(-raw.maxX, raw.minY - 0.5D, -raw.maxZ,
                -raw.minX, raw.maxY - 0.5D, -raw.minZ);
    }

    private static ResourceLocation textureFor(NTMAnvilBlock anvil) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(anvil);
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/legacy_blocks/" + key.getPath() + ".png");
    }
}
