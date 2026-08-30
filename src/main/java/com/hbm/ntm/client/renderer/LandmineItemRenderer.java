package com.hbm.ntm.client.renderer;

import com.hbm.ntm.block.LandmineBlock;
import com.hbm.ntm.block.NavalMineBlock;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjBombModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.LandmineBlockItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Exact 1.7.10 {@code ItemRenderLibrary} item transforms for AP/HE/shrapnel/FAT/naval mines.
 * Their world TESR is intentionally not used as the inventory contract.
 */
public final class LandmineItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final float LEGACY_GUI_PIXELS = 16.0F;

    public static final LandmineItemRenderer INSTANCE = new LandmineItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private LandmineItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof LandmineBlockItem item)) {
            return;
        }
        MineKind kind = MineKind.of(item.getBlock());
        if (kind == null) {
            return;
        }

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyGuiBase(poseStack);
            applyInventory(kind, poseStack);
            applyCommon(kind, poseStack);
            // The old ItemRenderBase has a negative unit scale.  LegacyWavefrontModel uses
            // unmirrored modern OBJ coordinates, so cancel only that model-space mirror.
            poseStack.scale(-1.0F, -1.0F, -1.0F);
        } else {
            applyLegacyNonInventoryBase(displayContext, poseStack);
            applyNonInventory(kind, poseStack);
            applyCommon(kind, poseStack);
        }
        render(kind, poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyLegacyGuiBase(PoseStack poseStack) {
        poseStack.translate(8.0D / LEGACY_GUI_PIXELS, 10.0D / LEGACY_GUI_PIXELS, 0.0D);
        LegacyPoseRotations.rotateXDegrees(poseStack, 30.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
        poseStack.scale(-1.0F / LEGACY_GUI_PIXELS, -1.0F / LEGACY_GUI_PIXELS, -1.0F / LEGACY_GUI_PIXELS);
    }

    private static void applyLegacyNonInventoryBase(ItemDisplayContext context, PoseStack poseStack) {
        if (context == ItemDisplayContext.GROUND) {
            poseStack.scale(1.5F, 1.5F, 1.5F);
        } else {
            poseStack.translate(0.5D, 0.25D, 0.0D);
        }
        poseStack.scale(0.25F, 0.25F, 0.25F);
        if (context == ItemDisplayContext.GROUND) {
            LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        }
    }

    private static void applyInventory(MineKind kind, PoseStack poseStack) {
        switch (kind) {
            case AP, SHRAP -> poseStack.scale(8.0F, 8.0F, 8.0F);
            case HE -> poseStack.scale(6.0F, 6.0F, 6.0F);
            case NAVAL -> {
                poseStack.translate(0.0D, 2.0D, -1.0D);
                poseStack.scale(5.0F, 5.0F, 5.0F);
            }
            case FAT -> {
                poseStack.translate(0.0D, -1.0D, 0.0D);
                poseStack.scale(7.0F, 7.0F, 7.0F);
            }
        }
    }

    private static void applyNonInventory(MineKind kind, PoseStack poseStack) {
        if (kind == MineKind.HE) {
            poseStack.translate(0.25D, 0.625D, 0.0D);
            LegacyPoseRotations.rotateYDegrees(poseStack, 45.0F);
            LegacyPoseRotations.rotateZDegrees(poseStack, -15.0F);
        }
    }

    private static void applyCommon(MineKind kind, PoseStack poseStack) {
        switch (kind) {
            case AP, SHRAP -> poseStack.scale(1.25F, 1.25F, 1.25F);
            case HE -> poseStack.scale(4.0F, 4.0F, 4.0F);
            case FAT -> {
                poseStack.translate(0.25D, 0.0D, 0.0D);
                LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
            }
            case NAVAL -> {
            }
        }
    }

    private static void render(MineKind kind, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay) {
        LegacyWavefrontModel model = switch (kind) {
            case AP, SHRAP -> ObjBombModels.MINE_AP;
            case HE -> ObjBombModels.MINE_MARELET;
            case FAT -> ObjBombModels.MINE_FAT;
            case NAVAL -> ObjBombModels.MINE_NAVAL;
        };
        ResourceLocation texture = switch (kind) {
            case AP -> ObjBombModels.MINE_AP_GRASS_TEXTURE;
            case SHRAP -> ObjBombModels.MINE_SHRAP_TEXTURE;
            case HE -> ObjBombModels.texture("mine_marelet");
            case FAT -> ObjBombModels.rootTexture("mine_fat");
            case NAVAL -> ObjBombModels.rootTexture("nmine");
        };
        model.renderAll(texture, poseStack, buffer, packedLight, packedOverlay, LegacyTexturedRenderMode.CUTOUT_NO_CULL);
    }

    private enum MineKind {
        AP, HE, SHRAP, FAT, NAVAL;

        private static MineKind of(Block block) {
            if (block instanceof NavalMineBlock) {
                return NAVAL;
            }
            if (!(block instanceof LandmineBlock landmine)) {
                return null;
            }
            return switch (landmine.kind()) {
                case AP -> AP;
                case HE -> HE;
                case SHRAP -> SHRAP;
                case FAT -> FAT;
            };
        }
    }
}
