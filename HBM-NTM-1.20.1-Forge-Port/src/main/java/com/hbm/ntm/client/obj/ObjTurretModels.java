package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import net.minecraft.resources.ResourceLocation;

public final class ObjTurretModels {
    public static final LegacyWavefrontModel CHEKHOV = model("turret_chekhov", "chekhov").asVBO();
    public static final LegacyWavefrontModel JEREMY = model("turret_jeremy", "jeremy").asVBO();
    public static final LegacyWavefrontModel TAUON = model("turret_tauon", "tauon").asVBO();
    public static final LegacyWavefrontModel RICHARD = model("turret_richard", "richard").asVBO();
    public static final LegacyWavefrontModel HOWARD = model("turret_howard", "howard").asVBO();
    public static final LegacyWavefrontModel HOWARD_DAMAGED = model("turret_howard_damaged", "rusted_howard").asVBO();
    public static final LegacyWavefrontModel MAXWELL = model("turret_microwave", "maxwell").asVBO();
    public static final LegacyWavefrontModel FRITZ = model("turret_fritz", "fritz").asVBO();
    public static final LegacyWavefrontModel ARTY = model("turret_arty", "arty").asVBO();
    public static final LegacyWavefrontModel HIMARS = model("turret_himars", "himars").asVBO();
    public static final LegacyWavefrontModel SENTRY = model("turret_sentry", "sentry").asVBO();

    public static final ResourceLocation BASE_TEXTURE = texture("base");
    public static final ResourceLocation BASE_FRIENDLY_TEXTURE = texture("base_friendly");
    public static final ResourceLocation CARRIAGE_TEXTURE = texture("carriage");
    public static final ResourceLocation CARRIAGE_CIWS_TEXTURE = texture("carriage_ciws");
    public static final ResourceLocation CARRIAGE_FRIENDLY_TEXTURE = texture("carriage_friendly");
    public static final ResourceLocation BASE_ZACH_TEXTURE = texture("base_zach");
    public static final ResourceLocation CARRIAGE_ZACH_TEXTURE = texture("carriage_zach");
    public static final ResourceLocation CONNECTOR_TEXTURE = texture("connector");
    public static final ResourceLocation CHEKHOV_TEXTURE = texture("chekhov");
    public static final ResourceLocation CHEKHOV_BARRELS_TEXTURE = texture("chekhov_barrels");
    public static final ResourceLocation JEREMY_TEXTURE = texture("jeremy");
    public static final ResourceLocation TAUON_TEXTURE = texture("tauon");
    public static final ResourceLocation RICHARD_TEXTURE = texture("richard");
    public static final ResourceLocation HOWARD_TEXTURE = texture("howard");
    public static final ResourceLocation HOWARD_BARRELS_TEXTURE = texture("howard_barrels");
    public static final ResourceLocation MAXWELL_TEXTURE = texture("maxwell");
    public static final ResourceLocation FRITZ_TEXTURE = texture("fritz");
    public static final ResourceLocation ARTY_TEXTURE = texture("arty");
    public static final ResourceLocation HIMARS_TEXTURE = texture("himars");
    public static final ResourceLocation SENTRY_TEXTURE = texture("sentry");
    public static final ResourceLocation SENTRY_DAMAGED_TEXTURE = texture("sentry_damaged");
    public static final ResourceLocation ZACH_TEXTURE = texture("zach");
    public static final ResourceLocation BASE_RUSTED_TEXTURE = texture("rusted_base");
    public static final ResourceLocation CARRIAGE_CIWS_RUSTED_TEXTURE = texture("rusted_carriage_ciws");
    public static final ResourceLocation HOWARD_RUSTED_TEXTURE = texture("rusted_howard");
    public static final ResourceLocation HOWARD_BARRELS_RUSTED_TEXTURE = texture("rusted_howard_barrels");

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/block/turrets/" + modelName + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/block/turrets/" + name + ".png");
    }

    private ObjTurretModels() {
    }
}
