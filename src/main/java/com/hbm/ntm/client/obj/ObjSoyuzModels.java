package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class ObjSoyuzModels {
    public static final LegacyWavefrontModel SOYUZ = new LegacyWavefrontModel(
            model("soyuz"),
            texture("soyuz/engineblock")).asVBO();
    public static final LegacyWavefrontModel LANDER = new LegacyWavefrontModel(
            model("soyuz_lander"),
            texture("capsule/soyuz_lander")).asVBO();
    public static final LegacyWavefrontModel MODULE = new LegacyWavefrontModel(
            model("soyuz_module"),
            texture("capsule/module_dome")).asVBO();

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
        renderSoyuzParts(mainPartPlan(textures), poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderBoosters(SoyuzTextureSet textures, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        renderSoyuzParts(boosterPartPlan(textures), poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderLanderCapsule(boolean rusted, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        LANDER.renderPart("Capsule", rusted ? LANDER_RUST_TEXTURE : LANDER_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderLanderChute(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LANDER.renderPart("Chute", CHUTE_TEXTURE, poseStack, buffer, packedLight, packedOverlay);
    }

    public static void renderModule(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (SoyuzPartPlan plan : modulePartPlan()) {
            MODULE.renderOnly(plan.texture(), poseStack, buffer, packedLight, packedOverlay,
                    plan.parts().toArray(String[]::new));
        }
    }

    public static SoyuzTextureSet textureSetForSkin(int skin) {
        return switch (skin) {
            case 1 -> LUNA_TEXTURES;
            case 2 -> AUTHENTIC_TEXTURES;
            default -> SOYUZ_TEXTURES;
        };
    }

    public static List<SoyuzPartPlan> mainPartPlan(SoyuzTextureSet textures) {
        return List.of(
                part(textures.engineBlock(), "EngineBlock"),
                part(textures.bottomStage(), "BottomStage"),
                part(textures.topStage(), "TopStage"),
                part(textures.payload(), "Payload"),
                part(MEMENTO, "Memento"),
                part(textures.payloadBlocks(), "PayloadBlocks"),
                part(textures.les(), "LES"),
                part(textures.lesThrusters(), "LESThrusters"),
                part(textures.mainEngines(), "MainEngines"),
                part(textures.sideEngines(), "SideEngines"));
    }

    public static List<SoyuzPartPlan> boosterPartPlan(SoyuzTextureSet textures) {
        return List.of(
                part(textures.booster(), "Booster.000", "Booster.001", "Booster.002", "Booster.003"),
                part(textures.mainEngines(), "BoosterEngines.000", "BoosterEngines.001", "BoosterEngines.002",
                        "BoosterEngines.003"),
                part(textures.boosterSide(), "BoosterSide.000", "BoosterSide.001", "BoosterSide.002",
                        "BoosterSide.003"));
    }

    public static List<SoyuzPartPlan> modulePartPlan() {
        return List.of(
                part(MODULE_DOME_TEXTURE, "Dome"),
                part(MODULE_LANDER_TEXTURE, "Capsule"),
                part(MODULE_PROPULSION_TEXTURE, "Propulsion"),
                part(MODULE_SOLAR_TEXTURE, "Solar"));
    }

    private static void renderSoyuzParts(List<SoyuzPartPlan> plans, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        for (SoyuzPartPlan plan : plans) {
            SOYUZ.renderOnly(plan.texture(), poseStack, buffer, packedLight, packedOverlay,
                    plan.parts().toArray(String[]::new));
        }
    }

    private static SoyuzPartPlan part(ResourceLocation texture, String... parts) {
        return new SoyuzPartPlan(texture, List.of(parts));
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

    public record SoyuzPartPlan(ResourceLocation texture, List<String> parts) {
    }
}
