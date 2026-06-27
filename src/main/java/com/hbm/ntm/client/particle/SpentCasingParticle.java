package com.hbm.ntm.client.particle;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyUntexturedQuadRenderer;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.sound.LegacySoundPlayer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SpentCasingParticle extends Particle {
    private static final float MODEL_SCALE = 0.05F;
    private static final float SMOKE_JITTER = 0.001F;
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(HbmNtm.MOD_ID, "models/effect/casings.obj");
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/casings.png");
    private static final LegacyWavefrontModel MODEL = new LegacyWavefrontModel(MODEL_LOCATION, TEXTURE_LOCATION).noSmooth().asVBO();
    private static final LegacyWavefrontModel.SelectionHandle STRAIGHT =
            MODEL.prepareRenderOnlyInCallOrder("Straight");
    private static final LegacyWavefrontModel.SelectionHandle BOTTLENECK =
            MODEL.prepareRenderOnlyInCallOrder("Bottleneck");
    private static final LegacyWavefrontModel.SelectionHandle SHOTGUN =
            MODEL.prepareRenderOnlyInCallOrder("Shotgun");
    private static final LegacyWavefrontModel.SelectionHandle SHOTGUN_CASE =
            MODEL.prepareRenderOnlyInCallOrder("ShotgunCase");
    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.disableBlend();
        }

        @Override
        public void end(Tesselator tesselator) {
        }

        @Override
        public String toString() {
            return "HBM_SPENT_CASING";
        }
    };

    private final SpentCasingDefinition definition;
    private final boolean smoking;
    private final int maxSmokeGen;
    private final double smokeLift;
    private final int nodeLife;
    private final List<SmokeNode> smokeNodes = new ArrayList<>();
    private float rotationPitch;
    private float prevRotationPitch;
    private float rotationYaw;
    private float prevRotationYaw;
    private float momentumPitch;
    private float momentumYaw;
    private boolean lastMoveHitGround;
    private double lastInitMotionY;
    private boolean setupSmokeDeltas;
    private double previousSmokeRenderX;
    private double previousSmokeRenderY;
    private double previousSmokeRenderZ;

    public SpentCasingParticle(ClientLevel level, double x, double y, double z, double motionX, double motionY, double motionZ,
            float yaw, float pitch, float momentumPitch, float momentumYaw, String name, boolean smoking, int smokeLife,
            double smokeLift, int nodeLife) {
        super(level, x, y, z);
        this.definition = SpentCasingDefinition.fromName(name);
        this.xd = motionX;
        this.yd = motionY;
        this.zd = motionZ;
        this.rotationYaw = yaw;
        this.prevRotationYaw = yaw;
        this.rotationPitch = pitch;
        this.prevRotationPitch = pitch;
        this.momentumPitch = momentumPitch;
        this.momentumYaw = momentumYaw;
        this.smoking = smoking;
        this.maxSmokeGen = Math.max(0, smokeLife);
        this.smokeLift = smokeLift;
        this.nodeLife = Math.max(1, nodeLife);
        this.lifetime = definition.maxAge();
        this.gravity = 1.0F;
        this.hasPhysics = true;
        this.setSize(2.0F * MODEL_SCALE * Math.max(definition.scaleX(), definition.scaleZ()), MODEL_SCALE * definition.scaleY());
        this.setPos(x, y, z);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.yd -= 0.04D * this.gravity;
        this.lastInitMotionY = this.yd;
        this.moveWithBounce(this.xd, this.yd, this.zd);
        this.xd *= 0.98D;
        this.yd *= 0.98D;
        this.zd *= 0.98D;

        if (this.lastMoveHitGround) {
            this.xd *= 0.7D;
            this.zd *= 0.7D;
            this.rotationPitch = (float) Math.floor(this.rotationPitch / 180.0F + 0.5F) * 180.0F;
            this.momentumYaw *= 0.7F;
            this.lastMoveHitGround = false;
        }

        updateSmoke();
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;
        this.rotationPitch += this.momentumPitch;
        this.rotationYaw += this.momentumYaw;
        this.prevRotationPitch = unwrapPrevious(this.prevRotationPitch, this.rotationPitch);
        this.prevRotationYaw = unwrapPrevious(this.prevRotationYaw, this.rotationYaw);
    }

    private void moveWithBounce(double motionX, double motionY, double motionZ) {
        double oldX = this.x;
        double oldY = this.y;
        double oldZ = this.z;
        this.move(motionX, motionY, motionZ);
        double movedX = this.x - oldX;
        double movedY = this.y - oldY;
        double movedZ = this.z - oldZ;
        boolean hitX = Math.abs(movedX - motionX) > 1.0E-6D;
        boolean hitY = Math.abs(movedY - motionY) > 1.0E-6D;
        boolean hitZ = Math.abs(movedZ - motionZ) > 1.0E-6D;
        this.lastMoveHitGround = hitY && motionY < 0.0D;

        if (hitX) {
            this.xd *= -0.25D;
            if (Math.abs(this.momentumYaw) > 1.0E-7F) {
                this.momentumYaw *= -0.75F;
            } else {
                this.momentumYaw = (float) this.random.nextGaussian() * 10.0F * this.definition.bounceYaw();
            }
        }
        if (hitY) {
            this.yd *= -0.5D;
            boolean rotFromSpeed = Math.abs(this.yd) > 0.04D;
            if (rotFromSpeed || Math.abs(this.momentumPitch) > 1.0E-7F) {
                this.momentumPitch *= -0.75F;
                if (rotFromSpeed) {
                    float mult = Mth.clamp((float) (this.lastInitMotionY / 0.2D), -1.0F, 1.0F);
                    this.momentumPitch += (float) this.random.nextGaussian() * 10.0F * this.definition.bouncePitch() * mult;
                    this.momentumYaw += (float) this.random.nextGaussian() * 10.0F * this.definition.bounceYaw() * mult;
                }
            }
            if (Math.abs(this.lastInitMotionY) >= 0.2D) {
                playBounceSound();
            }
        }
        if (hitZ) {
            this.zd *= -0.25D;
            if (Math.abs(this.momentumYaw) > 1.0E-7F) {
                this.momentumYaw *= -0.75F;
            } else {
                this.momentumYaw = (float) this.random.nextGaussian() * 10.0F * this.definition.bounceYaw();
            }
        }
    }

    private void updateSmoke() {
        if (this.age > this.maxSmokeGen && !this.smokeNodes.isEmpty()) {
            this.smokeNodes.clear();
        }
        if (!this.smoking || this.age > this.maxSmokeGen) {
            return;
        }
        for (SmokeNode node : this.smokeNodes) {
            node.x += this.random.nextGaussian() * SMOKE_JITTER;
            node.z += this.random.nextGaussian() * SMOKE_JITTER;
            node.y += this.smokeLift * MODEL_SCALE;
            node.alpha = Math.max(0.0F, node.alpha - 1.0F / this.nodeLife);
        }
        if (this.age < this.maxSmokeGen) {
            this.smokeNodes.add(new SmokeNode(0.0D, 0.0D, 0.0D, this.smokeNodes.isEmpty() ? 0.0F : 1.0F));
        }
    }

    private void playBounceSound() {
        if (this.definition.bounceSound() == null) {
            return;
        }
        SoundEvent sound = LegacySoundPlayer.resolveEvent(this.definition.bounceSound());
        if (sound == null) {
            return;
        }
        float volume = this.definition.largeBounceSound() ? 1.0F : 0.5F;
        float pitch = 1.0F + this.random.nextFloat() * 0.2F;
        this.level.playLocalSound(this.x, this.y, this.z, sound, SoundSource.BLOCKS, volume, pitch, false);
    }

    @Override
    public void render(VertexConsumer ignored, Camera camera, float partialTick) {
        Vec3 cameraPos = camera.getPosition();
        double renderX = Mth.lerp(partialTick, this.xo, this.x);
        double renderY = Mth.lerp(partialTick, this.yo, this.y);
        double renderZ = Mth.lerp(partialTick, this.zo, this.z);
        PoseStack poseStack = new PoseStack();
        poseStack.translate(renderX - cameraPos.x(), renderY - cameraPos.y() - this.bbHeight / 4.0F + this.definition.scaleY() * 0.01F,
                renderZ - cameraPos.z());
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - Mth.lerp(partialTick, this.prevRotationYaw, this.rotationYaw)));
        poseStack.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partialTick, this.prevRotationPitch, this.rotationPitch)));
        poseStack.scale(this.definition.scaleX(), this.definition.scaleY(), this.definition.scaleZ());

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        String[] parts = this.definition.type().partNames();
        for (int i = 0; i < parts.length; i++) {
            int color = this.definition.color(i);
            renderCasingPart(parts[i], color, poseStack, buffer, getLightColor(partialTick));
        }
        buffer.endBatch();

        if (!this.smokeNodes.isEmpty()) {
            renderSmokeTrail(camera, partialTick);
        }
    }

    private void renderSmokeTrail(Camera camera, float partialTick) {
        double renderX = Mth.lerp(partialTick, this.xo, this.x);
        double renderY = Mth.lerp(partialTick, this.yo, this.y);
        double renderZ = Mth.lerp(partialTick, this.zo, this.z);
        if (!this.setupSmokeDeltas) {
            this.previousSmokeRenderX = renderX;
            this.previousSmokeRenderY = renderY;
            this.previousSmokeRenderZ = renderZ;
            this.setupSmokeDeltas = true;
        }
        Vec3 cameraPos = camera.getPosition();
        double originX = renderX - cameraPos.x();
        double originY = renderY - cameraPos.y() - this.bbHeight / 4.0F;
        double originZ = renderZ - cameraPos.z();
        double deltaX = this.previousSmokeRenderX - renderX;
        double deltaY = this.previousSmokeRenderY - renderY;
        double deltaZ = this.previousSmokeRenderZ - renderZ;
        for (SmokeNode node : this.smokeNodes) {
            node.x += deltaX;
            node.y += deltaY;
            node.z += deltaZ;
        }
        float width = this.definition.scaleX() * 0.5F * MODEL_SCALE;
        float yaw = camera.getYRot() * Mth.DEG_TO_RAD;
        float offX = Mth.cos(-yaw) * width;
        float offZ = Mth.sin(-yaw) * width;
        float timeAlpha = 1.0F - this.age / (float) Math.max(1, this.maxSmokeGen);
        PoseStack poseStack = new PoseStack();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        for (int i = 0; i + 1 < this.smokeNodes.size(); i++) {
            SmokeNode node = this.smokeNodes.get(i);
            SmokeNode past = this.smokeNodes.get(i + 1);
            int nodeAlpha = Mth.clamp((int) (255.0F * node.alpha * Math.max(0.0F, timeAlpha)), 0, 255);
            int pastAlpha = Mth.clamp((int) (255.0F * past.alpha * Math.max(0.0F, timeAlpha)), 0, 255);
            double nodeX = originX + node.x;
            double nodeY = originY + node.y;
            double nodeZ = originZ + node.z;
            double pastX = originX + past.x;
            double pastY = originY + past.y;
            double pastZ = originZ + past.z;
            LegacyUntexturedQuadRenderer.quad(poseStack, buffer, LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE,
                    nodeX, nodeY, nodeZ,
                    nodeX + offX, nodeY, nodeZ + offZ,
                    pastX + offX, pastY, pastZ + offZ,
                    pastX, pastY, pastZ,
                    0xFFFFFF, nodeAlpha, 0, 0, pastAlpha);

            LegacyUntexturedQuadRenderer.quad(poseStack, buffer, LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE,
                    nodeX, nodeY, nodeZ,
                    nodeX - offX, nodeY, nodeZ - offZ,
                    pastX - offX, pastY, pastZ - offZ,
                    pastX, pastY, pastZ,
                    0xFFFFFF, nodeAlpha, 0, 0, pastAlpha);
        }
        buffer.endBatch(LegacyUntexturedQuadRenderer.translucentNoCullType());
        this.previousSmokeRenderX = renderX;
        this.previousSmokeRenderY = renderY;
        this.previousSmokeRenderZ = renderZ;
    }

    private static void renderCasingPart(String partName, int color, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        LegacyWavefrontModel.SelectionHandle handle = casingHandle(partName);
        int red = color >> 16 & 255;
        int green = color >> 8 & 255;
        int blue = color & 255;
        if (handle != null) {
            MODEL.renderOnlyInCallOrder(TEXTURE_LOCATION, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                    red, green, blue, 255, false, handle);
            return;
        }
        MODEL.renderPart(partName, TEXTURE_LOCATION, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                red, green, blue, 255);
    }

    private static LegacyWavefrontModel.SelectionHandle casingHandle(String partName) {
        return switch (partName) {
            case "Straight" -> STRAIGHT;
            case "Bottleneck" -> BOTTLENECK;
            case "Shotgun" -> SHOTGUN;
            case "ShotgunCase" -> SHOTGUN_CASE;
            default -> null;
        };
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }

    private static float unwrapPrevious(float previous, float current) {
        if (Math.abs(previous - current) > 180.0F) {
            if (previous < current) {
                return previous + 360.0F;
            }
            return previous - 360.0F;
        }
        return previous;
    }

    private static final class SmokeNode {
        private double x;
        private double y;
        private double z;
        private float alpha;

        private SmokeNode(double x, double y, double z, float alpha) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.alpha = alpha;
        }
    }
}
