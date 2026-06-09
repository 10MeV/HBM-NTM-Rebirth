package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

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
    public static final LegacyWavefrontModel MISSILE_PAD = legacyModel("launch_pad_silo", "silo");
    public static final LegacyWavefrontModel MISSILE_ERECTOR = legacyModel("launch_pad_erector", "pad").asVBO();
    public static final LegacyWavefrontModel MISSILE_ASSEMBLY = legacyModel("missile_assembly");
    public static final LegacyWavefrontModel STRUT = legacyModel("strut");
    public static final LegacyWavefrontModel COMPACT_LAUNCHER = legacyModel("compact_launcher");
    public static final LegacyWavefrontModel LAUNCH_TABLE_BASE_LEGACY = legacyModel("launch_table_base", "launch_table");
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_PAD_LEGACY = legacyModel("launch_table_large_pad");
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_PAD_LEGACY = legacyModel("launch_table_small_pad");
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_SCAFFOLD_BASE_LEGACY = legacyModel("launch_table_large_scaffold_base");
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_SCAFFOLD_CONNECTOR_LEGACY = legacyModel("launch_table_large_scaffold_connector");
    public static final LegacyWavefrontModel LAUNCH_TABLE_LARGE_SCAFFOLD_EMPTY_LEGACY = legacyModel("launch_table_large_scaffold_empty", "launch_table_large_scaffold_base");
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_SCAFFOLD_BASE_LEGACY = legacyModel("launch_table_small_scaffold_base");
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_SCAFFOLD_CONNECTOR_LEGACY = legacyModel("launch_table_small_scaffold_connector");
    public static final LegacyWavefrontModel LAUNCH_TABLE_SMALL_SCAFFOLD_EMPTY_LEGACY = legacyModel("launch_table_small_scaffold_empty", "launch_table_small_scaffold_base");

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

    public static ObjModelPart part(String name) {
        return ObjModelLibrary.blockPart("launch_table/" + name, RenderType.cutout());
    }

    public static LegacyWavefrontModel legacyModel(String name) {
        return legacyModel(name, name);
    }

    public static LegacyWavefrontModel legacyModel(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/launch_table/" + modelName + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/launch_table/" + name + ".png");
    }

    public static List<SoyuzLauncherPartPlan> soyuzLauncherPlan(float rotation) {
        return List.of(
                fixed(SOYUZ_LAUNCHER_LEGS_LEGACY, SOYUZ_LAUNCHER_LEG_TEXTURE),
                fixed(SOYUZ_LAUNCHER_TABLE_LEGACY, SOYUZ_LAUNCHER_TABLE_TEXTURE),
                fixed(SOYUZ_LAUNCHER_TOWER_BASE_LEGACY, SOYUZ_LAUNCHER_TOWER_BASE_TEXTURE),
                moving(SOYUZ_LAUNCHER_TOWER_LEGACY, SOYUZ_LAUNCHER_TOWER_TEXTURE, 5.5D, 5.5D, rotation, false),
                fixed(SOYUZ_LAUNCHER_SUPPORT_BASE_LEGACY, SOYUZ_LAUNCHER_SUPPORT_BASE_TEXTURE),
                moving(SOYUZ_LAUNCHER_SUPPORT_LEGACY, SOYUZ_LAUNCHER_SUPPORT_TEXTURE, 5.5D, -6.5D, rotation, true));
    }

    public static void renderSoyuzLauncher(float rotation, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        for (SoyuzLauncherPartPlan plan : soyuzLauncherPlan(rotation)) {
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
    }
}
