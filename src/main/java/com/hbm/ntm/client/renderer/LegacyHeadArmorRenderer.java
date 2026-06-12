package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.armor.ArmorModGasMaskItem;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.radiation.ArmorUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public final class LegacyHeadArmorRenderer {
    private static final Model EMPTY_ARMOR_MODEL = new EmptyArmorModel();

    private static final ModelPart GAS_MASK = gasMaskLayer().bakeRoot().getChild("mask");
    private static final ModelPart M65_ROOT = m65Layer().bakeRoot();

    private static final ResourceLocation GAS_MASK_TEXTURE = texture("models/gasmask");
    private static final ResourceLocation M65_TEXTURE = texture("models/model_m65");
    private static final ResourceLocation M65_MONO_TEXTURE = texture("models/model_m65_mono");
    private static final ResourceLocation HAZMAT_RED_TEXTURE = texture("models/model_haz_red");
    private static final ResourceLocation HAZMAT_GREY_TEXTURE = texture("models/model_haz_grey");
    private static final ResourceLocation MASK_OLDE_TEXTURE = texture("models/armor/mask_olde");
    private static final ResourceLocation LIQUIDATOR_TEXTURE = texture("models/armor/liquidator_helmet");

    public static void acceptExtensions(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack,
                                                       EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return equipmentSlot == EquipmentSlot.HEAD && specFor(itemStack).hideVanillaArmor()
                        ? EMPTY_ARMOR_MODEL
                        : original;
            }
        });
    }

    public static void renderEquippedHeadArmor(LivingEntity entity, HumanoidModel<?> humanoid, PoseStack poseStack,
                                               MultiBufferSource buffer, int packedLight) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        Spec spec = specFor(head);
        if (spec.model() != HeadModel.NONE) {
            renderSpec(entity, head, humanoid, poseStack, buffer, packedLight, spec);
        }

        ItemStack attachment = installedAttachmentMask(head);
        if (attachment.isEmpty()) {
            return;
        }
        Spec attachmentSpec = specFor(attachment);
        if (attachmentSpec.model() != HeadModel.NONE) {
            renderSpec(entity, attachment, humanoid, poseStack, buffer, packedLight, attachmentSpec);
        }
    }

    private static void renderSpec(LivingEntity entity, ItemStack stack, HumanoidModel<?> humanoid, PoseStack poseStack,
                                   MultiBufferSource buffer, int packedLight, Spec spec) {
        poseStack.pushPose();
        humanoid.head.translateAndRotate(poseStack);
        switch (spec.model()) {
            case GAS_MASK -> renderGasMask(poseStack, buffer, packedLight, spec.texture());
            case M65 -> renderM65(entity, stack, poseStack, buffer, packedLight, spec.texture());
            case NONE -> {
            }
        }
        poseStack.popPose();
    }

    private static void renderGasMask(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                      ResourceLocation texture) {
        poseStack.pushPose();
        poseStack.scale(1.15F, 1.15F, 1.15F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        GAS_MASK.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderM65(LivingEntity entity, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer,
                                  int packedLight, ResourceLocation texture) {
        poseStack.pushPose();
        float scale = 18.0F / 16.0F;
        poseStack.scale(scale, scale, scale);
        poseStack.scale(1.01F, 1.01F, 1.01F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        M65_ROOT.getChild("mask").render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        if (ArmorUtil.hasGasMaskFilterRecursively(stack, entity)) {
            M65_ROOT.getChild("filter").render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    private static ItemStack installedAttachmentMask(ItemStack head) {
        if (!ArmorModHandler.hasMods(head)) {
            return ItemStack.EMPTY;
        }
        ItemStack mod = ArmorModHandler.pryMod(head, ArmorModHandler.helmet_only);
        return mod.getItem() instanceof ArmorModGasMaskItem ? mod : ItemStack.EMPTY;
    }

    private static Spec specFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return Spec.EMPTY;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = id == null ? "" : id.getPath().toLowerCase(Locale.ROOT);
        return switch (path) {
            case "gas_mask" -> new Spec(HeadModel.GAS_MASK, GAS_MASK_TEXTURE, true);
            case "gas_mask_m65", "attachment_mask" -> new Spec(HeadModel.M65, M65_TEXTURE, true);
            case "gas_mask_mono", "attachment_mask_mono" -> new Spec(HeadModel.M65, M65_MONO_TEXTURE, true);
            case "gas_mask_olde" -> new Spec(HeadModel.M65, MASK_OLDE_TEXTURE, true);
            case "hazmat_helmet_red" -> new Spec(HeadModel.M65, HAZMAT_RED_TEXTURE, true);
            case "hazmat_helmet_grey" -> new Spec(HeadModel.M65, HAZMAT_GREY_TEXTURE, true);
            case "liquidator_helmet" -> new Spec(HeadModel.M65, LIQUIDATOR_TEXTURE, true);
            default -> Spec.EMPTY;
        };
    }

    private static LayerDefinition gasMaskLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition mask = mesh.getRoot().addOrReplaceChild("mask", CubeListBuilder.create(), PartPose.ZERO);
        cube(mask, "face", 0, 0, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 3.0F,
                -4.0F, -7.9625F, -4.0F, 0.0F, 0.0F, 0.0F);
        cube(mask, "left_eye", 22, 0, 0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 1.0F,
                -3.0F, -4.9625F, -4.5333333F, 0.0F, 0.0F, 0.0F);
        cube(mask, "right_eye", 22, 0, 0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 1.0F,
                1.0F, -4.9625F, -4.5F, 0.0F, 0.0F, 0.0F);
        cube(mask, "nose", 0, 11, 0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F,
                -1.0F, -2.9625F, -4.0F, -0.7853982F, 0.0F, 0.0F);
        cube(mask, "filter", 0, 15, 0.0F, 2.0F, -0.5F, 3.0F, 4.0F, 3.0F,
                -1.5F, -2.9625F, -4.0F, -0.7853982F, 0.0F, 0.0F);
        cube(mask, "strap", 0, 22, 0.0F, 0.0F, 0.0F, 8.0F, 1.0F, 5.0F,
                -4.0F, -4.9625F, -1.0F, 0.0F, 0.0F, 0.0F);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static LayerDefinition m65Layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition mask = root.addOrReplaceChild("mask", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition filter = root.addOrReplaceChild("filter", CubeListBuilder.create(), PartPose.ZERO);
        float y = 0.5F;
        cube(mask, "head", 0, 0, 0.0F, 0.0F, 0.0F, 8.0F, 8.0F, 8.0F,
                -4.0F, -8.0F + y, -4.0F, 0.0F, 0.0F, 0.0F);
        cube(mask, "nose", 0, 16, 0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 1.0F,
                -1.5F, -3.5F + y, -5.0F, 0.0F, 0.0F, 0.0F);
        cube(mask, "outlet", 0, 20, 0.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F,
                -1.0F, -3.5F + y, -5.0F, -0.4799655F, 0.0F, 0.0F);
        cube(mask, "nose_slope", 8, 16, 0.0F, 0.0F, -2.0F, 3.0F, 2.0F, 2.0F,
                -1.5F, -2.0F + y, -4.0F, 0.6108652F, 0.0F, 0.0F);
        cube(mask, "left_eye", 0, 23, 0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 0.0F,
                -3.5F, -6.0F + y, -4.2F, 0.0F, 0.0F, 0.0F);
        cube(mask, "right_eye", 0, 26, 0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 0.0F,
                0.5F, -6.0F + y, -4.2F, 0.0F, 0.0F, 0.0F);
        cube(mask, "front", 6, 20, 0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 1.0F,
                -1.0F, -3.2F + y, -6.0F, 0.0F, 0.0F, 0.0F);
        cube(filter, "connector", 6, 23, 0.0F, 0.0F, -3.0F, 2.0F, 2.0F, 1.0F,
                -1.0F, -2.0F + y, -4.0F, 0.6108652F, 0.0F, 0.0F);
        cube(filter, "filter_tall", 18, 21, 0.0F, -1.0F, -5.0F, 3.0F, 4.0F, 2.0F,
                -1.5F, -2.0F + y, -4.0F, 0.6108652F, 0.0F, 0.0F);
        cube(filter, "filter_wide", 18, 16, 0.0F, -0.5F, -5.0F, 4.0F, 3.0F, 2.0F,
                -2.0F, -2.0F + y, -4.0F, 0.6108652F, 0.0F, 0.0F);
        return LayerDefinition.create(mesh, 32, 32);
    }

    private static void cube(PartDefinition parent, String name, int u, int v, float x, float y, float z,
                             float dx, float dy, float dz, float pivotX, float pivotY, float pivotZ,
                             float rotX, float rotY, float rotZ) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, dx, dy, dz),
                PartPose.offsetAndRotation(pivotX, pivotY, pivotZ, rotX, rotY, rotZ));
    }

    private static ResourceLocation texture(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/" + path + ".png");
    }

    private LegacyHeadArmorRenderer() {
    }

    private enum HeadModel {
        NONE,
        GAS_MASK,
        M65
    }

    private record Spec(HeadModel model, ResourceLocation texture, boolean hideVanillaArmor) {
        private static final Spec EMPTY = new Spec(HeadModel.NONE, GAS_MASK_TEXTURE, false);
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
}
