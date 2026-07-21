package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.armor.ArmorModHandler;
import com.hbm.ntm.armor.ArmorModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class LegacyJetpackRenderer {
    private static final ResourceLocation JETPACK_RED_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/jetpack_red.png");
    private static final ResourceLocation JETPACK_BLUE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/jetpack_blue.png");
    private static final ResourceLocation JETPACK_GREEN_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/jetpack_green.png");
    private static final ResourceLocation JETPACK_BOOST_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/jetpack.png");
    private static final ModelPart JETPACK = jetpackLayer().bakeRoot().getChild("jetpack");

    public static void renderEquippedJetpack(Player player, HumanoidModel<?> humanoid, PoseStack poseStack,
                                             MultiBufferSource buffer, int packedLight) {
        ArmorModItems.Jetpack jetpack = directOrInstalledJetpack(player);
        if (jetpack == null) {
            return;
        }

        poseStack.pushPose();
        humanoid.body.translateAndRotate(poseStack);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(textureFor(jetpack)));
        JETPACK.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static ArmorModItems.Jetpack directOrInstalledJetpack(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ArmorModItems.Jetpack jetpack) {
            return jetpack;
        }
        if (!ArmorModHandler.hasMods(chest)) {
            return null;
        }
        ItemStack mod = ArmorModHandler.pryMod(chest, ArmorModHandler.plate_only);
        return mod.getItem() instanceof ArmorModItems.Jetpack jetpack ? jetpack : null;
    }

    private static ResourceLocation textureFor(ArmorModItems.Jetpack jetpack) {
        return switch (jetpack.type()) {
            case REGULAR -> JETPACK_RED_TEXTURE;
            case HOVER -> JETPACK_BLUE_TEXTURE;
            case VECTORED -> JETPACK_GREEN_TEXTURE;
            case BOOST -> JETPACK_BOOST_TEXTURE;
        };
    }

    private static LayerDefinition jetpackLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition jetpack = mesh.getRoot().addOrReplaceChild("jetpack", CubeListBuilder.create(), PartPose.ZERO);
        cube(jetpack, "pack", 12, 10, 0.0F, 0.0F, 0.0F, 4.0F, 6.0F, 1.0F,
                -2.0F, 3.0F, 2.0F);
        cube(jetpack, "tank_1", 0, 0, 0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 3.0F,
                0.5F, 2.0F, 2.5F);
        cube(jetpack, "tank_2", 0, 11, 0.0F, 0.0F, 0.0F, 3.0F, 8.0F, 3.0F,
                -3.5F, 2.0F, 2.5F);
        cube(jetpack, "tip_1", 0, 22, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 2.0F,
                1.0F, 1.0F, 3.0F);
        cube(jetpack, "tip_2", 0, 25, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 2.0F,
                -3.0F, 1.0F, 3.0F);
        cube(jetpack, "duct_1", 8, 22, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 2.0F,
                1.0F, 9.5F, 3.0F);
        cube(jetpack, "duct_2", 8, 25, 0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 2.0F,
                -3.0F, 9.5F, 3.0F);
        cube(jetpack, "thruster_1", 12, 0, 0.0F, 0.0F, 0.0F, 3.0F, 2.0F, 3.0F,
                0.5F, 10.5F, 2.5F);
        cube(jetpack, "thruster_2", 12, 5, 0.0F, 0.0F, 0.0F, 3.0F, 2.0F, 3.0F,
                -3.5F, 10.5F, 2.5F);
        return LayerDefinition.create(mesh, 32, 32);
    }

    private static void cube(PartDefinition parent, String name, int u, int v, float x, float y, float z,
                             float dx, float dy, float dz, float pivotX, float pivotY, float pivotZ) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).mirror()
                        .addBox(x, y, z, dx, dy, dz),
                PartPose.offset(pivotX, pivotY, pivotZ));
    }

    private LegacyJetpackRenderer() {
    }
}
