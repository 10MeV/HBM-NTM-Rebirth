package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.anim.LegacyHbmAnimations;
import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.UniversalGrenadeItem;
import com.hbm.ntm.item.UniversalGrenadeItem.Filling;
import com.hbm.ntm.item.UniversalGrenadeItem.Shell;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Source-backed {@code ItemRenderGrenade}, using the shared legacy OBJ/animation backends. */
public final class UniversalGrenadeItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final UniversalGrenadeItemRenderer INSTANCE = new UniversalGrenadeItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private UniversalGrenadeItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof UniversalGrenadeItem)) {
            return;
        }
        poseStack.pushPose();
        switch (context) {
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND -> renderFirstPerson(stack, poseStack, buffer, packedLight);
            case GUI -> {
                poseStack.translate(8.0D, 8.0D, 0.0D);
                poseStack.scale(-1.0F, -1.0F, -1.0F);
                LegacyPoseRotations.rotateZDegrees(poseStack, 45.0F);
                LegacyPoseRotations.rotateYDegrees(poseStack, 150.0F);
                LegacyPoseRotations.rotateXDegrees(poseStack, 15.0F);
                renderGrenade(stack, Context.INVENTORY, poseStack, buffer, packedLight);
            }
            case THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> {
                poseStack.scale(0.125F, 0.125F, 0.125F);
                poseStack.translate(3.0D, 1.0D, -0.5D);
                renderGrenade(stack, Context.EQUIPPED, poseStack, buffer, packedLight);
            }
            default -> {
                poseStack.scale(0.125F, 0.125F, 0.125F);
                renderGrenade(stack, Context.ENTITY, poseStack, buffer, packedLight);
            }
        }
        poseStack.popPose();
    }

    private static void renderFirstPerson(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light) {
        Shell shell = UniversalGrenadeItem.getShell(stack);
        double[] bodyMove = LegacyHbmAnimations.getRelevantTransformation("BODYMOVE");
        double[] bodyTurn = LegacyHbmAnimations.getRelevantTransformation("BODYTURN");
        double[] ringMove = LegacyHbmAnimations.getRelevantTransformation("RINGMOVE");
        double[] ringTurn = LegacyHbmAnimations.getRelevantTransformation("RINGTURN");
        double[] renderRing = LegacyHbmAnimations.getRelevantTransformation("RENDERRING");
        poseStack.scale(0.125F, 0.125F, 0.125F);
        poseStack.translate(3.0D, 1.0D, -3.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.translate(bodyMove[0], bodyMove[1], bodyMove[2]);
        switch (shell) {
            case FRAG -> {
                LegacyPoseRotations.rotateXDegrees(poseStack, (float) bodyTurn[2]);
                renderBody(stack, poseStack, buffer, light);
                renderPart("FragSpoon", shell, poseStack, buffer, light);
                if (renderRing[0] != 0.0D) {
                    poseStack.translate(ringMove[0], ringMove[1], ringMove[2]);
                    LegacyPoseRotations.rotateXDegrees(poseStack, (float) ringTurn[2]);
                    renderPart("FragRing", shell, poseStack, buffer, light);
                }
            }
            case STICK -> {
                LegacyPoseRotations.rotateZDegrees(poseStack, (float) bodyTurn[2]);
                renderBody(stack, poseStack, buffer, light);
                if (renderRing[0] != 0.0D) {
                    poseStack.translate(ringMove[0], ringMove[1], ringMove[2]);
                    LegacyPoseRotations.rotateYDegrees(poseStack, (float) ringTurn[1]);
                    renderStickCap(stack, poseStack, buffer, light);
                }
            }
            case TECH -> {
                LegacyPoseRotations.rotateXDegrees(poseStack, (float) bodyTurn[2]);
                renderBody(stack, poseStack, buffer, light);
                if (renderRing[0] != 0.0D) {
                    poseStack.translate(ringMove[0], ringMove[1], ringMove[2]);
                    LegacyPoseRotations.rotateXDegrees(poseStack, (float) ringTurn[2]);
                    renderPart("TechRing", shell, poseStack, buffer, light);
                }
            }
            case NUKE -> {
                LegacyPoseRotations.rotateZDegrees(poseStack, (float) bodyTurn[2]);
                renderBody(stack, poseStack, buffer, light);
                renderPart("NukaSpoon", shell, poseStack, buffer, light);
                if (renderRing[0] != 0.0D) {
                    poseStack.translate(ringMove[0], ringMove[1], ringMove[2]);
                    poseStack.translate(-1.0D, 5.0D, 0.0D);
                    LegacyPoseRotations.rotateZDegrees(poseStack, (float) -ringTurn[2]);
                    poseStack.translate(1.0D, -5.0D, 0.0D);
                    renderPart("NukaRing", shell, poseStack, buffer, light);
                }
            }
        }
    }

    private static void renderGrenade(ItemStack stack, Context context, PoseStack poseStack,
            MultiBufferSource buffer, int light) {
        Shell shell = UniversalGrenadeItem.getShell(stack);
        switch (shell) {
            case FRAG -> {
                if (context == Context.INVENTORY) {
                    poseStack.scale(3.0F, 3.0F, 3.0F);
                    poseStack.translate(0.0D, -2.0D, 0.0D);
                }
                renderBody(stack, poseStack, buffer, light);
                renderPart("FragSpoon", shell, poseStack, buffer, light);
                renderPart("FragRing", shell, poseStack, buffer, light);
            }
            case STICK -> {
                if (context == Context.INVENTORY) {
                    poseStack.scale(2.0F, 2.0F, 2.0F);
                    poseStack.translate(0.0D, -4.5D, 0.0D);
                } else if (context == Context.EQUIPPED) {
                    poseStack.translate(0.0D, -2.0D, 0.0D);
                }
                renderBody(stack, poseStack, buffer, light);
                renderStickCap(stack, poseStack, buffer, light);
            }
            case TECH -> {
                if (context == Context.INVENTORY) {
                    poseStack.scale(3.5F, 3.5F, 3.5F);
                    poseStack.translate(0.0D, -1.75D, 0.0D);
                } else if (context == Context.EQUIPPED) {
                    poseStack.scale(1.5F, 1.5F, 1.5F);
                    poseStack.translate(0.5D, -1.0D, 0.5D);
                }
                renderBody(stack, poseStack, buffer, light);
                renderPart("TechRing", shell, poseStack, buffer, light);
            }
            case NUKE -> {
                if (context == Context.INVENTORY) {
                    poseStack.scale(2.5F, 2.5F, 2.5F);
                    poseStack.translate(0.0D, -2.75D, 0.0D);
                } else if (context == Context.EQUIPPED) {
                    poseStack.scale(1.5F, 1.5F, 1.5F);
                    poseStack.translate(0.5D, -3.0D, 0.5D);
                }
                renderBody(stack, poseStack, buffer, light);
                renderPart("NukaSpoon", shell, poseStack, buffer, light);
                renderPart("NukaRing", shell, poseStack, buffer, light);
            }
        }
    }

    private static void renderBody(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light) {
        UniversalGrenadeRenderer.renderBody(UniversalGrenadeItem.getShell(stack), UniversalGrenadeItem.getFilling(stack),
                UniversalGrenadeItem.getFuze(stack), poseStack, buffer, light);
    }

    private static void renderStickCap(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light) {
        Filling filling = UniversalGrenadeItem.getFilling(stack);
        renderPart("StickCap", Shell.STICK, poseStack, buffer, light);
        ObjWeaponModels.renderPart(ObjWeaponModels.GRENADES, "StickCap", ObjWeaponModels.GRENADE_STICK_BODY_TEXTURE,
                poseStack, buffer, light, OverlayTexture.NO_OVERLAY, red(filling.bodyColor()), green(filling.bodyColor()),
                blue(filling.bodyColor()), 255);
    }

    private static void renderPart(String part, Shell shell, PoseStack poseStack, MultiBufferSource buffer, int light) {
        ObjWeaponModels.renderPart(ObjWeaponModels.GRENADES, part, UniversalGrenadeRenderer.shellTexture(shell),
                poseStack, buffer, light, OverlayTexture.NO_OVERLAY);
    }

    private static int red(int color) { return color >> 16 & 255; }
    private static int green(int color) { return color >> 8 & 255; }
    private static int blue(int color) { return color & 255; }

    private enum Context { INVENTORY, EQUIPPED, ENTITY }
}
