package com.hbm.ntm.client.renderer;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.BulletPlink;
import com.hbm.ntm.bullet.BulletProjectileTickUtil;
import com.hbm.ntm.bullet.BulletStyle;
import com.hbm.ntm.bullet.BulletTauTrailUtil;
import com.hbm.ntm.bullet.BulletTrail;
import com.hbm.ntm.bullet.LegacySednaBulletAppearance;
import com.hbm.ntm.client.obj.LegacyBeamRenderer;
import com.hbm.ntm.client.obj.LegacyBeamRenderer.WaveType;
import com.hbm.ntm.client.obj.LegacyBillboardRenderer;
import com.hbm.ntm.client.obj.LegacyBillboardRenderer.CameraBasis;
import com.hbm.ntm.client.obj.LegacySparkRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedLineRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.client.obj.ObjRenderContext;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.item.ChargeThrowerItem;
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
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Random;

public class BulletProjectileRenderer extends EntityRenderer<BulletProjectileEntity> {
    private static final ResourceLocation PROJECTILES_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/projectiles/projectiles.obj");
    private static final ResourceLocation FATMAN_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/weapons/fatman.obj");
    private static final ResourceLocation PANZERSCHRECK_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/weapons/panzerschreck.obj");
    private static final ResourceLocation CHARGE_THROWER_MODEL =
            new ResourceLocation(HbmNtm.MOD_ID, "models/weapons/charge_thrower.obj");
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
    private static final ResourceLocation ROCKET_MIRV =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/projectiles/rocket_mirv.png");
    private static final ResourceLocation FATMAN_MININUKE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/weapons/fatman_mininuke.png");
    private static final ResourceLocation FATMAN_BALEFIRE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/weapons/fatman_balefire.png");
    private static final ResourceLocation FATMAN_SUBMUNITION =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/weapons/fatman_submunition.png");
    private static final ResourceLocation PANZERSCHRECK =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/weapons/panzerschreck.png");
    private static final ResourceLocation CHARGE_THROWER_HOOK =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/weapons/charge_thrower_hook.png");
    private static final ResourceLocation CHARGE_THROWER_MORTAR =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/weapons/charge_thrower_mortar.png");
    private static final ResourceLocation BALEFIRE_GLINT =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/misc/glintbf.png");
    private static final ResourceLocation LEADBURSTER_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/projectiles/leadburster.png");
    private static final ResourceLocation TOM_FLAME =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/models/weapons/tom_flame.png");
    private static final ResourceLocation METEOR_MOLTEN =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/block_meteor_molten.png");
    private static final ResourceLocation OBSIDIAN =
            new ResourceLocation("minecraft", "textures/block/obsidian.png");
    private static final ResourceLocation FLARE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/flare.png");
    private static final ResourceLocation WIRE_GREYSCALE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/block/network/wire_greyscale.png");
    private static final ResourceLocation BLADE_TITANIUM =
            new ResourceLocation(HbmNtm.MOD_ID, "blade_titanium");
    private static final LegacyWavefrontModel PROJECTILES =
            new LegacyWavefrontModel(PROJECTILES_MODEL, BULLET_RIFLE).asVBO();
    private static final LegacyWavefrontModel FATMAN =
            new LegacyWavefrontModel(FATMAN_MODEL, FATMAN_MININUKE).asVBO();
    private static final LegacyWavefrontModel PANZERSCHRECK_MODEL_OBJ =
            new LegacyWavefrontModel(PANZERSCHRECK_MODEL, PANZERSCHRECK).asVBO();
    private static final LegacyWavefrontModel CHARGE_THROWER =
            new LegacyWavefrontModel(CHARGE_THROWER_MODEL, CHARGE_THROWER_HOOK).asVBO();
    private static final LegacyWavefrontModel LEADBURSTER =
            new LegacyWavefrontModel(LEADBURSTER_MODEL, LEADBURSTER_TEXTURE).asVBO();
    private static final LegacyWavefrontModel METEOR =
            new LegacyWavefrontModel(METEOR_MODEL, METEOR_MOLTEN).asVBO();
    private static final RenderType FLARE_RENDER_TYPE =
            LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE.renderType(FLARE_TEXTURE);
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
        BulletConfig config = entity.config();
        int trail = Byte.toUnsignedInt(entity.trailId());
        if (config != null && config.plink() == BulletPlink.ENERGY
                && (style == BulletStyle.BOLT || style == BulletStyle.TAU)) {
            renderLegacySednaBeam(style, BulletTrail.fromLegacyId(trail), entity, partialTick, poseStack, buffer);
            super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        if (style == BulletStyle.TAU) {
            renderTau(entity, Byte.toUnsignedInt(entity.trailId()), partialTick, poseStack, buffer);
            super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
            return;
        }

        if (isLegacyFlare(trail)) {
            renderLegacyFlare(trail, entity, partialTick, poseStack, buffer);
            super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        if (trail == LegacySednaBulletAppearance.CHARGE_HOOK) {
            renderLegacyChargeHook(entity, partialTick, poseStack, buffer, packedLight);
            super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
            return;
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 180.0F));
        poseStack.scale(1.5F, 1.5F, 1.5F);
        if (renderLegacySpecialProjectile(trail, entity, partialTick, poseStack, buffer, packedLight)) {
            poseStack.popPose();
            super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        if (style != BulletStyle.BLADE) {
            poseStack.mulPose(Axis.XP.rotationDegrees(new Random(entity.getId()).nextInt(90) - 45.0F));
        }

        switch (style) {
            case NORMAL, FOLLY -> renderBullet(trail, entity, poseStack, buffer, packedLight, bulletCube);
            case PISTOL -> renderProjectilePart("BulletPistol", BULLET_PISTOL, 0.5F, poseStack, buffer, packedLight);
            case FLECHETTE -> renderProjectilePart("Flechette", FLECHETTE, 0.5F, poseStack, buffer, packedLight);
            case PELLET -> renderProjectilePart("Buckshot", BUCKSHOT, 0.5F, poseStack, buffer, packedLight);
            case BOLT -> renderBolt(BulletTrail.fromLegacyId(trail), entity.getId(), poseStack, buffer);
            case ROCKET -> {
                renderProjectilePart("Rocket", ROCKET, 0.5F, poseStack, buffer, packedLight);
                if (trail == LegacySednaBulletAppearance.ROCKET_THRUST) {
                    poseStack.pushPose();
                    poseStack.translate(0.375D, 0.0D, 0.0D);
                    renderLegacySednaBullet(entity, poseStack, buffer, 0x808080, 0xFFF2A7, 2.0D,
                            0.03125D, 0.03125D * 0.25D);
                    poseStack.popPose();
                }
            }
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

    private static void renderLegacySednaBeam(BulletStyle style, BulletTrail trail, BulletProjectileEntity entity,
            float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        double length = entity.beamLength();
        if (length <= 0.0D) {
            length = BulletProjectileTickUtil.LEGACY_BEAM_RANGE;
        }
        Vec3 delta = legacyBeamDelta(entity, partialTick, length);
        double age = Mth.clamp(1.0D - ((double) entity.tickCount - 2.0D + partialTick)
                / Math.max(1.0D, entity.config() == null ? 1.0D : entity.config().maxAge()), 0.0D, 1.0D);
        if (age <= 0.0D) {
            return;
        }
        if (trail == BulletTrail.SEDNA_FOLLY) {
            renderLegacyFollyBeam(entity, age, delta, poseStack, buffer);
            return;
        }
        if (style == BulletStyle.TAU) {
            boolean charge = Byte.toUnsignedInt(entity.trailId()) == 1;
            renderLegacyTauBeam(entity, age, delta, charge, poseStack, buffer);
            return;
        }
        switch (trail) {
            case SEDNA_LIGHTNING -> renderLegacyLightningBeam(entity, age, delta, 0.5D, poseStack, buffer);
            case SEDNA_LIGHTNING_SUB -> renderLegacyLightningBeam(entity, age, delta, 0.15D, poseStack, buffer);
            case SEDNA_CRACKLE -> renderLegacyBeamBar(age, delta, 0xE3D692, 0xFFFFFF, poseStack, buffer);
            case SEDNA_BLACK_LIGHTNING -> renderLegacyBeamBar(age, delta, 0x4C3093, 0x000000, poseStack, buffer);
            case SEDNA_NI4NI -> renderLegacyBeamBar(age, delta, 0xAAD2E5, 0xFFFFFF, poseStack, buffer);
            case LASER -> renderLegacyLaserBeam(entity, age, delta, 0x80, 0x15, 0x15, poseStack, buffer);
            case LACUNAE -> renderLegacyLaserBeam(entity, age, delta, 0x60, 0x15, 0x80, poseStack, buffer);
            case WORM -> renderLegacyLaserBeam(entity, age, delta, 0x15, 0x80, 0x15, poseStack, buffer);
            case GLASS_CYAN -> renderLegacyLaserBeam(entity, age, delta, 0x15, 0x15, 0x80, poseStack, buffer);
            default -> renderLegacyLaserBeam(entity, age, delta, 0x15, 0x15, 0x15, poseStack, buffer);
        }
    }

    private static Vec3 legacyBeamDelta(BulletProjectileEntity entity, float partialTick, double length) {
        Vec3 motion = entity.getDeltaMovement();
        Vec3 direction = motion.lengthSqr() > 1.0E-7D
                ? motion.normalize()
                : legacyBulletRotationDirection(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()),
                        Mth.lerp(partialTick, entity.xRotO, entity.getXRot()));
        return direction.scale(length);
    }

    private static Vec3 legacyBulletRotationDirection(float yawDegrees, float pitchDegrees) {
        float yaw = yawDegrees * Mth.DEG_TO_RAD;
        float pitch = pitchDegrees * Mth.DEG_TO_RAD;
        return new Vec3(Mth.sin(yaw) * Mth.cos(pitch), Mth.sin(pitch), Mth.cos(yaw) * Mth.cos(pitch));
    }

    private static void renderLegacyLightningBeam(BulletProjectileEntity entity, double age, Vec3 delta,
            double baseScale, PoseStack poseStack, MultiBufferSource buffer) {
        double widthScale = age / 2.0D + baseScale;
        double scale = 0.075D;
        int colorInner = scaledColor(0x20, 0x20, 0x40, age);
        int colorOuter = scaledColor(0x40, 0x40, 0x80, age);
        int segments = legacyBeamSegments(delta);
        LegacyBeamRenderer.solidBeam(poseStack, buffer, delta.x, delta.y, delta.z, WaveType.RANDOM,
                colorInner, colorInner, entity.tickCount / 3, segments, (float) (scale * widthScale),
                4, (float) (0.25F * widthScale));
        LegacyBeamRenderer.solidBeam(poseStack, buffer, delta.x, delta.y, delta.z, WaveType.RANDOM,
                colorOuter, colorOuter, entity.tickCount, segments, (float) (scale * 7.0D * widthScale),
                2, (float) (0.0625F * widthScale));
        LegacyBeamRenderer.solidBeam(poseStack, buffer, delta.x, delta.y, delta.z, WaveType.RANDOM,
                colorOuter, colorOuter, entity.tickCount / 2, segments, (float) (scale * 7.0D * widthScale),
                2, (float) (0.0625F * widthScale));
    }

    private static void renderLegacyTauBeam(BulletProjectileEntity entity, double age, Vec3 delta, boolean charge,
            PoseStack poseStack, MultiBufferSource buffer) {
        double widthScale = age / 2.0D + 0.5D;
        int colorInner = charge ? scaledColor(0x60, 0x50, 0x30, age) : scaledColor(0x30, 0x25, 0x10, age);
        LegacyBeamRenderer.solidBeam(poseStack, buffer, delta.x, delta.y, delta.z, WaveType.RANDOM,
                colorInner, colorInner, (entity.tickCount + entity.getId()) / 2, legacyBeamSegments(delta),
                (float) (0.075D * widthScale), 2, 0.0625F);
        double barScale = age * 2.0D;
        renderLegacyBeamBar(delta, charge ? 0xFFF0A0 : 0xFFBF00, 0xFFFFFF, Math.max(0.01D, barScale),
                poseStack, buffer);
    }

    private static void renderLegacyLaserBeam(BulletProjectileEntity entity, double age, Vec3 delta,
            int red, int green, int blue, PoseStack poseStack, MultiBufferSource buffer) {
        double widthScale = age / 2.0D + 0.5D;
        int colorInner = scaledColor(red, green, blue, age);
        LegacyBeamRenderer.solidBeam(poseStack, buffer, delta.x, delta.y, delta.z, WaveType.RANDOM,
                colorInner, colorInner, entity.tickCount / 3, legacyBeamSegments(delta), 0.0F,
                4, (float) (0.025F * widthScale));
    }

    private static void renderLegacyFollyBeam(BulletProjectileEntity entity, double age, Vec3 delta,
            PoseStack poseStack, MultiBufferSource buffer) {
        renderLegacyBeamFlare((1.0D - age) * 7.5D + 1.5D, 0.5F * (float) age, 0.75F * (float) age,
                poseStack, buffer);
        double widthScale = (1.0D - age) * 25.0D + 2.5D;
        int colorInner = scaledColor(0x20, 0x20, 0x20, age);
        LegacyBeamRenderer.solidBeam(poseStack, buffer, delta.x, delta.y, delta.z, WaveType.RANDOM,
                colorInner, colorInner, entity.tickCount / 3, legacyBeamSegments(delta), 0.0F,
                8, (float) (0.0625F * widthScale));
    }

    private static void renderLegacyBeamBar(double age, Vec3 delta, int dark, int light,
            PoseStack poseStack, MultiBufferSource buffer) {
        renderLegacyBeamBar(delta, dark, light, Math.max(0.01D, age * 5.0D), poseStack, buffer);
    }

    private static void renderLegacyBeamBar(Vec3 delta, int dark, int light, double widthScale,
            PoseStack poseStack, MultiBufferSource buffer) {
        LegacyBeamRenderer.solidBeam(poseStack, buffer, delta.x, delta.y, delta.z, WaveType.RANDOM,
                dark, light, 0, Math.max(1, (int) Math.ceil(delta.length())), 0.0F,
                2, (float) (0.03125D * widthScale));
    }

    private static void renderLegacyBeamFlare(double scale, float outerAlpha, float innerAlpha,
            PoseStack poseStack, MultiBufferSource buffer) {
        VertexConsumer consumer = buffer.getBuffer(FLARE_RENDER_TYPE);
        PoseStack.Pose pose = poseStack.last();
        CameraBasis cameraBasis = LegacyBillboardRenderer.currentCameraBasis();
        LegacyBillboardRenderer.billboardRgbaF(consumer, pose, cameraBasis,
                0.0D, 0.0D, 0.0D, scale, scale, 1.0F, 1.0F, 1.0F, outerAlpha, LightTexture.FULL_BRIGHT);
        scale *= 0.5D;
        LegacyBillboardRenderer.billboardRgbaF(consumer, pose, cameraBasis,
                0.0D, 0.0D, 0.0D, scale, scale, 1.0F, 1.0F, 1.0F, innerAlpha, LightTexture.FULL_BRIGHT);
    }

    private static int legacyBeamSegments(Vec3 delta) {
        return Math.max(1, (int) (delta.length() / 2.0D + 1.0D));
    }

    private static int scaledColor(int red, int green, int blue, double age) {
        int r = Mth.clamp((int) (red * age), 0, 255);
        int g = Mth.clamp((int) (green * age), 0, 255);
        int b = Mth.clamp((int) (blue * age), 0, 255);
        return r << 16 | g << 8 | b;
    }

    private static boolean isLegacyFlare(int trail) {
        return trail == LegacySednaBulletAppearance.FLARE
                || trail == LegacySednaBulletAppearance.FLARE_SUPPLY
                || trail == LegacySednaBulletAppearance.FLARE_WEAPON;
    }

    private static void renderLegacyFlare(int trail, BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer) {
        if (entity.tickCount < 2) {
            return;
        }
        float red = 1.0F;
        float green = 0.5F;
        float blue = 0.5F;
        if (trail == LegacySednaBulletAppearance.FLARE_SUPPLY) {
            red = 0.5F;
            blue = 1.0F;
        } else if (trail == LegacySednaBulletAppearance.FLARE_WEAPON) {
            red = 0.5F;
            green = 1.0F;
        }

        double scale = Math.min(5.0D, (entity.tickCount + partialTick - 2.0F) * 0.5D)
                * (0.8D + entity.level().random.nextDouble() * 0.4D);
        VertexConsumer consumer = buffer.getBuffer(FLARE_RENDER_TYPE);
        PoseStack.Pose pose = poseStack.last();
        CameraBasis cameraBasis = LegacyBillboardRenderer.currentCameraBasis();
        LegacyBillboardRenderer.billboardRgbaF(consumer, pose, cameraBasis,
                0.0D, 0.0D, 0.0D, scale, scale, red, green, blue, 0.5F, LightTexture.FULL_BRIGHT);
        scale *= 0.5D;
        LegacyBillboardRenderer.billboardRgbaF(consumer, pose, cameraBasis,
                0.0D, 0.0D, 0.0D, scale, scale, 1.0F, 1.0F, 1.0F, 0.75F, LightTexture.FULL_BRIGHT);
    }

    private static boolean renderLegacySpecialProjectile(int trail, BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        switch (trail) {
            case LegacySednaBulletAppearance.MINI_NUKE -> renderLegacyFatmanNuke(FATMAN_MININUKE, poseStack,
                    buffer, packedLight);
            case LegacySednaBulletAppearance.MINI_NUKE_BALEFIRE -> renderLegacyBalefireNuke(entity, partialTick,
                    poseStack, buffer, packedLight);
            case LegacySednaBulletAppearance.HIVE_ROCKET -> renderLegacyHiveRocket(poseStack, buffer, packedLight);
            case LegacySednaBulletAppearance.CLUSTER_BOMB -> renderLegacyClusterBomb(poseStack, buffer, packedLight);
            case LegacySednaBulletAppearance.BIG_NUKE_MIRV -> renderLegacyBigNukeMirv(poseStack, buffer,
                    packedLight);
            case LegacySednaBulletAppearance.GRENADE -> renderLegacySednaGrenade(poseStack, buffer, packedLight);
            case LegacySednaBulletAppearance.CHARGE_MORTAR -> renderLegacyChargeMortar(false, poseStack, buffer,
                    packedLight);
            case LegacySednaBulletAppearance.CHARGE_MORTAR_CHARGE -> renderLegacyChargeMortar(true, poseStack,
                    buffer, packedLight);
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void renderLegacyFatmanNuke(ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.125F / 1.5F, 0.125F / 1.5F, 0.125F / 1.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(0.0D, -1.0D, 1.0D);
        FATMAN.renderPart("MiniNuke", texture, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderLegacyClusterBomb(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.0625F / 1.5F, 0.0625F / 1.5F, 0.0625F / 1.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(0.0D, -1.0D, 1.0D);
        FATMAN.renderPart("MiniNuke", FATMAN_SUBMUNITION, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderLegacyBalefireNuke(BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        renderLegacyFatmanNuke(FATMAN_BALEFIRE, poseStack, buffer, packedLight);
        poseStack.pushPose();
        poseStack.scale(0.125F / 1.5F, 0.125F / 1.5F, 0.125F / 1.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(0.0D, -1.0D, 1.0D);
        float offset = entity.tickCount + partialTick;
        for (int layer = 0; layer < 3; layer++) {
            float movement = offset * (0.001F + layer * 0.003F) * -6.0F;
            FATMAN.renderPartGlintWithLegacyTextureMatrix("MiniNuke", BALEFIRE_GLINT, poseStack, buffer,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0, 155, 29, 255,
                    2.0F, 2.0F, 30.0F - layer * 60.0F, 0.0F, movement);
        }
        poseStack.popPose();
    }

    private static void renderLegacyHiveRocket(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.125F / 1.5F, 0.125F / 1.5F, 0.125F / 1.5F);
        poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
        poseStack.translate(0.0D, 0.0D, 3.5D);
        PANZERSCHRECK_MODEL_OBJ.renderPart("Rocket", PANZERSCHRECK, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderLegacyBigNukeMirv(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.5F / 1.5F, 0.5F / 1.5F, 0.5F / 1.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        PROJECTILES.renderPart("MissileMIRV", ROCKET_MIRV, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderLegacySednaGrenade(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.25F / 1.5F, 0.25F / 1.5F, 0.25F / 1.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        PROJECTILES.renderPart("Grenade", GRENADE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderLegacyChargeHook(BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 180.0F));
        poseStack.scale(0.125F, 0.125F, 0.125F);
        poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, 0.0D, -6.0D);
        CHARGE_THROWER.renderPart("Hook", CHARGE_THROWER_HOOK, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        renderLegacyChargeHookWire(entity, partialTick, poseStack, buffer);
    }

    private static void renderLegacyChargeHookWire(BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer) {
        Entity owner = entity.getOwner();
        if (!(owner instanceof Player player) || !isHoldingThisChargeHook(player, entity)) {
            return;
        }

        Vec3 bulletPos = new Vec3(
                Mth.lerp(partialTick, entity.xOld, entity.getX()),
                Mth.lerp(partialTick, entity.yOld, entity.getY()),
                Mth.lerp(partialTick, entity.zOld, entity.getZ()));
        Vec3 playerPos = new Vec3(
                Mth.lerp(partialTick, player.xOld, player.getX()),
                Mth.lerp(partialTick, player.yOld, player.getY()),
                Mth.lerp(partialTick, player.zOld, player.getZ()));
        float yaw = Mth.lerp(partialTick, player.yRotO, player.getYRot());
        float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        Vec3 offset = rotateLegacyChargeWireOffset(new Vec3(0.125D, 0.25D, -0.75D), yaw, pitch);
        Vec3 target = new Vec3(playerPos.x - offset.x, playerPos.y + player.getEyeHeight() - offset.y,
                playerPos.z - offset.z);
        Vec3 delta = target.subtract(bulletPos);
        double length = delta.length();
        if (length <= 1.0E-7D) {
            return;
        }

        double hang = Math.min(length / 15.0D, 0.5D);
        WireOffsets offsets = legacyChargeWireOffsets(delta, 0.03125D);
        for (int j = 0; j < 10; j++) {
            int k = j + 1;
            double sagJ = Math.sin(j / 10.0D * Math.PI) * hang;
            double sagK = Math.sin(k / 10.0D * Math.PI) * hang;
            double sagMean = (sagJ + sagK) * 0.5D;
            Vec3 sample = delta.scale((j + 0.5D) / 10.0D).subtract(0.0D, sagMean, 0.0D);
            int light = LevelRenderer.getLightColor(entity.level(), BlockPos.containing(bulletPos.add(sample)));
            ObjRenderContext context = new ObjRenderContext(poseStack, buffer, Blocks.AIR.defaultBlockState(),
                    light, OverlayTexture.NO_OVERLAY).withColor(0x606060);
            LegacyTexturedLineRenderer.wrappedLineSegment(WIRE_GREYSCALE, context,
                    delta.x * j / 10.0D,
                    delta.y * j / 10.0D - sagJ,
                    delta.z * j / 10.0D,
                    delta.x * k / 10.0D,
                    delta.y * k / 10.0D - sagK,
                    delta.z * k / 10.0D,
                    offsets.iX(), offsets.iY(), offsets.iZ(), offsets.jX(), offsets.jZ(), 8.0D);
        }
    }

    private static boolean isHoldingThisChargeHook(Player player, BulletProjectileEntity entity) {
        return isThisChargeHookStack(player.getMainHandItem(), entity)
                || isThisChargeHookStack(player.getOffhandItem(), entity);
    }

    private static boolean isThisChargeHookStack(ItemStack stack, BulletProjectileEntity entity) {
        return stack.getItem() instanceof ChargeThrowerItem && ChargeThrowerItem.getLastHook(stack) == entity.getId();
    }

    private static Vec3 rotateLegacyChargeWireOffset(Vec3 offset, float yawDegrees, float pitchDegrees) {
        double pitch = -pitchDegrees * Mth.DEG_TO_RAD;
        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);
        double y = offset.y * cosPitch + offset.z * sinPitch;
        double z = offset.z * cosPitch - offset.y * sinPitch;

        double yaw = -yawDegrees * Mth.DEG_TO_RAD;
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double x = offset.x * cosYaw + z * sinYaw;
        double rz = z * cosYaw - offset.x * sinYaw;
        return new Vec3(x, y, rz);
    }

    private static WireOffsets legacyChargeWireOffsets(Vec3 delta, double girth) {
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double yaw = Math.atan2(delta.x, delta.z);
        double pitch = Math.atan2(delta.y, horizontal);
        double newPitch = pitch + Math.PI * 0.5D;
        double newYaw = yaw + Math.PI * 0.5D;
        double iZ = Math.cos(yaw) * Math.cos(newPitch) * girth;
        double iX = Math.sin(yaw) * Math.cos(newPitch) * girth;
        double iY = Math.sin(newPitch) * girth;
        double jZ = Math.cos(newYaw) * girth;
        double jX = Math.sin(newYaw) * girth;
        return new WireOffsets(iX, iY, iZ, jX, jZ);
    }

    private static void renderLegacyChargeMortar(boolean charge, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.125F / 1.5F, 0.125F / 1.5F, 0.125F / 1.5F);
        poseStack.mulPose(Axis.YN.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(0.0D, 0.0D, -6.0D);
        CHARGE_THROWER.renderPart("Mortar", CHARGE_THROWER_MORTAR, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        if (charge) {
            CHARGE_THROWER.renderPart("Oomph", CHARGE_THROWER_MORTAR, poseStack, buffer, packedLight,
                    OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    private record WireOffsets(double iX, double iY, double iZ, double jX, double jZ) {
    }

    private static void renderBullet(int trail, BulletProjectileEntity entity, PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight, ModelPart bulletCube) {
        if (renderLegacySednaBullet(trail, entity, poseStack, buffer)) {
            return;
        }
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

    private static boolean renderLegacySednaBullet(int trail, BulletProjectileEntity entity, PoseStack poseStack,
            MultiBufferSource buffer) {
        switch (trail) {
            case LegacySednaBulletAppearance.STANDARD ->
                    renderLegacySednaBullet(entity, poseStack, buffer, 0xFFBF00, 0xFFFFFF);
            case LegacySednaBulletAppearance.AP ->
                    renderLegacySednaBullet(entity, poseStack, buffer, 0xFF6A00, 0xFFE28D);
            case LegacySednaBulletAppearance.EXPRESS ->
                    renderLegacySednaBullet(entity, poseStack, buffer, 0x9E082E, 0xFF8A79);
            case LegacySednaBulletAppearance.DU ->
                    renderLegacySednaBullet(entity, poseStack, buffer, 0x5CCD41, 0xE9FF8D);
            case LegacySednaBulletAppearance.HE ->
                    renderLegacySednaBullet(entity, poseStack, buffer, 0xD8CA00, 0xFFF19D);
            case LegacySednaBulletAppearance.SM ->
                    renderLegacySednaBullet(entity, poseStack, buffer, 0x42A8DD, 0xFFFFFF);
            case LegacySednaBulletAppearance.BLACK ->
                    renderLegacySednaBullet(entity, poseStack, buffer, 0x000000, 0x7F006E);
            case LegacySednaBulletAppearance.LEGENDARY ->
                    renderLegacySednaBullet(entity, poseStack, buffer, 0x7F006E, 0xFF7FED);
            case LegacySednaBulletAppearance.FRAGMENTATION ->
                    renderLegacySednaBullet(entity, poseStack, buffer, 0xFF6A00, 0xFFE28D);
            case LegacySednaBulletAppearance.FLECHETTE ->
                    renderLegacySednaBullet(entity, poseStack, buffer, 0x8C8C8C, 0xCACACA);
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void renderLegacySednaBullet(BulletProjectileEntity entity, PoseStack poseStack,
            MultiBufferSource buffer, int dark, int light) {
        renderLegacySednaBullet(entity, poseStack, buffer, dark, light, 1.0D, 0.03125D, 0.03125D * 0.25D);
    }

    private static void renderLegacySednaBullet(BulletProjectileEntity entity, PoseStack poseStack,
            MultiBufferSource buffer, int dark, int light, double lengthMultiplier, double widthF, double widthB) {
        double length = entity.getDeltaMovement().length() * lengthMultiplier / 1.5D;
        if (length <= 0.0D) {
            return;
        }
        double scaledWidthF = widthF / 1.5D;
        double scaledWidthB = widthB / 1.5D;
        VertexConsumer consumer = LegacyUntexturedQuadRenderer.solid(buffer);
        PoseStack.Pose pose = poseStack.last();
        sednaQuad(consumer, pose, dark, light,
                length, scaledWidthB, -scaledWidthB, length, scaledWidthB, scaledWidthB,
                0.0D, scaledWidthF, scaledWidthF, 0.0D, scaledWidthF, -scaledWidthF);
        sednaQuad(consumer, pose, dark, light,
                length, -scaledWidthB, -scaledWidthB, length, -scaledWidthB, scaledWidthB,
                0.0D, -scaledWidthF, scaledWidthF, 0.0D, -scaledWidthF, -scaledWidthF);
        sednaQuad(consumer, pose, dark, light,
                length, -scaledWidthB, scaledWidthB, length, scaledWidthB, scaledWidthB,
                0.0D, scaledWidthF, scaledWidthF, 0.0D, -scaledWidthF, scaledWidthF);
        sednaQuad(consumer, pose, dark, light,
                length, -scaledWidthB, -scaledWidthB, length, scaledWidthB, -scaledWidthB,
                0.0D, scaledWidthF, -scaledWidthF, 0.0D, -scaledWidthF, -scaledWidthF);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, length, scaledWidthB, scaledWidthB, dark, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, length, scaledWidthB, -scaledWidthB, dark, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, length, -scaledWidthB, -scaledWidthB, dark, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, length, -scaledWidthB, scaledWidthB, dark, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, 0.0D, scaledWidthF, scaledWidthF, light, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, 0.0D, scaledWidthF, -scaledWidthF, light, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, 0.0D, -scaledWidthF, -scaledWidthF, light, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, 0.0D, -scaledWidthF, scaledWidthF, light, 255);
    }

    private static void sednaQuad(VertexConsumer consumer, PoseStack.Pose pose, int dark, int light,
            double x0, double y0, double z0, double x1, double y1, double z1,
            double x2, double y2, double z2, double x3, double y3, double z3) {
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, x0, y0, z0, dark, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, x1, y1, z1, dark, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, x2, y2, z2, light, 255);
        LegacyUntexturedQuadRenderer.vertex(consumer, pose, x3, y3, z3, light, 255);
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
            case SEDNA_LIGHTNING -> {
                red = 0.25F;
                green = 0.25F;
                blue = 0.5F;
            }
            case SEDNA_CRACKLE -> {
                red = 0.89F;
                green = 0.84F;
                blue = 0.57F;
            }
            case SEDNA_BLACK_LIGHTNING -> {
                red = 0.30F;
                green = 0.19F;
                blue = 0.58F;
            }
            case SEDNA_NI4NI -> {
                red = 0.67F;
                green = 0.82F;
                blue = 0.90F;
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
