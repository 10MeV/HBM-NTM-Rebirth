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
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_ENGINE_BLOCK =
            SOYUZ.prepareRenderOnly("EngineBlock");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_BOTTOM_STAGE =
            SOYUZ.prepareRenderOnly("BottomStage");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_TOP_STAGE =
            SOYUZ.prepareRenderOnly("TopStage");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_PAYLOAD =
            SOYUZ.prepareRenderOnly("Payload");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_MEMENTO =
            SOYUZ.prepareRenderOnly("Memento");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_PAYLOAD_BLOCKS =
            SOYUZ.prepareRenderOnly("PayloadBlocks");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_LES =
            SOYUZ.prepareRenderOnly("LES");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_LES_THRUSTERS =
            SOYUZ.prepareRenderOnly("LESThrusters");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_MAIN_ENGINES =
            SOYUZ.prepareRenderOnly("MainEngines");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_SIDE_ENGINES =
            SOYUZ.prepareRenderOnly("SideEngines");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_BOOSTERS =
            SOYUZ.prepareRenderOnly("Booster.000", "Booster.001", "Booster.002", "Booster.003");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_BOOSTER_ENGINES =
            SOYUZ.prepareRenderOnly("BoosterEngines.000", "BoosterEngines.001", "BoosterEngines.002",
                    "BoosterEngines.003");
    private static final LegacyWavefrontModel.SelectionHandle SOYUZ_BOOSTER_SIDES =
            SOYUZ.prepareRenderOnly("BoosterSide.000", "BoosterSide.001", "BoosterSide.002", "BoosterSide.003");
    private static final LegacyWavefrontModel.SelectionHandle LANDER_CAPSULE =
            LANDER.prepareRenderOnlyInCallOrder("Capsule");
    private static final LegacyWavefrontModel.SelectionHandle LANDER_CHUTE =
            LANDER.prepareRenderOnlyInCallOrder("Chute");
    private static final LegacyWavefrontModel.SelectionHandle MODULE_DOME =
            MODULE.prepareRenderOnlyInCallOrder("Dome");
    private static final LegacyWavefrontModel.SelectionHandle MODULE_CAPSULE =
            MODULE.prepareRenderOnlyInCallOrder("Capsule");
    private static final LegacyWavefrontModel.SelectionHandle MODULE_PROPULSION =
            MODULE.prepareRenderOnlyInCallOrder("Propulsion");
    private static final LegacyWavefrontModel.SelectionHandle MODULE_SOLAR =
            MODULE.prepareRenderOnlyInCallOrder("Solar");

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

    public static final SoyuzRenderStatePlan MAIN_RENDER_STATE = new SoyuzRenderStatePlan(true, true, false, true);
    public static final SoyuzRenderStatePlan BOOSTER_RENDER_STATE = new SoyuzRenderStatePlan(true, true, false, true);
    public static final SoyuzRenderStatePlan MODULE_RENDER_STATE = new SoyuzRenderStatePlan(true, true, true, true);

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
        LANDER.renderOnlyInCallOrder(rusted ? LANDER_RUST_TEXTURE : LANDER_TEXTURE, poseStack, buffer, packedLight,
                packedOverlay, LANDER_CAPSULE);
    }

    public static void renderLanderChute(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        LANDER.renderOnlyInCallOrder(CHUTE_TEXTURE, poseStack, buffer, packedLight, packedOverlay, LANDER_CHUTE);
    }

    public static void renderModule(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        for (SoyuzPartPlan plan : modulePartPlan()) {
            MODULE.renderOnlyInCallOrder(plan.texture(), poseStack, buffer, packedLight, packedOverlay,
                    plan.selection());
        }
    }

    public static SoyuzRenderPlan soyuzRenderPlan(int skin) {
        SoyuzTextureSet textures = textureSetForSkin(skin);
        return new SoyuzRenderPlan(skin, textures,
                new SoyuzSectionPlan("main", MAIN_RENDER_STATE, mainPartPlan(textures)),
                new SoyuzSectionPlan("boosters", BOOSTER_RENDER_STATE, boosterPartPlan(textures)));
    }

    public static SoyuzSectionPlan moduleRenderPlan() {
        return new SoyuzSectionPlan("module", MODULE_RENDER_STATE, modulePartPlan());
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
                part(textures.engineBlock(), SOYUZ_ENGINE_BLOCK, "EngineBlock"),
                part(textures.bottomStage(), SOYUZ_BOTTOM_STAGE, "BottomStage"),
                part(textures.topStage(), SOYUZ_TOP_STAGE, "TopStage"),
                part(textures.payload(), SOYUZ_PAYLOAD, "Payload"),
                part(MEMENTO, SOYUZ_MEMENTO, "Memento"),
                part(textures.payloadBlocks(), SOYUZ_PAYLOAD_BLOCKS, "PayloadBlocks"),
                part(textures.les(), SOYUZ_LES, "LES"),
                part(textures.lesThrusters(), SOYUZ_LES_THRUSTERS, "LESThrusters"),
                part(textures.mainEngines(), SOYUZ_MAIN_ENGINES, "MainEngines"),
                part(textures.sideEngines(), SOYUZ_SIDE_ENGINES, "SideEngines"));
    }

    public static List<SoyuzPartPlan> boosterPartPlan(SoyuzTextureSet textures) {
        return List.of(
                part(textures.booster(), SOYUZ_BOOSTERS,
                        "Booster.000", "Booster.001", "Booster.002", "Booster.003"),
                part(textures.mainEngines(), SOYUZ_BOOSTER_ENGINES,
                        "BoosterEngines.000", "BoosterEngines.001", "BoosterEngines.002",
                        "BoosterEngines.003"),
                part(textures.boosterSide(), SOYUZ_BOOSTER_SIDES,
                        "BoosterSide.000", "BoosterSide.001", "BoosterSide.002",
                        "BoosterSide.003"));
    }

    public static List<SoyuzPartPlan> modulePartPlan() {
        return List.of(
                part(MODULE_DOME_TEXTURE, MODULE_DOME, "Dome"),
                part(MODULE_LANDER_TEXTURE, MODULE_CAPSULE, "Capsule"),
                part(MODULE_PROPULSION_TEXTURE, MODULE_PROPULSION, "Propulsion"),
                part(MODULE_SOLAR_TEXTURE, MODULE_SOLAR, "Solar"));
    }

    private static void renderSoyuzParts(List<SoyuzPartPlan> plans, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        for (SoyuzPartPlan plan : plans) {
            SOYUZ.renderOnlyInCallOrder(plan.texture(), poseStack, buffer, packedLight, packedOverlay,
                    plan.selection());
        }
    }

    private static SoyuzPartPlan part(ResourceLocation texture, LegacyWavefrontModel.SelectionHandle selection,
            String... parts) {
        return new SoyuzPartPlan(texture, List.of(parts), selection);
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
        return new ResourceLocation(HbmNtm.MOD_ID, "models/" + name + ".obj");
    }

    private static ResourceLocation texture(String name) {
        if ("polaroid_memento".equals(name)) {
            return new ResourceLocation(HbmNtm.MOD_ID, "textures/item/polaroid_memento.png");
        }
        if (name.startsWith("capsule/")) {
            return new ResourceLocation(HbmNtm.MOD_ID,
                    "textures/models/soyuz_capsule/" + name.substring("capsule/".length()) + ".png");
        }
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + name + ".png");
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

    public record SoyuzPartPlan(ResourceLocation texture, List<String> parts,
                                LegacyWavefrontModel.SelectionHandle selection) {
    }

    public record SoyuzSectionPlan(String section, SoyuzRenderStatePlan state, List<SoyuzPartPlan> parts) {
    }

    public record SoyuzRenderPlan(int requestedSkin, SoyuzTextureSet textures,
                                  SoyuzSectionPlan main, SoyuzSectionPlan boosters) {
    }

    public record SoyuzRenderStatePlan(boolean cullEnabled, boolean smoothShade,
                                       boolean lightingForcedEnabled, boolean restoreFlatShade) {
    }
}
