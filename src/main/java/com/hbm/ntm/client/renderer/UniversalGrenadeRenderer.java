package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.entity.projectile.DynamiteStickEntity;
import com.hbm.ntm.item.UniversalGrenadeItem;
import com.hbm.ntm.item.UniversalGrenadeItem.Filling;
import com.hbm.ntm.item.UniversalGrenadeItem.Fuze;
import com.hbm.ntm.item.UniversalGrenadeItem.Shell;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/** Exact RenderGrenadeUniversal body-layer order, routed through the shared OBJ backend. */
public final class UniversalGrenadeRenderer extends EntityRenderer<DynamiteStickEntity> {
    private static final ResourceLocation FALLBACK_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/item/stick_dynamite.png");
    private final ThrownItemRenderer<DynamiteStickEntity> fallback;

    public UniversalGrenadeRenderer(EntityRendererProvider.Context context) {
        super(context);
        fallback = new ThrownItemRenderer<>(context);
        shadowRadius = 0.0F;
    }

    @Override
    public void render(DynamiteStickEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getItem();
        if (!(stack.getItem() instanceof UniversalGrenadeItem)) {
            fallback.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
            return;
        }

        Shell shell = UniversalGrenadeItem.getShell(stack);
        Filling filling = UniversalGrenadeItem.getFilling(stack);
        Fuze fuze = UniversalGrenadeItem.getFuze(stack);
        poseStack.pushPose();
        poseStack.scale(0.0625F, 0.0625F, 0.0625F);
        LegacyPoseRotations.rotateYDegrees(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()));
        LegacyPoseRotations.rotateXDegrees(poseStack, entity.grenadeSpin(partialTick));
        if (entity.bounceCount() > 0) {
            LegacyPoseRotations.rotateZDegrees(poseStack, -80.0F);
        }
        renderBody(shell, filling, fuze, poseStack, buffer, packedLight);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    static void renderBody(Shell shell, Filling filling, Fuze fuze, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        String part = switch (shell) {
            case FRAG -> "Frag";
            case STICK -> "Stick";
            case TECH -> "Tech";
            case NUKE -> "Nuka";
        };
        ObjWeaponModels.renderPart(ObjWeaponModels.GRENADES, part, shellTexture(shell), poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY);
        ObjWeaponModels.renderPart(ObjWeaponModels.GRENADES, part, bodyTexture(shell), poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY, red(filling.bodyColor()), green(filling.bodyColor()),
                blue(filling.bodyColor()), 255);
        ObjWeaponModels.renderPart(ObjWeaponModels.GRENADES, part, fuzeTexture(shell), poseStack, buffer,
                packedLight, OverlayTexture.NO_OVERLAY, red(fuze.bandColor()), green(fuze.bandColor()),
                blue(fuze.bandColor()), 255);
        ObjWeaponModels.renderPart(ObjWeaponModels.GRENADES, part, labelTexture(shell), poseStack, buffer,
                shell == Shell.TECH ? 0xF000F0 : packedLight, OverlayTexture.NO_OVERLAY,
                red(filling.labelColor()), green(filling.labelColor()), blue(filling.labelColor()), 255);
    }

    static ResourceLocation shellTexture(Shell shell) {
        return switch (shell) {
            case FRAG -> ObjWeaponModels.GRENADE_FRAG_TEXTURE;
            case STICK -> ObjWeaponModels.GRENADE_STICK_TEXTURE;
            case TECH -> ObjWeaponModels.GRENADE_TECH_TEXTURE;
            case NUKE -> ObjWeaponModels.GRENADE_NUKA_TEXTURE;
        };
    }

    private static ResourceLocation bodyTexture(Shell shell) {
        return switch (shell) {
            case FRAG -> ObjWeaponModels.GRENADE_FRAG_BODY_TEXTURE;
            case STICK -> ObjWeaponModels.GRENADE_STICK_BODY_TEXTURE;
            case TECH -> ObjWeaponModels.GRENADE_TECH_BODY_TEXTURE;
            case NUKE -> ObjWeaponModels.GRENADE_NUKA_BODY_TEXTURE;
        };
    }

    private static ResourceLocation labelTexture(Shell shell) {
        return switch (shell) {
            case FRAG -> ObjWeaponModels.GRENADE_FRAG_LABEL_TEXTURE;
            case STICK -> ObjWeaponModels.GRENADE_STICK_LABEL_TEXTURE;
            case TECH -> ObjWeaponModels.GRENADE_TECH_LIGHTS_TEXTURE;
            case NUKE -> ObjWeaponModels.GRENADE_NUKA_LABEL_TEXTURE;
        };
    }

    private static ResourceLocation fuzeTexture(Shell shell) {
        return switch (shell) {
            case FRAG -> ObjWeaponModels.GRENADE_FRAG_FUZE_TEXTURE;
            case STICK -> ObjWeaponModels.GRENADE_STICK_FUZE_TEXTURE;
            case TECH -> ObjWeaponModels.GRENADE_TECH_FUZE_TEXTURE;
            case NUKE -> ObjWeaponModels.GRENADE_NUKA_FUZE_TEXTURE;
        };
    }

    private static int red(int color) { return color >> 16 & 255; }
    private static int green(int color) { return color >> 8 & 255; }
    private static int blue(int color) { return color & 255; }

    @Override
    public ResourceLocation getTextureLocation(DynamiteStickEntity entity) {
        return entity.getItem().getItem() instanceof UniversalGrenadeItem
                ? shellTexture(UniversalGrenadeItem.getShell(entity.getItem())) : FALLBACK_TEXTURE;
    }
}
