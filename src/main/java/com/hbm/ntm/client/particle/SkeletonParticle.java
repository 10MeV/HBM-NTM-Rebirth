package com.hbm.ntm.client.particle;

import com.hbm.ntm.HbmNtm;
import com.hbm.ntm.client.obj.LegacyTexturedRenderMode;
import com.hbm.ntm.client.obj.LegacyWavefrontModel;
import com.hbm.ntm.client.obj.ObjRenderContext;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SkeletonParticle extends Particle {
    private static final ResourceLocation MODEL_LOCATION = new ResourceLocation(HbmNtm.MOD_ID, "models/effect/skeleton.obj");
    private static final ResourceLocation SKELETON_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/skeleton.png");
    private static final ResourceLocation SKELETON_BLOOD_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/skeleton_blood.png");
    private static final ResourceLocation SKOILET_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/skoilet.png");
    private static final ResourceLocation SKOILET_BLOOD_TEXTURE = new ResourceLocation(HbmNtm.MOD_ID, "textures/particle/skoilet_blood.png");
    private static final LegacyWavefrontModel MODEL = new LegacyWavefrontModel(MODEL_LOCATION, SKELETON_TEXTURE).noSmooth().asVBO();
    private static final LegacyWavefrontModel.SelectionHandle SKULL =
            MODEL.prepareRenderOnlyInCallOrder("Skull");
    private static final LegacyWavefrontModel.SelectionHandle TORSO =
            MODEL.prepareRenderOnlyInCallOrder("Torso");
    private static final LegacyWavefrontModel.SelectionHandle LIMB =
            MODEL.prepareRenderOnlyInCallOrder("Limb");
    private static final LegacyWavefrontModel.SelectionHandle SKULL_VILLAGER =
            MODEL.prepareRenderOnlyInCallOrder("SkullVillager");
    private static final ParticleRenderType RENDER_TYPE = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
        }

        @Override
        public void end(Tesselator tesselator) {
        }

        @Override
        public String toString() {
            return "HBM_SKELETON";
        }
    };

    private final BoneType type;
    private float rotationPitch;
    private float prevRotationPitch;
    private float rotationYaw;
    private float prevRotationYaw;
    private float momentumPitch;
    private float momentumYaw;
    private int initialDelay;
    private boolean bloodTexture;

    public SkeletonParticle(ClientLevel level, double x, double y, double z, float brightness, BoneType type, float yaw, float pitch) {
        super(level, x, y, z);
        this.type = type;
        this.rotationYaw = yaw;
        this.prevRotationYaw = yaw;
        this.rotationPitch = pitch;
        this.prevRotationPitch = pitch;
        this.rCol = brightness;
        this.gCol = brightness;
        this.bCol = brightness;
        this.alpha = 1.0F;
        this.gravity = 0.02F;
        this.initialDelay = 20;
        this.momentumPitch = this.random.nextFloat() * 5.0F * (this.random.nextBoolean() ? 1.0F : -1.0F);
        this.momentumYaw = this.random.nextFloat() * 5.0F * (this.random.nextBoolean() ? 1.0F : -1.0F);
        this.lifetime = 1200 + this.random.nextInt(20);
        this.hasPhysics = true;
        this.setSize(type == BoneType.TORSO ? 0.5F : 0.35F, type == BoneType.LIMB ? 0.75F : 0.5F);
        this.setPos(x, y, z);
    }

    public SkeletonParticle makeGib(boolean keepNormalTexture) {
        this.initialDelay = -2;
        this.bloodTexture = !keepNormalTexture;
        this.gravity = 0.04F;
        this.lifetime = 600 + this.random.nextInt(20);
        return this;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.prevRotationPitch = this.rotationPitch;
        this.prevRotationYaw = this.rotationYaw;

        if (this.initialDelay-- > 0) {
            return;
        }

        if (this.initialDelay == -1) {
            this.xd = this.random.nextGaussian() * 0.025D;
            this.zd = this.random.nextGaussian() * 0.025D;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        boolean wasOnGround = this.onGround;
        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.98D;
        this.yd *= 0.98D;
        this.zd *= 0.98D;

        if (!this.onGround) {
            this.rotationPitch += this.momentumPitch;
            this.rotationYaw += this.momentumYaw;
        } else {
            this.xd = 0.0D;
            this.yd = 0.0D;
            this.zd = 0.0D;
            if (!wasOnGround) {
                this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.SKELETON_HURT, SoundSource.HOSTILE,
                        0.25F, 0.8F + this.random.nextFloat() * 0.4F, false);
            }
        }

        this.prevRotationPitch = unwrapPrevious(this.prevRotationPitch, this.rotationPitch);
        this.prevRotationYaw = unwrapPrevious(this.prevRotationYaw, this.rotationYaw);
    }

    @Override
    public void render(VertexConsumer ignored, Camera camera, float partialTick) {
        Vec3 cameraPos = camera.getPosition();
        double renderX = Mth.lerp(partialTick, this.xo, this.x);
        double renderY = Mth.lerp(partialTick, this.yo, this.y);
        double renderZ = Mth.lerp(partialTick, this.zo, this.z);
        float timeLeft = this.lifetime - (this.age + partialTick);
        this.alpha = timeLeft < 40.0F ? Math.max(timeLeft / 40.0F, 0.0F) : 1.0F;

        PoseStack poseStack = new PoseStack();
        poseStack.translate(renderX - cameraPos.x(), renderY - cameraPos.y(), renderZ - cameraPos.z());
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, this.prevRotationYaw, this.rotationYaw)));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, this.prevRotationPitch, this.rotationPitch)));
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        renderSkeletonPart(this.type.partName, textureLocation(), poseStack, buffer, getLightColor(partialTick),
                color(this.rCol), color(this.gCol), color(this.bCol), color(this.alpha));
        buffer.endBatch();
    }

    private static void renderSkeletonPart(String partName, ResourceLocation texture, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int red, int green, int blue, int alpha) {
        LegacyWavefrontModel.SelectionHandle handle = skeletonHandle(partName);
        if (handle != null) {
            ObjRenderContext context = new ObjRenderContext(poseStack, buffer, null, packedLight,
                    OverlayTexture.NO_OVERLAY)
                    .withRgba(red, green, blue, alpha)
                    .withRenderMode(LegacyTexturedRenderMode.TRANSLUCENT_NO_DEPTH_WRITE);
            MODEL.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        MODEL.renderPartTranslucent(partName, texture, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY,
                red, green, blue, alpha);
    }

    private static LegacyWavefrontModel.SelectionHandle skeletonHandle(String partName) {
        return switch (partName) {
            case "Skull" -> SKULL;
            case "Torso" -> TORSO;
            case "Limb" -> LIMB;
            case "SkullVillager" -> SKULL_VILLAGER;
            default -> null;
        };
    }

    @Override
    public ParticleRenderType getRenderType() {
        return RENDER_TYPE;
    }

    private ResourceLocation textureLocation() {
        if (this.type == BoneType.SKULL_VILLAGER) {
            return this.bloodTexture ? SKOILET_BLOOD_TEXTURE : SKOILET_TEXTURE;
        }
        return this.bloodTexture ? SKELETON_BLOOD_TEXTURE : SKELETON_TEXTURE;
    }

    private static int color(float value) {
        return Mth.clamp((int) (value * 255.0F), 0, 255);
    }

    private static float unwrapPrevious(float previous, float current) {
        if (Math.abs(previous - current) > 180.0F) {
            return previous < current ? previous + 360.0F : previous - 360.0F;
        }
        return previous;
    }

    public enum BoneType {
        SKULL("Skull"),
        TORSO("Torso"),
        LIMB("Limb"),
        SKULL_VILLAGER("SkullVillager");

        private final String partName;

        BoneType(String partName) {
            this.partName = partName;
        }
    }
}
