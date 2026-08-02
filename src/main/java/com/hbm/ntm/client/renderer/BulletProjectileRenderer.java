package com.hbm.ntm.client.renderer;

import com.hbm.ntm.util.HbmRegistryUtil;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.bullet.BulletConfig;
import com.hbm.ntm.bullet.LegacyBulletConfigs;
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
import com.hbm.ntm.client.obj.LegacyTexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedLineRenderer;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjEffectModels;
import com.hbm.ntm.client.obj.ObjNetworkModels;
import com.hbm.ntm.client.obj.ObjProjectileModels;
import com.hbm.ntm.client.obj.ObjWeaponModels;
import com.hbm.ntm.client.render.LegacyPoseRotations;
import com.hbm.ntm.client.render.LegacyRenderRandom;
import com.hbm.ntm.entity.projectile.BulletProjectileEntity;
import com.hbm.ntm.item.ChargeThrowerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;
import org.joml.Matrix4f;

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
    private static final ResourceLocation FLARE_TEXTURE =
            new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/flare.png");
    private static final ResourceLocation WIRE_GREYSCALE = ObjNetworkModels.texture("wire_greyscale");
    private static final ThreadLocal<LegacyWavefrontModel.TexturedPreparedSequence> ORB_TEXTURED_SEQUENCE =
            ThreadLocal.withInitial(LegacyWavefrontModel.TexturedPreparedSequence::new);
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
    private static final LegacyWavefrontModel.SelectionHandle PROJECTILE_BULLET_RIFLE =
            PROJECTILES.prepareRenderOnlyInCallOrder("BulletRifle");
    private static final LegacyWavefrontModel.SelectionHandle PROJECTILE_BULLET_PISTOL =
            PROJECTILES.prepareRenderOnlyInCallOrder("BulletPistol");
    private static final LegacyWavefrontModel.SelectionHandle PROJECTILE_BUCKSHOT =
            PROJECTILES.prepareRenderOnlyInCallOrder("Buckshot");
    private static final LegacyWavefrontModel.SelectionHandle PROJECTILE_FLECHETTE =
            PROJECTILES.prepareRenderOnlyInCallOrder("Flechette");
    private static final LegacyWavefrontModel.SelectionHandle PROJECTILE_GRENADE =
            PROJECTILES.prepareRenderOnlyInCallOrder("Grenade");
    private static final LegacyWavefrontModel.SelectionHandle PROJECTILE_ROCKET =
            PROJECTILES.prepareRenderOnlyInCallOrder("Rocket");
    private static final LegacyWavefrontModel.SelectionHandle PROJECTILE_MISSILE_MIRV =
            PROJECTILES.prepareRenderOnlyInCallOrder("MissileMIRV");
    private static final LegacyWavefrontModel.SelectionHandle LEADBURSTER_BASE =
            LEADBURSTER.prepareRenderOnlyInCallOrder("Based");
    private static final LegacyWavefrontModel.SelectionHandle LEADBURSTER_SPINNER =
            LEADBURSTER.prepareRenderOnlyInCallOrder("Based.001");
    private static final LegacyWavefrontModel.SelectionHandle LEADBURSTER_BACKLIGHT =
            LEADBURSTER.prepareRenderOnlyInCallOrder("Backlight");
    private static final ThreadLocal<BlockPos.MutableBlockPos> CHARGE_WIRE_LIGHT_POS =
            ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
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
        BulletConfig config = entity.config();
        if (config == LegacyBulletConfigs.PILE_DEBRIS) {
            renderLegacyPileDebris(entity, partialTick, poseStack, buffer, packedLight);
            super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        if (style == BulletStyle.NONE) {
            return;
        }
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
        if (config == null || config.renderRotations()) {
            LegacyPoseRotations.rotateYDegrees(poseStack,
                    Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F);
            LegacyPoseRotations.rotateZDegrees(poseStack,
                    Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 180.0F);
        }
        poseStack.scale(1.5F, 1.5F, 1.5F);
        if (renderLegacySpecialProjectile(trail, entity, partialTick, poseStack, buffer, packedLight)) {
            poseStack.popPose();
            super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
            return;
        }
        if (style != BulletStyle.BLADE) {
            LegacyPoseRotations.rotateXDegrees(poseStack, LegacyRenderRandom.seeded(entity.getId()).nextInt(90) - 45.0F);
        }

        switch (style) {
            case NORMAL, FOLLY -> renderBullet(trail, entity, partialTick, poseStack, buffer, packedLight, bulletCube);
            case PISTOL -> renderProjectilePart(PROJECTILE_BULLET_PISTOL, BULLET_PISTOL, 0.5F,
                    poseStack, buffer, packedLight);
            case FLECHETTE -> renderProjectilePart(PROJECTILE_FLECHETTE, FLECHETTE, 0.5F,
                    poseStack, buffer, packedLight);
            case PELLET -> renderProjectilePart(PROJECTILE_BUCKSHOT, BUCKSHOT, 0.5F, poseStack, buffer, packedLight);
            case BOLT -> renderBolt(BulletTrail.fromLegacyId(trail), entity.getId(), poseStack, buffer);
            case ROCKET -> {
                renderProjectilePart(PROJECTILE_ROCKET, ROCKET, 0.5F, poseStack, buffer, packedLight);
                if (trail == LegacySednaBulletAppearance.ROCKET_THRUST) {
                    poseStack.pushPose();
                    poseStack.translate(0.375D, 0.0D, 0.0D);
                    renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0x808080, 0xFFF2A7, 2.0D,
                            0.03125D, 0.03125D * 0.25D);
                    poseStack.popPose();
                }
            }
            case GRENADE -> renderProjectilePart(PROJECTILE_GRENADE, GRENADE, 0.25F, poseStack, buffer, packedLight);
            case ORB -> renderOrb(trail, entity, partialTick, poseStack, buffer);
            case APDS -> renderProjectilePart(PROJECTILE_FLECHETTE, FLECHETTE, 2.0F,
                    poseStack, buffer, packedLight);
            case BLADE -> renderBlade(entity, partialTick, poseStack, buffer, packedLight);
            case LEADBURSTER -> renderLeadburster(entity, partialTick, poseStack, buffer, packedLight);
            default -> {
            }
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    /** Exact RenderBulletMK4 + LegoClient.RENDER_GRAPHITE transform chain. */
    private static void renderLegacyPileDebris(BulletProjectileEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        try {
            LegacyPoseRotations.rotateYDegrees(poseStack,
                    Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F);
            LegacyPoseRotations.rotateZDegrees(poseStack,
                    Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 180.0F);
            poseStack.scale(2.0F, 2.0F, 2.0F);
            ObjProjectileModels.DEBRIS_GRAPHITE.renderAll(ObjProjectileModels.GRAPHITE_TEXTURE, poseStack, buffer,
                    packedLight, OverlayTexture.NO_OVERLAY);
        } finally {
            poseStack.popPose();
        }
    }

    private static void renderLegacySednaBeam(BulletStyle style, BulletTrail trail, BulletProjectileEntity entity,
            float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
        double length = entity.beamLength();
        if (length <= 0.0D) {
            length = BulletProjectileTickUtil.LEGACY_BEAM_RANGE;
        }
        double motionX = entity.getDeltaMovement().x;
        double motionY = entity.getDeltaMovement().y;
        double motionZ = entity.getDeltaMovement().z;
        double motionLengthSqr = motionX * motionX + motionY * motionY + motionZ * motionZ;
        double deltaX;
        double deltaY;
        double deltaZ;
        if (motionLengthSqr > 1.0E-7D) {
            double scale = length / Math.sqrt(motionLengthSqr);
            deltaX = motionX * scale;
            deltaY = motionY * scale;
            deltaZ = motionZ * scale;
        } else {
            float yawDegrees = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
            float pitchDegrees = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
            float yawRadians = yawDegrees * Mth.DEG_TO_RAD;
            float pitchRadians = pitchDegrees * Mth.DEG_TO_RAD;
            float cosPitch = Mth.cos(pitchRadians);
            deltaX = Mth.sin(yawRadians) * cosPitch * length;
            deltaY = Mth.sin(pitchRadians) * length;
            deltaZ = Mth.cos(yawRadians) * cosPitch * length;
        }
        double age = Mth.clamp(1.0D - ((double) entity.tickCount - 2.0D + partialTick)
                / Math.max(1.0D, entity.config() == null ? 1.0D : entity.config().maxAge()), 0.0D, 1.0D);
        if (age <= 0.0D) {
            return;
        }
        if (trail == BulletTrail.SEDNA_FOLLY) {
            renderLegacyFollyBeam(entity, age, deltaX, deltaY, deltaZ, length, poseStack, buffer);
            return;
        }
        if (style == BulletStyle.TAU) {
            boolean charge = Byte.toUnsignedInt(entity.trailId()) == 1;
            renderLegacyTauBeam(entity, age, deltaX, deltaY, deltaZ, length, charge, poseStack, buffer);
            return;
        }
        switch (trail) {
            case SEDNA_LIGHTNING ->
                    renderLegacyLightningBeam(entity, age, deltaX, deltaY, deltaZ, length, 0.5D, poseStack, buffer);
            case SEDNA_LIGHTNING_SUB ->
                    renderLegacyLightningBeam(entity, age, deltaX, deltaY, deltaZ, length, 0.15D, poseStack, buffer);
            case SEDNA_CRACKLE ->
                    renderLegacyBeamBar(age, deltaX, deltaY, deltaZ, length, 0xE3D692, 0xFFFFFF, poseStack, buffer);
            case SEDNA_BLACK_LIGHTNING ->
                    renderLegacyBeamBar(age, deltaX, deltaY, deltaZ, length, 0x4C3093, 0x000000, poseStack, buffer);
            case SEDNA_NI4NI ->
                    renderLegacyBeamBar(age, deltaX, deltaY, deltaZ, length, 0xAAD2E5, 0xFFFFFF, poseStack, buffer);
            case LASER ->
                    renderLegacyLaserBeam(entity, age, deltaX, deltaY, deltaZ, length, 0x80, 0x15, 0x15,
                            poseStack, buffer);
            case LACUNAE ->
                    renderLegacyLaserBeam(entity, age, deltaX, deltaY, deltaZ, length, 0x60, 0x15, 0x80,
                            poseStack, buffer);
            case WORM ->
                    renderLegacyLaserBeam(entity, age, deltaX, deltaY, deltaZ, length, 0x15, 0x80, 0x15,
                            poseStack, buffer);
            case GLASS_CYAN ->
                    renderLegacyLaserBeam(entity, age, deltaX, deltaY, deltaZ, length, 0x15, 0x15, 0x80,
                            poseStack, buffer);
            default ->
                    renderLegacyLaserBeam(entity, age, deltaX, deltaY, deltaZ, length, 0x15, 0x15, 0x15,
                            poseStack, buffer);
        }
    }

    private static void renderLegacyLightningBeam(BulletProjectileEntity entity, double age,
            double deltaX, double deltaY, double deltaZ, double length, double baseScale,
            PoseStack poseStack, MultiBufferSource buffer) {
        double widthScale = age / 2.0D + baseScale;
        double scale = 0.075D;
        int colorInner = scaledColor(0x20, 0x20, 0x40, age);
        int colorOuter = scaledColor(0x40, 0x40, 0x80, age);
        int segments = legacyBeamSegments(length);
        LegacyBeamRenderer.DirectSolidBeamBatch beamBatch =
                LegacyBeamRenderer.directSolidBeamBatch(poseStack, buffer, false);
        LegacyBeamRenderer.solidBeam(beamBatch, deltaX, deltaY, deltaZ, WaveType.RANDOM,
                colorInner, colorInner, entity.tickCount / 3, segments, (float) (scale * widthScale),
                4, (float) (0.25F * widthScale));
        LegacyBeamRenderer.solidBeam(beamBatch, deltaX, deltaY, deltaZ, WaveType.RANDOM,
                colorOuter, colorOuter, entity.tickCount, segments, (float) (scale * 7.0D * widthScale),
                2, (float) (0.0625F * widthScale));
        LegacyBeamRenderer.solidBeam(beamBatch, deltaX, deltaY, deltaZ, WaveType.RANDOM,
                colorOuter, colorOuter, entity.tickCount / 2, segments, (float) (scale * 7.0D * widthScale),
                2, (float) (0.0625F * widthScale));
    }

    private static void renderLegacyTauBeam(BulletProjectileEntity entity, double age,
            double deltaX, double deltaY, double deltaZ, double length, boolean charge,
            PoseStack poseStack, MultiBufferSource buffer) {
        double widthScale = age / 2.0D + 0.5D;
        int colorInner = charge ? scaledColor(0x60, 0x50, 0x30, age) : scaledColor(0x30, 0x25, 0x10, age);
        LegacyBeamRenderer.DirectSolidBeamBatch beamBatch =
                LegacyBeamRenderer.directSolidBeamBatch(poseStack, buffer, false);
        LegacyBeamRenderer.solidBeam(beamBatch, deltaX, deltaY, deltaZ, WaveType.RANDOM,
                colorInner, colorInner, (entity.tickCount + entity.getId()) / 2, legacyBeamSegments(length),
                (float) (0.075D * widthScale), 2, 0.0625F);
        double barScale = age * 2.0D;
        renderLegacyBeamBar(beamBatch, deltaX, deltaY, deltaZ, length, charge ? 0xFFF0A0 : 0xFFBF00, 0xFFFFFF,
                Math.max(0.01D, barScale));
    }

    private static void renderLegacyLaserBeam(BulletProjectileEntity entity, double age,
            double deltaX, double deltaY, double deltaZ, double length, int red, int green, int blue,
            PoseStack poseStack, MultiBufferSource buffer) {
        double widthScale = age / 2.0D + 0.5D;
        int colorInner = scaledColor(red, green, blue, age);
        LegacyBeamRenderer.DirectSolidBeamBatch beamBatch =
                LegacyBeamRenderer.directSolidBeamBatch(poseStack, buffer, false);
        LegacyBeamRenderer.solidBeam(beamBatch, deltaX, deltaY, deltaZ, WaveType.RANDOM,
                colorInner, colorInner, entity.tickCount / 3, legacyBeamSegments(length), 0.0F,
                4, (float) (0.025F * widthScale));
    }

    private static void renderLegacyFollyBeam(BulletProjectileEntity entity, double age,
            double deltaX, double deltaY, double deltaZ, double length, PoseStack poseStack, MultiBufferSource buffer) {
        renderLegacyBeamFlare((1.0D - age) * 7.5D + 1.5D, 0.5F * (float) age, 0.75F * (float) age,
                poseStack, buffer);
        double widthScale = (1.0D - age) * 25.0D + 2.5D;
        int colorInner = scaledColor(0x20, 0x20, 0x20, age);
        LegacyBeamRenderer.DirectSolidBeamBatch beamBatch =
                LegacyBeamRenderer.directSolidBeamBatch(poseStack, buffer, false);
        LegacyBeamRenderer.solidBeam(beamBatch, deltaX, deltaY, deltaZ, WaveType.RANDOM,
                colorInner, colorInner, entity.tickCount / 3, legacyBeamSegments(length), 0.0F,
                8, (float) (0.0625F * widthScale));
    }

    private static void renderLegacyBeamBar(double age, double deltaX, double deltaY, double deltaZ, double length,
            int dark, int light, PoseStack poseStack, MultiBufferSource buffer) {
        renderLegacyBeamBar(deltaX, deltaY, deltaZ, length, dark, light, Math.max(0.01D, age * 5.0D),
                poseStack, buffer);
    }

    private static void renderLegacyBeamBar(double deltaX, double deltaY, double deltaZ, double length,
            int dark, int light, double widthScale, PoseStack poseStack, MultiBufferSource buffer) {
        LegacyBeamRenderer.DirectSolidBeamBatch beamBatch =
                LegacyBeamRenderer.directSolidBeamBatch(poseStack, buffer, false);
        renderLegacyBeamBar(beamBatch, deltaX, deltaY, deltaZ, length, dark, light, widthScale);
    }

    private static void renderLegacyBeamBar(LegacyBeamRenderer.DirectSolidBeamBatch beamBatch,
            double deltaX, double deltaY, double deltaZ, double length,
            int dark, int light, double widthScale) {
        LegacyBeamRenderer.solidBeam(beamBatch, deltaX, deltaY, deltaZ, WaveType.RANDOM,
                dark, light, 0, Math.max(1, (int) Math.ceil(length)), 0.0F,
                2, (float) (0.03125D * widthScale));
    }

    private static void renderLegacyBeamFlare(double scale, float outerAlpha, float innerAlpha,
            PoseStack poseStack, MultiBufferSource buffer) {
        CameraBasis cameraBasis = LegacyBillboardRenderer.currentCameraBasisScratch();
        double innerScale = scale * 0.5D;
        LegacyBillboardRenderer.billboardPairRgbaF(FLARE_TEXTURE, poseStack, buffer,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, cameraBasis,
                0.0D, 0.0D, 0.0D,
                scale, scale, 1.0F, 1.0F, 1.0F, outerAlpha,
                innerScale, innerScale, 1.0F, 1.0F, 1.0F, innerAlpha,
                LightTexture.FULL_BRIGHT);
    }

    private static int legacyBeamSegments(double length) {
        return Math.max(1, (int) (length / 2.0D + 1.0D));
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
        CameraBasis cameraBasis = LegacyBillboardRenderer.currentCameraBasisScratch();
        double innerScale = scale * 0.5D;
        LegacyBillboardRenderer.billboardPairRgbaF(FLARE_TEXTURE, poseStack, buffer,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, cameraBasis,
                0.0D, 0.0D, 0.0D,
                scale, scale, red, green, blue, 0.5F,
                innerScale, innerScale, 1.0F, 1.0F, 1.0F, 0.75F,
                LightTexture.FULL_BRIGHT);
    }

    private static boolean renderLegacySpecialProjectile(int trail, BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        switch (trail) {
            case LegacySednaBulletAppearance.MINI_NUKE -> renderLegacyFatmanNuke(FATMAN_MININUKE, poseStack,
                    buffer, packedLight);
            case LegacySednaBulletAppearance.MINI_NUKE_BALEFIRE -> renderLegacyBalefireNuke(entity, partialTick,
                    poseStack, buffer, packedLight);
            case LegacySednaBulletAppearance.HIVE_ROCKET -> renderLegacyPanzerschreckRocket(poseStack, buffer,
                    packedLight);
            case LegacySednaBulletAppearance.ROCKET_RPZB -> {
                renderLegacyPanzerschreckRocket(poseStack, buffer, packedLight);
                renderLegacyRocketThrust(entity, partialTick, poseStack, buffer);
            }
            case LegacySednaBulletAppearance.ROCKET_QD -> {
                renderLegacyQuickDrawRocket(poseStack, buffer, packedLight);
                renderLegacyRocketThrust(entity, partialTick, poseStack, buffer);
            }
            case LegacySednaBulletAppearance.ROCKET_ML -> {
                renderLegacyMissileLauncherRocket(poseStack, buffer, packedLight);
                renderLegacyRocketThrust(entity, partialTick, poseStack, buffer);
            }
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
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        poseStack.translate(0.0D, -1.0D, 1.0D);
        ObjWeaponModels.renderPart(FATMAN, "MiniNuke", texture, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderLegacyClusterBomb(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.0625F / 1.5F, 0.0625F / 1.5F, 0.0625F / 1.5F);
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        poseStack.translate(0.0D, -1.0D, 1.0D);
        ObjWeaponModels.renderPart(FATMAN, "MiniNuke", FATMAN_SUBMUNITION, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderLegacyBalefireNuke(BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        renderLegacyFatmanNuke(FATMAN_BALEFIRE, poseStack, buffer, packedLight);
        poseStack.pushPose();
        poseStack.scale(0.125F / 1.5F, 0.125F / 1.5F, 0.125F / 1.5F);
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        poseStack.translate(0.0D, -1.0D, 1.0D);
        float offset = entity.tickCount + partialTick;
        for (int layer = 0; layer < 3; layer++) {
            float movement = offset * (0.001F + layer * 0.003F) * -6.0F;
            ObjWeaponModels.renderPartGlintWithLegacyTextureMatrix(FATMAN, "MiniNuke", BALEFIRE_GLINT, poseStack,
                    buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0, 155, 29, 255,
                    2.0F, 2.0F, 30.0F - layer * 60.0F, 0.0F, movement);
        }
        poseStack.popPose();
    }

    private static void renderLegacyPanzerschreckRocket(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.125F / 1.5F, 0.125F / 1.5F, 0.125F / 1.5F);
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        poseStack.translate(0.0D, 0.0D, 3.5D);
        ObjWeaponModels.renderPart(PANZERSCHRECK_MODEL_OBJ, "Rocket", PANZERSCHRECK, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderLegacyQuickDrawRocket(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(1.0F / 1.5F, 1.0F / 1.5F, 1.0F / 1.5F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 90.0F);
        PROJECTILES.renderOnlyInCallOrder(ROCKET, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                PROJECTILE_ROCKET);
        poseStack.popPose();
    }

    private static void renderLegacyMissileLauncherRocket(PoseStack poseStack, MultiBufferSource buffer,
            int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.25F / 1.5F, 0.25F / 1.5F, 0.25F / 1.5F);
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        poseStack.translate(0.0D, -1.0D, -4.5D);
        ObjWeaponModels.renderPart(ObjWeaponModels.MISSILE_LAUNCHER, "Missile",
                ObjWeaponModels.MISSILE_LAUNCHER_TEXTURE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderLegacyRocketThrust(BulletProjectileEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer) {
        poseStack.pushPose();
        poseStack.translate(0.375D, 0.0D, 0.0D);
        renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0x808080, 0xFFF2A7, 2.0D,
                0.03125D, 0.03125D * 0.25D);
        poseStack.popPose();
    }

    private static void renderLegacyBigNukeMirv(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.5F / 1.5F, 0.5F / 1.5F, 0.5F / 1.5F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 90.0F);
        PROJECTILES.renderOnlyInCallOrder(ROCKET_MIRV, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                PROJECTILE_MISSILE_MIRV);
        poseStack.popPose();
    }

    private static void renderLegacySednaGrenade(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(0.25F / 1.5F, 0.25F / 1.5F, 0.25F / 1.5F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 90.0F);
        PROJECTILES.renderOnlyInCallOrder(GRENADE, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                PROJECTILE_GRENADE);
        poseStack.popPose();
    }

    private static void renderLegacyChargeHook(BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateYDegrees(poseStack, Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, Mth.lerp(partialTick, entity.xRotO, entity.getXRot()) + 180.0F);
        poseStack.scale(0.125F, 0.125F, 0.125F);
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 180.0F);
        poseStack.translate(0.0D, 0.0D, -6.0D);
        ObjWeaponModels.renderPart(CHARGE_THROWER, "Hook", CHARGE_THROWER_HOOK, poseStack, buffer, packedLight,
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

        double bulletX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double bulletY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double bulletZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        double playerX = Mth.lerp(partialTick, player.xOld, player.getX());
        double playerY = Mth.lerp(partialTick, player.yOld, player.getY());
        double playerZ = Mth.lerp(partialTick, player.zOld, player.getZ());
        float yaw = Mth.lerp(partialTick, player.yRotO, player.getYRot());
        float pitch = Mth.lerp(partialTick, player.xRotO, player.getXRot());
        double pitchRadians = -pitch * Mth.DEG_TO_RAD;
        double cosPitch = Math.cos(pitchRadians);
        double sinPitch = Math.sin(pitchRadians);
        double offsetY = 0.25D * cosPitch - 0.75D * sinPitch;
        double pitchRotatedZ = -0.75D * cosPitch - 0.25D * sinPitch;
        double yawRadians = -yaw * Mth.DEG_TO_RAD;
        double cosYaw = Math.cos(yawRadians);
        double sinYaw = Math.sin(yawRadians);
        double offsetX = 0.125D * cosYaw + pitchRotatedZ * sinYaw;
        double offsetZ = pitchRotatedZ * cosYaw - 0.125D * sinYaw;
        double deltaX = playerX - offsetX - bulletX;
        double deltaY = playerY + player.getEyeHeight() - offsetY - bulletY;
        double deltaZ = playerZ - offsetZ - bulletZ;
        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        if (length <= 1.0E-7D) {
            return;
        }

        double hang = Math.min(length / 15.0D, 0.5D);
        WireOffsets offsets = legacyChargeWireOffsets(deltaX, deltaY, deltaZ, 0.03125D);
        Level level = entity.level();
        BlockPos.MutableBlockPos lightPos = CHARGE_WIRE_LIGHT_POS.get();
        VertexConsumer wireConsumer = LegacyTexturedQuadRenderer.vertexAlphaConsumer(WIRE_GREYSCALE, buffer,
                LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        PoseStack.Pose wirePose = poseStack.last();
        int lastLightX = Integer.MIN_VALUE;
        int lastLightY = Integer.MIN_VALUE;
        int lastLightZ = Integer.MIN_VALUE;
        int lastLight = 0;
        for (int j = 0; j < 10; j++) {
            int k = j + 1;
            double sagJ = Math.sin(j / 10.0D * Math.PI) * hang;
            double sagK = Math.sin(k / 10.0D * Math.PI) * hang;
            double sagMean = (sagJ + sagK) * 0.5D;
            double sampleScale = (j + 0.5D) / 10.0D;
            lightPos.set(bulletX + deltaX * sampleScale, bulletY + deltaY * sampleScale - sagMean,
                    bulletZ + deltaZ * sampleScale);
            int sampleX = lightPos.getX();
            int sampleY = lightPos.getY();
            int sampleZ = lightPos.getZ();
            if (sampleX != lastLightX || sampleY != lastLightY || sampleZ != lastLightZ) {
                lastLight = LevelRenderer.getLightColor(level, lightPos);
                lastLightX = sampleX;
                lastLightY = sampleY;
                lastLightZ = sampleZ;
            }
            int light = lastLight;
            LegacyTexturedLineRenderer.wrappedLineSegment(wireConsumer, wirePose,
                    light, OverlayTexture.NO_OVERLAY,
                    deltaX * j / 10.0D,
                    deltaY * j / 10.0D - sagJ,
                    deltaZ * j / 10.0D,
                    deltaX * k / 10.0D,
                    deltaY * k / 10.0D - sagK,
                    deltaZ * k / 10.0D,
                    offsets.iX(), offsets.iY(), offsets.iZ(), offsets.jX(), offsets.jZ(), 8.0D,
                    0x606060, 255);
        }
    }

    private static boolean isHoldingThisChargeHook(Player player, BulletProjectileEntity entity) {
        return isThisChargeHookStack(player.getMainHandItem(), entity)
                || isThisChargeHookStack(player.getOffhandItem(), entity);
    }

    private static boolean isThisChargeHookStack(ItemStack stack, BulletProjectileEntity entity) {
        return stack.getItem() instanceof ChargeThrowerItem && ChargeThrowerItem.getLastHook(stack) == entity.getId();
    }

    private static WireOffsets legacyChargeWireOffsets(double deltaX, double deltaY, double deltaZ, double girth) {
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double yaw = Math.atan2(deltaX, deltaZ);
        double pitch = Math.atan2(deltaY, horizontal);
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
        LegacyPoseRotations.rotateYDegrees(poseStack, -90.0F);
        LegacyPoseRotations.rotateZDegrees(poseStack, 180.0F);
        poseStack.translate(0.0D, 0.0D, -6.0D);
        ObjWeaponModels.renderPart(CHARGE_THROWER, "Mortar", CHARGE_THROWER_MORTAR, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY);
        if (charge) {
            ObjWeaponModels.renderPart(CHARGE_THROWER, "Oomph", CHARGE_THROWER_MORTAR, poseStack, buffer, packedLight,
                    OverlayTexture.NO_OVERLAY);
        }
        poseStack.popPose();
    }

    private record WireOffsets(double iX, double iY, double iZ, double jX, double jZ) {
    }

    private static void renderBullet(int trail, BulletProjectileEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight, ModelPart bulletCube) {
        if (renderLegacySednaBullet(trail, entity, partialTick, poseStack, buffer)) {
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
        renderProjectilePart(PROJECTILE_BULLET_RIFLE, BULLET_RIFLE, 0.5F, poseStack, buffer, packedLight);
    }

    private static boolean renderLegacySednaBullet(int trail, BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer) {
        switch (trail) {
            case LegacySednaBulletAppearance.STANDARD ->
                    renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0xFFBF00, 0xFFFFFF);
            case LegacySednaBulletAppearance.AP ->
                    renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0xFF6A00, 0xFFE28D);
            case LegacySednaBulletAppearance.EXPRESS ->
                    renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0x9E082E, 0xFF8A79);
            case LegacySednaBulletAppearance.DU ->
                    renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0x5CCD41, 0xE9FF8D);
            case LegacySednaBulletAppearance.HE ->
                    renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0xD8CA00, 0xFFF19D);
            case LegacySednaBulletAppearance.SM ->
                    renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0x42A8DD, 0xFFFFFF);
            case LegacySednaBulletAppearance.BLACK ->
                    renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0x000000, 0x7F006E);
            case LegacySednaBulletAppearance.LEGENDARY ->
                    renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0x7F006E, 0xFF7FED);
            case LegacySednaBulletAppearance.FRAGMENTATION ->
                    renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0xFF6A00, 0xFFE28D);
            case LegacySednaBulletAppearance.FLECHETTE ->
                    renderLegacySednaBullet(entity, partialTick, poseStack, buffer, 0x8C8C8C, 0xCACACA);
            default -> {
                return false;
            }
        }
        return true;
    }

    private static void renderLegacySednaBullet(BulletProjectileEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int dark, int light) {
        renderLegacySednaBullet(entity, partialTick, poseStack, buffer, dark, light, 1.0D, 0.03125D,
                0.03125D * 0.25D);
    }

    private static void renderLegacySednaBullet(BulletProjectileEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int dark, int light, double lengthMultiplier, double widthF, double widthB) {
        double length = entity.legacyInterpolatedClientVisualSpeed(partialTick) * lengthMultiplier / 1.5D;
        if (length <= 0.0D) {
            return;
        }
        double scaledWidthF = widthF / 1.5D;
        double scaledWidthB = widthB / 1.5D;
        LegacyUntexturedQuadRenderer.DirectQuadBatch batch =
                LegacyUntexturedQuadRenderer.directQuadBatch(poseStack, buffer,
                        LegacyTexturedRenderMode.CUTOUT_NO_CULL);
        sednaQuad(batch, dark, light,
                length, scaledWidthB, -scaledWidthB, length, scaledWidthB, scaledWidthB,
                0.0D, scaledWidthF, scaledWidthF, 0.0D, scaledWidthF, -scaledWidthF);
        sednaQuad(batch, dark, light,
                length, -scaledWidthB, -scaledWidthB, length, -scaledWidthB, scaledWidthB,
                0.0D, -scaledWidthF, scaledWidthF, 0.0D, -scaledWidthF, -scaledWidthF);
        sednaQuad(batch, dark, light,
                length, -scaledWidthB, scaledWidthB, length, scaledWidthB, scaledWidthB,
                0.0D, scaledWidthF, scaledWidthF, 0.0D, -scaledWidthF, scaledWidthF);
        sednaQuad(batch, dark, light,
                length, -scaledWidthB, -scaledWidthB, length, scaledWidthB, -scaledWidthB,
                0.0D, scaledWidthF, -scaledWidthF, 0.0D, -scaledWidthF, -scaledWidthF);
        LegacyUntexturedQuadRenderer.quadDirect(batch,
                length, scaledWidthB, scaledWidthB,
                length, scaledWidthB, -scaledWidthB,
                length, -scaledWidthB, -scaledWidthB,
                length, -scaledWidthB, scaledWidthB,
                dark, 255, 255, 255, 255);
        LegacyUntexturedQuadRenderer.quadDirect(batch,
                0.0D, scaledWidthF, scaledWidthF,
                0.0D, scaledWidthF, -scaledWidthF,
                0.0D, -scaledWidthF, -scaledWidthF,
                0.0D, -scaledWidthF, scaledWidthF,
                light, 255, 255, 255, 255);
    }

    private static void sednaQuad(LegacyUntexturedQuadRenderer.DirectQuadBatch batch, int dark, int light,
            double x0, double y0, double z0, double x1, double y1, double z1,
            double x2, double y2, double z2, double x3, double y3, double z3) {
        LegacyUntexturedQuadRenderer.quadDirect(batch,
                x0, y0, z0, dark, 255,
                x1, y1, z1, dark, 255,
                x2, y2, z2, light, 255,
                x3, y3, z3, light, 255);
    }

    private static void renderProjectilePart(LegacyWavefrontModel.SelectionHandle part, ResourceLocation texture, float scale,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        LegacyPoseRotations.rotateZDegrees(poseStack, 90.0F);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        PROJECTILES.renderOnlyInCallOrder(texture, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, part);
        poseStack.popPose();
    }

    private static void renderOrb(int trail, BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer) {
        if (trail == 0) {
            LegacyWavefrontModel.TexturedPreparedSequence orbSequence = ORB_TEXTURED_SEQUENCE.get();
            boolean prepared = ObjEffectModels.SPHERE_UV.prepareTexturedAllSequence(orbSequence, TOM_FLAME, buffer,
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 255, 255, 255, 255, false,
                    LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, LegacyWavefrontModel.UvTransform.DEFAULT,
                    LegacyWavefrontModel.RenderBackendFallbackReason.NONE);
            if (prepared) {
                try {
                    orbSequence.render(poseStack);
                    poseStack.pushPose();
                    try {
                        poseStack.scale(0.3F, 0.3F, 0.3F);
                        orbSequence.render(poseStack);
                    } finally {
                        poseStack.popPose();
                    }
                } finally {
                    orbSequence.clear();
                }
            }
            renderOrbSparks(entity, partialTick, poseStack, buffer, 5, 0.5F, 2, 2, 0x8080FF, 0xFFFFFF);
            return;
        }
        if (trail == 1) {
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            VertexConsumer sphereConsumer = ObjEffectModels.dynamicUntexturedConsumer(buffer, 128, true);
            Matrix4f spherePosition = poseStack.last().pose();
            ObjEffectModels.renderSphereUvDynamicUntextured(sphereConsumer, spherePosition, 128, 0, 0, 128);
            poseStack.scale(0.75F, 0.75F, 0.75F);
            ObjEffectModels.renderSphereUvDynamicUntextured(sphereConsumer, spherePosition, 128, 0, 0, 128);
            poseStack.popPose();
            renderOrbSparks(entity, partialTick, poseStack, buffer, 3, 1.0F, 2, 3, 0xFF0000, 0xFF8080);
        }
    }

    private static void renderOrbSparks(BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int count, float length, int min, int max,
            int color1, int color2) {
        int timeSeed = (int) ((entity.tickCount + partialTick) * 5.0F);
        VertexConsumer outer = LegacySparkRenderer.outerLineConsumer(buffer,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE);
        VertexConsumer inner = LegacySparkRenderer.innerLineConsumer(buffer,
                LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE);
        PoseStack.Pose pose = poseStack.last();
        for (int i = 0; i < count; i++) {
            LegacySparkRenderer.renderSpark(pose, outer, inner, timeSeed + 100 * i, 0.0D, 0.0D, 0.0D,
                    length, min, max, color1, color2);
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

        LegacyUntexturedQuadRenderer.DirectQuadBatch batch =
                LegacyUntexturedQuadRenderer.directQuadBatch(poseStack, buffer,
                        LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 0);
        for (int i = 0; i < nodes.size() - 1; i++) {
            BulletTauTrailUtil.TauTrailNode node = nodes.get(i);
            BulletTauTrailUtil.TauTrailNode past = nodes.get(i + 1);
            double nodeAlpha = node.weight() * timeAlpha;
            double pastAlpha = past.weight() * timeAlpha;
            if (nodeAlpha == 0.0D && pastAlpha == 0.0D) {
                break;
            }
            tauRibbon(batch, node.offset().x, node.offset().y, node.offset().z,
                    past.offset().x, past.offset().y, past.offset().z, scale, red, green, blue,
                    (float) nodeAlpha, (float) pastAlpha);
            tauRibbon(batch, node.offset().x, node.offset().y, node.offset().z,
                    past.offset().x, past.offset().y, past.offset().z, -scale, red, green, blue,
                    (float) nodeAlpha, (float) pastAlpha);
        }
    }

    private static void tauRibbon(LegacyUntexturedQuadRenderer.DirectQuadBatch batch,
            double nodeX, double nodeY, double nodeZ, double pastX, double pastY, double pastZ,
            double yOffset, float red, float green, float blue, float nodeAlpha, float pastAlpha) {
        float outerAlpha = 0.25F;
        LegacyUntexturedQuadRenderer.quadRgbaFDirect(batch,
                nodeX, nodeY, nodeZ,
                nodeX, nodeY + yOffset, nodeZ,
                pastX, pastY + yOffset, pastZ,
                pastX, pastY, pastZ,
                red, green, blue,
                nodeAlpha, nodeAlpha * outerAlpha, pastAlpha * outerAlpha, pastAlpha);
    }

    private static void renderBlade(BulletProjectileEntity entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ItemStack stack = new ItemStack(HbmRegistryUtil.item(BLADE_TITANIUM).orElseThrow());
        if (stack.is(Items.AIR)) {
            return;
        }
        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, 90.0F);
        poseStack.translate(0.0D, 0.5D, 0.0D);
        // Legacy RenderBullet uses the client wall-clock angle rather than entity age.
        LegacyPoseRotations.rotateXDegrees(poseStack, (float) (System.currentTimeMillis() % 360L));
        poseStack.translate(0.0D, -0.5D, 0.0D);
        LegacyPoseRotations.rotateYDegrees(poseStack, 90.0F);
        poseStack.scale(1.0F, 2.0F, 1.0F);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED,
                packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
    }

    private static void renderLeadburster(BulletProjectileEntity entity, float partialTick,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        LegacyPoseRotations.rotateZDegrees(poseStack, -90.0F);
        poseStack.scale(0.05F, 0.05F, 0.05F);
        LEADBURSTER.renderOnlyInCallOrder(LEADBURSTER_TEXTURE, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, LEADBURSTER_BASE);
        if (entity.getStuckIn() != -1) {
            LegacyPoseRotations.rotateYDegrees(poseStack, (entity.tickCount + partialTick) * -18.0F);
        }
        LEADBURSTER.renderOnlyInCallOrder(LEADBURSTER_TEXTURE, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, LEADBURSTER_SPINNER);
        LEADBURSTER.renderOnlyInCallOrder(LEADBURSTER_TEXTURE, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, LEADBURSTER_BACKLIGHT);
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
                Random random = LegacyRenderRandom.seeded(entityId * (long) entityId);
                red = random.nextInt(2) * 0.6F;
                green = random.nextInt(2) * 0.6F;
                blue = random.nextInt(2) * 0.6F;
            }
        }

        poseStack.pushPose();
        poseStack.scale(0.25F, 0.125F, 0.125F);
        poseStack.scale(-1.0F, 1.0F, 1.0F);
        poseStack.scale(2.0F, 2.0F, 2.0F);
        int color = rgb(red, green, blue);
        LegacyUntexturedQuadRenderer.DirectTriangleBatch triangleBatch =
                LegacyUntexturedQuadRenderer.directTriangleBatch(poseStack, buffer,
                        LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 0);
        triangle(triangleBatch, color, 1.0F, 6, 0, 0, 3, -1, -1, 3, 1, -1);
        triangle(triangleBatch, color, 1.0F, 6, 0, 0, 3, 1, 1, 3, -1, 1);
        triangle(triangleBatch, color, 1.0F, 6, 0, 0, 3, -1, 1, 3, -1, -1);
        triangle(triangleBatch, color, 1.0F, 6, 0, 0, 3, 1, -1, 3, 1, 1);
        triangle(triangleBatch, color, 1.0F, 6, 0, 0, 4, -0.5F, -0.5F, 4, 0.5F, -0.5F);
        triangle(triangleBatch, color, 1.0F, 6, 0, 0, 4, 0.5F, 0.5F, 4, -0.5F, 0.5F);
        triangle(triangleBatch, color, 1.0F, 6, 0, 0, 4, -0.5F, 0.5F, 4, -0.5F, -0.5F);
        triangle(triangleBatch, color, 1.0F, 6, 0, 0, 4, 0.5F, -0.5F, 4, 0.5F, 0.5F);
        LegacyUntexturedQuadRenderer.DirectQuadBatch tailBatch =
                LegacyUntexturedQuadRenderer.directQuadBatch(poseStack, buffer,
                        LegacyTexturedRenderMode.ADDITIVE_NO_DEPTH_WRITE, 0);
        tailQuad(tailBatch, red, green, blue, 4, 0.5F, -0.5F, 4, 0.5F, 0.5F, 0, 0.5F, 0.5F, 0, 0.5F, -0.5F);
        tailQuad(tailBatch, red, green, blue, 4, -0.5F, -0.5F, 4, -0.5F, 0.5F, 0, -0.5F, 0.5F, 0, -0.5F, -0.5F);
        tailQuad(tailBatch, red, green, blue, 4, -0.5F, 0.5F, 4, 0.5F, 0.5F, 0, 0.5F, 0.5F, 0, -0.5F, 0.5F);
        tailQuad(tailBatch, red, green, blue, 4, -0.5F, -0.5F, 4, 0.5F, -0.5F, 0, 0.5F, -0.5F, 0, -0.5F, -0.5F);
        poseStack.popPose();
    }

    private static void triangle(LegacyUntexturedQuadRenderer.DirectTriangleBatch batch, int color, float alpha,
            float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        LegacyUntexturedQuadRenderer.triangleDirect(batch,
                x1, y1, z1, color, alpha(alpha),
                x2, y2, z2, color, 0,
                x3, y3, z3, color, 0);
    }

    private static void tailQuad(LegacyUntexturedQuadRenderer.DirectQuadBatch batch, float red, float green, float blue,
            float x1, float y1, float z1, float x2, float y2, float z2,
            float x3, float y3, float z3, float x4, float y4, float z4) {
        LegacyUntexturedQuadRenderer.quadRgbaFDirect(batch,
                x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4,
                red, green, blue, 1.0F, 1.0F, 0.0F, 0.0F);
    }

    private static int rgb(float red, float green, float blue) {
        return alpha(red) << 16 | alpha(green) << 8 | alpha(blue);
    }

    private static int alpha(float value) {
        return Mth.clamp((int) (value * 255.0F), 0, 255);
    }

    @Override
    public ResourceLocation getTextureLocation(BulletProjectileEntity entity) {
        return BULLET_RIFLE;
    }
}
