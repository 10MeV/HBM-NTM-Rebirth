package com.hbm.ntm.client.renderer;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bullet.BulletStyle;
import com.hbm.ntm.bullet.BulletTauTrailUtil;
import com.hbm.ntm.bullet.BulletTrail;
import com.hbm.ntm.client.obj.LegacySparkRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Random;

public class BulletProjectileRenderer extends EntityRenderer<BulletProjectileEntity> {
    private static final ResourceLocation PROJECTILES_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/projectiles/projectiles.obj");
    private static final ResourceLocation LEADBURSTER_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/projectiles/leadburster.obj");
    private static final ResourceLocation METEOR_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/weapons/meteor.obj");
    private static final ResourceLocation BULLET_RIFLE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/projectiles/bullet_rifle.png");
    private static final ResourceLocation TAU_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/tau.png");
    private static final ResourceLocation EMPLACER_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/emplacer.png");
    private static final ResourceLocation BULLET_PISTOL =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/projectiles/bullet_pistol.png");
    private static final ResourceLocation BUCKSHOT =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/projectiles/pellet_buckshot.png");
    private static final ResourceLocation FLECHETTE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/projectiles/flechette.png");
    private static final ResourceLocation GRENADE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/projectiles/grenade.png");
    private static final ResourceLocation ROCKET =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/projectiles/rocket.png");
    private static final ResourceLocation LEADBURSTER_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/projectiles/leadburster.png");
    private static final ResourceLocation TOM_FLAME =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/weapons/tom_flame.png");
    private static final ResourceLocation METEOR_MOLTEN =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/block_meteor_molten.png");
    private static final ResourceLocation OBSIDIAN =
            new ResourceLocation("minecraft", "textures/block/obsidian.png");
    private static final ResourceLocation BLADE_TITANIUM =
            new ResourceLocation(HbmNtm.MOD_ID, "blade_titanium");
    private static final LegacyWavefrontModel PROJECTILES =
            new LegacyWavefrontModel(PROJECTILES_MODEL, BULLET_RIFLE).asVBO();
    private static final LegacyWavefrontModel LEADBURSTER =
            new LegacyWavefrontModel(LEADBURSTER_MODEL, LEADBURSTER_TEXTURE).asVBO();
    private static final LegacyWavefrontModel METEOR =
            new LegacyWavefrontModel(METEOR_MODEL, METEOR_MOLTEN).asVBO();
    private final ModelPart bulletCube;

