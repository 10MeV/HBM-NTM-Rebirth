package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.client.obj.ObjArmorModels;
import com.hbm.ntm.item.FsbArmorItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public final class LegacyObjArmorRenderer {
    private static final Model EMPTY_ARMOR_MODEL = new EmptyArmorModel();
    private static final int FULL_BRIGHT = 0xF000F0;

    public static void acceptFsbArmorExtensions(Consumer<IClientItemExtensions> consumer) {
        acceptObjArmorExtensions(consumer);
    }

    public static void acceptObjArmorExtensions(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                       EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return specFor(itemStack).isSupportedSlot(equipmentSlot) ? EMPTY_ARMOR_MODEL : original;
            }

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ItemRendererHolder.INSTANCE;
            }
        });
    }

    public static void renderEquippedArmor(LivingEntity entity, HumanoidModel<?> humanoid, PoseStack poseStack,
                                           MultiBufferSource buffer, int packedLight) {
        renderSlot(entity.getItemBySlot(EquipmentSlot.HEAD), EquipmentSlot.HEAD, humanoid, poseStack, buffer, packedLight);
        renderSlot(entity.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST, humanoid, poseStack, buffer, packedLight);
        renderSlot(entity.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS, humanoid, poseStack, buffer, packedLight);
        renderSlot(entity.getItemBySlot(EquipmentSlot.FEET), EquipmentSlot.FEET, humanoid, poseStack, buffer, packedLight);
    }

    public static PartVisibilityState hideLegacyPlayerParts(LivingEntity entity, HumanoidModel<?> humanoid) {
        if (!(entity instanceof Player player) || humanoid == null) {
            return PartVisibilityState.NONE;
        }
        PartVisibilityState state = PartVisibilityState.capture(humanoid);
        boolean changed = false;
        if (hidesSkinHat(player.getItemBySlot(EquipmentSlot.HEAD))) {
            humanoid.hat.visible = false;
            changed = true;
        }
        if (hidesFauLegs(player.getItemBySlot(EquipmentSlot.LEGS), player)) {
            humanoid.leftLeg.visible = false;
            humanoid.rightLeg.visible = false;
            changed = true;
        }
        return changed ? state : PartVisibilityState.NONE;
    }

    public static void restoreLegacyPlayerParts(PartVisibilityState state) {
        if (state != null) {
            state.restore();
        }
    }

    private static void renderSlot(ItemStack stack, EquipmentSlot slot, HumanoidModel<?> humanoid, PoseStack poseStack,
                                   MultiBufferSource buffer, int packedLight) {
        if (!(stack.getItem() instanceof ArmorItem armor) || slotFor(armor.getType()) != slot) {
            return;
        }
        Spec spec = specFor(stack);
        if (!spec.isSupportedSlot(slot)) {
            return;
        }

        switch (slot) {
            case HEAD -> {
                renderPart(spec, humanoid.head, poseStack, buffer, packedLight, spec.headTexture(), spec.headParts());
                renderExtras(spec, stack, humanoid.head, poseStack, buffer, packedLight, spec.headExtras());
            }
            case CHEST -> {
                renderPart(spec, humanoid.body, poseStack, buffer, packedLight, spec.chestTexture(), spec.chestParts());
                renderPart(spec, humanoid.leftArm, poseStack, buffer, packedLight, spec.armTexture(), spec.leftArmParts());
                renderPart(spec, humanoid.rightArm, poseStack, buffer, packedLight, spec.armTexture(), spec.rightArmParts());
                if (spec.jetpackTexture() != null && shouldRenderJetpack(spec, stack)) {
                    renderPart(spec, humanoid.body, poseStack, buffer, packedLight, spec.jetpackTexture(), "Jetpack");
                }
                if (spec.cassetteTexture() != null) {
                    renderTranslucentPart(spec, humanoid.body, poseStack, buffer, packedLight, spec.cassetteTexture(), "Cassette");
                }
                renderExtras(spec, stack, humanoid.body, poseStack, buffer, packedLight, spec.chestExtras());
            }
            case LEGS -> {
                renderPart(spec, humanoid.leftLeg, poseStack, buffer, packedLight, spec.legTexture(), spec.leftLegParts());
                renderPart(spec, humanoid.rightLeg, poseStack, buffer, packedLight, spec.legTexture(), spec.rightLegParts());
            }
            case FEET -> {
                renderPart(spec, humanoid.leftLeg, poseStack, buffer, packedLight, spec.legTexture(), spec.leftBootParts());
                renderPart(spec, humanoid.rightLeg, poseStack, buffer, packedLight, spec.legTexture(), spec.rightBootParts());
            }
            default -> {
            }
        }
    }

    private static EquipmentSlot slotFor(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> EquipmentSlot.HEAD;
            case CHESTPLATE -> EquipmentSlot.CHEST;
            case LEGGINGS -> EquipmentSlot.LEGS;
            case BOOTS -> EquipmentSlot.FEET;
        };
    }

    private static void renderPart(Spec spec, net.minecraft.client.model.geom.ModelPart modelPart, PoseStack poseStack,
                                   MultiBufferSource buffer, int packedLight, ResourceLocation texture, String... parts) {
        poseStack.pushPose();
        modelPart.translateAndRotate(poseStack);
        poseStack.scale(LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE);
        for (String part : parts) {
            spec.model().renderPart(part, texture, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    private static void renderTranslucentPart(Spec spec, net.minecraft.client.model.geom.ModelPart modelPart,
                                              PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                              ResourceLocation texture, String... parts) {
        poseStack.pushPose();
        modelPart.translateAndRotate(poseStack);
        poseStack.scale(LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE);
        for (String part : parts) {
            spec.model().renderPartTranslucent(part, texture, poseStack, buffer, packedLight,
                    OverlayTexture.NO_OVERLAY, 255, 255, 255, 255);
        }
        poseStack.popPose();
    }

    private static void renderExtras(Spec spec, ItemStack stack, net.minecraft.client.model.geom.ModelPart modelPart,
                                     PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                     ExtraPart... extras) {
        renderExtrasPass(spec, stack, modelPart, poseStack, buffer, packedLight, false, extras);
        renderExtrasPass(spec, stack, modelPart, poseStack, buffer, packedLight, true, extras);
    }

    private static void renderExtrasPass(Spec spec, ItemStack stack, net.minecraft.client.model.geom.ModelPart modelPart,
                                         PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                         boolean translucentPass, ExtraPart... extras) {
        for (ExtraPart extra : extras) {
            if (!extra.shouldRender(stack) || extra.usesTranslucentRenderType() != translucentPass) {
                continue;
            }
            poseStack.pushPose();
            modelPart.translateAndRotate(poseStack);
            poseStack.scale(LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                    LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE,
                    LegacyAccessoryRenderHelper.BIPED_MODEL_SCALE);
            if (extra.rotatingZ()) {
                poseStack.translate(extra.pivotX(), extra.pivotY(), extra.pivotZ());
                poseStack.mulPose(Axis.ZP.rotationDegrees((float) extra.rotationDegrees()));
                poseStack.translate(-extra.pivotX(), -extra.pivotY(), -extra.pivotZ());
            }
            renderExtra(spec, extra, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
            poseStack.popPose();
        }
    }

    private static void renderExtra(Spec spec, ExtraPart extra, PoseStack poseStack, MultiBufferSource buffer,
                                    int packedLight, int packedOverlay) {
        int light = extra.fullBright() ? FULL_BRIGHT : packedLight;
        if (extra.untextured()) {
            ObjRenderContext context = new ObjRenderContext(poseStack, buffer, null, light, packedOverlay)
                    .withColor((extra.red() << 16) | (extra.green() << 8) | extra.blue())
                    .withAlpha(extra.alpha());
            if (extra.additive()) {
                spec.model().renderPartUntexturedAdditive(extra.part(), context);
            } else {
                spec.model().renderPartUntextured(extra.part(), context);
            }
        } else if (extra.additive()) {
            spec.model().renderPartAdditive(extra.part(), extra.texture(), poseStack, buffer, FULL_BRIGHT,
                    packedOverlay, extra.red(), extra.green(), extra.blue(), extra.alpha());
        } else if (extra.translucent()) {
            spec.model().renderPartTranslucent(extra.part(), extra.texture(), poseStack, buffer, light,
                    packedOverlay, extra.red(), extra.green(), extra.blue(), extra.alpha());
        } else {
            spec.model().renderPart(extra.part(), extra.texture(), poseStack, buffer, light,
                    packedOverlay, extra.red(), extra.green(), extra.blue(), extra.alpha());
        }
    }

    private static Spec specFor(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id == null ? "" : id.getPath().toLowerCase(Locale.ROOT);
        if (path.startsWith("t51_")) {
            return T51;
        }
        if (path.startsWith("steamsuit_")) {
            return STEAMSUIT;
        }
        if (path.startsWith("dieselsuit_")) {
            return DIESELSUIT;
        }
        if (path.startsWith("ajr_")) {
            return AJR;
        }
        if (path.startsWith("ajro_")) {
            return AJRO;
        }
        if (path.startsWith("rpa_")) {
            return RPA;
        }
        if (path.startsWith("ncrpa_")) {
            return NCRPA;
        }
        if (path.startsWith("bj_")) {
            return BJ;
        }
        if (path.startsWith("envsuit_")) {
            return ENVSUIT;
        }
        if (path.startsWith("hev_")) {
            return HEV;
        }
        if (path.startsWith("fau_")) {
            return FAU;
        }
        if (path.startsWith("dns_") || path.startsWith("dnt_")) {
            return DNT;
        }
        if (path.startsWith("bismuth_")) {
            return BISMUTH;
        }
        if (path.startsWith("taurun_")) {
            return TAURUN;
        }
        if (path.startsWith("trenchmaster_")) {
            return TRENCHMASTER;
        }
        if (path.equals("goggles") || path.equals("ashglasses")) {
            return GOGGLES;
        }
        if (path.equals("nossy_hat") || path.equals("hat")) {
            return HAT;
        }
        if (path.equals("no9")) {
            return NO9;
        }
        return Spec.EMPTY;
    }

    private static boolean hidesSkinHat(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id == null ? "" : id.getPath().toLowerCase(Locale.ROOT);
        return switch (path) {
            case "t51_helmet", "steamsuit_helmet", "ajr_helmet", "ajro_helmet", "rpa_helmet",
                    "ncrpa_helmet", "envsuit_helmet", "hev_helmet", "fau_helmet", "dns_helmet",
                    "taurun_helmet", "trenchmaster_helmet" -> true;
            default -> false;
        };
    }

    private static boolean hidesFauLegs(ItemStack stack, Player player) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id == null ? "" : id.getPath().toLowerCase(Locale.ROOT);
        return "fau_legs".equals(path) && FsbArmorItem.hasFullFsbSet(player, true);
    }

    private static boolean shouldRenderJetpack(Spec spec, ItemStack stack) {
        if (spec != BJ) {
            return true;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id == null ? "" : id.getPath().toLowerCase(Locale.ROOT);
        return "bj_plate_jetpack".equals(path);
    }

    private static EquipmentSlot slotFor(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armor)) {
            return null;
        }
        return slotFor(armor.getType());
    }

    private static final Spec T51 = standard(ObjArmorModels.T51, ObjArmorModels.T51_HELMET_TEXTURE,
            ObjArmorModels.T51_CHEST_TEXTURE, ObjArmorModels.T51_ARM_TEXTURE, ObjArmorModels.T51_LEG_TEXTURE);
    private static final Spec STEAMSUIT = headBody(ObjArmorModels.STEAMSUIT, ObjArmorModels.STEAMSUIT_HELMET_TEXTURE,
            ObjArmorModels.STEAMSUIT_CHEST_TEXTURE, ObjArmorModels.STEAMSUIT_ARM_TEXTURE, ObjArmorModels.STEAMSUIT_LEG_TEXTURE,
            "LeftBoot", "RightBoot");
    private static final Spec DIESELSUIT = headBody(ObjArmorModels.DIESELSUIT, ObjArmorModels.DIESELSUIT_HELMET_TEXTURE,
            ObjArmorModels.DIESELSUIT_CHEST_TEXTURE, ObjArmorModels.DIESELSUIT_ARM_TEXTURE, ObjArmorModels.DIESELSUIT_LEG_TEXTURE,
            "LeftBoot", "RightBoot");
    private static final Spec AJR = headBody(ObjArmorModels.AJR, ObjArmorModels.AJR_HELMET_TEXTURE,
            ObjArmorModels.AJR_CHEST_TEXTURE, ObjArmorModels.AJR_ARM_TEXTURE, ObjArmorModels.AJR_LEG_TEXTURE,
            "LeftBoot", "RightBoot");
    private static final Spec AJRO = headBody(ObjArmorModels.AJR, ObjArmorModels.AJRO_HELMET_TEXTURE,
            ObjArmorModels.AJRO_CHEST_TEXTURE, ObjArmorModels.AJRO_ARM_TEXTURE, ObjArmorModels.AJRO_LEG_TEXTURE,
            "LeftBoot", "RightBoot");
    private static final Spec RPA = headBody(ObjArmorModels.REMNANT, ObjArmorModels.RPA_HELMET_TEXTURE,
            ObjArmorModels.RPA_CHEST_TEXTURE, ObjArmorModels.RPA_ARM_TEXTURE, ObjArmorModels.RPA_LEG_TEXTURE,
            "LeftBoot", "RightBoot")
            .withChestExtras(ExtraPart.fullBright("Glow", ObjArmorModels.RPA_CHEST_TEXTURE),
                    ExtraPart.rotating("Fan", ObjArmorModels.RPA_CHEST_TEXTURE, 0.0D, 4.875D, 0.0D));
    private static final Spec NCRPA = standard(ObjArmorModels.NCR, ObjArmorModels.NCRPA_HELMET_TEXTURE,
            ObjArmorModels.NCRPA_CHEST_TEXTURE, ObjArmorModels.NCRPA_ARM_TEXTURE, ObjArmorModels.NCRPA_LEG_TEXTURE)
            .withHeadExtras(ExtraPart.glow("Eyes", ObjArmorModels.NCRPA_HELMET_TEXTURE));
    private static final Spec BJ = new Spec(ObjArmorModels.BJ, ObjArmorModels.BJ_EYEPATCH_TEXTURE,
            ObjArmorModels.BJ_CHEST_TEXTURE, ObjArmorModels.BJ_ARM_TEXTURE, ObjArmorModels.BJ_LEG_TEXTURE,
            "Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftFoot", "RightFoot",
            ObjArmorModels.BJ_JETPACK_TEXTURE, null, new ExtraPart[0], new ExtraPart[0]);
    private static final Spec ENVSUIT = new Spec(ObjArmorModels.ENVSUIT, ObjArmorModels.ENVSUIT_HELMET_TEXTURE,
            ObjArmorModels.ENVSUIT_CHEST_TEXTURE, ObjArmorModels.ENVSUIT_ARM_TEXTURE, ObjArmorModels.ENVSUIT_LEG_TEXTURE,
            "Helmet", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftFoot", "RightFoot",
            null, null, new ExtraPart[] {ExtraPart.untexturedLit("Lamps", 255, 255, 204, 255)},
            new ExtraPart[0]);
    private static final Spec HEV = headBody(ObjArmorModels.HEV, ObjArmorModels.HEV_HELMET_TEXTURE,
            ObjArmorModels.HEV_CHEST_TEXTURE, ObjArmorModels.HEV_ARM_TEXTURE, ObjArmorModels.HEV_LEG_TEXTURE,
            "LeftFoot", "RightFoot");
    private static final Spec FAU = new Spec(ObjArmorModels.FAU, ObjArmorModels.FAU_HELMET_TEXTURE,
            ObjArmorModels.FAU_CHEST_TEXTURE, ObjArmorModels.FAU_ARM_TEXTURE, ObjArmorModels.FAU_LEG_TEXTURE,
            "Head", "Body", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot",
            null, ObjArmorModels.FAU_CASSETTE_TEXTURE, new ExtraPart[0], new ExtraPart[0]);
    private static final Spec DNT = headBody(ObjArmorModels.DNT, ObjArmorModels.DNT_HELMET_TEXTURE,
            ObjArmorModels.DNT_CHEST_TEXTURE, ObjArmorModels.DNT_ARM_TEXTURE, ObjArmorModels.DNT_LEG_TEXTURE,
            "LeftBoot", "RightBoot");
    private static final Spec BISMUTH = headBody(ObjArmorModels.BISMUTH, ObjArmorModels.BISMUTH_TEXTURE,
            ObjArmorModels.BISMUTH_TEXTURE, ObjArmorModels.BISMUTH_TEXTURE, ObjArmorModels.BISMUTH_TEXTURE,
            "LeftFoot", "RightFoot");
    private static final Spec TAURUN = standard(ObjArmorModels.TAURUN, ObjArmorModels.TAURUN_HELMET_TEXTURE,
            ObjArmorModels.TAURUN_CHEST_TEXTURE, ObjArmorModels.TAURUN_ARM_TEXTURE, ObjArmorModels.TAURUN_LEG_TEXTURE);
    private static final Spec TRENCHMASTER = standard(ObjArmorModels.TRENCHMASTER, ObjArmorModels.TRENCHMASTER_HELMET_TEXTURE,
            ObjArmorModels.TRENCHMASTER_CHEST_TEXTURE, ObjArmorModels.TRENCHMASTER_ARM_TEXTURE, ObjArmorModels.TRENCHMASTER_LEG_TEXTURE)
            .withHeadExtras(ExtraPart.glow("Light", ObjArmorModels.TRENCHMASTER_HELMET_TEXTURE));
    private static final Spec GOGGLES = headOnly(ObjArmorModels.GOGGLES, ObjArmorModels.GOGGLES_TEXTURE, "Cube");
    private static final Spec HAT = headOnly(ObjArmorModels.HAT, ObjArmorModels.HAT_TEXTURE, "Cube_Cube.001");
    private static final Spec NO9 = headOnly(ObjArmorModels.NO9, ObjArmorModels.NO9_TEXTURE, "Helmet")
            .withHeadExtras(ExtraPart.lit("Insignia", ObjArmorModels.NO9_INSIGNIA_TEXTURE),
                    ExtraPart.untexturedGlow("Flame", "isOn", 255, 255, 204, 255));

    private static Spec standard(LegacyWavefrontModel model, ResourceLocation helmet, ResourceLocation chest,
                                 ResourceLocation arm, ResourceLocation leg) {
        return new Spec(model, helmet, chest, arm, leg, "Helmet", "Chest", "LeftArm", "RightArm",
                "LeftLeg", "RightLeg", "LeftBoot", "RightBoot", null, null, new ExtraPart[0], new ExtraPart[0]);
    }

    private static Spec headBody(LegacyWavefrontModel model, ResourceLocation helmet, ResourceLocation chest,
                                 ResourceLocation arm, ResourceLocation leg, String leftBoot, String rightBoot) {
        return new Spec(model, helmet, chest, arm, leg, "Head", "Body", "LeftArm", "RightArm",
                "LeftLeg", "RightLeg", leftBoot, rightBoot, null, null, new ExtraPart[0], new ExtraPart[0]);
    }

    private static Spec headOnly(LegacyWavefrontModel model, ResourceLocation helmet, String headPart) {
        return new Spec(model, helmet, null, null, null, headPart, "", "", "", "", "", "", "",
                null, null, new ExtraPart[0], new ExtraPart[0]);
    }

    private static final class EmptyArmorModel extends Model {
        private EmptyArmorModel() {
            super(RenderType::entityCutoutNoCull);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
                                   float red, float green, float blue, float alpha) {
        }
    }

    private static final class LegacyObjArmorItemRenderer extends BlockEntityWithoutLevelRenderer {
        private LegacyObjArmorItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
            super(dispatcher, modelSet);
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay) {
            Spec spec = specFor(stack);
            EquipmentSlot slot = slotFor(stack);
            if (slot == null || !spec.isSupportedSlot(slot)) {
                return;
            }

            poseStack.pushPose();
            applyLegacyItemBaseTransform(displayContext, poseStack);
            applyLegacyArmorInventoryTransform(stack, slot, displayContext, poseStack);
            renderLegacyItemSlot(spec, stack, slot, poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }

        private static void applyLegacyItemBaseTransform(ItemDisplayContext displayContext, PoseStack poseStack) {
            if (displayContext == ItemDisplayContext.GUI) {
                poseStack.translate(0.5D, 0.625D, 0.0D);
                poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                poseStack.scale(-0.0625F, -0.0625F, -0.0625F);
                return;
            }

            if (displayContext == ItemDisplayContext.GROUND) {
                poseStack.translate(0.5D, 0.5D, 0.5D);
                poseStack.scale(1.5F, 1.5F, 1.5F);
            } else {
                poseStack.translate(0.5D, 0.25D, 0.0D);
            }
            poseStack.scale(0.25F, 0.25F, 0.25F);
            if (displayContext != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                    && displayContext != ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            }
        }

        private static void applyLegacyArmorInventoryTransform(ItemStack stack, EquipmentSlot slot,
                ItemDisplayContext displayContext, PoseStack poseStack) {
            if (displayContext == ItemDisplayContext.GUI) {
                applyLegacyArmorSpecialInventoryTransform(stack, slot, poseStack);
                poseStack.translate(0.0D, -1.5D, 0.0D);
                poseStack.scale(3.25F, 3.25F, 3.25F);
                poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(-135.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(-20.0F));
                return;
            }

            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.scale(0.75F, 0.75F, 0.75F);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        }

        private static void applyLegacyArmorSpecialInventoryTransform(ItemStack stack, EquipmentSlot slot,
                PoseStack poseStack) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            String path = id == null ? "" : id.getPath().toLowerCase(Locale.ROOT);
            if (path.startsWith("dns_") && slot == EquipmentSlot.HEAD) {
                poseStack.translate(0.0D, -1.0D, 0.0D);
            } else if (path.startsWith("fau_") && slot == EquipmentSlot.HEAD) {
                poseStack.scale(0.875F, 0.875F, 0.875F);
                poseStack.translate(0.0D, -2.0D, 0.0D);
            } else if ((path.startsWith("taurun_") || path.startsWith("trenchmaster_"))
                    && slot == EquipmentSlot.HEAD) {
                poseStack.translate(0.0D, 1.0D, 0.0D);
            } else if ((path.startsWith("taurun_") || path.startsWith("trenchmaster_"))
                    && slot == EquipmentSlot.CHEST) {
                poseStack.translate(0.0D, 1.5D, 0.0D);
            }
        }

        private static void renderLegacyItemSlot(Spec spec, ItemStack stack, EquipmentSlot slot, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay) {
            switch (slot) {
                case HEAD -> {
                    poseStack.scale(0.3125F, 0.3125F, 0.3125F);
                    poseStack.translate(0.0D, 1.0D, 0.0D);
                    renderParts(spec, spec.headTexture(), poseStack, buffer, packedLight, packedOverlay, spec.headParts());
                    renderItemExtras(spec, stack, poseStack, buffer, packedLight, packedOverlay, spec.headExtras());
                }
                case CHEST -> {
                    poseStack.scale(0.225F, 0.225F, 0.225F);
                    poseStack.translate(0.0D, -10.0D, 0.0D);
                    renderParts(spec, spec.chestTexture(), poseStack, buffer, packedLight, packedOverlay, spec.chestParts());
                    poseStack.translate(0.0D, 0.0D, 0.1D);
                    renderParts(spec, spec.armTexture(), poseStack, buffer, packedLight, packedOverlay, spec.leftArmParts());
                    renderParts(spec, spec.armTexture(), poseStack, buffer, packedLight, packedOverlay, spec.rightArmParts());
                    if (spec.jetpackTexture() != null && shouldRenderJetpack(spec, stack)) {
                        renderParts(spec, spec.jetpackTexture(), poseStack, buffer, packedLight, packedOverlay, "Jetpack");
                    }
                    if (spec.cassetteTexture() != null) {
                        renderTranslucentParts(spec, spec.cassetteTexture(), poseStack, buffer, packedLight,
                                packedOverlay, "Cassette");
                    }
                    renderItemExtras(spec, stack, poseStack, buffer, packedLight, packedOverlay, spec.chestExtras());
                }
                case LEGS -> {
                    poseStack.scale(0.25F, 0.25F, 0.25F);
                    poseStack.translate(0.0D, -20.0D, 0.0D);
                    renderParts(spec, spec.legTexture(), poseStack, buffer, packedLight, packedOverlay, spec.leftLegParts());
                    poseStack.translate(0.0D, 0.0D, 0.1D);
                    renderParts(spec, spec.legTexture(), poseStack, buffer, packedLight, packedOverlay, spec.rightLegParts());
                }
                case FEET -> {
                    poseStack.scale(0.25F, 0.25F, 0.25F);
                    poseStack.translate(0.0D, -22.0D, 0.0D);
                    renderParts(spec, spec.legTexture(), poseStack, buffer, packedLight, packedOverlay, spec.leftBootParts());
                    poseStack.translate(0.0D, 0.0D, 0.1D);
                    renderParts(spec, spec.legTexture(), poseStack, buffer, packedLight, packedOverlay, spec.rightBootParts());
                }
                default -> {
                }
            }
        }

        private static void renderParts(Spec spec, ResourceLocation texture, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, String... parts) {
            if (texture == null) {
                return;
            }
            for (String part : parts) {
                if (part == null || part.isBlank()) {
                    continue;
                }
                spec.model().renderPart(part, texture, poseStack, buffer, packedLight, packedOverlay);
            }
        }

        private static void renderTranslucentParts(Spec spec, ResourceLocation texture, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay, String... parts) {
            if (texture == null) {
                return;
            }
            for (String part : parts) {
                spec.model().renderPartTranslucent(part, texture, poseStack, buffer, packedLight, packedOverlay,
                        255, 255, 255, 255);
            }
        }

        private static void renderItemExtras(Spec spec, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                int packedLight, int packedOverlay, ExtraPart... extras) {
            renderItemExtrasPass(spec, stack, poseStack, buffer, packedLight, packedOverlay, false, extras);
            renderItemExtrasPass(spec, stack, poseStack, buffer, packedLight, packedOverlay, true, extras);
        }

        private static void renderItemExtrasPass(Spec spec, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                int packedLight, int packedOverlay, boolean translucentPass, ExtraPart... extras) {
            for (ExtraPart extra : extras) {
                if (!extra.shouldRender(stack) || extra.usesTranslucentRenderType() != translucentPass) {
                    continue;
                }
                renderExtra(spec, extra, poseStack, buffer, packedLight, packedOverlay);
            }
        }
    }

    private static final class ItemRendererHolder {
        private static final LegacyObjArmorItemRenderer INSTANCE = new LegacyObjArmorItemRenderer(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels());

        private ItemRendererHolder() {
        }
    }

    private record Spec(LegacyWavefrontModel model, ResourceLocation headTexture, ResourceLocation chestTexture,
                        ResourceLocation armTexture, ResourceLocation legTexture, String headPart, String chestPart,
                        String leftArmPart, String rightArmPart, String leftLegPart, String rightLegPart,
                        String leftBootPart, String rightBootPart, ResourceLocation jetpackTexture,
                        ResourceLocation cassetteTexture, ExtraPart[] headExtras, ExtraPart[] chestExtras) {
        private static final Spec EMPTY = new Spec(null, null, null, null, null, "", "", "", "", "", "", "", "",
                null, null, new ExtraPart[0], new ExtraPart[0]);

        private boolean isSupportedSlot(EquipmentSlot slot) {
            return model != null && switch (slot) {
                case HEAD -> headTexture != null;
                case CHEST -> chestTexture != null && armTexture != null;
                case LEGS, FEET -> legTexture != null;
                default -> false;
            };
        }

        private String[] headParts() {
            return new String[] {headPart};
        }

        private String[] chestParts() {
            return new String[] {chestPart};
        }

        private String[] leftArmParts() {
            return new String[] {leftArmPart};
        }

        private String[] rightArmParts() {
            return new String[] {rightArmPart};
        }

        private String[] leftLegParts() {
            return new String[] {leftLegPart};
        }

        private String[] rightLegParts() {
            return new String[] {rightLegPart};
        }

        private String[] leftBootParts() {
            return new String[] {leftBootPart};
        }

        private String[] rightBootParts() {
            return new String[] {rightBootPart};
        }

        private Spec withHeadExtras(ExtraPart... extras) {
            return new Spec(model, headTexture, chestTexture, armTexture, legTexture, headPart, chestPart,
                    leftArmPart, rightArmPart, leftLegPart, rightLegPart, leftBootPart, rightBootPart,
                    jetpackTexture, cassetteTexture, extras, chestExtras);
        }

        private Spec withChestExtras(ExtraPart... extras) {
            return new Spec(model, headTexture, chestTexture, armTexture, legTexture, headPart, chestPart,
                    leftArmPart, rightArmPart, leftLegPart, rightLegPart, leftBootPart, rightBootPart,
                    jetpackTexture, cassetteTexture, headExtras, extras);
        }
    }

    private record ExtraPart(String part, ResourceLocation texture, int red, int green, int blue, int alpha,
                             boolean translucent, boolean additive, boolean fullBright, boolean untextured,
                             String requiredBooleanTag, boolean rotatingZ, double pivotX, double pivotY, double pivotZ) {
        private static ExtraPart lit(String part, ResourceLocation texture) {
            return new ExtraPart(part, texture, 255, 255, 255, 255, false, false, false, false, null,
                    false, 0.0D, 0.0D, 0.0D);
        }

        private static ExtraPart fullBright(String part, ResourceLocation texture) {
            return new ExtraPart(part, texture, 255, 255, 255, 255, false, false, true, false, null,
                    false, 0.0D, 0.0D, 0.0D);
        }

        private static ExtraPart glow(String part, ResourceLocation texture) {
            return glow(part, texture, 255, 255, 255, 255);
        }

        private static ExtraPart glow(String part, ResourceLocation texture, int red, int green, int blue, int alpha) {
            return new ExtraPart(part, texture, red, green, blue, alpha, false, true, true, false, null,
                    false, 0.0D, 0.0D, 0.0D);
        }

        private static ExtraPart rotating(String part, ResourceLocation texture, double pivotX, double pivotY, double pivotZ) {
            return new ExtraPart(part, texture, 255, 255, 255, 255, false, false, false, false, null,
                    true, pivotX, pivotY, pivotZ);
        }

        private static ExtraPart untexturedLit(String part, int red, int green, int blue, int alpha) {
            return new ExtraPart(part, null, red, green, blue, alpha, false, false, true, true, null,
                    false, 0.0D, 0.0D, 0.0D);
        }

        private static ExtraPart untexturedGlow(String part, String requiredBooleanTag, int red, int green, int blue, int alpha) {
            return new ExtraPart(part, null, red, green, blue, alpha, false, true, true, true, requiredBooleanTag,
                    false, 0.0D, 0.0D, 0.0D);
        }

        private boolean shouldRender(ItemStack stack) {
            return requiredBooleanTag == null || stack.hasTag() && stack.getTag().getBoolean(requiredBooleanTag);
        }

        private boolean usesTranslucentRenderType() {
            return translucent || additive;
        }

        private double rotationDegrees() {
            return -(System.currentTimeMillis() / 2.0D) % 360.0D;
        }
    }

    public record PartVisibilityState(HumanoidModel<?> model, boolean head, boolean hat, boolean body,
                                      boolean rightArm, boolean leftArm, boolean rightLeg, boolean leftLeg) {
        private static final PartVisibilityState NONE = new PartVisibilityState(null, true, true, true,
                true, true, true, true);

        private static PartVisibilityState capture(HumanoidModel<?> model) {
            return new PartVisibilityState(model, visible(model.head), visible(model.hat), visible(model.body),
                    visible(model.rightArm), visible(model.leftArm), visible(model.rightLeg), visible(model.leftLeg));
        }

        private void restore() {
            if (model == null) {
                return;
            }
            model.head.visible = head;
            model.hat.visible = hat;
            model.body.visible = body;
            model.rightArm.visible = rightArm;
            model.leftArm.visible = leftArm;
            model.rightLeg.visible = rightLeg;
            model.leftLeg.visible = leftLeg;
        }

        private static boolean visible(ModelPart part) {
            return part == null || part.visible;
        }
    }

    private LegacyObjArmorRenderer() {
    }
}
