package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjUtilityModels {
    public static final LegacyWavefrontModel GEIGER_COUNTER = model("geiger_counter", "geiger").asVBO();
    public static final LegacyWavefrontModel FORCEFIELD_TOP = model("forcefield_top");
    public static final LegacyWavefrontModel SAT_FOEQ_BURNING = model("sat_foeq_burning");
    public static final LegacyWavefrontModel SAT_FOEQ_FIRE = model("sat_foeq_fire", "sat_foeq_burning");
    public static final LegacyWavefrontModel SAT_DOCK = model("sat_dock");
    public static final LegacyWavefrontModel TESLA = model("tesla");
    public static final LegacyWavefrontModel FILE_CABINET = model("file_cabinet");

    public static final ResourceLocation GEIGER_TEXTURE = texture("geiger");
    public static final ResourceLocation FORCEFIELD_BASE_TEXTURE = texture("forcefield_base");
    public static final ResourceLocation FORCEFIELD_TOP_TEXTURE = texture("forcefield_top");
    public static final ResourceLocation SAT_FOEQ_BURNING_TEXTURE = texture("sat_foeq_burning");
    public static final ResourceLocation SAT_DOCK_TEXTURE = texture("sat_dock");
    public static final ResourceLocation TESLA_TEXTURE = texture("tesla");
    public static final ResourceLocation FILE_CABINET_TEXTURE = texture("file_cabinet");
    public static final ResourceLocation FILE_CABINET_STEEL_TEXTURE = texture("file_cabinet_steel");

    public static LegacyWavefrontModel model(String name) {
        return model(name, name);
    }

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/utility/" + modelName + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/utility/" + name + ".png");
    }

    private ObjUtilityModels() {
    }
}