    public BulletProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.bulletCube = createBulletLayer().bakeRoot().getChild("bullet");
        this.shadowRadius = 0.0F;
    }

    public static LayerDefinition createBulletLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bullet", CubeListBuilder.create()
                .texOffs(0, 0)
                .addBox(0.0F, 0.0F, 0.0F, 2.0F, 1.0F, 1.0F),
                PartPose.offset(1.0F, -0.5F, -0.5F));
        return LayerDefinition.create(mesh, 8, 4);
    }

    @Override
    public void render(BulletProjectileEntity entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        BulletStyle style = BulletStyle.fromLegacyId(entity.styleId());
        if (style == BulletStyle.NONE) {
            return;
        }
        if (style == BulletStyle.TAU) {
            renderTau(entity, Byte.toUnsignedInt(entity.trailId()), partialTick, poseStack, buffer);
            super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 180.0F));
        poseStack.scale(1.5F, 1.5F, 1.5F);
        if (style != BulletStyle.BLADE) {
            poseStack.mulPose(Axis.XP.rotationDegrees(new Random(entity.getId()).nextInt(90) - 45.0F));
        }

        int trail = Byte.toUnsignedInt(entity.trailId());
        switch (style) {
            case NORMAL, FOLLY -> renderBullet(trail, poseStack, buffer, packedLight, bulletCube);
            case PISTOL -> renderProjectilePart("BulletPistol", BULLET_PISTOL, 0.5F, poseStack, buffer, packedLight);
            case FLECHETTE -> renderProjectilePart("Flechette", FLECHETTE, 0.5F, poseStack, buffer, packedLight);
            case PELLET -> renderProjectilePart("Buckshot", BUCKSHOT, 0.5F, poseStack, buffer, packedLight);
            case BOLT -> renderBolt(BulletTrail.fromLegacyId(trail), entity.getId(), poseStack, buffer);
            case ROCKET -> renderProjectilePart("Rocket", ROCKET, 0.5F, poseStack, buffer, packedLight);
            case GRENADE -> renderProjectilePart("Grenade", GRENADE, 0.25F, poseStack, buffer, packedLight);
            case ORB -> renderOrb(trail, entity, partialTick, poseStack, buffer);
            case METEOR -> renderMeteor(trail, poseStack, buffer);
            case APDS -> renderProjectilePart("Flechette", FLECHETTE, 2.0F, poseStack, buffer, packedLight);
            case BLADE -> renderBlade(entity, partialTick, poseStack, buffer, packedLight);
            case LEADBURSTER -> renderLeadburster(entity, partialTick, poseStack, buffer, packedLight);
            default -> {
            }
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderBullet(int trail, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, ModelPart bulletCube) {
        if (trail == 2) {
            bulletCube.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(EMPLACER_TEXTURE)),
                    packedLight, OverlayTexture.NO_OVERLAY);
            return;
        }
        if (trail == 1) {
            bulletCube.render(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TAU_TEXTURE)),
                    packedLight, OverlayTexture.NO_OVERLAY);
            return;
        }
        renderProjectilePart("BulletRifle", BULLET_RIFLE, 0.5F, poseStack, buffer, packedLight);
    }

    private static void renderProjectilePart(String part, ResourceLocation texture, float scale,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        PROJECTILES.renderPart(part, texture, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderOrb(int trail, BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer) {
        ObjRenderContext context = new ObjRenderContext(poseStack, buffer, Blocks.AIR.defaultBlockState(),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY).withAdditiveTranslucency();
        if (trail == 0) {
            ObjEffectModels.SPHERE_UV.renderAll(TOM_FLAME, context);
            poseStack.pushPose();
            poseStack.scale(0.3F, 0.3F, 0.3F);
            ObjEffectModels.SPHERE_UV.renderAll(TOM_FLAME, context);
            poseStack.popPose();
            int timeSeed = (int) ((entity.tickCount + partialTick) * 5.0F);
            for (int i = 0; i < 5; i++) {
                LegacySparkRenderer.renderSpark(context, timeSeed + 100 * i, 0.0D, 0.0D, 0.0D,
                        0.5F, 2, 2, 0x8080FF, 0xFFFFFF);
            }
            return;
        }
        if (trail == 1) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            ObjEffectModels.SPHERE_UV.renderAllUntextured(poseStack, buffer, 128, 0, 0, 128, true);
            poseStack.scale(0.75F, 0.75F, 0.75F);
            ObjEffectModels.SPHERE_UV.renderAllUntextured(poseStack, buffer, 128, 0, 0, 128, true);
            poseStack.popPose();
            int timeSeed = (int) ((entity.tickCount + partialTick) * 5.0F);
            for (int i = 0; i < 3; i++) {
                LegacySparkRenderer.renderSpark(context, timeSeed + 100 * i, 0.0D, 0.0D, 0.0D,
                        1.0F, 2, 3, 0xFF0000, 0xFF8080);
            }
        }
    }

    private static void renderTau(BulletProjectileEntity entity, int trail, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer) {
        double renderX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double renderY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double renderZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        entity.updateTauTrailRenderPosition(renderX, renderY, renderZ);

        List<BulletTauTrailUtil.TauTrailNode> nodes = entity.tauTrailNodes();
        if (nodes.size() < 2) {
            return;
        }

        float red = 1.0F;
        float green = trail == 1 ? 1.0F : 0.5F;
        float blue = trail == 1 ? 1.0F : 0.0F;
        double scale = 0.125D;
        double timeAlpha = Math.max(2.0D - (entity.tickCount + partialTick) * 0.2D, 0.0D);
        if (timeAlpha <= 0.0D) {
            return;
        }

        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();
        for (int i = 0; i < nodes.size() - 1; i++) {
            BulletTauTrailUtil.TauTrailNode node = nodes.get(i);
            BulletTauTrailUtil.TauTrailNode past = nodes.get(i + 1);
            double nodeAlpha = node.weight() * timeAlpha;
            double pastAlpha = past.weight() * timeAlpha;
            if (nodeAlpha == 0.0D && pastAlpha == 0.0D) {
                break;
            }
            tauRibbon(consumer, pose, node.offset().x, node.offset().y, node.offset().z,
                    past.offset().x, past.offset().y, past.offset().z, scale, red, green, blue,
                    (float) nodeAlpha, (float) pastAlpha);
            tauRibbon(consumer, pose, node.offset().x, node.offset().y, node.offset().z,
                    past.offset().x, past.offset().y, past.offset().z, -scale, red, green, blue,
                    (float) nodeAlpha, (float) pastAlpha);
        }
    }

    private static void tauRibbon(VertexConsumer consumer, Matrix4f pose,
            double nodeX, double nodeY, double nodeZ, double pastX, double pastY, double pastZ,
            double yOffset, float red, float green, float blue, float nodeAlpha, float pastAlpha) {
        float outerAlpha = 0.25F;
        vertex(consumer, pose, (float) nodeX, (float) nodeY, (float) nodeZ, red, green, blue, nodeAlpha);
        vertex(consumer, pose, (float) nodeX, (float) (nodeY + yOffset), (float) nodeZ,
                red, green, blue, nodeAlpha * outerAlpha);
        vertex(consumer, pose, (float) pastX, (float) (pastY + yOffset), (float) pastZ,
                red, green, blue, pastAlpha * outerAlpha);
        vertex(consumer, pose, (float) pastX, (float) pastY, (float) pastZ, red, green, blue, pastAlpha);
    }

    private static void renderMeteor(int trail, PoseStack poseStack, MultiBufferSource buffer) {
        ResourceLocation texture = trail == 1 ? OBSIDIAN : METEOR_MOLTEN;
        METEOR.renderAll(texture, poseStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
    }

    private static void renderBlade(BulletProjectileEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ItemStack stack = new ItemStack(HbmRegistryUtil.item(BLADE_TITANIUM).orElseThrow());
        if (stack.is(Items.AIR)) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.translate(0.0D, 0.5D, 0.0D);
        poseStack.mulPose(Axis.XP.rotationDegrees((entity.tickCount + partialTick) * 18.0F));
        poseStack.translate(0.0D, -0.5D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.scale(1.0F, 2.0F, 1.0F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED,
                packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
    }

    private static void renderLeadburster(BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZN.rotationDegrees(90.0F));
        poseStack.scale(0.05F, 0.05F, 0.05F);
        LEADBURSTER.renderPart("Based", LEADBURSTER_TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        if (entity.getStuckIn() != -1) {
            poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * -18.0F));
        }
        LEADBURSTER.renderPart("Based.001", LEADBURSTER_TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        LEADBURSTER.renderPart("Backlight", LEADBURSTER_TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderBolt(BulletTrail trail, int entityId, PoseStack poseStack, MultiBufferSource buffer) {
        float red = 1.0F;
        float green = 1.0F;
        float blue = 1.0F;
        switch (trail) {
            case LASER -> {
                green = 0.0F;
                blue = 0.0F;
            }
            case NIGHTMARE -> blue = 0.0F;
            case LACUNAE -> {
                red = 0.25F;
                green = 0.0F;
                blue = 0.75F;
            }
            case WORM -> {
                red = 0.0F;
                blue = 0.0F;
            }
            case GLASS_CYAN -> red = 0.0F;
            case GLASS_BLUE -> {
                red = 0.0F;
                green = 0.0F;
            }
            default -> {
                Random random = new Random(entityId * (long) entityId);
                red = random.nextInt(2) * 0.6F;
                green = random.nextInt(2) * 0.6F;
                blue = random.nextInt(2) * 0.6F;
            }
        }

        poseStack.pushPose();
        poseStack.scale(0.25F, 0.125F, 0.125F);
        poseStack.scale(-1.0F, 1.0F, 1.0F);
        poseStack.scale(2.0F, 2.0F, 2.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();
        triangle(consumer, pose, red, green, blue, 1.0F, 6, 0, 0, 3, -1, -1, 3, 1, -1);
        triangle(consumer, pose, red, green, blue, 1.0F, 6, 0, 0, 3, 1, 1, 3, -1, 1);
        triangle(consumer, pose, red, green, blue, 1.0F, 6, 0, 0, 3, -1, 1, 3, -1, -1);
        triangle(consumer, pose, red, green, blue, 1.0F, 6, 0, 0, 3, 1, -1, 3, 1, 1);
        triangle(consumer, pose, red, green, blue, 1.0F, 6, 0, 0, 4, -0.5F, -0.5F, 4, 0.5F, -0.5F);
        triangle(consumer, pose, red, green, blue, 1.0F, 6, 0, 0, 4, 0.5F, 0.5F, 4, -0.5F, 0.5F);
        triangle(consumer, pose, red, green, blue, 1.0F, 6, 0, 0, 4, -0.5F, 0.5F, 4, -0.5F, -0.5F);
        triangle(consumer, pose, red, green, blue, 1.0F, 6, 0, 0, 4, 0.5F, -0.5F, 4, 0.5F, 0.5F);
        tailQuad(consumer, pose, red, green, blue, 4, 0.5F, -0.5F, 4, 0.5F, 0.5F, 0, 0.5F, 0.5F, 0, 0.5F, -0.5F);
        tailQuad(consumer, pose, red, green, blue, 4, -0.5F, -0.5F, 4, -0.5F, 0.5F, 0, -0.5F, 0.5F, 0, -0.5F, -0.5F);
        tailQuad(consumer, pose, red, green, blue, 4, -0.5F, 0.5F, 4, 0.5F, 0.5F, 0, 0.5F, 0.5F, 0, -0.5F, 0.5F);
        tailQuad(consumer, pose, red, green, blue, 4, -0.5F, -0.5F, 4, 0.5F, -0.5F, 0, 0.5F, -0.5F, 0, -0.5F, -0.5F);
        poseStack.popPose();
    }

    private static void triangle(VertexConsumer consumer, Matrix4f pose, float red, float green, float blue, float alpha,
            float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        vertex(consumer, pose, x1, y1, z1, red, green, blue, alpha);
        vertex(consumer, pose, x2, y2, z2, red, green, blue, 0.0F);
        vertex(consumer, pose, x3, y3, z3, red, green, blue, 0.0F);
    }

    private static void tailQuad(VertexConsumer consumer, Matrix4f pose, float red, float green, float blue,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4) {
        vertex(consumer, pose, x1, y1, z1, red, green, blue, 1.0F);
        vertex(consumer, pose, x2, y2, z2, red, green, blue, 1.0F);
        vertex(consumer, pose, x3, y3, z3, red, green, blue, 0.0F);
        vertex(consumer, pose, x4, y4, z4, red, green, blue, 0.0F);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, float x, float y, float z,
            float red, float green, float blue, float alpha) {
        consumer.vertex(pose, x, y, z).color(red, green, blue, alpha).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(BulletProjectileEntity entity) {
        return BULLET_RIFLE;
    }
}
