package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ObjLaunchModels {
    public static final ObjModelPart LAUNCH_TABLE_BASE = part("launch_table_base");
    public static final ObjModelPart LAUNCH_TABLE_LARGE_PAD = part("launch_table_large_pad");
    public static final ObjModelPart LAUNCH_TABLE_SMALL_PAD = part("launch_table_small_pad");
    public static final ObjModelPart LAUNCH_TABLE_LARGE_SCAFFOLD_BASE = part("launch_table_large_scaffold_base");
    public static final ObjModelPart LAUNCH_TABLE_LARGE_SCAFFOLD_CONNECTOR = part("launch_table_large_scaffold_connector");
    public static final ObjModelPart LAUNCH_TABLE_LARGE_SCAFFOLD_EMPTY = part("launch_table_large_scaffold_empty");
    public static final ObjModelPart LAUNCH_TABLE_SMALL_SCAFFOLD_BASE = part("launch_table_small_scaffold_base");
    public static final ObjModelPart LAUNCH_TABLE_SMALL_SCAFFOLD_CONNECTOR = part("launch_table_small_scaffold_connector");
    public static final ObjModelPart LAUNCH_TABLE_SMALL_SCAFFOLD_EMPTY = part("launch_table_small_scaffold_empty");

    public static final ObjModelPart SOYUZ_LAUNCHER_LEGS = part("soyuz_launcher_legs");
    public static final ObjModelPart SOYUZ_LAUNCHER_TABLE = part("soyuz_launcher_table");
    public static final ObjModelPart SOYUZ_LAUNCHER_TOWER_BASE = part("soyuz_launcher_tower_base");
    public static final ObjModelPart SOYUZ_LAUNCHER_TOWER = part("soyuz_launcher_tower");
    public static final ObjModelPart SOYUZ_LAUNCHER_SUPPORT_BASE = part("soyuz_launcher_support_base");
    public static final ObjModelPart SOYUZ_LAUNCHER_SUPPORT = part("soyuz_launcher_support");

    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_LEGS_LEGACY = legacyModel("soyuz_launcher_legs").noSmooth().asVBO();
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_TABLE_LEGACY = legacyModel("soyuz_launcher_table").noSmooth().asVBO();
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_TOWER_BASE_LEGACY = legacyModel("soyuz_launcher_tower_base").noSmooth().asVBO();
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_TOWER_LEGACY = legacyModel("soyuz_launcher_tower").noSmooth().asVBO();
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_SUPPORT_BASE_LEGACY = legacyModel("soyuz_launcher_support_base").noSmooth().asVBO();
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_SUPPORT_LEGACY = legacyModel("soyuz_launcher_support").noSmooth().asVBO();
    public static final LegacyWavefrontModel MISSILE_PAD = legacyModel("launch_pad_silo", "silo").asVBO();
    public static final LegacyWavefrontModel MISSILE_ERECTOR = legacyModel("launch_pad_erector", "pad").asVBO();
    public static final LegacyWavefrontModel MISSILE_ASSEMBLY = legacyModel("missile_assembly").asVBO();
    public static final LegacyWavefrontModel STRUT = legacyModel("strut").asVBO();
    public static final LegacyWavefrontModel COMPACT_LAUNCHER = legacyModel("compact_launcher").asVBO();
    public static final LegacyWavefrontModel LAUNCH_TABLE_BASE_LEGACY = legacyModel("launch_table_base", "launch_table").asVBO();
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_PAD_LEGACY = legacyModel("launch_table_large_pad").asVBO();
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_PAD_LEGACY = legacyModel("launch_table_small_pad").asVBO();
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_SCAFFOLD_BASE_LEGACY = legacyModel("launch_table_large_scaffold_base").asVBO();
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_SCAFFOLD_CONNECTOR_LEGACY = legacyModel("launch_table_large_scaffold_connector").asVBO();
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_SCAFFOLD_EMPTY_LEGACY = legacyModel("launch_table_large_scaffold_empty", "launch_table_large_scaffold_base").asVBO();
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_SCAFFOLD_BASE_LEGACY = legacyModel("launch_table_small_scaffold_base").asVBO();
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_SCAFFOLD_CONNECTOR_LEGACY = legacyModel("launch_table_small_scaffold_connector").asVBO();
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_SCAFFOLD_EMPTY_LEGACY = legacyModel("launch_table_small_scaffold_empty", "launch_table_small_scaffold_base").asVBO();

    private static final Map<String, LegacyWavefrontModel.SelectionHandle> MISSILE_ERECTOR_HANDLES =
            missileErectorHandles(
                    "Pad",
                    "ABM_Pad", "ABM_Erector", "ABM_Pivot", "ABM_Rope",
                    "Micro_Pad", "Micro_Erector", "Micro_Pivot", "Micro_Rope",
                    "V2_Pad", "V2_Erector", "V2_Pivot", "V2_Rope",
                    "Strong_Pad", "Strong_Erector", "Strong_Pivot", "Strong_Rope",
                    "Huge_Pad", "Huge_Erector", "Huge_Pivot", "Huge_Rope",
                    "Atlas_Pad", "Atlas_Erector", "Atlas_Pivot", "Atlas_Rope");

    public static final ResourceLocation MISSILE_PAD_TEXTURE = texture("silo");
    public static final ResourceLocation MISSILE_PAD_RUSTED_TEXTURE = texture("silo_rusted");
    public static final ResourceLocation MISSILE_ERECTOR_TEXTURE = texture("pad");
    public static final ResourceLocation MISSILE_ERECTOR_MICRO_TEXTURE = texture("erector_micro");
    public static final ResourceLocation MISSILE_ERECTOR_V2_TEXTURE = texture("erector_v2");
    public static final ResourceLocation MISSILE_ERECTOR_STRONG_TEXTURE = texture("erector_strong");
    public static final ResourceLocation MISSILE_ERECTOR_HUGE_TEXTURE = texture("erector_huge");
    public static final ResourceLocation MISSILE_ERECTOR_ATLAS_TEXTURE = texture("erector_atlas");
    public static final ResourceLocation MISSILE_ERECTOR_ABM_TEXTURE = texture("erector_abm");
    public static final ResourceLocation MISSILE_ASSEMBLY_TEXTURE = texture("missile_assembly");
    public static final ResourceLocation STRUT_TEXTURE = texture("strut");
    public static final ResourceLocation COMPACT_LAUNCHER_TEXTURE = texture("compact_launcher");
    public static final ResourceLocation LAUNCH_TABLE_BASE_TEXTURE = texture("launch_table");
    public static final ResourceLocation LAUNCH_TABLE_LARGE_PAD_TEXTURE = texture("launch_table_large_pad");
    public static final ResourceLocation LAUNCH_TABLE_SMALL_PAD_TEXTURE = texture("launch_table_small_pad");
    public static final ResourceLocation LAUNCH_TABLE_LARGE_SCAFFOLD_BASE_TEXTURE = texture("launch_table_large_scaffold_base");
    public static final ResourceLocation LAUNCH_TABLE_LARGE_SCAFFOLD_CONNECTOR_TEXTURE = texture("launch_table_large_scaffold_connector");
    public static final ResourceLocation LAUNCH_TABLE_SMALL_SCAFFOLD_BASE_TEXTURE = texture("launch_table_small_scaffold_base");
    public static final ResourceLocation LAUNCH_TABLE_SMALL_SCAFFOLD_CONNECTOR_TEXTURE = texture("launch_table_small_scaffold_connector");
    public static final ResourceLocation SOYUZ_LAUNCHER_LEG_TEXTURE = texture("soyuz_launcher_legs");
    public static final ResourceLocation SOYUZ_LAUNCHER_TABLE_TEXTURE = texture("soyuz_launcher_table");
    public static final ResourceLocation SOYUZ_LAUNCHER_TOWER_BASE_TEXTURE = texture("soyuz_launcher_tower_base");
    public static final ResourceLocation SOYUZ_LAUNCHER_TOWER_TEXTURE = texture("soyuz_launcher_tower");
    public static final ResourceLocation SOYUZ_LAUNCHER_SUPPORT_BASE_TEXTURE = texture("soyuz_launcher_support_base");
    public static final ResourceLocation SOYUZ_LAUNCHER_SUPPORT_TEXTURE = texture("soyuz_launcher_support");

    public static final SoyuzLauncherStatePlan SOYUZ_LAUNCHER_STATE = new SoyuzLauncherStatePlan(false, true);

    public static ObjModelPart part(String name) {
        return ObjModelLibrary.blockPart("launch_table/" + name, RenderType.cutout());
    }

    public static LegacyWavefrontModel legacyModel(String name) {
        return legacyModel(name, name);
    }

    public static LegacyWavefrontModel legacyModel(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                legacyModelLocation(modelName),
                texture(textureName));
    }

    private static ResourceLocation legacyModelLocation(String modelName) {
        String path = switch (modelName) {
            case "launch_pad_silo", "launch_pad_erector" -> "models/weapons/" + modelName + ".obj";
            case "missile_assembly", "strut", "compact_launcher" -> "models/" + modelName + ".obj";
            default -> "models/launch_table/" + modelName + ".obj";
        };
        return new ResourceLocation(HbmNtm.MOD_ID, path);
    }

    public static ResourceLocation texture(String name) {
        return switch (name) {
            case "silo", "silo_rusted", "pad", "erector_micro", "erector_v2", "erector_strong",
                    "erector_huge", "erector_atlas", "erector_abm" -> modelTexture("launchpad/" + name);
            case "missile_assembly", "strut", "compact_launcher" -> modelTexture(name);
            case "launch_table", "launch_table_large_pad", "launch_table_small_pad",
                    "launch_table_large_scaffold_base", "launch_table_large_scaffold_connector",
                    "launch_table_small_scaffold_base", "launch_table_small_scaffold_connector" ->
                    modelTexture("missile_parts/" + name);
            case "soyuz_launcher_legs" -> modelTexture("soyuz_launcher/launcher_leg");
            case "soyuz_launcher_table" -> modelTexture("soyuz_launcher/launcher_table");
            case "soyuz_launcher_tower_base" -> modelTexture("soyuz_launcher/launcher_tower_base");
            case "soyuz_launcher_tower" -> modelTexture("soyuz_launcher/launcher_tower");
            case "soyuz_launcher_support_base" -> modelTexture("soyuz_launcher/launcher_support_base");
            case "soyuz_launcher_support" -> modelTexture("soyuz_launcher/launcher_support");
            default -> new ResourceLocation(HbmNtm.MOD_ID, "textures/block/launch_table/" + name + ".png");
        };
    }

    private static ResourceLocation modelTexture(String path) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/" + path + ".png");
    }

    public static List<SoyuzLauncherPartPlan> soyuzLauncherPlan(float rotation) {
        return soyuzLauncherRenderPlan(rotation).parts();
    }

    public static SoyuzLauncherRenderPlan soyuzLauncherRenderPlan(float rotation) {
        List<SoyuzLauncherPartPlan> parts = List.of(
                fixed(SOYUZ_LAUNCHER_LEGS_LEGACY, SOYUZ_LAUNCHER_LEG_TEXTURE),
                fixed(SOYUZ_LAUNCHER_TABLE_LEGACY, SOYUZ_LAUNCHER_TABLE_TEXTURE),
                fixed(SOYUZ_LAUNCHER_TOWER_BASE_LEGACY, SOYUZ_LAUNCHER_TOWER_BASE_TEXTURE),
                moving(SOYUZ_LAUNCHER_TOWER_LEGACY, SOYUZ_LAUNCHER_TOWER_TEXTURE, 5.5D, 5.5D, rotation, false),
                fixed(SOYUZ_LAUNCHER_SUPPORT_BASE_LEGACY, SOYUZ_LAUNCHER_SUPPORT_BASE_TEXTURE),
                moving(SOYUZ_LAUNCHER_SUPPORT_LEGACY, SOYUZ_LAUNCHER_SUPPORT_TEXTURE, 5.5D, -6.5D, rotation, true));
        return new SoyuzLauncherRenderPlan(rotation, SOYUZ_LAUNCHER_STATE, parts);
    }

    public static void renderMissileErectorPart(String partName, ResourceLocation texture, ObjRenderContext context) {
        LegacyWavefrontModel.SelectionHandle handle = missileErectorHandle(partName);
        if (handle != null) {
            MISSILE_ERECTOR.renderOnlyInCallOrder(texture, context, handle);
            return;
        }
        MISSILE_ERECTOR.renderPart(partName, texture, context);
    }

    public static void renderSoyuzLauncher(float rotation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        for (SoyuzLauncherPartPlan plan : soyuzLauncherRenderPlan(rotation).parts()) {
            poseStack.pushPose();
            if (plan.rotates()) {
                poseStack.translate(0.0D, plan.pivotY(), plan.pivotZ());
                poseStack.mulPose((plan.negativeXAxis() ? Axis.XN : Axis.XP).rotationDegrees(plan.rotationDegrees()));
                poseStack.translate(0.0D, -plan.pivotY(), -plan.pivotZ());
            }
            plan.model().renderAll(plan.texture(), poseStack, buffer, packedLight, packedOverlay);
            poseStack.popPose();
        }
    }

    private static SoyuzLauncherPartPlan fixed(LegacyWavefrontModel model, ResourceLocation texture) {
        return new SoyuzLauncherPartPlan(model, texture, 0.0D, 0.0D, 0.0F, false);
    }

    private static SoyuzLauncherPartPlan moving(LegacyWavefrontModel model, ResourceLocation texture, double pivotY,
            double pivotZ, float rotationDegrees, boolean negativeXAxis) {
        return new SoyuzLauncherPartPlan(model, texture, pivotY, pivotZ, rotationDegrees, negativeXAxis);
    }

    private static LegacyWavefrontModel.SelectionHandle missileErectorHandle(String partName) {
        return MISSILE_ERECTOR_HANDLES.get(partName);
    }

    private static Map<String, LegacyWavefrontModel.SelectionHandle> missileErectorHandles(String... partNames) {
        Map<String, LegacyWavefrontModel.SelectionHandle> handles = new HashMap<>();
        for (String partName : partNames) {
            handles.put(partName, MISSILE_ERECTOR.prepareRenderOnlyInCallOrder(partName));
        }
        return Collections.unmodifiableMap(handles);
    }

    private ObjLaunchModels() {
    }

    public record SoyuzLauncherPartPlan(
            LegacyWavefrontModel model,
            ResourceLocation texture,
            double pivotY,
            double pivotZ,
            float rotationDegrees,
            boolean negativeXAxis) {
        public boolean rotates() {
            return rotationDegrees != 0.0F || pivotY != 0.0D || pivotZ != 0.0D;
        }

        public double axisX() {
            return negativeXAxis ? -1.0D : 1.0D;
        }
    }

    public record SoyuzLauncherRenderPlan(float rotationDegrees, SoyuzLauncherStatePlan state,
                                          List<SoyuzLauncherPartPlan> parts) {
    }

    public record SoyuzLauncherStatePlan(boolean cullEnabled, boolean restoreCullEnabled) {
    }
}
