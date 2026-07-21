package com.hbm.ntm.client.renderer;

import com.hbm.ntm.armor.PowerArmorWeaponRuntime;
import com.hbm.ntm.client.LegacySednaFirstPersonMotion;
import com.hbm.ntm.client.obj.ObjArmorModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.PowerArmorWeaponItem;
import com.hbm.ntm.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Exact 1.7.10 {@code ItemRenderPAMelee}/{@code Armor*PAMelee} arm submission,
 * expressed through the existing modern OBJ armor renderer.
 */
public final class PowerArmorWeaponItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final PowerArmorWeaponItemRenderer INSTANCE = new PowerArmorWeaponItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());

    private ItemStack lastFirstPersonStack = ItemStack.EMPTY;
    private boolean lastFirstPersonNcrpa;
    private long equipStartNanos;

    private PowerArmorWeaponItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet entityModels) {
        super(dispatcher, entityModels);
    }

    /**
     * Called by the existing RenderHandEvent takeover point. Returning false leaves
     * the vanilla hand untouched when the legacy armor provider is unavailable.
     */
    public boolean renderFirstPerson(ItemStack stack, Player player, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, float partialTick) {
        if (!(stack.getItem() instanceof PowerArmorWeaponItem weapon)
                || weapon.kind() != PowerArmorWeaponItem.Kind.MELEE
                || !PowerArmorWeaponRuntime.canUse(player, PowerArmorWeaponItem.Kind.MELEE)) {
            return false;
        }
        boolean ncrpa = player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.NCRPA_PLATE.get());
        updateEquipStart(stack, ncrpa);

        poseStack.pushPose();
        // ItemRenderPAMelee inherits ItemRenderWeaponBase: default turn 2.75,
        // with its source-specific sway magnitude 2 and period .5.
        LegacySednaFirstPersonMotion.applyPowerArmorMelee(Minecraft.getInstance(), poseStack, partialTick);
        applyLegacyFirstPersonSetup(player, poseStack);
        poseStack.translate(0.0D, -1.5D, 0.5D);
        poseStack.scale(0.125F, 0.125F, 0.125F);
        renderAnimatedArms(stack, ncrpa, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, partialTick);
        poseStack.popPose();
        return true;
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof PowerArmorWeaponItem weapon)
                || weapon.kind() != PowerArmorWeaponItem.Kind.MELEE) {
            return;
        }
        // ItemRenderPAMelee#renderOther returns immediately for old EQUIPPED.
        if (displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            return;
        }

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            // ItemRenderPAMelee#setupInv followed by #renderInv.
            poseStack.scale(1.0F, 1.0F, -1.0F);
            poseStack.translate(8.0D, 8.0D, 0.0D);
            poseStack.scale(2.5F, 2.5F, 2.5F);
            poseStack.scale(0.3125F, 0.3125F, 0.3125F);
            LegacyPoseRotations.rotateZDegrees(poseStack, 135.0F);
            LegacyPoseRotations.rotateYDegrees(poseStack, 135.0F);
            poseStack.translate(0.0D, -5.5D, 0.0D);
            poseStack.translate(-3.5D, 0.0D, 0.0D);
            renderStaticArmPair(poseStack, buffer, packedLight, packedOverlay, 7.0D, 1.0D, -1.0D);
        } else {
            // ItemRenderWeaponBase#setupEntity followed by ItemRenderPAMelee#renderOther.
            poseStack.scale(0.125F, 0.125F, 0.125F);
            LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
            poseStack.scale(0.3125F, 0.3125F, 0.3125F);
            LegacyPoseRotations.rotateXDegrees(poseStack, 90.0F);
            poseStack.translate(0.0D, -5.5D, 0.0D);
            poseStack.translate(-2.0D, 0.0D, 0.0D);
            renderStaticArmPair(poseStack, buffer, packedLight, packedOverlay, 4.0D, 0.0D, 0.0D);
        }
        poseStack.popPose();
    }

    private void updateEquipStart(ItemStack stack, boolean ncrpa) {
        if (stack != lastFirstPersonStack || ncrpa != lastFirstPersonNcrpa) {
            lastFirstPersonStack = stack;
            lastFirstPersonNcrpa = ncrpa;
            equipStartNanos = System.nanoTime();
        }
    }

    private void renderAnimatedArms(ItemStack stack, boolean ncrpa, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay, float partialTick) {
        double millis = (PowerArmorWeaponRuntime.age(stack) + Math.max(0.0F, partialTick)) * 50.0D;
        int phase = PowerArmorWeaponRuntime.phase(stack);
        double equip = phase == 0 ? equipProgress(ncrpa) : 1.0D;
        if (ncrpa) {
            renderNcrpa(stack, phase, millis, equip, poseStack, buffer, packedLight, packedOverlay);
        } else {
            renderRpa(stack, phase, millis, equip, poseStack, buffer, packedLight, packedOverlay);
        }
    }

    private void renderRpa(ItemStack stack, int phase, double millis, double equip, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double swingRight = phase == 1 ? sequence(millis, frame(1, 150, Ease.SIN_DOWN), frame(0, 250, Ease.SIN_FULL)) : 0.0D;
        double swingLeft = phase == 1 ? sequence(millis, frame(0, 300, Ease.LINEAR), frame(1, 150, Ease.SIN_DOWN), frame(0, 250, Ease.SIN_FULL)) : 0.0D;
        double slapTurn = phase == 2 ? sequence(millis, frame(1, 250, Ease.LINEAR), frame(1, 150, Ease.LINEAR), frame(0, 350, Ease.LINEAR)) : 0.0D;
        double slap = phase == 2 ? sequence(millis, frame(0, 250, Ease.LINEAR), frame(1, 150, Ease.SIN_DOWN), frame(0, 350, Ease.SIN_FULL)) : 0.0D;
        double forwardTilt = 60.0D - 60.0D * equip;

        poseStack.pushPose();
        poseStack.translate(-12.0D * swingLeft + 2.0D * slapTurn - 5.0D * slap, 6.0D * slap,
                5.0D * swingLeft + 8.0D * slap);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (forwardTilt - swingRight * 20.0D));
        poseStack.translate(3.0D, 0.0D, 0.0D);
        poseStack.translate(6.0D, 8.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (60.0D * swingLeft + 45.0D * slap));
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) (60.0D + 15.0D * swingLeft + 45.0D * slapTurn));
        poseStack.translate(-6.0D, -8.0D, 0.0D);
        ObjArmorModels.renderPart(ObjArmorModels.REMNANT, "LeftArm", ObjArmorModels.RPA_ARM_TEXTURE, poseStack,
                buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(12.0D * swingRight - 2.0D * slapTurn + 5.0D * slap, 6.0D * slap,
                5.0D * swingRight + 8.0D * slap);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (forwardTilt - swingLeft * 20.0D));
        poseStack.translate(-3.0D, 0.0D, 0.0D);
        poseStack.translate(-6.0D, 8.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (-60.0D * swingRight - 45.0D * slap));
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) (-60.0D - 15.0D * swingRight - 45.0D * slapTurn));
        poseStack.translate(6.0D, -8.0D, 0.0D);
        ObjArmorModels.renderPart(ObjArmorModels.REMNANT, "RightArm", ObjArmorModels.RPA_ARM_TEXTURE, poseStack,
                buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void renderNcrpa(ItemStack stack, int phase, double millis, double equip, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        double swingRight = phase == 1 ? sequence(millis, frame(1, 250, Ease.SIN_DOWN), frame(0, 500, Ease.SIN_FULL)) : 0.0D;
        double swingLeft = phase == 1 ? sequence(millis, frame(0, 500, Ease.LINEAR), frame(1, 250, Ease.SIN_DOWN), frame(0, 500, Ease.SIN_FULL)) : 0.0D;
        double sweepTurn = phase == 2 ? sequence(millis, frame(1, 100, Ease.LINEAR), frame(1, 350, Ease.LINEAR), frame(0, 500, Ease.LINEAR)) : 0.0D;
        double sweepCut = phase == 2 ? sequence(millis, frame(0, 100, Ease.LINEAR), frame(1, 250, Ease.SIN_DOWN), frame(1, 100, Ease.LINEAR), frame(0, 500, Ease.SIN_FULL)) : 0.0D;
        double forwardTilt = 60.0D - 60.0D * equip;

        poseStack.pushPose();
        poseStack.translate(-14.0D * swingLeft - 4.0D * sweepTurn, 6.0D * sweepCut,
                2.0D * swingLeft + 8.0D * sweepCut);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (forwardTilt + swingRight * 40.0D - 60.0D * sweepCut));
        poseStack.translate(3.0D, 0.0D, 0.0D);
        poseStack.translate(6.0D, 8.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (90.0D * swingLeft));
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) (60.0D + 30.0D * swingLeft - 90.0D * sweepTurn));
        poseStack.translate(-6.0D, -8.0D, 0.0D);
        ObjArmorModels.renderPart(ObjArmorModels.NCR, "LeftArm", ObjArmorModels.NCRPA_ARM_TEXTURE, poseStack,
                buffer, packedLight, packedOverlay);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(14.0D * swingRight + 4.0D * sweepTurn, 6.0D * sweepCut,
                2.0D * swingRight + 8.0D * sweepCut);
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (forwardTilt + swingLeft * 40.0D - 60.0D * sweepCut));
        poseStack.translate(-3.0D, 0.0D, 0.0D);
        poseStack.translate(-6.0D, 8.0D, 0.0D);
        LegacyPoseRotations.rotateZDegrees(poseStack, (float) (-90.0D * swingRight));
        LegacyPoseRotations.rotateYDegrees(poseStack, (float) (-60.0D - 30.0D * swingRight + 90.0D * sweepTurn));
        poseStack.translate(6.0D, -8.0D, 0.0D);
        ObjArmorModels.renderPart(ObjArmorModels.NCR, "RightArm", ObjArmorModels.NCRPA_ARM_TEXTURE, poseStack,
                buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void applyLegacyFirstPersonSetup(Player player, PoseStack poseStack) {
        poseStack.translate(0.0D, 0.0D, 1.0D);
        if (player.isCrouching()) {
            poseStack.translate(0.0D, -3.875D / 8.0D, 0.0D);
            return;
        }
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
        poseStack.translate(0.8D, -0.6D, -0.4D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F);
    }

    private static void renderStaticArmPair(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            int packedOverlay, double rightOffsetX, double rightOffsetY, double rightOffsetZ) {
        ObjArmorModels.renderPart(ObjArmorModels.NCR, "LeftArm", ObjArmorModels.NCRPA_ARM_TEXTURE, poseStack,
                buffer, packedLight, packedOverlay);
        poseStack.translate(rightOffsetX, rightOffsetY, rightOffsetZ);
        ObjArmorModels.renderPart(ObjArmorModels.NCR, "RightArm", ObjArmorModels.NCRPA_ARM_TEXTURE, poseStack,
                buffer, packedLight, packedOverlay);
    }

    private double equipProgress(boolean ncrpa) {
        if (equipStartNanos == 0L) {
            return 1.0D;
        }
        double elapsedMillis = (System.nanoTime() - equipStartNanos) / 1_000_000.0D;
        return sinDown(elapsedMillis / (ncrpa ? 750.0D : 250.0D));
    }

    private static double sequence(double millis, Frame... frames) {
        double previous = 0.0D;
        double start = 0.0D;
        for (Frame frame : frames) {
            double end = start + frame.durationMillis();
            if (millis < end) {
                return interpolate(previous, frame.value(), (millis - start) / frame.durationMillis(), frame.ease());
            }
            previous = frame.value();
            start = end;
        }
        return previous;
    }

    private static double interpolate(double previous, double value, double progress, Ease ease) {
        double t = Math.max(0.0D, Math.min(1.0D, progress));
        return previous + (value - previous) * switch (ease) {
            case LINEAR -> t;
            case SIN_DOWN -> sinDown(t);
            case SIN_FULL -> (-Math.cos(t * Math.PI) + 1.0D) / 2.0D;
        };
    }

    private static double sinDown(double progress) {
        return Math.sin(Math.max(0.0D, Math.min(1.0D, progress)) * Math.PI / 2.0D);
    }

    private static Frame frame(double value, double durationMillis, Ease ease) {
        return new Frame(value, durationMillis, ease);
    }

    private record Frame(double value, double durationMillis, Ease ease) {
    }

    private enum Ease {
        LINEAR,
        SIN_DOWN,
        SIN_FULL
    }
}
