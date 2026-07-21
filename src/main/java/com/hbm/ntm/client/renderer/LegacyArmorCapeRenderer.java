package com.hbm.ntm.client.renderer;

import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.item.ArmorCapeItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

/**
 * The source-shaped {@code ModelCloak} path for the four registered armor cape
 * items. The old model was a vanilla 10x16x1 biped cube, not an OBJ model.
 */
public final class LegacyArmorCapeRenderer {
    private static final float LEGACY_MODEL_SCALE = 0.0625F;
    private static final Model EMPTY_ARMOR_MODEL = new EmptyArmorModel();
    private static final ModelPart CAPE = capeLayer().bakeRoot().getChild("cape");

    public static void acceptExtensions(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull Model getGenericArmorModel(LivingEntity entity, ItemStack stack,
                                                       EquipmentSlot slot, HumanoidModel<?> original) {
                return slot == EquipmentSlot.CHEST && stack.getItem() instanceof ArmorCapeItem
                        ? EMPTY_ARMOR_MODEL
                        : original;
            }
        });
    }

    public static void renderEquippedCape(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer,
                                           int packedLight) {
        if (!(entity instanceof Player player)) {
            return;
        }
        ResourceLocation texture = textureFor(player.getItemBySlot(EquipmentSlot.CHEST));
        if (texture == null) {
            return;
        }

        // ModelCloak used the ModelBiped render scale as its interpolation value.
        // Preserve that legacy contract instead of substituting modern frame partial ticks.
        float scale = LEGACY_MODEL_SCALE;
        double cloakX = player.xCloakO + (player.xCloak - player.xCloakO) * scale
                - (player.xo + (player.getX() - player.xo) * scale);
        double cloakY = player.yCloakO + (player.yCloak - player.yCloakO) * scale
                - (player.yo + (player.getY() - player.yo) * scale);
        double cloakZ = player.zCloakO + (player.zCloak - player.zCloakO) * scale
                - (player.zo + (player.getZ() - player.zo) * scale);
        float bodyYaw = player.yBodyRotO + (player.yBodyRot - player.yBodyRotO) * scale;
        double sin = Mth.sin(bodyYaw * Mth.DEG_TO_RAD);
        double negativeCos = -Mth.cos(bodyYaw * Mth.DEG_TO_RAD);
        float pitch = Mth.clamp((float) cloakY * 10.0F, -6.0F, 32.0F);
        float forward = Math.max(0.0F, (float) (cloakX * sin + cloakZ * negativeCos) * 100.0F);
        float sideways = (float) (cloakX * negativeCos - cloakZ * sin) * 100.0F;
        float bob = player.oBob + (player.bob - player.oBob) * scale;
        pitch += Mth.sin((player.walkDistO + (player.walkDist - player.walkDistO) * scale) * 6.0F) * 32.0F * bob;
        if (player.isShiftKeyDown()) {
            pitch += 25.0F;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.0F, 0.125F);
        LegacyPoseRotations.rotateXDegrees(poseStack, 6.0F + forward / 2.0F + pitch);
        LegacyPoseRotations.rotateZDegrees(poseStack, sideways / 2.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 180.0F - sideways / 2.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(texture));
        CAPE.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static ResourceLocation textureFor(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorCapeItem)) {
            return null;
        }
        ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (id == null) {
            return null;
        }
        return switch (id.getPath()) {
            case "cape_radiation" -> LegacyAccessoryRenderHelper.capeTexture("CapeRadiation");
            case "cape_gasmask" -> LegacyAccessoryRenderHelper.capeTexture("CapeGasMask");
            case "cape_schrabidium" -> LegacyAccessoryRenderHelper.capeTexture("CapeSchrabidium");
            case "cape_hidden" -> LegacyAccessoryRenderHelper.capeTexture("CapeHidden");
            default -> null;
        };
    }

    private static LayerDefinition capeLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private LegacyArmorCapeRenderer() {
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
