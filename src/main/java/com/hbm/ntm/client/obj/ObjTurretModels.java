package com.hbm.ntm.client.obj;

import com.hbm.ntm.HbmNtm;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public final class ObjTurretModels {
    public static final LegacyWavefrontModel CHEKHOV = model("turret_chekhov", "chekhov").asVBO();
    public static final LegacyWavefrontModel JEREMY = model("turret_jeremy", "jeremy").asVBO();
    public static final LegacyWavefrontModel TAUON = model("turret_tauon", "tauon").asVBO();
    public static final LegacyWavefrontModel RICHARD = model("turret_richard", "richard").asVBO();
    public static final LegacyWavefrontModel HOWARD = model("turret_howard", "howard").asVBO();
    public static final LegacyWavefrontModel HOWARD_DAMAGED = model("turret_howard_damaged", "rusted/howard").asVBO();
    public static final LegacyWavefrontModel MAXWELL = model("turret_microwave", "maxwell").asVBO();
    public static final LegacyWavefrontModel FRITZ = model("turret_fritz", "fritz").asVBO();
    public static final LegacyWavefrontModel ARTY = model("turret_arty", "arty").asVBO();
    public static final LegacyWavefrontModel HIMARS = model("turret_himars", "himars").asVBO();
    public static final LegacyWavefrontModel SENTRY = model("turret_sentry", "sentry").asVBO();
    private static final LegacyWavefrontModel.SelectionHandle CHEKHOV_BASE = CHEKHOV.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle CHEKHOV_CARRIAGE = CHEKHOV.prepareRenderOnlyInCallOrder("Carriage");
    private static final LegacyWavefrontModel.SelectionHandle CHEKHOV_BODY = CHEKHOV.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle CHEKHOV_BARRELS = CHEKHOV.prepareRenderOnlyInCallOrder("Barrels");
    private static final LegacyWavefrontModel.SelectionHandle CHEKHOV_CONNECTORS = CHEKHOV.prepareRenderOnlyInCallOrder("Connectors");
    private static final LegacyWavefrontModel.SelectionHandle JEREMY_GUN = JEREMY.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle TAUON_CANNON = TAUON.prepareRenderOnlyInCallOrder("Cannon");
    private static final LegacyWavefrontModel.SelectionHandle TAUON_ROTOR = TAUON.prepareRenderOnlyInCallOrder("Rotor");
    private static final LegacyWavefrontModel.SelectionHandle RICHARD_LAUNCHER = RICHARD.prepareRenderOnlyInCallOrder("Launcher");
    private static final LegacyWavefrontModel.SelectionHandle RICHARD_MISSILE_LOADED = RICHARD.prepareRenderOnlyInCallOrder("MissileLoaded");
    private static final LegacyWavefrontModel.SelectionHandle HOWARD_CARRIAGE = HOWARD.prepareRenderOnlyInCallOrder("Carriage");
    private static final LegacyWavefrontModel.SelectionHandle HOWARD_BODY = HOWARD.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle HOWARD_BARRELS_TOP = HOWARD.prepareRenderOnlyInCallOrder("BarrelsTop");
    private static final LegacyWavefrontModel.SelectionHandle HOWARD_BARRELS_BOTTOM = HOWARD.prepareRenderOnlyInCallOrder("BarrelsBottom");
    private static final LegacyWavefrontModel.SelectionHandle HOWARD_DAMAGED_CARRIAGE = HOWARD_DAMAGED.prepareRenderOnlyInCallOrder("Carriage");
    private static final LegacyWavefrontModel.SelectionHandle HOWARD_DAMAGED_BODY = HOWARD_DAMAGED.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle HOWARD_DAMAGED_BARRELS_TOP = HOWARD_DAMAGED.prepareRenderOnlyInCallOrder("BarrelsTop");
    private static final LegacyWavefrontModel.SelectionHandle HOWARD_DAMAGED_BARRELS_BOTTOM = HOWARD_DAMAGED.prepareRenderOnlyInCallOrder("BarrelsBottom");
    private static final LegacyWavefrontModel.SelectionHandle MAXWELL_MICROWAVE = MAXWELL.prepareRenderOnlyInCallOrder("Microwave");
    private static final LegacyWavefrontModel.SelectionHandle FRITZ_GUN = FRITZ.prepareRenderOnlyInCallOrder("Gun");
    private static final LegacyWavefrontModel.SelectionHandle ARTY_BASE = ARTY.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle ARTY_CARRIAGE = ARTY.prepareRenderOnlyInCallOrder("Carriage");
    private static final LegacyWavefrontModel.SelectionHandle ARTY_CANNON = ARTY.prepareRenderOnlyInCallOrder("Cannon");
    private static final LegacyWavefrontModel.SelectionHandle ARTY_BARREL = ARTY.prepareRenderOnlyInCallOrder("Barrel");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_CARRIAGE = HIMARS.prepareRenderOnlyInCallOrder("Carriage");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_LAUNCHER = HIMARS.prepareRenderOnlyInCallOrder("Launcher");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_CRANE = HIMARS.prepareRenderOnlyInCallOrder("Crane");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_TUBE_STANDARD = HIMARS.prepareRenderOnlyInCallOrder("TubeStandard");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_ROCKET_STANDARD = HIMARS.prepareRenderOnlyInCallOrder("RocketStandard");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_CAP_STANDARD_1 = HIMARS.prepareRenderOnlyInCallOrder("CapStandard1");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_CAP_STANDARD_2 = HIMARS.prepareRenderOnlyInCallOrder("CapStandard2");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_CAP_STANDARD_3 = HIMARS.prepareRenderOnlyInCallOrder("CapStandard3");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_CAP_STANDARD_4 = HIMARS.prepareRenderOnlyInCallOrder("CapStandard4");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_CAP_STANDARD_5 = HIMARS.prepareRenderOnlyInCallOrder("CapStandard5");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_CAP_STANDARD_6 = HIMARS.prepareRenderOnlyInCallOrder("CapStandard6");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_TUBE_SINGLE = HIMARS.prepareRenderOnlyInCallOrder("TubeSingle");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_ROCKET_SINGLE = HIMARS.prepareRenderOnlyInCallOrder("RocketSingle");
    private static final LegacyWavefrontModel.SelectionHandle HIMARS_CAP_SINGLE = HIMARS.prepareRenderOnlyInCallOrder("CapSingle");
    private static final LegacyWavefrontModel.SelectionHandle SENTRY_BASE = SENTRY.prepareRenderOnlyInCallOrder("Base");
    private static final LegacyWavefrontModel.SelectionHandle SENTRY_PIVOT = SENTRY.prepareRenderOnlyInCallOrder("Pivot");
    private static final LegacyWavefrontModel.SelectionHandle SENTRY_BODY = SENTRY.prepareRenderOnlyInCallOrder("Body");
    private static final LegacyWavefrontModel.SelectionHandle SENTRY_DRUM = SENTRY.prepareRenderOnlyInCallOrder("Drum");
    private static final LegacyWavefrontModel.SelectionHandle SENTRY_BARREL_L = SENTRY.prepareRenderOnlyInCallOrder("BarrelL");
    private static final LegacyWavefrontModel.SelectionHandle SENTRY_BARREL_R = SENTRY.prepareRenderOnlyInCallOrder("BarrelR");

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
    public static final ResourceLocation BASE_RUSTED_TEXTURE = texture("rusted/base");
    public static final ResourceLocation CARRIAGE_CIWS_RUSTED_TEXTURE = texture("rusted/carriage_ciws");
    public static final ResourceLocation HOWARD_RUSTED_TEXTURE = texture("rusted/howard");
    public static final ResourceLocation HOWARD_BARRELS_RUSTED_TEXTURE = texture("rusted/howard_barrels");

    public static LegacyWavefrontModel model(String modelName, String textureName) {
        return new LegacyWavefrontModel(
                new ResourceLocation(HbmNtm.MOD_ID, "models/turrets/" + modelName + ".obj"),
                texture(textureName));
    }

    public static ResourceLocation texture(String name) {
        return new ResourceLocation(HbmNtm.MOD_ID, "textures/models/turrets/" + name + ".png");
    }

    public static void renderPart(LegacyWavefrontModel model, String partName, ResourceLocation texture,
            PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        PreparedPart prepared = preparedPart(model, partName);
        if (prepared != null) {
            ObjRenderContext context = new ObjRenderContext(poseStack, buffer, null, packedLight, packedOverlay);
            prepared.model().renderOnlyInCallOrder(texture, context, prepared.selection());
            return;
        }
        model.renderPart(partName, texture, poseStack, buffer, packedLight, packedOverlay);
    }

    private static PreparedPart preparedPart(LegacyWavefrontModel model, String partName) {
        if (model == null || partName == null) {
            return null;
        }
        if (sameModel(model, CHEKHOV)) {
            return prepared(CHEKHOV, switch (partName) {
                case "Base" -> CHEKHOV_BASE;
                case "Carriage" -> CHEKHOV_CARRIAGE;
                case "Body" -> CHEKHOV_BODY;
                case "Barrels" -> CHEKHOV_BARRELS;
                case "Connectors" -> CHEKHOV_CONNECTORS;
                default -> null;
            });
        }
        if (sameModel(model, JEREMY)) {
            return prepared(JEREMY, "Gun".equals(partName) ? JEREMY_GUN : null);
        }
        if (sameModel(model, TAUON)) {
            return prepared(TAUON, switch (partName) {
                case "Cannon" -> TAUON_CANNON;
                case "Rotor" -> TAUON_ROTOR;
                default -> null;
            });
        }
        if (sameModel(model, RICHARD)) {
            return prepared(RICHARD, switch (partName) {
                case "Launcher" -> RICHARD_LAUNCHER;
                case "MissileLoaded" -> RICHARD_MISSILE_LOADED;
                default -> null;
            });
        }
        if (sameModel(model, HOWARD)) {
            return prepared(HOWARD, howardHandle(partName));
        }
        if (sameModel(model, HOWARD_DAMAGED)) {
            return prepared(HOWARD_DAMAGED, switch (partName) {
                case "Carriage" -> HOWARD_DAMAGED_CARRIAGE;
                case "Body" -> HOWARD_DAMAGED_BODY;
                case "BarrelsTop" -> HOWARD_DAMAGED_BARRELS_TOP;
                case "BarrelsBottom" -> HOWARD_DAMAGED_BARRELS_BOTTOM;
                default -> null;
            });
        }
        if (sameModel(model, MAXWELL)) {
            return prepared(MAXWELL, "Microwave".equals(partName) ? MAXWELL_MICROWAVE : null);
        }
        if (sameModel(model, FRITZ)) {
            return prepared(FRITZ, "Gun".equals(partName) ? FRITZ_GUN : null);
        }
        if (sameModel(model, ARTY)) {
            return prepared(ARTY, switch (partName) {
                case "Base" -> ARTY_BASE;
                case "Carriage" -> ARTY_CARRIAGE;
                case "Cannon" -> ARTY_CANNON;
                case "Barrel" -> ARTY_BARREL;
                default -> null;
            });
        }
        if (sameModel(model, HIMARS)) {
            return prepared(HIMARS, himarsHandle(partName));
        }
        if (sameModel(model, SENTRY)) {
            return prepared(SENTRY, switch (partName) {
                case "Base" -> SENTRY_BASE;
                case "Pivot" -> SENTRY_PIVOT;
                case "Body" -> SENTRY_BODY;
                case "Drum" -> SENTRY_DRUM;
                case "BarrelL" -> SENTRY_BARREL_L;
                case "BarrelR" -> SENTRY_BARREL_R;
                default -> null;
            });
        }
        return null;
    }

    private static LegacyWavefrontModel.SelectionHandle howardHandle(String partName) {
        return switch (partName) {
            case "Carriage" -> HOWARD_CARRIAGE;
            case "Body" -> HOWARD_BODY;
            case "BarrelsTop" -> HOWARD_BARRELS_TOP;
            case "BarrelsBottom" -> HOWARD_BARRELS_BOTTOM;
            default -> null;
        };
    }

    private static LegacyWavefrontModel.SelectionHandle himarsHandle(String partName) {
        return switch (partName) {
            case "Carriage" -> HIMARS_CARRIAGE;
            case "Launcher" -> HIMARS_LAUNCHER;
            case "Crane" -> HIMARS_CRANE;
            case "TubeStandard" -> HIMARS_TUBE_STANDARD;
            case "RocketStandard" -> HIMARS_ROCKET_STANDARD;
            case "CapStandard1" -> HIMARS_CAP_STANDARD_1;
            case "CapStandard2" -> HIMARS_CAP_STANDARD_2;
            case "CapStandard3" -> HIMARS_CAP_STANDARD_3;
            case "CapStandard4" -> HIMARS_CAP_STANDARD_4;
            case "CapStandard5" -> HIMARS_CAP_STANDARD_5;
            case "CapStandard6" -> HIMARS_CAP_STANDARD_6;
            case "TubeSingle" -> HIMARS_TUBE_SINGLE;
            case "RocketSingle" -> HIMARS_ROCKET_SINGLE;
            case "CapSingle" -> HIMARS_CAP_SINGLE;
            default -> null;
        };
    }

    private static PreparedPart prepared(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle selection) {
        return selection == null ? null : new PreparedPart(model, selection);
    }

    private static boolean sameModel(LegacyWavefrontModel model, LegacyWavefrontModel expected) {
        return model == expected || model.modelLocation().equals(expected.modelLocation());
    }

    private record PreparedPart(LegacyWavefrontModel model, LegacyWavefrontModel.SelectionHandle selection) {
    }

    private ObjTurretModels() {
    }
}
