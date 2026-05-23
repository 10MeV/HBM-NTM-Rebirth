package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

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

    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_LEGS_LEGACY = legacyModel("soyuz_launcher_legs").noSmooth();
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_TABLE_LEGACY = legacyModel("soyuz_launcher_table").noSmooth();
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_TOWER_BASE_LEGACY = legacyModel("soyuz_launcher_tower_base").noSmooth();
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_TOWER_LEGACY = legacyModel("soyuz_launcher_tower").noSmooth();
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_SUPPORT_BASE_LEGACY = legacyModel("soyuz_launcher_support_base").noSmooth();
    public static final LegacyWavefrontModel SOYUZ_LAUNCHER_SUPPORT_LEGACY = legacyModel("soyuz_launcher_support").noSmooth();

    public static ObjModelPart part(String name) {
        return ObjModelLibrary.blockPart("launch_table/" + name, RenderType.cutout());
    }

    public static LegacyWavefrontModel legacyModel(String name) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/launch_table/" + name + ".obj"),
                texture(name));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/launch_table/" + name + ".png");
    }

    private ObjLaunchModels() {
    }
}
