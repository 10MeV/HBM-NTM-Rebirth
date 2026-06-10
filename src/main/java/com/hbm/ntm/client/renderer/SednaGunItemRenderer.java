package com.hbm.ntm.client.renderer;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.item.SednaGunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SednaGunItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final double LEGACY_GUI_SLOT_PIXELS = 16.0D;
    private static final double LEGACY_GUI_UNIT = 1.0D / LEGACY_GUI_SLOT_PIXELS;
    private static final double FIRST_PERSON_SCREEN_UNIT = 0.25D;

    private static final Map<String, RenderSpec> SPECS = Map.ofEntries(
            specOnly("gun_pepperbox", "pepperbox", "pepperbox",
                    inv(1.5D, 0.5D, 0.5D, 0.0D), fp(0.25D, 1.5D, -1.0D, -0.6D, 0.8D),
                    "Grip", "Cylinder", "Hammer", "Trigger"),
            spec("gun_light_revolver", "bio_revolver", "bio_revolver",
                    inv(1.125D, -0.5D, 1.5D, 0.0D), fp(0.125D, 0.875D, -0.8D, -0.6D, 0.8D)),
            spec("gun_light_revolver_atlas", "bio_revolver", "bio_revolver_atlas",
                    inv(1.125D, -0.5D, 1.5D, 0.0D), fp(0.125D, 0.875D, -0.8D, -0.6D, 0.8D)),
            spec("gun_henry", "henry", "henry",
                    inv(1.5D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.4D)),
            spec("gun_henry_lincoln", "henry", "henry_lincoln",
                    inv(1.5D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.4D)),
            specYaw("gun_heavy_revolver", "lilmac", "heavy_revolver", 90.0D,
                    inv(1.25D, 0.0D, 0.0D, 0.0D), fp(0.125D, 1.0D, -0.8D, -0.6D, 0.8D)),
            specYaw("gun_heavy_revolver_lilmac", "lilmac", "lilmac", 90.0D,
                    inv(1.25D, 0.0D, 0.0D, 0.0D), fp(0.125D, 1.0D, -0.8D, -0.6D, 0.8D)),
            specYaw("gun_heavy_revolver_protege", "lilmac", "protege", 90.0D,
                    inv(1.25D, 0.0D, 0.0D, 0.0D), fp(0.125D, 1.0D, -0.8D, -0.6D, 0.8D)),
            spec("gun_greasegun", "greasegun", "greasegun",
                    inv(1.5D, -0.5D, 2.0D, 0.0D), fp(0.375D, 0.875D, -1.5D * 0.8D, -1.0D * 0.8D, 1.75D * 0.8D)),
            spec("gun_maresleg", "maresleg", "maresleg",
                    inv(1.4375D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.6D)),
            spec("gun_maresleg_broken", "maresleg", "maresleg_broken",
                    inv(1.4375D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.6D)),
            spec("gun_flaregun", "flaregun", "flaregun",
                    inv(1.0D, -0.5D, 0.0D, 0.0D), fp(0.125D, 0.875D, -1.0D, -1.2D, 1.6D)),
            spec("gun_carbine", "carbine", "huntsman",
                    inv(1.375D, -0.5D, 0.0D, 0.0D), fp(0.5D, 0.875D, -1.2D, -1.2D, 0.7D)),
            spec("gun_am180", "am180", "am180",
                    inv(0.75D, 1.5D, 0.0D, 0.0D), fp(0.1875D, 0.875D, -0.8D, -0.8D, 0.8D)),
            spec("gun_liberator", "liberator", "liberator",
                    inv(1.5D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.2D, -1.0D, 1.0D)),
            spec("gun_congolake", "congolake", "congolake",
                    inv(2.5D, 0.0D, -1.25D, 0.0D), fp(0.5D, 0.875D, -1.2D, -1.6D, 1.0D)),
            specYaw("gun_lag", "mike_hawk", "lag", 90.0D,
                    inv(1.5D, 2.5D, 1.0D, 0.0D), fp(0.25D, 0.875D, -1.2D, -0.8D, 1.2D)),
            spec("gun_uzi", "uzi", "uzi",
                    inv(1.5D, 0.0D, 1.0D, 0.0D), fp(0.25D, 0.875D, -1.4D, -1.2D, 2.0D)),
            specYaw("gun_spas12", "spas-12", "spas-12", 180.0D,
                    inv(2.0D, 4.25D, -0.5D, 0.0D), fp(0.5D, 0.875D, -1.0D, -1.4D, -0.4D)),
            spec("gun_star_f", "star_f", "star_f",
                    inv(1.5D, -1.0D, -0.5D, 0.0D), fp(0.25D, 0.875D, -1.4D, -1.4D, 2.0D)),
            spec("gun_g3", "g3", "g3",
                    inv(0.875D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 2.2D)),
            spec("gun_g3_zebra", "g3", "g3_zebra",
                    inv(0.875D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 2.2D)),
            spec("gun_mk108", "mk108", "mk108",
                    inv(1.375D, 0.0D, 0.5D, 0.25D), fp(0.375D, 0.875D, -0.8D, -1.2D, 2.0D)),
            spec("gun_amat", "amat", "amat",
                    inv(0.9375D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -0.8D, -0.8D, 2.6D)),
            spec("gun_amat_subtlety", "amat", "amat_subtlety",
                    inv(0.9375D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -0.8D, -0.8D, 2.6D)),
            spec("gun_amat_penance", "amat", "amat_penance",
                    inv(0.9375D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -0.8D, -0.8D, 2.6D)),
            spec("gun_autoshotgun", "shredder", "shredder",
                    inv(1.25D, -1.5D, 0.0D, 0.0D), fp(0.25D, 0.875D, -1.2D, -1.0D, 1.2D)),
            spec("gun_autoshotgun_sexy", "sexy", "sexy_real_no_fake",
                    inv(1.375D, 0.0D, 0.5D, 0.25D), fp(0.375D, 0.875D, -0.8D, -0.6D, 2.4D)),
            spec("gun_autoshotgun_heretic", "sexy", "sexy_heretic",
                    inv(1.375D, 0.0D, 0.5D, 0.25D), fp(0.375D, 0.875D, -0.8D, -0.6D, 2.4D)),
            spec("gun_stg77", "stg77", "stg77",
                    inv(1.375D, -0.5D, 0.5D, 0.0D), fp(0.5D, 0.875D, -1.2D, -0.8D, 2.0D)),
            spec("gun_hangman", "hangman", "hangman",
                    inv(0.375D, -0.5D, 2.5D, 0.0D), fp(0.125D, 0.875D, -1.2D, -0.7D, 1.4D)),
            spec("gun_mas36", "mas36", "mas36",
                    inv(1.5D, -0.5D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.2D, -1.0D, 1.4D)),
            specYaw("gun_bolter", "bolter", "bolter", 180.0D,
                    inv(2.75D, -0.25D, -0.5D, 0.0D), fp(0.5D, 0.875D, -1.2D, -1.6D, 2.0D)),
            spec("gun_double_barrel", "sacred_dragon", "double_barrel",
                    inv(1.375D, 0.0D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.6D)),
            spec("gun_double_barrel_sacred_dragon", "sacred_dragon", "double_barrel_sacred_dragon",
                    inv(1.375D, 0.0D, 0.5D, 0.0D), fp(0.375D, 0.875D, -1.0D, -0.8D, 1.6D)));

    private static final Map<RenderSpec, LegacyWavefrontModel> MODELS = new ConcurrentHashMap<>();

    public static final SednaGunItemRenderer INSTANCE = new SednaGunItemRenderer(
            Minecraft.getInstance().getBlockEntityRenderDispatcher(),
            Minecraft.getInstance().getEntityModels());

    private SednaGunItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!(stack.getItem() instanceof SednaGunItem gunItem)) {
            return;
        }
        RenderSpec spec = SPECS.get(gunItem.gunConfig().legacyName());
        if (spec == null) {
            return;
        }

        LegacyWavefrontModel model = MODELS.computeIfAbsent(spec,
                key -> new LegacyWavefrontModel(key.modelLocation(), key.textureLocation()).asVBO());
        String[] visibleParts = spec.visibleParts().toArray(String[]::new);
        AABB modelBounds = visibleParts.length == 0 ? model.boundsAll() : model.boundsOnly(visibleParts);
        AABB bounds = displayBounds(displayContext, modelBounds, spec);
        if (bounds.getXsize() <= 0.0D || bounds.getYsize() <= 0.0D || bounds.getZsize() <= 0.0D) {
            return;
        }

        poseStack.pushPose();
        applyDisplay(displayContext, poseStack, bounds, spec);
        if (visibleParts.length == 0) {
            model.renderAll(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay);
        } else {
            model.renderOnly(spec.textureLocation(), poseStack, buffer, packedLight, packedOverlay, visibleParts);
        }
        poseStack.popPose();
    }

    private static AABB displayBounds(ItemDisplayContext displayContext, AABB bounds, RenderSpec spec) {
        return bounds;
    }

    private static void applyDisplay(ItemDisplayContext displayContext, PoseStack poseStack, AABB bounds,
            RenderSpec spec) {
        Vec3 center = bounds.getCenter();
        double maxSize = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));

        if (displayContext == ItemDisplayContext.GUI) {
            applyLegacyInventoryDisplay(poseStack, spec);
            return;
        }

        if (displayContext.firstPerson()) {
            applyLegacyFirstPersonDisplay(displayContext, poseStack, spec);
            return;
        }

        poseStack.translate(0.5D, 0.5D, 0.5D);
        float fitScale = (float) (0.82D / Math.max(1.0D, maxSize));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) (180.0D + spec.modelYawDegrees())));
        poseStack.scale(fitScale, fitScale, fitScale);
        poseStack.translate(-center.x, -center.y, -center.z);

        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.scale(0.65F, 0.65F, 0.65F);
        }
    }

    private static void applyLegacyInventoryDisplay(PoseStack poseStack, RenderSpec spec) {
        InventoryPose inventory = spec.inventory();
        poseStack.translate(0.5D, 0.5D, 0.5D);
        poseStack.scale((float) LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT, (float) -LEGACY_GUI_UNIT);
        poseStack.mulPose(Axis.ZP.rotationDegrees(225.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale((float) inventory.scale(), (float) inventory.scale(), (float) inventory.scale());
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees((float) inventory.yRot()));
        poseStack.translate(inventory.x(), inventory.y(), inventory.z());
        if (spec.modelYawDegrees() != 0.0D) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) spec.modelYawDegrees()));
        }
    }

    private static void applyLegacyFirstPersonDisplay(ItemDisplayContext displayContext, PoseStack poseStack,
            RenderSpec spec) {
        FirstPersonPose firstPerson = spec.firstPerson();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if (leftHand) {
            poseStack.scale(-1.0F, 1.0F, 1.0F);
        }
        poseStack.scale((float) FIRST_PERSON_SCREEN_UNIT, (float) FIRST_PERSON_SCREEN_UNIT,
                (float) FIRST_PERSON_SCREEN_UNIT);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, 0.0D, firstPerson.setupZ());
        poseStack.translate(firstPerson.aimX(), firstPerson.aimY(), firstPerson.aimZ());
        poseStack.scale((float) firstPerson.renderScale(), (float) firstPerson.renderScale(),
                (float) firstPerson.renderScale());
        if (spec.modelYawDegrees() != 0.0D) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) spec.modelYawDegrees()));
        }
    }

    private static Map.Entry<String, RenderSpec> spec(String legacyName, String modelName, String textureName,
            InventoryPose inventory, FirstPersonPose firstPerson) {
        return specYaw(legacyName, modelName, textureName, 0.0D, inventory, firstPerson);
    }

    private static Map.Entry<String, RenderSpec> specYaw(String legacyName, String modelName, String textureName,
            double modelYawDegrees, InventoryPose inventory, FirstPersonPose firstPerson) {
        return specOnly(legacyName, modelName, textureName, modelYawDegrees, inventory, firstPerson);
    }

    private static Map.Entry<String, RenderSpec> specOnly(String legacyName, String modelName, String textureName,
            InventoryPose inventory, FirstPersonPose firstPerson, String... visibleParts) {
        return specOnly(legacyName, modelName, textureName, 0.0D, inventory, firstPerson, visibleParts);
    }

    private static Map.Entry<String, RenderSpec> specOnly(String legacyName, String modelName, String textureName,
            double modelYawDegrees, InventoryPose inventory, FirstPersonPose firstPerson, String... visibleParts) {
        return Map.entry(legacyName, new RenderSpec(
                new ResourceLocation(HbmNtm.MOD_ID, "models/weapons/" + modelName + ".obj"),
                new ResourceLocation(HbmNtm.MOD_ID, "textures/models/weapons/" + textureName + ".png"),
                modelYawDegrees,
                inventory,
                firstPerson,
                List.of(visibleParts)));
    }

    private static InventoryPose inv(double scale, double x, double y, double z) {
        return new InventoryPose(scale, 45.0D, x, y, z);
    }

    private static FirstPersonPose fp(double renderScale, double setupZ, double aimX, double aimY, double aimZ) {
        return new FirstPersonPose(renderScale, setupZ, aimX, aimY, aimZ);
    }

    private record RenderSpec(ResourceLocation modelLocation, ResourceLocation textureLocation, double modelYawDegrees,
            InventoryPose inventory, FirstPersonPose firstPerson, List<String> visibleParts) {
    }

    private record InventoryPose(double scale, double yRot, double x, double y, double z) {
    }

    private record FirstPersonPose(double renderScale, double setupZ, double aimX, double aimY, double aimZ) {
    }

}
