package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public final class ObjSoyuzModels {
    public static final LegacyWavefrontModel SOYUZ = new LegacyWavefrontModel(
            model("soyuz"),
            texture("soyuz/engineblock"));
    public static final LegacyWavefrontModel LANDER = new LegacyWavefrontModel(
            model("soyuz_lander"),
            texture("capsule/soyuz_lander"));
    public static final LegacyWavefrontModel MODULE = new LegacyWavefrontModel(
            model("soyuz_module"),
            texture("capsule/module_dome"));

    public static final SoyuzTextureSet SOYUZ_TEXTURES = textureSet("soyuz");
    public static final SoyuzTextureSet LUNA_TEXTURES = textureSet("soyuz_luna");
    public static final SoyuzTextureSet AUTHENTIC_TEXTURES = textureSet("soyuz_authentic");

    public static final ResourceLocation MEMENTO = texture("polaroid_memento");
    public static final ResourceLocation LANDER_TEXTURE = texture("capsule/soyuz_lander");
    public static final ResourceLocation LANDER_RUST_TEXTURE = texture("capsule/soyuz_lander_rust");
    public static final ResourceLocation CHUTE_TEXTURE = texture("capsule/soyuz_chute");
    public static final ResourceLocation MODULE_DOME_TEXTURE = texture("capsule/module_dome");
    public static final ResourceLocation MODULE_LANDER_TEXTURE = texture("capsule/module_lander");
    public static final ResourceLocation MODULE_PROPULSION_TEXTURE = texture("capsule/module_propulsion");
    public static final ResourceLocation MODULE_SOLAR_TEXTURE = texture("capsule/module_solar");

    public static void renderSoyuz(SoyuzTextureSet textures, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        renderMain(textures, poseStack, buffer, packedLight, packedOverlay);
        renderBoosters(textures, poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderMain(SoyuzTextureSet textures, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        SOYUZ.renderOnly(textures.engineBlock(), poseStack, buffer, packedLight, packedOverlay, "EngineBlock");
        SOYUZ.renderOnly(textures.bottomStage(), poseStack, buffer, packedLight, packedOverlay, "BottomStage");
        SOYUZ.renderOnly(textures.topStage(), poseStack, buffer, packedLight, packedOverlay, "TopStage");
        SOYUZ.renderOnly(textures.payload(), poseStack, buffer, packedLight, packedOverlay, "Payload");
        SOYUZ.renderOnly(MEMENTO, poseStack, buffer, packedLight, packedOverlay, "Memento");
        SOYUZ.renderOnly(textures.payloadBlocks(), poseStack, buffer, packedLight, packedOverlay, "PayloadBlocks");
        SOYUZ.renderOnly(textures.les(), poseStack, buffer, packedLight, packedOverlay, "LES");
        SOYUZ.renderOnly(textures.lesThrusters(), poseStack, buffer, packedLight, packedOverlay, "LESThrusters");
        SOYUZ.renderOnly(textures.mainEngines(), poseStack, buffer, packedLight, packedOverlay, "MainEngines");
        SOYUZ.renderOnly(textures.sideEngines(), poseStack, buffer, packedLight, packedOverlay, "SideEngines");
    }

    public static void renderBoosters(SoyuzTextureSet textures, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        SOYUZ.renderOnly(textures.booster(), poseStack, buffer, packedLight, packedOverlay,
                "Booster.000", "Booster.001", "Booster.002", "Booster.003");
        SOYUZ.renderOnly(textures.mainEngines(), poseStack, buffer, packedLight, packedOverlay,
                "BoosterEngines.000", "BoosterEngines.001", "BoosterEngines.002", "BoosterEngines.003");
        SOYUZ.renderOnly(textures.boosterSide(), poseStack, buffer, packedLight, packedOverlay,
                "BoosterSide.000", "BoosterSide.001", "BoosterSide.002", "BoosterSide.003");
    }

    public static void renderLanderCapsule(boolean rusted, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        LANDER.renderPart("Capsule", rusted ? LANDER_RUST_TEXTURE : LANDER_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderLanderChute(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LANDER.renderPart("Chute", CHUTE_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderModule(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        MODULE.renderPart("Dome", MODULE_DOME_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        MODULE.renderPart("Capsule", MODULE_LANDER_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        MODULE.renderPart("Propulsion", MODULE_PROPULSION_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
        MODULE.renderPart("Solar", MODULE_SOLAR_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
    }

    private static SoyuzTextureSet textureSet(String folder) {
        return new SoyuzTextureSet(
                texture(folder + "/engineblock"),
                texture(folder + "/bottomstage"),
                texture(folder + "/topstage"),
                texture(folder + "/payload"),
                texture(folder + "/payloadblocks"),
                texture(folder + "/les"),
                texture(folder + "/lesthrusters"),
                texture(folder + "/mainengines"),
                texture(folder + "/sideengines"),
                texture(folder + "/booster"),
                texture(folder + "/boosterside"));
    }

    private static ResourceLocation model(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "models/block/soyuz/" + name + ".obj");
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/soyuz/" + name + ".png");
    }

    private ObjSoyuzModels() {
    }

    public record SoyuzTextureSet(
            ResourceLocation engineBlock,
            ResourceLocation bottomStage,
            ResourceLocation topStage,
            ResourceLocation payload,
            ResourceLocation payloadBlocks,
            ResourceLocation les,
            ResourceLocation lesThrusters,
            ResourceLocation mainEngines,
            ResourceLocation sideEngines,
            ResourceLocation booster,
            ResourceLocation boosterSide) {
    }
}
