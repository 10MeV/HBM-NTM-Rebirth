package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.ObjMissilePartModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.missile.CustomMissileItem;
import com.hbm.ntm.item.missile.MissilePartItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class CustomMissileItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation FALLBACK_ICON =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/item/missile_custom.png");
    private static final float GUI_TARGET_SIZE = 0.9F;
    private static final float WORLD_TARGET_SIZE = 0.68F;

    public static final CustomMissileItemRenderer INSTANCE = new CustomMissileItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private CustomMissileItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        CustomMissileParts parts = CustomMissileParts.fromStack(stack);
        if (parts == null || !parts.hasRenderablePart()) {
            renderFallbackIcon(displayContext, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        poseStack.pushPose();
        applyDisplay(displayContext, poseStack, parts);
        ObjMissilePartModels.renderMissile(parts.thrusterPart(), parts.finsPart(), parts.fuselagePart(),
                parts.warheadPart(), poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack, CustomMissileParts parts) {
        MissilePartRenderCache.CustomMissileFit fit = parts.fit();
        float fitScale = displayContext == ItemDisplayContext.GUI ? fit.guiFitScale() : fit.worldFitScale();

        poseStack.translate(0.5D, 0.5D, 0.5D);
        if (displayContext == ItemDisplayContext.GUI) {
            LegacyPoseRotations.rotateZDegrees(poseStack, 135.0F);
            LegacyPoseRotations.rotateXDegrees(poseStack, 215.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, -((System.currentTimeMillis() / 25L) % 360L));
        } else {
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            if (displayContext == ItemDisplayContext.GROUND) {
                poseStack.scale(0.8F, 0.8F, 0.8F);
            } else if (displayContext.firstPerson()) {
                poseStack.translate(0.0D, 0.1D, 0.0D);
                poseStack.scale(0.85F, 0.85F, 0.85F);
            }
        }
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.scale(-fitScale, -fitScale, -fitScale);
        } else {
            poseStack.scale(fitScale, fitScale, fitScale);
        }
        poseStack.translate(-fit.centerX(), -fit.centerY(), -fit.centerZ());
    }

    private static void renderFallbackIcon(ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.0D, 0.0D, 0.5D);
        } else {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.translate(-0.5D, -0.5D, 0.0D);
        }

        LegacyTexturedQuadRenderer.quadDirect(FALLBACK_ICON, poseStack, buffer, packedLight, packedOverlay,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL, 0.0F, 0.0F, 1.0F,
                0.0F, 1.0F, 0.0F, 0.0F, 1.0F,
                1.0F, 1.0F, 0.0F, 1.0F, 1.0F,
                1.0F, 0.0F, 0.0F, 1.0F, 0.0F,
                0.0F, 0.0F, 0.0F, 0.0F, 0.0F,
                0xFFFFFF, 255);
        poseStack.popPose();
    }

    @Nullable
    private static MissilePartRenderCache.PartRenderSpec part(ItemStack stack, String tagKey,
            ObjMissilePartModels.PartKind expectedKind) {
        ResourceLocation id = CustomMissileItem.getPartId(stack, tagKey);
        if (id == null) {
            return null;
        }
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (!(item instanceof MissilePartItem missilePart)) {
            return null;
        }
        MissilePartRenderCache.PartRenderSpec spec = MissilePartRenderCache.spec(missilePart.legacyModelKey());
        return spec != null && spec.part().kind() == expectedKind ? spec : null;
    }

    private static final class CustomMissileParts {
        private final MissilePartRenderCache.PartRenderSpec warhead;
        private final MissilePartRenderCache.PartRenderSpec fuselage;
        private final MissilePartRenderCache.PartRenderSpec fins;
        private final MissilePartRenderCache.PartRenderSpec thruster;
        private final MissilePartRenderCache.CustomMissileFit fit;

        private CustomMissileParts(MissilePartRenderCache.PartRenderSpec warhead,
                MissilePartRenderCache.PartRenderSpec fuselage,
                MissilePartRenderCache.PartRenderSpec fins,
                MissilePartRenderCache.PartRenderSpec thruster) {
            this.warhead = warhead;
            this.fuselage = fuselage;
            this.fins = fins;
            this.thruster = thruster;
            this.fit = MissilePartRenderCache.customMissileFit(thruster, fins, fuselage, warhead,
                    GUI_TARGET_SIZE, WORLD_TARGET_SIZE);
        }

        @Nullable
        static CustomMissileParts fromStack(ItemStack stack) {
            if (!(stack.getItem() instanceof CustomMissileItem)) {
                return null;
            }
            return new CustomMissileParts(
                    CustomMissileItemRenderer.part(stack, CustomMissileItem.TAG_WARHEAD,
                            ObjMissilePartModels.PartKind.WARHEAD),
                    CustomMissileItemRenderer.part(stack, CustomMissileItem.TAG_FUSELAGE,
                            ObjMissilePartModels.PartKind.FUSELAGE),
                    CustomMissileItemRenderer.part(stack, CustomMissileItem.TAG_STABILITY,
                            ObjMissilePartModels.PartKind.FINS),
                    CustomMissileItemRenderer.part(stack, CustomMissileItem.TAG_THRUSTER,
                            ObjMissilePartModels.PartKind.THRUSTER));
        }

        boolean hasRenderablePart() {
            return warhead != null || fuselage != null || fins != null || thruster != null;
        }

        MissilePartRenderCache.CustomMissileFit fit() {
            return fit;
        }

        ObjMissilePartModels.LegacyMissilePart warheadPart() {
            return part(warhead);
        }

        ObjMissilePartModels.LegacyMissilePart fuselagePart() {
            return part(fuselage);
        }

        ObjMissilePartModels.LegacyMissilePart finsPart() {
            return part(fins);
        }

        ObjMissilePartModels.LegacyMissilePart thrusterPart() {
            return part(thruster);
        }

        private static ObjMissilePartModels.LegacyMissilePart part(MissilePartRenderCache.PartRenderSpec spec) {
            return spec == null ? null : spec.part();
        }
    }
}
